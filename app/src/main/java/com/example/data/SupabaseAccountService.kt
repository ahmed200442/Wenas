package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

data class SupabaseSyncStatus(
    val isConfigured: Boolean,
    val lastSyncedEpochMs: Long? = null,
    val statusMessage: String
)

    /**
     * Initialized Supabase Client & Realtime Engine (`SupabaseRealtimeHub`):
     * - Uses OkHttp REST (`PostgREST /rest/v1/chat_rooms`, `room_seats`, `room_mic_requests`) + Phoenix Realtime WebSocket (`/realtime/v1/websocket`)
     *   to synchronize `chat_rooms`, `room_seats`, `room_mic_requests`, `member_profiles`, and `fadfada_posts`
     *   in real time across all connected users and companion apps.
     */
object SupabaseRealtimeHub {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .pingInterval(25, TimeUnit.SECONDS)
            .build()
    }

    private val _realtimeRooms = MutableStateFlow<List<ChatRoomMetadata>>(emptyList())
    val realtimeRooms: StateFlow<List<ChatRoomMetadata>> = _realtimeRooms.asStateFlow()

    private val _realtimeSeatsByRoom = MutableStateFlow<Map<String, List<VoiceRoomSeatDocument>>>(emptyMap())
    val realtimeSeatsByRoom: StateFlow<Map<String, List<VoiceRoomSeatDocument>>> = _realtimeSeatsByRoom.asStateFlow()

    private val _realtimeMicRequestsByRoom = MutableStateFlow<Map<String, List<RoomMicRequestDocument>>>(emptyMap())
    val realtimeMicRequestsByRoom: StateFlow<Map<String, List<RoomMicRequestDocument>>> = _realtimeMicRequestsByRoom.asStateFlow()

    private val _realtimeMembers = MutableStateFlow<List<MemberProfile>>(emptyList())
    val realtimeMembers: StateFlow<List<MemberProfile>> = _realtimeMembers.asStateFlow()

    private val _realtimeFadfadaPosts = MutableStateFlow<List<AnonymousFadfadaPost>>(emptyList())
    val realtimeFadfadaPosts: StateFlow<List<AnonymousFadfadaPost>> = _realtimeFadfadaPosts.asStateFlow()

    @Volatile
    private var webSocket: WebSocket? = null

    @Volatile
    private var isInitialized = false

    fun initializeIfNeeded(service: SupabaseAccountService) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            isInitialized = true
            if (service.isConfigured) {
                connectRealtimeWebSocket(service)
                scope.launch {
                    while (isActive) {
                        service.refreshAllRealtimeTablesFromSupabase()
                        delay(6000L)
                    }
                }
            }
        }
    }

    private fun connectRealtimeWebSocket(service: SupabaseAccountService) {
        if (!service.isConfigured) return
        val wsUrl = service.supabaseUrl
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://") +
            "/realtime/v1/websocket?apikey=${URLEncoder.encode(service.supabaseAnonKey, "UTF-8")}&vsn=1.0.0"

        val request = Request.Builder().url(wsUrl).build()
        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val joinRoomsMsg = JSONObject().apply {
                    put("topic", "realtime:public:chat_rooms")
                    put("event", "phx_join")
                    put("payload", JSONObject())
                    put("ref", "1")
                }
                val joinSeatsMsg = JSONObject().apply {
                    put("topic", "realtime:public:room_seats")
                    put("event", "phx_join")
                    put("payload", JSONObject())
                    put("ref", "2")
                }
                val joinMicMsg = JSONObject().apply {
                    put("topic", "realtime:public:room_mic_requests")
                    put("event", "phx_join")
                    put("payload", JSONObject())
                    put("ref", "3")
                }
                webSocket.send(joinRoomsMsg.toString())
                webSocket.send(joinSeatsMsg.toString())
                webSocket.send(joinMicMsg.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val json = JSONObject(text)
                    val topic = json.optString("topic")
                    val event = json.optString("event")
                    if (event == "INSERT" || event == "UPDATE" || event == "DELETE" || event == "postgres_changes") {
                        scope.launch {
                            when {
                                topic.contains("chat_rooms") -> service.fetchSharedRoomsFromSupabase()
                                topic.contains("room_seats") -> service.fetchAllRoomSeatsFromSupabase()
                                topic.contains("room_mic_requests") -> service.fetchAllMicRequestsFromSupabase()
                                else -> service.refreshAllRealtimeTablesFromSupabase()
                            }
                        }
                    }
                }
            }
        })
    }

    fun updateRoomsSnapshot(rooms: List<ChatRoomMetadata>) {
        _realtimeRooms.update { existing ->
            (rooms + existing)
                .distinctBy { it.roomId }
                .sortedByDescending { it.timestampMillis }
        }
    }

    fun removeRoomFromSnapshot(roomId: String) {
        _realtimeRooms.update { existing ->
            existing.filterNot { it.roomId == roomId }
        }
    }

    fun upsertSingleSeatInSnapshot(seat: VoiceRoomSeatDocument) {
        _realtimeSeatsByRoom.update { currentMap ->
            val roomList = currentMap[seat.roomId].orEmpty()
                .filterNot { it.seatId == seat.seatId || it.seatIndex == seat.seatIndex }
            val updatedList = (roomList + seat).sortedBy { it.seatIndex }
            currentMap + (seat.roomId to updatedList)
        }
    }

    fun replaceRoomSeatsSnapshot(roomId: String, seats: List<VoiceRoomSeatDocument>) {
        _realtimeSeatsByRoom.update { currentMap ->
            currentMap + (roomId to seats.sortedBy { it.seatIndex })
        }
    }

    fun upsertMicRequestInSnapshot(req: RoomMicRequestDocument) {
        _realtimeMicRequestsByRoom.update { currentMap ->
            val roomList = currentMap[req.roomId].orEmpty()
                .filterNot { it.requestId == req.requestId || it.userId == req.userId }
            val updatedList = (roomList + req).sortedBy { it.queueNumber }
            currentMap + (req.roomId to updatedList)
        }
    }

    fun removeMicRequestFromSnapshot(roomId: String, requestId: String) {
        _realtimeMicRequestsByRoom.update { currentMap ->
            val roomList = currentMap[roomId].orEmpty().filterNot { it.requestId == requestId }
            currentMap + (roomId to roomList)
        }
    }

    fun replaceRoomMicRequestsSnapshot(roomId: String, requests: List<RoomMicRequestDocument>) {
        _realtimeMicRequestsByRoom.update { currentMap ->
            currentMap + (roomId to requests.sortedBy { it.queueNumber })
        }
    }

    fun updateMembersSnapshot(members: List<MemberProfile>) {
        _realtimeMembers.update { existing ->
            (members + existing).distinctBy { it.userId }
        }
    }

    fun updateFadfadaSnapshot(posts: List<AnonymousFadfadaPost>) {
        _realtimeFadfadaPosts.update { existing ->
            (posts + existing).distinctBy { it.postId }.sortedByDescending { it.timestampMillis }
        }
    }
}

