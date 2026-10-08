package com.example.data

import com.google.firebase.Timestamp

/**
 * Communication intent/topic selected before matching ("إنت داخل تعمل إيه؟").
 */
enum class MatchTopicType(
    val id: String,
    val emoji: String,
    val titleAr: String,
    val subtitleAr: String
) {
    VENTING("VENTING", "😔", "محتاج أفضفض", "فضفضة وراحة بال مع مستمع متفهم"),
    LAUGH_CHAT("LAUGH_CHAT", "😂", "دردشة وضحك", "عايز أضحك وفرفشة وهزار خفيف"),
    CASUAL_TALK("CASUAL_TALK", "🗣️", "كلام عادي", "عايز دردشة وسوالف عامة"),
    LISTEN_ONLY("LISTEN_ONLY", "👂", "عايز أسمع", "مستعد أسمعك وأدعمك بقلبي"),
    GAMING("GAMING", "🎮", "عايز ألعب", "تحديات وألعاب صوتية سريعة"),
    MEET_FRIENDS("MEET_FRIENDS", "🤝", "تعارف وصداقات", "عايز أتعرف على ناس جديدة محترمة"),
    STUDY("STUDY", "📚", "مذاكرة", "تشجيع ومذاكرة وتنظيم وقت"),
    WORK_DISCUSSION("WORK_DISCUSSION", "💼", "نقاش عن العمل", "تبادل خبرات وأفكار مهنية")
}

/**
 * Role in the 1-on-1 matching engine: Speaker ("محتاج أتكلم") vs Listener ("عايز أسمع").
 */
enum class MatchRole(val id: String, val labelAr: String, val emoji: String) {
    NEED_TO_SPEAK("SPEAKER", "محتاج أتكلم", "🗣️"),
    WANT_TO_LISTEN("LISTENER", "عايز أسمع", "👂")
}

/**
 * Represents a user waiting in the live server matching queue or available for 1-on-1 voice session.
 */
