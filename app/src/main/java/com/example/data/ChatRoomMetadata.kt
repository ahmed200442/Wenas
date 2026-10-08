package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

/**
 * Generates a deterministic unique member code (e.g. "WNS-482109") for each member
 * so their identity in Fadfada (anonymous venting), rooms, seats, and Supabase sync
 * has a consistent unique numeric code (`رقم معرف لكل عضو`).
 */
fun generateMemberIdCode(userId: String): String {
    val clean = userId.trim()
    if (clean.isEmpty()) return "WNS-100000"
    var hash = 2166136261L
    for (ch in clean) {
        hash = (hash xor ch.code.toLong()) * 16777619L
    }
    val numeric = ((hash and 0x7FFFFFFFL) % 900000L) + 100000L
    return "WNS-$numeric"
}

/**
 * Role-based access control (RBAC) assignment stored in `/user_roles/{userId}` in Firestore
 * and synchronized with Supabase.
 * The primary app owner `hamadanagy1979@gmail.com` always holds `OWNER` role with full permissions.
 */
data class UserRoleAssignment(
    val userId: String = "",
    val memberIdCode: String = "",
    val displayName: String = "",
    val role: String = "MEMBER",
    val permissions: List<String> = emptyList(),
    val assignedBy: String = "",
    val updatedAt: Timestamp = Timestamp.now()
)

data class ChatRoomMetadata(
    val roomId: String = "",
    val roomName: String = "",
    val creatorId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val roomTopicTag: String = "🔥 بث مباشر",
    val micSeatsCount: Int = 8,
    val roomCategory: String = "PUBLIC_PARTY",
    val pinnedAnnouncement: String = "",
    val isPinnedByAdmin: Boolean = false,
    val roomPassword: String = "",
    val isPasswordProtected: Boolean = false,
    val pkTeamRedScore: Int = 0,
    val pkTeamBlueScore: Int = 0,
    val roomOwnerName: String = "صاحب الغرفة 👑",
    val assignedRoomAdminName: String = "",
    val coHostMemberNames: List<String> = emptyList(),
    val bannedFromRoomMemberNames: List<String> = emptyList(),
    val mutedChatMemberNames: List<String> = emptyList(),
    val isRoomChatLocked: Boolean = false,
    val isSlowModeActive: Boolean = false,
    val isAdminOnlyChatMode: Boolean = false,
    val areRoomGiftsEnabled: Boolean = true,
    val roomRulesText: String = "احترام الجميع • عدم المقاطعة • التحدث بالدور عبر طلب المايك",
    val roomWelcomeMessage: String = "أهلاً بكم في غرفة وَنَس الصوتية! نتمنى لكم وقتاً ممتعاً",
    val roomDescription: String = "غرفة صوتية تفاعلية مباشرة في مجتمع وَنَس",
    val maxRoomMembersCapacity: Int = 100,
    val activeMembersCount: Int = 4,
    val activeSpeakersCount: Int = 2,
    val activeVoiceChallengeQuestion: String = "",
    val activeSoundEffectBanner: String = "",
    val isRoomLockedByAdmin: Boolean = false,
    val ambientThemeId: String = "ROYAL_NIGHT"
) {
    val timestampMillis: Long
        get() = timestamp.toDate().time

    val hasPassword: Boolean
        get() = isPasswordProtected || roomPassword.isNotBlank()

    val trendingRankScore: Int
        get() = (if (isPinnedByAdmin) 500 else 0) +
            (activeMembersCount * 15) +
            (activeSpeakersCount * 25) +
            pkTeamRedScore +
            pkTeamBlueScore
}

data class MemberPersonalStats(
    val roomsCreatedCount: Int = 0,
    val roomsJoinedCount: Int = 0,
    val messagesSentCount: Int = 0,
    val totalActivityScore: Int = 0
)

