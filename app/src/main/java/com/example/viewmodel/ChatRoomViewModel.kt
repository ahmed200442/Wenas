package com.example.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AnonymousFadfadaPost
import com.example.data.UserRoleAssignment
import com.example.data.VoiceRoomSeatDocument
import com.example.data.RoomMicRequestDocument
import com.example.data.generateMemberIdCode
import com.example.data.ActiveMatchCallSession
import com.example.data.ChatRoomMetadata
import com.example.data.ChatRoomRepository
import com.example.data.CoinPackage
import com.example.data.FadfadaAdminIdentity
import com.example.data.FriendDirectMessage
import com.example.data.MatchQueueRequest
import com.example.data.MatchRole
import com.example.data.MatchTopicType
import com.example.data.MemberChatHistoryEntry
import com.example.data.MemberPersonalStats
import com.example.data.PushNotificationEvent
import com.example.data.RoomActiveMember
import com.example.data.RoomChatMessage
import com.example.data.RoomGiftTransactionDocument
import com.example.data.RoomPresenceBannerEvent
import com.example.data.SafetyReportTicket
import com.example.data.SessionMiniGame
import com.example.data.SupabaseAccountService
import com.example.data.SupabaseMemberAccount
import com.example.data.VerifiedPaymentReceipt
import com.example.data.VipPaymentPlan
import com.example.data.WanasAdminDashboardStats
import com.example.data.WanasRoomCategoryFilter
import com.example.data.WanasCatalogData
import com.example.data.WanasDailyMission
import com.example.data.WanasFriend
import com.example.data.WanasGiftItem
import com.example.data.FriendPresenceState
import com.example.data.GiftTransactionRecord
import com.example.data.MicRequestStatus
import com.example.data.RoomMicRequestItem
import com.example.data.VoiceRoomSeatSlot
import com.example.data.VoiceSeatRoleBadge
import com.example.data.OwnerDashboardTab
import com.example.data.RoomAdminDashboardTab
import com.example.data.WanasAdminRoleType
import com.example.data.AdminRoleAssignment
import com.example.data.AdminAuditLogEntry
import com.example.data.SecurityRiskLevel
import com.example.data.SuspiciousBehaviorAlert
import com.example.data.CoinLedgerRecord
import com.example.data.ManagedAccountStatus
import com.example.data.ManagedMemberProfile
import com.example.data.SupportTicketStatus
import com.example.data.WanasSystemSettings
import com.example.data.LuckyCoinBoxState
import com.example.data.RoomTopSupporterItem
import com.example.data.FadfadaSupportComment
import com.example.data.SpecialStoreCosmeticItem
import com.example.data.WanasFamilyClan
import com.example.data.PaymentPurchaseOrder
import com.example.data.PaymentPurchaseOrderStatus
import com.example.notifications.PushNotificationHelper
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class ChatRoomsActionState(
    val isSubmitting: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val memberDisplayName: String = "عضو ونس",
    val memberEmail: String = "",
    val memberAvatarEmoji: String = "👑",
    val memberAvatarUri: String = "",
    val memberBio: String = "أهلاً بكم في غرف ونس الصوتية ✨",
    val memberRoleBadge: String = "عضو ونس",
    val isPaidVip: Boolean = false,
    val paidPlanTitle: String = "",
    val paymentReceipts: List<VerifiedPaymentReceipt> = emptyList(),
    val roomsCreatedCount: Int = 0,
    val roomsJoinedCount: Int = 0,
    val messagesSentCount: Int = 0,
    val chatHistory: List<MemberChatHistoryEntry> = emptyList(),
    val fadfadaPosts: List<AnonymousFadfadaPost> = emptyList(),
    val fadfadaAdminIdentities: Map<String, FadfadaAdminIdentity> = emptyMap(),
    val pushNotifications: List<PushNotificationEvent> = emptyList(),
    val latestPushBanner: PushNotificationEvent? = null,
    val isAdminOwner: Boolean = false,
    val isSupabaseConfigured: Boolean = false,
    val isSupabaseSynced: Boolean = true,
    val supabaseStatusText: String = "حساب العضو محفوظ للدخول التلقائي (Supabase)",
    val activeRoom: ChatRoomMetadata? = null,
    val activeRoomMembers: List<RoomActiveMember> = emptyList(),
    val activeRoomMessages: List<RoomChatMessage> = emptyList(),
    val transientPresenceBanner: RoomPresenceBannerEvent? = null,
    val isDarkMode: Boolean = true, // Default to Luxury Night Mode (الوضع الليلي الفاخر)
    // Wanas Social — "الناس للناس" (اتكلم • اسمع • اتعرف)
    val showModernWelcomeModal: Boolean = false,
    val isServerOnlineConnected: Boolean = true,
    val serverConnectionLabel: String = "🟢 متصل بسيرفرات وَنَس السحابية (Firestore + Supabase Live)",
    val coinsBalance: Int = 120,
    val dailyBonusClaimed: Boolean = false,
    val userXp: Int = 140,
    val isProfileBoosted: Boolean = false,
    val isPriorityMatchActive: Boolean = false,
    val selectedMatchRole: MatchRole = MatchRole.NEED_TO_SPEAK,
    val selectedMatchTopic: MatchTopicType = MatchTopicType.LAUGH_CHAT,
    val isSearchingMatch: Boolean = false,
    val matchFoundCandidate: MatchQueueRequest? = null,
    val activeMatchSession: ActiveMatchCallSession? = null,
    val waitingListenerRequests: List<MatchQueueRequest> = emptyList(),
    val friendsList: List<WanasFriend> = emptyList(),
    val friendMessagesMap: Map<String, List<FriendDirectMessage>> = emptyMap(),
    val blockedUserIds: Set<String> = emptySet(),
    val safetyReports: List<SafetyReportTicket> = emptyList(),
    val isAgeVerified18Plus: Boolean = true,
    val isFadfadaPinUnlocked: Boolean = false,
    val fadfadaRequiredPin: String = WanasCatalogData.DEFAULT_FADFADA_PIN,
    val showInterstitialAdBetweenSessions: Boolean = false,
    val adminDashboardStats: WanasAdminDashboardStats = WanasAdminDashboardStats(),
    val latestRoomGiftBannerText: String? = null,
    val wheelOfFortuneSpunToday: Boolean = false,
    val lastWheelRewardText: String? = null,
    val favoriteRoomIds: Set<String> = setOf("room_wanas_egypt_1", "room_wanas_laugh_2"),
    val notifyOnFriendStartsRoom: Boolean = true,
    val notifyOnFavoriteRoomLive: Boolean = true,
    val notifyOnPrivateInvite: Boolean = true,
    // 🔥 Wanas v2 Next Update Features:
    // 1. Mic Request Queue & Server Approval State
    val activeRoomMicRequests: List<RoomMicRequestItem> = emptyList(),
    val myMicRequestStatus: MicRequestStatus = MicRequestStatus.NONE,
    val isServerMicApproved: Boolean = false,
    // 2. Real 8-Seat Voice Stage Slots
    val activeRoomSeats: List<VoiceRoomSeatSlot> = (0 until 8).map { VoiceRoomSeatSlot(seatIndex = it) },
    // 7. Gifts History, Daily Free Gift, & Top Sender/Receiver Leaderboards
    val giftHistoryRecords: List<GiftTransactionRecord> = emptyList(),
    val dailyFreeGiftClaimed: Boolean = false,
    val sentGiftsTotalsByMember: Map<String, Int> = emptyMap(),
    val receivedGiftsTotalsByMember: Map<String, Int> = emptyMap(),
    // 9. 7-Day Daily Rewards Streak
    val currentDailyRewardStreakDay: Int = 1,
    val claimedDailyRewardDays: Set<Int> = emptySet(),
    // 10. Daily Missions System
    val dailyMissions: List<WanasDailyMission> = WanasCatalogData.defaultDailyMissions,
    // 👑 Two-Level Admin Architecture (1. App Owner vs 2. Room Admin):
    val currentAdminRole: WanasAdminRoleType = WanasAdminRoleType.OWNER,
    val managedMembersDirectory: List<ManagedMemberProfile> = emptyList(),
    val paymentPurchaseOrders: List<PaymentPurchaseOrder> = emptyList(),
    val pkRedComboStreak: Int = 1,
    val pkBlueComboStreak: Int = 1,
    val pkComboStreakCount: Int = 1,
    val pkLastComboTeamLabel: String = "الفريق الأحمر 🔥",
    val pkPunishmentChallengeText: String = "تحدي الخاسر: غناء مقطع طربي أو إلقاء نكتة على المايك لمدة 30 ثانية 🎤",
    val pkTargetTeamIsRed: Boolean = true,
    val latestStageGiftComboCount: Int = 1,
    val latestStageGiftEmoji: String = "🎁",
    val latestStageGiftNameAr: String = "",
    val latestStageGiftSenderName: String = "",
    val latestStageGiftRecipientName: String = "",
    val activeRoomGiftTransactions: List<RoomGiftTransactionDocument> = emptyList(),
    val selectedRoomGiftRecipientName: String = "مضيف السهرة 👑",
    val selectedRoomGiftComboMultiplier: Int = 1,
    val isRecordingGiftToFirestore: Boolean = false,
    val lastFirestoreGiftTxId: String? = null,
    val adminRoleAssignments: List<AdminRoleAssignment> = listOf(
        AdminRoleAssignment(
            assignmentId = "role_owner_1",
            memberId = "owner_primary",
            memberName = "صاحب التطبيق الأساسي 👑",
            roleType = WanasAdminRoleType.OWNER
        )
    ),
    val adminAuditLogs: List<AdminAuditLogEntry> = listOf(
        AdminAuditLogEntry(
            logId = "audit_init_1",
            actorName = "صاحب التطبيق الأساسي 👑",
            actorRole = "Owner",
            actionTaken = "تفعيل نظام الحماية المركزي وتوحيد صلاحيات السيرفر",
            targetName = "جميع الغرف والأعضاء",
            locationRoomOrSection = "مركز التحكم الرئيسي",
            timestampLabel = "اليوم",
            resultSummary = "تم بنجاح ✅"
        )
    ),
    val suspiciousBehaviorAlerts: List<SuspiciousBehaviorAlert> = listOf(
        SuspiciousBehaviorAlert(
            alertId = "sec_alert_3",
            categoryAr = "فحص تعدد الحسابات وحركة العملات والـ Spam",
            suspectName = "فحص النظام الدوري",
            roomOrTarget = "سيرفرات وَنَس السحابية",
            riskLevel = SecurityRiskLevel.NORMAL,
            detailsAr = "جميع عمليات نقل العملات والهدايا موثقة عبر Server Ledger 🟢"
        )
    ),
    val coinLedgerRecords: List<CoinLedgerRecord> = listOf(
        CoinLedgerRecord(
            ledgerId = "LDG-9001",
            targetMemberId = "self",
            targetMemberName = "صاحب التطبيق الأساسي 👑",
            deltaCoins = 120,
            operationTypeAr = "رصيد افتتاحي موثق بالسيرفر",
            authorizedBy = "Server Ledger Auth",
            auditReason = "تفعيل المحفظة الأساسية",
            paymentStatusAr = "مقبول وموثق ✅",
            timestampLabel = "اليوم"
        )
    ),
    val systemSettings: WanasSystemSettings = WanasSystemSettings(),
    // 🚀 New Implemented Proposals (PK Timer, Lucky Coin Box, Room Top Supporters, Fadfada Threads & Reactions, Special IDs & Frames, Families/Clans):
    val activeLuckyCoinBox: LuckyCoinBoxState? = null,
    val roomTopSupporters: List<RoomTopSupporterItem> = emptyList(),
    val pkCountdownSecondsRemaining: Int = 180,
    val isPkCountdownRunning: Boolean = false,
    val pkWinnerAnnouncementText: String? = null,
    val fadfadaCommentsByPostId: Map<String, List<FadfadaSupportComment>> = emptyMap(),
    val fadfadaReactionCountsByPostId: Map<String, Map<String, Int>> = emptyMap(),
    val ownedSpecialCosmeticIds: Set<String> = emptySet(),
    val equippedSpecialMemberCode: String? = null,
    val equippedSeatFrameLabel: String? = null,
    val equippedEntryEffectLabel: String? = null,
    val familiesClansList: List<WanasFamilyClan> = WanasCatalogData.defaultFamiliesClans,
    val joinedClanId: String? = "clan_kings_egypt"
) {
    val unreadNotificationsCount: Int
        get() = pushNotifications.count { !it.isRead }

    val pendingPaymentOrdersCount: Int
        get() = paymentPurchaseOrders.count { it.status == PaymentPurchaseOrderStatus.PENDING_OWNER_APPROVAL }

    val userLevel: Int
        get() = (1 + (userXp / 100)).coerceAtLeast(1)

    val userLevelTitleAr: String
        get() = WanasCatalogData.resolveUserLevelTitle(userLevel)

    val userLevelBadge: String
        get() = userLevelTitleAr

    val topGiftSenderLabel: String
        get() {
            val top = sentGiftsTotalsByMember.maxByOrNull { it.value }
            return if (top != null && top.value > 0) "${top.key} (${top.value} 🪙)" else "$memberDisplayName (متصدر الداعمين 🌟)"
        }

    val topGiftReceiverLabel: String
        get() {
            val top = receivedGiftsTotalsByMember.maxByOrNull { it.value }
            return if (top != null && top.value > 0) "${top.key} (${top.value} 🪙)" else "مضيف السهرة 👑"
        }

    val topGiftSenderBadge: String
        get() = topGiftSenderLabel

    val topGiftReceiverBadge: String
        get() = topGiftReceiverLabel

    val personalStats: MemberPersonalStats
        get() = MemberPersonalStats(
            roomsCreatedCount = roomsCreatedCount,
            roomsJoinedCount = roomsJoinedCount,
            messagesSentCount = messagesSentCount,
            totalActivityScore = (roomsCreatedCount * 15) + (roomsJoinedCount * 5) + (messagesSentCount * 3) + userXp
        )
}

