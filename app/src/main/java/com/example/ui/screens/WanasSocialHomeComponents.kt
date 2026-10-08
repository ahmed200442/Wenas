package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.ActiveMatchCallSession
import com.example.data.MatchQueueRequest
import com.example.data.MatchRole
import com.example.data.MatchTopicType
import com.example.data.WanasAppSection
import com.example.data.WanasCatalogData
import com.example.data.WanasGiftItem
import com.example.ui.theme.WanasAmberGold
import com.example.ui.theme.WanasCardBg
import com.example.ui.theme.WanasCoralWarm
import com.example.ui.theme.WanasDayDarkEmerald
import com.example.ui.theme.WanasDayDarkHeader
import com.example.ui.theme.WanasDayLightSurface
import com.example.ui.theme.WanasDaySurfaceVariant
import com.example.ui.theme.WanasDayTextPrimary
import com.example.ui.theme.WanasDayTextSecondary
import com.example.ui.theme.WanasEmeraldOnline
import com.example.ui.theme.WanasTealCalm
import com.example.ui.theme.WanasVioletAccent
import com.example.viewmodel.ChatRoomsActionState

/**
 * Attractive Welcome Screen (شاشة ترحيب جذابة) built with Jetpack Compose:
 * - Features the Wanas App Logo ("وَنَس — الناس للناس").
 * - Warm, welcoming Arabic message ("رسالة ترحيبية دافئة").
 * - "ابدأ الاستكشاف" button to transition to the main screen.
 */