data class SupabaseMemberAccount(
    val userId: String = "",
    val memberIdCode: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarEmoji: String = "👑",
    val avatarUri: String = "",
    val bio: String = "أهلاً بكم في غرف ونس الصوتية ✨",
    val roleBadge: String = "عضو ونس",
    val roomsCreatedCount: Int = 0,
    val roomsJoinedCount: Int = 0,
    val messagesSentCount: Int = 0,
    val isPaidVip: Boolean = false,
    val paidPlanTitle: String = "",
    val supabaseSynced: Boolean = true,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class MemberProfile(
    val userId: String = "",
    val memberIdCode: String = "",
    val displayName: String = "",
    val avatarEmoji: String = "🎙️",
    val avatarUri: String? = null,
    val bio: String? = null,
    val roleBadge: String = "VIP عضو",
    val roomsCreatedCount: Int = 0,
    val roomsJoinedCount: Int = 0,
    val messagesSentCount: Int = 0,
    val supabaseSynced: Boolean = true,
    val updatedAt: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val updatedAtMillis: Long
        get() = updatedAt.toDate().time
}

data class MemberChatHistoryEntry(
    val entryId: String = "",
    val userId: String = "",
    val roomId: String = "",
    val roomName: String = "",
    val messageText: String = "",
    val activityType: String = "MESSAGE",
    val timestamp: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val timestampMillis: Long
        get() = timestamp.toDate().time
}

data class RoomActiveMember(
    val userId: String = "",
    val roomId: String = "",
    val memberName: String = "",
    val avatarEmoji: String = "🎙️",
    val roleBadge: String = "VIP عضو",
    val supabaseLinked: Boolean = true,
    val joinedAt: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val joinedAtMillis: Long
        get() = joinedAt.toDate().time
}

/**
 * Real-time synchronized seat on the 8-seat voice stage (`/chat_rooms/{roomId}/seats/{seatId}`),
 * visible to all users in the room across Supabase Realtime and Firebase.
 */
data class VoiceRoomSeatDocument(
    val seatId: String = "",
    val roomId: String = "",
    val seatIndex: Int = 0,
    val occupantUserId: String = "",
    val occupantMemberCode: String = "",
    val occupantName: String = "",
    val occupantEmoji: String = "🎙️",
    val seatRole: String = "EMPTY",
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false,
    val hasPendingMicRequest: Boolean = false,
    val isSeatLocked: Boolean = false,
    val updatedAt: Timestamp = Timestamp.now()
)

/**
 * Real-time synchronized mic request in a voice room (`/chat_rooms/{roomId}/mic_requests/{requestId}`),
 * ordered by queueNumber and visible to the host and all room participants.
 */
data class RoomMicRequestDocument(
    val requestId: String = "",
    val roomId: String = "",
    val userId: String = "",
    val memberIdCode: String = "",
    val userName: String = "",
    val userEmoji: String = "🙋",
    val queueNumber: Int = 1,
    val status: String = "PENDING",
    val requestedSeatIndex: Int = 1,
    val timestamp: Timestamp = Timestamp.now()
)

/**
 * Real-time synchronized room chat message, visible to all room participants.
 */
data class RoomChatMessage(
    val messageId: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderMemberCode: String = "",
    val senderName: String = "",
    val senderEmoji: String = "💬",
    val senderRoleBadge: String = "VIP عضو",
    val messageText: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val timestampMillis: Long
        get() = timestamp.toDate().time
}

typealias RoomChatMessageDocument = RoomChatMessage

data class RoomPresenceBannerEvent(
    val eventId: String = "",
    val roomId: String = "",
    val userId: String = "",
    val memberName: String = "",
    val avatarEmoji: String = "🎙️",
    val eventType: String = "JOIN",
    val createdAt: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val createdAtMillis: Long
        get() = createdAt.toDate().time

    @get:Exclude
    val isJoinEvent: Boolean
        get() = eventType.equals("JOIN", ignoreCase = true)

    @get:Exclude
    val bannerText: String
        get() = if (isJoinEvent) {
            "دخل العضو $memberName"
        } else {
            "خرج العضو $memberName"
        }
}

typealias RoomPresenceEvent = RoomPresenceBannerEvent

/**
 * Public anonymous Fadfada (venting) post stored in `/fadfada_posts/{postId}` and Supabase.
 * Regular users see the author's unique numeric member ID code (`memberIdCode`, e.g. `WNS-482109`)
 * without exposing their real name or email.
 */
data class AnonymousFadfadaPost(
    val postId: String = "",
    val authorId: String = "",
    val memberIdCode: String = "",
    val content: String = "",
    val moodTag: String = "💭 فضفضة عامة",
    val heartsCount: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val timestampMillis: Long
        get() = timestamp.toDate().time
}

typealias FadfadaPost = AnonymousFadfadaPost

/**
 * Admin-only identity record stored in `/fadfada_admin_identities/{postId}`.
 * Readable ONLY by App Owner (`hamadanagy1979@gmail.com`) and authorized admins.
 */
data class FadfadaAdminIdentity(
    val postId: String = "",
    val authorId: String = "",
    val memberIdCode: String = "",
    val authorName: String = "",
    val authorEmail: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val timestampMillis: Long
        get() = timestamp.toDate().time
}

data class PushNotificationEvent(
    val notificationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val recipientQuery: String = "",
    val roomId: String = "",
    val roomName: String = "",
    val notificationType: String = "NEW_MESSAGE",
    val messageBody: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
) {
    val timestampMillis: Long
        get() = timestamp.toDate().time

    val isFriendStartedRoom: Boolean
        get() = notificationType == "FRIEND_STARTED_ROOM"

    val isFollowedRoomActive: Boolean
        get() = notificationType == "FOLLOWED_ROOM_ACTIVE" || notificationType == "FAVORITE_ROOM_LIVE"

    val isFavoriteRoomLive: Boolean
        get() = isFollowedRoomActive

    val isPrivateInvite: Boolean
        get() = notificationType == "PRIVATE_INVITE"

    val isRoomInvite: Boolean
        get() = notificationType == "ROOM_INVITE" || isPrivateInvite || isFollowedRoomActive || isFriendStartedRoom

    val badgeCategoryAr: String
        get() = when (notificationType) {
            "FRIEND_STARTED_ROOM" -> "🎙️ صديقك بدأ غرفة صوتية جديدة"
            "FOLLOWED_ROOM_ACTIVE" -> "🔴 غرفة تتابعها أصبحت نشطة الآن"
            "FAVORITE_ROOM_LIVE" -> "🔴 بث مباشر لغرفة تتابعها"
            "PRIVATE_INVITE" -> "💌 دعوة خاصة"
            "ROOM_INVITE" -> "🎙️ دعوة للانضمام لغرفة"
            "FRIEND_REQUEST" -> "👥 طلب صداقة"
            "FRIEND_ACCEPTED" -> "✅ قبول صداقة"
            "MIC_REQUEST" -> "✋ طلب مايك"
            "MIC_APPROVED" -> "🎙️ قبول المايك"
            "GIFT_RECEIVED" -> "🎁 هدية جديدة"
            "ADMIN_ALERT" -> "🛡️ إشعار إداري"
            else -> "💬 رسالة جديدة"
        }

    val formattedTitle: String
        get() = when (notificationType) {
            "FRIEND_STARTED_ROOM" -> "🎙️ صديقك $senderName بدأ غرفة جديدة: «$roomName»"
            "FOLLOWED_ROOM_ACTIVE" -> "🔴 الغرفة التي تتابعها «$roomName» أصبحت نشطة الآن!"
            "FAVORITE_ROOM_LIVE" -> "🔴 غرفة تتابعها بدأت بثاً مباشراً: $roomName"
            "PRIVATE_INVITE" -> "💌 دعوة خاصة من $senderName إلى «$roomName»"
            "ROOM_INVITE" -> "🎙️ $senderName يدعوك للانضمام إلى «$roomName»"
            "FRIEND_REQUEST" -> "👥 طلب صداقة جديد من $senderName"
            "FRIEND_ACCEPTED" -> "✅ وافق $senderName على طلب صداقتك"
            "MIC_REQUEST" -> "✋ $senderName يطلب المايك في «$roomName»"
            "MIC_APPROVED" -> "🎙️ تمت الموافقة على فتح المايك لك في «$roomName»"
            "GIFT_RECEIVED" -> "🎁 هدية جديدة من $senderName في «$roomName»"
            "ADMIN_ALERT" -> "🛡️ تنبيه إداري في «$roomName»"
            else -> "💬 رسالة جديدة من $senderName في «$roomName»"
        }
}

data class VipPaymentPlan(
    val planId: String,
    val title: String,
    val badgeLabel: String,
    val amountEgp: Int,
    val description: String
)

data class VerifiedPaymentReceipt(
    val receiptId: String = "",
    val userId: String = "",
    val memberName: String = "",
    val planId: String = "",
    val planTitle: String = "",
    val amountEgp: Int = 0,
    val paymentMethod: String = "BANK_CARD",
    val transactionReference: String = "",
    val verified: Boolean = true,
    val timestamp: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val timestampMillis: Long
        get() = timestamp.toDate().time
}

typealias PaymentReceipt = VerifiedPaymentReceipt

data class PaymentValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val normalizedReference: String = ""
)