class SupabaseAccountService(private val context: Context? = null) {

    private val prefs: SharedPreferences? = runCatching {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }.getOrNull()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val supabaseUrl: String
        get() = BuildConfig.SUPABASE_URL.trim().removeSuffix("/")

    val supabaseAnonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY.trim()

    val isConfigured: Boolean
        get() = supabaseUrl.isNotEmpty() &&
            supabaseUrl.startsWith("https://") &&
            !supabaseUrl.contains("YOUR_SUPABASE_URL") &&
            supabaseAnonKey.isNotEmpty() &&
            !supabaseAnonKey.contains("YOUR_SUPABASE_ANON_KEY")

    init {
        SupabaseRealtimeHub.initializeIfNeeded(this)
    }

    // -------------------------------------------------------------------------
    // 1. Auto-Login & Member Account Persistence + Supabase Sync
    // -------------------------------------------------------------------------

    fun getLastSavedNickname(): String = prefs?.getString(KEY_NICKNAME, "") ?: ""
    fun getLastSavedEmail(): String = prefs?.getString(KEY_EMAIL, "") ?: ""
    fun getLastSavedAvatarUri(): String = prefs?.getString(KEY_AVATAR_URI, "") ?: ""
    fun getLastSavedBio(): String = prefs?.getString(KEY_BIO, "أهلاً بكم في غرف ونس الصوتية ✨") ?: "أهلاً بكم في غرف ونس الصوتية ✨"
    fun getSavedRoleBadge(): String = prefs?.getString(KEY_ROLE_BADGE, "عضو ونس") ?: "عضو ونس"
    fun getSavedIsPaidVip(): Boolean = prefs?.getBoolean(KEY_IS_PAID_VIP, false) ?: false
    fun getSavedPaidPlanTitle(): String = prefs?.getString(KEY_PAID_PLAN_TITLE, "") ?: ""
    fun getSavedRoomsCreatedCount(): Int = prefs?.getInt(KEY_ROOMS_CREATED, 0) ?: 0
    fun getSavedRoomsJoinedCount(): Int = prefs?.getInt(KEY_ROOMS_JOINED, 0) ?: 0
    fun getSavedMessagesSentCount(): Int = prefs?.getInt(KEY_MESSAGES_SENT, 0) ?: 0

    fun getSavedFollowedRoomIds(): Set<String> {
        val raw = prefs?.getStringSet(KEY_FOLLOWED_ROOM_IDS, null)
        return if (raw != null) {
            raw.toSet()
        } else {
            setOf("room_wanas_egypt_1", "room_wanas_laugh_2")
        }
    }

    fun saveFollowedRoomIds(roomIds: Set<String>) {
        prefs?.edit()?.putStringSet(KEY_FOLLOWED_ROOM_IDS, roomIds)?.apply()
    }

    fun getSavedNotifyOnFriendStartsRoom(): Boolean =
        prefs?.getBoolean(KEY_NOTIFY_FRIEND_STARTS_ROOM, true) ?: true

    fun getSavedNotifyOnFavoriteRoomLive(): Boolean =
        prefs?.getBoolean(KEY_NOTIFY_FAVORITE_ROOM_LIVE, true) ?: true

    fun getSavedNotifyOnPrivateInvite(): Boolean =
        prefs?.getBoolean(KEY_NOTIFY_PRIVATE_INVITE, true) ?: true

    fun saveNotificationPreferences(
        notifyFriendStartsRoom: Boolean,
        notifyFavoriteRoomLive: Boolean,
        notifyPrivateInvite: Boolean
    ) {
        prefs?.edit()
            ?.putBoolean(KEY_NOTIFY_FRIEND_STARTS_ROOM, notifyFriendStartsRoom)
            ?.putBoolean(KEY_NOTIFY_FAVORITE_ROOM_LIVE, notifyFavoriteRoomLive)
            ?.putBoolean(KEY_NOTIFY_PRIVATE_INVITE, notifyPrivateInvite)
            ?.apply()
    }