@Composable
fun WanasModernWelcomeDialog(
    memberName: String,
    serverStatusLabel: String,
    onStartExploring: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStartExploring,
        containerColor = Color(0xFF160D33),
        shape = RoundedCornerShape(30.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wanas_welcome_dialog")
            ) {
                // App Logo Emblem (شعار التطبيق)
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(2.5.dp, WanasAmberGold),
                    color = Color(0xFF1A103C),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .size(88.dp)
                        .testTag("wanas_welcome_logo")
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_wanas_launcher_logo_1791392363941),
                        contentDescription = "شعار تطبيق ونس",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                    )
                }

                Text(
                    text = "أهلاً بك يا $memberName في عالم «وَنَس» ✨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = WanasAmberGold,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = WanasEmeraldOnline.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, WanasEmeraldOnline)
                ) {
                    Text(
                        text = "وَنَس — الناس للناس • اتكلم • اسمع • اتعرف",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = WanasEmeraldOnline
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Warm welcoming message (رسالة ترحيبية دافئة)
                Text(
                    text = "يسعدنا وجودك معنا! تطبيق «وَنَس» هو مساحتك الدافئة التي تجمع القلوب — ادخل في أي وقت سواء كنت تريد الضحك والدردشة، اللعب، تكوين صداقات راقية، أو حتى الفضفضة مع من يسمعك بقلبه.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.94f),
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🌟 استكشف أقسام وَنَس المميزة:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Text("🗣️ محتاج أتكلم ↔ 👂 عايز أسمع (مطابقة ذكية فورية)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text("🎙️ غرف بث صوتية تفاعلية ومقاعد مايكات حية", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text("🎁 هدايا رقمية فاخرة بتأثيرات مبهرة ومستويات 🏆", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text("🔐 قسم فضفضة محمي بكود سري خاص للأمان والخصوصية", style = MaterialTheme.typography.bodySmall, color = WanasTealCalm)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = WanasEmeraldOnline,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = serverStatusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = WanasEmeraldOnline,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onStartExploring,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WanasAmberGold,
                    contentColor = Color(0xFF1A103C)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("welcome_dialog_start_button")
            ) {
                Text("ابدأ الاستكشاف", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            }
        }
    )
}

@Composable
fun WanasModernSectionDirectoryCard(
    selectedSection: WanasAppSection,
    isDarkMode: Boolean,
    isAdminOwner: Boolean,
    onSelectSection: (WanasAppSection) -> Unit,
    onOpenAdminDashboard: () -> Unit
) {
    val cardBg = if (isDarkMode) Color(0xFF1A1136) else WanasDayDarkHeader

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("wanas_sections_directory_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.5.dp, WanasAmberGold.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🧭 أقسام تطبيق «وَنَس» المنظمة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = WanasAmberGold
                    )
                    Text(
                        text = "كل قسم له صفحته الخاصة بتصميم عصري سريع التنقل",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                if (isAdminOwner) {
                    FilledTonalButton(
                        onClick = onOpenAdminDashboard,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasAmberGold,
                            contentColor = Color(0xFF1A103C)
                        ),
                        modifier = Modifier.testTag("sections_bar_admin_button")
                    ) {
                        Text("👑 لوحة الإدارة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wanas_sections_pills_row")
            ) {
                items(WanasAppSection.entries, key = { it.id }) { sec ->
                    val isSelected = selectedSection == sec
                    Surface(
                        color = if (isSelected) WanasAmberGold else Color.White.copy(alpha = 0.09f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) WanasAmberGold else WanasTealCalm.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .clickable { onSelectSection(sec) }
                            .testTag("section_tab_pill_${sec.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(sec.emoji, style = MaterialTheme.typography.titleMedium)
                            Column {
                                Text(
                                    text = sec.titleAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFF1A103C) else Color.White
                                )
                                Text(
                                    text = sec.subtitleAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color(0xFF1A103C).copy(alpha = 0.8f) else WanasTealCalm
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The 4 Big Hero Choices on the Main Screen + Coins / Level / Admin / Safety quick bar:
 * 1. 🗣️ محتاج أتكلم
 * 2. 👂 عايز أسمع
 * 3. 🎙️ غرف صوتية
 * 4. 👥 أصدقاء
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WanasFourHeroChoicesSection(
    actionState: ChatRoomsActionState,
    isDarkMode: Boolean,
    onSelectSpeakNow: () -> Unit,
    onSelectListenNow: () -> Unit,
    onSelectVoiceRooms: () -> Unit,
    onOpenFriendsHub: () -> Unit,
    onOpenCoinsAndGiftsHub: () -> Unit,
    onOpenSafetyCenter: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onSelectTopicAndStartMatch: (MatchRole, MatchTopicType) -> Unit,
    onAcceptWaitingRequest: (MatchQueueRequest) -> Unit
) {
    val cardBg = if (isDarkMode) WanasCardBg else WanasDayLightSurface
    val textPrimary = if (isDarkMode) Color.White else WanasDayTextPrimary
    val textSub = if (isDarkMode) Color.White.copy(alpha = 0.75f) else WanasDayTextSecondary

    var showListenerQueueList by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("wanas_four_hero_choices_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Server Live Status + Coins + Level + Safety/Admin Strip
        Surface(
            color = if (isDarkMode) Color(0xFF1E143B) else WanasDayDarkHeader,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("server_and_economy_status_bar")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(WanasEmeraldOnline)
                        )
                        Text(
                            text = actionState.serverConnectionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasEmeraldOnline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        color = WanasAmberGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${actionState.userLevelBadge} (${actionState.userXp} XP)",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onOpenCoinsAndGiftsHub,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasAmberGold,
                            contentColor = Color(0xFF1A103C)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("open_coins_store_button")
                    ) {
                        Text(
                            text = "🪙 ${actionState.coinsBalance} Coins • المتجر والهدايا",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenSafetyCenter,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasTealCalm,
                            contentColor = Color(0xFF00201D)
                        ),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("open_safety_center_button")
                    ) {
                        Text(
                            text = "🛡️ الأمان (+18)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Admin Dashboard Button ONLY visible to App Owner / Admin ("لوحة تحكم الادمن لا تظهر للاعضاء")
                    if (actionState.isAdminOwner) {
                        FilledTonalButton(
                            onClick = onOpenAdminDashboard,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WanasVioletAccent,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("open_admin_dashboard_button")
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "لوحة الإدارة",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // The 4 Big Choices Grid ("الشاشة الرئيسية تظهر 4 اختيارات كبيرة")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("four_main_choices_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.5.dp, WanasAmberGold.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🌟 ونس — الناس للناس (اتكلم • اسمع • اتعرف)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary
                        )
                        Text(
                            text = "ادخل في أي وقت للضحك أو الدردشة أو الألعاب أو الفضفضة",
                            style = MaterialTheme.typography.labelSmall,
                            color = textSub
                        )
                    }
                }

                // Row 1: 🗣️ محتاج أتكلم  |  👂 عايز أسمع
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroChoiceTile(
                        emoji = "🗣️",
                        title = "محتاج أتكلم",
                        subtitle = "يبحث فوراً عن شخص اختار «عايز أسمع»",
                        accentColor = WanasEmeraldOnline,
                        isSelected = actionState.selectedMatchRole == MatchRole.NEED_TO_SPEAK && !showListenerQueueList,
                        onClick = {
                            showListenerQueueList = false
                            onSelectSpeakNow()
                        },
                        testTag = "hero_choice_need_to_speak",
                        modifier = Modifier.weight(1f)
                    )

                    HeroChoiceTile(
                        emoji = "👂",
                        title = "عايز أسمع",
                        subtitle = "تظهر لك طلبات المتحدثين لتختار شخصاً",
                        accentColor = WanasAmberGold,
                        isSelected = actionState.selectedMatchRole == MatchRole.WANT_TO_LISTEN || showListenerQueueList,
                        onClick = {
                            showListenerQueueList = true
                            onSelectListenNow()
                        },
                        testTag = "hero_choice_want_to_listen",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: 🎙️ غرف صوتية  |  👥 أصدقاء
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroChoiceTile(
                        emoji = "🎙️",
                        title = "غرف صوتية",
                        subtitle = "سهرة مصرية • ضحك • تعارف • كورة • ألعاب • مذاكرة",
                        accentColor = WanasVioletAccent,
                        isSelected = false,
                        onClick = onSelectVoiceRooms,
                        testTag = "hero_choice_voice_rooms",
                        modifier = Modifier.weight(1f)
                    )

                    HeroChoiceTile(
                        emoji = "👥",
                        title = "أصدقاء (${actionState.friendsList.size})",
                        subtitle = "رسائل • تسجيلات صوتية • حالة الاتصال • مكالمة",
                        accentColor = WanasTealCalm,
                        isSelected = false,
                        onClick = onOpenFriendsHub,
                        testTag = "hero_choice_friends",
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = textSub.copy(alpha = 0.2f))

                // ⭐ Smart Matching Engine: "إنت داخل تعمل إيه؟"
                Text(
                    text = "⭐ نظام المطابقة الذكي — إنت داخل تعمل إيه النهاردة؟",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("match_topics_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MatchTopicType.entries, key = { it.id }) { topic ->
                        val selected = actionState.selectedMatchTopic == topic
                        Surface(
                            color = if (selected) {
                                WanasAmberGold
                            } else if (isDarkMode) {
                                Color.White.copy(alpha = 0.08f)
                            } else {
                                WanasDaySurfaceVariant
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selected) WanasAmberGold else WanasTealCalm.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    onSelectTopicAndStartMatch(actionState.selectedMatchRole, topic)
                                }
                                .testTag("match_topic_chip_${topic.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = topic.emoji)
                                Column {
                                    Text(
                                        text = topic.titleAr,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (selected) Color(0xFF1A103C) else textPrimary
                                    )
                                    Text(
                                        text = topic.subtitleAr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color(0xFF1A103C).copy(alpha = 0.8f) else textSub
                                    )
                                }
                            }
                        }
                    }
                }

                // Instant Match Search Trigger Button
                Button(
                    onClick = {
                        onSelectTopicAndStartMatch(actionState.selectedMatchRole, actionState.selectedMatchTopic)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) WanasEmeraldOnline else WanasDayDarkHeader,
                        contentColor = if (isDarkMode) Color.White else WanasAmberGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_smart_matching_button")
                ) {
                    if (actionState.isSearchingMatch) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = WanasAmberGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🔎 جاري البحث عن شريك مناسب الآن...", fontWeight = FontWeight.ExtraBold)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${actionState.selectedMatchRole.emoji} ابحث الآن (${actionState.selectedMatchRole.labelAr} • ${actionState.selectedMatchTopic.titleAr})",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Found Match Candidate Banner ("🎉 وجدنا لك شخصًا مناسبًا -> 🎙️ ابدأ المكالمة")
                val candidate = actionState.matchFoundCandidate
                if (candidate != null) {
                    Surface(
                        color = WanasDayDarkEmerald,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, WanasAmberGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("match_found_candidate_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🎉 وجدنا لك شخصًا مناسبًا!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(WanasAmberGold.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(candidate.userEmoji, style = MaterialTheme.typography.titleMedium)
                                    }
                                    Column {
                                        Text(
                                            text = "${candidate.userName} (Level ${candidate.userLevel})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${candidate.topic.emoji} ${candidate.topic.titleAr} • متاح للاتصال الصوتي (10 دقائق)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WanasTealCalm
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onAcceptWaitingRequest(candidate) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WanasAmberGold,
                                        contentColor = Color(0xFF1A103C)
                                    ),
                                    modifier = Modifier.testTag("start_matched_voice_call_button")
                                ) {
                                    Text("🎙️ ابدأ المكالمة", fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }

                // Listener Queue ("👂 عايز أسمع: تظهر لك طلبات أشخاص يريدون التحدث وتختار شخصاً")
                AnimatedVisibility(visible = showListenerQueueList || actionState.selectedMatchRole == MatchRole.WANT_TO_LISTEN) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("listener_requests_queue_box")
                    ) {
                        Text(
                            text = "👂 طلبات أشخاص يريدون التحدث الآن (${actionState.waitingListenerRequests.size}):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDarkMode) WanasTealCalm else WanasDayDarkHeader
                        )
                        if (actionState.waitingListenerRequests.isEmpty()) {
                            Surface(
                                color = if (isDarkMode) Color.White.copy(alpha = 0.06f) else WanasDaySurfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("empty_listener_requests_card")
                            ) {
                                Text(
                                    text = "✨ لا توجد طلبات وهمية! اضغط «محتاج أتكلم» أو «ابحث الآن» لتسجيل طلبك الحقيقي وانتظار الأعضاء المتصلين فعلياً.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        actionState.waitingListenerRequests.forEach { req ->
                            Surface(
                                color = if (isDarkMode) Color.White.copy(alpha = 0.07f) else WanasDaySurfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("waiting_speaker_item_${req.requestId}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(req.userEmoji, style = MaterialTheme.typography.titleMedium)
                                        Column {
                                            Text(
                                                text = "${req.userName} • 🏆 Lv.${req.userLevel}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = textPrimary
                                            )
                                            Text(
                                                text = "${req.topic.emoji} ${req.topic.titleAr}: ${req.statusText}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textSub
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onAcceptWaitingRequest(req) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = WanasEmeraldOnline,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("accept_speaker_request_${req.requestId}")
                                    ) {
                                        Text("🎙️ ابدأ الجلسة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroChoiceTile(
    emoji: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) Color(0xFF1F123B) else Color(0xFF1A103C),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) WanasAmberGold else accentColor.copy(alpha = 0.65f)
        ),
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WanasAmberGold
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ActiveTenMinuteMatchCallCard(
    session: ActiveMatchCallSession,
    coinsBalance: Int,
    showInterstitialAd: Boolean,
    onExtendTimeWithCoins: () -> Unit,
    onToggleMutePartner: () -> Unit,
    onSendGift: (WanasGiftItem) -> Unit,
    onStartMiniGame: (String) -> Unit,
    onAnswerGameQuestion: (Int) -> Unit,
    onAddFriend: () -> Unit,
    onRateSession: (Int) -> Unit,
    onStartAnotherSession: () -> Unit,
    onWatchRewardedAd: () -> Unit,
    onBlockPartner: () -> Unit,
    onReportPartner: () -> Unit,
    onEndOrCloseSession: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("active_ten_minute_call_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1B1038))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header + Live 10-Minute Timer ("⏱️ 09:58")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WanasAmberGold.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(session.partnerEmoji, style = MaterialTheme.typography.titleLarge)
                    }
                    Column {
                        Text(
                            text = session.partnerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${session.topic.emoji} ${session.topic.titleAr} • 🏆 Level ${session.partnerLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WanasTealCalm
                        )
                    }
                }

                // Countdown Timer Pill
                Surface(
                    color = if (session.isEnded) WanasCoralWarm else WanasDayDarkEmerald,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, WanasAmberGold),
                    modifier = Modifier.testTag("ten_minute_countdown_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "عداد الجلسة",
                            tint = WanasAmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (session.isEnded) "انتهت الجلسة ❤️" else "⏱️ ${session.formattedCountdown}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            if (session.isEnded) {
                // End-of-Session Screen ("انتهت الجلسة ❤️ -> ⭐ تقييم | 🔄 جلسة جديدة | 👥 إضافة كصديق | 🏠 الرئيسية")
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, WanasAmberGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_ended_summary_box")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "انتهت الجلسة ❤️",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Text(
                            text = "نتمنى أن تكون قضيت وقتاً ممتعاً ومفيداً مع «${session.partnerName}»",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f),
                            textAlign = TextAlign.Center
                        )

                        // ⭐ Rating Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐ تقييم الشريك:", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "تقييم $star",
                                    tint = if (star <= session.ratingGiven) WanasAmberGold else Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable { onRateSession(star) }
                                        .testTag("rate_session_star_$star")
                                )
                            }
                        }

                        // Interstitial Ad Between Sessions (Only for Free Tier, never during voice call!)
                        if (showInterstitialAd) {
                            Surface(
                                color = Color(0xFF2D1950),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, WanasTealCalm),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("between_sessions_ad_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📢 إعلان بين الجلسات (النسخة المجانية)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = WanasAmberGold
                                        )
                                        Text(
                                            text = "شاهد إعلاناً قصيراً واحصل على +25 Coins مجانية أو اشترك في ونس Premium لإزالة الإعلانات",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                    FilledTonalButton(
                                        onClick = onWatchRewardedAd,
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = WanasAmberGold,
                                            contentColor = Color(0xFF1A103C)
                                        ),
                                        modifier = Modifier.testTag("watch_ad_reward_button")
                                    ) {
                                        Text("+25 🪙", fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onAddFriend,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasEmeraldOnline,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("post_session_add_friend_button")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("⭐ أضف لصداقاتك", fontWeight = FontWeight.ExtraBold)
                            }

                            Button(
                                onClick = onStartAnotherSession,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasAmberGold,
                                    contentColor = Color(0xFF1A103C)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("post_session_new_call_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🔄 جلسة أخرى", fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        OutlinedButton(
                            onClick = onEndOrCloseSession,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("post_session_home_button")
                        ) {
                            Text("🏠 العودة للرئيسية", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Active Call Controls: Extend +10m, Add Friend, Mute, Report, Block, End Call
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = onExtendTimeWithCoins,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasAmberGold,
                            contentColor = Color(0xFF1A103C)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("extend_call_time_button")
                    ) {
                        Text("⏱️ +10د (25🪙)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }

                    FilledTonalButton(
                        onClick = onAddFriend,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasTealCalm,
                            contentColor = Color(0xFF00201D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("in_call_add_friend_button")
                    ) {
                        Text("👥 إضافة صديق", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }

                    Button(
                        onClick = onEndOrCloseSession,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WanasCoralWarm,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("end_active_call_button")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إنهاء", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }
                }

                // 🎁 Send Instant Gift Bar during Call ("❤️ قلب 5، 🌹 وردة 10/25، ⭐ نجمة 50، 👑 تاج 100")
                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🎁 أرسل هدية أثناء المحادثة (رصيدك: $coinsBalance Coins):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(WanasCatalogData.digitalGifts, key = { it.giftId }) { gift ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .clickable { onSendGift(gift) }
                                        .testTag("send_call_gift_${gift.giftId}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(gift.emoji)
                                        Text(
                                            text = "${gift.nameAr} (${gift.costCoins}🪙)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 🎮 In-Session Icebreaker Mini-Games ("الألعاب أثناء الجلسة: صح أم خطأ، أسئلة عامة، خمن الشخصية، تحدي الكلمات، مين يعرف الثاني أكثر؟")
                Surface(
                    color = Color.White.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WanasVioletAccent.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("in_session_games_box")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = WanasTealCalm, modifier = Modifier.size(18.dp))
                            Text(
                                text = "🎮 ألعاب الجلسة التفاعلية (اختر لعبة مع شريكك):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasTealCalm
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(WanasCatalogData.sessionMiniGames, key = { it.gameId }) { game ->
                                val active = session.activeGameId == game.gameId
                                Surface(
                                    color = if (active) WanasAmberGold else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .clickable { onStartMiniGame(game.gameId) }
                                        .testTag("select_session_game_${game.gameId}")
                                ) {
                                    Text(
                                        text = "${game.emoji} ${game.titleAr}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (active) Color(0xFF1A103C) else Color.White
                                    )
                                }
                            }
                        }

                        val currentGame = WanasCatalogData.sessionMiniGames.find { it.gameId == session.activeGameId }
                        if (currentGame != null) {
                            val prompt = currentGame.questions.getOrNull(session.currentGameQuestionIndex)
                            if (prompt != null) {
                                Surface(
                                    color = Color(0xFF120B29),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("active_mini_game_prompt_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${currentGame.emoji} ${currentGame.titleAr} (نقاطك: ${session.myGameScore}):",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WanasAmberGold,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = prompt.promptText,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            prompt.options.forEachIndexed { idx, opt ->
                                                OutlinedButton(
                                                    onClick = { onAnswerGameQuestion(idx) },
                                                    border = BorderStroke(1.dp, WanasTealCalm),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("game_option_button_$idx")
                                                ) {
                                                    Text(
                                                        text = opt,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 🛡️ Immediate Safety Row inside Call: 🔇 كتم | ⚠️ إبلاغ | 🚫 حظر
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleMutePartner,
                        border = BorderStroke(1.dp, WanasTealCalm),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("safety_mute_partner_button")
                    ) {
                        Icon(
                            imageVector = if (session.isMutedPartner) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = WanasTealCalm,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (session.isMutedPartner) "إلغاء الكتم" else "🔇 كتم",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onReportPartner,
                        border = BorderStroke(1.dp, WanasAmberGold),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("safety_report_partner_button")
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = WanasAmberGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⚠️ إبلاغ", style = MaterialTheme.typography.labelSmall, color = WanasAmberGold)
                    }

                    OutlinedButton(
                        onClick = onBlockPartner,
                        border = BorderStroke(1.dp, WanasCoralWarm),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("safety_block_partner_button")
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = WanasCoralWarm, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🚫 حظر", style = MaterialTheme.typography.labelSmall, color = WanasCoralWarm)
                    }
                }
            }
        }
    }
}