/**
 * Firestore document recording an interactive gift transaction sent between users
 * inside a voice room (`/chat_rooms/{roomId}/room_gifts/{transactionId}`) and
 * in the global ledger (`/gift_transactions/{transactionId}`).
 */
data class RoomGiftTransactionDocument(
    val transactionId: String = "",
    val roomId: String = "",
    val roomName: String = "",
    val giftId: String = "",
    val giftNameAr: String = "",
    val giftEmoji: String = "🎁",
    val costCoins: Int = 0,
    val quantity: Int = 1,
    val senderId: String = "",
    val senderMemberCode: String = "",
    val senderName: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val pkTeamSupported: String = "RED",
    val pkBonusPoints: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val timestampMillis: Long
        get() = timestamp.toDate().time
}

/**
 * Firestore document representing a WebRTC Native P2P Audio Signaling message
 * (`OFFER`, `ANSWER`, or `ICE_CANDIDATE`) stored in `/chat_rooms/{roomId}/webrtc_signals/{signalId}`.
 * Includes `sessionEpochMs` so newly joining members never replay stale signals.
 */
data class WebRtcSignalingDocument(
    val signalId: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val targetUserId: String = "",
    val signalType: String = "OFFER", // OFFER | ANSWER | ICE_CANDIDATE
    val sdp: String = "",
    val sdpMid: String = "",
    val sdpMLineIndex: Int = -1,
    val candidate: String = "",
    val sessionEpochMs: Long = 0L,
    val timestamp: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val timestampMillis: Long
        get() = timestamp.toDate().time
}
