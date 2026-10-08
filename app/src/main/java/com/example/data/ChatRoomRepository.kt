package com.example.data

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRoomRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val supabaseService: SupabaseAccountService = SupabaseAccountService()
) {
    private val roomsCollection = firestore.collection("chat_rooms")
    private val membersCollection = firestore.collection("members")
    private val userRolesCollection = firestore.collection("user_roles")
    private val fadfadaPostsCollection = firestore.collection("fadfada_posts")
    private val fadfadaAdminIdentitiesCollection = firestore.collection("fadfada_admin_identities")
    private val pushNotificationsCollection = firestore.collection("push_notifications")
    private val paymentReceiptsCollection = firestore.collection("payment_receipts")
    private val giftTransactionsCollection = firestore.collection("gift_transactions")

    companion object {
        const val PRIMARY_APP_OWNER_EMAIL = "hamadanagy1979@gmail.com"
        const val APP_OWNER_EMAIL = PRIMARY_APP_OWNER_EMAIL
    }

    /**
     * Observes active chat rooms using Supabase Realtime as the primary real-time source
     * so all rooms are immediately visible to ALL users across devices and companion apps,
     * combined with Firestore rooms if signed in.
     */
    fun getChatRooms(): Flow<List<ChatRoomMetadata>> = observeChatRooms(null)

    fun observeChatRooms(creatorIdFilter: String? = null): Flow<List<ChatRoomMetadata>> {
        val supabaseRealtimeFlow = supabaseService.observeRealtimeRooms()
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser == null || currentUser.isAnonymous) {
            return supabaseRealtimeFlow
        }

        val firestoreFlow = callbackFlow {
            val subscription = roomsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val rooms = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(ChatRoomMetadata::class.java)
                        }
                        SupabaseRealtimeHub.updateRoomsSnapshot(rooms)
                        trySend(rooms)
                    }
                }
            awaitClose { subscription.remove() }
        }

        return combine(supabaseRealtimeFlow, firestoreFlow) { supRooms, fbRooms ->
            (supRooms + fbRooms)
                .distinctBy { it.roomId }
                .sortedByDescending { it.timestampMillis }
        }
    }

    /**
     * Observes ALL public member profiles synced with Supabase (`/members` where `supabaseSynced == true`)
     * so all online members and their unique `memberIdCode` are visible across the community.
     */
    fun observeAllSyncedMembers(): Flow<List<MemberProfile>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
            ?: return SupabaseRealtimeHub.realtimeMembers
        if (currentUser.isAnonymous) return SupabaseRealtimeHub.realtimeMembers

        return callbackFlow {
            val subscription = membersCollection
                .whereEqualTo("supabaseSynced", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(SupabaseRealtimeHub.realtimeMembers.value)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val profiles = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(MemberProfile::class.java)?.let { p ->
                                if (p.memberIdCode.isBlank()) {
                                    p.copy(memberIdCode = generateMemberIdCode(p.userId))
                                } else {
                                    p
                                }
                            }
                        }.sortedByDescending { it.updatedAt.seconds }
                        SupabaseRealtimeHub.updateMembersSnapshot(profiles)
                        trySend((profiles + SupabaseRealtimeHub.realtimeMembers.value).distinctBy { it.userId })
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    /**
     * Observes the current user's RBAC role assignment in `/user_roles/{userId}`.
     * Automatically grants `OWNER` role if email is `hamadanagy1979@gmail.com`.
     */
    fun observeCurrentUserRole(userId: String): Flow<UserRoleAssignment?> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous || userId.isBlank()) return emptyFlow()

        return callbackFlow {
            val subscription = userRolesCollection.document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    val docObj = snapshot?.toObject(UserRoleAssignment::class.java)
                    trySend(docObj)
                }
            awaitClose { subscription.remove() }
        }
    }

    /**
     * Observes all RBAC role assignments in `/user_roles` (accessible to App Owner and Admins).
     */
    fun observeAllUserRoles(): Flow<List<UserRoleAssignment>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous) return emptyFlow()

        return callbackFlow {
            val subscription = userRolesCollection
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull {
                            it.toObject(UserRoleAssignment::class.java)
                        }.sortedByDescending { it.updatedAt.seconds }
                        trySend(list)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    /**
     * Ensures the authenticated user's RBAC record is initialized in Firestore `/user_roles/{userId}`.
     * If the user's email matches `hamadanagy1979@gmail.com`, initializes them as `OWNER` with full permissions.
     */
    suspend fun ensureUserRoleInitialized(
        userId: String,
        email: String?,
        displayName: String
    ): UserRoleAssignment? {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return null
        if (currentUser.isAnonymous || currentUser.uid != userId) return null

        val memberCode = generateMemberIdCode(userId)
        val isOwnerEmail = email?.trim()?.equals(PRIMARY_APP_OWNER_EMAIL, ignoreCase = true) == true
        val targetRole = if (isOwnerEmail) "OWNER" else "MEMBER"
        val defaultPermissions = if (isOwnerEmail) {
            listOf(
                "MANAGE_USERS",
                "MANAGE_ROOMS",
                "MANAGE_ROLES",
                "MODERATE_VOICE",
                "MODERATE_CHAT",
                "VIEW_FINANCE",
                "RESOLVE_REPORTS",
                "EDIT_BALANCE",
                "DELETE_ROOMS",
                "ASSIGN_ADMIN",
                "SYSTEM_SETTINGS",
                "VIEW_AUDIT_LOGS"
            )
        } else {
            listOf("JOIN_ROOMS")
        }

        val existing = runCatching {
            userRolesCollection.document(userId).get().await().toObject(UserRoleAssignment::class.java)
        }.getOrNull()

        if (existing != null) {
            if (isOwnerEmail && existing.role != "OWNER") {
                val upgraded = existing.copy(
                    memberIdCode = existing.memberIdCode.ifBlank { memberCode },
                    displayName = displayName.ifBlank { existing.displayName },
                    role = "OWNER",
                    permissions = defaultPermissions,
                    assignedBy = PRIMARY_APP_OWNER_EMAIL,
                    updatedAt = Timestamp.now()
                )
                runCatching { userRolesCollection.document(userId).set(upgraded).await() }
                return upgraded
            }
            return existing
        }

        val assignment = UserRoleAssignment(
            userId = userId,
            memberIdCode = memberCode,
            displayName = displayName.ifBlank { if (isOwnerEmail) "صاحب التطبيق الأساسي 👑" else "عضو وَنَس" },
            role = targetRole,
            permissions = defaultPermissions,
            assignedBy = if (isOwnerEmail) PRIMARY_APP_OWNER_EMAIL else userId,
            updatedAt = Timestamp.now()
        )
        runCatching {
            userRolesCollection.document(userId).set(assignment).await()
        }
        return assignment
    }

    /**
     * Allows App Owner (`hamadanagy1979@gmail.com`) or `SUPER_ADMIN` to assign or update a member's RBAC role
     * and granular permissions in Firestore `/user_roles/{targetUserId}`.
     */
    suspend fun assignUserRoleInFirestore(
        targetUserId: String,
        targetMemberCode: String,
        targetDisplayName: String,
        roleName: String,
        permissions: List<String>
    ): Boolean {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return false
        if (currentUser.isAnonymous || targetUserId.isBlank()) return false

        val cleanId = targetUserId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val assignment = UserRoleAssignment(
            userId = cleanId,
            memberIdCode = targetMemberCode.ifBlank { generateMemberIdCode(cleanId) },
            displayName = targetDisplayName.ifBlank { "عضو وَنَس" }.take(80),
            role = roleName,
            permissions = permissions.take(20),
            assignedBy = (currentUser.email ?: currentUser.uid).take(128),
            updatedAt = Timestamp.now()
        )
        return runCatching {
            userRolesCollection.document(cleanId).set(assignment).await()
            true
        }.getOrDefault(false)
    }

    fun observeMemberProfile(userId: String): Flow<MemberProfile?> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous || currentUser.uid != userId) return emptyFlow()

        return callbackFlow {
            val subscription = membersCollection.document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    val profile = snapshot?.toObject(MemberProfile::class.java)?.let { p ->
                        if (p.memberIdCode.isBlank()) {
                            p.copy(memberIdCode = generateMemberIdCode(p.userId))
                        } else {
                            p
                        }
                    }
                    trySend(profile)
                }
            awaitClose { subscription.remove() }
        }
    }

    fun observeMemberChatHistory(userId: String): Flow<List<MemberChatHistoryEntry>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous || currentUser.uid != userId) return emptyFlow()

        return callbackFlow {
            val subscription = membersCollection.document(userId)
                .collection("chat_history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val entries = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(MemberChatHistoryEntry::class.java)
                        }
                        trySend(entries)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    fun observeRoomActiveMembers(roomId: String): Flow<List<RoomActiveMember>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous || roomId.isBlank()) return emptyFlow()

        return callbackFlow {
            val subscription = roomsCollection.document(roomId)
                .collection("active_members")
                .whereEqualTo("roomId", roomId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val members = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(RoomActiveMember::class.java)
                        }.sortedByDescending { it.joinedAt }
                        trySend(members)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    /**
     * Observes real-time 8-seat stage state using Supabase Realtime (`observeRealtimeRoomSeats`)
     * as the primary real-time source so all users see identical seats, speakers, host, and mic lock status.
     */
    fun observeRoomSeats(roomId: String): Flow<List<VoiceRoomSeatDocument>> {
        val supabaseSeatsFlow = supabaseService.observeRealtimeRoomSeats(roomId)
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser == null || currentUser.isAnonymous || roomId.isBlank()) {
            return supabaseSeatsFlow
        }

        val firestoreSeatsFlow = callbackFlow {
            val subscription = roomsCollection.document(roomId)
                .collection("seats")
                .whereEqualTo("roomId", roomId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val seats = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(VoiceRoomSeatDocument::class.java)
                        }.sortedBy { it.seatIndex }
                        if (seats.isNotEmpty()) {
                            SupabaseRealtimeHub.replaceRoomSeatsSnapshot(roomId, seats)
                        }
                        trySend(seats)
                    }
                }
            awaitClose { subscription.remove() }
        }

        return combine(supabaseSeatsFlow, firestoreSeatsFlow) { supSeats, fbSeats ->
            (supSeats + fbSeats)
                .distinctBy { it.seatIndex }
                .sortedBy { it.seatIndex }
        }
    }

    /**
     * Persists a seat state change in Supabase Realtime (primary) and Firestore so all users in the room see it immediately.
     */
    suspend fun upsertRoomSeatState(seatDoc: VoiceRoomSeatDocument): Boolean {
        if (seatDoc.roomId.isBlank() || seatDoc.seatId.isBlank()) return false

        val normalized = seatDoc.copy(
            occupantMemberCode = if (seatDoc.occupantUserId.isNotBlank() && seatDoc.occupantMemberCode.isBlank()) {
                generateMemberIdCode(seatDoc.occupantUserId)
            } else {
                seatDoc.occupantMemberCode
            },
            updatedAt = Timestamp.now()
        )
        supabaseService.upsertRoomSeat(normalized)

        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser != null && !currentUser.isAnonymous) {
            runCatching {
                roomsCollection.document(normalized.roomId)
                    .collection("seats")
                    .document(normalized.seatId)
                    .set(normalized)
                    .await()
            }
        }
        return true
    }

    /**
     * Observes real-time mic request queue for a room via Supabase Realtime + Firestore.
     */
    fun observeRoomMicRequests(roomId: String): Flow<List<RoomMicRequestDocument>> {
        val supabaseMicFlow = supabaseService.observeRealtimeRoomMicRequests(roomId)
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser == null || currentUser.isAnonymous || roomId.isBlank()) {
            return supabaseMicFlow
        }

        val firestoreMicFlow = callbackFlow {
            val subscription = roomsCollection.document(roomId)
                .collection("mic_requests")
                .whereEqualTo("roomId", roomId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val requests = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(RoomMicRequestDocument::class.java)
                        }.sortedBy { it.queueNumber }
                        if (requests.isNotEmpty()) {
                            SupabaseRealtimeHub.replaceRoomMicRequestsSnapshot(roomId, requests)
                        }
                        trySend(requests)
                    }
                }
            awaitClose { subscription.remove() }
        }

        return combine(supabaseMicFlow, firestoreMicFlow) { supReqs, fbReqs ->
            (supReqs + fbReqs)
                .distinctBy { it.requestId }
                .sortedBy { it.queueNumber }
        }
    }

    /**
     * Publishes or updates a live mic request in Supabase Realtime and `/chat_rooms/{roomId}/mic_requests/{requestId}`.
     */
    suspend fun upsertRoomMicRequest(requestDoc: RoomMicRequestDocument): Boolean {
        if (requestDoc.roomId.isBlank() || requestDoc.requestId.isBlank()) return false

        val normalized = requestDoc.copy(
            memberIdCode = requestDoc.memberIdCode.ifBlank { generateMemberIdCode(requestDoc.userId) },
            timestamp = Timestamp.now()
        )
        supabaseService.upsertRoomMicRequest(normalized)

        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser != null && !currentUser.isAnonymous) {
            runCatching {
                roomsCollection.document(normalized.roomId)
                    .collection("mic_requests")
                    .document(normalized.requestId)
                    .set(normalized)
                    .await()
            }
        }
        return true
    }

    suspend fun deleteRoomMicRequest(roomId: String, requestId: String): Boolean {
        if (roomId.isBlank() || requestId.isBlank()) return false
        supabaseService.deleteRoomMicRequestFromSupabase(roomId, requestId)
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser != null && !currentUser.isAnonymous) {
            runCatching {
                roomsCollection.document(roomId)
                    .collection("mic_requests")
                    .document(requestId)
                    .delete()
                    .await()
            }
        }
        return true
    }

    fun observeRoomPresenceEvents(roomId: String): Flow<List<RoomPresenceEvent>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous || roomId.isBlank()) return emptyFlow()

        return callbackFlow {
            val subscription = roomsCollection.document(roomId)
                .collection("presence_events")
                .whereEqualTo("roomId", roomId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val events = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(RoomPresenceEvent::class.java)
                        }.sortedByDescending { it.createdAt }.take(25)
                        trySend(events)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    /**
     * Observes public anonymous Fadfada (venting) posts in real time across Supabase and Firestore.
     * Regular users see `memberIdCode` (e.g., `WNS-482109`) without author's real name or email.
     */
    fun observeFadfadaPosts(): Flow<List<FadfadaPost>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser == null || currentUser.isAnonymous) {
            return SupabaseRealtimeHub.realtimeFadfadaPosts
        }

        return callbackFlow {
            val subscription = fadfadaPostsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(60)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(SupabaseRealtimeHub.realtimeFadfadaPosts.value)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val posts = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(FadfadaPost::class.java)?.let { post ->
                                if (post.memberIdCode.isBlank()) {
                                    post.copy(memberIdCode = generateMemberIdCode(post.authorId))
                                } else {
                                    post
                                }
                            }
                        }
                        SupabaseRealtimeHub.updateFadfadaSnapshot(posts)
                        trySend((posts + SupabaseRealtimeHub.realtimeFadfadaPosts.value).distinctBy { it.postId })
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    fun observeAnonymousFadfadaPosts(): Flow<List<AnonymousFadfadaPost>> = observeFadfadaPosts()

    fun observeFadfadaAdminIdentities(): Flow<Map<String, FadfadaAdminIdentity>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous) return emptyFlow()

        return callbackFlow {
            val subscription = fadfadaAdminIdentitiesCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(60)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyMap())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val map = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(FadfadaAdminIdentity::class.java)
                        }.associateBy { it.postId }
                        trySend(map)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun createFadfadaPost(
        content: String,
        moodTag: String,
        authorName: String,
        authorEmail: String,
        authorId: String? = null,
        customPostId: String? = null
    ): Result<FadfadaPost> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedUid = currentUser?.uid ?: authorId?.takeIf { it.isNotBlank() } ?: "wanas_member"
        val trimmedContent = content.trim().take(1000)
        require(trimmedContent.isNotEmpty()) { "يرجى إدخال نص الفضفضة" }

        val postId = customPostId?.takeIf { it.isNotBlank() }
            ?: "fadfada_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val now = Timestamp.now()
        val memberCode = generateMemberIdCode(resolvedUid)

        val publicPost = FadfadaPost(
            postId = postId,
            authorId = resolvedUid,
            memberIdCode = memberCode,
            content = trimmedContent,
            moodTag = moodTag.trim().ifEmpty { "💭 فضفضة عامة" }.take(50),
            heartsCount = 0,
            timestamp = now
        )

        val safeName = authorName.trim().ifEmpty {
            currentUser?.displayName?.trim()?.takeIf { it.isNotEmpty() } ?: "عضو وَنَس"
        }.take(80)
        val safeEmail = authorEmail.trim().ifEmpty {
            currentUser?.email?.trim()?.takeIf { it.isNotEmpty() } ?: "$resolvedUid@wanas.app"
        }.take(160)

        val adminIdentity = FadfadaAdminIdentity(
            postId = postId,
            authorId = resolvedUid,
            memberIdCode = memberCode,
            authorName = safeName,
            authorEmail = safeEmail,
            timestamp = now
        )

        val publicPostDoc = mapOf(
            "postId" to postId,
            "authorId" to resolvedUid,
            "memberIdCode" to memberCode,
            "content" to publicPost.content,
            "moodTag" to publicPost.moodTag,
            "heartsCount" to 0,
            "timestamp" to FieldValue.serverTimestamp()
        )

        val adminIdentityDoc = mapOf(
            "postId" to postId,
            "authorId" to resolvedUid,
            "memberIdCode" to memberCode,
            "authorName" to safeName,
            "authorEmail" to safeEmail,
            "timestamp" to FieldValue.serverTimestamp()
        )

        supabaseService.upsertFadfadaPost(publicPost)

        if (currentUser != null && !currentUser.isAnonymous) {
            val batch = firestore.batch()
            batch.set(fadfadaPostsCollection.document(postId), publicPostDoc)
            batch.set(fadfadaAdminIdentitiesCollection.document(postId), adminIdentityDoc)
            batch.commit().await()
        }
        publicPost
    }

    suspend fun createAnonymousFadfadaPost(
        content: String,
        moodTag: String,
        authorName: String,
        authorEmail: String,
        authorId: String? = null,
        customPostId: String? = null
    ): Result<AnonymousFadfadaPost> = createFadfadaPost(
        content = content,
        moodTag = moodTag,
        authorName = authorName,
        authorEmail = authorEmail,
        authorId = authorId,
        customPostId = customPostId
    )

    suspend fun supportFadfadaPostWithHeart(post: FadfadaPost) {
        if (post.postId.isBlank()) return
        val updated = post.copy(
            memberIdCode = post.memberIdCode.ifBlank { generateMemberIdCode(post.authorId) },
            heartsCount = post.heartsCount + 1
        )
        supabaseService.upsertFadfadaPost(updated)
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser != null && !currentUser.isAnonymous) {
            runCatching { fadfadaPostsCollection.document(post.postId).set(updated).await() }
        }
    }

    suspend fun sendHeartToFadfadaPost(postId: String, currentHearts: Int): Result<Unit> = runCatching {
        if (postId.isBlank()) return@runCatching
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser != null && !currentUser.isAnonymous) {
            val snap = fadfadaPostsCollection.document(postId).get().await()
            val existing = snap.toObject(FadfadaPost::class.java)
            if (existing != null) {
                val updated = existing.copy(heartsCount = existing.heartsCount + 1)
                fadfadaPostsCollection.document(postId).set(updated).await()
                supabaseService.upsertFadfadaPost(updated)
            }
        }
    }

    suspend fun deleteFadfadaPost(postId: String): Result<Unit> = runCatching {
        if (postId.isBlank()) return@runCatching
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser != null && !currentUser.isAnonymous) {
            runCatching { fadfadaPostsCollection.document(postId).delete().await() }
            runCatching { fadfadaAdminIdentitiesCollection.document(postId).delete().await() }
        }
    }

    suspend fun getFadfadaAdminIdentity(postId: String): Result<FadfadaAdminIdentity?> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val isOwnerEmail = currentUser?.email?.trim()?.equals(PRIMARY_APP_OWNER_EMAIL, ignoreCase = true) == true
        if (currentUser == null || currentUser.isAnonymous || !isOwnerEmail) {
            throw com.google.firebase.firestore.FirebaseFirestoreException(
                "Permission denied: only primary app owner can access fadfada author identity",
                com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
        val snap = fadfadaAdminIdentitiesCollection.document(postId).get(com.google.firebase.firestore.Source.SERVER).await()
        snap.toObject(FadfadaAdminIdentity::class.java)
    }

    suspend fun getChatRoomById(roomId: String): Result<ChatRoomMetadata?> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser == null || currentUser.isAnonymous) {
            throw com.google.firebase.firestore.FirebaseFirestoreException(
                "Permission denied: authentication required",
                com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
        val snap = roomsCollection.document(roomId).get(com.google.firebase.firestore.Source.SERVER).await()
        snap.toObject(ChatRoomMetadata::class.java)
    }

    /**
     * Creates a room and immediately publishes it to Supabase Realtime (and Firestore when authenticated)
     * so all users see the new room in real time.
     */
    suspend fun createChatRoom(
        roomName: String,
        creatorId: String? = null,
        roomPassword: String = "",
        roomCategory: String = "GENERAL",
        customRoomId: String? = null,
        ambientThemeId: String = WanasRoomAmbientTheme.ROYAL_NIGHT.id
    ): Result<ChatRoomMetadata> = runCatching {
        val trimmedName = roomName.trim().take(120)
        require(trimmedName.isNotEmpty()) { "يرجى إدخال اسم الغرفة" }

        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedCreator = creatorId?.takeIf { it.isNotBlank() } ?: currentUser?.uid ?: "wanas_host"
        val roomId = customRoomId?.takeIf { it.isNotBlank() }
            ?: "room_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val now = Timestamp.now()
        val cleanPass = roomPassword.trim()
        val resolvedCat = roomCategory.trim().ifBlank { "GENERAL" }
        val resolvedFilter = WanasRoomCategoryFilter.resolveDisplayFilter(resolvedCat)
        val resolvedTheme = WanasRoomAmbientTheme.resolveTheme(ambientThemeId)

        val room = ChatRoomMetadata(
            roomId = roomId,
            roomName = trimmedName,
            creatorId = resolvedCreator,
            timestamp = now,
            roomCategory = resolvedFilter.id,
            roomTopicTag = "${resolvedFilter.emoji} ${resolvedFilter.labelAr}",
            roomPassword = cleanPass,
            isPasswordProtected = cleanPass.isNotEmpty(),
            ambientThemeId = resolvedTheme.id
        )

        // Publish immediately to Supabase Realtime so all users see it right away
        supabaseService.upsertSharedRoom(room)

        if (currentUser != null && !currentUser.isAnonymous) {
            val firestoreDoc = mapOf(
                "roomId" to roomId,
                "roomName" to trimmedName,
                "creatorId" to resolvedCreator,
                "timestamp" to FieldValue.serverTimestamp(),
                "ambientThemeId" to resolvedTheme.id
            )
            roomsCollection.document(roomId).set(firestoreDoc).await()
        }
        room
    }

    suspend fun createRoom(roomName: String): ChatRoomMetadata? {
        return createChatRoom(roomName).getOrNull()
    }

    suspend fun saveMemberProfile(
        displayName: String,
        avatarEmoji: String,
        avatarUri: String? = null,
        bio: String? = null,
        roleBadge: String = "VIP عضو",
        roomsCreatedCount: Int = 0,
        roomsJoinedCount: Int = 0,
        messagesSentCount: Int = 0,
        supabaseSynced: Boolean = true,
        userId: String? = null
    ): Result<MemberProfile> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedUid = currentUser?.uid ?: userId?.takeIf { it.isNotBlank() } ?: "wanas_member"
        val memberCode = generateMemberIdCode(resolvedUid)

        val profile = MemberProfile(
            userId = resolvedUid,
            memberIdCode = memberCode,
            displayName = displayName.trim().ifEmpty { "عضو وَنَس" }.take(80),
            avatarEmoji = avatarEmoji.trim().ifEmpty { "🎙️" }.take(16),
            avatarUri = avatarUri?.take(1024),
            bio = bio?.trim()?.take(240),
            roleBadge = roleBadge.trim().ifEmpty { "VIP عضو" }.take(40),
            roomsCreatedCount = roomsCreatedCount.coerceAtLeast(0),
            roomsJoinedCount = roomsJoinedCount.coerceAtLeast(0),
            messagesSentCount = messagesSentCount.coerceAtLeast(0),
            supabaseSynced = supabaseSynced,
            updatedAt = Timestamp.now()
        )

        supabaseService.upsertMemberAccount(profile)

        val firestoreMemberDoc = mutableMapOf<String, Any>(
            "userId" to resolvedUid,
            "memberIdCode" to memberCode,
            "displayName" to profile.displayName,
            "avatarEmoji" to profile.avatarEmoji,
            "roleBadge" to profile.roleBadge,
            "roomsCreatedCount" to profile.roomsCreatedCount,
            "roomsJoinedCount" to profile.roomsJoinedCount,
            "messagesSentCount" to profile.messagesSentCount,
            "supabaseSynced" to profile.supabaseSynced,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        profile.avatarUri?.let { firestoreMemberDoc["avatarUri"] = it }
        profile.bio?.let { firestoreMemberDoc["bio"] = it }

        if (currentUser != null && !currentUser.isAnonymous) {
            membersCollection.document(currentUser.uid).set(firestoreMemberDoc).await()
        }
        profile
    }

    suspend fun addMemberChatHistoryEntry(
        roomId: String,
        roomName: String,
        messageText: String,
        activityType: String,
        userId: String? = null,
        customEntryId: String? = null
    ): Result<MemberChatHistoryEntry> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedUid = currentUser?.uid ?: userId?.takeIf { it.isNotBlank() } ?: "wanas_member"
        val entryId = customEntryId?.takeIf { it.isNotBlank() }
            ?: "hist_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifEmpty { "wanas_lobby" }

        val entry = MemberChatHistoryEntry(
            entryId = entryId,
            userId = resolvedUid,
            roomId = safeRoomId,
            roomName = roomName.trim().ifEmpty { "غرفة وَنَس" }.take(120),
            messageText = messageText.trim().ifEmpty { "نشاط في الغرفة" }.take(500),
            activityType = activityType,
            timestamp = Timestamp.now()
        )

        supabaseService.insertChatHistoryEntry(entry)

        if (currentUser != null && !currentUser.isAnonymous) {
            val firestoreEntryDoc = mapOf(
                "entryId" to entryId,
                "userId" to currentUser.uid,
                "roomId" to safeRoomId,
                "roomName" to entry.roomName,
                "messageText" to entry.messageText,
                "activityType" to entry.activityType,
                "timestamp" to FieldValue.serverTimestamp()
            )
            membersCollection.document(currentUser.uid)
                .collection("chat_history")
                .document(entryId)
                .set(firestoreEntryDoc)
                .await()
        }
        entry
    }

    suspend fun enterRoom(
        roomId: String,
        memberName: String,
        avatarEmoji: String,
        roleBadge: String,
        supabaseLinked: Boolean = true,
        userId: String? = null
    ): Result<RoomPresenceBannerEvent> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedUid = currentUser?.uid ?: userId?.takeIf { it.isNotBlank() } ?: "wanas_member"
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        val now = Timestamp.now()

        val activeMember = RoomActiveMember(
            userId = resolvedUid,
            roomId = safeRoomId,
            memberName = memberName.trim().ifEmpty { "عضو وَنَس" }.take(80),
            avatarEmoji = avatarEmoji.trim().ifEmpty { "🎙️" }.take(16),
            roleBadge = roleBadge.trim().ifEmpty { "VIP عضو" }.take(40),
            supabaseLinked = supabaseLinked,
            joinedAt = now
        )

        val eventId = "join_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val joinEvent = RoomPresenceBannerEvent(
            eventId = eventId,
            roomId = safeRoomId,
            userId = resolvedUid,
            memberName = activeMember.memberName,
            avatarEmoji = activeMember.avatarEmoji,
            eventType = "JOIN",
            createdAt = now
        )

        if (currentUser != null && !currentUser.isAnonymous) {
            val activeMemberDoc = mapOf(
                "userId" to currentUser.uid,
                "roomId" to safeRoomId,
                "memberName" to activeMember.memberName,
                "avatarEmoji" to activeMember.avatarEmoji,
                "roleBadge" to activeMember.roleBadge,
                "supabaseLinked" to supabaseLinked,
                "joinedAt" to FieldValue.serverTimestamp()
            )
            val joinEventDoc = mapOf(
                "eventId" to eventId,
                "roomId" to safeRoomId,
                "userId" to currentUser.uid,
                "memberName" to activeMember.memberName,
                "avatarEmoji" to activeMember.avatarEmoji,
                "eventType" to "JOIN",
                "createdAt" to FieldValue.serverTimestamp()
            )
            roomsCollection.document(safeRoomId)
                .collection("active_members")
                .document(currentUser.uid)
                .set(activeMemberDoc)
                .await()
            roomsCollection.document(safeRoomId)
                .collection("presence_events")
                .document(eventId)
                .set(joinEventDoc)
                .await()
        }
        joinEvent
    }

    /**
     * Adds an authenticated user or invited member to an active voice chat room in Firestore
     * (`/chat_rooms/{roomId}/active_members/{memberId}`) and logs a `JOIN` presence event
     * (`/chat_rooms/{roomId}/presence_events/{eventId}`).
     */
    suspend fun addUserToActiveRoomInFirestore(
        roomId: String,
        targetUserId: String,
        memberName: String,
        avatarEmoji: String = "🎙️",
        roleBadge: String = "عضو الغرفة 🎙️",
        supabaseLinked: Boolean = true
    ): Result<RoomActiveMember> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        require(safeRoomId.isNotEmpty()) { "معرف الغرفة غير صالح" }
        val cleanName = memberName.trim().ifEmpty { "عضو وَنَس" }.take(80)
        val rawId = targetUserId.trim().ifEmpty {
            "usr_${cleanName.hashCode().let { if (it < 0) -it else it }}"
        }
        val safeMemberId = rawId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifEmpty { "wanas_user" }
        val cleanEmoji = avatarEmoji.trim().ifEmpty { "🎙️" }.take(16)
        val cleanBadge = roleBadge.trim().ifEmpty { "عضو الغرفة 🎙️" }.take(40)
        val now = Timestamp.now()

        val activeMember = RoomActiveMember(
            userId = safeMemberId,
            roomId = safeRoomId,
            memberName = cleanName,
            avatarEmoji = cleanEmoji,
            roleBadge = cleanBadge,
            supabaseLinked = supabaseLinked,
            joinedAt = now
        )

        if (currentUser != null && !currentUser.isAnonymous) {
            val activeMemberDoc = mapOf(
                "userId" to safeMemberId,
                "roomId" to safeRoomId,
                "memberName" to cleanName,
                "avatarEmoji" to cleanEmoji,
                "roleBadge" to cleanBadge,
                "supabaseLinked" to supabaseLinked,
                "joinedAt" to FieldValue.serverTimestamp()
            )
            roomsCollection.document(safeRoomId)
                .collection("active_members")
                .document(safeMemberId)
                .set(activeMemberDoc)
                .await()

            if (safeMemberId == currentUser.uid) {
                val eventId = "join_${UUID.randomUUID().toString().replace("-", "").take(16)}"
                val joinEventDoc = mapOf(
                    "eventId" to eventId,
                    "roomId" to safeRoomId,
                    "userId" to currentUser.uid,
                    "memberName" to cleanName,
                    "avatarEmoji" to cleanEmoji,
                    "eventType" to "JOIN",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                roomsCollection.document(safeRoomId)
                    .collection("presence_events")
                    .document(eventId)
                    .set(joinEventDoc)
                    .await()
            }
        }
        activeMember
    }

    /**
     * Removes a user from an active voice chat room in Firestore (`/chat_rooms/{roomId}/active_members/{memberId}`).
     */
    suspend fun removeUserFromActiveRoomInFirestore(
        roomId: String,
        targetUserId: String
    ): Result<Unit> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        val safeMemberId = targetUserId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        if (currentUser != null && !currentUser.isAnonymous && safeRoomId.isNotEmpty() && safeMemberId.isNotEmpty()) {
            roomsCollection.document(safeRoomId)
                .collection("active_members")
                .document(safeMemberId)
                .delete()
                .await()
        }
    }

    suspend fun leaveRoom(
        roomId: String,
        memberName: String,
        avatarEmoji: String,
        userId: String? = null
    ): Result<RoomPresenceBannerEvent> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedUid = currentUser?.uid ?: userId?.takeIf { it.isNotBlank() } ?: "wanas_member"
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        val now = Timestamp.now()
        val eventId = "leave_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        val leaveEvent = RoomPresenceBannerEvent(
            eventId = eventId,
            roomId = safeRoomId,
            userId = resolvedUid,
            memberName = memberName.trim().ifEmpty { "عضو وَنَس" }.take(80),
            avatarEmoji = avatarEmoji.trim().ifEmpty { "🎙️" }.take(16),
            eventType = "LEAVE",
            createdAt = now
        )

        if (currentUser != null && !currentUser.isAnonymous) {
            val leaveEventDoc = mapOf(
                "eventId" to eventId,
                "roomId" to safeRoomId,
                "userId" to currentUser.uid,
                "memberName" to leaveEvent.memberName,
                "avatarEmoji" to leaveEvent.avatarEmoji,
                "eventType" to "LEAVE",
                "createdAt" to FieldValue.serverTimestamp()
            )
            roomsCollection.document(safeRoomId)
                .collection("active_members")
                .document(currentUser.uid)
                .delete()
                .await()
            roomsCollection.document(safeRoomId)
                .collection("presence_events")
                .document(eventId)
                .set(leaveEventDoc)
                .await()
        }
        leaveEvent
    }

    suspend fun deleteChatRoom(roomId: String): Result<Unit> = runCatching {
        supabaseService.deleteSharedRoomFromSupabase(roomId)
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentUser != null && !currentUser.isAnonymous) {
            roomsCollection.document(roomId).delete().await()
        }
    }

    suspend fun deleteRoom(roomId: String) {
        deleteChatRoom(roomId)
    }

    fun observePushNotifications(): Flow<List<PushNotificationEvent>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous) return emptyFlow()

        return callbackFlow {
            val subscription = pushNotificationsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(40)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val events = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(PushNotificationEvent::class.java)
                        }
                        trySend(events)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun publishPushNotificationEvent(
        senderName: String,
        recipientQuery: String,
        roomId: String,
        roomName: String,
        notificationType: String,
        messageBody: String,
        senderId: String? = null,
        customNotificationId: String? = null
    ): Result<PushNotificationEvent> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedSenderId = currentUser?.uid ?: senderId?.takeIf { it.isNotBlank() } ?: "wanas_member"
        val notificationId = customNotificationId?.takeIf { it.isNotBlank() }
            ?: "notif_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifEmpty { "wanas_lobby" }
        val now = Timestamp.now()

        val event = PushNotificationEvent(
            notificationId = notificationId,
            senderId = resolvedSenderId,
            senderName = senderName.trim().ifEmpty { "عضو وَنَس" }.take(80),
            recipientQuery = recipientQuery.trim().ifEmpty { "ALL" }.take(160),
            roomId = safeRoomId,
            roomName = roomName.trim().ifEmpty { "غرفة وَنَس" }.take(120),
            notificationType = notificationType,
            messageBody = messageBody.trim().take(500),
            timestamp = now
        )

        if (currentUser != null && !currentUser.isAnonymous) {
            val firestoreDoc = mapOf(
                "notificationId" to event.notificationId,
                "senderId" to currentUser.uid,
                "senderName" to event.senderName,
                "recipientQuery" to event.recipientQuery,
                "roomId" to event.roomId,
                "roomName" to event.roomName,
                "notificationType" to event.notificationType,
                "messageBody" to event.messageBody,
                "timestamp" to FieldValue.serverTimestamp()
            )
            pushNotificationsCollection.document(notificationId).set(firestoreDoc).await()
        }
        event
    }

    fun observePaymentReceiptsForUser(userId: String): Flow<List<VerifiedPaymentReceipt>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous || currentUser.uid != userId) return emptyFlow()

        return callbackFlow {
            val subscription = paymentReceiptsCollection
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val receipts = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(VerifiedPaymentReceipt::class.java)
                        }.sortedByDescending { it.timestamp }
                        trySend(receipts)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun createVerifiedPaymentReceipt(
        memberName: String,
        planId: String,
        planTitle: String,
        amountEgp: Int,
        paymentMethod: String,
        transactionReference: String,
        userId: String? = null,
        customReceiptId: String? = null
    ): Result<VerifiedPaymentReceipt> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedUid = currentUser?.uid ?: userId?.takeIf { it.isNotBlank() } ?: "wanas_member"
        require(amountEgp > 0 && transactionReference.trim().length >= 4) { "بيانات عملية الدفع غير مكتملة" }

        val receiptId = customReceiptId?.takeIf { it.isNotBlank() }
            ?: "rcpt_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val safePlanId = planId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifEmpty { "vip_plan" }
        val now = Timestamp.now()

        val receipt = VerifiedPaymentReceipt(
            receiptId = receiptId,
            userId = resolvedUid,
            memberName = memberName.trim().ifEmpty { "عضو وَنَس" }.take(80),
            planId = safePlanId,
            planTitle = planTitle.trim().take(100),
            amountEgp = amountEgp,
            paymentMethod = paymentMethod,
            transactionReference = transactionReference.trim().take(80),
            verified = true,
            timestamp = now
        )

        if (currentUser != null && !currentUser.isAnonymous) {
            val receiptDoc = mapOf(
                "receiptId" to receiptId,
                "userId" to currentUser.uid,
                "memberName" to receipt.memberName,
                "planId" to safePlanId,
                "planTitle" to receipt.planTitle,
                "amountEgp" to amountEgp,
                "paymentMethod" to paymentMethod,
                "transactionReference" to receipt.transactionReference,
                "verified" to true,
                "timestamp" to FieldValue.serverTimestamp()
            )
            paymentReceiptsCollection.document(receiptId).set(receiptDoc).await()
        }
        receipt
    }

    /**
     * Records a gift transaction between users in Firestore:
     * 1. Inside the interactive room's subcollection `/chat_rooms/{roomId}/room_gifts/{transactionId}`
     * 2. Inside the global gift ledger `/gift_transactions/{transactionId}`
     */
    suspend fun recordGiftTransactionInFirestore(
        roomId: String,
        roomName: String,
        giftId: String,
        giftNameAr: String,
        giftEmoji: String,
        costCoins: Int,
        quantity: Int = 1,
        senderName: String,
        senderMemberCode: String = "",
        recipientId: String = "",
        recipientName: String,
        pkTeamSupported: String = "RED",
        pkBonusPoints: Int = 0,
        senderId: String? = null,
        customTransactionId: String? = null
    ): Result<RoomGiftTransactionDocument> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedSenderId = currentUser?.uid
            ?: senderId?.takeIf { it.isNotBlank() }
            ?: "wanas_member"
        val safeSenderId = resolvedSenderId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128).ifEmpty { "wanas_member" }
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifEmpty { "room_wanas_egypt_1" }
        val safeGiftId = giftId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifEmpty { "gift_rose" }
        val rawRecipientId = recipientId.trim().ifBlank {
            "user_${recipientName.hashCode().let { if (it < 0) -it else it }}"
        }
        val safeRecipientId = rawRecipientId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128).ifEmpty { "wanas_recipient" }
        val transactionId = customTransactionId?.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")?.takeIf { it.isNotBlank() }
            ?: "gift_tx_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val resolvedMemberCode = senderMemberCode.trim().takeIf { it.length in 3..32 }
            ?: generateMemberIdCode(safeSenderId)
        val safeQuantity = quantity.coerceIn(1, 999)
        val safeCostCoins = costCoins.coerceAtLeast(0)
        val safePkTeam = when (pkTeamSupported.uppercase()) {
            "RED", "BLUE", "NONE" -> pkTeamSupported.uppercase()
            else -> "RED"
        }
        val safePkBonus = pkBonusPoints.coerceAtLeast(0)
        val now = Timestamp.now()

        val txDocObj = RoomGiftTransactionDocument(
            transactionId = transactionId,
            roomId = safeRoomId,
            roomName = roomName.trim().ifEmpty { "غرفة وَنَس التفاعلية" }.take(120),
            giftId = safeGiftId,
            giftNameAr = giftNameAr.trim().ifEmpty { "هدية تفاعلية" }.take(80),
            giftEmoji = giftEmoji.trim().ifEmpty { "🎁" }.take(16),
            costCoins = safeCostCoins,
            quantity = safeQuantity,
            senderId = safeSenderId,
            senderMemberCode = resolvedMemberCode,
            senderName = senderName.trim().ifEmpty { "عضو وَنَس" }.take(80),
            recipientId = safeRecipientId,
            recipientName = recipientName.trim().ifEmpty { "مضيف السهرة 👑" }.take(80),
            pkTeamSupported = safePkTeam,
            pkBonusPoints = safePkBonus,
            timestamp = now
        )

        if (currentUser != null && !currentUser.isAnonymous) {
            val firestorePayload = mapOf(
                "transactionId" to txDocObj.transactionId,
                "roomId" to txDocObj.roomId,
                "roomName" to txDocObj.roomName,
                "giftId" to txDocObj.giftId,
                "giftNameAr" to txDocObj.giftNameAr,
                "giftEmoji" to txDocObj.giftEmoji,
                "costCoins" to txDocObj.costCoins,
                "quantity" to txDocObj.quantity,
                "senderId" to currentUser.uid,
                "senderMemberCode" to txDocObj.senderMemberCode,
                "senderName" to txDocObj.senderName,
                "recipientId" to txDocObj.recipientId,
                "recipientName" to txDocObj.recipientName,
                "pkTeamSupported" to txDocObj.pkTeamSupported,
                "pkBonusPoints" to txDocObj.pkBonusPoints,
                "timestamp" to FieldValue.serverTimestamp()
            )
            val batch = firestore.batch()
            val roomGiftRef = roomsCollection
                .document(txDocObj.roomId)
                .collection("room_gifts")
                .document(txDocObj.transactionId)
            val globalGiftRef = giftTransactionsCollection.document(txDocObj.transactionId)
            batch.set(roomGiftRef, firestorePayload)
            batch.set(globalGiftRef, firestorePayload)
            batch.commit().await()
        }
        txDocObj
    }

    /**
     * Observes real-time interactive gift transactions inside a specific voice room
     * (`/chat_rooms/{roomId}/room_gifts`).
     */
    fun observeRoomGiftTransactions(roomId: String): Flow<List<RoomGiftTransactionDocument>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous || roomId.isBlank()) return emptyFlow()
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)

        return callbackFlow {
            val subscription = roomsCollection.document(safeRoomId)
                .collection("room_gifts")
                .whereEqualTo("roomId", safeRoomId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val transactions = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(RoomGiftTransactionDocument::class.java)
                        }.sortedByDescending { it.timestampMillis }
                        trySend(transactions)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    /**
     * Observes the global gift transactions ledger (`/gift_transactions`).
     */
    fun observeGlobalGiftTransactions(): Flow<List<RoomGiftTransactionDocument>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous) return emptyFlow()

        return callbackFlow {
            val subscription = giftTransactionsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val transactions = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(RoomGiftTransactionDocument::class.java)
                        }
                        trySend(transactions)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    /**
     * Publishes a WebRTC Native P2P Audio Signaling message (`OFFER`, `ANSWER`, or `ICE_CANDIDATE`)
     * to `/chat_rooms/{roomId}/webrtc_signals/{signalId}` in Firestore so audio streams across the internet
     * between room members.
     */
    suspend fun publishWebRtcSignal(
        roomId: String,
        targetUserId: String,
        signalType: String,
        sdp: String = "",
        sdpMid: String = "",
        sdpMLineIndex: Int = -1,
        candidate: String = "",
        sessionEpochMs: Long = System.currentTimeMillis(),
        senderName: String = "عضو وَنَس",
        senderId: String? = null,
        customSignalId: String? = null
    ): Result<WebRtcSignalingDocument> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val resolvedSenderId = currentUser?.uid
            ?: senderId?.takeIf { it.isNotBlank() }
            ?: "wanas_member"
        val safeSenderId = resolvedSenderId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128).ifEmpty { "wanas_member" }
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifEmpty { "room_wanas_egypt_1" }
        val safeTargetUserId = targetUserId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128).ifEmpty { "wanas_peer" }
        val normalizedType = when (signalType.uppercase()) {
            "OFFER", "ANSWER", "ICE_CANDIDATE" -> signalType.uppercase()
            else -> "OFFER"
        }
        val signalId = customSignalId?.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")?.takeIf { it.isNotBlank() }
            ?: "sig_${normalizedType.lowercase()}_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val safeEpochMs = sessionEpochMs.coerceAtLeast(0L)
        val now = Timestamp.now()

        val docObj = WebRtcSignalingDocument(
            signalId = signalId,
            roomId = safeRoomId,
            senderId = safeSenderId,
            senderName = senderName.trim().ifEmpty { "عضو وَنَس" }.take(80),
            targetUserId = safeTargetUserId,
            signalType = normalizedType,
            sdp = sdp.take(16000),
            sdpMid = sdpMid.take(128),
            sdpMLineIndex = sdpMLineIndex.coerceIn(-1, 32),
            candidate = candidate.take(4096),
            sessionEpochMs = safeEpochMs,
            timestamp = now
        )

        if (currentUser != null && !currentUser.isAnonymous) {
            val payload = mapOf(
                "signalId" to docObj.signalId,
                "roomId" to docObj.roomId,
                "senderId" to currentUser.uid,
                "senderName" to docObj.senderName,
                "targetUserId" to docObj.targetUserId,
                "signalType" to docObj.signalType,
                "sdp" to docObj.sdp,
                "sdpMid" to docObj.sdpMid,
                "sdpMLineIndex" to docObj.sdpMLineIndex,
                "candidate" to docObj.candidate,
                "sessionEpochMs" to docObj.sessionEpochMs,
                "timestamp" to FieldValue.serverTimestamp()
            )
            roomsCollection
                .document(docObj.roomId)
                .collection("webrtc_signals")
                .document(docObj.signalId)
                .set(payload)
                .await()
        }
        docObj
    }

    /**
     * Observes incoming WebRTC signaling documents (`OFFER`, `ANSWER`, `ICE_CANDIDATE`) targeted to
     * the current member in `/chat_rooms/{roomId}/webrtc_signals`.
     *
     * Strictly filters out stale signals (`sessionEpochMs < minSessionEpochMs`) and tracks processed
     * `signalId`s so old signals are NEVER replayed when a new member joins the room.
     */
    fun observeIncomingWebRtcSignals(
        roomId: String,
        targetUserId: String? = null,
        minSessionEpochMs: Long = 0L
    ): Flow<List<WebRtcSignalingDocument>> {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return emptyFlow()
        if (currentUser.isAnonymous || roomId.isBlank()) return emptyFlow()
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        val resolvedTargetUid = (targetUserId?.takeIf { it.isNotBlank() } ?: currentUser.uid)
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(128)

        return callbackFlow {
            val subscription = roomsCollection.document(safeRoomId)
                .collection("webrtc_signals")
                .whereEqualTo("roomId", safeRoomId)
                .whereEqualTo("targetUserId", resolvedTargetUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val freshSignals = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(WebRtcSignalingDocument::class.java)
                        }.filter { signal ->
                            // Prevent replaying stale signals created before this member's session started
                            signal.sessionEpochMs >= minSessionEpochMs &&
                                signal.senderId != resolvedTargetUid
                        }.sortedBy { it.sessionEpochMs }
                        trySend(freshSignals)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    /**
     * Deletes a consumed WebRTC signaling document from Firestore so it is never replayed in future sessions.
     */
    suspend fun deleteWebRtcSignal(roomId: String, signalId: String): Result<Unit> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return@runCatching
        if (currentUser.isAnonymous || roomId.isBlank() || signalId.isBlank()) return@runCatching
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        val safeSignalId = signalId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128)
        roomsCollection.document(safeRoomId)
            .collection("webrtc_signals")
            .document(safeSignalId)
            .delete()
            .await()
    }

    /**
     * Cleans up any stale WebRTC signaling documents sent by or targeted to [userId] prior to [beforeEpochMs]
     * upon room entry or exit so newly joining members never replay stale offers/answers/candidates.
     */
    suspend fun clearStaleWebRtcSignalsForMember(
        roomId: String,
        userId: String? = null,
        beforeEpochMs: Long = System.currentTimeMillis()
    ): Result<Int> = runCatching {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return@runCatching 0
        if (currentUser.isAnonymous || roomId.isBlank()) return@runCatching 0
        val safeRoomId = roomId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        val resolvedUid = (userId?.takeIf { it.isNotBlank() } ?: currentUser.uid)
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(128)

        val incomingSnap = runCatching {
            roomsCollection.document(safeRoomId)
                .collection("webrtc_signals")
                .whereEqualTo("roomId", safeRoomId)
                .whereEqualTo("targetUserId", resolvedUid)
                .get()
                .await()
        }.getOrNull()

        val outgoingSnap = runCatching {
            roomsCollection.document(safeRoomId)
                .collection("webrtc_signals")
                .whereEqualTo("roomId", safeRoomId)
                .whereEqualTo("senderId", resolvedUid)
                .get()
                .await()
        }.getOrNull()

        val docsToDelete = (incomingSnap?.documents.orEmpty() + outgoingSnap?.documents.orEmpty())
            .distinctBy { it.id }
            .filter { doc ->
                val epoch = doc.getLong("sessionEpochMs") ?: 0L
                epoch <= beforeEpochMs
            }

        var deletedCount = 0
        docsToDelete.forEach { doc ->
            runCatching {
                doc.reference.delete().await()
                deletedCount++
            }
        }
        deletedCount
    }
}