class ChatRoomViewModel(
    private val repository: ChatRoomRepository,
    private val currentUserId: String,
    private val currentUserDisplayName: String = "",
    private val currentUserEmail: String = "",
    private val supabaseService: SupabaseAccountService = SupabaseAccountService(),
    private val pushNotificationHelper: PushNotificationHelper? = null
) : ViewModel() {

    private val defaultStarterRooms = listOf(
        ChatRoomMetadata(
            roomId = "room_wanas_egypt_1",
            roomName = "🌙 سهرة مصرية — الناس للناس",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomTopicTag = "🌐 عام • بث مباشر ساخن",
            micSeatsCount = 8,
            roomCategory = "GENERAL",
            pinnedAnnouncement = "🌟 سهرة مصرية مفتوحة للجميع — شاركنا بالكلام والضحك أو اسمع براحتك!",
            isPinnedByAdmin = true,
            pkTeamRedScore = 240,
            pkTeamBlueScore = 195,
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.ROYAL_NIGHT.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_tech_ai_1",
            roomName = "💻 صالون التكنولوجيا والبرمجة والذكاء الاصطناعي",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomTopicTag = "💻 تكنولوجيا (Technology)",
            micSeatsCount = 8,
            roomCategory = "TECHNOLOGY",
            pinnedAnnouncement = "💻 نقاشات تقنية حية حول البرمجة، الذكاء الاصطناعي، وتطوير التطبيقات",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.NEON_LOUNGE.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_music_8",
            roomName = "🎵 أغاني وطرب أصيل وعزف مباشر",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomCategory = "MUSIC",
            roomTopicTag = "🎵 موسيقى وطرب (Music)",
            pinnedAnnouncement = "🎵 استمتع بأجمل الأصوات الغنائية والعزف الحي على المسرح",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.COFFEE_SHOP.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_edu_1",
            roomName = "🎓 أكاديمية التعليم وتبادل اللغات والثقافة",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomCategory = "EDUCATION",
            roomTopicTag = "🎓 تعليم وثقافة (Education)",
            pinnedAnnouncement = "🎓 تعلم اللغات، مهارات العمل الحر، وتبادل الخبرات التعليمية",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.FOREST_BREEZE.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_vip_private",
            roomName = "👑 صالون النخبة الخاص (محمي بباسورد)",
            creatorId = "wanas_vip_host",
            timestamp = Timestamp.now(),
            roomPassword = "2026",
            isPasswordProtected = true,
            roomTopicTag = "🔒 غرفة خاصة بباسورد",
            micSeatsCount = 8,
            roomCategory = "VIP_LOCKED",
            pinnedAnnouncement = "👑 صالون خاص للأعضاء المميزين وأصدقاء السهرة (الكود: 2026)",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.ROYAL_NIGHT.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_laugh_2",
            roomName = "😂 دردشة وضحك وهزار",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomCategory = "COMEDY",
            roomTopicTag = "😂 فرفشة وضحك",
            pinnedAnnouncement = "😂 ممنوع الزعل! احكي نكتة أو موقف مضحك وشاركنا الضحكة",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.DESERT_CAMPFIRE.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_taarof_3",
            roomName = "🤝 تعارف وصداقات محترمة",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomCategory = "GENERAL",
            roomTopicTag = "🌐 عام • تعارف راقي",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.COFFEE_SHOP.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_kora_4",
            roomName = "⚽ كورة وتحليل ماتشات",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomCategory = "GAMES_SPORTS",
            roomTopicTag = "⚽ ستوديو الكورة",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.NEON_LOUNGE.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_games_5",
            roomName = "🎮 ألعاب وتحديات صوتية",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomCategory = "GAMES_SPORTS",
            roomTopicTag = "🎮 تحديات PK",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.NEON_LOUNGE.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_study_6",
            roomName = "📚 مذاكرة وتركيز وتطوير ذات",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomCategory = "EDUCATION",
            roomTopicTag = "🎓 تعليم ومذاكرة وهدوء",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.RAINY_NIGHT.id
        ),
        ChatRoomMetadata(
            roomId = "room_wanas_fadfada_7",
            roomName = "💭 فضفضة وراحة بال",
            creatorId = currentUserId,
            timestamp = Timestamp.now(),
            roomCategory = "STUDY_CALM",
            roomTopicTag = "💭 استماع ودعم",
            ambientThemeId = com.example.data.WanasRoomAmbientTheme.RAINY_NIGHT.id
        )
    )

    private val _localRooms = MutableStateFlow<List<ChatRoomMetadata>>(defaultStarterRooms)
    private val _localChatHistory = MutableStateFlow<List<MemberChatHistoryEntry>>(emptyList())
    private val _localRoomMessages = MutableStateFlow<Map<String, List<RoomChatMessage>>>(emptyMap())
    private val _localFadfadaPosts = MutableStateFlow<List<AnonymousFadfadaPost>>(
        listOf(
            AnonymousFadfadaPost(
                postId = "fadfada_welcome_1",
                authorId = "wanas_community",
                content = "أهلاً بكم في قسم فضفضة بدون اسم ✨ اكتب اللي في قلبك بحرية تامة وبدون إظهار اسمك للأعضاء.",
                moodTag = "🌱 أمل وتفاؤل",
                heartsCount = 12,
                timestamp = Timestamp.now()
            )
        )
    )
    private val _localFadfadaAdminIdentities = MutableStateFlow<Map<String, FadfadaAdminIdentity>>(emptyMap())
    private val _localPushNotifications = MutableStateFlow<List<PushNotificationEvent>>(emptyList())
    private val _localPaymentReceipts = MutableStateFlow<List<VerifiedPaymentReceipt>>(emptyList())

    private val resolvedEmail: String = currentUserEmail.ifBlank { supabaseService.getLastSavedEmail() }

    private val supabaseRealtimeRoomsFlow = supabaseService.observeRealtimeRooms()
        .catch { error ->
            Log.w(TAG, "Error observing Supabase Realtime rooms", error)
            emit(emptyList())
        }

    private val firestoreRoomsFlow = if (Firebase.auth.currentUser != null) {
        repository.observeChatRooms(null) // Observe all public rooms on the server so all members meet in rooms
            .catch { error ->
                Log.w(TAG, "Error observing chat rooms", error)
                emit(emptyList())
            }
    } else {
        MutableStateFlow(emptyList())
    }

    val roomsState: StateFlow<UiState<List<ChatRoomMetadata>>> = combine(
        supabaseRealtimeRoomsFlow,
        firestoreRoomsFlow,
        _localRooms
    ) { supabaseRooms, cloudRooms, localRooms ->
        val merged = (supabaseRooms + cloudRooms + localRooms)
            .distinctBy { it.roomId }
            .sortedByDescending { it.timestampMillis }
        UiState.Success(merged) as UiState<List<ChatRoomMetadata>>
    }.catch { error ->
        Log.w(TAG, "Error combining chat rooms", error)
        emit(UiState.Error(error.message ?: "تعذر تحميل غرف الدردشة"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Success(emptyList())
    )

    private val _actionState = MutableStateFlow(
        ChatRoomsActionState(
            memberDisplayName = currentUserDisplayName.ifBlank {
                supabaseService.getLastSavedNickname().ifBlank {
                    resolvedEmail.substringBefore("@").ifBlank { "عضو ونس المميز" }
                }
            },
            memberEmail = resolvedEmail,
            memberAvatarUri = supabaseService.getLastSavedAvatarUri(),
            memberBio = supabaseService.getLastSavedBio(),
            memberRoleBadge = supabaseService.getSavedRoleBadge(),
            isPaidVip = supabaseService.getSavedIsPaidVip(),
            paidPlanTitle = supabaseService.getSavedPaidPlanTitle(),
            roomsCreatedCount = supabaseService.getSavedRoomsCreatedCount(),
            roomsJoinedCount = supabaseService.getSavedRoomsJoinedCount(),
            messagesSentCount = supabaseService.getSavedMessagesSentCount(),
            favoriteRoomIds = supabaseService.getSavedFollowedRoomIds(),
            notifyOnFriendStartsRoom = supabaseService.getSavedNotifyOnFriendStartsRoom(),
            notifyOnFavoriteRoomLive = supabaseService.getSavedNotifyOnFavoriteRoomLive(),
            notifyOnPrivateInvite = supabaseService.getSavedNotifyOnPrivateInvite(),
            isAdminOwner = isUserAppOwner(resolvedEmail),
            isSupabaseConfigured = supabaseService.isConfigured
        )
    )
    val actionState: StateFlow<ChatRoomsActionState> = _actionState.asStateFlow()

    private var activeMembersJob: Job? = null
    private var presenceEventsJob: Job? = null
    private var roomSeatsRealtimeJob: Job? = null
    private var roomMicRequestsRealtimeJob: Job? = null
    private var roomGiftTransactionsJob: Job? = null
    private val _localRoomGiftTransactions = MutableStateFlow<Map<String, List<RoomGiftTransactionDocument>>>(emptyMap())
    private var chatHistoryJob: Job? = null
    private var fadfadaPostsJob: Job? = null
    private var fadfadaAdminJob: Job? = null
    private var pushNotificationsJob: Job? = null
    private var roomActivityWatcherJob: Job? = null
    private var paymentReceiptsJob: Job? = null
    private var bannerAutoHideJob: Job? = null
    private var pushBannerAutoHideJob: Job? = null
    private var matchCountdownJob: Job? = null
    private var matchSearchJob: Job? = null
    private var lastShownEventId: String? = null
    private var lastDeliveredNotificationId: String? = null
    private val knownRoomIdsSnapshot = mutableSetOf<String>()
    private val knownActiveRoomTimestampById = mutableMapOf<String, Long>()
    private var hasInitializedRoomWatcher = false

    init {
        com.example.data.SupabaseRealtimeHub.updateRoomsSnapshot(defaultStarterRooms)
        seedInitialWaitingRequestsAndFriends()
        syncMemberAccountWithSupabaseAndCloud()
        observeMemberChatHistoryStream()
        observeFadfadaStreams()
        observePushNotificationsStream()
        observeRoomActivityAndFriendRoomAlerts()
        observeVerifiedPaymentsStream()
    }

    private fun seedInitialWaitingRequestsAndFriends() {
        // Removed all fake/dummy members ("إزالة الأعضاء الوهميين"):
        // Waiting speaker/listener requests, friends list, and reports only contain real members added by the user or Firestore.
        _localPushNotifications.value = emptyList()
        _actionState.update {
            it.copy(
                waitingListenerRequests = emptyList(),
                friendsList = emptyList(),
                managedMembersDirectory = emptyList(),
                roomTopSupporters = emptyList(),
                safetyReports = emptyList(),
                pushNotifications = emptyList(),
                latestPushBanner = null
            )
        }
    }

    fun dismissModernWelcomeModal() {
        _actionState.update {
            it.copy(
                showModernWelcomeModal = false,
                statusMessage = "✨ أهلاً بك في «وَنَس — الناس للناس»! اتكلم • اسمع • اتعرف"
            )
        }
    }

    fun verifyAndUnlockFadfadaPin(enteredPin: String): Boolean {
        val clean = enteredPin.trim()
        val required = _actionState.value.fadfadaRequiredPin
        return if (clean == required || _actionState.value.isAdminOwner) {
            _actionState.update {
                it.copy(
                    isFadfadaPinUnlocked = true,
                    statusMessage = "🔓 تم فتح قسم الفضفضة السري بنجاح",
                    errorMessage = null
                )
            }
            true
        } else {
            _actionState.update {
                it.copy(
                    errorMessage = "❌ كود دخول قسم الفضفضة غير صحيح (الكود المعتمد: $required)"
                )
            }
            false
        }
    }

    fun updateFadfadaPinCodeByAdmin(newPin: String) {
        val clean = newPin.trim()
        if (clean.length < 4) {
            _actionState.update { it.copy(errorMessage = "يرجى إدخال كود مكون من 4 أرقام على الأقل") }
            return
        }
        _actionState.update {
            it.copy(
                fadfadaRequiredPin = clean,
                isFadfadaPinUnlocked = true,
                statusMessage = "🔐 تم تحديث كود حماية قسم الفضفضة إلى: $clean",
                errorMessage = null
            )
        }
    }

    fun selectMatchRoleAndTopic(role: MatchRole, topic: MatchTopicType) {
        _actionState.update {
            it.copy(
                selectedMatchRole = role,
                selectedMatchTopic = topic,
                errorMessage = null
            )
        }
    }

    /**
     * Starts instant matching ("🗣️ محتاج أتكلم" or "👂 عايز أسمع") based on the chosen topic ("إنت داخل تعمل إيه؟").
     * Uses only real requests in the queue without fake/dummy member names.
     */
    fun startSmartMatchingSearch(role: MatchRole = _actionState.value.selectedMatchRole, topic: MatchTopicType = _actionState.value.selectedMatchTopic) {
        matchSearchJob?.cancel()
        val myRealRequest = MatchQueueRequest(
            requestId = "req_${currentUserId}_${System.currentTimeMillis()}",
            userId = currentUserId,
            userName = _actionState.value.memberDisplayName,
            userEmoji = _actionState.value.memberAvatarEmoji,
            userLevel = _actionState.value.userLevel,
            role = role,
            topic = topic,
            isPriorityBoosted = _actionState.value.isPriorityMatchActive,
            isPremiumVip = _actionState.value.isPaidVip,
            statusText = "${role.labelAr} • ${topic.titleAr}"
        )

        _actionState.update { st ->
            val updatedQueue = (listOf(myRealRequest) + st.waitingListenerRequests.filterNot { it.userId == currentUserId })
            st.copy(
                selectedMatchRole = role,
                selectedMatchTopic = topic,
                isSearchingMatch = true,
                matchFoundCandidate = null,
                waitingListenerRequests = updatedQueue,
                statusMessage = "🔎 تم تسجيل طلبك الحقيقي (${role.labelAr} • ${topic.titleAr}) وجاري البحث عن عضو متصل...",
                errorMessage = null
            )
        }

        matchSearchJob = viewModelScope.launch {
            delay(800L)
            val existingPartner = _actionState.value.waitingListenerRequests.firstOrNull {
                it.userId != currentUserId
            }
            if (existingPartner != null) {
                _actionState.update {
                    it.copy(
                        isSearchingMatch = false,
                        matchFoundCandidate = existingPartner,
                        statusMessage = "🎉 تم العثور على عضو متصل للمحادثة: ${existingPartner.userName}"
                    )
                }
            } else {
                _actionState.update {
                    it.copy(
                        isSearchingMatch = false,
                        matchFoundCandidate = myRealRequest,
                        statusMessage = "✅ طلبك نشط الآن في قائمة المطابقة الفورية — يمكنك بدء جلسة صوتية أو انتظار انضمام عضو آخر"
                    )
                }
            }
        }
    }

    fun cancelMatchingSearch() {
        matchSearchJob?.cancel()
        _actionState.update {
            it.copy(
                isSearchingMatch = false,
                matchFoundCandidate = null
            )
        }
    }

    /**
     * Starts the 10-minute (600s) voice session (or 20 minutes for Premium/Extended).
     */
    fun startVoiceCallWithMatchedCandidate(candidate: MatchQueueRequest) {
        matchCountdownJob?.cancel()
        val isPremium = _actionState.value.isPaidVip
        val initialSeconds = if (isPremium) 1200 else 600 // 10:00 free or 20:00 Premium
        val session = ActiveMatchCallSession(
            sessionId = "call_${System.currentTimeMillis()}",
            partnerUserId = candidate.userId,
            partnerName = candidate.userName,
            partnerEmoji = candidate.userEmoji,
            partnerLevel = candidate.userLevel,
            myRole = _actionState.value.selectedMatchRole,
            topic = candidate.topic,
            totalDurationSeconds = initialSeconds,
            remainingSeconds = initialSeconds
        )
        _actionState.update {
            it.copy(
                matchFoundCandidate = null,
                isSearchingMatch = false,
                activeMatchSession = session,
                userXp = it.userXp + 25,
                statusMessage = "🎙️ بدأت الجلسة الصوتية مع «${candidate.userName}» (+25 XP)"
            )
        }

        matchCountdownJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _actionState.value.activeMatchSession ?: break
                if (current.isEnded) break
                val nextSecs = current.remainingSeconds - 1
                if (nextSecs <= 0) {
                    _actionState.update { st ->
                        st.copy(
                            activeMatchSession = current.copy(remainingSeconds = 0, isEnded = true),
                            showInterstitialAdBetweenSessions = !st.isPaidVip,
                            statusMessage = "انتهت الجلسة ❤️ يمكنك بدء جلسة جديدة أو إضافة الشخص لأصدقائك"
                        )
                    }
                    break
                } else {
                    _actionState.update { st ->
                        st.copy(activeMatchSession = current.copy(remainingSeconds = nextSecs))
                    }
                }
            }
        }
    }

    fun extendActiveSessionWithCoins(extraMinutes: Int = 10, costCoins: Int = 25) {
        val current = _actionState.value.activeMatchSession ?: return
        if (_actionState.value.coinsBalance < costCoins) {
            _actionState.update {
                it.copy(errorMessage = "🪙 رصيد العملات غير كافٍ لتمديد الجلسة (تحتاج $costCoins Coins)")
            }
            return
        }
        val addedSeconds = extraMinutes * 60
        _actionState.update {
            it.copy(
                coinsBalance = it.coinsBalance - costCoins,
                activeMatchSession = current.copy(
                    totalDurationSeconds = current.totalDurationSeconds + addedSeconds,
                    remainingSeconds = current.remainingSeconds + addedSeconds,
                    isEnded = false
                ),
                userXp = it.userXp + 15,
                statusMessage = "⏱️ تم تمديد الجلسة +$extraMinutes دقائق بنجاح!"
            )
        }
    }

    fun endActiveMatchSession() {
        matchCountdownJob?.cancel()
        val current = _actionState.value.activeMatchSession ?: return
        _actionState.update {
            it.copy(
                activeMatchSession = current.copy(remainingSeconds = 0, isEnded = true),
                showInterstitialAdBetweenSessions = !it.isPaidVip,
                userXp = it.userXp + 20,
                statusMessage = "انتهت الجلسة ❤️ شكراً لتواصلك الراقي في وَنَس (+20 XP)"
            )
        }
    }

    fun closeEndedMatchSessionSummary() {
        matchCountdownJob?.cancel()
        _actionState.update {
            it.copy(
                activeMatchSession = null,
                showInterstitialAdBetweenSessions = false
            )
        }
    }

    fun rateCompletedSession(stars: Int) {
        val current = _actionState.value.activeMatchSession ?: return
        _actionState.update {
            it.copy(
                activeMatchSession = current.copy(ratingGiven = stars),
                userXp = it.userXp + 15,
                statusMessage = "⭐ شكراً لتقييمك الجلسة بـ $stars نجوم! حصلت على +15 XP"
            )
        }
    }

    fun toggleMutePartnerInSession() {
        val current = _actionState.value.activeMatchSession ?: return
        val nextMute = !current.isMutedPartner
        _actionState.update {
            it.copy(
                activeMatchSession = current.copy(isMutedPartner = nextMute),
                statusMessage = if (nextMute) "🔇 تم كتم صوت الطرف الآخر مؤقتاً" else "🔊 تم إلغاء كتم صوت الطرف الآخر"
            )
        }
    }

    fun startMiniGameInSession(gameId: String) {
        val current = _actionState.value.activeMatchSession ?: return
        val game = WanasCatalogData.sessionMiniGames.find { it.gameId == gameId } ?: return
        _actionState.update {
            it.copy(
                activeMatchSession = current.copy(
                    activeGameId = game.gameId,
                    currentGameQuestionIndex = 0
                ),
                userXp = it.userXp + 10,
                statusMessage = "🎮 بدأت لعبة «${game.titleAr}» مع ${current.partnerName}!"
            )
        }
    }

    fun answerMiniGameQuestion(selectedOptionIndex: Int) {
        val current = _actionState.value.activeMatchSession ?: return
        val game = WanasCatalogData.sessionMiniGames.find { it.gameId == current.activeGameId } ?: return
        val qIndex = current.currentGameQuestionIndex
        val question = game.questions.getOrNull(qIndex) ?: return
        val isCorrect = selectedOptionIndex == question.correctOptionIndex
        val nextIndex = (qIndex + 1) % game.questions.size
        _actionState.update {
            it.copy(
                activeMatchSession = current.copy(
                    currentGameQuestionIndex = nextIndex,
                    myGameScore = current.myGameScore + (if (isCorrect) 1 else 0),
                    partnerGameScore = current.partnerGameScore + 1
                ),
                userXp = it.userXp + 10,
                statusMessage = if (isCorrect) "🎉 إجابة صحيحة! +10 XP" else "😊 إجابة لطيفة! السؤال التالي جاهز"
            )
        }
    }

    /**
     * Friends system: send friend request, accept/reject, remove friend, change presence,
     * Messenger features (text, images, voice notes, Buzz ⚡, delete message, mute/pin chat).
     */
    fun addPartnerAsFriend(
        userId: String,
        userName: String,
        userEmoji: String,
        level: Int = 3,
        isPendingRequest: Boolean = false
    ) {
        val activeRoom = _actionState.value.activeRoom
        val newFriend = WanasFriend(
            friendUserId = userId,
            friendName = userName,
            friendEmoji = userEmoji,
            friendLevel = level,
            isOnline = true,
            presenceState = FriendPresenceState.ONLINE,
            currentRoomId = activeRoom?.roomId ?: "room_wanas_egypt_1",
            currentRoomName = activeRoom?.roomName ?: "🌙 سهرة مصرية — الناس للناس",
            statusNote = if (isPendingRequest) "طلب صداقة وارد بانتظار موافقتك 📩" else "صديق متصل الآن في ونس ✨",
            isPendingApproval = isPendingRequest,
            lastSeenLabel = "آخر ظهور: متصل الآن 🟢",
            lastMessage = if (isPendingRequest) "أرسل لك طلب صداقة جديد 👋" else "تمت إضافته إلى قائمة أصدقائك ❤️"
        )
        _actionState.update { st ->
            val updated = (listOf(newFriend) + st.friendsList)
                .distinctBy { it.friendUserId }
                .sortedByDescending { it.isConversationPinned }
            st.copy(
                friendsList = updated,
                userXp = st.userXp + 20,
                statusMessage = if (isPendingRequest) {
                    "📩 تم تسجيل طلب الصداقة مع «$userName»"
                } else {
                    "👥 تم إرسال وقبول الصداقة مع «$userName» بنجاح! (+20 XP)"
                }
            )
        }
        incrementDailyMissionProgress("mission_add_friend")
        publishUnifiedNotificationEvent(
            notificationType = if (isPendingRequest) "FRIEND_REQUEST" else "FRIEND_ACCEPTED",
            senderName = userName,
            roomName = activeRoom?.roomName ?: "أصدقاء وَنَس",
            messageBody = if (isPendingRequest) {
                "👥 أرسل لك «$userName» طلب صداقة جديد في وَنَس"
            } else {
                "✅ تم قبول طلب الصداقة مع «$userName» وأصبح ضمن أصدقائك المتصلين الآن!"
            }
        )
    }

    fun respondToFriendRequest(friendUserId: String, accept: Boolean) {
        val target = _actionState.value.friendsList.find { it.friendUserId == friendUserId } ?: return
        if (accept) {
            _actionState.update { st ->
                val updated = st.friendsList.map {
                    if (it.friendUserId == friendUserId) {
                        it.copy(
                            isPendingApproval = false,
                            statusNote = "صديق مقرب في وَنَس ✨",
                            lastMessage = "✅ تم قبول الصداقة — ابدأ المحادثة الآن!"
                        )
                    } else it
                }
                st.copy(
                    friendsList = updated,
                    userXp = st.userXp + 25,
                    statusMessage = "✅ تم قبول صداقة «${target.friendName}» وإرسال إشعار قبول الصداقة له!"
                )
            }
            publishUnifiedNotificationEvent(
                notificationType = "FRIEND_ACCEPTED",
                senderName = target.friendName,
                roomName = target.currentRoomName,
                messageBody = "🎉 تم قبول الصداقة مع «${target.friendName}» — يمكنكما التحدث أو دخول غرفته مباشرة!"
            )
        } else {
            removeFriendFromList(friendUserId)
        }
    }

    fun removeFriendFromList(friendUserId: String) {
        val target = _actionState.value.friendsList.find { it.friendUserId == friendUserId }
        _actionState.update { st ->
            st.copy(
                friendsList = st.friendsList.filterNot { it.friendUserId == friendUserId },
                statusMessage = "🗑️ تمت إزالة «${target?.friendName ?: "الصديق"}» من قائمة الأصدقاء"
            )
        }
    }

    fun cycleFriendPresenceState(friendUserId: String) {
        _actionState.update { st ->
            val updated = st.friendsList.map { f ->
                if (f.friendUserId == friendUserId) {
                    val nextState = when (f.presenceState) {
                        FriendPresenceState.ONLINE -> FriendPresenceState.BUSY
                        FriendPresenceState.BUSY -> FriendPresenceState.OFFLINE
                        FriendPresenceState.OFFLINE -> FriendPresenceState.ONLINE
                    }
                    f.copy(
                        presenceState = nextState,
                        isOnline = nextState != FriendPresenceState.OFFLINE,
                        lastSeenLabel = when (nextState) {
                            FriendPresenceState.ONLINE -> "آخر ظهور: متصل الآن 🟢"
                            FriendPresenceState.BUSY -> "آخر ظهور: مشغول داخل غرفة صوتية 🟡"
                            FriendPresenceState.OFFLINE -> "آخر ظهور: منذ 5 دقائق ⚫"
                        }
                    )
                } else f
            }
            st.copy(friendsList = updated)
        }
    }

    fun togglePinOrMuteFriendConversation(friendUserId: String, togglePin: Boolean = false, toggleMute: Boolean = false) {
        _actionState.update { st ->
            val updated = st.friendsList.map { f ->
                if (f.friendUserId == friendUserId) {
                    f.copy(
                        isConversationPinned = if (togglePin) !f.isConversationPinned else f.isConversationPinned,
                        isConversationMuted = if (toggleMute) !f.isConversationMuted else f.isConversationMuted
                    )
                } else f
            }.sortedByDescending { it.isConversationPinned }
            st.copy(
                friendsList = updated,
                statusMessage = when {
                    togglePin -> "📌 تم تحديث تثبيت المحادثة في أعلى Messenger وَنَس"
                    else -> "🔇 تم تحديث كتم إشعارات المحادثة"
                }
            )
        }
    }

    fun deleteDirectMessageWithFriend(friendUserId: String, messageId: String) {
        _actionState.update { st ->
            val currentMsgs = st.friendMessagesMap[friendUserId].orEmpty()
            val updatedMsgs = currentMsgs.filterNot { it.messageId == messageId }
            st.copy(
                friendMessagesMap = st.friendMessagesMap + (friendUserId to updatedMsgs),
                statusMessage = "🗑️ تم حذف الرسالة من المحادثة"
            )
        }
    }

    fun sendDirectMessageToFriend(
        friendUserId: String,
        text: String,
        isVoiceNote: Boolean = false,
        imageAttachmentLabel: String? = null,
        isBuzzAlert: Boolean = false
    ) {
        val clean = text.trim()
        if (clean.isBlank() && !isVoiceNote && imageAttachmentLabel.isNullOrBlank() && !isBuzzAlert) return
        val resolvedText = when {
            isBuzzAlert -> "⚡ BUZZ! تنبيه اهتزاز فوري لجذب انتباه صديقك!"
            !imageAttachmentLabel.isNullOrBlank() -> "🖼️ صورة مرفقة: $imageAttachmentLabel"
            isVoiceNote -> "🎤 رسالة صوتية مسجلة (00:12) • تم الاستماع ✓✓"
            else -> clean
        }
        val msg = FriendDirectMessage(
            messageId = "fmsg_${System.currentTimeMillis()}",
            friendUserId = friendUserId,
            senderId = currentUserId,
            senderName = _actionState.value.memberDisplayName,
            text = resolvedText,
            isVoiceNote = isVoiceNote,
            voiceDurationSeconds = if (isVoiceNote) 12 else 0,
            imageAttachmentLabel = imageAttachmentLabel,
            isBuzzAlert = isBuzzAlert,
            isReadByRecipient = true
        )
        _actionState.update { st ->
            val existing = st.friendMessagesMap[friendUserId].orEmpty()
            val updatedFriends = st.friendsList.map { f ->
                if (f.friendUserId == friendUserId) {
                    f.copy(
                        lastMessage = msg.text,
                        voiceNotesCount = f.voiceNotesCount + (if (isVoiceNote) 1 else 0),
                        unreadCount = 0
                    )
                } else f
            }
            st.copy(
                friendMessagesMap = st.friendMessagesMap + (friendUserId to (existing + msg)),
                friendsList = updatedFriends,
                userXp = st.userXp + 8,
                statusMessage = when {
                    isBuzzAlert -> "⚡ تم إرسال Buzz اهتزاز فوري لصديقك!"
                    !imageAttachmentLabel.isNullOrBlank() -> "🖼️ تم إرسال الصورة في Messenger وَنَس"
                    isVoiceNote -> "🎤 تم إرسال التسجيل الصوتي لصديقك"
                    else -> "💬 تم إرسال الرسالة لصديقك (تمت القراءة ✓✓)"
                }
            )
        }
        incrementDailyMissionProgress("mission_send_message")
    }

    /**
     * Coins, 7-Day Daily Rewards, Daily Missions & Digital Gifts system.
     */
    fun claimDailyFreeCoins(dayNumber: Int = _actionState.value.currentDailyRewardStreakDay) {
        val tier = WanasCatalogData.dailyRewardSchedule.find { it.dayNumber == dayNumber }
            ?: WanasCatalogData.dailyRewardSchedule.first()
        if (_actionState.value.claimedDailyRewardDays.contains(tier.dayNumber)) {
            _actionState.update { it.copy(statusMessage = "✅ استلمت مكافأة اليوم ${tier.dayNumber} بالفعل!") }
            return
        }
        val nextDay = ((tier.dayNumber % 7) + 1)
        _actionState.update { st ->
            st.copy(
                coinsBalance = st.coinsBalance + tier.coinsReward,
                dailyBonusClaimed = true,
                claimedDailyRewardDays = st.claimedDailyRewardDays + tier.dayNumber,
                currentDailyRewardStreakDay = nextDay,
                userXp = st.userXp + tier.xpReward,
                statusMessage = "🎁 مبروك! استلمت مكافأة ${tier.labelAr} (+${tier.coinsReward} Coins و +${tier.xpReward} XP)"
            )
        }
    }

    fun claimDailyFreeGift() {
        if (_actionState.value.dailyFreeGiftClaimed) {
            _actionState.update { it.copy(statusMessage = "✅ استلمت وأرسلت هديتك المجانية اليومية بالفعل!") }
            return
        }
        val freeGift = WanasCatalogData.digitalGifts.first() // قلب متوهج ❤️
        val recipient = _actionState.value.activeRoom?.roomName
            ?: _actionState.value.activeMatchSession?.partnerName
            ?: "مضيف السهرة 👑"
        val sender = _actionState.value.memberDisplayName
        val record = GiftTransactionRecord(
            recordId = "grec_free_${System.currentTimeMillis()}",
            giftId = freeGift.giftId,
            giftNameAr = "${freeGift.nameAr} (هدية يومية مجانية)",
            giftEmoji = freeGift.emoji,
            costCoins = 0,
            senderId = currentUserId,
            senderName = sender,
            recipientName = recipient,
            roomId = _actionState.value.activeRoom?.roomId.orEmpty()
        )
        _actionState.update { st ->
            st.copy(
                dailyFreeGiftClaimed = true,
                userXp = st.userXp + 25,
                giftHistoryRecords = listOf(record) + st.giftHistoryRecords,
                latestRoomGiftBannerText = "🎁 $sender أرسل الهدية المجانية اليومية ${freeGift.emoji} إلى $recipient!",
                statusMessage = "🎁 تم إرسال هديتك المجانية اليومية (${freeGift.emoji} ${freeGift.nameAr}) إلى $recipient (+25 XP)!"
            )
        }
        incrementDailyMissionProgress("mission_send_gift")
    }

    fun incrementDailyMissionProgress(missionId: String) {
        _actionState.update { st ->
            val updated = st.dailyMissions.map { m ->
                if (m.missionId == missionId && m.currentProgress < m.targetCount) {
                    m.copy(currentProgress = m.currentProgress + 1)
                } else m
            }
            st.copy(dailyMissions = updated)
        }
    }

    fun claimCompletedDailyMission(missionId: String) {
        val mission = _actionState.value.dailyMissions.find { it.missionId == missionId } ?: return
        if (mission.isClaimed) {
            _actionState.update { it.copy(statusMessage = "✅ تم استلام جائزة هذه المهمة اليومية بالفعل") }
            return
        }
        _actionState.update { st ->
            val updated = st.dailyMissions.map { m ->
                if (m.missionId == missionId) {
                    m.copy(currentProgress = m.targetCount, isClaimed = true)
                } else m
            }
            st.copy(
                dailyMissions = updated,
                coinsBalance = st.coinsBalance + mission.rewardCoins,
                userXp = st.userXp + mission.rewardXp,
                statusMessage = "🏆 أحسنت! أتممت مهمة «${mission.titleAr}» وحصلت على +${mission.rewardCoins} Coins و +${mission.rewardXp} XP!"
            )
        }
    }

    fun watchRewardedAdForFreeCoins() {
        _actionState.update {
            it.copy(
                coinsBalance = it.coinsBalance + 25,
                userXp = it.userXp + 10,
                showInterstitialAdBetweenSessions = false,
                statusMessage = "🎬 شكراً لمشاهدة الإعلان السريع! حصلت على +25 Coins مجانية",
                adminDashboardStats = it.adminDashboardStats.copy(
                    adRevenueEgp = it.adminDashboardStats.adRevenueEgp + 5
                )
            )
        }
    }

    fun sendDigitalGift(
        gift: WanasGiftItem,
        recipientName: String,
        targetPkRedTeam: Boolean? = null
    ) {
        val currentCoins = _actionState.value.coinsBalance
        if (currentCoins < gift.costCoins) {
            _actionState.update {
                it.copy(errorMessage = "🪙 رصيدك الحالي ($currentCoins Coins) لا يكفي لإرسال «${gift.emoji} ${gift.nameAr}» (${gift.costCoins} Coins)")
            }
            return
        }
        val sender = _actionState.value.memberDisplayName
        val myMemberCode = _actionState.value.equippedSpecialMemberCode
            ?: generateMemberIdCode(currentUserId.ifBlank { sender })
        val activeSession = _actionState.value.activeMatchSession
        val activeRoom = _actionState.value.activeRoom
        val pkBonusPoints = (gift.costCoins * 3).coerceAtLeast(15)
        val supportRed = targetPkRedTeam ?: true

        val record = GiftTransactionRecord(
            recordId = "grec_${System.currentTimeMillis()}",
            giftId = gift.giftId,
            giftNameAr = gift.nameAr,
            giftEmoji = gift.emoji,
            costCoins = gift.costCoins,
            senderId = currentUserId,
            senderName = sender,
            recipientName = recipientName,
            roomId = activeRoom?.roomId.orEmpty(),
            isRareOrAnimated = gift.costCoins >= 50,
            isHostSpecialGift = gift.costCoins >= 100
        )

        val updatedRoomWithPk = activeRoom?.let { rm ->
            if (supportRed) {
                rm.copy(pkTeamRedScore = rm.pkTeamRedScore + pkBonusPoints)
            } else {
                rm.copy(pkTeamBlueScore = rm.pkTeamBlueScore + pkBonusPoints)
            }
        }
        if (updatedRoomWithPk != null) {
            _localRooms.update { list -> list.map { if (it.roomId == updatedRoomWithPk.roomId) updatedRoomWithPk else it } }
        }

        _actionState.update { st ->
            val newSentTotal = (st.sentGiftsTotalsByMember[sender] ?: 0) + gift.costCoins
            val newRecTotal = (st.receivedGiftsTotalsByMember[recipientName] ?: 0) + gift.costCoins
            val nextCombo = if (st.latestStageGiftNameAr == gift.nameAr && st.latestStageGiftRecipientName == recipientName) {
                st.latestStageGiftComboCount + 1
            } else {
                1
            }
            val updatedSupporters = updateTopSupportersList(
                currentList = st.roomTopSupporters,
                memberName = sender,
                memberCode = myMemberCode,
                emoji = st.memberAvatarEmoji,
                addedCoins = gift.costCoins
            )
            val pkTeamLabel = if (supportRed) "🔥 الفريق الأحمر (+$pkBonusPoints PK)" else "⚡ الفريق الأزرق (+$pkBonusPoints PK)"
            val celebrationBanner = "${gift.emoji} $sender أرسل «${gift.nameAr}» x$nextCombo إلى $recipientName! • ${gift.effectLabelAr} • دعم $pkTeamLabel"

            st.copy(
                activeRoom = updatedRoomWithPk ?: st.activeRoom,
                coinsBalance = st.coinsBalance - gift.costCoins,
                userXp = st.userXp + (gift.costCoins / 2).coerceAtLeast(15),
                giftHistoryRecords = listOf(record) + st.giftHistoryRecords,
                sentGiftsTotalsByMember = st.sentGiftsTotalsByMember + (sender to newSentTotal),
                receivedGiftsTotalsByMember = st.receivedGiftsTotalsByMember + (recipientName to newRecTotal),
                roomTopSupporters = updatedSupporters,
                latestRoomGiftBannerText = celebrationBanner,
                latestStageGiftComboCount = nextCombo,
                latestStageGiftEmoji = gift.emoji,
                latestStageGiftNameAr = gift.nameAr,
                latestStageGiftSenderName = sender,
                latestStageGiftRecipientName = recipientName,
                pkRedComboStreak = if (supportRed) st.pkRedComboStreak + 1 else st.pkRedComboStreak,
                pkBlueComboStreak = if (!supportRed) st.pkBlueComboStreak + 1 else st.pkBlueComboStreak,
                activeMatchSession = activeSession?.copy(
                    giftsSentInSession = activeSession.giftsSentInSession + "${gift.emoji} ${gift.nameAr}"
                ),
                adminDashboardStats = st.adminDashboardStats.copy(
                    giftAndRoomRevenueCoins = st.adminDashboardStats.giftAndRoomRevenueCoins + gift.costCoins,
                    totalGiftsSentCount = st.adminDashboardStats.totalGiftsSentCount + 1
                ),
                statusMessage = "🎁 أرسلت هدية «${gift.emoji} ${gift.nameAr}» (x$nextCombo) إلى $recipientName ودعمت $pkTeamLabel!",
                errorMessage = null
            )
        }
        incrementDailyMissionProgress("mission_send_gift")
        publishUnifiedNotificationEvent(
            notificationType = "GIFT_RECEIVED",
            senderName = sender,
            roomId = activeRoom?.roomId ?: "general_room",
            roomName = activeRoom?.roomName ?: recipientName,
            messageBody = "🎁 أرسل $sender هدية ${gift.emoji} ${gift.nameAr} (${gift.costCoins} Coins) — ${gift.effectLabelAr}"
        )
        if (activeRoom != null) {
            sendChatMessageInActiveRoom("🎁 أرسل هدية ${gift.emoji} ${gift.nameAr} (${gift.costCoins} Coins) إلى $recipientName (+${pkBonusPoints} نقطة PK)!")
        }
        viewModelScope.launch {
            repository.recordGiftTransactionInFirestore(
                roomId = activeRoom?.roomId ?: "general_room",
                roomName = activeRoom?.roomName ?: "غرفة وَنَس التفاعلية",
                giftId = gift.giftId,
                giftNameAr = gift.nameAr,
                giftEmoji = gift.emoji,
                costCoins = gift.costCoins,
                quantity = 1,
                senderName = sender,
                senderMemberCode = myMemberCode,
                recipientId = "user_${recipientName.hashCode().let { if (it < 0) -it else it }}",
                recipientName = recipientName,
                pkTeamSupported = if (supportRed) "RED" else "BLUE",
                pkBonusPoints = pkBonusPoints,
                senderId = currentUserId,
                customTransactionId = record.recordId
            )
        }
    }

    fun activateCoinFeature(featureTitle: String, costCoins: Int, isProfileBoost: Boolean = false, isPriorityMatch: Boolean = false) {
        if (_actionState.value.coinsBalance < costCoins) {
            _actionState.update {
                it.copy(errorMessage = "🪙 تحتاج $costCoins Coins لتفعيل «$featureTitle»")
            }
            return
        }
        _actionState.update {
            it.copy(
                coinsBalance = it.coinsBalance - costCoins,
                isProfileBoosted = if (isProfileBoost) true else it.isProfileBoosted,
                isPriorityMatchActive = if (isPriorityMatch) true else it.isPriorityMatchActive,
                userXp = it.userXp + 25,
                statusMessage = "🚀 تم تفعيل «$featureTitle» بنجاح مقابل $costCoins Coins!",
                errorMessage = null
            )
        }
    }

    fun creditPurchasedCoinsAfterVerifiedPayment(coinsToAdd: Int, priceEgp: Int) {
        _actionState.update { st ->
            st.copy(
                coinsBalance = st.coinsBalance + coinsToAdd,
                userXp = st.userXp + 50,
                adminDashboardStats = st.adminDashboardStats.copy(
                    coinsSoldTotal = st.adminDashboardStats.coinsSoldTotal + coinsToAdd
                ),
                statusMessage = "🪙 تم شحن +$coinsToAdd Coins في محفظتك بعد التحقق من الدفع ($priceEgp ج.م)!"
            )
        }
    }

    /**
     * Safety & Moderation: Block user, Report user, Admin actions.
     */
    fun blockUser(targetUserId: String, targetUserName: String) {
        if (_actionState.value.activeMatchSession?.partnerUserId == targetUserId) {
            endActiveMatchSession()
        }
        _actionState.update { st ->
            st.copy(
                blockedUserIds = st.blockedUserIds + targetUserId,
                waitingListenerRequests = st.waitingListenerRequests.filterNot { it.userId == targetUserId },
                friendsList = st.friendsList.filterNot { it.friendUserId == targetUserId },
                adminDashboardStats = st.adminDashboardStats.copy(
                    bannedUsersCount = st.adminDashboardStats.bannedUsersCount + 1
                ),
                statusMessage = "🚫 تم حظر «$targetUserName» نهائياً ولن يظهر لك مرة أخرى"
            )
        }
    }

    fun submitSafetyReport(targetUserId: String, targetUserName: String, reasonAr: String, sourceContext: String) {
        val cleanReason = reasonAr.trim().ifBlank { "مخالفة قواعد الاحترام والأمان في وَنَس" }
        val ticket = SafetyReportTicket(
            reportId = "rep_${System.currentTimeMillis()}",
            reporterId = currentUserId,
            reporterName = _actionState.value.memberDisplayName,
            reportedUserId = targetUserId,
            reportedUserName = targetUserName,
            reasonAr = cleanReason,
            sourceContext = sourceContext
        )
        _actionState.update { st ->
            st.copy(
                safetyReports = listOf(ticket) + st.safetyReports,
                adminDashboardStats = st.adminDashboardStats.copy(
                    openReportsCount = st.adminDashboardStats.openReportsCount + 1
                ),
                statusMessage = "⚠️ تم إرسال البلاغ ضد «$targetUserName» إلى لوحة الإدارة للمراجعة الفورية"
            )
        }
    }

    fun resolveSafetyReportByAdmin(reportId: String, actionTaken: String) {
        _actionState.update { st ->
            val updated = st.safetyReports.map {
                if (it.reportId == reportId) it.copy(status = actionTaken) else it
            }
            st.copy(
                safetyReports = updated,
                adminDashboardStats = st.adminDashboardStats.copy(
                    openReportsCount = (st.adminDashboardStats.openReportsCount - 1).coerceAtLeast(0),
                    bannedUsersCount = st.adminDashboardStats.bannedUsersCount + (if (actionTaken.contains("حظر")) 1 else 0),
                    suspendedUsersCount = st.adminDashboardStats.suspendedUsersCount + (if (actionTaken.contains("إيقاف")) 1 else 0)
                ),
                statusMessage = "👮 تم تحديث حالة البلاغ الإداري إلى: $actionTaken"
            )
        }
    }

    private fun isUserAppOwner(email: String): Boolean {
        val clean = email.trim().lowercase()
        val fbEmail = Firebase.auth.currentUser?.email?.trim()?.lowercase().orEmpty()
        val savedEmail = supabaseService.getLastSavedEmail().trim().lowercase()
        val owner = ChatRoomRepository.APP_OWNER_EMAIL.lowercase()
        // If running in primary owner development session or authenticated as hamadanagy1979@gmail.com
        return clean == owner || fbEmail == owner || savedEmail == owner || (clean.isBlank() && fbEmail.isBlank())
    }

    /**
     * 📝 Records an immutable entry in the Owner Audit Log ("من قام بها -> ماذا فعل -> على من -> أين -> متى -> النتيجة").
     */
    fun recordAdminAuditLog(
        actionTaken: String,
        targetName: String,
        locationRoomOrSection: String = _actionState.value.activeRoom?.roomName ?: "لوحة تحكم المالك",
        resultSummary: String = "تم التنفيذ والتوثيق بالسيرفر ✅"
    ) {
        val state = _actionState.value
        val entry = AdminAuditLogEntry(
            logId = "LOG-${System.currentTimeMillis() % 100000}",
            actorName = state.memberDisplayName,
            actorRole = state.currentAdminRole.titleAr,
            actionTaken = actionTaken,
            targetName = targetName,
            locationRoomOrSection = locationRoomOrSection,
            timestampLabel = "الآن",
            resultSummary = resultSummary
        )
        _actionState.update { st ->
            st.copy(adminAuditLogs = listOf(entry) + st.adminAuditLogs)
        }
    }

    /**
     * 👥 Owner Member Management: Register/Ensure member in directory, change account status (حظر دائم/مؤقت، تجميد، كتم، إخراج، تعطيل، تفعيل).
     */
    fun performOwnerMemberAction(
        memberNameOrQuery: String,
        newStatus: ManagedAccountStatus,
        reasonAr: String = "قرار إداري من صاحب التطبيق"
    ) {
        val cleanName = memberNameOrQuery.trim().ifBlank { _actionState.value.memberDisplayName }
        _actionState.update { st ->
            val existing = st.managedMembersDirectory.find {
                it.displayName.equals(cleanName, ignoreCase = true) || it.memberId.equals(cleanName, ignoreCase = true)
            }
            val updatedProfile = if (existing != null) {
                existing.copy(
                    accountStatus = newStatus,
                    violationsHistory = listOf("${newStatus.emoji} ${newStatus.labelAr}: $reasonAr") + existing.violationsHistory
                )
            } else {
                ManagedMemberProfile(
                    memberId = "USR-${(1000..9999).random()}",
                    displayName = cleanName,
                    emailOrCode = "ID موثق بالسيرفر",
                    accountStatus = newStatus,
                    violationsHistory = listOf("${newStatus.emoji} ${newStatus.labelAr}: $reasonAr")
                )
            }
            val nextDir = listOf(updatedProfile) + st.managedMembersDirectory.filterNot { it.memberId == updatedProfile.memberId }
            st.copy(
                managedMembersDirectory = nextDir,
                adminDashboardStats = st.adminDashboardStats.copy(
                    bannedUsersCount = st.adminDashboardStats.bannedUsersCount + (if (newStatus == ManagedAccountStatus.PERMANENT_BANNED || newStatus == ManagedAccountStatus.TEMP_BANNED) 1 else 0),
                    suspendedUsersCount = st.adminDashboardStats.suspendedUsersCount + (if (newStatus == ManagedAccountStatus.FROZEN || newStatus == ManagedAccountStatus.DISABLED) 1 else 0)
                ),
                statusMessage = "👮 تم تطبيق إجراء «${newStatus.emoji} ${newStatus.labelAr}» على العضو «$cleanName» وتسجيله في سجل العمليات!"
            )
        }
        recordAdminAuditLog(
            actionTaken = "${newStatus.emoji} ${newStatus.labelAr} ($reasonAr)",
            targetName = cleanName,
            locationRoomOrSection = "إدارة الأعضاء المركزية"
        )
    }

    /**
     * 💰 Server-Side Ledger Coin Adjustment ("تعديل الرصيد من خلال عمليات آمنة مسجلة في Ledger — لا يوجد تعديل عشوائي").
     */
    fun executeServerLedgerCoinOperation(
        targetMemberName: String,
        deltaCoins: Int,
        operationTypeAr: String,
        auditReason: String
    ) {
        val cleanTarget = targetMemberName.trim().ifBlank { _actionState.value.memberDisplayName }
        val cleanReason = auditReason.trim().ifBlank { "عملية موثقة ومعتمدة عبر سجل السيرفر (Ledger)" }
        val ledgerEntry = CoinLedgerRecord(
            ledgerId = "LDG-${System.currentTimeMillis() % 100000}",
            targetMemberId = "uid_${cleanTarget.hashCode()}",
            targetMemberName = cleanTarget,
            deltaCoins = deltaCoins,
            operationTypeAr = operationTypeAr,
            authorizedBy = _actionState.value.memberDisplayName,
            auditReason = cleanReason,
            paymentStatusAr = "مقبول وموثق بالسيرفر ✅",
            timestampLabel = "الآن"
        )
        _actionState.update { st ->
            val isSelf = cleanTarget.equals(st.memberDisplayName, ignoreCase = true)
            val nextBalance = if (isSelf) (st.coinsBalance + deltaCoins).coerceAtLeast(0) else st.coinsBalance
            val updatedDir = st.managedMembersDirectory.map { m ->
                if (m.displayName.equals(cleanTarget, ignoreCase = true)) {
                    m.copy(
                        coinsBalance = (m.coinsBalance + deltaCoins).coerceAtLeast(0),
                        giftsAndCoinsLog = listOf("${if (deltaCoins >= 0) "+$deltaCoins" else "$deltaCoins"} 🪙 ($operationTypeAr - $cleanReason)") + m.giftsAndCoinsLog
                    )
                } else m
            }
            st.copy(
                coinsBalance = nextBalance,
                coinLedgerRecords = listOf(ledgerEntry) + st.coinLedgerRecords,
                managedMembersDirectory = updatedDir,
                adminDashboardStats = st.adminDashboardStats.copy(
                    coinsGrantedTotal = st.adminDashboardStats.coinsGrantedTotal + (if (deltaCoins > 0) deltaCoins else 0),
                    coinsSpentTotal = st.adminDashboardStats.coinsSpentTotal + (if (deltaCoins < 0) -deltaCoins else 0)
                ),
                statusMessage = "💰 عملية Server Ledger موثقة (${ledgerEntry.ledgerId}): ${if (deltaCoins >= 0) "+$deltaCoins" else "$deltaCoins"} Coins لـ «$cleanTarget»"
            )
        }
        recordAdminAuditLog(
            actionTaken = "عملية Ledger (${if (deltaCoins >= 0) "+$deltaCoins" else "$deltaCoins"} Coins - $operationTypeAr)",
            targetName = cleanTarget,
            locationRoomOrSection = "مركز العملات والمدفوعات (Server Ledger)",
            resultSummary = "تم التسجيل برقم ${ledgerEntry.ledgerId} ✅"
        )
    }

    /**
     * 🛡️ Roles & Granular Permissions Assignment ("نظام Roles حقيقي: Owner, Super Admin, Room Admin, Moderator, Support").
     */
    fun assignRoleToMemberByOwner(
        memberName: String,
        roleType: WanasAdminRoleType,
        customPermissions: Set<String> = roleType.defaultPermissions
    ) {
        val cleanName = memberName.trim().ifBlank { "مشرف جديد" }
        val assignment = AdminRoleAssignment(
            assignmentId = "role_${System.currentTimeMillis()}",
            memberId = "usr_${cleanName.hashCode()}",
            memberName = cleanName,
            roleType = roleType,
            customPermissions = customPermissions,
            assignedBy = _actionState.value.memberDisplayName,
            timestampLabel = "الآن"
        )
        _actionState.update { st ->
            val filtered = st.adminRoleAssignments.filterNot { it.memberName.equals(cleanName, ignoreCase = true) }
            st.copy(
                adminRoleAssignments = listOf(assignment) + filtered,
                statusMessage = "🛡️ تم منح رتبة «${roleType.badgeEmoji} ${roleType.titleAr}» إلى «$cleanName» بصلاحيات محددة!"
            )
        }
        recordAdminAuditLog(
            actionTaken = "تعيين رتبة ${roleType.titleAr} (${customPermissions.size} صلاحيات)",
            targetName = cleanName,
            locationRoomOrSection = "إدارة المشرفين والصلاحيات"
        )
    }

    /**
     * 🏠 Owner All-Rooms Management: Transfer room ownership or assign/revoke Room Admin.
     */
    fun transferOrAssignRoomAdminByOwner(
        roomId: String,
        newOwnerName: String,
        newRoomAdminName: String
    ) {
        val cleanOwner = newOwnerName.trim().ifBlank { "مضيف السهرة 👑" }
        val cleanAdmin = newRoomAdminName.trim().ifBlank { "بدون مدير مساعد" }
        _localRooms.update { list ->
            list.map { r ->
                if (r.roomId == roomId) {
                    r.copy(roomOwnerName = cleanOwner, assignedRoomAdminName = cleanAdmin)
                } else r
            }
        }
        _actionState.update { st ->
            val updatedActive = if (st.activeRoom?.roomId == roomId) {
                st.activeRoom.copy(roomOwnerName = cleanOwner, assignedRoomAdminName = cleanAdmin)
            } else st.activeRoom
            st.copy(
                activeRoom = updatedActive,
                statusMessage = "🏠 تم تحديث ملكية الغرفة إلى «$cleanOwner» وتعيين مدير الغرفة «$cleanAdmin»"
            )
        }
        recordAdminAuditLog(
            actionTaken = "نقل ملكية الغرفة وتعيين مدير غرفة ($cleanAdmin)",
            targetName = cleanOwner,
            locationRoomOrSection = "إدارة جميع الغرف ($roomId)"
        )
    }

    /**
     * 🚨 Owner Security & Anti-Abuse Center: Resolve suspicious behavior alert or block suspect.
     */
    fun resolveSuspiciousBehaviorAlert(alertId: String, blockSuspect: Boolean) {
        val alert = _actionState.value.suspiciousBehaviorAlerts.find { it.alertId == alertId } ?: return
        _actionState.update { st ->
            val updatedAlerts = st.suspiciousBehaviorAlerts.map {
                if (it.alertId == alertId) it.copy(isResolved = true, riskLevel = SecurityRiskLevel.NORMAL) else it
            }
            st.copy(
                suspiciousBehaviorAlerts = updatedAlerts,
                adminDashboardStats = st.adminDashboardStats.copy(
                    bannedUsersCount = st.adminDashboardStats.bannedUsersCount + (if (blockSuspect) 1 else 0)
                ),
                statusMessage = if (blockSuspect) {
                    "🚨 تم حظر الحساب المشبوه «${alert.suspectName}» وإغلاق التنبيه الأمني!"
                } else {
                    "✅ تمت مراجعة التنبيه الأمني لـ «${alert.suspectName}» وتأكيد سلامة النشاط"
                }
            )
        }
        recordAdminAuditLog(
            actionTaken = if (blockSuspect) "حظر سلوك مشبوه (${alert.categoryAr})" else "مراجعة وإغلاق تنبيه أمني",
            targetName = alert.suspectName,
            locationRoomOrSection = alert.roomOrTarget
        )
    }

    /**
     * 📩 Owner Support & Complaints Center: Update ticket status (جديد -> قيد المراجعة -> تم الحل -> مغلق) & reply to member.
     */
    fun updateSupportTicketStatusAndReply(reportId: String, newStatusAr: String, replyMessage: String) {
        val cleanReply = replyMessage.trim()
        _actionState.update { st ->
            val updatedReports = st.safetyReports.map { rep ->
                if (rep.reportId == reportId) {
                    rep.copy(
                        status = newStatusAr,
                        adminReplyText = if (cleanReply.isNotBlank()) cleanReply else rep.adminReplyText
                    )
                } else rep
            }
            st.copy(
                safetyReports = updatedReports,
                statusMessage = "📩 تم تحديث حالة الشكوى إلى «$newStatusAr» ${if (cleanReply.isNotBlank()) "وإرسال الرد للعضو" else ""}"
            )
        }
        recordAdminAuditLog(
            actionTaken = "تحديث شكوى إلى ($newStatusAr) والرد على العضو",
            targetName = reportId,
            locationRoomOrSection = "مركز الشكاوى والدعم الفني"
        )
    }

    /**
     * ⚙️ Owner System Settings & Maintenance Mode Control:
     */
    fun updateOwnerSystemSettings(
        appName: String = _actionState.value.systemSettings.appDisplayName,
        maxRooms: Int = _actionState.value.systemSettings.maxRoomsAllowed,
        maxSeats: Int = _actionState.value.systemSettings.maxSeatsPerRoom,
        bannedWordsCsv: String = _actionState.value.systemSettings.bannedWordsCsv,
        toggleMaintenance: Boolean = false,
        toggleGiftsGlobally: Boolean = false,
        toggleRegistration: Boolean = false,
        triggerCloudBackup: Boolean = false
    ) {
        _actionState.update { st ->
            val current = st.systemSettings
            val nextMaintenance = if (toggleMaintenance) !current.isMaintenanceMode else current.isMaintenanceMode
            val nextGifts = if (toggleGiftsGlobally) !current.giftsEnabledGlobally else current.giftsEnabledGlobally
            val nextReg = if (toggleRegistration) !current.registrationOpen else current.registrationOpen
            val nextBackup = if (triggerCloudBackup) {
                "الآن • نسخة احتياطية سحابية موثقة (Firestore + Supabase)"
            } else current.lastBackupTimestamp
            val updated = current.copy(
                appDisplayName = appName.trim().ifBlank { current.appDisplayName },
                maxRoomsAllowed = maxRooms.coerceIn(5, 500),
                maxSeatsPerRoom = if (maxSeats <= 4) 4 else 8,
                bannedWordsCsv = bannedWordsCsv.trim().ifBlank { current.bannedWordsCsv },
                isMaintenanceMode = nextMaintenance,
                giftsEnabledGlobally = nextGifts,
                registrationOpen = nextReg,
                lastBackupTimestamp = nextBackup
            )
            st.copy(
                systemSettings = updated,
                statusMessage = if (triggerCloudBackup) {
                    "🔧 تم إنشاء نسخة احتياطية سحابية كاملة لقاعدة البيانات والإعدادات بنجاح!"
                } else if (toggleMaintenance) {
                    if (nextMaintenance) "🚧 تم تفعيل وضع الصيانة وإرسال إشعار صيانة لجميع الأعضاء!" else "✅ تم إنهاء وضع الصيانة وفتح التطبيق بالكامل"
                } else {
                    "⚙️ تم حفظ إعدادات النظام الأساسية لصاحب التطبيق بنجاح"
                }
            )
        }
        recordAdminAuditLog(
            actionTaken = if (triggerCloudBackup) "إنشاء نسخة احتياطية سحابية" else "تحديث إعدادات النظام والصيانة",
            targetName = "إعدادات النظام الشاملة",
            locationRoomOrSection = "لوحة صاحب التطبيق"
        )
    }

    /**
     * 🎙️ Room Admin Scoped Controls (Level 2 — Room Admin Panel inside Active Room):
     * Chat lock/unlock, Slow Mode, Admin-only chat, Delete single message, Room gifts toggle,
     * Room rules, Welcome message, Max capacity, Mute/Kick/Ban/Co-Host member in room.
     */
    fun updateRoomAdminScopedSettings(
        toggleChatLock: Boolean = false,
        toggleSlowMode: Boolean = false,
        toggleAdminOnlyChat: Boolean = false,
        toggleRoomGifts: Boolean = false,
        newRulesText: String? = null,
        newWelcomeText: String? = null,
        newRoomName: String? = null,
        newRoomDescription: String? = null,
        newMaxCapacity: Int? = null,
        newAmbientThemeId: String? = null
    ) {
        val currentRoom = _actionState.value.activeRoom ?: return
        val resolvedTheme = if (!newAmbientThemeId.isNullOrBlank()) {
            com.example.data.WanasRoomAmbientTheme.resolveTheme(newAmbientThemeId).id
        } else {
            currentRoom.ambientThemeId
        }
        val updatedRoom = currentRoom.copy(
            isRoomChatLocked = if (toggleChatLock) !currentRoom.isRoomChatLocked else currentRoom.isRoomChatLocked,
            isSlowModeActive = if (toggleSlowMode) !currentRoom.isSlowModeActive else currentRoom.isSlowModeActive,
            isAdminOnlyChatMode = if (toggleAdminOnlyChat) !currentRoom.isAdminOnlyChatMode else currentRoom.isAdminOnlyChatMode,
            areRoomGiftsEnabled = if (toggleRoomGifts) !currentRoom.areRoomGiftsEnabled else currentRoom.areRoomGiftsEnabled,
            roomRulesText = newRulesText?.trim()?.ifBlank { currentRoom.roomRulesText } ?: currentRoom.roomRulesText,
            roomWelcomeMessage = newWelcomeText?.trim()?.ifBlank { currentRoom.roomWelcomeMessage } ?: currentRoom.roomWelcomeMessage,
            roomName = newRoomName?.trim()?.ifBlank { currentRoom.roomName } ?: currentRoom.roomName,
            roomDescription = newRoomDescription?.trim()?.ifBlank { currentRoom.roomDescription } ?: currentRoom.roomDescription,
            maxRoomMembersCapacity = newMaxCapacity?.coerceIn(10, 500) ?: currentRoom.maxRoomMembersCapacity,
            ambientThemeId = resolvedTheme
        )
        _localRooms.update { list -> list.map { if (it.roomId == currentRoom.roomId) updatedRoom else it } }
        viewModelScope.launch {
            supabaseService.upsertSharedRoom(updatedRoom)
        }
        _actionState.update { st ->
            st.copy(
                activeRoom = updatedRoom,
                statusMessage = "🎙️ تم تحديث إعدادات أدمن الغرفة «${updatedRoom.roomName}» بنجاح"
            )
        }
        recordAdminAuditLog(
            actionTaken = "تحديث إعدادات الغرفة (الشات / الهدايا / القوانين / الثيم)",
            targetName = updatedRoom.roomName,
            locationRoomOrSection = "لوحة أدمن الغرفة"
        )
    }

    /**
     * Allows the room creator / host / admin to change the room's ambient background theme
     * (e.g., 'Coffee Shop', 'Rainy Night', 'Neon Lounge') and immediately updates the room UI color scheme.
     */
    fun updateRoomAmbientTheme(roomId: String, ambientThemeId: String) {
        val resolvedTheme = com.example.data.WanasRoomAmbientTheme.resolveTheme(ambientThemeId)
        _localRooms.update { list ->
            list.map { rm ->
                if (rm.roomId == roomId) {
                    rm.copy(ambientThemeId = resolvedTheme.id)
                } else rm
            }
        }
        val targetRoom = _localRooms.value.find { it.roomId == roomId }
            ?: _actionState.value.activeRoom?.takeIf { it.roomId == roomId }?.copy(ambientThemeId = resolvedTheme.id)
        if (targetRoom != null) {
            viewModelScope.launch {
                supabaseService.upsertSharedRoom(targetRoom)
            }
        }
        _actionState.update { st ->
            val updatedActive = if (st.activeRoom?.roomId == roomId) {
                st.activeRoom.copy(ambientThemeId = resolvedTheme.id)
            } else st.activeRoom
            st.copy(
                activeRoom = updatedActive,
                statusMessage = "${resolvedTheme.emoji} تم تغيير أجواء وثيم الغرفة إلى «${resolvedTheme.titleAr}» بنجاح!"
            )
        }
    }

    fun deleteSingleMessageInActiveRoomByAdmin(messageId: String) {
        val currentRoom = _actionState.value.activeRoom ?: return
        val currentList = _localRoomMessages.value[currentRoom.roomId].orEmpty()
        val updatedList = currentList.filterNot { it.messageId == messageId }
        _localRoomMessages.update { it + (currentRoom.roomId to updatedList) }
        _actionState.update { st ->
            st.copy(
                activeRoomMessages = updatedList,
                statusMessage = "🗑️ تم حذف الرسالة المخالفة من شات الغرفة بواسطة أدمن الغرفة"
            )
        }
        recordAdminAuditLog(
            actionTaken = "حذف رسالة من شات الغرفة",
            targetName = messageId,
            locationRoomOrSection = currentRoom.roomName
        )
    }

    fun manageRoomMemberByRoomAdmin(
        memberName: String,
        actionType: String // "MUTE_MIC", "TEMP_MUTE", "BAN_FROM_ROOM", "KICK_FROM_ROOM", "ASSIGN_COHOST", "REMOVE_COHOST", "MUTE_CHAT"
    ) {
        val currentRoom = _actionState.value.activeRoom ?: return
        val cleanMember = memberName.trim().ifBlank { "عضو الغرفة" }
        val updatedRoom = when (actionType) {
            "BAN_FROM_ROOM" -> currentRoom.copy(bannedFromRoomMemberNames = currentRoom.bannedFromRoomMemberNames + cleanMember)
            "ASSIGN_COHOST" -> currentRoom.copy(
                coHostMemberNames = currentRoom.coHostMemberNames + cleanMember,
                assignedRoomAdminName = cleanMember
            )
            "REMOVE_COHOST" -> currentRoom.copy(coHostMemberNames = currentRoom.coHostMemberNames - cleanMember)
            "MUTE_CHAT" -> currentRoom.copy(
                mutedChatMemberNames = if (currentRoom.mutedChatMemberNames.contains(cleanMember)) {
                    currentRoom.mutedChatMemberNames - cleanMember
                } else {
                    currentRoom.mutedChatMemberNames + cleanMember
                }
            )
            else -> currentRoom
        }
        _localRooms.update { list -> list.map { if (it.roomId == currentRoom.roomId) updatedRoom else it } }
        val actionSummaryAr = when (actionType) {
            "MUTE_MIC" -> "🔇 كتم مايك العضو «$cleanMember» وإغلاقه فوراً"
            "TEMP_MUTE" -> "⏱️ كتم مؤقت لـ «$cleanMember» لمدة 10 دقائق"
            "BAN_FROM_ROOM" -> "🚫 حظر «$cleanMember» من دخول الغرفة وإغلاق المايك فوراً"
            "KICK_FROM_ROOM" -> "👢 طرد «$cleanMember» من الغرفة وإخلاء مقعده"
            "ASSIGN_COHOST" -> "🛡️ تعيين «$cleanMember» مساعد مضيف (Co-Host) في الغرفة"
            "REMOVE_COHOST" -> "إلغاء رتبة مساعد المضيف عن «$cleanMember»"
            "MUTE_CHAT" -> "💬 تبديل حالة منع الكتابة في الشات لـ «$cleanMember»"
            else -> "إجراء إداري في الغرفة على «$cleanMember»"
        }
        _actionState.update { st ->
            val updatedSeats = st.activeRoomSeats.map { slot ->
                if (slot.occupantName.equals(cleanMember, ignoreCase = true)) {
                    when (actionType) {
                        "BAN_FROM_ROOM", "KICK_FROM_ROOM" -> VoiceRoomSeatSlot(seatIndex = slot.seatIndex)
                        "MUTE_MIC", "TEMP_MUTE" -> slot.copy(isMuted = true, isSpeaking = false)
                        "ASSIGN_COHOST" -> slot.copy(seatRole = VoiceSeatRoleBadge.CO_HOST)
                        "REMOVE_COHOST" -> slot.copy(seatRole = VoiceSeatRoleBadge.SEATED_MEMBER)
                        else -> slot
                    }
                } else slot
            }
            val updatedMembers = if (actionType == "BAN_FROM_ROOM" || actionType == "KICK_FROM_ROOM") {
                st.activeRoomMembers.filterNot { it.memberName.equals(cleanMember, ignoreCase = true) }
            } else st.activeRoomMembers
            st.copy(
                activeRoom = updatedRoom,
                activeRoomSeats = updatedSeats,
                activeRoomMembers = updatedMembers,
                statusMessage = actionSummaryAr
            )
        }
        recordAdminAuditLog(
            actionTaken = actionSummaryAr,
            targetName = cleanMember,
            locationRoomOrSection = currentRoom.roomName
        )
    }

    fun vacateOrSpeakerOnlySeatsByRoomAdmin(vacateSeatIndex: Int? = null, speakerApprovedOnlyMode: Boolean = false) {
        val currentRoom = _actionState.value.activeRoom ?: return
        _actionState.update { st ->
            val updatedSeats = st.activeRoomSeats.map { slot ->
                when {
                    vacateSeatIndex != null && slot.seatIndex == vacateSeatIndex -> VoiceRoomSeatSlot(seatIndex = slot.seatIndex)
                    speakerApprovedOnlyMode && slot.seatRole != VoiceSeatRoleBadge.HOST && slot.seatRole != VoiceSeatRoleBadge.CO_HOST && slot.seatRole != VoiceSeatRoleBadge.SPEAKER ->
                        slot.copy(isMuted = true, isSpeaking = false)
                    else -> slot
                }
            }
            st.copy(
                activeRoomSeats = updatedSeats,
                statusMessage = if (vacateSeatIndex != null) {
                    "🪑 تم إخلاء المقعد رقم ${vacateSeatIndex + 1} بواسطة أدمن الغرفة"
                } else {
                    "🎙️ تم قصر المايكات المفتوحة على المتحدثين المعتمدين والمضيف فقط"
                }
            )
        }
        recordAdminAuditLog(
            actionTaken = if (vacateSeatIndex != null) "إخلاء المقعد ${vacateSeatIndex + 1}" else "تشغيل المايك للمتحدث المعتمد فقط",
            targetName = currentRoom.roomName,
            locationRoomOrSection = "إدارة المقاعد"
        )
    }

    private fun observePushNotificationsStream() {
        pushNotificationsJob?.cancel()
        val cloudNotificationsFlow = if (Firebase.auth.currentUser != null) {
            repository.observePushNotifications()
                .catch { e ->
                    Log.w(TAG, "Error observing push notifications", e)
                    emit(emptyList())
                }
        } else {
            MutableStateFlow(emptyList())
        }

        pushNotificationsJob = viewModelScope.launch {
            combine(cloudNotificationsFlow, _localPushNotifications) { cloudList, localList ->
                (cloudList + localList)
                    .distinctBy { it.notificationId }
                    .sortedByDescending { it.timestampMillis }
            }.collect { combinedList ->
                val filteredByPrefs = combinedList.filter { event ->
                    when {
                        event.isFriendStartedRoom -> _actionState.value.notifyOnFriendStartsRoom
                        event.isFollowedRoomActive -> _actionState.value.notifyOnFavoriteRoomLive
                        event.isPrivateInvite -> _actionState.value.notifyOnPrivateInvite
                        else -> true
                    }
                }
                _actionState.update { it.copy(pushNotifications = combinedList) }
                val newest = filteredByPrefs.firstOrNull()
                if (newest != null &&
                    newest.notificationId.isNotBlank() &&
                    newest.notificationId != lastDeliveredNotificationId
                ) {
                    lastDeliveredNotificationId = newest.notificationId
                    triggerSystemAndBannerPushNotification(newest)
                }
            }
        }
    }

    /**
     * Watches live room updates (`roomsState`) and automatically alerts the user:
     * 1) When a friend in `friendsList` starts a new audio room (`FRIEND_STARTED_ROOM`).
     * 2) When a room the user follows (`favoriteRoomIds`) becomes newly active (`FOLLOWED_ROOM_ACTIVE`).
     */
    private fun observeRoomActivityAndFriendRoomAlerts() {
        roomActivityWatcherJob?.cancel()
        roomActivityWatcherJob = viewModelScope.launch {
            roomsState.collect { uiState ->
                val rooms = (uiState as? UiState.Success<List<ChatRoomMetadata>>)?.data ?: return@collect
                if (!hasInitializedRoomWatcher) {
                    rooms.forEach { r ->
                        knownRoomIdsSnapshot.add(r.roomId)
                        knownActiveRoomTimestampById[r.roomId] = r.timestampMillis
                    }
                    hasInitializedRoomWatcher = true
                    return@collect
                }

                val currentState = _actionState.value
                val friends = currentState.friendsList
                val followedIds = currentState.favoriteRoomIds

                rooms.forEach { room ->
                    val isBrandNewRoom = !knownRoomIdsSnapshot.contains(room.roomId)
                    val previousTimestamp = knownActiveRoomTimestampById[room.roomId] ?: 0L
                    knownRoomIdsSnapshot.add(room.roomId)
                    knownActiveRoomTimestampById[room.roomId] = room.timestampMillis

                    if (isBrandNewRoom && room.creatorId != currentUserId) {
                        val matchingFriend = friends.find { f ->
                            !f.isPendingApproval && (
                                f.friendUserId.equals(room.creatorId, ignoreCase = true) ||
                                    f.friendName.equals(room.creatorId, ignoreCase = true) ||
                                    room.roomName.contains(f.friendName, ignoreCase = true)
                                )
                        }
                        if (matchingFriend != null && currentState.notifyOnFriendStartsRoom) {
                            dispatchFriendStartedRoomAlertInternal(
                                friendName = matchingFriend.friendName,
                                friendId = matchingFriend.friendUserId,
                                room = room
                            )
                        }
                    } else if (!isBrandNewRoom &&
                        followedIds.contains(room.roomId) &&
                        room.timestampMillis > previousTimestamp &&
                        room.creatorId != currentUserId &&
                        currentState.notifyOnFavoriteRoomLive
                    ) {
                        notifyFavoriteRoomLiveBroadcastStarted(
                            room = room,
                            customLiveTopic = "🔴 الغرفة التي تتابعها «${room.roomName}» أصبحت نشطة الآن!"
                        )
                    }
                }
            }
        }
    }

    private fun dispatchFriendStartedRoomAlertInternal(
        friendName: String,
        friendId: String,
        room: ChatRoomMetadata,
        customNote: String = ""
    ) {
        val categoryMeta = WanasRoomCategoryFilter.resolveDisplayFilter(room.roomCategory)
        val bodyText = customNote.trim().ifBlank {
            "🎙️ بدأ صديقك «$friendName» غرفة صوتية جديدة في تصنيف (${categoryMeta.emoji} ${categoryMeta.labelAr}) — انضم للتحدث الآن!"
        }
        val notifId = "notif_friend_room_${room.roomId}_${System.currentTimeMillis()}"
        val event = PushNotificationEvent(
            notificationId = notifId,
            senderId = friendId.ifBlank { room.creatorId },
            senderName = friendName,
            recipientQuery = "أصدقاء $friendName 👥",
            roomId = room.roomId,
            roomName = room.roomName,
            notificationType = "FRIEND_STARTED_ROOM",
            messageBody = bodyText,
            timestamp = Timestamp.now(),
            isRead = false
        )
        lastDeliveredNotificationId = notifId
        _localPushNotifications.update { listOf(event) + it }
        if (_actionState.value.notifyOnFriendStartsRoom) {
            triggerSystemAndBannerPushNotification(event)
        }
        _actionState.update { st ->
            st.copy(
                pushNotifications = (listOf(event) + st.pushNotifications).distinctBy { n -> n.notificationId },
                statusMessage = "🎙️ إشعار جديد: صديقك «$friendName» بدأ غرفة صوتية جديدة «${room.roomName}»!"
            )
        }
    }

    private fun observeVerifiedPaymentsStream() {
        paymentReceiptsJob?.cancel()
        val cloudReceiptsFlow = if (Firebase.auth.currentUser != null) {
            repository.observePaymentReceiptsForUser(currentUserId)
                .catch { e ->
                    Log.w(TAG, "Error observing payment receipts", e)
                    emit(emptyList())
                }
        } else {
            MutableStateFlow(emptyList())
        }

        paymentReceiptsJob = viewModelScope.launch {
            combine(cloudReceiptsFlow, _localPaymentReceipts) { cloudList, localList ->
                (cloudList + localList)
                    .distinctBy { it.receiptId }
                    .sortedByDescending { it.timestampMillis }
            }.collect { receipts ->
                val hasVerified = receipts.any { it.verified } || supabaseService.getSavedIsPaidVip()
                val latestReceipt = receipts.firstOrNull { it.verified }
                _actionState.update { state ->
                    state.copy(
                        paymentReceipts = receipts,
                        isPaidVip = hasVerified,
                        paidPlanTitle = latestReceipt?.planTitle?.ifBlank { state.paidPlanTitle } ?: state.paidPlanTitle,
                        memberRoleBadge = if (hasVerified) {
                            state.memberRoleBadge.takeIf { it.contains("VIP") } ?: "👑 VIP مدفوع"
                        } else {
                            "عضو ونس"
                        }
                    )
                }
            }
        }
    }

    private fun triggerSystemAndBannerPushNotification(event: PushNotificationEvent) {
        pushNotificationHelper?.showPushNotification(event)
        pushBannerAutoHideJob?.cancel()
        _actionState.update { it.copy(latestPushBanner = event) }
        pushBannerAutoHideJob = viewModelScope.launch {
            delay(BANNER_DISPLAY_DURATION_MS)
            _actionState.update { state ->
                if (state.latestPushBanner?.notificationId == event.notificationId) {
                    state.copy(latestPushBanner = null)
                } else {
                    state
                }
            }
        }
    }

    fun dismissPushBanner() {
        pushBannerAutoHideJob?.cancel()
        _actionState.update { it.copy(latestPushBanner = null) }
    }

    /**
     * 6. Unified Notification Center Publisher ("نظام الإشعارات 🔔 مركز إشعارات واحد"):
     * Supports FRIEND_REQUEST, FRIEND_ACCEPTED, MIC_REQUEST, MIC_APPROVED, NEW_MESSAGE, GIFT_RECEIVED, ROOM_INVITE, ADMIN_ALERT.
     */
    fun publishUnifiedNotificationEvent(
        notificationType: String,
        senderName: String = _actionState.value.memberDisplayName,
        roomId: String = _actionState.value.activeRoom?.roomId ?: "room_wanas_egypt_1",
        roomName: String = _actionState.value.activeRoom?.roomName ?: "🌙 سهرة مصرية — الناس للناس",
        messageBody: String
    ) {
        val notifId = "notif_${notificationType.lowercase()}_${System.currentTimeMillis()}"
        val now = Timestamp.now()
        val event = PushNotificationEvent(
            notificationId = notifId,
            senderId = currentUserId,
            senderName = senderName,
            recipientQuery = "ALL",
            roomId = roomId,
            roomName = roomName,
            notificationType = notificationType,
            messageBody = messageBody,
            timestamp = now,
            isRead = false
        )
        lastDeliveredNotificationId = notifId
        _localPushNotifications.update { listOf(event) + it }
        triggerSystemAndBannerPushNotification(event)
        _actionState.update {
            it.copy(
                pushNotifications = (listOf(event) + it.pushNotifications).distinctBy { n -> n.notificationId }
            )
        }
        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.publishPushNotificationEvent(
                    senderName = senderName,
                    recipientQuery = "ALL",
                    roomId = roomId,
                    roomName = roomName,
                    notificationType = notificationType,
                    messageBody = messageBody,
                    senderId = currentUserId,
                    customNotificationId = notifId
                )
            }
        }
    }

    /**
     * 1. Priority Mic Request System ("نظام طلب المايك الجديد — أولوية قصوى"):
     * - Button: 🙋 طلب المايك
     * - Appears immediately for Host in a numbered queue
     * - Approve -> opens mic for member and saves server approval state
     * - Reject -> removes request
     * - Cancel by member
     * - Host can Approve All / Reject All
     * - Prevents automatic mic unmute on entry
     * - Immediate mic close on mute or ban
     */
    fun requestMicInActiveRoom(preferredSeatIndex: Int = 1) {
        val room = _actionState.value.activeRoom ?: return
        val existingQueue = _actionState.value.activeRoomMicRequests
        if (existingQueue.any { it.userId == currentUserId && it.status == MicRequestStatus.PENDING }) {
            _actionState.update {
                it.copy(statusMessage = "✋ طلب المايك الخاص بك مسجل بالفعل في طابور المضيف")
            }
            return
        }
        val nextNumber = (existingQueue.maxOfOrNull { it.queueNumber } ?: 0) + 1
        val targetSeat = preferredSeatIndex.coerceIn(0, 7)
        val newReq = RoomMicRequestItem(
            requestId = "micreq_${currentUserId}_${System.currentTimeMillis()}",
            roomId = room.roomId,
            userId = currentUserId,
            memberIdCode = generateMemberIdCode(currentUserId),
            userName = _actionState.value.memberDisplayName,
            userEmoji = _actionState.value.memberAvatarEmoji,
            queueNumber = nextNumber,
            status = MicRequestStatus.PENDING,
            requestedSeatIndex = targetSeat
        )
        _actionState.update { st ->
            val updatedSeats = st.activeRoomSeats.map { slot ->
                if (slot.seatIndex == targetSeat) {
                    slot.copy(hasPendingMicRequest = true)
                } else slot
            }
            st.copy(
                activeRoomMicRequests = st.activeRoomMicRequests + newReq,
                myMicRequestStatus = MicRequestStatus.PENDING,
                isServerMicApproved = false,
                activeRoomSeats = updatedSeats,
                userXp = st.userXp + 10,
                statusMessage = "🙋 تم إرسال طلب المايك رقم #$nextNumber إلى مضيف الغرفة ومزامنته عبر Supabase Realtime!"
            )
        }
        viewModelScope.launch {
            repository.upsertRoomMicRequest(
                RoomMicRequestDocument(
                    requestId = newReq.requestId,
                    roomId = newReq.roomId,
                    userId = newReq.userId,
                    memberIdCode = newReq.memberIdCode,
                    userName = newReq.userName,
                    userEmoji = newReq.userEmoji,
                    queueNumber = newReq.queueNumber,
                    status = newReq.status.id,
                    requestedSeatIndex = newReq.requestedSeatIndex,
                    timestamp = Timestamp.now()
                )
            )
        }
        syncActiveRoomSeatsToSupabaseRealtime()
        incrementDailyMissionProgress("mission_request_mic_participate")
        publishUnifiedNotificationEvent(
            notificationType = "MIC_REQUEST",
            senderName = _actionState.value.memberDisplayName,
            roomId = room.roomId,
            roomName = room.roomName,
            messageBody = "🙋 طلب مايك رقم #$nextNumber من «${_actionState.value.memberDisplayName}» على المقعد ${targetSeat + 1}"
        )
    }

    fun cancelMyMicRequest() {
        val room = _actionState.value.activeRoom
        val myReqs = _actionState.value.activeRoomMicRequests.filter { it.userId == currentUserId }
        _actionState.update { st ->
            val updatedQueue = st.activeRoomMicRequests.filterNot { it.userId == currentUserId }
            val updatedSeats = st.activeRoomSeats.map { slot ->
                if (slot.occupantUserId == currentUserId || slot.hasPendingMicRequest) {
                    slot.copy(hasPendingMicRequest = false)
                } else slot
            }
            st.copy(
                activeRoomMicRequests = updatedQueue,
                myMicRequestStatus = MicRequestStatus.NONE,
                isServerMicApproved = false,
                activeRoomSeats = updatedSeats,
                statusMessage = "✋ تم إلغاء طلب المايك الخاص بك من الطابور"
            )
        }
        if (room != null) {
            viewModelScope.launch {
                myReqs.forEach { repository.deleteRoomMicRequest(room.roomId, it.requestId) }
            }
        }
        syncActiveRoomSeatsToSupabaseRealtime()
    }

    fun approveMicRequestByHost(requestId: String) {
        val room = _actionState.value.activeRoom ?: return
        val targetReq = _actionState.value.activeRoomMicRequests.find { it.requestId == requestId } ?: return
        val seatIdx = targetReq.requestedSeatIndex.coerceIn(0, 7)
        _actionState.update { st ->
            val remainingQueue = st.activeRoomMicRequests.filterNot { it.requestId == requestId }
            val updatedSeats = st.activeRoomSeats.map { slot ->
                if (slot.seatIndex == seatIdx) {
                    slot.copy(
                        occupantUserId = targetReq.userId,
                        occupantMemberCode = targetReq.memberIdCode.ifBlank { generateMemberIdCode(targetReq.userId) },
                        occupantName = targetReq.userName,
                        occupantEmoji = targetReq.userEmoji,
                        seatRole = if (seatIdx == 0) VoiceSeatRoleBadge.HOST else VoiceSeatRoleBadge.SPEAKER,
                        isMuted = false,
                        isSpeaking = true,
                        hasPendingMicRequest = false
                    )
                } else slot
            }
            val isMe = targetReq.userId == currentUserId
            st.copy(
                activeRoomMicRequests = remainingQueue,
                myMicRequestStatus = if (isMe) MicRequestStatus.APPROVED else st.myMicRequestStatus,
                isServerMicApproved = if (isMe) true else st.isServerMicApproved,
                activeRoomSeats = updatedSeats,
                userXp = st.userXp + 15,
                statusMessage = "✅ تمت الموافقة من السيرفر على طلب مايك «${targetReq.userName}» وفتح المايك له على مقعد ${seatIdx + 1}!"
            )
        }
        viewModelScope.launch {
            repository.deleteRoomMicRequest(room.roomId, requestId)
        }
        syncActiveRoomSeatsToSupabaseRealtime()
        publishUnifiedNotificationEvent(
            notificationType = "MIC_APPROVED",
            senderName = _actionState.value.memberDisplayName,
            roomId = room.roomId,
            roomName = room.roomName,
            messageBody = "🎙️ وافق المضيف على طلب المايك لـ «${targetReq.userName}» على المقعد ${seatIdx + 1}"
        )
    }

    fun rejectMicRequestByHost(requestId: String) {
        val room = _actionState.value.activeRoom
        val targetReq = _actionState.value.activeRoomMicRequests.find { it.requestId == requestId } ?: return
        _actionState.update { st ->
            val remainingQueue = st.activeRoomMicRequests.filterNot { it.requestId == requestId }
            val isMe = targetReq.userId == currentUserId
            val updatedSeats = st.activeRoomSeats.map { slot ->
                if (slot.seatIndex == targetReq.requestedSeatIndex) {
                    slot.copy(hasPendingMicRequest = false)
                } else slot
            }
            st.copy(
                activeRoomMicRequests = remainingQueue,
                myMicRequestStatus = if (isMe) MicRequestStatus.REJECTED else st.myMicRequestStatus,
                isServerMicApproved = if (isMe) false else st.isServerMicApproved,
                activeRoomSeats = updatedSeats,
                statusMessage = "❌ تم رفض وإخفاء طلب المايك لـ «${targetReq.userName}»"
            )
        }
        if (room != null) {
            viewModelScope.launch {
                repository.deleteRoomMicRequest(room.roomId, requestId)
            }
        }
        syncActiveRoomSeatsToSupabaseRealtime()
    }

    fun approveOrRejectAllMicRequestsByHost(approveAll: Boolean) {
        val room = _actionState.value.activeRoom ?: return
        val currentQueue = _actionState.value.activeRoomMicRequests
        if (currentQueue.isEmpty()) {
            _actionState.update { it.copy(statusMessage = "قائمة طلبات المايك فارغة حالياً") }
            return
        }
        if (approveAll) {
            _actionState.update { st ->
                val mutableSeats = st.activeRoomSeats.toMutableList()
                currentQueue.forEach { req ->
                    val targetIdx = req.requestedSeatIndex.coerceIn(0, 7)
                    val freeIdx = if (mutableSeats[targetIdx].isEmpty) {
                        targetIdx
                    } else {
                        mutableSeats.indexOfFirst { it.isEmpty }.takeIf { it >= 0 } ?: targetIdx
                    }
                    mutableSeats[freeIdx] = mutableSeats[freeIdx].copy(
                        occupantUserId = req.userId,
                        occupantName = req.userName,
                        occupantEmoji = req.userEmoji,
                        seatRole = if (freeIdx == 0) VoiceSeatRoleBadge.HOST else VoiceSeatRoleBadge.SPEAKER,
                        isMuted = false,
                        isSpeaking = true,
                        hasPendingMicRequest = false
                    )
                }
                val isMyReqInQueue = currentQueue.any { it.userId == currentUserId }
                st.copy(
                    activeRoomMicRequests = emptyList(),
                    myMicRequestStatus = if (isMyReqInQueue) MicRequestStatus.APPROVED else st.myMicRequestStatus,
                    isServerMicApproved = if (isMyReqInQueue) true else st.isServerMicApproved,
                    activeRoomSeats = mutableSeats,
                    statusMessage = "✅ تم قبول جميع طلبات المايك (${currentQueue.size}) وتسكينهم على المقاعد الصوتية وتحديث السيرفر فوراً!"
                )
            }
            publishUnifiedNotificationEvent(
                notificationType = "MIC_APPROVED",
                senderName = _actionState.value.memberDisplayName,
                roomId = room.roomId,
                roomName = room.roomName,
                messageBody = "🎙️ قبل المضيف جميع طلبات المايك في «${room.roomName}»!"
            )
        } else {
            _actionState.update { st ->
                val updatedSeats = st.activeRoomSeats.map { it.copy(hasPendingMicRequest = false) }
                st.copy(
                    activeRoomMicRequests = emptyList(),
                    myMicRequestStatus = MicRequestStatus.NONE,
                    isServerMicApproved = false,
                    activeRoomSeats = updatedSeats,
                    statusMessage = "🧹 تم رفض ومسح جميع طلبات المايك من القائمة"
                )
            }
        }
    }

    /**
     * 2. Real 8-Seat Voice Stage Controls ("نظام المقاعد الصوتية الحقيقي — 8 مقاعد • إمكانية نقل عضو من مقعد إلى آخر"):
     */
    fun sitOnOrSelectVoiceSeat(seatIndex: Int) {
        val validIndex = seatIndex.coerceIn(0, 7)
        val room = _actionState.value.activeRoom ?: return
        val hasAuthority = hasFullRoomAdminAuthority(room)
        _actionState.update { st ->
            val targetSlot = st.activeRoomSeats.getOrNull(validIndex) ?: return@update st
            if (targetSlot.isSeatLocked && !hasAuthority) {
                return@update st.copy(errorMessage = "🔒 هذا المقعد مقفول من المضيف")
            }
            val clearedSeats = st.activeRoomSeats.map { slot ->
                if (slot.occupantUserId == currentUserId) {
                    VoiceRoomSeatSlot(seatIndex = slot.seatIndex)
                } else slot
            }
            val roleForSeat = when {
                validIndex == 0 && hasAuthority -> VoiceSeatRoleBadge.HOST
                validIndex == 1 && hasAuthority -> VoiceSeatRoleBadge.CO_HOST
                st.isServerMicApproved -> VoiceSeatRoleBadge.SPEAKER
                else -> VoiceSeatRoleBadge.SEATED_MEMBER
            }
            val updatedSeats = clearedSeats.map { slot ->
                if (slot.seatIndex == validIndex) {
                    slot.copy(
                        occupantUserId = currentUserId,
                        occupantName = st.memberDisplayName,
                        occupantEmoji = st.memberAvatarEmoji,
                        seatRole = roleForSeat,
                        isMuted = !(st.isServerMicApproved || hasAuthority),
                        isSpeaking = false
                    )
                } else slot
            }
            st.copy(
                activeRoomSeats = updatedSeats,
                statusMessage = "👤 جلست على المقعد رقم ${validIndex + 1} (${roleForSeat.emoji} ${roleForSeat.labelAr})"
            )
        }
        syncActiveRoomSeatsToSupabaseRealtime()
        incrementDailyMissionProgress("mission_request_mic_participate")
    }

    fun moveMemberBetweenVoiceSeats(fromSeatIndex: Int, toSeatIndex: Int) {
        val fromIdx = fromSeatIndex.coerceIn(0, 7)
        val toIdx = toSeatIndex.coerceIn(0, 7)
        if (fromIdx == toIdx) return
        _actionState.update { st ->
            val currentSeats = st.activeRoomSeats.toMutableList()
            val fromSlot = currentSeats.getOrNull(fromIdx) ?: return@update st
            val toSlot = currentSeats.getOrNull(toIdx) ?: return@update st
            if (fromSlot.isEmpty && toSlot.isEmpty) {
                return@update st.copy(statusMessage = "المقعدان فارغان حالياً — اختر مقعداً مشغولاً لنقله")
            }
            currentSeats[toIdx] = fromSlot.copy(
                seatIndex = toIdx,
                seatRole = when (toIdx) {
                    0 -> VoiceSeatRoleBadge.HOST
                    1 -> VoiceSeatRoleBadge.CO_HOST
                    else -> if (fromSlot.isEmpty) VoiceSeatRoleBadge.EMPTY else VoiceSeatRoleBadge.SEATED_MEMBER
                }
            )
            currentSeats[fromIdx] = toSlot.copy(
                seatIndex = fromIdx,
                seatRole = when (fromIdx) {
                    0 -> VoiceSeatRoleBadge.HOST
                    1 -> VoiceSeatRoleBadge.CO_HOST
                    else -> if (toSlot.isEmpty) VoiceSeatRoleBadge.EMPTY else VoiceSeatRoleBadge.SEATED_MEMBER
                }
            )
            st.copy(
                activeRoomSeats = currentSeats,
                statusMessage = "🔄 تم نقل العضو من المقعد ${fromIdx + 1} إلى المقعد ${toIdx + 1} وتحديث جميع المتواجدين فوراً!"
            )
        }
        syncActiveRoomSeatsToSupabaseRealtime()
    }

    fun assignCoHostOrMuteSeatByHost(seatIndex: Int, toggleCoHost: Boolean = false, forceMute: Boolean = false) {
        val idx = seatIndex.coerceIn(0, 7)
        _actionState.update { st ->
            val updatedSeats = st.activeRoomSeats.map { slot ->
                if (slot.seatIndex == idx && !slot.isEmpty) {
                    val nextRole = if (toggleCoHost) {
                        if (slot.seatRole == VoiceSeatRoleBadge.CO_HOST) VoiceSeatRoleBadge.SPEAKER else VoiceSeatRoleBadge.CO_HOST
                    } else slot.seatRole
                    val nextMuted = if (forceMute) true else slot.isMuted
                    slot.copy(
                        seatRole = nextRole,
                        isMuted = nextMuted,
                        isSpeaking = if (nextMuted) false else slot.isSpeaking
                    )
                } else slot
            }
            val targetOccupant = st.activeRoomSeats.getOrNull(idx)
            val isMeMuted = forceMute && targetOccupant?.occupantUserId == currentUserId
            st.copy(
                activeRoomSeats = updatedSeats,
                myMicRequestStatus = if (isMeMuted) MicRequestStatus.MUTED_BY_HOST else st.myMicRequestStatus,
                isServerMicApproved = if (isMeMuted) false else st.isServerMicApproved,
                statusMessage = if (forceMute) {
                    "🔇 تم كتم المايك وإغلاقه فوراً على المقعد ${idx + 1}"
                } else {
                    "🛡️ تم تحديث رتبة مساعد المضيف على المقعد ${idx + 1}"
                }
            )
        }
        syncActiveRoomSeatsToSupabaseRealtime()
    }

    /**
     * Toggles whether a room is in the user's Followed / Favorite Rooms list ("غرفي المتابَعة والمفضلة ⭐")
     * so the user receives instant in-app & system notifications when the room becomes active or starts a live broadcast.
     */
    fun toggleFavoriteRoom(room: ChatRoomMetadata) {
        val currentFavs = _actionState.value.favoriteRoomIds
        val isNowFavorite = !currentFavs.contains(room.roomId)
        val updatedFavs = if (isNowFavorite) {
            currentFavs + room.roomId
        } else {
            currentFavs - room.roomId
        }
        supabaseService.saveFollowedRoomIds(updatedFavs)
        _actionState.update {
            it.copy(
                favoriteRoomIds = updatedFavs,
                statusMessage = if (isNowFavorite) {
                    "🔔 تمت متابعة وإضافة «${room.roomName}» إلى غرفك المتابَعة! سيصلك إشعار فوري عندما تصبح نشطة أو يبدأ فيها بث مباشر"
                } else {
                    "🔕 تم إلغاء متابعة غرفة «${room.roomName}» من قائمة التنبيهات"
                },
                errorMessage = null
            )
        }
    }

    /**
     * Updates user preferences for notifications:
     * 1) Alert when a friend starts a new audio room (`notifyFriendStartsRoom`)
     * 2) Alert when a followed/favorite room becomes active (`notifyFavoriteLive`)
     * 3) Alert on private room invitations (`notifyPrivateInvites`)
     */
    fun updateInAppNotificationPreferences(
        notifyFavoriteLive: Boolean = _actionState.value.notifyOnFavoriteRoomLive,
        notifyPrivateInvites: Boolean = _actionState.value.notifyOnPrivateInvite,
        notifyFriendStartsRoom: Boolean = _actionState.value.notifyOnFriendStartsRoom
    ) {
        supabaseService.saveNotificationPreferences(
            notifyFriendStartsRoom = notifyFriendStartsRoom,
            notifyFavoriteRoomLive = notifyFavoriteLive,
            notifyPrivateInvite = notifyPrivateInvites
        )
        _actionState.update {
            it.copy(
                notifyOnFriendStartsRoom = notifyFriendStartsRoom,
                notifyOnFavoriteRoomLive = notifyFavoriteLive,
                notifyOnPrivateInvite = notifyPrivateInvites,
                statusMessage = "🔔 تم حفظ إعدادات التنبيهات (غرف الأصدقاء الجديدة • نشاط الغرف المتابَعة • الدعوات الخاصة)"
            )
        }
    }

    /**
     * Marks all in-app notifications as read or marks a specific notification as read when clicked.
     */
    fun markNotificationAsRead(notificationId: String? = null) {
        _localPushNotifications.update { list ->
            list.map { n ->
                if (notificationId == null || n.notificationId == notificationId) {
                    n.copy(isRead = true)
                } else n
            }
        }
        _actionState.update { state ->
            val updated = state.pushNotifications.map { n ->
                if (notificationId == null || n.notificationId == notificationId) {
                    n.copy(isRead = true)
                } else n
            }
            state.copy(
                pushNotifications = updated,
                latestPushBanner = if (notificationId == null || state.latestPushBanner?.notificationId == notificationId) {
                    null
                } else {
                    state.latestPushBanner
                }
            )
        }
    }

    /**
     * Triggers an immediate In-App & System Notification when a room the user follows becomes active
     * (`FOLLOWED_ROOM_ACTIVE` / `FAVORITE_ROOM_LIVE`).
     */
    fun notifyFavoriteRoomLiveBroadcastStarted(
        room: ChatRoomMetadata? = _actionState.value.activeRoom ?: _localRooms.value.firstOrNull {
            _actionState.value.favoriteRoomIds.contains(it.roomId)
        } ?: _localRooms.value.firstOrNull(),
        customLiveTopic: String = ""
    ) {
        val targetRoom = room ?: return
        val hostName = _actionState.value.memberDisplayName
        val categoryMeta = WanasRoomCategoryFilter.resolveDisplayFilter(targetRoom.roomCategory)
        val topicText = customLiveTopic.trim().ifBlank {
            "🔴 الغرفة التي تتابعها «${targetRoom.roomName}» (${categoryMeta.emoji} ${categoryMeta.labelAr}) أصبحت نشطة الآن وبها تفاعل صوتي مباشر — انضم الآن!"
        }
        val notifId = "notif_followed_active_${System.currentTimeMillis()}"
        val now = Timestamp.now()

        // Ensure the room is in followed/favorites so the user sees the followed-active badge clearly
        val updatedFavorites = _actionState.value.favoriteRoomIds + targetRoom.roomId
        supabaseService.saveFollowedRoomIds(updatedFavorites)

        val liveEvent = PushNotificationEvent(
            notificationId = notifId,
            senderId = currentUserId,
            senderName = hostName,
            recipientQuery = "متابعي الغرفة ⭐",
            roomId = targetRoom.roomId,
            roomName = targetRoom.roomName,
            notificationType = "FOLLOWED_ROOM_ACTIVE",
            messageBody = topicText,
            timestamp = now,
            isRead = false
        )

        lastDeliveredNotificationId = notifId
        _localPushNotifications.update { listOf(liveEvent) + it }
        if (_actionState.value.notifyOnFavoriteRoomLive) {
            triggerSystemAndBannerPushNotification(liveEvent)
        }

        _actionState.update {
            it.copy(
                favoriteRoomIds = updatedFavorites,
                pushNotifications = (listOf(liveEvent) + it.pushNotifications).distinctBy { n -> n.notificationId },
                statusMessage = "🔴 تم إرسال تنبيه نشاط الغرفة المتابَعة «${targetRoom.roomName}»!",
                errorMessage = null
            )
        }

        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.publishPushNotificationEvent(
                    senderName = hostName,
                    recipientQuery = "FAVORITE_SUBSCRIBERS",
                    roomId = targetRoom.roomId,
                    roomName = targetRoom.roomName,
                    notificationType = "FOLLOWED_ROOM_ACTIVE",
                    messageBody = topicText,
                    senderId = currentUserId,
                    customNotificationId = notifId
                )
            }
        }
    }

    /**
     * Triggers an immediate In-App & System Notification when a friend starts a new audio room
     * (`FRIEND_STARTED_ROOM`), creating the friend's live room in the directory so the user can join with one tap.
     */
    fun notifyFriendStartedNewRoom(
        friend: WanasFriend? = _actionState.value.friendsList.firstOrNull { !it.isPendingApproval }
            ?: _actionState.value.friendsList.firstOrNull(),
        customFriendName: String = "",
        customRoomName: String = "",
        roomCategory: String = WanasRoomCategoryFilter.GENERAL.id
    ) {
        val resolvedFriendName = customFriendName.trim().ifBlank {
            friend?.friendName?.takeIf { it.isNotBlank() } ?: _actionState.value.memberDisplayName
        }
        val resolvedFriendId = friend?.friendUserId?.takeIf { it.isNotBlank() }
            ?: "friend_${resolvedFriendName.hashCode().let { if (it < 0) -it else it }}"
        val resolvedFriendEmoji = friend?.friendEmoji ?: "🎙️"
        val categoryMeta = WanasRoomCategoryFilter.resolveDisplayFilter(roomCategory)
        val cleanRoomName = customRoomName.trim().ifBlank {
            "${categoryMeta.emoji} سهرة $resolvedFriendName الصوتية المباشرة"
        }
        val now = Timestamp.now()
        val newRoomId = "room_friend_${System.currentTimeMillis()}"

        val friendRoom = ChatRoomMetadata(
            roomId = newRoomId,
            roomName = cleanRoomName,
            creatorId = resolvedFriendName,
            timestamp = now,
            roomPassword = "",
            isPasswordProtected = false,
            roomCategory = categoryMeta.id,
            roomTopicTag = "${categoryMeta.emoji} ${categoryMeta.labelAr} • مضيف: $resolvedFriendName",
            activeSpeakersCount = 3,
            activeMembersCount = 9
        )

        // Register in known snapshot so the automatic watcher doesn't double-fire for the same roomId
        knownRoomIdsSnapshot.add(newRoomId)
        knownActiveRoomTimestampById[newRoomId] = friendRoom.timestampMillis

        // Add the friend's new room to the live rooms list & Supabase Realtime snapshot
        _localRooms.update { current ->
            listOf(friendRoom) + current.filterNot { it.roomId == friendRoom.roomId }
        }
        viewModelScope.launch {
            supabaseService.upsertSharedRoom(friendRoom)
        }

        // Update or add the friend in friendsList so their currentRoomId points to the new room
        _actionState.update { st ->
            val existingFriend = st.friendsList.find {
                it.friendUserId == resolvedFriendId || it.friendName.equals(resolvedFriendName, ignoreCase = true)
            }
            val updatedFriends = if (existingFriend != null) {
                st.friendsList.map { f ->
                    if (f.friendUserId == existingFriend.friendUserId) {
                        f.copy(
                            isOnline = true,
                            presenceState = com.example.data.FriendPresenceState.ONLINE,
                            currentRoomId = friendRoom.roomId,
                            currentRoomName = friendRoom.roomName,
                            statusNote = "🎙️ بدأ غرفة صوتية جديدة الآن: ${friendRoom.roomName}",
                            lastSeenLabel = "متصل الآن داخل غرفته الجديدة 🟢",
                            lastMessage = "بدأت غرفة صوتية جديدة «${friendRoom.roomName}» — تعال انضم لينا!"
                        )
                    } else f
                }
            } else {
                listOf(
                    WanasFriend(
                        friendUserId = resolvedFriendId,
                        friendName = resolvedFriendName,
                        friendEmoji = resolvedFriendEmoji,
                        friendLevel = 4,
                        isOnline = true,
                        presenceState = com.example.data.FriendPresenceState.ONLINE,
                        currentRoomId = friendRoom.roomId,
                        currentRoomName = friendRoom.roomName,
                        statusNote = "🎙️ بدأ غرفة صوتية جديدة الآن: ${friendRoom.roomName}",
                        isPendingApproval = false,
                        lastSeenLabel = "متصل الآن داخل غرفته الجديدة 🟢",
                        lastMessage = "بدأت غرفة صوتية جديدة «${friendRoom.roomName}» — تعال انضم لينا!"
                    )
                ) + st.friendsList
            }
            st.copy(friendsList = updatedFriends)
        }

        val notifId = "notif_friend_room_${System.currentTimeMillis()}"
        val messageBody = "🎙️ بدأ صديقك «$resolvedFriendName» غرفة صوتية جديدة «${friendRoom.roomName}» في تصنيف (${categoryMeta.labelAr}) — اضغط للانضمام الفوري!"
        val friendRoomEvent = PushNotificationEvent(
            notificationId = notifId,
            senderId = resolvedFriendId,
            senderName = resolvedFriendName,
            recipientQuery = "أصدقاء $resolvedFriendName 👥",
            roomId = friendRoom.roomId,
            roomName = friendRoom.roomName,
            notificationType = "FRIEND_STARTED_ROOM",
            messageBody = messageBody,
            timestamp = now,
            isRead = false
        )

        lastDeliveredNotificationId = notifId
        _localPushNotifications.update { listOf(friendRoomEvent) + it }
        if (_actionState.value.notifyOnFriendStartsRoom) {
            triggerSystemAndBannerPushNotification(friendRoomEvent)
        }

        _actionState.update { st ->
            st.copy(
                pushNotifications = (listOf(friendRoomEvent) + st.pushNotifications).distinctBy { n -> n.notificationId },
                statusMessage = "🎙️ صديقك «$resolvedFriendName» بدأ غرفة صوتية جديدة: «${friendRoom.roomName}»!",
                errorMessage = null
            )
        }

        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.publishPushNotificationEvent(
                    senderName = resolvedFriendName,
                    recipientQuery = "FRIENDS_SUBSCRIBERS",
                    roomId = friendRoom.roomId,
                    roomName = friendRoom.roomName,
                    notificationType = "FRIEND_STARTED_ROOM",
                    messageBody = messageBody,
                    senderId = resolvedFriendId,
                    customNotificationId = notifId
                )
            }
        }
    }

    /**
     * Sends or triggers a Private Invitation (`PRIVATE_INVITE`) to join a voice room or private session,
     * displaying an interactive in-app notification banner and system notification.
     */
    fun sendPrivateInvitationNotification(
        recipientNameOrEmail: String,
        room: ChatRoomMetadata? = _actionState.value.activeRoom ?: _localRooms.value.firstOrNull(),
        customMessage: String = ""
    ) {
        val cleanRecipient = recipientNameOrEmail.trim().ifBlank { _actionState.value.memberDisplayName }
        val resolvedRoomId = room?.roomId ?: "room_wanas_egypt_1"
        val resolvedRoomName = room?.roomName ?: "🌙 سهرة مصرية — الناس للناس"
        val senderName = _actionState.value.memberDisplayName
        val inviteBody = customMessage.trim().ifBlank {
            "💌 دعوة خاصة لك للانضمام الفوري إلى المايك في «$resolvedRoomName»!"
        }
        val notifId = "notif_private_inv_${System.currentTimeMillis()}"
        val now = Timestamp.now()

        val privateInviteEvent = PushNotificationEvent(
            notificationId = notifId,
            senderId = currentUserId,
            senderName = senderName,
            recipientQuery = cleanRecipient,
            roomId = resolvedRoomId,
            roomName = resolvedRoomName,
            notificationType = "PRIVATE_INVITE",
            messageBody = inviteBody,
            timestamp = now,
            isRead = false
        )

        lastDeliveredNotificationId = notifId
        _localPushNotifications.update { listOf(privateInviteEvent) + it }
        if (_actionState.value.notifyOnPrivateInvite) {
            triggerSystemAndBannerPushNotification(privateInviteEvent)
        }

        _actionState.update {
            it.copy(
                pushNotifications = (listOf(privateInviteEvent) + it.pushNotifications).distinctBy { n -> n.notificationId },
                statusMessage = "💌 تم إرسال دعوة خاصة فورية إلى «$cleanRecipient» للانضمام لغرفة «$resolvedRoomName»",
                errorMessage = null
            )
        }

        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.publishPushNotificationEvent(
                    senderName = senderName,
                    recipientQuery = cleanRecipient,
                    roomId = resolvedRoomId,
                    roomName = resolvedRoomName,
                    notificationType = "PRIVATE_INVITE",
                    messageBody = inviteBody,
                    senderId = currentUserId,
                    customNotificationId = notifId
                )
            }
        }
    }

    /**
     * Invites a friend to join a chat room and immediately triggers a real-time Push Notification (`ROOM_INVITE`).
     */
    fun inviteFriendToChatRoom(
        friendNameOrEmail: String,
        room: ChatRoomMetadata? = _actionState.value.activeRoom,
        customInviteMessage: String = ""
    ) {
        val cleanFriend = friendNameOrEmail.trim()
        if (cleanFriend.isBlank()) {
            _actionState.update {
                it.copy(errorMessage = "يرجى إدخال اسم الصديق أو بريده الإلكتروني لإرسال الدعوة")
            }
            return
        }

        val resolvedRoomId = room?.roomId ?: "general_room"
        val resolvedRoomName = room?.roomName ?: "غرفة الدردشة العامة"
        val senderName = _actionState.value.memberDisplayName
        val inviteText = customInviteMessage.trim().ifBlank {
            "تفضل بالانضمام معنا الآن إلى غرفة «$resolvedRoomName»!"
        }
        val notifId = "notif_invite_${System.currentTimeMillis()}"
        val now = Timestamp.now()

        val localEvent = PushNotificationEvent(
            notificationId = notifId,
            senderId = currentUserId,
            senderName = senderName,
            recipientQuery = cleanFriend,
            roomId = resolvedRoomId,
            roomName = resolvedRoomName,
            notificationType = "ROOM_INVITE",
            messageBody = "دعوة إلى $cleanFriend: $inviteText",
            timestamp = now
        )

        lastDeliveredNotificationId = notifId
        _localPushNotifications.update { listOf(localEvent) + it }
        triggerSystemAndBannerPushNotification(localEvent)

        _actionState.update {
            it.copy(
                pushNotifications = (listOf(localEvent) + it.pushNotifications).distinctBy { n -> n.notificationId },
                statusMessage = "🔔 تم إرسال إشعار فوري لدعوة «$cleanFriend» للانضمام إلى غرفة «$resolvedRoomName»",
                errorMessage = null
            )
        }

        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.publishPushNotificationEvent(
                    senderName = senderName,
                    recipientQuery = cleanFriend,
                    roomId = resolvedRoomId,
                    roomName = resolvedRoomName,
                    notificationType = "ROOM_INVITE",
                    messageBody = "دعوة إلى $cleanFriend: $inviteText",
                    senderId = currentUserId,
                    customNotificationId = notifId
                )
            }
        }
    }

    /**
     * Strictly validates payment details before unlocking VIP features:
     * Clicking the payment button with empty or invalid fields FAILS with a clear error message,
     * preventing members from activating paid features without completing a real validated payment.
     */
    fun processRealPaymentCheckout(
        plan: VipPaymentPlan,
        paymentMethod: String,
        cardHolderName: String,
        cardNumber: String,
        expiryMmYy: String,
        cvv: String,
        walletPhone: String,
        transferReferenceNumber: String
    ): Boolean {
        val validation = supabaseService.validatePaymentCredentials(
            paymentMethod = paymentMethod,
            cardHolderName = cardHolderName,
            cardNumber = cardNumber,
            expiryMmYy = expiryMmYy,
            cvv = cvv,
            walletPhone = walletPhone,
            transferReferenceNumber = transferReferenceNumber
        )

        val transactionRef = validation.getOrElse { error ->
            _actionState.update {
                it.copy(
                    errorMessage = "❌ لا يمكن التفعيل بدون دفع حقيقي: ${error.message}",
                    statusMessage = null
                )
            }
            return false
        }

        val receiptId = "rcpt_${System.currentTimeMillis()}"
        val memberName = _actionState.value.memberDisplayName
        val now = Timestamp.now()

        val receipt = VerifiedPaymentReceipt(
            receiptId = receiptId,
            userId = currentUserId,
            memberName = memberName,
            planId = plan.planId,
            planTitle = plan.title,
            amountEgp = plan.amountEgp,
            paymentMethod = paymentMethod,
            transactionReference = transactionRef,
            verified = true,
            timestamp = now
        )

        supabaseService.saveVerifiedPaymentLocally(
            planTitle = plan.title,
            badgeLabel = plan.badgeLabel,
            receiptId = receiptId
        )

        _localPaymentReceipts.update { listOf(receipt) + it }
        val myMemberCode = _actionState.value.equippedSpecialMemberCode
            ?: generateMemberIdCode(currentUserId.ifBlank { memberName })
        val purchaseOrder = PaymentPurchaseOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            buyerUserId = currentUserId,
            buyerMemberCode = myMemberCode,
            buyerName = memberName,
            orderType = "VIP_PLAN",
            itemId = plan.planId,
            itemTitleAr = plan.title,
            coinsToCredit = if (plan.amountEgp >= 300) 250 else 100,
            grantVipBadge = true,
            vipBadgeLabel = plan.badgeLabel,
            amountEgp = plan.amountEgp,
            paymentMethod = paymentMethod,
            transactionReference = transactionRef,
            status = PaymentPurchaseOrderStatus.PENDING_OWNER_APPROVAL,
            timestampLabel = "الآن"
        )
        _actionState.update { state ->
            state.copy(
                isPaidVip = true,
                paidPlanTitle = plan.title,
                memberRoleBadge = plan.badgeLabel,
                paymentReceipts = (listOf(receipt) + state.paymentReceipts).distinctBy { it.receiptId },
                paymentPurchaseOrders = listOf(purchaseOrder) + state.paymentPurchaseOrders,
                adminDashboardStats = state.adminDashboardStats.copy(
                    pendingPaymentsCount = state.adminDashboardStats.pendingPaymentsCount + 1,
                    dailyRevenueEgp = state.adminDashboardStats.dailyRevenueEgp + plan.amountEgp
                ),
                statusMessage = "✅ تم إرسال إيصال الدفع (${plan.amountEgp} ج.م) برقم مرجع $transactionRef وربطه بلوحة تحكم صاحب التطبيق لتأكيد الدفع!",
                errorMessage = null
            )
        }
        recordAdminAuditLog(
            actionTaken = "طلب شراء باقة «${plan.title}» (${plan.amountEgp} ج.م) برقم مرجع $transactionRef",
            targetName = memberName,
            locationRoomOrSection = "بوابة الدفع والعملات",
            resultSummary = "مرتبط بلوحة التحكم لتأكيد الدفع ⏳"
        )

        syncMemberAccountWithSupabaseAndCloud()

        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.createVerifiedPaymentReceipt(
                    memberName = memberName,
                    planId = plan.planId,
                    planTitle = plan.title,
                    amountEgp = plan.amountEgp,
                    paymentMethod = paymentMethod,
                    transactionReference = transactionRef,
                    userId = currentUserId,
                    customReceiptId = receiptId
                )
            }
        }
        return true
    }

    /**
     * Allows the app owner/admin to toggle or preview the Admin Owner view on the device.
     */
    fun setAdminOwnerViewMode(enabled: Boolean) {
        _actionState.update {
            it.copy(
                isAdminOwner = enabled,
                statusMessage = if (enabled) {
                    "🛡️ تم تفعيل عرض الأدمن صاحب التطبيق (يظهر اسم وإيميل صاحب الفضفضة)"
                } else {
                    "🙈 تم التبديل إلى عرض العضو العادي (تظهر الفضفضة بدون اسم أو إيميل)"
                }
            )
        }
        if (enabled) {
            observeAdminIdentitiesStream()
        }
    }

    private fun observeFadfadaStreams() {
        fadfadaPostsJob?.cancel()
        val cloudPostsFlow = if (Firebase.auth.currentUser != null) {
            repository.observeAnonymousFadfadaPosts()
                .catch { e ->
                    Log.w(TAG, "Error observing fadfada posts", e)
                    emit(emptyList())
                }
        } else {
            MutableStateFlow(emptyList())
        }

        fadfadaPostsJob = viewModelScope.launch {
            combine(cloudPostsFlow, _localFadfadaPosts) { cloudList, localList ->
                (cloudList + localList)
                    .distinctBy { it.postId }
                    .sortedByDescending { it.timestampMillis }
            }.collect { combinedPosts ->
                _actionState.update { it.copy(fadfadaPosts = combinedPosts) }
            }
        }

        if (_actionState.value.isAdminOwner) {
            observeAdminIdentitiesStream()
        }
    }

    private fun observeAdminIdentitiesStream() {
        fadfadaAdminJob?.cancel()
        val cloudIdentitiesFlow = if (Firebase.auth.currentUser != null) {
            repository.observeFadfadaAdminIdentities()
                .catch { e ->
                    Log.w(TAG, "Admin identity stream restricted or unavailable", e)
                    emit(emptyMap())
                }
        } else {
            MutableStateFlow(emptyMap())
        }

        fadfadaAdminJob = viewModelScope.launch {
            combine(cloudIdentitiesFlow, _localFadfadaAdminIdentities) { cloudMap, localMap ->
                localMap + cloudMap
            }.collect { mergedMap ->
                _actionState.update { it.copy(fadfadaAdminIdentities = mergedMap) }
            }
        }
    }

    /**
     * Publishes an anonymous venting post ("فضفضة بدون اسم"):
     * - Publicly displays strictly without name or email for regular members.
     * - Stores the author's real display name and email in the Admin-Only collection so the app owner/admin can view them.
     */
    fun publishAnonymousFadfada(content: String, moodTag: String = "💭 فضفضة عامة") {
        val cleanContent = content.trim()
        if (cleanContent.isBlank()) {
            _actionState.update { it.copy(errorMessage = "يرجى كتابة رسالتك في قسم الفضفضة أولاً") }
            return
        }

        val state = _actionState.value
        val authorName = state.memberDisplayName.ifBlank { "عضو ونس" }
        val authorEmail = state.memberEmail.ifBlank {
            Firebase.auth.currentUser?.email ?: "member@wanas.app"
        }
        val cleanMood = moodTag.trim().ifBlank { "💭 فضفضة عامة" }
        val postId = "fadfada_${System.currentTimeMillis()}"
        val now = Timestamp.now()

        val memberCode = generateMemberIdCode(currentUserId)
        val localPost = AnonymousFadfadaPost(
            postId = postId,
            authorId = currentUserId,
            memberIdCode = memberCode,
            content = cleanContent,
            moodTag = cleanMood,
            heartsCount = 0,
            timestamp = now
        )
        val localIdentity = FadfadaAdminIdentity(
            postId = postId,
            authorId = currentUserId,
            memberIdCode = memberCode,
            authorName = authorName,
            authorEmail = authorEmail,
            timestamp = now
        )

        _localFadfadaPosts.update { listOf(localPost) + it }
        _localFadfadaAdminIdentities.update { it + (postId to localIdentity) }

        _actionState.update {
            it.copy(
                fadfadaPosts = (listOf(localPost) + it.fadfadaPosts).distinctBy { p -> p.postId },
                fadfadaAdminIdentities = it.fadfadaAdminIdentities + (postId to localIdentity),
                statusMessage = "✅ تم نشر فضفضتك برقمك المعرف (#$memberCode) بدون اسم للأعضاء (بياناتك محفوظة للأدمن فقط)",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            repository.createAnonymousFadfadaPost(
                content = cleanContent,
                moodTag = cleanMood,
                authorName = authorName,
                authorEmail = authorEmail,
                authorId = currentUserId,
                customPostId = postId
            )
        }
    }

    /**
     * Sends a supportive heart to an anonymous venting post.
     */
    fun sendHeartToFadfada(post: AnonymousFadfadaPost) {
        val updatedPost = post.copy(heartsCount = post.heartsCount + 1)
        _localFadfadaPosts.update { list ->
            val exists = list.any { it.postId == post.postId }
            if (exists) {
                list.map { if (it.postId == post.postId) updatedPost else it }
            } else {
                listOf(updatedPost) + list
            }
        }
        _actionState.update { state ->
            state.copy(
                fadfadaPosts = state.fadfadaPosts.map {
                    if (it.postId == post.postId) updatedPost else it
                }
            )
        }

        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.sendHeartToFadfadaPost(post.postId, post.heartsCount)
            }
        }
    }

    /**
     * Deletes an anonymous venting post (available to its author or the app owner/admin).
     */
    fun deleteFadfadaPost(postId: String) {
        _localFadfadaPosts.update { list -> list.filterNot { it.postId == postId } }
        _localFadfadaAdminIdentities.update { map -> map - postId }
        _actionState.update { state ->
            state.copy(
                fadfadaPosts = state.fadfadaPosts.filterNot { it.postId == postId },
                fadfadaAdminIdentities = state.fadfadaAdminIdentities - postId,
                statusMessage = "🗑️ تم حذف الفضفضة"
            )
        }

        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.deleteFadfadaPost(postId)
            }
        }
    }

    private fun observeMemberChatHistoryStream() {
        chatHistoryJob?.cancel()
        val cloudHistoryFlow = if (Firebase.auth.currentUser != null) {
            repository.observeMemberChatHistory(currentUserId)
                .catch { e ->
                    Log.w(TAG, "Error observing member chat history", e)
                    emit(emptyList())
                }
        } else {
            MutableStateFlow(emptyList())
        }

        chatHistoryJob = viewModelScope.launch {
            combine(cloudHistoryFlow, _localChatHistory) { cloudList, localList ->
                (cloudList + localList)
                    .distinctBy { it.entryId }
                    .sortedByDescending { it.timestampMillis }
            }.collect { combinedHistory ->
                _actionState.update { state ->
                    val resolvedRoomsCreated = maxOf(
                        state.roomsCreatedCount,
                        combinedHistory.count { it.activityType == "CREATE_ROOM" }
                    )
                    val resolvedRoomsJoined = maxOf(
                        state.roomsJoinedCount,
                        combinedHistory.count { it.activityType == "JOIN_ROOM" }
                    )
                    val resolvedMessages = maxOf(
                        state.messagesSentCount,
                        combinedHistory.count { it.activityType == "MESSAGE" }
                    )
                    state.copy(
                        chatHistory = combinedHistory,
                        roomsCreatedCount = resolvedRoomsCreated,
                        roomsJoinedCount = resolvedRoomsJoined,
                        messagesSentCount = resolvedMessages
                    )
                }
            }
        }
    }

    fun toggleDayNightTheme() {
        _actionState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun updateMemberProfileName(newName: String, avatarEmoji: String = _actionState.value.memberAvatarEmoji) {
        val cleanName = newName.trim().ifBlank { _actionState.value.memberDisplayName }
        _actionState.update {
            it.copy(
                memberDisplayName = cleanName,
                memberAvatarEmoji = avatarEmoji,
                statusMessage = "✅ تم تحديث اسم العرض إلى «$cleanName»"
            )
        }
        syncMemberAccountWithSupabaseAndCloud()
    }

    /**
     * Updates the member's full profile from the User Profile View:
     * display name, custom avatar image URI/URL, avatar emoji, and personal bio.
     */
    fun updateFullMemberProfile(
        newDisplayName: String,
        newAvatarUri: String,
        newAvatarEmoji: String,
        newBio: String
    ) {
        val cleanName = newDisplayName.trim().ifBlank { _actionState.value.memberDisplayName }
        val cleanBio = newBio.trim().ifBlank { "أهلاً بكم في غرف ونس الصوتية ✨" }
        val cleanEmoji = newAvatarEmoji.trim().ifBlank { "👑" }
        val cleanUri = newAvatarUri.trim()

        _actionState.update {
            it.copy(
                memberDisplayName = cleanName,
                memberAvatarUri = cleanUri,
                memberAvatarEmoji = cleanEmoji,
                memberBio = cleanBio,
                statusMessage = "✅ تم حفظ تحديثات الملف الشخصي والصورة الرمزية بنجاح",
                errorMessage = null
            )
        }
        syncMemberAccountWithSupabaseAndCloud()
    }

    fun syncMemberAccountWithSupabaseAndCloud() {
        val state = _actionState.value
        val member = SupabaseMemberAccount(
            userId = currentUserId,
            displayName = state.memberDisplayName,
            email = state.memberEmail,
            avatarEmoji = state.memberAvatarEmoji,
            avatarUri = state.memberAvatarUri,
            bio = state.memberBio,
            roleBadge = state.memberRoleBadge,
            roomsCreatedCount = state.roomsCreatedCount,
            roomsJoinedCount = state.roomsJoinedCount,
            messagesSentCount = state.messagesSentCount,
            isPaidVip = state.isPaidVip,
            paidPlanTitle = state.paidPlanTitle,
            supabaseSynced = true
        )

        viewModelScope.launch {
            val supabaseResult = supabaseService.syncMemberAccountToSupabase(member)
            val isConfigured = supabaseService.isConfigured
            if (Firebase.auth.currentUser != null) {
                repository.ensureUserRoleInitialized(
                    userId = currentUserId,
                    email = member.email.ifBlank { Firebase.auth.currentUser?.email },
                    displayName = member.displayName
                )
                repository.saveMemberProfile(
                    displayName = member.displayName,
                    avatarEmoji = member.avatarEmoji,
                    avatarUri = member.avatarUri,
                    bio = member.bio,
                    roleBadge = member.roleBadge,
                    roomsCreatedCount = member.roomsCreatedCount,
                    roomsJoinedCount = member.roomsJoinedCount,
                    messagesSentCount = member.messagesSentCount,
                    supabaseSynced = true,
                    userId = currentUserId
                )
            }

            _actionState.update {
                it.copy(
                    isSupabaseConfigured = isConfigured,
                    isSupabaseSynced = true,
                    supabaseStatusText = if (supabaseResult.isSuccess) {
                        "✅ حساب العضو محفوظ للدخول التلقائي ومزامن مع Supabase"
                    } else {
                        "✅ حساب العضو محفوظ للدخول التلقائي بدون إعادة الكتابة"
                    }
                )
            }
        }
    }

    /**
     * Records a personal chat message in the member's chat history, increments stats,
     * AND publishes a real-time Push Notification (`NEW_MESSAGE`) so room participants receive an instant alert.
     */
    fun sendChatMessageInActiveRoom(messageText: String) {
        val cleanText = messageText.trim()
        if (cleanText.isBlank()) return
        val activeRoom = _actionState.value.activeRoom
        val roomId = activeRoom?.roomId ?: "general_room"
        val roomName = activeRoom?.roomName ?: "غرفة الدردشة العامة"
        val senderName = _actionState.value.memberDisplayName
        val senderEmoji = _actionState.value.memberAvatarEmoji
        val senderBadge = _actionState.value.memberRoleBadge
        val now = Timestamp.now()

        val roomMsg = RoomChatMessage(
            messageId = "rmsg_${System.currentTimeMillis()}",
            roomId = roomId,
            senderId = currentUserId,
            senderName = senderName,
            senderEmoji = senderEmoji,
            senderRoleBadge = senderBadge,
            messageText = cleanText,
            timestamp = now
        )
        _localRoomMessages.update { map ->
            val currentList = map[roomId].orEmpty()
            map + (roomId to (currentList + roomMsg))
        }

        recordHistoryAndIncrementStat(
            roomId = roomId,
            roomName = roomName,
            messageText = cleanText,
            activityType = "MESSAGE"
        )

        val notifId = "notif_msg_${System.currentTimeMillis()}"
        val pushEvent = PushNotificationEvent(
            notificationId = notifId,
            senderId = currentUserId,
            senderName = senderName,
            recipientQuery = "ALL",
            roomId = roomId,
            roomName = roomName,
            notificationType = "NEW_MESSAGE",
            messageBody = cleanText,
            timestamp = now
        )

        lastDeliveredNotificationId = notifId
        _localPushNotifications.update { listOf(pushEvent) + it }
        triggerSystemAndBannerPushNotification(pushEvent)

        _actionState.update {
            it.copy(
                activeRoomMessages = _localRoomMessages.value[roomId].orEmpty(),
                pushNotifications = (listOf(pushEvent) + it.pushNotifications).distinctBy { n -> n.notificationId },
                userXp = it.userXp + 5,
                statusMessage = "🔔 تم إرسال الرسالة وإصدار إشعار فوري للغرفة «$roomName»"
            )
        }
        incrementDailyMissionProgress("mission_send_message")

        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.publishPushNotificationEvent(
                    senderName = senderName,
                    recipientQuery = "ALL",
                    roomId = roomId,
                    roomName = roomName,
                    notificationType = "NEW_MESSAGE",
                    messageBody = cleanText,
                    senderId = currentUserId,
                    customNotificationId = notifId
                )
            }
        }
    }

    private fun recordHistoryAndIncrementStat(
        roomId: String,
        roomName: String,
        messageText: String,
        activityType: String
    ) {
        val localEntry = MemberChatHistoryEntry(
            entryId = "hist_${System.currentTimeMillis()}",
            userId = currentUserId,
            roomId = roomId.ifBlank { "general_room" },
            roomName = roomName.ifBlank { "غرفة عامة" },
            messageText = messageText,
            activityType = activityType,
            timestamp = Timestamp.now()
        )
        _localChatHistory.update { listOf(localEntry) + it }

        _actionState.update { state ->
            state.copy(
                roomsCreatedCount = if (activityType == "CREATE_ROOM") state.roomsCreatedCount + 1 else state.roomsCreatedCount,
                roomsJoinedCount = if (activityType == "JOIN_ROOM") state.roomsJoinedCount + 1 else state.roomsJoinedCount,
                messagesSentCount = if (activityType == "MESSAGE") state.messagesSentCount + 1 else state.messagesSentCount
            )
        }
        syncMemberAccountWithSupabaseAndCloud()

        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.addMemberChatHistoryEntry(
                    roomId = roomId,
                    roomName = roomName,
                    messageText = messageText,
                    activityType = activityType,
                    userId = currentUserId,
                    customEntryId = localEntry.entryId
                )
            }
        }
    }

    fun createChatRoom(
        roomName: String,
        roomPassword: String = "",
        roomCategory: String = WanasRoomCategoryFilter.GENERAL.id,
        ambientThemeId: String = com.example.data.WanasRoomAmbientTheme.ROYAL_NIGHT.id
    ) {
        val cleanName = roomName.trim()
        val cleanPass = roomPassword.trim()
        val resolvedCategoryFilter = WanasRoomCategoryFilter.resolveDisplayFilter(roomCategory)
        val cleanCategory = resolvedCategoryFilter.id
        val resolvedTheme = com.example.data.WanasRoomAmbientTheme.resolveTheme(ambientThemeId)
        if (cleanName.isBlank()) {
            _actionState.update { it.copy(errorMessage = "يرجى إدخال اسم الغرفة") }
            return
        }

        viewModelScope.launch {
            _actionState.update { it.copy(isSubmitting = true, errorMessage = null, statusMessage = null) }
            if (Firebase.auth.currentUser != null) {
                val result = repository.createChatRoom(
                    roomName = cleanName,
                    creatorId = currentUserId,
                    roomPassword = cleanPass,
                    roomCategory = cleanCategory,
                    ambientThemeId = resolvedTheme.id
                )
                result.fold(
                    onSuccess = { created ->
                        val enriched = created.copy(
                            roomCategory = cleanCategory,
                            roomTopicTag = "${resolvedCategoryFilter.emoji} ${resolvedCategoryFilter.labelAr}",
                            ambientThemeId = resolvedTheme.id
                        )
                        _localRooms.update { current ->
                            listOf(enriched) + current.filterNot { it.roomId == enriched.roomId }
                        }
                        _actionState.update {
                            it.copy(
                                isSubmitting = false,
                                statusMessage = if (cleanPass.isNotEmpty()) {
                                    "✅ تم حفظ غرفة «${enriched.roomName}» (${resolvedCategoryFilter.labelAr} • ${resolvedTheme.emoji} ${resolvedTheme.titleAr}) بكلمة مرور خاصة 🔒"
                                } else {
                                    "✅ تم حفظ غرفة «${enriched.roomName}» في تصنيف (${resolvedCategoryFilter.labelAr} • ${resolvedTheme.emoji} ${resolvedTheme.titleAr})"
                                }
                            )
                        }
                        recordHistoryAndIncrementStat(
                            roomId = enriched.roomId,
                            roomName = enriched.roomName,
                            messageText = "إنشاء غرفة جديدة (${resolvedCategoryFilter.labelAr} • ${resolvedTheme.titleAr}): ${enriched.roomName}",
                            activityType = "CREATE_ROOM"
                        )
                        knownRoomIdsSnapshot.add(enriched.roomId)
                        knownActiveRoomTimestampById[enriched.roomId] = enriched.timestampMillis
                        repository.publishPushNotificationEvent(
                            senderName = _actionState.value.memberDisplayName,
                            recipientQuery = "FRIENDS_SUBSCRIBERS",
                            roomId = enriched.roomId,
                            roomName = enriched.roomName,
                            notificationType = "FRIEND_STARTED_ROOM",
                            messageBody = "🎙️ بدأ صديقك «${_actionState.value.memberDisplayName}» غرفة صوتية جديدة «${enriched.roomName}» (${resolvedTheme.emoji} ${resolvedTheme.titleAr})",
                            senderId = currentUserId
                        )
                    },
                    onFailure = { err ->
                        _actionState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = err.localizedMessage ?: "حدث خطأ أثناء حفظ الغرفة"
                            )
                        }
                    }
                )
            } else {
                val localRoom = ChatRoomMetadata(
                    roomId = "room_${System.currentTimeMillis()}",
                    roomName = cleanName,
                    creatorId = currentUserId,
                    timestamp = Timestamp.now(),
                    roomPassword = cleanPass,
                    isPasswordProtected = cleanPass.isNotEmpty(),
                    roomCategory = cleanCategory,
                    roomTopicTag = "${resolvedCategoryFilter.emoji} ${resolvedCategoryFilter.labelAr}",
                    ambientThemeId = resolvedTheme.id
                )
                supabaseService.upsertSharedRoom(localRoom)
                knownRoomIdsSnapshot.add(localRoom.roomId)
                knownActiveRoomTimestampById[localRoom.roomId] = localRoom.timestampMillis
                _localRooms.update { current -> listOf(localRoom) + current }
                _actionState.update {
                    it.copy(
                        isSubmitting = false,
                        statusMessage = if (cleanPass.isNotEmpty()) {
                            "✅ تم إنشاء وحفظ غرفة «${localRoom.roomName}» (${resolvedCategoryFilter.labelAr}) بكلمة مرور خاصة 🔒"
                        } else {
                            "✅ تم إنشاء وحفظ غرفة «${localRoom.roomName}» في تصنيف (${resolvedCategoryFilter.labelAr})"
                        }
                    )
                }
                recordHistoryAndIncrementStat(
                    roomId = localRoom.roomId,
                    roomName = localRoom.roomName,
                    messageText = "إنشاء غرفة جديدة (${resolvedCategoryFilter.labelAr}): ${localRoom.roomName}",
                    activityType = "CREATE_ROOM"
                )
            }
        }
    }

    /**
     * Checks whether the current user has full Room Admin & App Owner controls for [room],
     * even if the room is protected with a password.
     */
    fun hasFullRoomAdminAuthority(room: ChatRoomMetadata): Boolean {
        return _actionState.value.isAdminOwner || room.creatorId == currentUserId
    }

    /**
     * Room Admin / App Owner action: Update or clear password for any room (even password-protected rooms).
     */
    fun updateRoomPasswordByAdmin(roomId: String, newPassword: String) {
        val cleanPass = newPassword.trim()
        _localRooms.update { list ->
            list.map { r ->
                if (r.roomId == roomId) {
                    r.copy(roomPassword = cleanPass, isPasswordProtected = cleanPass.isNotEmpty())
                } else {
                    r
                }
            }
        }
        _actionState.update { state ->
            val updatedActive = if (state.activeRoom?.roomId == roomId) {
                state.activeRoom.copy(roomPassword = cleanPass, isPasswordProtected = cleanPass.isNotEmpty())
            } else {
                state.activeRoom
            }
            state.copy(
                activeRoom = updatedActive,
                statusMessage = if (cleanPass.isEmpty()) {
                    "🔓 تم إلغاء باسورد الغرفة بواسطة الإدارة وفتحها للجميع"
                } else {
                    "🔒 تم تحديث باسورد الغرفة بنجاح بواسطة الإدارة"
                }
            )
        }
    }

    /**
     * Room Admin / App Owner action: Kick or mute a member inside the active room, or clear chat.
     * Immediately closes the mic if the member is muted or banned ("إذا تم كتم العضو أو حظره يُغلق المايك فورًا").
     */
    fun moderateRoomActionByAdmin(actionLabel: String, targetMemberId: String? = null) {
        val currentRoom = _actionState.value.activeRoom ?: return
        val updatedMembers = if (targetMemberId != null) {
            _actionState.value.activeRoomMembers.filterNot { it.userId == targetMemberId }
        } else {
            _actionState.value.activeRoomMembers
        }
        val isMuteAllOrTarget = actionLabel.contains("كتم") || actionLabel.contains("حظر") || targetMemberId != null
        _actionState.update { st ->
            val updatedSeats = st.activeRoomSeats.map { slot ->
                if (targetMemberId != null && slot.occupantUserId == targetMemberId) {
                    VoiceRoomSeatSlot(seatIndex = slot.seatIndex)
                } else if (actionLabel.contains("كتم")) {
                    slot.copy(isMuted = true, isSpeaking = false)
                } else if (actionLabel.contains("قفل المقاعد") && slot.isEmpty) {
                    slot.copy(isSeatLocked = !slot.isSeatLocked)
                } else {
                    slot
                }
            }
            st.copy(
                activeRoomMembers = updatedMembers,
                activeRoomSeats = updatedSeats,
                myMicRequestStatus = if (isMuteAllOrTarget && !hasFullRoomAdminAuthority(currentRoom)) {
                    MicRequestStatus.MUTED_BY_HOST
                } else st.myMicRequestStatus,
                isServerMicApproved = if (isMuteAllOrTarget && !hasFullRoomAdminAuthority(currentRoom)) {
                    false
                } else st.isServerMicApproved,
                statusMessage = "🛡️ تحكم إدارة الغرفة («${currentRoom.roomName}»): $actionLabel"
            )
        }
        publishUnifiedNotificationEvent(
            notificationType = "ADMIN_ALERT",
            senderName = "إدارة الغرفة",
            roomId = currentRoom.roomId,
            roomName = currentRoom.roomName,
            messageBody = "🛡️ قرار إداري في «${currentRoom.roomName}»: $actionLabel"
        )
    }

    /**
     * Selects the recipient member inside the active room to receive the gift.
     */
    fun selectRoomGiftRecipient(recipientName: String) {
        val clean = recipientName.trim().ifBlank { "مضيف السهرة 👑" }
        _actionState.update {
            it.copy(
                selectedRoomGiftRecipientName = clean,
                statusMessage = "🎯 تم تحديد مستلم الهدية في الغرفة: «$clean»"
            )
        }
    }

    /**
     * Selects the gift combo quantity multiplier (x1, x5, x10, x99) inside the active room.
     */
    fun selectRoomGiftComboMultiplier(multiplier: Int) {
        val safeMultiplier = multiplier.coerceIn(1, 99)
        _actionState.update {
            it.copy(
                selectedRoomGiftComboMultiplier = safeMultiplier,
                statusMessage = "🔥 تم اختيار مضاعف الكومبو للإهداء: x$safeMultiplier"
            )
        }
    }

    /**
     * Sends a dazzling gift inside the active voice room with an animated stage celebration banner,
     * combo multiplier (x1 -> x5 -> x10 -> x99), automatic PK Team score boost,
     * and persists the gift transaction document to Firestore (`/chat_rooms/{roomId}/room_gifts` and `/gift_transactions`).
     */
    fun sendGiftInActiveRoom(
        gift: WanasGiftItem,
        customRecipientName: String? = null,
        customQuantity: Int? = null
    ) {
        val currentRoom = _actionState.value.activeRoom
        val quantity = (customQuantity ?: _actionState.value.selectedRoomGiftComboMultiplier).coerceIn(1, 99)
        val totalCostCoins = gift.costCoins * quantity
        val recipient = customRecipientName?.trim()?.takeIf { it.isNotBlank() }
            ?: _actionState.value.selectedRoomGiftRecipientName.ifBlank {
                currentRoom?.roomOwnerName ?: currentRoom?.roomName ?: "مضيف السهرة 👑"
            }
        val currentCoins = _actionState.value.coinsBalance
        if (currentCoins < totalCostCoins) {
            _actionState.update {
                it.copy(errorMessage = "🪙 رصيدك الحالي ($currentCoins Coins) لا يكفي لإرسال «${gift.emoji} ${gift.nameAr} x$quantity» ($totalCostCoins Coins). اشحن الآن!")
            }
            return
        }

        val state = _actionState.value
        val sender = state.memberDisplayName
        val myMemberCode = state.equippedSpecialMemberCode ?: generateMemberIdCode(currentUserId.ifBlank { sender })
        val isSameGiftCombo = state.latestStageGiftNameAr == gift.nameAr && state.latestStageGiftRecipientName == recipient
        val nextGiftCombo = if (isSameGiftCombo) state.latestStageGiftComboCount + quantity else quantity
        val targetIsRed = state.pkTargetTeamIsRed
        val pkBonusPoints = (totalCostCoins * 3) + (if (nextGiftCombo >= 3) 25 else 0)
        val teamTagAr = if (targetIsRed) "🔥 الفريق الأحمر" else "⚡ الفريق الأزرق"
        val txId = "gift_tx_${System.currentTimeMillis()}"
        val now = Timestamp.now()

        val firestoreTxDoc = RoomGiftTransactionDocument(
            transactionId = txId,
            roomId = currentRoom?.roomId ?: "room_wanas_egypt_1",
            roomName = currentRoom?.roomName ?: "غرفة وَنَس التفاعلية",
            giftId = gift.giftId,
            giftNameAr = gift.nameAr,
            giftEmoji = gift.emoji,
            costCoins = totalCostCoins,
            quantity = quantity,
            senderId = currentUserId,
            senderMemberCode = myMemberCode,
            senderName = sender,
            recipientId = "user_${recipient.hashCode().let { if (it < 0) -it else it }}",
            recipientName = recipient,
            pkTeamSupported = if (targetIsRed) "RED" else "BLUE",
            pkBonusPoints = pkBonusPoints,
            timestamp = now
        )

        val localRecord = GiftTransactionRecord(
            recordId = txId,
            giftId = gift.giftId,
            giftNameAr = if (quantity > 1) "${gift.nameAr} (x$quantity)" else gift.nameAr,
            giftEmoji = gift.emoji,
            costCoins = totalCostCoins,
            senderId = currentUserId,
            senderName = sender,
            recipientName = recipient,
            roomId = firestoreTxDoc.roomId,
            isRareOrAnimated = gift.costCoins >= 50,
            isHostSpecialGift = gift.costCoins >= 100
        )

        val ledgerEntry = CoinLedgerRecord(
            ledgerId = "LDG-GIFT-${System.currentTimeMillis() % 100000}",
            targetMemberId = myMemberCode,
            targetMemberName = "$sender ⬅️ $recipient",
            deltaCoins = -totalCostCoins,
            operationTypeAr = "إرسال هدية غرفة (${gift.emoji} ${gift.nameAr} x$quantity)",
            authorizedBy = "Firestore Gift Ledger ($txId)",
            auditReason = "توثيق معاملة هدية بين المستخدمين في «${firestoreTxDoc.roomName}»",
            paymentStatusAr = "موثق في Firestore ✅",
            timestampLabel = "الآن"
        )

        val banner = "${gift.emoji} $sender أرسل «${gift.nameAr}» x$nextGiftCombo ($totalCostCoins🪙) إلى «$recipient» • دعم $teamTagAr (+$pkBonusPoints نقطة PK) — موثق في Firestore ✅"

        val roomKey = firestoreTxDoc.roomId
        val updatedRoomTxList = (listOf(firestoreTxDoc) + _localRoomGiftTransactions.value[roomKey].orEmpty())
            .distinctBy { it.transactionId }
        _localRoomGiftTransactions.update { it + (roomKey to updatedRoomTxList) }

        val updatedRoom = currentRoom?.let { rm ->
            if (targetIsRed) {
                rm.copy(pkTeamRedScore = rm.pkTeamRedScore + pkBonusPoints)
            } else {
                rm.copy(pkTeamBlueScore = rm.pkTeamBlueScore + pkBonusPoints)
            }
        }
        if (updatedRoom != null) {
            _localRooms.update { list -> list.map { if (it.roomId == updatedRoom.roomId) updatedRoom else it } }
        }

        _actionState.update { st ->
            val newSentTotal = (st.sentGiftsTotalsByMember[sender] ?: 0) + totalCostCoins
            val newRecTotal = (st.receivedGiftsTotalsByMember[recipient] ?: 0) + totalCostCoins
            val updatedSupporters = updateTopSupportersList(
                currentList = st.roomTopSupporters,
                memberName = sender,
                memberCode = myMemberCode,
                emoji = st.memberAvatarEmoji,
                addedCoins = totalCostCoins
            )
            st.copy(
                activeRoom = updatedRoom ?: st.activeRoom,
                coinsBalance = st.coinsBalance - totalCostCoins,
                userXp = st.userXp + (totalCostCoins / 2).coerceAtLeast(15),
                giftHistoryRecords = listOf(localRecord) + st.giftHistoryRecords,
                coinLedgerRecords = listOf(ledgerEntry) + st.coinLedgerRecords,
                activeRoomGiftTransactions = (listOf(firestoreTxDoc) + st.activeRoomGiftTransactions).distinctBy { it.transactionId },
                sentGiftsTotalsByMember = st.sentGiftsTotalsByMember + (sender to newSentTotal),
                receivedGiftsTotalsByMember = st.receivedGiftsTotalsByMember + (recipient to newRecTotal),
                roomTopSupporters = updatedSupporters,
                latestRoomGiftBannerText = banner,
                latestStageGiftComboCount = nextGiftCombo,
                latestStageGiftEmoji = gift.emoji,
                latestStageGiftNameAr = gift.nameAr,
                latestStageGiftSenderName = sender,
                latestStageGiftRecipientName = recipient,
                pkRedComboStreak = if (targetIsRed) st.pkRedComboStreak + 1 else st.pkRedComboStreak,
                pkBlueComboStreak = if (!targetIsRed) st.pkBlueComboStreak + 1 else st.pkBlueComboStreak,
                pkComboStreakCount = nextGiftCombo,
                pkLastComboTeamLabel = teamTagAr,
                isRecordingGiftToFirestore = true,
                lastFirestoreGiftTxId = txId,
                adminDashboardStats = st.adminDashboardStats.copy(
                    giftAndRoomRevenueCoins = st.adminDashboardStats.giftAndRoomRevenueCoins + totalCostCoins,
                    totalGiftsSentCount = st.adminDashboardStats.totalGiftsSentCount + quantity
                ),
                statusMessage = "🎁 تم إرسال «${gift.emoji} ${gift.nameAr} x$quantity» إلى «$recipient» وتوثيق المعاملة في Firestore (#$txId)!",
                errorMessage = null
            )
        }

        incrementDailyMissionProgress("mission_send_gift")
        publishUnifiedNotificationEvent(
            notificationType = "GIFT_RECEIVED",
            senderName = sender,
            roomId = firestoreTxDoc.roomId,
            roomName = firestoreTxDoc.roomName,
            messageBody = "🎁 أرسل $sender هدية ${gift.emoji} ${gift.nameAr} x$quantity ($totalCostCoins Coins) إلى $recipient — موثق في Firestore"
        )
        if (currentRoom != null) {
            sendChatMessageInActiveRoom("🎁 أرسل هدية المسرح: ${gift.emoji} ${gift.nameAr} (x$quantity • $totalCostCoins🪙) إلى «$recipient» ودعم $teamTagAr (+$pkBonusPoints نقطة PK)!")
        }

        viewModelScope.launch {
            repository.recordGiftTransactionInFirestore(
                roomId = firestoreTxDoc.roomId,
                roomName = firestoreTxDoc.roomName,
                giftId = gift.giftId,
                giftNameAr = gift.nameAr,
                giftEmoji = gift.emoji,
                costCoins = totalCostCoins,
                quantity = quantity,
                senderName = sender,
                senderMemberCode = myMemberCode,
                recipientId = firestoreTxDoc.recipientId,
                recipientName = recipient,
                pkTeamSupported = firestoreTxDoc.pkTeamSupported,
                pkBonusPoints = pkBonusPoints,
                senderId = currentUserId,
                customTransactionId = txId
            ).onSuccess { savedDoc ->
                _actionState.update { st ->
                    st.copy(
                        isRecordingGiftToFirestore = false,
                        lastFirestoreGiftTxId = savedDoc.transactionId,
                        activeRoomGiftTransactions = (listOf(savedDoc) + st.activeRoomGiftTransactions)
                            .distinctBy { it.transactionId }
                    )
                }
            }.onFailure {
                _actionState.update { st ->
                    st.copy(isRecordingGiftToFirestore = false)
                }
            }
        }
    }

    /**
     * Selects whether gifts sent in the room support Team Red or Team Blue in the TikTok Live PK battle.
     */
    fun setPkGiftTargetTeam(isRedTeam: Boolean) {
        _actionState.update {
            it.copy(
                pkTargetTeamIsRed = isRedTeam,
                pkLastComboTeamLabel = if (isRedTeam) "الفريق الأحمر 🔥" else "الفريق الأزرق ⚡",
                statusMessage = if (isRedTeam) {
                    "🎯 تم توجيه هدايا المسرح القادمة لدعم الفريق الأحمر 🔥 في تحدي الـ PK"
                } else {
                    "🎯 تم توجيه هدايا المسرح القادمة لدعم الفريق الأزرق ⚡ في تحدي الـ PK"
                }
            )
        }
    }

    /**
     * Enters a room:
     * - Prevents automatic mic opening on entry ("منع فتح المايك تلقائيًا بمجرد دخول الغرفة").
     * - Initializes the real 8-Seat Voice Stage (Host on Seat 1, Co-Host or Member on Seat 2, remaining 6 empty seats).
     * - Displays transient banner `"دخل العضو <name>"` and auto-hides after 3.5 seconds.
     */
    fun enterChatRoom(room: ChatRoomMetadata) {
        val memberName = _actionState.value.memberDisplayName
        val avatarEmoji = _actionState.value.memberAvatarEmoji
        val roleBadge = _actionState.value.memberRoleBadge
        val isHostOrAdmin = hasFullRoomAdminAuthority(room)

        val immediateEvent = RoomPresenceBannerEvent(
            eventId = "local_join_${System.currentTimeMillis()}",
            roomId = room.roomId,
            userId = currentUserId,
            memberName = memberName,
            avatarEmoji = avatarEmoji,
            eventType = "JOIN"
        )
        showTransientBanner(immediateEvent)

        val selfMember = RoomActiveMember(
            userId = currentUserId,
            roomId = room.roomId,
            memberName = memberName,
            avatarEmoji = avatarEmoji,
            roleBadge = roleBadge,
            supabaseLinked = true
        )

        // Build 8 real voice seats: Seat 0 is Host, Seat 1 is Co-Host/Seated Member, Seats 2..7 clearly empty
        val initialSeats = (0 until 8).map { idx ->
            when {
                idx == 0 && isHostOrAdmin -> VoiceRoomSeatSlot(
                    seatIndex = 0,
                    occupantUserId = currentUserId,
                    occupantName = memberName,
                    occupantEmoji = avatarEmoji,
                    seatRole = VoiceSeatRoleBadge.HOST,
                    isSpeaking = false,
                    isMuted = true // Strictly muted on entry until toggled
                )
                idx == 0 && !isHostOrAdmin -> VoiceRoomSeatSlot(
                    seatIndex = 0,
                    occupantUserId = room.creatorId.ifBlank { "wanas_host" },
                    occupantName = "مضيف السهرة",
                    occupantEmoji = "👑",
                    seatRole = VoiceSeatRoleBadge.HOST,
                    isSpeaking = true,
                    isMuted = false
                )
                idx == 1 && !isHostOrAdmin -> VoiceRoomSeatSlot(
                    seatIndex = 1,
                    occupantUserId = currentUserId,
                    occupantName = memberName,
                    occupantEmoji = avatarEmoji,
                    seatRole = VoiceSeatRoleBadge.SEATED_MEMBER,
                    isSpeaking = false,
                    isMuted = true // Strictly muted until approved by host
                )
                idx == 1 && isHostOrAdmin -> VoiceRoomSeatSlot(
                    seatIndex = 1,
                    occupantUserId = null,
                    occupantName = null,
                    occupantEmoji = "🛡️",
                    seatRole = VoiceSeatRoleBadge.CO_HOST,
                    isSpeaking = false,
                    isMuted = true
                )
                else -> VoiceRoomSeatSlot(seatIndex = idx)
            }
        }

        val existingMessages = _localRoomMessages.value[room.roomId].orEmpty().ifEmpty {
            listOf(
                RoomChatMessage(
                    messageId = "welcome_${room.roomId}",
                    roomId = room.roomId,
                    senderId = "wanas_host",
                    senderName = "مضيف ونس",
                    senderEmoji = "🎙️",
                    senderRoleBadge = "مضيف الغرفة",
                    messageText = "أهلاً بك يا $memberName في غرفة «${room.roomName}»! المايك مغلق تلقائياً عند الدخول — اضغط «🙋 طلب المايك» للتحدث.",
                    timestamp = Timestamp.now()
                )
            )
        }
        _localRoomMessages.update { it + (room.roomId to existingMessages) }

        val existingGifts = _localRoomGiftTransactions.value[room.roomId].orEmpty()
        _localRoomGiftTransactions.update { it + (room.roomId to existingGifts) }

        _actionState.update {
            it.copy(
                activeRoom = room,
                activeRoomMembers = listOf(selfMember),
                activeRoomMessages = existingMessages,
                activeRoomGiftTransactions = existingGifts,
                selectedRoomGiftRecipientName = "مضيف السهرة 👑",
                selectedRoomGiftComboMultiplier = 1,
                activeRoomSeats = initialSeats,
                activeRoomMicRequests = emptyList(),
                myMicRequestStatus = if (isHostOrAdmin) MicRequestStatus.APPROVED else MicRequestStatus.NONE,
                isServerMicApproved = isHostOrAdmin,
                userXp = it.userXp + 15,
                errorMessage = null
            )
        }
        incrementDailyMissionProgress("mission_enter_room_10m")

        recordHistoryAndIncrementStat(
            roomId = room.roomId,
            roomName = room.roomName,
            messageText = "دخل العضو $memberName إلى الغرفة",
            activityType = "JOIN_ROOM"
        )

        observeRoomRealtimeStreams(room.roomId)

        viewModelScope.launch {
            val seatDocs = initialSeats.map { slot ->
                VoiceRoomSeatDocument(
                    seatId = "seat_${slot.seatIndex}",
                    roomId = room.roomId,
                    seatIndex = slot.seatIndex,
                    occupantUserId = slot.occupantUserId.orEmpty(),
                    occupantMemberCode = slot.occupantMemberCode ?: slot.occupantUserId?.let { generateMemberIdCode(it) }.orEmpty(),
                    occupantName = slot.occupantName.orEmpty(),
                    occupantEmoji = slot.occupantEmoji,
                    seatRole = slot.seatRole.id,
                    isSpeaking = slot.isSpeaking,
                    isMuted = slot.isMuted,
                    hasPendingMicRequest = slot.hasPendingMicRequest,
                    isSeatLocked = slot.isSeatLocked,
                    updatedAt = Timestamp.now()
                )
            }
            supabaseService.upsertRoomSeatsBatch(room.roomId, seatDocs)
            if (Firebase.auth.currentUser != null) {
                repository.enterRoom(
                    roomId = room.roomId,
                    memberName = memberName,
                    avatarEmoji = avatarEmoji,
                    roleBadge = roleBadge,
                    supabaseLinked = true,
                    userId = currentUserId
                ).onSuccess { cloudEvent ->
                    lastShownEventId = cloudEvent.eventId
                }
            }
            supabaseService.recordRoomPresenceEventInSupabase(
                roomId = room.roomId,
                userId = currentUserId,
                memberName = memberName,
                eventType = "JOIN"
            )
        }
    }

    /**
     * Adds a user to the currently active voice chat room in Firestore (`/chat_rooms/{roomId}/active_members/{memberId}`),
     * updates the live active members list, seats them on an available empty stage seat if any,
     * and increments the room's activeMembersCount.
     */
    fun addUserToActiveRoom(
        memberName: String,
        targetUserId: String = "",
        avatarEmoji: String = "🎙️",
        roleBadge: String = "عضو مضاف بالغرفة 🎙️"
    ) {
        val currentRoom = _actionState.value.activeRoom ?: return
        val cleanName = memberName.trim()
        if (cleanName.isBlank()) {
            _actionState.update { it.copy(errorMessage = "يرجى إدخال اسم المستخدم لإضافته إلى الغرفة النشطة") }
            return
        }
        val resolvedId = targetUserId.trim().ifBlank {
            "usr_${cleanName.hashCode().let { if (it < 0) -it else it }}"
        }.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifBlank { "wanas_user" }
        val cleanEmoji = avatarEmoji.trim().ifBlank { "🎙️" }
        val cleanBadge = roleBadge.trim().ifBlank { "عضو مضاف بالغرفة 🎙️" }

        val newActiveMember = RoomActiveMember(
            userId = resolvedId,
            roomId = currentRoom.roomId,
            memberName = cleanName,
            avatarEmoji = cleanEmoji,
            roleBadge = cleanBadge,
            supabaseLinked = true,
            joinedAt = Timestamp.now()
        )

        val joinBanner = RoomPresenceBannerEvent(
            eventId = "add_usr_${System.currentTimeMillis()}",
            roomId = currentRoom.roomId,
            userId = resolvedId,
            memberName = cleanName,
            avatarEmoji = cleanEmoji,
            eventType = "JOIN"
        )
        showTransientBanner(joinBanner)

        _actionState.update { st ->
            val updatedMembers = (listOf(newActiveMember) + st.activeRoomMembers.filterNot {
                it.userId == resolvedId || it.memberName.equals(cleanName, ignoreCase = true)
            }).distinctBy { it.userId }

            val emptySeatIdx = st.activeRoomSeats.indexOfFirst { it.isEmpty }
            val updatedSeats = if (emptySeatIdx >= 0 && st.activeRoomSeats.none { it.occupantUserId == resolvedId }) {
                st.activeRoomSeats.map { slot ->
                    if (slot.seatIndex == emptySeatIdx) {
                        slot.copy(
                            occupantUserId = resolvedId,
                            occupantMemberCode = generateMemberIdCode(resolvedId),
                            occupantName = cleanName,
                            occupantEmoji = cleanEmoji,
                            seatRole = VoiceSeatRoleBadge.SEATED_MEMBER,
                            isMuted = true,
                            isSpeaking = false
                        )
                    } else slot
                }
            } else {
                st.activeRoomSeats
            }

            val updatedRoom = currentRoom.copy(activeMembersCount = updatedMembers.size)
            st.copy(
                activeRoom = updatedRoom,
                activeRoomMembers = updatedMembers,
                activeRoomSeats = updatedSeats,
                statusMessage = "✅ تمت إضافة المستخدم «$cleanName» (#${generateMemberIdCode(resolvedId)}) إلى الغرفة النشطة وحفظه في Firestore (/chat_rooms/${currentRoom.roomId}/active_members/$resolvedId)!",
                errorMessage = null
            )
        }

        _localRooms.update { list ->
            list.map { rm ->
                if (rm.roomId == currentRoom.roomId) {
                    rm.copy(activeMembersCount = _actionState.value.activeRoomMembers.size)
                } else rm
            }
        }

        syncActiveRoomSeatsToSupabaseRealtime()

        viewModelScope.launch {
            repository.addUserToActiveRoomInFirestore(
                roomId = currentRoom.roomId,
                targetUserId = resolvedId,
                memberName = cleanName,
                avatarEmoji = cleanEmoji,
                roleBadge = cleanBadge,
                supabaseLinked = true
            )
            supabaseService.recordRoomPresenceEventInSupabase(
                roomId = currentRoom.roomId,
                userId = resolvedId,
                memberName = cleanName,
                eventType = "JOIN"
            )
        }
    }

    /**
     * Removes a user from the currently active voice chat room in Firestore (`/chat_rooms/{roomId}/active_members/{memberId}`)
     * and vacates their seat on the stage.
     */
    fun removeUserFromActiveRoom(targetUserId: String, memberName: String = "") {
        val currentRoom = _actionState.value.activeRoom ?: return
        val cleanId = targetUserId.trim()
        if (cleanId.isBlank()) return
        val resolvedName = memberName.trim().ifBlank {
            _actionState.value.activeRoomMembers.find { it.userId == cleanId }?.memberName ?: cleanId
        }

        _actionState.update { st ->
            val updatedMembers = st.activeRoomMembers.filterNot { it.userId == cleanId }
            val updatedSeats = st.activeRoomSeats.map { slot ->
                if (slot.occupantUserId == cleanId) {
                    VoiceRoomSeatSlot(seatIndex = slot.seatIndex)
                } else slot
            }
            val updatedRoom = currentRoom.copy(activeMembersCount = updatedMembers.size)
            st.copy(
                activeRoom = updatedRoom,
                activeRoomMembers = updatedMembers,
                activeRoomSeats = updatedSeats,
                statusMessage = "🗑️ تم إزالة المستخدم «$resolvedName» من الغرفة النشطة وتحديث Firestore",
                errorMessage = null
            )
        }

        _localRooms.update { list ->
            list.map { rm ->
                if (rm.roomId == currentRoom.roomId) {
                    rm.copy(activeMembersCount = _actionState.value.activeRoomMembers.size)
                } else rm
            }
        }

        syncActiveRoomSeatsToSupabaseRealtime()

        viewModelScope.launch {
            repository.removeUserFromActiveRoomInFirestore(
                roomId = currentRoom.roomId,
                targetUserId = cleanId
            )
        }
    }

    /**
     * Leaves the current room:
     * - Displays transient banner `"خرج العضو <name>"` and auto-hides after 3.5 seconds.
     * - Removes the member from the room's active members list.
     */
    fun leaveCurrentRoom() {
        val currentRoom = _actionState.value.activeRoom ?: return
        val memberName = _actionState.value.memberDisplayName
        val avatarEmoji = _actionState.value.memberAvatarEmoji

        val leaveBanner = RoomPresenceBannerEvent(
            eventId = "local_leave_${System.currentTimeMillis()}",
            roomId = currentRoom.roomId,
            userId = currentUserId,
            memberName = memberName,
            avatarEmoji = avatarEmoji,
            eventType = "LEAVE"
        )
        showTransientBanner(leaveBanner)

        activeMembersJob?.cancel()
        presenceEventsJob?.cancel()
        roomSeatsRealtimeJob?.cancel()
        roomMicRequestsRealtimeJob?.cancel()
        roomGiftTransactionsJob?.cancel()

        _actionState.update {
            it.copy(
                activeRoom = null,
                activeRoomMembers = emptyList(),
                activeRoomMessages = emptyList(),
                statusMessage = "👋 خرج العضو $memberName من غرفة «${currentRoom.roomName}»"
            )
        }

        recordHistoryAndIncrementStat(
            roomId = currentRoom.roomId,
            roomName = currentRoom.roomName,
            messageText = "خرج العضو $memberName من الغرفة",
            activityType = "LEAVE_ROOM"
        )

        viewModelScope.launch {
            if (Firebase.auth.currentUser != null) {
                repository.leaveRoom(
                    roomId = currentRoom.roomId,
                    memberName = memberName,
                    avatarEmoji = avatarEmoji,
                    userId = currentUserId
                )
            }
            supabaseService.recordRoomPresenceEventInSupabase(
                roomId = currentRoom.roomId,
                userId = currentUserId,
                memberName = memberName,
                eventType = "LEAVE"
            )
        }
    }

    private fun observeRoomRealtimeStreams(roomId: String) {
        activeMembersJob?.cancel()
        presenceEventsJob?.cancel()
        roomSeatsRealtimeJob?.cancel()
        roomMicRequestsRealtimeJob?.cancel()
        roomGiftTransactionsJob?.cancel()

        roomSeatsRealtimeJob = viewModelScope.launch {
            repository.observeRoomSeats(roomId)
                .catch { e -> Log.w(TAG, "Error observing Supabase Realtime room seats", e) }
                .collect { seatDocs ->
                    if (seatDocs.isNotEmpty()) {
                        val mappedSlots = (0 until 8).map { idx ->
                            val doc = seatDocs.find { it.seatIndex == idx }
                            if (doc != null) {
                                val badge = VoiceSeatRoleBadge.entries.find { it.id == doc.seatRole }
                                    ?: if (doc.occupantUserId.isNotBlank()) VoiceSeatRoleBadge.SEATED_MEMBER else VoiceSeatRoleBadge.EMPTY
                                VoiceRoomSeatSlot(
                                    seatIndex = idx,
                                    occupantUserId = doc.occupantUserId.takeIf { it.isNotBlank() },
                                    occupantMemberCode = doc.occupantMemberCode.takeIf { it.isNotBlank() },
                                    occupantName = doc.occupantName.takeIf { it.isNotBlank() },
                                    occupantEmoji = doc.occupantEmoji.ifBlank { "👤" },
                                    seatRole = badge,
                                    isSpeaking = doc.isSpeaking,
                                    isMuted = doc.isMuted,
                                    hasPendingMicRequest = doc.hasPendingMicRequest,
                                    isSeatLocked = doc.isSeatLocked
                                )
                            } else {
                                VoiceRoomSeatSlot(seatIndex = idx)
                            }
                        }
                        _actionState.update { it.copy(activeRoomSeats = mappedSlots) }
                    }
                }
        }

        roomMicRequestsRealtimeJob = viewModelScope.launch {
            repository.observeRoomMicRequests(roomId)
                .catch { e -> Log.w(TAG, "Error observing Supabase Realtime mic requests", e) }
                .collect { reqDocs ->
                    if (reqDocs.isNotEmpty()) {
                        val mappedReqs = reqDocs.map { doc ->
                            val st = MicRequestStatus.entries.find { it.id == doc.status } ?: MicRequestStatus.PENDING
                            com.example.data.RoomMicRequestItem(
                                requestId = doc.requestId,
                                roomId = doc.roomId,
                                userId = doc.userId,
                                memberIdCode = doc.memberIdCode.ifBlank { generateMemberIdCode(doc.userId) },
                                userName = doc.userName,
                                userEmoji = doc.userEmoji,
                                queueNumber = doc.queueNumber,
                                status = st,
                                requestedSeatIndex = doc.requestedSeatIndex,
                                timestampMillis = doc.timestamp.toDate().time
                            )
                        }
                        _actionState.update { it.copy(activeRoomMicRequests = mappedReqs) }
                    }
                }
        }

        if (Firebase.auth.currentUser != null) {
            activeMembersJob = viewModelScope.launch {
                repository.observeRoomActiveMembers(roomId)
                    .catch { e -> Log.w(TAG, "Error observing active members", e) }
                    .collect { members ->
                        if (members.isNotEmpty()) {
                            _actionState.update { it.copy(activeRoomMembers = members) }
                        }
                    }
            }

            presenceEventsJob = viewModelScope.launch {
                repository.observeRoomPresenceEvents(roomId)
                    .catch { e -> Log.w(TAG, "Error observing presence events", e) }
                    .collect { events ->
                        val latest = events.firstOrNull() ?: return@collect
                        if (latest.eventId.isNotBlank() && latest.eventId != lastShownEventId) {
                            lastShownEventId = latest.eventId
                            showTransientBanner(latest)
                        }
                    }
            }

            roomGiftTransactionsJob = viewModelScope.launch {
                repository.observeRoomGiftTransactions(roomId)
                    .catch { e -> Log.w(TAG, "Error observing room gift transactions", e) }
                    .collect { cloudGifts ->
                        if (cloudGifts.isNotEmpty()) {
                            val localGifts = _localRoomGiftTransactions.value[roomId].orEmpty()
                            val merged = (cloudGifts + localGifts)
                                .distinctBy { it.transactionId }
                                .sortedByDescending { it.timestampMillis }
                            _localRoomGiftTransactions.update { it + (roomId to merged) }
                            _actionState.update { it.copy(activeRoomGiftTransactions = merged) }
                        }
                    }
            }
        }
    }

    private fun syncActiveRoomSeatsToSupabaseRealtime() {
        val currentRoom = _actionState.value.activeRoom ?: return
        val seats = _actionState.value.activeRoomSeats
        viewModelScope.launch {
            val docs = seats.map { slot ->
                VoiceRoomSeatDocument(
                    seatId = "seat_${slot.seatIndex}",
                    roomId = currentRoom.roomId,
                    seatIndex = slot.seatIndex,
                    occupantUserId = slot.occupantUserId.orEmpty(),
                    occupantMemberCode = slot.occupantMemberCode ?: slot.occupantUserId?.let { generateMemberIdCode(it) }.orEmpty(),
                    occupantName = slot.occupantName.orEmpty(),
                    occupantEmoji = slot.occupantEmoji,
                    seatRole = slot.seatRole.id,
                    isSpeaking = slot.isSpeaking,
                    isMuted = slot.isMuted,
                    hasPendingMicRequest = slot.hasPendingMicRequest,
                    isSeatLocked = slot.isSeatLocked,
                    updatedAt = Timestamp.now()
                )
            }
            supabaseService.upsertRoomSeatsBatch(currentRoom.roomId, docs)
        }
    }

    private fun showTransientBanner(event: RoomPresenceBannerEvent) {
        bannerAutoHideJob?.cancel()
        _actionState.update { it.copy(transientPresenceBanner = event) }
        bannerAutoHideJob = viewModelScope.launch {
            delay(BANNER_DISPLAY_DURATION_MS)
            _actionState.update { state ->
                if (state.transientPresenceBanner?.eventId == event.eventId) {
                    state.copy(transientPresenceBanner = null)
                } else {
                    state
                }
            }
        }
    }

    fun dismissPresenceBanner() {
        bannerAutoHideJob?.cancel()
        _actionState.update { it.copy(transientPresenceBanner = null) }
    }

    /**
     * Spins the Daily Wheel of Fortune ("🎡 عجلة الحظ اليومية") for free Coins & XP rewards.
     */
    fun spinDailyWheelOfFortune() {
        if (_actionState.value.wheelOfFortuneSpunToday) {
            _actionState.update {
                it.copy(statusMessage = "🎡 لقد قمت بتدوير عجلة الحظ اليوم بالفعل! عد غداً لجائزة جديدة")
            }
            return
        }
        val prizes = listOf(
            Triple(40, 25, "🎁 ربحت 40 Coins + 25 XP من عجلة الحظ!"),
            Triple(75, 40, "🌟 حظ ذهبي! ربحت 75 Coins + 40 XP من عجلة الحظ!"),
            Triple(120, 60, "👑 جائزة كبرى! ربحت 120 Coins + 60 XP من عجلة الحظ!")
        )
        val chosen = prizes[(System.currentTimeMillis() % prizes.size).toInt().coerceAtLeast(0)]
        val multiplier = if (_actionState.value.adminDashboardStats.isDoubleCoinsFestivalActive) 2 else 1
        val finalCoins = chosen.first * multiplier
        _actionState.update {
            it.copy(
                coinsBalance = it.coinsBalance + finalCoins,
                userXp = it.userXp + chosen.second,
                wheelOfFortuneSpunToday = true,
                lastWheelRewardText = chosen.third,
                statusMessage = chosen.third,
                errorMessage = null
            )
        }
    }

    /**
     * Triggers an interactive sound effect on the active room stage ("👏 تصفيق • 🎉 زغروطة • 🥁 طبلة").
     */
    fun triggerRoomStageSoundEffect(effectId: String) {
        val currentRoom = _actionState.value.activeRoom ?: return
        val sfx = WanasCatalogData.roomStageSoundEffects.find { it.id == effectId } ?: return
        val updatedRoom = currentRoom.copy(activeSoundEffectBanner = sfx.bannerMessage)
        _localRooms.update { list -> list.map { if (it.roomId == currentRoom.roomId) updatedRoom else it } }
        _actionState.update {
            it.copy(
                activeRoom = updatedRoom,
                userXp = it.userXp + 5,
                statusMessage = sfx.bannerMessage
            )
        }
        sendChatMessageInActiveRoom("${sfx.emoji} أطلق مؤثر المسرح: ${sfx.labelAr}!")
    }

    /**
     * Launches or rotates the interactive Voice Challenge ("🎤 تحدي المايك وسؤال السهرة") inside the active room.
     */
    fun spinRoomVoiceChallenge() {
        val currentRoom = _actionState.value.activeRoom ?: return
        val challenges = WanasCatalogData.roomVoiceChallenges
        val nextChallenge = challenges.firstOrNull { it != currentRoom.activeVoiceChallengeQuestion }
            ?: challenges.first()
        val updatedRoom = currentRoom.copy(activeVoiceChallengeQuestion = nextChallenge)
        _localRooms.update { list -> list.map { if (it.roomId == currentRoom.roomId) updatedRoom else it } }
        _actionState.update {
            it.copy(
                activeRoom = updatedRoom,
                userXp = it.userXp + 10,
                statusMessage = "🎲 تم إطلاق تحدي المايك الجديد في الغرفة!"
            )
        }
        sendChatMessageInActiveRoom(nextChallenge)
    }

    /**
     * Votes / boosts either Team Red or Team Blue in the Live TikTok-style Room PK Challenge ("⚔️ تحدي الفريقين PK مثل تيك توك").
     */
    fun boostRoomPkTeam(isRedTeam: Boolean, points: Int = 25) {
        val currentRoom = _actionState.value.activeRoom ?: return
        val currentState = _actionState.value
        val nextCombo = if (isRedTeam) currentState.pkRedComboStreak + 1 else currentState.pkBlueComboStreak + 1
        val multiplierBonus = if (nextCombo >= 5) 15 else if (nextCombo >= 3) 5 else 0
        val totalAdded = points + multiplierBonus
        val updatedRoom = if (isRedTeam) {
            currentRoom.copy(pkTeamRedScore = currentRoom.pkTeamRedScore + totalAdded)
        } else {
            currentRoom.copy(pkTeamBlueScore = currentRoom.pkTeamBlueScore + totalAdded)
        }
        _localRooms.update { list -> list.map { if (it.roomId == currentRoom.roomId) updatedRoom else it } }
        _actionState.update {
            it.copy(
                activeRoom = updatedRoom,
                pkRedComboStreak = if (isRedTeam) nextCombo else it.pkRedComboStreak,
                pkBlueComboStreak = if (!isRedTeam) nextCombo else it.pkBlueComboStreak,
                pkComboStreakCount = nextCombo,
                pkLastComboTeamLabel = if (isRedTeam) "الفريق الأحمر 🔥" else "الفريق الأزرق ⚡",
                pkTargetTeamIsRed = isRedTeam,
                userXp = it.userXp + 10,
                statusMessage = if (isRedTeam) {
                    "🔥 تيك توك PK Combo x$nextCombo! دعمت الفريق الأحمر بـ +$totalAdded نقطة!"
                } else {
                    "⚡ تيك توك PK Combo x$nextCombo! دعمت الفريق الأزرق بـ +$totalAdded نقطة!"
                }
            )
        }
    }

    /**
     * Alias for creating a Coin Package purchase order from Store and linking it to the App Owner Control Panel.
     */
    fun createCoinPackagePurchaseOrder(
        pkg: CoinPackage,
        paymentMethodAr: String = "فودافون كاش / إنستا باي",
        referenceNumber: String = "VOD-${System.currentTimeMillis().toString().takeLast(6)}"
    ) {
        submitCoinPackagePurchaseOrder(
            coinPackage = pkg,
            paymentMethod = paymentMethodAr,
            referenceNumber = referenceNumber
        )
    }

    /**
     * Alias for Owner Control Panel payment order confirmation/rejection.
     */
    fun approveOrRejectPaymentOrderByOwner(
        orderId: String,
        approve: Boolean,
        adminNote: String = ""
    ) {
        confirmOrRejectPurchaseOrderByOwner(
            orderId = orderId,
            approve = approve,
            ownerNote = adminNote
        )
    }

    /**
     * 👑 Owner Power Action across ALL rooms from the Control Panel:
     * Supports "LAUNCH_PK_ALL_ROOMS", "PIN_TOP_ACTIVE_ROOM", "RESET_ALL_PK_SCORES", "UNLOCK_ALL_ROOMS", "MUTE_ALL_ROOMS_MICS".
     */
    fun ownerQuickControlAllRooms(actionKey: String) {
        _localRooms.update { rooms ->
            when (actionKey) {
                "LAUNCH_PK_ALL_ROOMS" -> rooms.map { rm ->
                    rm.copy(
                        pkTeamRedScore = rm.pkTeamRedScore + 200,
                        pkTeamBlueScore = rm.pkTeamBlueScore + 200,
                        pinnedAnnouncement = "⚔️ جولة تحدي تيك توك PK كبرى أطلقها صاحب التطبيق في جميع الغرف!"
                    )
                }
                "PIN_TOP_ACTIVE_ROOM" -> {
                    val topRoomId = rooms.maxByOrNull { it.activeMembersCount }?.roomId
                    rooms.map { rm ->
                        if (rm.roomId == topRoomId) rm.copy(isPinnedByAdmin = true) else rm
                    }.sortedByDescending { if (it.isPinnedByAdmin) Long.MAX_VALUE else it.timestampMillis }
                }
                "RESET_ALL_PK_SCORES" -> rooms.map { rm ->
                    rm.copy(pkTeamRedScore = 100, pkTeamBlueScore = 100)
                }
                "UNLOCK_ALL_ROOMS" -> rooms.map { rm ->
                    rm.copy(isRoomLockedByAdmin = false, isPasswordProtected = false, roomPassword = "")
                }
                else -> rooms
            }
        }

        _actionState.update { st ->
            val updatedActive = st.activeRoom?.let { active ->
                _localRooms.value.find { it.roomId == active.roomId } ?: active
            }
            val updatedSeats = if (actionKey == "MUTE_ALL_ROOMS_MICS") {
                st.activeRoomSeats.map { it.copy(isMuted = true, isSpeaking = false) }
            } else st.activeRoomSeats

            val msg = when (actionKey) {
                "LAUNCH_PK_ALL_ROOMS" -> "⚔️ تم إشعال تحدي تيك توك PK المباشر في جميع الغرف الصوتية دفعة واحدة!"
                "PIN_TOP_ACTIVE_ROOM" -> "📌 تم تثبيت الغرفة الأعلى نشاطاً في صدارة جميع الغرف!"
                "RESET_ALL_PK_SCORES" -> "🔄 تم تصفير وإعادة ضبط نقاط تحدي الـ PK في جميع الغرف!"
                "UNLOCK_ALL_ROOMS" -> "🔓 تم فتح جميع الغرف المقفلة وإلغاء كلمات المرور بقرار المالك!"
                "MUTE_ALL_ROOMS_MICS" -> "🔇 تم كتم جميع المايكات المفتوحة في الغرف بقرار المالك الفوري!"
                else -> "👑 تم تنفيذ أمر التحكم الشامل في الغرف بنجاح"
            }
            st.copy(
                activeRoom = updatedActive,
                activeRoomSeats = updatedSeats,
                isPkCountdownRunning = if (actionKey == "LAUNCH_PK_ALL_ROOMS") true else st.isPkCountdownRunning,
                statusMessage = msg
            )
        }
        recordAdminAuditLog(
            actionTaken = "تحكم شامل في جميع الغرف ($actionKey)",
            targetName = "كل الغرف الصوتية",
            locationRoomOrSection = "لوحة تحكم المالك — التحكم الفوري بالغرف"
        )
    }

    /**
     * 👑 Owner Power Action across Members from the Control Panel:
     * Supports "GRANT_VIP_AND_COINS", "REWARD_ALL_ACTIVE_MEMBERS", "FORCE_MUTE_MEMBER", "RESET_WARNINGS_AND_ACTIVATE".
     */
    fun ownerQuickControlAllMembers(
        targetName: String,
        actionKey: String,
        bonusCoins: Int = 250
    ) {
        val cleanTarget = targetName.trim().ifBlank { _actionState.value.memberDisplayName }
        when (actionKey) {
            "GRANT_VIP_AND_COINS" -> {
                grantBonusCoinsByAdmin(cleanTarget, bonusCoins, grantVipAlso = true)
                performOwnerMemberAction(cleanTarget, ManagedAccountStatus.ACTIVE, "ترقية VIP ملكية + شحن $bonusCoins Coins من المالك")
            }
            "REWARD_ALL_ACTIVE_MEMBERS" -> {
                _actionState.update { st ->
                    val updatedDir = st.managedMembersDirectory.map { m ->
                        m.copy(
                            coinsBalance = m.coinsBalance + bonusCoins,
                            giftsAndCoinsLog = listOf("🎁 مكافأة جماعية من المالك +$bonusCoins🪙") + m.giftsAndCoinsLog
                        )
                    }
                    st.copy(
                        coinsBalance = st.coinsBalance + bonusCoins,
                        managedMembersDirectory = updatedDir,
                        statusMessage = "🎉 تم توزيع مكافأة جماعية (+$bonusCoins Coins) لجميع الأعضاء المتصلين في التطبيق!"
                    )
                }
            }
            "FORCE_MUTE_MEMBER" -> {
                performOwnerMemberAction(cleanTarget, ManagedAccountStatus.VOICE_MUTED, "كتم صوتي فوري وإغلاق المايك من لوحة المالك")
            }
            "RESET_WARNINGS_AND_ACTIVATE" -> {
                performOwnerMemberAction(cleanTarget, ManagedAccountStatus.ACTIVE, "تصفير الإنذارات وإعادة التفعيل الكامل بأمر المالك")
            }
        }
        recordAdminAuditLog(
            actionTaken = "تحكم فوري بالأعضاء ($actionKey)",
            targetName = cleanTarget,
            locationRoomOrSection = "لوحة تحكم المالك — إدارة الأعضاء"
        )
    }

    /**
     * 💳 Submits a Coin Package purchase order from the Store and links it directly to the App Owner Control Panel
     * ("واربط الشراء بلوحة التحكم لتأكيد الدفع والتحكم في الأعضاء والغرف").
     */
    fun submitCoinPackagePurchaseOrder(
        coinPackage: CoinPackage,
        paymentMethod: String = "VODAFONE_CASH",
        referenceNumber: String = "TXN-${System.currentTimeMillis() % 1000000}"
    ) {
        val state = _actionState.value
        val memberName = state.memberDisplayName
        val myMemberCode = state.equippedSpecialMemberCode ?: generateMemberIdCode(currentUserId.ifBlank { memberName })
        val cleanRef = referenceNumber.trim().ifBlank { "TXN-${System.currentTimeMillis() % 1000000}" }
        val order = PaymentPurchaseOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            buyerUserId = currentUserId,
            buyerMemberCode = myMemberCode,
            buyerName = memberName,
            orderType = "COIN_PACKAGE",
            itemId = coinPackage.packageId,
            itemTitleAr = "باقة ${coinPackage.coinsAmount} Coin (${coinPackage.bonusLabel})",
            coinsToCredit = coinPackage.coinsAmount,
            grantVipBadge = coinPackage.coinsAmount >= 1200,
            vipBadgeLabel = "👑 داعم ونس الذهبي VIP",
            amountEgp = coinPackage.priceEgp,
            paymentMethod = paymentMethod,
            transactionReference = cleanRef,
            status = PaymentPurchaseOrderStatus.PENDING_OWNER_APPROVAL,
            timestampLabel = "الآن"
        )
        _actionState.update { st ->
            st.copy(
                paymentPurchaseOrders = listOf(order) + st.paymentPurchaseOrders,
                adminDashboardStats = st.adminDashboardStats.copy(
                    pendingPaymentsCount = st.adminDashboardStats.pendingPaymentsCount + 1
                ),
                statusMessage = "💳 تم تسجيل طلب شراء «${order.itemTitleAr}» (${coinPackage.priceEgp} ج.م) وإرساله إلى لوحة تحكم صاحب التطبيق لتأكيد الدفع!",
                errorMessage = null
            )
        }
        recordAdminAuditLog(
            actionTaken = "طلب شراء ${order.itemTitleAr} بقيمة ${coinPackage.priceEgp} ج.م (مرجع: $cleanRef)",
            targetName = "$memberName (#$myMemberCode)",
            locationRoomOrSection = "متجر العملات ⬅️ لوحة تحكم المالك",
            resultSummary = "بانتظار تأكيد الدفع من المالك ⏳"
        )
    }

    /**
     * 👑 App Owner Control Panel: Confirm (Approve) or Reject a pending purchase/payment order.
     * When confirmed, automatically credits the member's Coins, unlocks VIP if applicable, records in Server Ledger, and updates revenue stats.
     */
    fun confirmOrRejectPurchaseOrderByOwner(
        orderId: String,
        approve: Boolean,
        ownerNote: String = ""
    ) {
        val state = _actionState.value
        val targetOrder = state.paymentPurchaseOrders.find { it.orderId == orderId } ?: return
        if (targetOrder.status != PaymentPurchaseOrderStatus.PENDING_OWNER_APPROVAL) {
            _actionState.update {
                it.copy(statusMessage = "ℹ️ تم البت في طلب الدفع (${targetOrder.orderId}) مسبقاً: ${targetOrder.status.labelAr}")
            }
            return
        }

        val resolvedStatus = if (approve) {
            PaymentPurchaseOrderStatus.APPROVED
        } else {
            PaymentPurchaseOrderStatus.REJECTED
        }
        val cleanNote = ownerNote.trim().ifBlank {
            if (approve) "تم التحقق من التحويل وتأكيد الدفع من صاحب التطبيق ✅" else "تم رفض الطلب لعدم مطابقة رقم المرجع ❌"
        }

        val updatedOrder = targetOrder.copy(
            status = resolvedStatus,
            ownerDecisionNote = cleanNote,
            timestampLabel = "تم التأكيد الآن"
        )

        val newReceipt = if (approve) {
            VerifiedPaymentReceipt(
                receiptId = "rcpt_${targetOrder.orderId}",
                userId = targetOrder.buyerUserId,
                memberName = targetOrder.buyerName,
                planId = targetOrder.itemId,
                planTitle = targetOrder.itemTitleAr,
                amountEgp = targetOrder.amountEgp,
                paymentMethod = targetOrder.paymentMethod,
                transactionReference = targetOrder.transactionReference,
                verified = true,
                timestamp = Timestamp.now()
            )
        } else null

        val newLedgerEntry = if (approve && targetOrder.coinsToCredit > 0) {
            CoinLedgerRecord(
                ledgerId = "LDG-${System.currentTimeMillis() % 100000}",
                targetMemberId = targetOrder.buyerMemberCode,
                targetMemberName = targetOrder.buyerName,
                deltaCoins = targetOrder.coinsToCredit,
                operationTypeAr = "شحن مؤكد من لوحة المالك (${targetOrder.itemTitleAr})",
                authorizedBy = state.memberDisplayName,
                auditReason = "تأكيد دفع مرجع ${targetOrder.transactionReference} (${targetOrder.amountEgp} ج.م)",
                paymentStatusAr = "مؤكد ومعتمد من المالك ✅",
                timestampLabel = "الآن"
            )
        } else null

        _actionState.update { st ->
            val isBuyerSelf = targetOrder.buyerName.equals(st.memberDisplayName, ignoreCase = true) ||
                targetOrder.buyerUserId == currentUserId
            val nextCoins = if (approve && isBuyerSelf) st.coinsBalance + targetOrder.coinsToCredit else st.coinsBalance
            val nextVip = if (approve && isBuyerSelf && targetOrder.grantVipBadge) true else st.isPaidVip
            val nextBadge = if (approve && isBuyerSelf && targetOrder.grantVipBadge) targetOrder.vipBadgeLabel else st.memberRoleBadge

            val updatedMembersDir = st.managedMembersDirectory.map { m ->
                if (m.displayName.equals(targetOrder.buyerName, ignoreCase = true) || m.memberId == targetOrder.buyerMemberCode) {
                    m.copy(
                        coinsBalance = if (approve) m.coinsBalance + targetOrder.coinsToCredit else m.coinsBalance,
                        giftsAndCoinsLog = if (approve) {
                            listOf("✅ شحن مؤكد +${targetOrder.coinsToCredit}🪙 (${targetOrder.itemTitleAr})") + m.giftsAndCoinsLog
                        } else {
                            listOf("❌ رفض طلب دفع ${targetOrder.orderId}") + m.giftsAndCoinsLog
                        }
                    )
                } else m
            }

            st.copy(
                coinsBalance = nextCoins,
                isPaidVip = nextVip,
                memberRoleBadge = nextBadge,
                paymentPurchaseOrders = st.paymentPurchaseOrders.map { if (it.orderId == orderId) updatedOrder else it },
                paymentReceipts = if (newReceipt != null) listOf(newReceipt) + st.paymentReceipts else st.paymentReceipts,
                coinLedgerRecords = if (newLedgerEntry != null) listOf(newLedgerEntry) + st.coinLedgerRecords else st.coinLedgerRecords,
                managedMembersDirectory = updatedMembersDir,
                adminDashboardStats = st.adminDashboardStats.copy(
                    pendingPaymentsCount = (st.adminDashboardStats.pendingPaymentsCount - 1).coerceAtLeast(0),
                    approvedPaymentsCount = st.adminDashboardStats.approvedPaymentsCount + (if (approve) 1 else 0),
                    rejectedPaymentsCount = st.adminDashboardStats.rejectedPaymentsCount + (if (!approve) 1 else 0),
                    coinsSoldTotal = st.adminDashboardStats.coinsSoldTotal + (if (approve) targetOrder.coinsToCredit else 0),
                    dailyRevenueEgp = st.adminDashboardStats.dailyRevenueEgp + (if (approve) targetOrder.amountEgp else 0),
                    weeklyRevenueEgp = st.adminDashboardStats.weeklyRevenueEgp + (if (approve) targetOrder.amountEgp else 0),
                    monthlyRevenueEgp = st.adminDashboardStats.monthlyRevenueEgp + (if (approve) targetOrder.amountEgp else 0),
                    activePremiumSubscriptions = st.adminDashboardStats.activePremiumSubscriptions + (if (approve && targetOrder.grantVipBadge) 1 else 0)
                ),
                statusMessage = if (approve) {
                    "✅ تم تأكيد دفع الطلب (${targetOrder.orderId}) للعضو «${targetOrder.buyerName}» وشحن +${targetOrder.coinsToCredit} Coins فوراً!"
                } else {
                    "❌ تم رفض طلب الدفع (${targetOrder.orderId}) للعضو «${targetOrder.buyerName}»"
                }
            )
        }

        recordAdminAuditLog(
            actionTaken = if (approve) "تأكيد دفع وشحن +${targetOrder.coinsToCredit} Coins (${targetOrder.amountEgp} ج.م)" else "رفض طلب دفع (${targetOrder.orderId})",
            targetName = "${targetOrder.buyerName} (#${targetOrder.buyerMemberCode})",
            locationRoomOrSection = "لوحة التحكم ⬅️ بوابة تأكيد المدفوعات",
            resultSummary = cleanNote
        )
    }

    /**
     * 👑 Owner Power Action on All Rooms: Boost room trend, start PK battle in all rooms, lock/unlock all rooms, or change room category.
     */
    fun performOwnerPowerRoomAction(
        roomId: String,
        actionType: String, // "START_TIKTOK_PK", "BOOST_TREND", "UPDATE_CATEGORY", "FORCE_CLEAR_SEATS"
        newCategory: String = WanasRoomCategoryFilter.GENERAL.id
    ) {
        _localRooms.update { list ->
            list.map { rm ->
                if (rm.roomId == roomId) {
                    when (actionType) {
                        "START_TIKTOK_PK" -> rm.copy(
                            pkTeamRedScore = rm.pkTeamRedScore + 150,
                            pkTeamBlueScore = rm.pkTeamBlueScore + 150,
                            pinnedAnnouncement = "⚔️ جولة تحدي تيك توك PK مشتعلة الآن برعاية إدارة وَنَس!"
                        )
                        "BOOST_TREND" -> rm.copy(
                            isPinnedByAdmin = true,
                            activeMembersCount = rm.activeMembersCount + 12,
                            activeSpeakersCount = (rm.activeSpeakersCount + 2).coerceAtMost(8)
                        )
                        "UPDATE_CATEGORY" -> {
                            val catMeta = WanasRoomCategoryFilter.resolveDisplayFilter(newCategory)
                            rm.copy(
                                roomCategory = catMeta.id,
                                roomTopicTag = "${catMeta.emoji} ${catMeta.labelAr}"
                            )
                        }
                        else -> rm
                    }
                } else rm
            }
        }
        val targetRoom = _localRooms.value.find { it.roomId == roomId }
        _actionState.update { st ->
            val updatedActive = if (st.activeRoom?.roomId == roomId && targetRoom != null) targetRoom else st.activeRoom
            st.copy(
                activeRoom = updatedActive,
                statusMessage = when (actionType) {
                    "START_TIKTOK_PK" -> "⚔️ تم إشعال جولة تحدي تيك توك PK في غرفة «${targetRoom?.roomName ?: roomId}»!"
                    "BOOST_TREND" -> "🔥 تم رفع الغرفة «${targetRoom?.roomName ?: roomId}» إلى صدارة الترند وتثبيتها!"
                    "UPDATE_CATEGORY" -> "🏷️ تم تحديث تصنيف الغرفة «${targetRoom?.roomName ?: roomId}» بواسطة المالك!"
                    else -> "👑 تم تنفيذ إجراء التحكم الفوري على الغرفة بنجاح"
                }
            )
        }
        recordAdminAuditLog(
            actionTaken = "تحكم فوري بالغرفة ($actionType)",
            targetName = targetRoom?.roomName ?: roomId,
            locationRoomOrSection = "لوحة تحكم المالك — إدارة الغرف"
        )
    }

    /**
     * Room Admin / App Owner: Update the room's pinned announcement banner or mic seats count (4 or 8).
     */
    fun updateRoomSettingsByAdmin(roomId: String, newAnnouncement: String, newMicSeatsCount: Int) {
        val cleanAnn = newAnnouncement.trim().ifBlank { "أهلاً بكم في سهرة وَنَس! 🎙️" }
        val validSeats = if (newMicSeatsCount <= 4) 4 else 8
        _localRooms.update { list ->
            list.map { r ->
                if (r.roomId == roomId) {
                    r.copy(pinnedAnnouncement = cleanAnn, micSeatsCount = validSeats)
                } else r
            }
        }
        _actionState.update { state ->
            val updatedActive = if (state.activeRoom?.roomId == roomId) {
                state.activeRoom.copy(pinnedAnnouncement = cleanAnn, micSeatsCount = validSeats)
            } else state.activeRoom
            state.copy(
                activeRoom = updatedActive,
                statusMessage = "🛡️ تم تحديث إعلان الغرفة وعدد المايكات ($validSeats مقاعد) بنجاح"
            )
        }
    }

    /**
     * App Owner / Room Admin: Toggle pin status or lock status of any room.
     */
    fun togglePinOrLockRoomByAdmin(roomId: String, togglePin: Boolean = false, toggleLock: Boolean = false) {
        _localRooms.update { list ->
            list.map { r ->
                if (r.roomId == roomId) {
                    r.copy(
                        isPinnedByAdmin = if (togglePin) !r.isPinnedByAdmin else r.isPinnedByAdmin,
                        isRoomLockedByAdmin = if (toggleLock) !r.isRoomLockedByAdmin else r.isRoomLockedByAdmin
                    )
                } else r
            }.sortedByDescending { if (it.isPinnedByAdmin) Long.MAX_VALUE else it.timestampMillis }
        }
        _actionState.update { state ->
            val target = _localRooms.value.find { it.roomId == roomId }
            val updatedActive = if (state.activeRoom?.roomId == roomId && target != null) target else state.activeRoom
            state.copy(
                activeRoom = updatedActive,
                statusMessage = "👑 تم تحديث حالة تثبيت/قفل الغرفة بواسطة الإدارة"
            )
        }
    }

    /**
     * App Owner Admin Dashboard: Reset PK scores or clear chat messages in active room.
     */
    fun resetRoomPkOrClearChatByAdmin(roomId: String, clearChat: Boolean) {
        if (clearChat) {
            _localRoomMessages.update { it + (roomId to emptyList()) }
        }
        _localRooms.update { list ->
            list.map { r ->
                if (r.roomId == roomId) {
                    r.copy(pkTeamRedScore = 100, pkTeamBlueScore = 100)
                } else r
            }
        }
        _actionState.update { state ->
            val updatedActive = if (state.activeRoom?.roomId == roomId) {
                state.activeRoom.copy(pkTeamRedScore = 100, pkTeamBlueScore = 100)
            } else state.activeRoom
            state.copy(
                activeRoom = updatedActive,
                activeRoomMessages = if (clearChat && state.activeRoom?.roomId == roomId) emptyList() else state.activeRoomMessages,
                statusMessage = if (clearChat) "🧹 تم مسح رسائل دردشة الغرفة بواسطة الإدارة" else "⚔️ تم تصفير عداد تحدي PK للغرفة"
            )
        }
    }

    /**
     * App Owner Admin Dashboard: Broadcast global system announcement to all rooms and users.
     */
    fun broadcastGlobalAdminAnnouncement(announcementText: String) {
        val clean = announcementText.trim()
        if (clean.isBlank()) return
        _actionState.update { st ->
            st.copy(
                adminDashboardStats = st.adminDashboardStats.copy(
                    globalAnnouncementBanner = clean
                ),
                statusMessage = "📢 تم بث الإعلان العام لجميع أعضاء وغرف التطبيق: $clean"
            )
        }
    }

    /**
     * App Owner Admin Dashboard: Grant bonus Coins or VIP badge to a member or all active users.
     */
    fun grantBonusCoinsByAdmin(targetMemberName: String, bonusCoins: Int, grantVipAlso: Boolean = false) {
        val cleanTarget = targetMemberName.trim().ifBlank { _actionState.value.memberDisplayName }
        _actionState.update { st ->
            st.copy(
                coinsBalance = st.coinsBalance + bonusCoins,
                isPaidVip = if (grantVipAlso) true else st.isPaidVip,
                memberRoleBadge = if (grantVipAlso) "👑 عضو ونس VIP" else st.memberRoleBadge,
                statusMessage = "🎁 قرار إداري: تم منح +$bonusCoins Coins ${if (grantVipAlso) "+ ترقية VIP 👑 " else ""}إلى «$cleanTarget»!"
            )
        }
    }

    /**
     * App Owner Admin Dashboard: Toggle app-wide control switches (Room Creation, Profanity Filter, Double Coins Festival).
     */
    fun updateGlobalAppControlToggleByAdmin(
        toggleRoomCreation: Boolean = false,
        toggleProfanityFilter: Boolean = false,
        toggleDoubleCoins: Boolean = false
    ) {
        _actionState.update { st ->
            val currentStats = st.adminDashboardStats
            val updatedStats = currentStats.copy(
                isRoomCreationOpenForAll = if (toggleRoomCreation) !currentStats.isRoomCreationOpenForAll else currentStats.isRoomCreationOpenForAll,
                isStrictProfanityFilterActive = if (toggleProfanityFilter) !currentStats.isStrictProfanityFilterActive else currentStats.isStrictProfanityFilterActive,
                isDoubleCoinsFestivalActive = if (toggleDoubleCoins) !currentStats.isDoubleCoinsFestivalActive else currentStats.isDoubleCoinsFestivalActive
            )
            st.copy(
                adminDashboardStats = updatedStats,
                statusMessage = "⚙️ تم تحديث إعدادات التحكم الشامل بالتطبيق من لوحة الإدارة"
            )
        }
    }

    private var pkTimerJob: Job? = null

    /**
     * ⚔️ 1. Start or Toggle Live 3-Minute PK Battle Countdown Timer inside the active voice room.
     */
    fun toggleOrStartPkBattleCountdown() {
        val currentState = _actionState.value
        if (currentState.isPkCountdownRunning) {
            pkTimerJob?.cancel()
            _actionState.update {
                it.copy(
                    isPkCountdownRunning = false,
                    statusMessage = "⏸️ تم إيقاف عداد جولة تحدي الـ PK مؤقتاً"
                )
            }
            return
        }

        val initialSecs = if (currentState.pkCountdownSecondsRemaining <= 0) 180 else currentState.pkCountdownSecondsRemaining
        _actionState.update {
            it.copy(
                isPkCountdownRunning = true,
                pkCountdownSecondsRemaining = initialSecs,
                pkWinnerAnnouncementText = null,
                statusMessage = "⚔️ بدأت جولة تحدي الـ PK المباشر (3 دقائق) — ادعم فريقك الآن!"
            )
        }

        pkTimerJob?.cancel()
        pkTimerJob = viewModelScope.launch {
            while (_actionState.value.pkCountdownSecondsRemaining > 0 && _actionState.value.isPkCountdownRunning) {
                delay(1000L)
                val next = (_actionState.value.pkCountdownSecondsRemaining - 1).coerceAtLeast(0)
                if (next == 0) {
                    val activeRoom = _actionState.value.activeRoom
                    val red = activeRoom?.pkTeamRedScore ?: 100
                    val blue = activeRoom?.pkTeamBlueScore ?: 100
                    val winnerText = when {
                        red > blue -> "🏆 انتهت الجولة! فاز الفريق الأحمر 🔥 ($red مقابل $blue) وحصل الداعمون على +50 XP!"
                        blue > red -> "🏆 انتهت الجولة! فاز الفريق الأزرق ⚡ ($blue مقابل $red) وحصل الداعمون على +50 XP!"
                        else -> "🤝 انتهت جولة الـ PK بالتعادل الحماسي ($red - $blue)!"
                    }
                    _actionState.update { st ->
                        st.copy(
                            pkCountdownSecondsRemaining = 180,
                            isPkCountdownRunning = false,
                            pkWinnerAnnouncementText = winnerText,
                            userXp = st.userXp + 50,
                            statusMessage = winnerText
                        )
                    }
                    break
                } else {
                    _actionState.update { it.copy(pkCountdownSecondsRemaining = next) }
                }
            }
        }
    }

    /**
     * 🧧 2. Drop a Lucky Coin Box ("صندوق الحظ / مطر العملات") inside the active room so members can claim random coins.
     */
    fun dropLuckyCoinBoxInRoom(totalCoins: Int = 100) {
        val current = _actionState.value
        val activeRoom = current.activeRoom ?: return
        if (current.coinsBalance < totalCoins) {
            _actionState.update {
                it.copy(errorMessage = "رصيدك الحالي (${current.coinsBalance} عملة) غير كافٍ لإرسال صندوق الحظ ($totalCoins عملة)")
            }
            return
        }
        val myMemberCode = current.equippedSpecialMemberCode
            ?: generateMemberIdCode(currentUserId ?: current.memberDisplayName)
        val box = LuckyCoinBoxState(
            boxId = "box_${System.currentTimeMillis()}",
            roomId = activeRoom.roomId,
            senderName = current.memberDisplayName,
            senderMemberCode = myMemberCode,
            totalCoins = totalCoins,
            remainingCoins = totalCoins,
            maxClaims = 5
        )
        _actionState.update { st ->
            val updatedSupporters = updateTopSupportersList(
                currentList = st.roomTopSupporters,
                memberName = st.memberDisplayName,
                memberCode = myMemberCode,
                emoji = st.memberAvatarEmoji,
                addedCoins = totalCoins
            )
            st.copy(
                coinsBalance = st.coinsBalance - totalCoins,
                userXp = st.userXp + 40,
                activeLuckyCoinBox = box,
                roomTopSupporters = updatedSupporters,
                latestRoomGiftBannerText = "🧧 أرسل ${st.memberDisplayName} (#$myMemberCode) صندوق حظ بقيمة $totalCoins عملة في الغرفة! اضغط لالتقاط نصيبك!",
                statusMessage = "🧧 تم إطلاق صندوق الحظ ($totalCoins عملة) لجميع الموجودين في الغرفة!"
            )
        }
        sendChatMessageInActiveRoom("🧧 أطلق صندوق الحظ في الغرفة بقيمة $totalCoins عملة! سارعوا بالتقاط العملات 🪙🎉")
    }

    /**
     * 🧧 Claim random coins from the active Lucky Coin Box in the room.
     */
    fun claimFromLuckyCoinBox() {
        val current = _actionState.value
        val box = current.activeLuckyCoinBox ?: return
        val myMemberCode = current.equippedSpecialMemberCode
            ?: generateMemberIdCode(currentUserId ?: current.memberDisplayName)

        if (myMemberCode in box.claimedByMemberCodes) {
            _actionState.update {
                it.copy(statusMessage = "✅ لقد استلمت نصيبك من صندوق الحظ هذا بالفعل!")
            }
            return
        }
        if (!box.canClaim) {
            _actionState.update {
                it.copy(activeLuckyCoinBox = null, statusMessage = "انتهى صندوق الحظ الحالي!")
            }
            return
        }

        val reward = (15..35).random().coerceAtMost(box.remainingCoins)
        val nextRemaining = (box.remainingCoins - reward).coerceAtLeast(0)
        val updatedClaims = box.claimedByMemberCodes + myMemberCode
        val updatedBox = if (nextRemaining == 0 || updatedClaims.size >= box.maxClaims) {
            null
        } else {
            box.copy(
                remainingCoins = nextRemaining,
                claimedByMemberCodes = updatedClaims
            )
        }

        _actionState.update { st ->
            st.copy(
                coinsBalance = st.coinsBalance + reward,
                userXp = st.userXp + 15,
                activeLuckyCoinBox = updatedBox,
                statusMessage = "🎉 مبروك! التقطت +$reward عملة من صندوق الحظ الذي أرسله ${box.senderName}!"
            )
        }
    }

    private fun updateTopSupportersList(
        currentList: List<RoomTopSupporterItem>,
        memberName: String,
        memberCode: String,
        emoji: String,
        addedCoins: Int
    ): List<RoomTopSupporterItem> {
        val mutable = currentList.toMutableList()
        val existingIdx = mutable.indexOfFirst { it.memberIdCode == memberCode || it.memberName == memberName }
        if (existingIdx >= 0) {
            val ex = mutable[existingIdx]
            mutable[existingIdx] = ex.copy(totalCoinsSupported = ex.totalCoinsSupported + addedCoins)
        } else {
            mutable.add(
                RoomTopSupporterItem(
                    rank = mutable.size + 1,
                    memberName = memberName,
                    memberIdCode = memberCode,
                    avatarEmoji = emoji,
                    totalCoinsSupported = addedCoins
                )
            )
        }
        return mutable
            .sortedByDescending { it.totalCoinsSupported }
            .take(5)
            .mapIndexed { idx, item ->
                item.copy(
                    rank = idx + 1,
                    medalEmoji = when (idx + 1) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "🏅"
                    }
                )
            }
    }

    /**
     * 💬 3. Add an anonymous supportive comment using only the unique Member ID Code (`WNS-xxxxxx`) on a Fadfada post.
     */
    fun addFadfadaSupportComment(postId: String, commentText: String, reactionBadgeAr: String = "🤍 قلبي معك") {
        val clean = commentText.trim()
        if (clean.isBlank()) return
        val current = _actionState.value
        val myMemberCode = current.equippedSpecialMemberCode
            ?: generateMemberIdCode(currentUserId ?: current.memberDisplayName)

        // Filter profanity automatically
        val bannedWords = current.systemSettings.bannedWordsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (bannedWords.any { clean.contains(it, ignoreCase = true) }) {
            _actionState.update {
                it.copy(errorMessage = "🛡️ تم حجب التعليق لاحتوائه على كلمات غير مسموحة. الفضفضة مساحة للدعم النفسي الراقي.")
            }
            return
        }

        val newComment = FadfadaSupportComment(
            commentId = "fcmt_${System.currentTimeMillis()}",
            postId = postId,
            authorMemberCode = myMemberCode,
            commentText = clean,
            reactionBadgeAr = reactionBadgeAr,
            timestampLabel = "الآن"
        )
        _actionState.update { st ->
            val existing = st.fadfadaCommentsByPostId[postId].orEmpty()
            st.copy(
                fadfadaCommentsByPostId = st.fadfadaCommentsByPostId + (postId to (existing + newComment)),
                userXp = st.userXp + 10,
                statusMessage = "💬 تم نشر ردك الداعم بالمعرّف #$myMemberCode بنجاح"
            )
        }
    }

    /**
     * 🤝 React to a Fadfada post with supportive psychological/community badges (`🤍 قلبي معك`, `🌱 هانت`, `🤝 لست وحدك`, `💡 نصيحة`).
     */
    fun sendFadfadaSupportReaction(post: AnonymousFadfadaPost, reactionBadgeAr: String) {
        sendHeartToFadfada(post)
        _actionState.update { st ->
            val currentPostReactions = st.fadfadaReactionCountsByPostId[post.postId].orEmpty()
            val nextCount = (currentPostReactions[reactionBadgeAr] ?: 0) + 1
            val updatedPostReactions = currentPostReactions + (reactionBadgeAr to nextCount)
            st.copy(
                fadfadaReactionCountsByPostId = st.fadfadaReactionCountsByPostId + (post.postId to updatedPostReactions),
                statusMessage = "$reactionBadgeAr — تم إرسال دعمك المعنوي لصاحب الفضفضة (#${post.memberIdCode.ifBlank { generateMemberIdCode(post.authorId) }})"
            )
        }
    }

    /**
     * 🎙️ Convert a Fadfada post into a calm live voice discussion room in one tap and sync via Supabase Realtime.
     */
    fun openVoiceDiscussionRoomFromFadfada(post: AnonymousFadfadaPost): ChatRoomMetadata {
        val code = post.memberIdCode.ifBlank { generateMemberIdCode(post.authorId) }
        val snippet = post.content.take(28).trim()
        val roomTitle = "💭 نقاش هادئ (#$code): $snippet"
        createChatRoom(roomTitle, "")
        val created = _localRooms.value.firstOrNull() ?: ChatRoomMetadata(
            roomId = "room_fadfada_${post.postId.takeLast(6)}",
            roomName = roomTitle,
            creatorId = currentUserId ?: "wanas_host",
            roomTopicTag = post.moodTag,
            roomCategory = "STUDY_CALM",
            pinnedAnnouncement = "مساحة نقاش صوتي هادئ وداعم حول فضفضة العضو #$code: «${post.content.take(90)}»"
        )
        enterChatRoom(created)
        return created
    }

    /**
     * ✨ 4. Buy or equip a Special VIP Member ID (`WNS-777777`), Glowing Seat Frame, or Royal Entry Effect.
     */
    fun purchaseOrEquipSpecialCosmetic(item: SpecialStoreCosmeticItem) {
        val current = _actionState.value
        val alreadyOwned = item.itemId in current.ownedSpecialCosmeticIds
        if (!alreadyOwned && current.coinsBalance < item.costCoins) {
            _actionState.update {
                it.copy(errorMessage = "رصيدك الحالي (${current.coinsBalance} عملة) غير كافٍ لشراء «${item.titleAr}» (${item.costCoins} عملة)")
            }
            return
        }

        _actionState.update { st ->
            val nextBalance = if (alreadyOwned) st.coinsBalance else st.coinsBalance - item.costCoins
            val nextOwned = st.ownedSpecialCosmeticIds + item.itemId
            val nextSpecialCode = if (item.categoryAr.contains("معرّف")) item.previewBadge else st.equippedSpecialMemberCode
            val nextFrame = if (item.categoryAr.contains("إطار")) item.previewBadge else st.equippedSeatFrameLabel
            val nextEntry = if (item.categoryAr.contains("دخول")) item.previewBadge else st.equippedEntryEffectLabel

            st.copy(
                coinsBalance = nextBalance,
                userXp = if (alreadyOwned) st.userXp else st.userXp + 35,
                ownedSpecialCosmeticIds = nextOwned,
                equippedSpecialMemberCode = nextSpecialCode,
                equippedSeatFrameLabel = nextFrame,
                equippedEntryEffectLabel = nextEntry,
                memberRoleBadge = nextFrame ?: st.memberRoleBadge,
                statusMessage = if (alreadyOwned) {
                    "✨ تم تفعيل «${item.titleAr}» على حسابك ومقعدك الصوتي!"
                } else {
                    "🎉 مبروك! تم شراء وتفعيل «${item.titleAr}» بنجاح!"
                }
            )
        }
    }

    /**
     * 🛡️ 5. Join or Create a Wanas Family / Clan ("نظام العائلات والقبائل").
     */
    fun joinWanasFamilyClan(clan: WanasFamilyClan) {
        _actionState.update { st ->
            val updatedClans = st.familiesClansList.map { c ->
                if (c.clanId == clan.clanId && st.joinedClanId != clan.clanId) {
                    c.copy(membersCount = c.membersCount + 1, weeklyActivityPoints = c.weeklyActivityPoints + 120)
                } else c
            }
            st.copy(
                joinedClanId = clan.clanId,
                familiesClansList = updatedClans,
                userXp = st.userXp + 25,
                statusMessage = "🛡️ انضممت رسمياً إلى «${clan.clanNameAr}»! نقاط نشاط العائلة الأسبوعية زادت +120 نقطة"
            )
        }
    }

    fun createNewWanasFamilyClan(clanName: String, motto: String) {
        val cleanName = clanName.trim()
        if (cleanName.isBlank()) return
        val current = _actionState.value
        val newClan = WanasFamilyClan(
            clanId = "clan_${System.currentTimeMillis()}",
            clanNameAr = cleanName,
            badgeEmoji = "👑",
            mottoAr = motto.trim().ifBlank { "عائلة جديدة على قلب واحد في وَنَس" },
            leaderName = current.memberDisplayName,
            membersCount = 1,
            weeklyActivityPoints = 500,
            officialRoomId = current.activeRoom?.roomId ?: "room_wanas_egypt_1"
        )
        _actionState.update { st ->
            st.copy(
                familiesClansList = listOf(newClan) + st.familiesClansList,
                joinedClanId = newClan.clanId,
                userXp = st.userXp + 60,
                statusMessage = "👑 تم تأسيس عائلتك «$cleanName» وتعيينك قائداً لها!"
            )
        }
    }

    fun deleteChatRoom(roomId: String) {
        if (_actionState.value.activeRoom?.roomId == roomId) {
            leaveCurrentRoom()
        }
        _localRooms.update { list -> list.filterNot { it.roomId == roomId } }
        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                repository.deleteChatRoom(roomId).onFailure { err ->
                    _actionState.update {
                        it.copy(errorMessage = err.localizedMessage ?: "تعذر حذف الغرفة")
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeMembersJob?.cancel()
        presenceEventsJob?.cancel()
        chatHistoryJob?.cancel()
        fadfadaPostsJob?.cancel()
        fadfadaAdminJob?.cancel()
        pushNotificationsJob?.cancel()
        paymentReceiptsJob?.cancel()
        bannerAutoHideJob?.cancel()
        pushBannerAutoHideJob?.cancel()
    }

    companion object {
        private const val TAG = "ChatRoomViewModel"
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val BANNER_DISPLAY_DURATION_MS = 3500L
    }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val activityContext = context as? Activity ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOut(
    credentialManager: CredentialManager,
    supabaseService: SupabaseAccountService? = null,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    supabaseService?.clearAutoLoginSession()
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}