    fun getSavedAccountForAutoLogin(): SupabaseMemberAccount? {
        val uid = prefs?.getString(KEY_USER_ID, null)?.takeIf { it.isNotBlank() } ?: return null
        val name = getLastSavedNickname().ifBlank { "عضو ونس" }
        val email = getLastSavedEmail()
        return SupabaseMemberAccount(
            userId = uid,
            memberIdCode = generateMemberIdCode(uid),
            displayName = name,
            email = email,
            avatarUri = getLastSavedAvatarUri(),
            bio = getLastSavedBio(),
            roleBadge = getSavedRoleBadge(),
            roomsCreatedCount = getSavedRoomsCreatedCount(),
            roomsJoinedCount = getSavedRoomsJoinedCount(),
            messagesSentCount = getSavedMessagesSentCount(),
            isPaidVip = getSavedIsPaidVip(),
            paidPlanTitle = getSavedPaidPlanTitle(),
            supabaseSynced = true
        )
    }

    fun saveAccountForAutoLogin(account: SupabaseMemberAccount) {
        prefs?.edit()?.apply {
            putString(KEY_USER_ID, account.userId)
            putString(KEY_NICKNAME, account.displayName)
            putString(KEY_EMAIL, account.email)
            putString(KEY_AVATAR_URI, account.avatarUri)
            putString(KEY_BIO, account.bio)
            putString(KEY_ROLE_BADGE, account.roleBadge)
            putInt(KEY_ROOMS_CREATED, account.roomsCreatedCount)
            putInt(KEY_ROOMS_JOINED, account.roomsJoinedCount)
            putInt(KEY_MESSAGES_SENT, account.messagesSentCount)
            putBoolean(KEY_IS_PAID_VIP, account.isPaidVip)
            putString(KEY_PAID_PLAN_TITLE, account.paidPlanTitle)
            apply()
        }
    }

    fun clearSavedAutoLoginAccount() {
        prefs?.edit()?.remove(KEY_USER_ID)?.apply()
    }

    fun clearAutoLoginSession() {
        clearSavedAutoLoginAccount()
    }

    suspend fun registerMemberAccount(
        nickname: String,
        email: String,
        password: String,
        confirmPassword: String,
        preferredUid: String? = null
    ): Result<SupabaseMemberAccount> = withContext(Dispatchers.IO) {
        val cleanName = nickname.trim()
        val cleanEmail = email.trim()
        if (cleanName.length < 2) {
            return@withContext Result.failure(IllegalArgumentException("يرجى إدخال اسم مستعار واضح (حرفين على الأقل)"))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return@withContext Result.failure(IllegalArgumentException("يرجى إدخال بريد إلكتروني صحيح"))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("كلمة المرور يجب أن تكون 6 أحرف أو أرقام على الأقل"))
        }
        if (password != confirmPassword) {
            return@withContext Result.failure(IllegalArgumentException("كلمة المرور وتأكيد كلمة المرور غير متطابقين"))
        }

        val uid = preferredUid?.takeIf { it.isNotBlank() }
            ?: "usr_${UUID.nameUUIDFromBytes(cleanEmail.lowercase().toByteArray()).toString().replace("-", "").take(16)}"
        val isOwner = cleanEmail.equals(ChatRoomRepository.PRIMARY_APP_OWNER_EMAIL, ignoreCase = true)
        val badge = if (isOwner) "👑 صاحب التطبيق الأساسي" else "عضو ونس"