data class MatchQueueRequest(
    val requestId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmoji: String = "👑",
    val userLevel: Int = 1,
    val role: MatchRole = MatchRole.NEED_TO_SPEAK,
    val topic: MatchTopicType = MatchTopicType.LAUGH_CHAT,
    val isPriorityBoosted: Boolean = false,
    val isPremiumVip: Boolean = false,
    val statusText: String = "متاح الآن للاتصال الفوري",
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * Active 1-on-1 matched session with a 10-minute (600 seconds) free countdown timer.
 */
data class ActiveMatchCallSession(
    val sessionId: String,
    val partnerUserId: String,
    val partnerName: String,
    val partnerEmoji: String,
    val partnerLevel: Int,
    val myRole: MatchRole,
    val topic: MatchTopicType,
    val totalDurationSeconds: Int = 600, // 10 minutes default
    val remainingSeconds: Int = 600,
    val isEnded: Boolean = false,
    val isMutedPartner: Boolean = false,
    val activeGameId: String? = null,
    val currentGameQuestionIndex: Int = 0,
    val myGameScore: Int = 0,
    val partnerGameScore: Int = 0,
    val giftsSentInSession: List<String> = emptyList(),
    val ratingGiven: Int = 0
) {
    val formattedCountdown: String
        get() {
            val mins = (remainingSeconds.coerceAtLeast(0)) / 60
            val secs = (remainingSeconds.coerceAtLeast(0)) % 60
            return String.format("%02d:%02d", mins, secs)
        }
}

/**
 * Friend entry in the Wanas Friends system ("نظام أصدقاء أقوى").
 */
data class WanasFriend(
    val friendUserId: String,
    val friendName: String,
    val friendEmoji: String,
    val friendLevel: Int = 2,
    val isOnline: Boolean = true,
    val presenceState: FriendPresenceState = FriendPresenceState.ONLINE,
    val currentRoomId: String = "room_wanas_egypt_1",
    val currentRoomName: String = "🌙 سهرة مصرية — الناس للناس",
    val statusNote: String = "متصل الآن في ونس ✨",
    val isPendingApproval: Boolean = false,
    val isConversationPinned: Boolean = false,
    val isConversationMuted: Boolean = false,
    val lastSeenLabel: String = "آخر ظهور: متصل الآن",
    val lastMessage: String = "أهلاً بك يا صديقي في ونس!",
    val unreadCount: Int = 0,
    val voiceNotesCount: Int = 0
)

/**
 * Direct chat or voice note message between friends ("Messenger داخل وَنَس").
 */
data class FriendDirectMessage(
    val messageId: String,
    val friendUserId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val isVoiceNote: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val imageAttachmentLabel: String? = null,
    val isBuzzAlert: Boolean = false,
    val isReadByRecipient: Boolean = true,
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * Coin store package ("نظام العملات Coins").
 */
data class CoinPackage(
    val packageId: String,
    val coinsAmount: Int,
    val priceEgp: Int,
    val bonusLabel: String,
    val badgeEmoji: String
)

/**
 * Digital gift item ("الهدايا داخل الغرف والمحادثات").
 */
data class WanasGiftItem(
    val giftId: String,
    val nameAr: String,
    val emoji: String,
    val costCoins: Int,
    val hostShareCoins: Int,
    val effectLabelAr: String = "تأثير بصري متألق في الغرفة ✨",
    val tierLabelAr: String = "هدية مميزة"
)

/**
 * Interactive mini-game available during a voice session or room ("الألعاب أثناء الجلسة").
 */
data class SessionMiniGame(
    val gameId: String,
    val titleAr: String,
    val emoji: String,
    val descriptionAr: String,
    val questions: List<MiniGamePrompt>
)

data class MiniGamePrompt(
    val promptText: String,
    val options: List<String>,
    val correctOptionIndex: Int = 0
)

/**
 * Safety report / moderation ticket ("الأمان والبلاغات").
 */
data class SafetyReportTicket(
    val reportId: String,
    val reporterId: String,
    val reporterName: String,
    val reportedUserId: String,
    val reportedUserName: String,
    val reasonAr: String,
    val sourceContext: String,
    val status: String = "جديد", // جديد | قيد المراجعة | تم الحل | مغلق
    val priorityAr: String = "عالية 🔴",
    val adminReplyText: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * Organized App Sections ("تقسيم التطبيق إلى أقسام وكل قسم له صفحة خاصة به وبترتيب وشكل عصري مميز").
 */
enum class WanasAppSection(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val emoji: String
) {
    MATCHING_HUB("MATCHING_HUB", "الرئيسية والمطابقة", "اتكلم • اسمع • مطابقة فورية", "🏠"),
    VOICE_ROOMS("VOICE_ROOMS", "الغرف الصوتية", "مسرح المايكات • تحدي PK • باسورد", "🎙️"),
    FRIENDS_HUB("FRIENDS_HUB", "الأصدقاء والدردشة", "رسائل • فويس نوت • اتصال مباشر", "👥"),
    STORE_AND_GIFTS("STORE_AND_GIFTS", "المتجر والهدايا", "العملات • عجلة الحظ • هدايا فاخرة", "🎁"),
    FADFADA_SECRET("FADFADA_SECRET", "فضفضة بكود", "مساحة سرية بدون اسم بكود خاص", "🔐"),
    MY_ACCOUNT("MY_ACCOUNT", "حسابي والأمان", "ملفي • الدفع VIP • مركز الأمان", "👤")
}

/**
 * Room categories for filtering the Voice Rooms Hub ("General", "Technology", "Music", "Education", etc.).
 */
enum class WanasRoomCategoryFilter(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val emoji: String,
    val isSelectableForNewRoom: Boolean = true
) {
    ALL("ALL", "الكل", "All", "🌟", isSelectableForNewRoom = false),
    GENERAL("GENERAL", "عام (General)", "General", "🌐"),
    TECHNOLOGY("TECHNOLOGY", "تكنولوجيا (Technology)", "Technology", "💻"),
    MUSIC("MUSIC", "موسيقى وطرب (Music)", "Music", "🎵"),
    EDUCATION("EDUCATION", "تعليم وثقافة (Education)", "Education", "🎓"),
    TRENDING_NOW("TRENDING_NOW", "غرف ترند الآن", "Trending", "🔥", isSelectableForNewRoom = false),
    FAVORITES("FAVORITES", "غرفي المفضلة", "Favorites", "⭐", isSelectableForNewRoom = false),
    PUBLIC_PARTY("PUBLIC_PARTY", "سهرة ونس", "Party", "🌙"),
    COMEDY("COMEDY", "ضحك وهزار", "Comedy", "😂"),
    GAMES_SPORTS("GAMES_SPORTS", "كورة وألعاب", "Sports & Games", "⚽"),
    STUDY_CALM("STUDY_CALM", "مذاكرة وهدوء", "Study & Calm", "📚"),
    VIP_LOCKED("VIP_LOCKED", "غرف بباسورد", "Private VIP", "🔒", isSelectableForNewRoom = false);

    companion object {
        val creatableCategories: List<WanasRoomCategoryFilter>
            get() = listOf(
                GENERAL,
                TECHNOLOGY,
                MUSIC,
                EDUCATION,
                PUBLIC_PARTY,
                COMEDY,
                GAMES_SPORTS,
                STUDY_CALM
            )

        fun resolveDisplayFilter(categoryCode: String): WanasRoomCategoryFilter {
            return entries.find {
                it.id.equals(categoryCode, ignoreCase = true) ||
                    it.labelEn.equals(categoryCode, ignoreCase = true)
            } ?: when (categoryCode.uppercase()) {
                "PUBLIC_PARTY" -> GENERAL
                "STUDY_CALM" -> EDUCATION
                else -> GENERAL
            }
        }
    }
}

/**
 * Ambient background themes selectable by room creators and hosts (e.g., 'Coffee Shop', 'Rainy Night')
 * that dynamically change the room's background UI color scheme, card surfaces, and stage accents.
 */
enum class WanasRoomAmbientTheme(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val emoji: String,
    val vibeSubtitleAr: String,
    val gradientStartHex: Long,
    val gradientMidHex: Long,
    val gradientEndHex: Long,
    val cardSurfaceHex: Long,
    val stageSurfaceHex: Long,
    val accentHex: Long
) {
    ROYAL_NIGHT(
        id = "ROYAL_NIGHT",
        titleEn = "Royal Night",
        titleAr = "ليلة ملكية (Royal Night)",
        emoji = "👑",
        vibeSubtitleAr = "فخامة بنفسجية وذهبية لسهرة وَنَس",
        gradientStartHex = 0xFF0E0922,
        gradientMidHex = 0xFF171033,
        gradientEndHex = 0xFF241346,
        cardSurfaceHex = 0xFF1E1140,
        stageSurfaceHex = 0xFF140B2E,
        accentHex = 0xFFFFC857
    ),
    COFFEE_SHOP(
        id = "COFFEE_SHOP",
        titleEn = "Coffee Shop",
        titleAr = "مقهى دافئ (Coffee Shop)",
        emoji = "☕",
        vibeSubtitleAr = "أجواء كافيه كلاسيكية دافئة برائحة القهوة",
        gradientStartHex = 0xFF23140D,
        gradientMidHex = 0xFF382015,
        gradientEndHex = 0xFF4E2D1E,
        cardSurfaceHex = 0xFF331D13,
        stageSurfaceHex = 0xFF26150E,
        accentHex = 0xFFF59E0B
    ),
    RAINY_NIGHT(
        id = "RAINY_NIGHT",
        titleEn = "Rainy Night",
        titleAr = "ليلة ممطرة (Rainy Night)",
        emoji = "🌧️",
        vibeSubtitleAr = "هدوء المطر الليلي وألوان النيل الداكنة",
        gradientStartHex = 0xFF091526,
        gradientMidHex = 0xFF112640,
        gradientEndHex = 0xFF1A365D,
        cardSurfaceHex = 0xFF132842,
        stageSurfaceHex = 0xFF0C1B2E,
        accentHex = 0xFF38BDF8
    ),
    NEON_LOUNGE(
        id = "NEON_LOUNGE",
        titleEn = "Neon Lounge",
        titleAr = "صالون نيون (Neon Lounge)",
        emoji = "🌆",
        vibeSubtitleAr = "إضاءة نيون عصرية نابضة بالحياة",
        gradientStartHex = 0xFF1A0926,
        gradientMidHex = 0xFF2E1042,
        gradientEndHex = 0xFF4A154B,
        cardSurfaceHex = 0xFF2B0F3E,
        stageSurfaceHex = 0xFF1D092B,
        accentHex = 0xFFF472B6
    ),
    DESERT_CAMPFIRE(
        id = "DESERT_CAMPFIRE",
        titleEn = "Campfire Oasis",
        titleAr = "سمر الصحراء (Campfire Oasis)",
        emoji = "🔥",
        vibeSubtitleAr = "دفء نيران السمر تحت نجوم السماء",
        gradientStartHex = 0xFF240F0B,
        gradientMidHex = 0xFF3B1810,
        gradientEndHex = 0xFF522214,
        cardSurfaceHex = 0xFF33150E,
        stageSurfaceHex = 0xFF220D09,
        accentHex = 0xFFFB923C
    ),
    FOREST_BREEZE(
        id = "FOREST_BREEZE",
        titleEn = "Forest Breeze",
        titleAr = "نسيم الطبيعة (Forest Breeze)",
        emoji = "🌲",
        vibeSubtitleAr = "استرخاء الطبيعة الخضراء والهدوء النفسي",
        gradientStartHex = 0xFF071F18,
        gradientMidHex = 0xFF0D3328,
        gradientEndHex = 0xFF14493A,
        cardSurfaceHex = 0xFF0F382C,
        stageSurfaceHex = 0xFF09241C,
        accentHex = 0xFF34D399
    );

    companion object {
        fun resolveTheme(themeId: String?): WanasRoomAmbientTheme {
            if (themeId.isNullOrBlank()) return ROYAL_NIGHT
            return entries.find {
                it.id.equals(themeId, ignoreCase = true) ||
                    it.titleEn.equals(themeId, ignoreCase = true)
            } ?: ROYAL_NIGHT
        }
    }
}

/**
 * Sound effect item inside active voice rooms.
 */
data class RoomStageSoundEffect(
    val id: String,
    val labelAr: String,
    val emoji: String,
    val bannerMessage: String
)

/**
 * Comprehensive Admin Dashboard metrics for the app owner.
 */
data class WanasAdminDashboardStats(
    val totalUsers: Int = 1480,
    val onlineUsersNow: Int = 94,
    val activeUsersToday: Int = 342,
    val weeklyActiveUsersWau: Int = 910,
    val monthlyActiveUsersMau: Int = 1480,
    val newUsersToday: Int = 58,
    val returningUsersToday: Int = 284,
    val retentionRatePercent: Int = 86,
    val peakActivityHoursAr: String = "9 مساءً – 2 صباحاً (ذروة السهرة)",
    val totalMatchSessions: Int = 920,
    val avgSessionMinutes: Double = 8.7,
    val avgRoomDurationMinutes: Double = 42.5,
    val completedSessionsRatePercent: Int = 89,
    val totalActiveRooms: Int = 8,
    val activeSpeakersCountNow: Int = 1,
    val topActiveRoomName: String = "🌙 سهرة مصرية — الناس للناس",
    val topActiveMemberName: String = "عضو ونس",
    val activeHostsCount: Int = 1,
    val coinsSoldTotal: Int = 64500,
    val coinsGrantedTotal: Int = 4200,
    val coinsSpentTotal: Int = 38900,
    val topSellingCoinPackageAr: String = "باقة 500 Coin (80 ج.م)",
    val totalGiftsSentCount: Int = 640,
    val activePremiumSubscriptions: Int = 124,
    val dailyRevenueEgp: Int = 1850,
    val weeklyRevenueEgp: Int = 11400,
    val monthlyRevenueEgp: Int = 46800,
    val adRevenueEgp: Int = 4850,
    val giftAndRoomRevenueCoins: Int = 18200,
    val pendingPaymentsCount: Int = 2,
    val approvedPaymentsCount: Int = 138,
    val rejectedPaymentsCount: Int = 4,
    val openReportsCount: Int = 3,
    val bannedUsersCount: Int = 6,
    val suspendedUsersCount: Int = 9,
    val globalAnnouncementBanner: String = "🎉 أهلاً بكم في الموسم الجديد من وَنَس — الناس للناس!",
    val isRoomCreationOpenForAll: Boolean = true,
    val isStrictProfanityFilterActive: Boolean = true,
    val isDoubleCoinsFestivalActive: Boolean = false,
    val defaultFreeSessionMinutes: Int = 10
) {
    val currentSpeakersNow: Int
        get() = activeSpeakersCountNow
    val mostActiveMemberName: String
        get() = topActiveMemberName
    val peakActivityHourLabel: String
        get() = peakActivityHoursAr
}

object WanasCatalogData {
    const val DEFAULT_FADFADA_PIN = "2026"

    val coinPackages = listOf(
        CoinPackage(
            packageId = "coins_100",
            coinsAmount = 100,
            priceEgp = 20,
            bonusLabel = "باقة البداية السريعة",
            badgeEmoji = "🪙"
        ),
        CoinPackage(
            packageId = "coins_500",
            coinsAmount = 500,
            priceEgp = 80,
            bonusLabel = "توفير 20% + أولوية مطابقة",
            badgeEmoji = "💰"
        ),
        CoinPackage(
            packageId = "coins_1200",
            coinsAmount = 1200,
            priceEgp = 150,
            bonusLabel = "أفضل قيمة + شارة داعم ذهبي",
            badgeEmoji = "👑"
        )
    )

    val digitalGifts = listOf(
        WanasGiftItem(
            giftId = "gift_heart",
            nameAr = "قلب متوهج",
            emoji = "❤️",
            costCoins = 5,
            hostShareCoins = 3,
            effectLabelAr = "قلوب حمراء متطايرة على المسرح ✨ (+15 نقطة PK)",
            tierLabelAr = "شائع"
        ),
        WanasGiftItem(
            giftId = "gift_rose",
            nameAr = "وردة تيك توك",
            emoji = "🌹",
            costCoins = 10,
            hostShareCoins = 6,
            effectLabelAr = "بتلات ورد مع إشعار ترحيبي 🌹 (+30 نقطة PK)",
            tierLabelAr = "أنيق"
        ),
        WanasGiftItem(
            giftId = "gift_rose_vip",
            nameAr = "بوكيه ورد فاخر",
            emoji = "💐",
            costCoins = 25,
            hostShareCoins = 15,
            effectLabelAr = "إضاءة وردية وإبراز اسم الداعم 🌸 (+75 نقطة PK)",
            tierLabelAr = "مميز"
        ),
        WanasGiftItem(
            giftId = "gift_star",
            nameAr = "نجمة المسرح الذهبية",
            emoji = "⭐",
            costCoins = 50,
            hostShareCoins = 30,
            effectLabelAr = "شهاب ذهبي يضيء الغرفة بالكامل 🌟 (+150 نقطة PK)",
            tierLabelAr = "نادر"
        ),
        WanasGiftItem(
            giftId = "gift_perfume_royal",
            nameAr = "عطر الماس الملكي",
            emoji = "💎",
            costCoins = 75,
            hostShareCoins = 48,
            effectLabelAr = "رذاذ ألماسي براق يملأ المسرح ✨ (+220 نقطة PK)",
            tierLabelAr = "فاخر"
        ),
        WanasGiftItem(
            giftId = "gift_crown",
            nameAr = "التاج الملكي",
            emoji = "👑",
            costCoins = 100,
            hostShareCoins = 65,
            effectLabelAr = "مهرجان ملكي وشارة داعم VIP 👑 (+300 نقطة PK)",
            tierLabelAr = "ملكي"
        ),
        WanasGiftItem(
            giftId = "gift_diamond_car",
            nameAr = "سيارة الماس الخارقة",
            emoji = "🏎️",
            costCoins = 250,
            hostShareCoins = 165,
            effectLabelAr = "دخول أسطوري بصوت محرك وإضاءة نيون 🔥 (+750 نقطة PK)",
            tierLabelAr = "أسطوري"
        ),
        WanasGiftItem(
            giftId = "gift_lion_throne",
            nameAr = "أسد اللايف الذهبي",
            emoji = "🦁",
            costCoins = 500,
            hostShareCoins = 340,
            effectLabelAr = "زئير الأسد الملكي وتاج الصدارة لكل الغرف ⚡ (+1500 نقطة PK)",
            tierLabelAr = "خرافي"
        ),
        WanasGiftItem(
            giftId = "gift_wanas_universe",
            nameAr = "مجرة وصاروخ وَنَس",
            emoji = "🚀",
            costCoins = 1000,
            hostShareCoins = 700,
            effectLabelAr = "انطلاق صاروخ المجرة وإعلان عام في كل الغرف 🌌 (+3000 نقطة PK)",
            tierLabelAr = "كوني"
        )
    )

    val sessionMiniGames = listOf(
        SessionMiniGame(
            gameId = "game_true_false",
            titleAr = "صح أم خطأ",
            emoji = "✅",
            descriptionAr = "تحدي معلومات سريعة لكسر الجليد أثناء المكالمة",
            questions = listOf(
                MiniGamePrompt("نهر النيل هو أطول نهر في العالم؟", listOf("صح ✅", "خطأ ❌"), 0),
                MiniGamePrompt("القهوة اكتُشفت لأول مرة في البرازيل؟", listOf("صح ✅", "خطأ ❌"), 1),
                MiniGamePrompt("الصوت ينتقل في الماء أسرع من الهواء؟", listOf("صح ✅", "خطأ ❌"), 0)
            )
        ),
        SessionMiniGame(
            gameId = "game_general_trivia",
            titleAr = "أسئلة عامة",
            emoji = "🧠",
            descriptionAr = "أسئلة ثقافية وترفيهية ممتعة بين الطرفين",
            questions = listOf(
                MiniGamePrompt("ما هي عاصمة مصر القديمة في عهد الدولة القديمة؟", listOf("منف (ممفيس)", "الإسكندرية", "أسوان"), 0),
                MiniGamePrompt("كم عدد لاعبي فريق كرة القدم داخل الملعب؟", listOf("9 لاعبين", "11 لاعباً", "12 لاعباً"), 1),
                MiniGamePrompt("أي كوكب يُعرف بالكوكب الأحمر؟", listOf("الزهرة", "المريخ", "المشتري"), 1)
            )
        ),
        SessionMiniGame(
            gameId = "game_guess_character",
            titleAr = "خمن الشخصية",
            emoji = "🕵️",
            descriptionAr = "وصف سريع وعليك تخمين الشخصية الشهيرة",
            questions = listOf(
                MiniGamePrompt("أديب مصري عالمي حصل على جائزة نوبل في الأدب عام 1988؟", listOf("نجيب محفوظ", "طه حسين", "توفيق الحكيم"), 0),
                MiniGamePrompt("عالم عربي مؤسس علم البصريات؟", listOf("ابن سينا", "الحسن بن الهيثم", "الخوارزمي"), 1),
                MiniGamePrompt("أسطورة كرة قدم مصري يلقب بـ الملك المصري في ليفربول؟", listOf("محمد صلاح", "محمود الخطيب", "أبو تريكة"), 0)
            )
        ),
        SessionMiniGame(
            gameId = "game_word_challenge",
            titleAr = "تحدي الكلمات",
            emoji = "🔤",
            descriptionAr = "كون كلمات أو اختر المعنى الصحيح في ثوانٍ",
            questions = listOf(
                MiniGamePrompt("ما معنى كلمة «وَنَس» في اللهجة المصرية والعربية؟", listOf("الأُلفة والصحبة الطيبة التي تزيل الوحشة", "السفر البعيد", "العمل الشاق"), 0),
                MiniGamePrompt("كلمة تبدأ بحرف (س) وتعني الفرح والبهجة؟", listOf("سرور", "سحاب", "سكون"), 0)
            )
        ),
        SessionMiniGame(
            gameId = "game_who_knows_better",
            titleAr = "مين يعرف الثاني أكثر؟",
            emoji = "🤝",
            descriptionAr = "أسئلة تعارف وتقارب لاكتشاف شخصيتكما",
            questions = listOf(
                MiniGamePrompt("لو عندك يوم إجازة مفاجئ، تفضل تقضيه إزاي؟", listOf("خروجة وسهرة مع أصحاب", "هدوء وقهوة وفيلم بالبيت", "سفر وتغيير جو سريع"), 0),
                MiniGamePrompt("أكثر صفة بتقدرها في الصديق المقرب؟", listOf("الجدعنة وحفظ السر", "خفة الدم والضحك", "الاستماع بدون أحكام"), 0)
            )
        )
    )

    val safetyRulesList = listOf(
        "احترام جميع الأعضاء وعدم استخدام أي ألفاظ جارحة أو مسيئة.",
        "يُمنع منعاً باتاً التنمر أو المضايقة أو طلب بيانات حساسة.",
        "التطبيق مخصص للتواصل الاجتماعي والونس للبالغين (+18) في بيئة محترمة.",
        "قسم «وَنَس» ليس عيادة نفسية ولا بديلاً عن الطبيب أو المختص النفسي.",
        "أي مخالفة لقواعد الأمان تعرض الحساب للحظر الفوري من قِبل الإدارة."
    )

    val roomStageSoundEffects = listOf(
        RoomStageSoundEffect("sfx_applause", "تصفيق حار", "👏", "👏 تفاعل الجمهور بتصفيق حار على المسرح!"),
        RoomStageSoundEffect("sfx_zaghrouta", "زغروطة فرح", "🎉", "🎉 زغروطة مصرية مبهجة أشعلت أجواء الغرفة!"),
        RoomStageSoundEffect("sfx_laugh", "ضحكة عالية", "😂", "😂 موجة ضحك وفرفشة في مسرح الغرفة!"),
        RoomStageSoundEffect("sfx_drum", "طبلة حماسية", "🥁", "🥁 دقات طبلة حماسية ترحيباً بالمتحدثين!"),
        RoomStageSoundEffect("sfx_fire", "حماس ناري", "🔥", "🔥 المسرح يشتعل حماساً وتفاعلاً الآن!")
    )

    val roomVoiceChallenges = listOf(
        "🎤 تحدي المايك: احكي موقف مضحك حصل معاك ومستحيل تنساه في دقيقة!",
        "🎤 سؤال السهرة: إيه أكثر أغنية أو فيلم بتحبه ولماذا؟",
        "🎤 تحدي الصراحة: إيه أحلى نصيحة سمعتها في حياتك وفرقت معاك؟",
        "🎤 تحدي اللهجات: قول جملة مشهورة بلهجة محافظة أو بلد تحبها!",
        "🎤 سؤال الونس: لو معاك تذكرة سفر مجانية دلوقتي، تختار تسافر فين ومع مين؟"
    )

    val dailyRewardSchedule = listOf(
        DailyRewardTier(dayNumber = 1, coinsReward = 20, xpReward = 15, labelAr = "اليوم 1: 20 عملة 🪙"),
        DailyRewardTier(dayNumber = 2, coinsReward = 30, xpReward = 20, labelAr = "اليوم 2: 30 عملة 🪙"),
        DailyRewardTier(dayNumber = 3, coinsReward = 50, xpReward = 30, labelAr = "اليوم 3: 50 عملة 🪙"),
        DailyRewardTier(dayNumber = 4, coinsReward = 60, xpReward = 35, labelAr = "اليوم 4: 60 عملة 🪙"),
        DailyRewardTier(dayNumber = 5, coinsReward = 75, xpReward = 40, labelAr = "اليوم 5: 75 عملة 🪙"),
        DailyRewardTier(dayNumber = 6, coinsReward = 100, xpReward = 50, labelAr = "اليوم 6: 100 عملة 🪙"),
        DailyRewardTier(dayNumber = 7, coinsReward = 150, xpReward = 100, labelAr = "اليوم 7: هدية مجانية نادرة 🎁 + 150 عملة", freeGiftId = "gift_crown")
    )

    val defaultDailyMissions = listOf(
        WanasDailyMission(
            missionId = "mission_enter_room_10m",
            titleAr = "ادخل غرفة صوتية وتفاعل لمدة 10 دقائق",
            emoji = "🎙️",
            rewardCoins = 25,
            rewardXp = 30,
            targetCount = 1
        ),
        WanasDailyMission(
            missionId = "mission_send_message",
            titleAr = "أرسل رسالة في غرفة أو محادثة صديق",
            emoji = "💬",
            rewardCoins = 15,
            rewardXp = 20,
            targetCount = 1
        ),
        WanasDailyMission(
            missionId = "mission_add_friend",
            titleAr = "أضف صديقاً جديداً إلى قائمتك",
            emoji = "👥",
            rewardCoins = 30,
            rewardXp = 35,
            targetCount = 1
        ),
        WanasDailyMission(
            missionId = "mission_send_gift",
            titleAr = "أرسل هدية لدعم مضيف أو صديق",
            emoji = "🎁",
            rewardCoins = 40,
            rewardXp = 50,
            targetCount = 1
        ),
        WanasDailyMission(
            missionId = "mission_request_mic_participate",
            titleAr = "شارك في غرفة بطلب المايك أو الجلوس على مقعد",
            emoji = "🙋",
            rewardCoins = 35,
            rewardXp = 45,
            targetCount = 1
        )
    )

    const val PRIMARY_APP_OWNER_EMAIL = "hamadanagy1979@gmail.com"

    val fadfadaReactionTypes = listOf(
        "🤍 قلبي معك",
        "🌱 هانت بإذن الله",
        "🤝 لست وحدك",
        "💡 نصيحة من ذهب"
    )

    val specialCosmeticItems = listOf(
        SpecialStoreCosmeticItem(
            itemId = "special_id_777777",
            titleAr = "رقم معرّف ملكي WNS-777777",
            categoryAr = "رقم معرّف مميز",
            previewBadge = "WNS-777777",
            emoji = "💎",
            costCoins = 200,
            descriptionAr = "رقم معرّف VIP نادر يظهر بجانب اسمك في الغرف والفضفضة والمقاعد"
        ),
        SpecialStoreCosmeticItem(
            itemId = "special_id_100001",
            titleAr = "رقم النخبة الأول WNS-100001",
            categoryAr = "رقم معرّف مميز",
            previewBadge = "WNS-100001",
            emoji = "👑",
            costCoins = 300,
            descriptionAr = "معرّف النخبة الذهبي المميز لكبار داعمي ونجوم وَنَس"
        ),
        SpecialStoreCosmeticItem(
            itemId = "frame_neon_gold",
            titleAr = "إطار المقعد الذهبي المضيء",
            categoryAr = "إطار مقعد مضيء",
            previewBadge = "✨ إطار ذهبي ملكي",
            emoji = "🌟",
            costCoins = 120,
            descriptionAr = "يحيط بمقعدك الصوتي في الغرف بهالة ذهبية متوهجة تلفت الأنظار"
        ),
        SpecialStoreCosmeticItem(
            itemId = "entry_royal_car",
            titleAr = "دخول الأسطورة بسيارة نيون",
            categoryAr = "تأثير دخول ملكي",
            previewBadge = "🏎️ دخول أسطوري",
            emoji = "🏎️",
            costCoins = 150,
            descriptionAr = "إشعار دخول فاخر يظهر لجميع الموجودين فور دخولك أي غرفة صوتية"
        )
    )

    val defaultFamiliesClans = listOf(
        WanasFamilyClan(
            clanId = "clan_kings_egypt",
            clanNameAr = "عائلة ملوك السهرة 👑",
            badgeEmoji = "🦁",
            mottoAr = "الجدعنة والونس وأقوى سهرات مصرية",
            leaderName = "مؤسس العائلة 👑",
            membersCount = 1,
            weeklyActivityPoints = 500,
            officialRoomId = "room_wanas_egypt_1"
        ),
        WanasFamilyClan(
            clanId = "clan_nights_arab",
            clanNameAr = "قبيلة نجوم العرب ✨",
            badgeEmoji = "🦅",
            mottoAr = "قلوب صافية وصحبة راقية من كل مكان",
            leaderName = "قائد القبيلة ✨",
            membersCount = 1,
            weeklyActivityPoints = 500,
            officialRoomId = "room_wanas_laugh_2"
        ),
        WanasFamilyClan(
            clanId = "clan_gamers_pk",
            clanNameAr = "تحالف أبطال الـ PK ⚔️",
            badgeEmoji = "🔥",
            mottoAr = "ملوك التحديات والمسابقات الصوتية المباشرة",
            leaderName = "قائد التحالف ⚡",
            membersCount = 1,
            weeklyActivityPoints = 500,
            officialRoomId = "room_wanas_games_3"
        )
    )

    fun resolveUserLevelTitle(level: Int): String = when {
        level >= 50 -> "👑 أسطورة وَنَس (Level $level)"
        level >= 20 -> "💎 عضو VIP (Level $level)"
        level >= 10 -> "🌟 نجم وَنَس (Level $level)"
        level >= 5 -> "🔥 عضو نشيط (Level $level)"
        else -> "🌱 مبتدئ ونس (Level $level)"
    }
}

/**
 * 👑 أولاً: هيكل أقسام لوحة تحكم صاحب التطبيق الأساسي (12 قسماً منفصلاً عن أدمن الغرفة):
 * 👑 صاحب التطبيق
 * ├── 📊 الإحصائيات
 * ├── 👥 الأعضاء
 * ├── 🏠 جميع الغرف
 * ├── 💰 المدفوعات والعملات
 * ├── 🎁 الهدايا
 * ├── 🛡️ المشرفون
 * ├── 📩 الشكاوى
 * ├── 📢 الإعلانات
 * ├── 🚨 الأمان
 * ├── 📝 سجل العمليات
 * ├── ⚙️ إعدادات النظام
 * └── 🔧 النسخ الاحتياطي والصيانة
 */
enum class OwnerDashboardTab(val id: String, val titleAr: String, val emoji: String) {
    STATS_AND_ANALYTICS("STATS_AND_ANALYTICS", "الإحصائيات والتحليلات", "📊"),
    MEMBERS_MANAGEMENT("MEMBERS_MANAGEMENT", "الأعضاء", "👥"),
    ALL_ROOMS_CONTROL("ALL_ROOMS_CONTROL", "جميع الغرف", "🏠"),
    PAYMENTS_AND_LEDGER("PAYMENTS_AND_LEDGER", "المدفوعات والعملات", "💰"),
    GIFTS_CONTROL("GIFTS_CONTROL", "الهدايا", "🎁"),
    ROLES_AND_ADMINS("ROLES_AND_ADMINS", "المشرفون والصلاحيات", "🛡️"),
    SUPPORT_TICKETS("SUPPORT_TICKETS", "الشكاوى والدعم", "📩"),
    ANNOUNCEMENTS_PUSH("ANNOUNCEMENTS_PUSH", "الإعلانات والإشعارات", "📢"),
    SECURITY_ANTI_ABUSE("SECURITY_ANTI_ABUSE", "الأمان ومكافحة المشاكل", "🚨"),
    AUDIT_LOGS("AUDIT_LOGS", "سجل العمليات", "📝"),
    SYSTEM_SETTINGS("SYSTEM_SETTINGS", "إعدادات النظام", "⚙️"),
    BACKUP_MAINTENANCE("BACKUP_MAINTENANCE", "النسخ الاحتياطي والصيانة", "🔧")
}

enum class OwnerDashboardModuleTab(val id: String, val titleAr: String, val emoji: String) {
    DASHBOARD("DASHBOARD", "مركز التحكم الرئيسي", "📊"),
    MEMBERS("MEMBERS", "إدارة الأعضاء", "👥"),
    ROOMS("ROOMS", "جميع الغرف", "🏠"),
    ADMINS_ROLES("ADMINS_ROLES", "المشرفون والصلاحيات", "🛡️"),
    COINS_PAYMENTS("COINS_PAYMENTS", "المدفوعات والعملات", "💰"),
    GIFTS_STORE("GIFTS_STORE", "الهدايا والمتجر", "🎁"),
    SUPPORT_REPORTS("SUPPORT_REPORTS", "الشكاوى والدعم", "📩"),
    ANNOUNCEMENTS("ANNOUNCEMENTS", "الإعلانات والتنبيهات", "📢"),
    SECURITY_ANTI_ABUSE("SECURITY_ANTI_ABUSE", "الأمان ومكافحة الإساءة", "🚨"),
    AUDIT_LOGS("AUDIT_LOGS", "سجل العمليات", "📝"),
    ANALYTICS_REPORTS("ANALYTICS_REPORTS", "التقارير المتقدمة", "📈"),
    SYSTEM_SETTINGS("SYSTEM_SETTINGS", "إعدادات النظام والصيانة", "⚙️")
}

enum class RoomAdminModuleTab(val id: String, val titleAr: String, val emoji: String) {
    ROOM_OVERVIEW("ROOM_OVERVIEW", "نظرة عامة", "🏠"),
    ROOM_SETTINGS("ROOM_SETTINGS", "إعدادات الغرفة", "⚙️"),
    ROOM_MEMBERS("ROOM_MEMBERS", "الأعضاء والرتب", "👥"),
    MIC_REQUESTS("MIC_REQUESTS", "طلبات المايك", "✋"),
    SEATS_MANAGEMENT("SEATS_MANAGEMENT", "المقاعد", "🪑"),
    SPEAKERS_STAGE("SPEAKERS_STAGE", "المتحدثون الآن", "🎙️"),
    CHAT_CONTROL("CHAT_CONTROL", "الشات والفلترة", "💬"),
    GIFTS_CONTROL("GIFTS_CONTROL", "الهدايا", "🎁"),
    PINNED_ANNOUNCEMENT("PINNED_ANNOUNCEMENT", "الإعلان والترحيب", "📌"),
    ROOM_RULES("ROOM_RULES", "قوانين الغرفة", "📜"),
    ROOM_STATS("ROOM_STATS", "إحصائيات الغرفة", "📊")
}

/**
 * 🎙️ ثانياً: هيكل أقسام لوحة تحكم أدمن الغرفة (لوحة مخصصة للغرفة فقط منفصلة عن إعدادات النظام):
 * 🎙️ إدارة الغرفة
 * ├── 👥 الأعضاء
 * ├── ✋ طلبات المايك
 * ├── 🪑 المقاعد
 * ├── 🔇 الكتم
 * ├── 🚫 الحظر والطرد
 * ├── 🛡️ المساعدون
 * ├── 💬 الشات
 * ├── 🎁 الهدايا
 * ├── 📌 الإعلان المثبت
 * ├── 📜 قوانين الغرفة
 * └── 📊 إحصائيات الغرفة
 */
enum class RoomAdminDashboardTab(val id: String, val titleAr: String, val emoji: String) {
    ROOM_MEMBERS("ROOM_MEMBERS", "الأعضاء", "👥"),
    MIC_REQUESTS("MIC_REQUESTS", "طلبات المايك", "✋"),
    SEATS_CONTROL("SEATS_CONTROL", "المقاعد", "🪑"),
    MUTE_CONTROL("MUTE_CONTROL", "الكتم", "🔇"),
    BAN_AND_KICK("BAN_AND_KICK", "الحظر والطرد", "🚫"),
    CO_HOSTS("CO_HOSTS", "المساعدون", "🛡️"),
    ROOM_CHAT("ROOM_CHAT", "الشات", "💬"),
    ROOM_GIFTS("ROOM_GIFTS", "الهدايا", "🎁"),
    PINNED_BANNER("PINNED_BANNER", "الإعلان المثبت", "📌"),
    ROOM_RULES("ROOM_RULES", "قوانين الغرفة", "📜"),
    ROOM_STATS("ROOM_STATS", "إحصائيات الغرفة", "📊")
}

/**
 * 🛡️ نظام الصلاحيات الحقيقي (Roles System) بدل زر Admin واحد:
 * Owner, Super Admin, Room Admin, Moderator, Support, Member.
 */
enum class WanasAdminRoleType(
    val id: String,
    val titleAr: String,
    val badgeEmoji: String,
    val descriptionAr: String,
    val defaultPermissions: Set<String>
) {
    OWNER(
        id = "OWNER",
        titleAr = "Owner (صاحب التطبيق الأساسي)",
        badgeEmoji = "👑",
        descriptionAr = "كل الصلاحيات الكاملة على التطبيق، السيرفر، العملات، الغرف، وإعدادات النظام",
        defaultPermissions = setOf(
            "MANAGE_USERS", "MANAGE_ROOMS", "MANAGE_COINS_LEDGER", "MANAGE_GIFTS",
            "MANAGE_ROLES", "MANAGE_SUPPORT", "SEND_GLOBAL_ALERTS", "VIEW_SECURITY_CENTER",
            "VIEW_AUDIT_LOGS", "SYSTEM_SETTINGS", "BACKUP_MAINTENANCE"
        )
    ),
    SUPER_ADMIN(
        id = "SUPER_ADMIN",
        titleAr = "Super Admin (مشرف عام)",
        badgeEmoji = "🌟",
        descriptionAr = "إدارة المستخدمين والغرف والشكاوى، بدون إعدادات النظام الحساسة",
        defaultPermissions = setOf("MANAGE_USERS", "MANAGE_ROOMS", "MANAGE_SUPPORT", "VIEW_AUDIT_LOGS")
    ),
    ROOM_ADMIN(
        id = "ROOM_ADMIN",
        titleAr = "Room Admin (أدمن غرفة)",
        badgeEmoji = "🎙️",
        descriptionAr = "إدارة الغرف الصوتية والمقاعد وطلبات المايك فقط",
        defaultPermissions = setOf("MANAGE_ROOMS")
    ),
    MODERATOR(
        id = "MODERATOR",
        titleAr = "Moderator (مراقب)",
        badgeEmoji = "🛡️",
        descriptionAr = "كتم / طرد / حظر المخالفين حسب الصلاحية المحددة",
        defaultPermissions = setOf("MUTE_KICK_BAN")
    ),
    SUPPORT(
        id = "SUPPORT",
        titleAr = "Support (الدعم والشكاوى)",
        badgeEmoji = "🎧",
        descriptionAr = "مراجعة الشكاوى والرد على الأعضاء والدعم الفني فقط",
        defaultPermissions = setOf("MANAGE_SUPPORT")
    )
}

data class AdminRoleAssignment(
    val assignmentId: String,
    val memberId: String,
    val memberName: String,
    val roleType: WanasAdminRoleType,
    val customPermissions: Set<String> = roleType.defaultPermissions,
    val assignedBy: String = "صاحب التطبيق الأساسي",
    val timestampLabel: String = "الآن"
)

/**
 * 📝 سجل العمليات الإدارية (Audit Log):
 * من قام بها -> ماذا فعل -> على من -> أين -> متى -> النتيجة.
 */
data class AdminAuditLogEntry(
    val logId: String,
    val actorName: String,
    val actorRole: String,
    val actionTaken: String,
    val targetName: String,
    val locationRoomOrSection: String,
    val timestampLabel: String,
    val resultSummary: String
)

/**
 * 🚨 نظام مكافحة المشاكل وكشف السلوك المشبوه (Anti-Abuse & Security Alerts):
 * 🔴 خطر مرتفع | 🟠 يحتاج مراجعة | 🟢 طبيعي
 */
enum class SecurityRiskLevel(val id: String, val labelAr: String, val badgeEmoji: String) {
    HIGH_RISK("HIGH_RISK", "خطر مرتفع", "🔴"),
    NEEDS_REVIEW("NEEDS_REVIEW", "يحتاج مراجعة", "🟠"),
    NORMAL("NORMAL", "طبيعي", "🟢");

    val emoji: String
        get() = badgeEmoji
}

data class SuspiciousBehaviorAlert(
    val alertId: String,
    val categoryAr: String, // حسابات متعددة، إرسال عملات غير طبيعي، Spam، طلبات مايك آلية، محاولة تجاوز صلاحيات
    val suspectName: String,
    val roomOrTarget: String,
    val riskLevel: SecurityRiskLevel,
    val detailsAr: String,
    val timestampLabel: String = "منذ دقائق",
    val isResolved: Boolean = false
) {
    val descriptionAr: String
        get() = detailsAr
}

/**
 * 💰 سجل عمليات العملات والمدفوعات المسجل من السيرفر (Server-Side Coins Ledger):
 * يمنع تعديل العملات العشوائي من الواجهة، ويسجل كل عملية برقم مرجعي وسبب ومعتمد من السيرفر.
 */
data class CoinLedgerRecord(
    val ledgerId: String,
    val targetMemberId: String,
    val targetMemberName: String,
    val deltaCoins: Int,
    val operationTypeAr: String, // شحن باقة معتمدة | خصم إرسال هدية | تعديل إداري موثق بالسيرفر | مكافأة مهمة
    val authorizedBy: String,
    val auditReason: String,
    val paymentStatusAr: String = "مقبول وموثق ✅", // معلق | مقبول | مرفوض
    val timestampLabel: String = "الآن"
)

/**
 * 👥 ملف العضو الكامل في لوحة إدارة الأعضاء لصاحب التطبيق:
 */
enum class ManagedAccountStatus(val id: String, val labelAr: String, val emoji: String) {
    ACTIVE("ACTIVE", "نشط", "🟢"),
    MUTED("MUTED", "مكتوم الصوت", "🔇"),
    FROZEN("FROZEN", "حساب مجمد", "❄️"),
    TEMP_BANNED("TEMP_BANNED", "حظر مؤقت (24 ساعة)", "⏳"),
    PERMANENT_BANNED("PERMANENT_BANNED", "حظر دائم", "🚫"),
    DISABLED("DISABLED", "حساب معطل", "⛔");

    companion object {
        val VOICE_MUTED: ManagedAccountStatus
            get() = MUTED
    }
}

data class ManagedMemberProfile(
    val memberId: String,
    val displayName: String,
    val emailOrCode: String,
    val avatarEmoji: String = "👤",
    val roleType: WanasAdminRoleType? = null,
    val accountStatus: ManagedAccountStatus = ManagedAccountStatus.ACTIVE,
    val coinsBalance: Int = 120,
    val currentRoomName: String = "🌙 سهرة مصرية — الناس للناس",
    val activeDeviceSession: String = "Android • جلسة موثقة نشطة الآن",
    val violationsHistory: List<String> = emptyList(),
    val roomsEnteredHistory: List<String> = listOf("🌙 سهرة مصرية — الناس للناس", "😂 دردشة وضحك وهزار"),
    val giftsAndCoinsLog: List<String> = listOf("رصيد ابتدائي: 120 عملة")
) {
    val activeDeviceSessions: List<String>
        get() = listOf(activeDeviceSession)
}

/**
 * 📩 تذكرة مركز الشكاوى والدعم الفني المتقدم:
 * الحالات: جديد -> قيد المراجعة -> تم الحل -> مغلق، مع الرد المباشر على العضو.
 */
enum class SupportTicketStatus(val id: String, val labelAr: String, val emoji: String) {
    NEW("NEW", "جديد", "🆕"),
    IN_REVIEW("IN_REVIEW", "قيد المراجعة", "🔎"),
    RESOLVED("RESOLVED", "تم الحل", "✅"),
    CLOSED("CLOSED", "مغلق", "🔒")
}

/**
 * ⚙️ إعدادات النظام الشاملة الخاصة بصاحب التطبيق فقط:
 */
data class WanasSystemSettings(
    val appDisplayName: String = "وَنَس — الناس للناس",
    val appTagline: String = "اتكلم • اسمع • اتعرف في مجتمع صوتي راقي",
    val appVersionLabel: String = "v18.0 Production",
    val isMaintenanceMode: Boolean = false,
    val maintenanceBannerText: String = "📢 سيتم إجراء صيانة دورية لتحديث السيرفرات الساعة 2 صباحاً.",
    val maxRoomsAllowed: Int = 50,
    val maxSeatsPerRoom: Int = 8,
    val giftsEnabledGlobally: Boolean = true,
    val coinsPurchasesEnabled: Boolean = true,
    val registrationOpen: Boolean = true,
    val directMessagesEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val bannedWordsCsv: String = "شتيمة,إهانة,تنمر,سبام,احتيال",
    val lastBackupTimestamp: String = "اليوم • نسخة احتياطية سحابية كاملة (Firestore + Supabase)"
)

/**
 * 1. Mic Request Queue Item ("نظام طلب المايك الجديد — أولوية قصوى"):
 * Ordered by queueNumber, server-synced status (PENDING, APPROVED, REJECTED, CANCELLED).
 */
enum class MicRequestStatus(val id: String, val labelAr: String) {
    NONE("NONE", "المايك مغلق تلقائياً عند الدخول"),
    PENDING("PENDING", "✋ في طابور انتظار موافقة المضيف"),
    APPROVED("APPROVED", "✅ تمت الموافقة من السيرفر — المايك متاح لك"),
    REJECTED("REJECTED", "❌ تم رفض طلب المايك"),
    MUTED_BY_HOST("MUTED_BY_HOST", "🔇 تم كتم المايك من المضيف")
}

data class RoomMicRequestItem(
    val requestId: String = "",
    val roomId: String = "",
    val userId: String = "",
    val memberIdCode: String = "",
    val userName: String = "",
    val userEmoji: String = "🙋",
    val queueNumber: Int = 1,
    val status: MicRequestStatus = MicRequestStatus.PENDING,
    val requestedSeatIndex: Int = 1,
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * 2. Real 8-Seat Voice Stage Seat Model ("نظام المقاعد الصوتية الحقيقي بدل مجرد أسماء أعضاء"):
 * 8 seats: Empty, Seated Member 👤, Speaking with audio pulse 🎙️, Mic Request ✋, Muted 🔇, Host 👑, Co-Host 🛡️.
 */
enum class VoiceSeatRoleBadge(val id: String, val labelAr: String, val emoji: String) {
    HOST("HOST", "المضيف", "👑"),
    CO_HOST("CO_HOST", "مساعد المضيف", "🛡️"),
    SPEAKER("SPEAKER", "متحدث معتمد", "🎙️"),
    SEATED_MEMBER("SEATED_MEMBER", "عضو جالس", "👤"),
    EMPTY("EMPTY", "مقعد فارغ", "➕")
}

data class VoiceRoomSeatSlot(
    val seatIndex: Int, // 0 to 7 (8 seats)
    val occupantUserId: String? = null,
    val occupantMemberCode: String? = null,
    val occupantName: String? = null,
    val occupantEmoji: String = "👤",
    val seatRole: VoiceSeatRoleBadge = VoiceSeatRoleBadge.EMPTY,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = true,
    val hasPendingMicRequest: Boolean = false,
    val isSeatLocked: Boolean = false
) {
    val isEmpty: Boolean
        get() = occupantUserId.isNullOrBlank()
}

/**
 * 4. Friend Presence Status ("حالة: 🟢 متصل / 🟡 مشغول / ⚫ غير متصل") & Request state.
 */
enum class FriendPresenceState(val id: String, val labelAr: String, val dotEmoji: String) {
    ONLINE("ONLINE", "متصل الآن", "🟢"),
    BUSY("BUSY", "مشغول في جلسة/غرفة", "🟡"),
    OFFLINE("OFFLINE", "غير متصل", "⚫")
}

/**
 * 7. Gift History & Top Senders/Receivers Record ("سجل الهدايا • أكثر عضو أرسل/استقبل هدايا").
 */
data class GiftTransactionRecord(
    val recordId: String,
    val giftId: String,
    val giftNameAr: String,
    val giftEmoji: String,
    val costCoins: Int,
    val senderId: String,
    val senderName: String,
    val recipientName: String,
    val roomId: String = "",
    val isRareOrAnimated: Boolean = false,
    val isHostSpecialGift: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * 9. 7-Day Daily Rewards Tier ("نظام Daily Rewards كل يوم يدخل المستخدم").
 */
data class DailyRewardTier(
    val dayNumber: Int,
    val coinsReward: Int,
    val xpReward: Int,
    val labelAr: String,
    val freeGiftId: String? = null
)

/**
 * 10. Daily Mission Item ("نظام المهام اليومية").
 */
data class WanasDailyMission(
    val missionId: String,
    val titleAr: String,
    val emoji: String,
    val rewardCoins: Int,
    val rewardXp: Int,
    val currentProgress: Int = 0,
    val targetCount: Int = 1,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean
        get() = currentProgress >= targetCount
}

/**
 * 🧧 صندوق الحظ في الغرفة (Lucky Coin Box / مطر العملات):
 * يرسله المضيف أو عضو VIP في الغرفة ليتقاسم الأعضاء العملات عشوائياً.
 */
data class LuckyCoinBoxState(
    val boxId: String,
    val roomId: String,
    val senderName: String,
    val senderMemberCode: String,
    val totalCoins: Int = 100,
    val remainingCoins: Int = 100,
    val maxClaims: Int = 5,
    val claimedByMemberCodes: List<String> = emptyList(),
    val isExpired: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val canClaim: Boolean
        get() = !isExpired && remainingCoins > 0 && claimedByMemberCodes.size < maxClaims
}

/**
 * 🏆 لوحة صدارة الداعمين داخل الغرفة (Room Top Supporters):
 */
data class RoomTopSupporterItem(
    val rank: Int,
    val memberName: String,
    val memberIdCode: String,
    val avatarEmoji: String,
    val totalCoinsSupported: Int,
    val medalEmoji: String = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "🏅"
    }
)

/**
 * 💬 تعليق داعم برقم المعرّف على منشور الفضفضة (Anonymous Fadfada Comment with Member ID Code):
 */
data class FadfadaSupportComment(
    val commentId: String,
    val postId: String,
    val authorMemberCode: String,
    val commentText: String,
    val reactionBadgeAr: String = "🤍 قلبي معك",
    val timestampLabel: String = "الآن"
)

/**
 * ✨ عنصر متجر الأرقام المميزة وإطارات المقاعد وتأثيرات الدخول (Special IDs, Seat Frames & Entry Effects):
 */
data class SpecialStoreCosmeticItem(
    val itemId: String,
    val titleAr: String,
    val categoryAr: String, // رقم معرّف مميز | إطار مقعد مضيء | تأثير دخول ملكي
    val previewBadge: String,
    val emoji: String,
    val costCoins: Int,
    val descriptionAr: String
)

/**
 * 🛡️ نظام العائلات والقبائل في وَنَس (Wanas Families / Clans):
 */
data class WanasFamilyClan(
    val clanId: String,
    val clanNameAr: String,
    val badgeEmoji: String,
    val mottoAr: String,
    val leaderName: String,
    val membersCount: Int,
    val weeklyActivityPoints: Int,
    val officialRoomId: String = "room_wanas_egypt_1"
)

/**
 * 💳 نظام ربط عمليات الشراء والدفع بلوحة تحكم صاحب التطبيق لتأكيد أو رفض الدفع:
 */
enum class PaymentPurchaseOrderStatus(val id: String, val labelAr: String, val emoji: String) {
    PENDING_OWNER_APPROVAL("PENDING_OWNER_APPROVAL", "بانتظار تأكيد الدفع من الإدارة", "⏳"),
    APPROVED("APPROVED", "تم تأكيد الدفع وتفعيل الباقة", "✅"),
    REJECTED("REJECTED", "مرفوض من الإدارة", "❌");

    companion object {
        val PENDING_ADMIN_CONFIRMATION: PaymentPurchaseOrderStatus
            get() = PENDING_OWNER_APPROVAL
        val CONFIRMED_BY_ADMIN: PaymentPurchaseOrderStatus
            get() = APPROVED
        val REJECTED_BY_ADMIN: PaymentPurchaseOrderStatus
            get() = REJECTED
    }
}

data class PaymentPurchaseOrder(
    val orderId: String,
    val buyerUserId: String,
    val buyerMemberCode: String,
    val buyerName: String,
    val orderType: String, // "COIN_PACKAGE" or "VIP_PLAN"
    val itemId: String,
    val itemTitleAr: String,
    val coinsToCredit: Int = 0,
    val grantVipBadge: Boolean = false,
    val vipBadgeLabel: String = "👑 عضو ونس VIP",
    val amountEgp: Int,
    val paymentMethod: String,
    val transactionReference: String,
    val status: PaymentPurchaseOrderStatus = PaymentPurchaseOrderStatus.PENDING_OWNER_APPROVAL,
    val timestampLabel: String = "الآن",
    val ownerDecisionNote: String = ""
) {
    val packageOrPlanTitle: String
        get() = itemTitleAr
    val paymentMethodAr: String
        get() = paymentMethod
    val referenceNumber: String
        get() = transactionReference
    val adminNoteAr: String
        get() = ownerDecisionNote
}