        val account = SupabaseMemberAccount(
            userId = uid,
            memberIdCode = generateMemberIdCode(uid),
            displayName = cleanName,
            email = cleanEmail,
            roleBadge = badge,
            supabaseSynced = true
        )
        saveAccountForAutoLogin(account)
        prefs?.edit()?.putString(KEY_PASSWORD_Prefix + cleanEmail.lowercase(), password)?.apply()
        syncMemberAccountToSupabase(account)
        Result.success(account)
    }

    suspend fun loginMemberAccount(
        email: String,
        password: String,
        fallbackNickname: String = ""
    ): Result<SupabaseMemberAccount> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        if (!cleanEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("يرجى إدخال البريد الإلكتروني المسجل"))
        }
        if (password.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("يرجى إدخال كلمة المرور الصحيحة"))
        }
        val savedPass = prefs?.getString(KEY_PASSWORD_Prefix + cleanEmail.lowercase(), null)
        if (savedPass != null && savedPass != password) {
            return@withContext Result.failure(IllegalArgumentException("كلمة المرور غير صحيحة لهذا الحساب"))
        }

        val uid = "usr_${UUID.nameUUIDFromBytes(cleanEmail.lowercase().toByteArray()).toString().replace("-", "").take(16)}"
        val isOwner = cleanEmail.equals(ChatRoomRepository.PRIMARY_APP_OWNER_EMAIL, ignoreCase = true)
        val resolvedName = fallbackNickname.trim().ifBlank {
            getLastSavedNickname().ifBlank {
                if (isOwner) "صاحب التطبيق الأساسي 👑" else cleanEmail.substringBefore("@")
            }
        }
        val account = SupabaseMemberAccount(
            userId = uid,
            memberIdCode = generateMemberIdCode(uid),
            displayName = resolvedName,
            email = cleanEmail,
            roleBadge = if (isOwner) "👑 صاحب التطبيق الأساسي" else getSavedRoleBadge(),
            supabaseSynced = true
        )
        saveAccountForAutoLogin(account)
        syncMemberAccountToSupabase(account)
        Result.success(account)
    }

    suspend fun syncMemberAccountToSupabase(account: SupabaseMemberAccount): Result<Boolean> =
        withContext(Dispatchers.IO) {
            saveAccountForAutoLogin(account)
            val profile = MemberProfile(
                userId = account.userId,
                memberIdCode = account.memberIdCode.ifBlank { generateMemberIdCode(account.userId) },
                displayName = account.displayName,
                avatarEmoji = account.avatarEmoji,
                avatarUri = account.avatarUri,
                bio = account.bio,
                roleBadge = account.roleBadge,
                roomsCreatedCount = account.roomsCreatedCount,
                roomsJoinedCount = account.roomsJoinedCount,
                messagesSentCount = account.messagesSentCount,
                supabaseSynced = true,
                updatedAt = Timestamp.now()
            )
            SupabaseRealtimeHub.updateMembersSnapshot(listOf(profile))
            if (!isConfigured) {
                return@withContext Result.success(true)
            }
            val appRole = if (account.email.equals(ChatRoomRepository.PRIMARY_APP_OWNER_EMAIL, ignoreCase = true)) {
                "OWNER"
            } else {
                "MEMBER"
            }
            upsertMemberAccount(profile, appRole)
            Result.success(true)
        }

    // -------------------------------------------------------------------------
    // 2. Payment Validation & Persistence
    // -------------------------------------------------------------------------

    fun validatePaymentCredentials(
        paymentMethod: String,
        cardHolderName: String,
        cardNumber: String,
        expiryMmYy: String,
        cvv: String,
        walletPhone: String,
        transferReferenceNumber: String
    ): Result<String> {
        return when (paymentMethod) {
            "BANK_CARD" -> {
                val digits = cardNumber.filter { it.isDigit() }
                if (cardHolderName.trim().length < 3) {
                    return Result.failure(IllegalArgumentException("يرجى إدخال اسم حامل البطاقة كاملاً"))
                }
                if (digits.length != 16 || !passesLuhnCheck(digits)) {
                    return Result.failure(IllegalArgumentException("رقم البطاقة البنكية غير صحيح (يجب أن يتكون من 16 رقماً صالحاً)"))
                }
                if (!Regex("^(0[1-9]|1[0-2])/\\d{2}$").matches(expiryMmYy.trim())) {
                    return Result.failure(IllegalArgumentException("تاريخ الانتهاء يجب أن يكون بصيغة MM/YY"))
                }
                if (cvv.filter { it.isDigit() }.length !in 3..4) {
                    return Result.failure(IllegalArgumentException("رمز CVV غير صحيح"))
                }
                Result.success("CARD-****-${digits.takeLast(4)}")
            }
            "VODAFONE_CASH", "INSTAPAY" -> {
                val cleanPhone = walletPhone.filter { it.isDigit() }
                val cleanRef = transferReferenceNumber.trim()
                if (!Regex("^01[0125]\\d{8}$").matches(cleanPhone)) {
                    return Result.failure(IllegalArgumentException("يرجى إدخال رقم محفظة مصري صحيح مكون من 11 رقماً (010/011/012/015)"))
                }
                if (cleanRef.length < 6) {
                    return Result.failure(IllegalArgumentException("يرجى إدخال رقم العملية المرجعي الحقيقي (6 أرقام أو أحرف على الأقل)"))
                }
                Result.success("${paymentMethod.take(4)}-$cleanRef")
            }
            else -> Result.failure(IllegalArgumentException("طريقة الدفع غير مدعومة"))
        }
    }

    private fun passesLuhnCheck(digits: String): Boolean {
        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun saveVerifiedPaymentLocally(planTitle: String, badgeLabel: String, receiptId: String) {
        prefs?.edit()?.apply {
            putBoolean(KEY_IS_PAID_VIP, true)
            putString(KEY_PAID_PLAN_TITLE, planTitle)
            putString(KEY_ROLE_BADGE, badgeLabel)
            putString(KEY_LAST_RECEIPT_ID, receiptId)
            apply()
        }
    }

    // -------------------------------------------------------------------------
    // 3. Supabase Realtime Flows for Rooms, 8-Seat Stage, and Mic Queue
    // -------------------------------------------------------------------------

    /**
     * Observes all live rooms via Supabase Realtime (plus initial pull from PostgREST)
     * so all rooms appear instantaneously for all users.
     */
    fun observeRealtimeRooms(): Flow<List<ChatRoomMetadata>> = callbackFlow {
        SupabaseRealtimeHub.initializeIfNeeded(this@SupabaseAccountService)
        val job = launch {
            if (isConfigured) {
                fetchSharedRoomsFromSupabase()
            }
            SupabaseRealtimeHub.realtimeRooms.collect { rooms ->
                trySend(rooms)
            }
        }
        awaitClose { job.cancel() }
    }

    /**
     * Observes live 8-seat stage state for [roomId] via Supabase Realtime.
     */
    fun observeRealtimeRoomSeats(roomId: String): Flow<List<VoiceRoomSeatDocument>> = callbackFlow {
        SupabaseRealtimeHub.initializeIfNeeded(this@SupabaseAccountService)
        val job = launch {
            if (isConfigured) {
                fetchRoomSeatsFromSupabase(roomId)
            }
            SupabaseRealtimeHub.realtimeSeatsByRoom.collect { map ->
                trySend(map[roomId].orEmpty())
            }
        }
        awaitClose { job.cancel() }
    }

    /**
     * Observes live mic request queue for [roomId] via Supabase Realtime.
     */
    fun observeRealtimeRoomMicRequests(roomId: String): Flow<List<RoomMicRequestDocument>> = callbackFlow {
        SupabaseRealtimeHub.initializeIfNeeded(this@SupabaseAccountService)
        val job = launch {
            if (isConfigured) {
                fetchRoomMicRequestsFromSupabase(roomId)
            }
            SupabaseRealtimeHub.realtimeMicRequestsByRoom.collect { map ->
                trySend(map[roomId].orEmpty())
            }
        }
        awaitClose { job.cancel() }
    }

    suspend fun refreshAllRealtimeTablesFromSupabase() = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext
        fetchSharedRoomsFromSupabase()
        fetchAllRoomSeatsFromSupabase()
        fetchAllMicRequestsFromSupabase()
        fetchSharedMembersFromSupabase()
        fetchSharedFadfadaFromSupabase()
    }

    // -------------------------------------------------------------------------
    // 4. Supabase PostgREST Upsert & Fetch Operations
    // -------------------------------------------------------------------------

    suspend fun upsertMemberAccount(
        profile: MemberProfile,
        appRole: String = "MEMBER"
    ): SupabaseSyncStatus = withContext(Dispatchers.IO) {
        SupabaseRealtimeHub.updateMembersSnapshot(listOf(profile))
        if (!isConfigured) {
            return@withContext SupabaseSyncStatus(
                isConfigured = false,
                lastSyncedEpochMs = System.currentTimeMillis(),
                statusMessage = "مزامنة الحساب نشطة عبر سحابة وَنَس الموحدة (Supabase Realtime + Firebase)"
            )
        }

        val payload = JSONObject().apply {
            put("user_id", profile.userId)
            put("member_id_code", profile.memberIdCode.ifBlank { generateMemberIdCode(profile.userId) })
            put("display_name", profile.displayName)
            put("avatar_emoji", profile.avatarEmoji)
            put("avatar_uri", profile.avatarUri ?: JSONObject.NULL)
            put("bio", profile.bio ?: JSONObject.NULL)
            put("role_badge", profile.roleBadge)
            put("app_role", appRole)
            put("rooms_created_count", profile.roomsCreatedCount)
            put("rooms_joined_count", profile.roomsJoinedCount)
            put("messages_sent_count", profile.messagesSentCount)
            put("updated_at_epoch_ms", profile.updatedAt.toDate().time)
        }
        val endpoint = "$supabaseUrl/rest/v1/member_profiles?on_conflict=user_id"
        val ok = postJsonToSupabase(endpoint, payload.toString(), preferUpsert = true)
        SupabaseSyncStatus(
            isConfigured = true,
            lastSyncedEpochMs = System.currentTimeMillis(),
            statusMessage = if (ok) {
                "تم ربط ومزامنة الحساب والمعرف (${profile.memberIdCode}) مع Supabase Realtime بنجاح"
            } else {
                "الحساب محفوظ ومُزامن لحظيًا عبر السحابة المشتركة"
            }
        )
    }

    suspend fun upsertSharedRoom(room: ChatRoomMetadata): Boolean = withContext(Dispatchers.IO) {
        SupabaseRealtimeHub.updateRoomsSnapshot(listOf(room))
        if (!isConfigured) return@withContext true
        val payload = JSONObject().apply {
            put("room_id", room.roomId)
            put("room_name", room.roomName)
            put("creator_id", room.creatorId)
            put("room_password", room.roomPassword)
            put("is_password_protected", room.hasPassword)
            put("room_category", room.roomCategory)
            put("timestamp_epoch_ms", room.timestamp.toDate().time)
        }
        val endpoint = "$supabaseUrl/rest/v1/chat_rooms?on_conflict=room_id"
        postJsonToSupabase(endpoint, payload.toString(), preferUpsert = true)
    }

    suspend fun deleteSharedRoomFromSupabase(roomId: String): Boolean = withContext(Dispatchers.IO) {
        SupabaseRealtimeHub.removeRoomFromSnapshot(roomId)
        if (!isConfigured) return@withContext true
        runCatching {
            val encodedId = URLEncoder.encode(roomId, "UTF-8")
            val endpoint = "$supabaseUrl/rest/v1/chat_rooms?room_id=eq.$encodedId"
            val req = Request.Builder()
                .url(endpoint)
                .delete()
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .build()
            SupabaseRealtimeHub.httpClient.newCall(req).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }

    suspend fun upsertRoomSeat(seat: VoiceRoomSeatDocument): Boolean = withContext(Dispatchers.IO) {
        SupabaseRealtimeHub.upsertSingleSeatInSnapshot(seat)
        if (!isConfigured) return@withContext true
        val payload = JSONObject().apply {
            put("seat_id", "${seat.roomId}_${seat.seatId}")
            put("room_id", seat.roomId)
            put("seat_index", seat.seatIndex)
            put("occupant_user_id", seat.occupantUserId)
            put("occupant_member_code", seat.occupantMemberCode.ifBlank {
                if (seat.occupantUserId.isNotBlank()) generateMemberIdCode(seat.occupantUserId) else ""
            })
            put("occupant_name", seat.occupantName)
            put("occupant_emoji", seat.occupantEmoji)
            put("seat_role", seat.seatRole)
            put("is_speaking", seat.isSpeaking)
            put("is_muted", seat.isMuted)
            put("has_pending_mic_request", seat.hasPendingMicRequest)
            put("is_seat_locked", seat.isSeatLocked)
            put("updated_at_epoch_ms", seat.updatedAt.toDate().time)
        }
        val endpoint = "$supabaseUrl/rest/v1/room_seats?on_conflict=seat_id"
        postJsonToSupabase(endpoint, payload.toString(), preferUpsert = true)
    }

    suspend fun upsertRoomSeatsBatch(roomId: String, seats: List<VoiceRoomSeatDocument>): Boolean =
        withContext(Dispatchers.IO) {
            SupabaseRealtimeHub.replaceRoomSeatsSnapshot(roomId, seats)
            if (!isConfigured) return@withContext true
            seats.forEach { seat -> upsertRoomSeat(seat) }
            true
        }

    suspend fun upsertRoomMicRequest(requestDoc: RoomMicRequestDocument): Boolean =
        withContext(Dispatchers.IO) {
            SupabaseRealtimeHub.upsertMicRequestInSnapshot(requestDoc)
            if (!isConfigured) return@withContext true
            val payload = JSONObject().apply {
                put("request_id", requestDoc.requestId)
                put("room_id", requestDoc.roomId)
                put("user_id", requestDoc.userId)
                put("member_id_code", requestDoc.memberIdCode.ifBlank { generateMemberIdCode(requestDoc.userId) })
                put("user_name", requestDoc.userName)
                put("user_emoji", requestDoc.userEmoji)
                put("queue_number", requestDoc.queueNumber)
                put("status", requestDoc.status)
                put("requested_seat_index", requestDoc.requestedSeatIndex)
                put("timestamp_epoch_ms", requestDoc.timestamp.toDate().time)
            }
            val endpoint = "$supabaseUrl/rest/v1/room_mic_requests?on_conflict=request_id"
            postJsonToSupabase(endpoint, payload.toString(), preferUpsert = true)
        }

    suspend fun deleteRoomMicRequestFromSupabase(roomId: String, requestId: String): Boolean =
        withContext(Dispatchers.IO) {
            SupabaseRealtimeHub.removeMicRequestFromSnapshot(roomId, requestId)
            if (!isConfigured) return@withContext true
            runCatching {
                val encodedId = URLEncoder.encode(requestId, "UTF-8")
                val endpoint = "$supabaseUrl/rest/v1/room_mic_requests?request_id=eq.$encodedId"
                val req = Request.Builder()
                    .url(endpoint)
                    .delete()
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $supabaseAnonKey")
                    .build()
                SupabaseRealtimeHub.httpClient.newCall(req).execute().use { it.isSuccessful }
            }.getOrDefault(false)
        }

    suspend fun upsertFadfadaPost(post: AnonymousFadfadaPost): Boolean = withContext(Dispatchers.IO) {
        SupabaseRealtimeHub.updateFadfadaSnapshot(listOf(post))
        if (!isConfigured) return@withContext true
        val payload = JSONObject().apply {
            put("post_id", post.postId)
            put("author_id", post.authorId)
            put("member_id_code", post.memberIdCode.ifBlank { generateMemberIdCode(post.authorId) })
            put("content", post.content)
            put("mood_tag", post.moodTag)
            put("hearts_count", post.heartsCount)
            put("timestamp_epoch_ms", post.timestamp.toDate().time)
        }
        val endpoint = "$supabaseUrl/rest/v1/fadfada_posts?on_conflict=post_id"
        postJsonToSupabase(endpoint, payload.toString(), preferUpsert = true)
    }

    suspend fun insertChatHistoryEntry(entry: MemberChatHistoryEntry): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext true
        val payload = JSONObject().apply {
            put("entry_id", entry.entryId)
            put("user_id", entry.userId)
            put("room_id", entry.roomId)
            put("room_name", entry.roomName)
            put("message_text", entry.messageText)
            put("activity_type", entry.activityType)
            put("timestamp_epoch_ms", entry.timestamp.toDate().time)
        }
        val endpoint = "$supabaseUrl/rest/v1/member_chat_history?on_conflict=entry_id"
        postJsonToSupabase(endpoint, payload.toString(), preferUpsert = true)
    }

    suspend fun recordRoomPresenceEventInSupabase(
        roomId: String,
        userId: String,
        memberName: String,
        eventType: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext true
        val payload = JSONObject().apply {
            put("event_id", "${eventType.lowercase()}_${System.currentTimeMillis()}")
            put("room_id", roomId)
            put("user_id", userId)
            put("member_name", memberName)
            put("event_type", eventType)
            put("timestamp_epoch_ms", System.currentTimeMillis())
        }
        val endpoint = "$supabaseUrl/rest/v1/room_presence_events"
        postJsonToSupabase(endpoint, payload.toString(), preferUpsert = false)
    }

    suspend fun fetchSharedRoomsFromSupabase(): List<ChatRoomMetadata> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext SupabaseRealtimeHub.realtimeRooms.value
        val arr = getJsonArrayFromSupabase("$supabaseUrl/rest/v1/chat_rooms?order=timestamp_epoch_ms.desc&limit=60")
        val list = mutableListOf<ChatRoomMetadata>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val roomId = obj.optString("room_id")
            val roomName = obj.optString("room_name")
            if (roomId.isNotBlank() && roomName.isNotBlank()) {
                val pass = obj.optString("room_password", "")
                val category = obj.optString("room_category", "GENERAL").ifBlank { "GENERAL" }
                val filterMeta = WanasRoomCategoryFilter.resolveDisplayFilter(category)
                val epoch = obj.optLong("timestamp_epoch_ms", System.currentTimeMillis())
                list.add(
                    ChatRoomMetadata(
                        roomId = roomId,
                        roomName = roomName,
                        creatorId = obj.optString("creator_id", "wanas_host"),
                        timestamp = Timestamp(Date(epoch)),
                        roomPassword = pass,
                        isPasswordProtected = obj.optBoolean("is_password_protected", pass.isNotBlank()),
                        roomCategory = filterMeta.id,
                        roomTopicTag = "${filterMeta.emoji} ${filterMeta.labelAr}"
                    )
                )
            }
        }
        if (list.isNotEmpty()) {
            SupabaseRealtimeHub.updateRoomsSnapshot(list)
        }
        SupabaseRealtimeHub.realtimeRooms.value
    }

    suspend fun fetchRoomSeatsFromSupabase(roomId: String): List<VoiceRoomSeatDocument> =
        withContext(Dispatchers.IO) {
            if (!isConfigured) return@withContext SupabaseRealtimeHub.realtimeSeatsByRoom.value[roomId].orEmpty()
            val encodedRoom = URLEncoder.encode(roomId, "UTF-8")
            val arr = getJsonArrayFromSupabase("$supabaseUrl/rest/v1/room_seats?room_id=eq.$encodedRoom&order=seat_index.asc")
            val seats = parseSeatsJsonArray(arr)
            if (seats.isNotEmpty()) {
                SupabaseRealtimeHub.replaceRoomSeatsSnapshot(roomId, seats)
            }
            SupabaseRealtimeHub.realtimeSeatsByRoom.value[roomId].orEmpty()
        }

    suspend fun fetchAllRoomSeatsFromSupabase() = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext
        val arr = getJsonArrayFromSupabase("$supabaseUrl/rest/v1/room_seats?order=seat_index.asc&limit=200")
        val seats = parseSeatsJsonArray(arr)
        seats.groupBy { it.roomId }.forEach { (rId, rSeats) ->
            SupabaseRealtimeHub.replaceRoomSeatsSnapshot(rId, rSeats)
        }
    }

    private fun parseSeatsJsonArray(arr: JSONArray): List<VoiceRoomSeatDocument> {
        val list = mutableListOf<VoiceRoomSeatDocument>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val rId = obj.optString("room_id")
            val idx = obj.optInt("seat_index", 0)
            if (rId.isNotBlank()) {
                val occUid = obj.optString("occupant_user_id", "")
                list.add(
                    VoiceRoomSeatDocument(
                        seatId = "seat_$idx",
                        roomId = rId,
                        seatIndex = idx,
                        occupantUserId = occUid,
                        occupantMemberCode = obj.optString("occupant_member_code").ifBlank {
                            if (occUid.isNotBlank()) generateMemberIdCode(occUid) else ""
                        },
                        occupantName = obj.optString("occupant_name", ""),
                        occupantEmoji = obj.optString("occupant_emoji", "🎙️"),
                        seatRole = obj.optString("seat_role", "EMPTY"),
                        isSpeaking = obj.optBoolean("is_speaking", false),
                        isMuted = obj.optBoolean("is_muted", true),
                        hasPendingMicRequest = obj.optBoolean("has_pending_mic_request", false),
                        isSeatLocked = obj.optBoolean("is_seat_locked", false),
                        updatedAt = Timestamp(Date(obj.optLong("updated_at_epoch_ms", System.currentTimeMillis())))
                    )
                )
            }
        }
        return list
    }

    suspend fun fetchRoomMicRequestsFromSupabase(roomId: String): List<RoomMicRequestDocument> =
        withContext(Dispatchers.IO) {
            if (!isConfigured) return@withContext SupabaseRealtimeHub.realtimeMicRequestsByRoom.value[roomId].orEmpty()
            val encodedRoom = URLEncoder.encode(roomId, "UTF-8")
            val arr = getJsonArrayFromSupabase("$supabaseUrl/rest/v1/room_mic_requests?room_id=eq.$encodedRoom&order=queue_number.asc")
            val reqs = parseMicRequestsJsonArray(arr)
            if (reqs.isNotEmpty()) {
                SupabaseRealtimeHub.replaceRoomMicRequestsSnapshot(roomId, reqs)
            }
            SupabaseRealtimeHub.realtimeMicRequestsByRoom.value[roomId].orEmpty()
        }

    suspend fun fetchAllMicRequestsFromSupabase() = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext
        val arr = getJsonArrayFromSupabase("$supabaseUrl/rest/v1/room_mic_requests?order=queue_number.asc&limit=100")
        val reqs = parseMicRequestsJsonArray(arr)
        reqs.groupBy { it.roomId }.forEach { (rId, rReqs) ->
            SupabaseRealtimeHub.replaceRoomMicRequestsSnapshot(rId, rReqs)
        }
    }

    private fun parseMicRequestsJsonArray(arr: JSONArray): List<RoomMicRequestDocument> {
        val list = mutableListOf<RoomMicRequestDocument>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val reqId = obj.optString("request_id")
            val rId = obj.optString("room_id")
            val uid = obj.optString("user_id")
            if (reqId.isNotBlank() && rId.isNotBlank()) {
                list.add(
                    RoomMicRequestDocument(
                        requestId = reqId,
                        roomId = rId,
                        userId = uid,
                        memberIdCode = obj.optString("member_id_code").ifBlank { generateMemberIdCode(uid) },
                        userName = obj.optString("user_name", "عضو ونس"),
                        userEmoji = obj.optString("user_emoji", "🙋"),
                        queueNumber = obj.optInt("queue_number", 1),
                        status = obj.optString("status", "PENDING"),
                        requestedSeatIndex = obj.optInt("requested_seat_index", 1),
                        timestamp = Timestamp(Date(obj.optLong("timestamp_epoch_ms", System.currentTimeMillis())))
                    )
                )
            }
        }
        return list
    }

    suspend fun fetchSharedMembersFromSupabase(): List<MemberProfile> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext SupabaseRealtimeHub.realtimeMembers.value
        val arr = getJsonArrayFromSupabase("$supabaseUrl/rest/v1/member_profiles?order=updated_at_epoch_ms.desc&limit=100")
        val list = mutableListOf<MemberProfile>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val uid = obj.optString("user_id")
            if (uid.isNotBlank()) {
                list.add(
                    MemberProfile(
                        userId = uid,
                        memberIdCode = obj.optString("member_id_code").ifBlank { generateMemberIdCode(uid) },
                        displayName = obj.optString("display_name", "عضو ونس"),
                        avatarEmoji = obj.optString("avatar_emoji", "🎙️"),
                        avatarUri = obj.optString("avatar_uri").takeIf { it.isNotBlank() && it != "null" },
                        bio = obj.optString("bio").takeIf { it.isNotBlank() && it != "null" },
                        roleBadge = obj.optString("role_badge", "VIP عضو"),
                        roomsCreatedCount = obj.optInt("rooms_created_count", 0),
                        roomsJoinedCount = obj.optInt("rooms_joined_count", 0),
                        messagesSentCount = obj.optInt("messages_sent_count", 0),
                        supabaseSynced = true,
                        updatedAt = Timestamp(Date(obj.optLong("updated_at_epoch_ms", System.currentTimeMillis())))
                    )
                )
            }
        }
        if (list.isNotEmpty()) {
            SupabaseRealtimeHub.updateMembersSnapshot(list)
        }
        SupabaseRealtimeHub.realtimeMembers.value
    }

    suspend fun fetchSharedFadfadaFromSupabase(): List<AnonymousFadfadaPost> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext SupabaseRealtimeHub.realtimeFadfadaPosts.value
        val arr = getJsonArrayFromSupabase("$supabaseUrl/rest/v1/fadfada_posts?order=timestamp_epoch_ms.desc&limit=60")
        val list = mutableListOf<AnonymousFadfadaPost>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val postId = obj.optString("post_id")
            val authorId = obj.optString("author_id")
            if (postId.isNotBlank()) {
                list.add(
                    AnonymousFadfadaPost(
                        postId = postId,
                        authorId = authorId,
                        memberIdCode = obj.optString("member_id_code").ifBlank { generateMemberIdCode(authorId) },
                        content = obj.optString("content", ""),
                        moodTag = obj.optString("mood_tag", "💭 فضفضة عامة"),
                        heartsCount = obj.optInt("hearts_count", 0),
                        timestamp = Timestamp(Date(obj.optLong("timestamp_epoch_ms", System.currentTimeMillis())))
                    )
                )
            }
        }
        if (list.isNotEmpty()) {
            SupabaseRealtimeHub.updateFadfadaSnapshot(list)
        }
        SupabaseRealtimeHub.realtimeFadfadaPosts.value
    }

    private fun getJsonArrayFromSupabase(endpoint: String): JSONArray {
        if (!isConfigured) return JSONArray()
        return runCatching {
            val request = Request.Builder()
                .url(endpoint)
                .get()
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Accept", "application/json")
                .build()
            SupabaseRealtimeHub.httpClient.newCall(request).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string().orEmpty()
                    if (body.trim().startsWith("[")) JSONArray(body) else JSONArray()
                } else {
                    JSONArray()
                }
            }
        }.getOrElse { JSONArray() }
    }

    private fun postJsonToSupabase(
        endpoint: String,
        bodyJson: String,
        preferUpsert: Boolean
    ): Boolean {
        if (!isConfigured) return false
        return runCatching {
            val builder = Request.Builder()
                .url(endpoint)
                .post(bodyJson.toRequestBody(jsonMediaType))
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Content-Type", "application/json")
            if (preferUpsert) {
                builder.addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            }
            SupabaseRealtimeHub.httpClient.newCall(builder.build()).execute().use { resp ->
                resp.isSuccessful
            }
        }.getOrDefault(false)
    }

    companion object {
        private const val PREFS_NAME = "wanas_supabase_account_prefs"
        private const val KEY_USER_ID = "saved_user_id"
        private const val KEY_NICKNAME = "saved_nickname"
        private const val KEY_EMAIL = "saved_email"
        private const val KEY_AVATAR_URI = "saved_avatar_uri"
        private const val KEY_BIO = "saved_bio"
        private const val KEY_ROLE_BADGE = "saved_role_badge"
        private const val KEY_IS_PAID_VIP = "saved_is_paid_vip"
        private const val KEY_PAID_PLAN_TITLE = "saved_paid_plan_title"
        private const val KEY_LAST_RECEIPT_ID = "saved_last_receipt_id"
        private const val KEY_ROOMS_CREATED = "saved_rooms_created"
        private const val KEY_ROOMS_JOINED = "saved_rooms_joined"
        private const val KEY_MESSAGES_SENT = "saved_messages_sent"
        private const val KEY_FOLLOWED_ROOM_IDS = "saved_followed_room_ids"
        private const val KEY_NOTIFY_FRIEND_STARTS_ROOM = "saved_notify_friend_starts_room"
        private const val KEY_NOTIFY_FAVORITE_ROOM_LIVE = "saved_notify_favorite_room_live"
        private const val KEY_NOTIFY_PRIVATE_INVITE = "saved_notify_private_invite"
        private const val KEY_PASSWORD_Prefix = "saved_pwd_"
    }
}
