package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CoinPackage
import com.example.data.ManagedAccountStatus
import com.example.data.MatchQueueRequest
import com.example.data.MatchRole
import com.example.data.MatchTopicType
import com.example.data.OwnerDashboardModuleTab
import com.example.data.PaymentPurchaseOrderStatus
import com.example.data.SecurityRiskLevel
import com.example.data.WanasAdminRoleType
import com.example.data.WanasCatalogData
import com.example.data.WanasFamilyClan
import com.example.data.WanasFriend
import com.example.data.WanasGiftItem
import com.example.data.SpecialStoreCosmeticItem
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
 * 1. 👥 Friends Hub Screen ("نظام الأصدقاء"):
 * Shows online status, allows sending text messages, sending voice notes, or starting a voice call.
 */
@Composable
fun WanasFriendsHubScreen(
    actionState: ChatRoomsActionState,
    isDarkMode: Boolean,
    onAddFriendByName: (friendName: String) -> Unit = {},
    onRespondToFriendRequest: (friendUserId: String, accept: Boolean) -> Unit = { _, _ -> },
    onRemoveFriend: (friendUserId: String) -> Unit = {},
    onCycleFriendPresence: (friendUserId: String) -> Unit = {},
    onTogglePinOrMuteChat: (friendUserId: String, togglePin: Boolean, toggleMute: Boolean) -> Unit = { _, _, _ -> },
    onDeleteDirectMessage: (friendUserId: String, messageId: String) -> Unit = { _, _ -> },
    onEnterFriendRoomDirect: (roomId: String) -> Unit = {},
    onNotifyFriendStartedNewRoom: (WanasFriend?) -> Unit = {},
    onToggleNotifyFriendStartsRoom: (Boolean) -> Unit = {},
    onSendDirectMessage: (friendUserId: String, text: String, isVoiceNote: Boolean, imageLabel: String?, isBuzz: Boolean) -> Unit,
    onStartVoiceCallWithFriend: (WanasFriend) -> Unit,
    onBlockFriend: (WanasFriend) -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler { onBackToHome() }

    val cardBg = if (isDarkMode) WanasCardBg else WanasDayLightSurface
    val textPrimary = if (isDarkMode) Color.White else WanasDayTextPrimary
    val textSub = if (isDarkMode) Color.White.copy(alpha = 0.72f) else WanasDayTextSecondary

    var selectedFriendId by rememberSaveable {
        mutableStateOf(actionState.friendsList.firstOrNull()?.friendUserId.orEmpty())
    }
    var messageInput by rememberSaveable { mutableStateOf("") }
    var newFriendNameInput by rememberSaveable { mutableStateOf("") }
    var showOnlineOnlyFilter by rememberSaveable { mutableStateOf(false) }

    val displayedFriends = if (showOnlineOnlyFilter) {
        actionState.friendsList.filter { it.isOnline }
    } else {
        actionState.friendsList
    }
    val onlineFriendsCount = actionState.friendsList.count { it.isOnline }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("wanas_friends_screen_root"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isDarkMode) WanasCardBg else WanasDayDarkHeader
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = WanasAmberGold)
                    Column {
                        Text(
                            text = "👥 أصدقاء وَنَس (${actionState.friendsList.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Text(
                            text = "رسائل خاصة • تسجيلات صوتية • حالة الاتصال • بدء مكالمة",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = WanasAmberGold,
                        contentColor = Color(0xFF1A103C)
                    ),
                    modifier = Modifier.testTag("friends_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الرئيسية", fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Add Real Friend Card & Online Now Filter
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_real_friend_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newFriendNameInput,
                        onValueChange = { newFriendNameInput = it },
                        label = { Text("➕ أضف صديقاً بالاسم أو المعرّف") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_friend_name_input")
                    )
                    Button(
                        onClick = {
                            val clean = newFriendNameInput.trim()
                            if (clean.isNotEmpty()) {
                                onAddFriendByName(clean)
                                newFriendNameInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WanasEmeraldOnline,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("confirm_add_friend_button")
                    ) {
                        Text("إضافة صديق", fontWeight = FontWeight.ExtraBold)
                    }
                }

                // Online Friends Now Toggle + Friend New Room Alert Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🟢 المتصلون الآن: $onlineFriendsCount • المجموع: ${actionState.friendsList.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WanasEmeraldOnline
                    )
                    FilledTonalButton(
                        onClick = { showOnlineOnlyFilter = !showOnlineOnlyFilter },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (showOnlineOnlyFilter) WanasEmeraldOnline else WanasTealCalm.copy(alpha = 0.25f),
                            contentColor = if (showOnlineOnlyFilter) Color.White else textPrimary
                        ),
                        modifier = Modifier.testTag("toggle_online_friends_filter")
                    ) {
                        Text(
                            text = if (showOnlineOnlyFilter) "📋 عرض كل الأصدقاء" else "🟢 الأصدقاء المتصلون الآن فقط",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Friend New Room Notification Bar inside Friends Hub
                Surface(
                    color = if (actionState.notifyOnFriendStartsRoom) WanasEmeraldOnline.copy(alpha = 0.14f) else Color.Gray.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (actionState.notifyOnFriendStartsRoom) WanasEmeraldOnline else Color.Gray.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friends_hub_new_room_alert_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onToggleNotifyFriendStartsRoom(!actionState.notifyOnFriendStartsRoom) }
                        ) {
                            Text(
                                text = if (actionState.notifyOnFriendStartsRoom) {
                                    "🎙️ تنبيه غرف الأصدقاء الجديدة: مفعّل ✅"
                                } else {
                                    "🔕 تنبيه غرف الأصدقاء الجديدة: متوقف"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary
                            )
                            Text(
                                text = "إشعار فوري عندما يبدأ صديقك غرفة صوتية جديدة",
                                style = MaterialTheme.typography.labelSmall,
                                color = textSub
                            )
                        }

                        FilledTonalButton(
                            onClick = { onNotifyFriendStartedNewRoom(actionState.friendsList.firstOrNull()) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WanasEmeraldOnline,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("friends_hub_trigger_friend_room_button")
                        ) {
                            Text(
                                text = "🎙️ بدأ غرفة جديدة",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // Incoming Friend Requests Section ("قبول/رفض طلبات الصداقة")
        val pendingFriendRequests = actionState.friendsList.filter { it.isPendingApproval }
        if (pendingFriendRequests.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incoming_friend_requests_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF231442)),
                border = BorderStroke(1.5.dp, WanasAmberGold)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "👋 طلبات صداقة واردة (${pendingFriendRequests.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = WanasAmberGold
                    )
                    pendingFriendRequests.forEach { req ->
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${req.friendEmoji} ${req.friendName} (Level ${req.friendLevel})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { onRespondToFriendRequest(req.friendUserId, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = WanasEmeraldOnline),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("✅ قبول", style = MaterialTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(
                                        onClick = { onRespondToFriendRequest(req.friendUserId, false) },
                                        border = BorderStroke(1.dp, WanasCoralWarm),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("❌ رفض", style = MaterialTheme.typography.labelSmall, color = WanasCoralWarm)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (displayedFriends.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_friends_list_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("👥", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "قائمة أصدقائك الحقيقيين فارغة حالياً",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = textPrimary
                    )
                    Text(
                        text = "أضف أصدقاءك الحقيقيين من الحقل بالأعلى أو بعد انتهاء الجلسات الصوتية للتواصل عبر Messenger وَنَس.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSub
                    )
                }
            }
        }

        displayedFriends.forEach { friend ->
            val isSelected = selectedFriendId == friend.friendUserId
            val friendMessages = actionState.friendMessagesMap[friend.friendUserId].orEmpty()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedFriendId = friend.friendUserId }
                    .testTag("friend_card_${friend.friendUserId}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) WanasAmberGold else WanasTealCalm.copy(alpha = 0.35f)
                )
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
                                Text(friend.friendEmoji, style = MaterialTheme.typography.titleMedium)
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${if (friend.isConversationPinned) "📌 " else ""}${if (friend.isConversationMuted) "🔇 " else ""}${friend.friendName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = textPrimary
                                    )
                                    Surface(
                                        color = WanasEmeraldOnline.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.clickable { onCycleFriendPresence(friend.friendUserId) }
                                    ) {
                                        Text(
                                            text = "${friend.presenceState.dotEmoji} ${friend.presenceState.labelAr}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WanasEmeraldOnline,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "🏆 Level ${friend.friendLevel} • 🕒 ${friend.lastSeenLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textSub
                                )
                                Text(
                                    text = "آخر تفاعل: ${friend.lastMessage}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDarkMode) WanasTealCalm else WanasDayDarkHeader,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onStartVoiceCallWithFriend(friend) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasEmeraldOnline,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.testTag("call_friend_button_${friend.friendUserId}")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "بدء مكالمة", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مكالمة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                            }

                            OutlinedButton(
                                onClick = { onBlockFriend(friend) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, WanasCoralWarm)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = "حظر", tint = WanasCoralWarm, modifier = Modifier.size(15.dp))
                            }
                        }
                    }

                    // Friend Actions Row: Enter Friend's Room Directly, Pin Chat, Mute Chat, Remove Friend
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!friend.currentRoomId.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { onEnterFriendRoomDirect(friend.currentRoomId) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = WanasAmberGold,
                                        contentColor = Color(0xFF1A103C)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("join_friend_room_btn_${friend.friendUserId}")
                                ) {
                                    Text(
                                        text = "🚪 دخول غرفته الآن",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { onNotifyFriendStartedNewRoom(friend) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = WanasEmeraldOnline,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("friend_started_new_room_btn_${friend.friendUserId}")
                                ) {
                                    Text(
                                        text = "🎙️ بدأ غرفة جديدة",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { onTogglePinOrMuteChat(friend.friendUserId, true, false) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = if (friend.isConversationPinned) "📌 إلغاء التثبيت" else "📌 تثبيت",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                            FilledTonalButton(
                                onClick = { onTogglePinOrMuteChat(friend.friendUserId, false, true) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = if (friend.isConversationMuted) "🔔 تفعيل التنبيه" else "🔇 كتم",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                            OutlinedButton(
                                onClick = { onRemoveFriend(friend.friendUserId) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, WanasCoralWarm.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = "🗑️ إزالة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WanasCoralWarm,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Messenger inside Wanas ("Messenger داخل وَنَس: نصية، صور، صوتية، Buzz ⚡، حالة قراءة، حذف")
                    HorizontalDivider(color = textSub.copy(alpha = 0.2f))

                    friendMessages.takeLast(4).forEach { msg ->
                        Surface(
                            color = if (msg.isBuzzAlert) {
                                WanasAmberGold.copy(alpha = 0.22f)
                            } else if (isDarkMode) {
                                Color.White.copy(alpha = 0.06f)
                            } else {
                                WanasDaySurfaceVariant
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${msg.senderName}: ${msg.text}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textPrimary,
                                        fontWeight = if (msg.isBuzzAlert) FontWeight.ExtraBold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "الآن • ${if (msg.isReadByRecipient) "✓✓ تمت القراءة" else "✓ تم الإرسال"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WanasEmeraldOnline
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onDeleteDirectMessage(friend.friendUserId, msg.messageId) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, WanasCoralWarm.copy(alpha = 0.5f)),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("حذف", style = MaterialTheme.typography.labelSmall, color = WanasCoralWarm)
                                }
                            }
                        }
                    }

                    // Messenger Rich Actions Bar: Image 📷, Voice Note 🎙️, Buzz ⚡
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                onSendDirectMessage(friend.friendUserId, "", false, "صورة تذكارية في وَنَس", false)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_image_msg_btn_${friend.friendUserId}")
                        ) {
                            Text("📷 إرسال صورة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = {
                                onSendDirectMessage(friend.friendUserId, "", true, null, false)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WanasTealCalm.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_voice_note_button_${friend.friendUserId}")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "تسجيل صوتي", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسجيل صوتي", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                onSendDirectMessage(friend.friendUserId, "", false, null, true)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasAmberGold,
                                contentColor = Color(0xFF1A103C)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_buzz_btn_${friend.friendUserId}")
                        ) {
                            Text("⚡ Buzz!", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = if (isSelected) messageInput else "",
                            onValueChange = {
                                selectedFriendId = friend.friendUserId
                                messageInput = it
                            },
                            label = { Text("اكتب رسالة في Messenger وَنَس إلى ${friend.friendName}...") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("friend_message_input_${friend.friendUserId}")
                        )

                        Button(
                            onClick = {
                                if (messageInput.isNotBlank()) {
                                    onSendDirectMessage(friend.friendUserId, messageInput, false, null, false)
                                    messageInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasTealCalm,
                                contentColor = Color(0xFF00201D)
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("send_friend_text_button_${friend.friendUserId}")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. 🪙 Coins Store, Digital Gifts, Boosts, 7-Day Daily Rewards, Daily Missions & Premium Screen:
 */
@Composable
fun WanasCoinsAndGiftsScreen(
    actionState: ChatRoomsActionState,
    isDarkMode: Boolean,
    onClaimDailyCoins: () -> Unit,
    onClaimDailyStreakDay: (Int) -> Unit = {},
    onClaimDailyMission: (String) -> Unit = {},
    onSendFreeDailyGift: () -> Unit = {},
    onWatchRewardedAd: () -> Unit,
    onSpinWheelOfFortune: () -> Unit = {},
    onActivateFeature: (title: String, cost: Int, isBoost: Boolean, isPriority: Boolean) -> Unit,
    onSendGift: (WanasGiftItem) -> Unit,
    onPurchaseOrEquipSpecialCosmetic: (SpecialStoreCosmeticItem) -> Unit = {},
    onJoinWanasClan: (WanasFamilyClan) -> Unit = {},
    onCreateWanasClan: (clanName: String, motto: String) -> Unit = { _, _ -> },
    onBuyCoinPackageViaRealPayment: (CoinPackage) -> Unit,
    onOpenVipPaymentScreen: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler { onBackToHome() }

    val cardBg = if (isDarkMode) WanasCardBg else WanasDayLightSurface
    val textPrimary = if (isDarkMode) Color.White else WanasDayTextPrimary
    val textSub = if (isDarkMode) Color.White.copy(alpha = 0.72f) else WanasDayTextSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("wanas_coins_and_gifts_screen_root"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isDarkMode) WanasCardBg else WanasDayDarkHeader
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🪙 محفظة العملات والهدايا و Premium",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Text(
                            text = "رصيدك الحالي: ${actionState.coinsBalance} Coins • ${actionState.userLevelBadge}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = WanasEmeraldOnline
                        )
                    }

                    FilledTonalButton(
                        onClick = onBackToHome,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasAmberGold,
                            contentColor = Color(0xFF1A103C)
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الرئيسية", fontWeight = FontWeight.ExtraBold)
                    }
                }

                // Daily Free Coins + Watch Ad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onClaimDailyCoins,
                        enabled = !actionState.dailyBonusClaimed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WanasEmeraldOnline,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("claim_daily_coins_button")
                    ) {
                        Text(
                            text = if (actionState.dailyBonusClaimed) "✅ استلمت هدية اليوم" else "🎁 عملات اليوم (+35🪙)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    FilledTonalButton(
                        onClick = onWatchRewardedAd,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasTealCalm,
                            contentColor = Color(0xFF00201D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("watch_ad_coins_button")
                    ) {
                        Text(
                            text = "🎬 شاهد إعلاناً (+25🪙)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 🎁 9. 7-Day Daily Rewards Streak ("نظام Daily Rewards: اليوم 1: 20 عملة، اليوم 2: 30، اليوم 3: 50... اليوم 7: هدية مجانية")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_rewards_streak_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.5.dp, WanasAmberGold.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎁 نظام المكافآت اليومية المتتالية (Daily Rewards — 7 أيام):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                )
                Text(
                    text = "⭐ مستواك الحالي: ${actionState.userLevelBadge} (${actionState.userXp} XP) • اليوم المتاح: اليوم ${actionState.currentDailyRewardStreakDay}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = WanasEmeraldOnline
                )

                WanasCatalogData.dailyRewardSchedule.chunked(4).forEach { rowDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowDays.forEach { dayItem ->
                            val isClaimed = dayItem.dayNumber in actionState.claimedDailyRewardDays
                            val isCurrent = dayItem.dayNumber == actionState.currentDailyRewardStreakDay
                            Surface(
                                color = when {
                                    isClaimed -> WanasEmeraldOnline.copy(alpha = 0.22f)
                                    isCurrent -> WanasAmberGold.copy(alpha = 0.28f)
                                    isDarkMode -> Color.White.copy(alpha = 0.06f)
                                    else -> WanasDaySurfaceVariant
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = if (isClaimed) WanasEmeraldOnline else if (isCurrent) WanasAmberGold else textSub.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isClaimed) {
                                        onClaimDailyStreakDay(dayItem.dayNumber)
                                    }
                                    .testTag("daily_reward_day_${dayItem.dayNumber}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (isClaimed) "✅" else if (dayItem.dayNumber == 7) "🎁" else "🪙",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "اليوم ${dayItem.dayNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = if (dayItem.freeGiftId != null) "هدية مجانية" else "+${dayItem.coinsReward}🪙",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isClaimed) WanasEmeraldOnline else WanasAmberGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🎯 10. Daily Missions ("نظام المهام اليومية: ادخل غرفة 10 دقائق، أرسل رسالة، أضف صديقاً، أرسل هدية، شارك في غرفة")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_missions_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯 المهام اليومية (أكمل المهام لربح Coins + نقاط مستوى XP):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary
                )
                actionState.dailyMissions.forEach { mission ->
                    Surface(
                        color = if (isDarkMode) Color.White.copy(alpha = 0.06f) else WanasDaySurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (mission.isCompleted) WanasEmeraldOnline else WanasAmberGold.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_mission_row_${mission.missionId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${mission.emoji} ${mission.titleAr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "المكافأة: +${mission.rewardCoins} Coins • +${mission.rewardXp} XP (${mission.currentProgress}/${mission.targetCount})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WanasEmeraldOnline,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { onClaimDailyMission(mission.missionId) },
                                enabled = !mission.isClaimed,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (mission.isClaimed) WanasTealCalm else WanasAmberGold,
                                    contentColor = Color(0xFF1A103C)
                                ),
                                modifier = Modifier.testTag("claim_mission_btn_${mission.missionId}")
                            ) {
                                Text(
                                    text = if (mission.isClaimed) "✅ تم الاستلام" else "🎁 استلام (+${mission.rewardCoins}🪙)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🎡 Suggestion 1: Daily Wheel of Fortune ("عجلة الحظ اليومية لربح العملات والمستويات")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_wheel_of_fortune_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF241242)),
            border = BorderStroke(1.5.dp, WanasAmberGold)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎡 عجلة الحظ اليومية في وَنَس",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Text(
                            text = actionState.lastWheelRewardText
                                ?: "لف عجلة الحظ مجاناً كل يوم واربح حتى 120 Coins + نقاط مستوى XP!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Button(
                        onClick = onSpinWheelOfFortune,
                        enabled = !actionState.wheelOfFortuneSpunToday,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WanasAmberGold,
                            contentColor = Color(0xFF1A103C)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("spin_wheel_of_fortune_button")
                    ) {
                        Text(
                            text = if (actionState.wheelOfFortuneSpunToday) "✅ تم اللعب اليوم" else "🎡 لف العجلة الآن",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Coin Packages: 100 Coin = 20 EGP, 500 Coin = 80 EGP, 1200 Coin = 150 EGP
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.5.dp, WanasAmberGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "💰 باقات شحن العملات (Coins) بالدفع الفعلي:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary
                )

                WanasCatalogData.coinPackages.forEach { pkg ->
                    var quickRefInput by rememberSaveable(pkg.packageId) { mutableStateOf("VOD-010984512") }
                    Surface(
                        color = if (isDarkMode) Color.White.copy(alpha = 0.07f) else WanasDaySurfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("coin_package_card_${pkg.packageId}")
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
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(pkg.badgeEmoji, style = MaterialTheme.typography.headlineSmall)
                                    Column {
                                        Text(
                                            text = "${pkg.coinsAmount} Coin = ${pkg.priceEgp} جنيه",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = pkg.bonusLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = textSub
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onBuyCoinPackageViaRealPayment(pkg) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WanasAmberGold,
                                        contentColor = Color(0xFF1A103C)
                                    ),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .testTag("buy_coin_pkg_${pkg.packageId}")
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("شراء (${pkg.priceEgp} ج)", fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = quickRefInput,
                                    onValueChange = { quickRefInput = it },
                                    label = { Text("مرجع التحويل لتأكيد الإدارة") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("coin_pkg_ref_input_${pkg.packageId}")
                                )
                                FilledTonalButton(
                                    onClick = { onBuyCoinPackageViaRealPayment(pkg) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = WanasEmeraldOnline,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .testTag("submit_coin_order_admin_btn_${pkg.packageId}")
                                ) {
                                    Text("📩 إرسال لتأكيد الدفع", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }

                // Pending / Confirmed Purchase Orders Summary inside Store
                val pendingOrdersCount = actionState.paymentPurchaseOrders.count {
                    it.status == PaymentPurchaseOrderStatus.PENDING_ADMIN_CONFIRMATION
                }
                Surface(
                    color = if (isDarkMode) Color(0xFF211342) else WanasDayDarkHeader,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WanasEmeraldOnline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("store_linked_admin_payment_status_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛡️ الشراء مربوط بلوحة تحكم صاحب التطبيق لتأكيد الدفع",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            Text(
                                text = "طلبات بانتظار التأكيد: ($pendingOrdersCount) • إجمالي الطلبات المسجلة: (${actionState.paymentPurchaseOrders.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = WanasEmeraldOnline
                            )
                        }
                    }
                }
            }
        }

        // Spend Coins on Boosts & Features
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🚀 استخدم Coins لتفعيل المزايا الرقمية الفورية:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onActivateFeature("⭐ إبراز حسابي في الصدارة", 30, true, false) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("boost_profile_coins_button")
                    ) {
                        Text("⭐ إبراز حسابي (30🪙)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }

                    FilledTonalButton(
                        onClick = { onActivateFeature("🚀 أولوية في المطابقة الفورية", 40, false, true) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("priority_match_coins_button")
                    ) {
                        Text("🚀 أولوية المطابقة (40🪙)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }
                }

                // Digital Gifts Catalog — Modern & Enticing Showcase
                HorizontalDivider(color = textSub.copy(alpha = 0.2f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁 متجر الهدايا التفاعلية الفاخرة (تأثيرات حية + هدايا المضيف):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onSendFreeDailyGift,
                        enabled = !actionState.dailyFreeGiftClaimed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WanasEmeraldOnline,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("claim_free_daily_gift_button")
                    ) {
                        Text(
                            text = if (actionState.dailyFreeGiftClaimed) "✅ أرسلت الهدية المجانية" else "🎁 هدية يومية مجانية",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Top Gift Sender & Top Gift Receiver Leaderboard Banner ("أكثر عضو أرسل هدايا • أكثر عضو استقبل هدايا")
                Surface(
                    color = Color(0xFF231344),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gift_leaderboard_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🏆 لوحة شرف الهدايا في وَنَس:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Text(
                            text = "👑 أكثر عضو أرسل هدايا: ${actionState.topGiftSenderLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🌟 أكثر مضيف/عضو استقبل هدايا: ${actionState.topGiftReceiverLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WanasEmeraldOnline,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                WanasCatalogData.digitalGifts.forEach { gift ->
                    val isLuxuryGift = gift.costCoins >= 100
                    Surface(
                        color = if (isLuxuryGift) {
                            Color(0xFF231344)
                        } else if (isDarkMode) {
                            Color.White.copy(alpha = 0.08f)
                        } else {
                            WanasDaySurfaceVariant
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = if (isLuxuryGift) 1.5.dp else 1.dp,
                            color = if (isLuxuryGift) WanasAmberGold else WanasVioletAccent.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gift_store_card_${gift.giftId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
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
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                if (isLuxuryGift) {
                                                    listOf(WanasAmberGold, WanasCoralWarm, WanasVioletAccent)
                                                } else {
                                                    listOf(WanasVioletAccent.copy(alpha = 0.35f), WanasTealCalm.copy(alpha = 0.35f))
                                                }
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(gift.emoji, style = MaterialTheme.typography.titleLarge)
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = gift.nameAr,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isLuxuryGift) WanasAmberGold else textPrimary
                                        )
                                        Surface(
                                            color = WanasAmberGold.copy(alpha = 0.22f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = gift.tierLabelAr,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isLuxuryGift || isDarkMode) WanasAmberGold else WanasDayDarkHeader
                                            )
                                        }
                                    }
                                    Text(
                                        text = gift.effectLabelAr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isLuxuryGift) Color.White.copy(alpha = 0.9f) else textSub
                                    )
                                    Text(
                                        text = "🪙 السعر: ${gift.costCoins} Coins • مكافأة المضيف: +${gift.hostShareCoins} Coins",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WanasEmeraldOnline
                                    )
                                }
                            }

                            Button(
                                onClick = { onSendGift(gift) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLuxuryGift) WanasAmberGold else WanasVioletAccent,
                                    contentColor = if (isLuxuryGift) Color(0xFF1A103C) else Color.White
                                ),
                                modifier = Modifier.testTag("send_gift_button_${gift.giftId}")
                            ) {
                                Text(
                                    text = "🎁 إهداء (${gift.costCoins}🪙)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
                // Gift History Log ("سجل الهدايا المرسلة والمستقبلة")
                if (actionState.giftHistoryRecords.isNotEmpty()) {
                    HorizontalDivider(color = textSub.copy(alpha = 0.2f))
                    Text(
                        text = "📜 سجل الهدايا الأخير (${actionState.giftHistoryRecords.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = textPrimary
                    )
                    actionState.giftHistoryRecords.take(6).forEach { entry ->
                        Surface(
                            color = if (isDarkMode) Color.White.copy(alpha = 0.06f) else WanasDaySurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${entry.giftEmoji} ${entry.senderName} ⬅️ ${entry.recipientName} (${entry.giftNameAr})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${entry.costCoins}🪙 • الآن",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WanasEmeraldOnline
                                )
                            }
                        }
                    }
                }
            }
        }

        // ✨ New Proposal 4: Special VIP IDs, Glowing Seat Frames & Royal Entry Effects Store ("متجر الأرقام المميزة وإطارات المقاعد وتأثيرات الدخول")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("special_ids_and_frames_store_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.5.dp, WanasAmberGold)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "✨ متجر الأرقام المميزة وإطارات المقاعد وتأثيرات الدخول:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                )
                if (actionState.equippedSpecialMemberCode != null || actionState.equippedSeatFrameLabel != null) {
                    Surface(
                        color = WanasDayDarkEmerald,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WanasAmberGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👑 المفعّل حالياً: ${actionState.equippedSpecialMemberCode ?: "معرّفك القياسي"} • ${actionState.equippedSeatFrameLabel ?: "بدون إطار خاص"} • ${actionState.equippedEntryEffectLabel ?: ""}",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                    }
                }

                WanasCatalogData.specialCosmeticItems.forEach { item ->
                    val isOwned = item.itemId in actionState.ownedSpecialCosmeticIds
                    Surface(
                        color = if (isDarkMode) Color.White.copy(alpha = 0.07f) else WanasDaySurfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isOwned) WanasEmeraldOnline else WanasAmberGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("special_cosmetic_card_${item.itemId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${item.emoji} ${item.titleAr} (${item.categoryAr})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = item.descriptionAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textSub
                                )
                                Text(
                                    text = "المعاينة: ${item.previewBadge} • ${item.costCoins} Coins",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WanasEmeraldOnline
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onPurchaseOrEquipSpecialCosmetic(item) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOwned) WanasEmeraldOnline else WanasAmberGold,
                                    contentColor = if (isOwned) Color.White else Color(0xFF1A103C)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("buy_special_cosmetic_btn_${item.itemId}")
                            ) {
                                Text(
                                    text = if (isOwned) "✅ مفعّل" else "شراء (${item.costCoins}🪙)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🛡️ New Proposal 5: Wanas Families / Clans Competition ("نظام العائلات والقبائل والتنافس الأسبوعي")
        var newClanNameInput by rememberSaveable { mutableStateOf("") }
        var newClanMottoInput by rememberSaveable { mutableStateOf("") }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wanas_families_clans_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.5.dp, WanasTealCalm)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🛡️ نظام عائلات وقبائل وَنَس (تحدي الصدارة الأسبوعي):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                )

                actionState.familiesClansList.forEachIndexed { idx, clan ->
                    val isMyClan = actionState.joinedClanId == clan.clanId
                    Surface(
                        color = if (isMyClan) WanasEmeraldOnline.copy(alpha = 0.16f) else if (isDarkMode) Color.White.copy(alpha = 0.06f) else WanasDaySurfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isMyClan) WanasEmeraldOnline else WanasAmberGold.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("family_clan_row_${clan.clanId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "#${idx + 1} ${clan.badgeEmoji} ${clan.clanNameAr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "الشعار: «${clan.mottoAr}» • القائد: ${clan.leaderName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textSub
                                )
                                Text(
                                    text = "👥 ${clan.membersCount} عضو • 🔥 نشاط الأسبوع: ${clan.weeklyActivityPoints} نقطة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WanasEmeraldOnline
                                )
                            }
                            Button(
                                onClick = { onJoinWanasClan(clan) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMyClan) WanasEmeraldOnline else WanasTealCalm,
                                    contentColor = if (isMyClan) Color.White else Color(0xFF00201D)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("join_clan_btn_${clan.clanId}")
                            ) {
                                Text(
                                    text = if (isMyClan) "🛡️ عائلتك" else "انضمام +25XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newClanNameInput,
                        onValueChange = { newClanNameInput = it },
                        label = { Text("تأسيس عائلة جديدة باسمك...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("create_clan_name_input")
                    )
                    Button(
                        onClick = {
                            if (newClanNameInput.isNotBlank()) {
                                onCreateWanasClan(newClanNameInput, newClanMottoInput)
                                newClanNameInput = ""
                                newClanMottoInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WanasAmberGold,
                            contentColor = Color(0xFF1A103C)
                        ),
                        modifier = Modifier.testTag("create_clan_submit_button")
                    ) {
                        Text("➕ تأسيس عائلة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // 💎 Wanas Premium Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E133C)),
            border = BorderStroke(1.5.dp, WanasAmberGold)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💎 اشتراك وَنَس Premium",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WanasAmberGold
                )
                Text("✅ بدون إعلانات نهائياً بين الجلسات", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("✅ أولوية قصوى في المطابقة + فلاتر إضافية", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("✅ جلسات أطول (20 دقيقة بدل 10 دقائق) + شارة Premium 👑", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("✅ إنشاء غرف خاصة ومزايا إضافية للملف الشخصي", style = MaterialTheme.typography.bodySmall, color = Color.White)

                Button(
                    onClick = onOpenVipPaymentScreen,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WanasAmberGold,
                        contentColor = Color(0xFF1A103C)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("subscribe_wanas_premium_button")
                ) {
                    Text(
                        text = if (actionState.isPaidVip) "✅ باقة ونس Premium مفعلة على حسابك" else "👑 اشترك الآن في ونس Premium",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * 3. 🛡️ Safety Center & 4. 📊 Owner Admin Dashboard Screen:
 * - Regular members see ONLY the Safety Center (+18 rules, non-therapy disclaimer, report submission).
 * - The Admin Dashboard (`admin_dashboard_metrics_card`) is strictly hidden from regular members and ONLY shown when `actionState.isAdminOwner == true`.
 */
@Composable
fun WanasSafetyAndAdminDashboardScreen(
    actionState: ChatRoomsActionState,
    totalRoomsCount: Int,
    availableRooms: List<com.example.data.ChatRoomMetadata> = emptyList(),
    isDarkMode: Boolean,
    initialShowAdminTab: Boolean = false,
    onSubmitReport: (targetName: String, reason: String) -> Unit,
    onResolveReportByAdmin: (reportId: String, actionTaken: String) -> Unit,
    onUpdateFadfadaPin: (String) -> Unit,
    onBroadcastGlobalAnnouncement: (String) -> Unit = {},
    onGrantBonusCoinsByAdmin: (targetName: String, coins: Int, grantVip: Boolean) -> Unit = { _, _, _ -> },
    onToggleGlobalAppControl: (toggleRooms: Boolean, toggleProfanity: Boolean, toggleDoubleCoins: Boolean) -> Unit = { _, _, _ -> },
    onAdminPinOrLockRoom: (roomId: String, togglePin: Boolean, toggleLock: Boolean) -> Unit = { _, _, _ -> },
    onAdminUpdateRoomPassword: (roomId: String, newPassword: String) -> Unit = { _, _ -> },
    onAdminDeleteRoom: (roomId: String) -> Unit = {},
    onAdminEnterRoomDirect: (com.example.data.ChatRoomMetadata) -> Unit = {},
    onPerformOwnerMemberAction: (memberQuery: String, newStatus: ManagedAccountStatus, reasonAr: String) -> Unit = { _, _, _ -> },
    onExecuteServerLedgerOperation: (targetMember: String, deltaCoins: Int, opTypeAr: String, auditReason: String) -> Unit = { _, _, _, _ -> },
    onAssignAdminRole: (memberName: String, roleType: WanasAdminRoleType, customPerms: Set<String>) -> Unit = { _, _, _ -> },
    onTransferOrAssignRoomAdmin: (roomId: String, newOwnerName: String, newRoomAdminName: String) -> Unit = { _, _, _ -> },
    onResolveSuspiciousAlert: (alertId: String, blockSuspect: Boolean) -> Unit = { _, _ -> },
    onUpdateSupportTicketAndReply: (reportId: String, newStatusAr: String, replyMessage: String) -> Unit = { _, _, _ -> },
    onApproveOrRejectPaymentOrder: (orderId: String, approve: Boolean, adminNote: String) -> Unit = { _, _, _ -> },
    onOwnerQuickControlAllRooms: (String) -> Unit = {},
    onOwnerQuickControlAllMembers: (String, String, Int) -> Unit = { _, _, _ -> },
    onUpdateOwnerSystemSettings: (
        appName: String,
        maxRooms: Int,
        maxSeats: Int,
        bannedWordsCsv: String,
        toggleMaintenance: Boolean,
        toggleGiftsGlobally: Boolean,
        toggleRegistration: Boolean,
        triggerCloudBackup: Boolean
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onToggleAdminOwnerMode: (Boolean) -> Unit,
    onToggleGlobalTheme: (Boolean) -> Unit = {},
    onBackToHome: () -> Unit
) {
    BackHandler { onBackToHome() }

    val cardBg = if (isDarkMode) WanasCardBg else WanasDayLightSurface
    val textPrimary = if (isDarkMode) Color.White else WanasDayTextPrimary
    val textSub = if (isDarkMode) Color.White.copy(alpha = 0.72f) else WanasDayTextSecondary

    var reportTargetInput by rememberSaveable { mutableStateOf("") }
    var reportReasonInput by rememberSaveable { mutableStateOf("") }
    var newPinInput by rememberSaveable { mutableStateOf(actionState.fadfadaRequiredPin) }
    var globalAnnouncementInput by rememberSaveable { mutableStateOf(actionState.adminDashboardStats.globalAnnouncementBanner) }
    var grantTargetMemberInput by rememberSaveable { mutableStateOf("") }
    var grantCoinsAmountInput by rememberSaveable { mutableStateOf("100") }

    // 12-Module Owner Control Panel state
    var selectedOwnerModuleId by rememberSaveable { mutableStateOf(OwnerDashboardModuleTab.DASHBOARD.id) }
    var memberSearchQuery by rememberSaveable { mutableStateOf("") }
    var memberActionReasonInput by rememberSaveable { mutableStateOf("مخالفة شروط الاستخدام أو إزعاج المتحدثين") }
    var ledgerTargetInput by rememberSaveable { mutableStateOf("") }
    var ledgerAmountInput by rememberSaveable { mutableStateOf("250") }
    var ledgerReasonInput by rememberSaveable { mutableStateOf("عملية موثقة ومعتمدة عبر سجل السيرفر (Ledger)") }
    var roleMemberNameInput by rememberSaveable { mutableStateOf("") }
    var selectedRoleTypeName by rememberSaveable { mutableStateOf(WanasAdminRoleType.SUPER_ADMIN.name) }
    var auditSearchQuery by rememberSaveable { mutableStateOf("") }
    var transferOwnerInput by rememberSaveable { mutableStateOf("مضيف السهرة 👑") }
    var assignRoomAdminInput by rememberSaveable { mutableStateOf("مشرف الغرفة 🛡️") }
    var supportReplyInput by rememberSaveable { mutableStateOf("تمت مراجعة بلاغك من إدارة وَنَس واتخاذ الإجراء اللازم فوراً ✅") }
    var sysAppNameInput by rememberSaveable { mutableStateOf(actionState.systemSettings.appDisplayName) }
    var sysMaxRoomsInput by rememberSaveable { mutableStateOf(actionState.systemSettings.maxRoomsAllowed.toString()) }
    var sysBannedWordsInput by rememberSaveable { mutableStateOf(actionState.systemSettings.bannedWordsCsv) }

    val stats = actionState.adminDashboardStats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("wanas_safety_and_admin_screen_root"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isDarkMode) WanasCardBg else WanasDayDarkHeader
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = WanasAmberGold)
                    Column {
                        Text(
                            text = if (actionState.isAdminOwner) {
                                "📊 لوحة تحكم الإدارة والأمان الشامل"
                            } else {
                                "🛡️ مركز الأمان وقواعد الاستخدام (+18)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Text(
                            text = if (actionState.isAdminOwner) {
                                "صلاحيات الإدارة العليا مفعلة لحساب مالك التطبيق فقط"
                            } else {
                                "بيئة آمنة ومحترمة لجميع أعضاء وَنَس"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = WanasEmeraldOnline,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = WanasAmberGold,
                        contentColor = Color(0xFF1A103C)
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الرئيسية", fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // 🌓 Global App Settings — Dark / Light Theme Switch Card (Available to all users)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("global_settings_dark_light_theme_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.5.dp, WanasAmberGold.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "تبديل المظهر الليلي والنهاري",
                        tint = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                    )
                    Column {
                        Text(
                            text = "⚙️ مظهر التطبيق العام (Dark / Light Theme)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary
                        )
                        Text(
                            text = if (isDarkMode) "🌙 الوضع الليلي الفاخر مفعّل حالياً" else "☀️ الوضع النهاري المشرق مفعّل حالياً",
                            style = MaterialTheme.typography.labelSmall,
                            color = textSub
                        )
                    }
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { enabled -> onToggleGlobalTheme(enabled) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF1A103C),
                        checkedTrackColor = WanasAmberGold,
                        uncheckedThumbColor = WanasAmberGold,
                        uncheckedTrackColor = WanasDayDarkHeader
                    ),
                    modifier = Modifier.testTag("safety_settings_theme_switch")
                )
            }
        }

        // 📊 Owner Admin Dashboard Card — strictly visible ONLY to App Owner/Admin ("لوحة تحكم الادمن لا تظهر للاعضاء")
        if (actionState.isAdminOwner) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_dashboard_metrics_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1136)),
                border = BorderStroke(1.5.dp, WanasAmberGold)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👑 لوحة تحكم صاحب التطبيق (${WanasCatalogData.PRIMARY_APP_OWNER_EMAIL})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            Text(
                                text = "هيكل المستويين: لوحة المالك الشاملة (12 قسماً) منفصلة عن لوحة أدمن الغرفة",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            color = WanasEmeraldOnline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, WanasEmeraldOnline)
                        ) {
                            Text(
                                text = "👑 مالك أساسي",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WanasEmeraldOnline
                            )
                        }
                    }

                    // 🗂️ 12-Module Tree Selector for App Owner ("الهيكل النهائي المقترح — لوحة صاحب التطبيق")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("owner_12_modules_tabs_row")
                    ) {
                        items(OwnerDashboardModuleTab.entries.toList(), key = { it.id }) { tab ->
                            val isSelected = selectedOwnerModuleId == tab.id
                            Surface(
                                color = if (isSelected) WanasAmberGold else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) WanasAmberGold else WanasTealCalm.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .clickable { selectedOwnerModuleId = tab.id }
                                    .testTag("owner_module_tab_${tab.id}")
                            ) {
                                Text(
                                    text = "${tab.emoji} ${tab.titleAr}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFF1A103C) else Color.White
                                )
                            }
                        }
                    }

                    // 📊 Active Selected Module Card from the 12 Owner Modules
                    Surface(
                        color = Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("owner_selected_module_panel_$selectedOwnerModuleId")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when (selectedOwnerModuleId) {
                                OwnerDashboardModuleTab.DASHBOARD.id -> {
                                    Text(
                                        text = "📊 1. مركز التحكم الرئيسي (الإحصائيات الفورية الشاملة):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    AdminMetricGroupRow(
                                        groupTitle = "🟢 الحضور المباشر والغرف والمتحدثون الآن",
                                        items = listOf(
                                            "المستخدمون الآن" to "${stats.onlineUsersNow}",
                                            "النشطون اليوم" to "${stats.activeUsersToday}",
                                            "الغرف المفتوحة" to "$totalRoomsCount",
                                            "المتحدثون حالياً" to "${stats.currentSpeakersNow}"
                                        )
                                    )
                                    AdminMetricGroupRow(
                                        groupTitle = "💰 الأرباح اليومية / الأسبوعية / الشهرية والهدايا",
                                        items = listOf(
                                            "اليوم" to "${stats.dailyRevenueEgp} ج.م",
                                            "الأسبوع" to "${stats.weeklyRevenueEgp} ج.م",
                                            "الشهر" to "${stats.monthlyRevenueEgp} ج.م",
                                            "إجمالي الهدايا" to "${stats.totalGiftsSentCount} 🎁"
                                        )
                                    )
                                    Text(
                                        text = "🔥 أكثر الغرف نشاطاً: ${stats.topActiveRoomName} • ⭐ أنشط عضو: ${stats.mostActiveMemberName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WanasEmeraldOnline,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OwnerDashboardModuleTab.MEMBERS.id -> {
                                    Text(
                                        text = "👥 2. إدارة الأعضاء (بحث متقدم بالاسم / ID / البريد + سجل المخالفات والجلسات):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    OutlinedTextField(
                                        value = memberSearchQuery,
                                        onValueChange = { memberSearchQuery = it },
                                        label = { Text("ابحث بالاسم أو ID أو البريد (أو اكتب اسم عضو لتطبيق إجراء)") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = WanasAmberGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("owner_member_search_input")
                                    )
                                    OutlinedTextField(
                                        value = memberActionReasonInput,
                                        onValueChange = { memberActionReasonInput = it },
                                        label = { Text("سبب الإجراء الإداري (يُسجل في سجل المخالفات والعمليات)") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = WanasAmberGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(ManagedAccountStatus.entries.toList(), key = { it.name }) { status ->
                                            Button(
                                                onClick = {
                                                    val target = memberSearchQuery.ifBlank { actionState.memberDisplayName }
                                                    onPerformOwnerMemberAction(target, status, memberActionReasonInput)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (status == ManagedAccountStatus.ACTIVE) WanasEmeraldOnline else WanasCoralWarm
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("owner_member_action_${status.name}")
                                            ) {
                                                Text(
                                                    text = "${status.emoji} ${status.labelAr}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                    val filteredMembers = actionState.managedMembersDirectory.filter {
                                        memberSearchQuery.isBlank() ||
                                            it.displayName.contains(memberSearchQuery, ignoreCase = true) ||
                                            it.memberId.contains(memberSearchQuery, ignoreCase = true) ||
                                            it.emailOrCode.contains(memberSearchQuery, ignoreCase = true)
                                    }
                                    if (filteredMembers.isEmpty()) {
                                        Text(
                                            text = "💡 حساب المالك النشط حالياً: ${actionState.memberDisplayName} (${WanasCatalogData.PRIMARY_APP_OWNER_EMAIL}) • الجلسات النشطة: Android Streaming Device (نشط الآن)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    } else {
                                        filteredMembers.take(5).forEach { m ->
                                            Surface(
                                                color = Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    Text(
                                                        text = "${m.avatarEmoji} ${m.displayName} (${m.memberId}) — الحالة: ${m.accountStatus.emoji} ${m.accountStatus.labelAr}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = WanasAmberGold
                                                    )
                                                    Text(
                                                        text = "🪙 الرصيد: ${m.coinsBalance} • الأجهزة النشطة: ${m.activeDeviceSessions.joinToString()}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.White
                                                    )
                                                    if (m.violationsHistory.isNotEmpty()) {
                                                        Text(
                                                            text = "⚠️ سجل المخالفات: ${m.violationsHistory.first()}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = WanasCoralWarm
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                OwnerDashboardModuleTab.ROOMS.id -> {
                                    Text(
                                        text = "🏠 3. إدارة جميع الغرف (نقل ملكية الغرفة • تعيين/إلغاء مدير غرفة • حالة الغرفة):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = transferOwnerInput,
                                            onValueChange = { transferOwnerInput = it },
                                            label = { Text("صاحب الغرفة الجديد") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = WanasAmberGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = assignRoomAdminInput,
                                            onValueChange = { assignRoomAdminInput = it },
                                            label = { Text("مدير الغرفة المعين") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = WanasAmberGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    availableRooms.take(4).forEach { rm ->
                                        Surface(
                                            color = Color.White.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${rm.roomName} (${if (rm.hasPassword) "خاصة 🔒 ${rm.roomPassword}" else "عامة 🌐"})",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "👑 المالك: ${rm.roomOwnerName} • 🛡️ المدير: ${rm.assignedRoomAdminName.ifBlank { "غير معين" }} • 👥 ${rm.activeMembersCount} حاليًا (🎙️ ${rm.activeSpeakersCount})",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = WanasEmeraldOnline
                                                    )
                                                }
                                                Button(
                                                    onClick = {
                                                        onTransferOrAssignRoomAdmin(rm.roomId, transferOwnerInput, assignRoomAdminInput)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = WanasAmberGold,
                                                        contentColor = Color(0xFF1A103C)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.testTag("owner_transfer_room_btn_${rm.roomId}")
                                                ) {
                                                    Text("نقل/تعيين", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }
                                }

                                OwnerDashboardModuleTab.ADMINS_ROLES.id -> {
                                    Text(
                                        text = "🛡️ 4. نظام الصلاحيات والرتب الحقيقي (Owner • Super Admin • Room Admin • Moderator • Support):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    OutlinedTextField(
                                        value = roleMemberNameInput,
                                        onValueChange = { roleMemberNameInput = it },
                                        label = { Text("اسم العضو لتعيين رتبته وصلاحياته") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = WanasAmberGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("owner_assign_role_name_input")
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(WanasAdminRoleType.entries.toList(), key = { it.name }) { rType ->
                                            val isSelectedRole = selectedRoleTypeName == rType.name
                                            Surface(
                                                color = if (isSelectedRole) WanasAmberGold else Color.White.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.clickable { selectedRoleTypeName = rType.name }
                                            ) {
                                                Text(
                                                    text = "${rType.badgeEmoji} ${rType.titleAr}",
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSelectedRole) Color(0xFF1A103C) else Color.White
                                                )
                                            }
                                        }
                                    }
                                    val chosenRole = WanasAdminRoleType.entries.find { it.name == selectedRoleTypeName }
                                        ?: WanasAdminRoleType.SUPER_ADMIN
                                    Text(
                                        text = "📋 وصف الصلاحيات: ${chosenRole.descriptionAr} (${chosenRole.defaultPermissions.joinToString(" • ")})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WanasTealCalm
                                    )
                                    Button(
                                        onClick = {
                                            onAssignAdminRole(roleMemberNameInput, chosenRole, chosenRole.defaultPermissions)
                                            roleMemberNameInput = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = WanasEmeraldOnline,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("owner_confirm_assign_role_button")
                                    ) {
                                        Text("🛡️ اعتماد ومنح الرتبة والصلاحيات", fontWeight = FontWeight.ExtraBold)
                                    }
                                    actionState.adminRoleAssignments.forEach { assign ->
                                        Text(
                                            text = "• ${assign.roleType.badgeEmoji} ${assign.memberName}: ${assign.roleType.titleAr} (بواسطة ${assign.assignedBy})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }

                                OwnerDashboardModuleTab.COINS_PAYMENTS.id -> {
                                    Text(
                                        text = "💰 5. العملات والمدفوعات — ربط الشراء بلوحة التحكم لتأكيد الدفع + Server Ledger:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    AdminMetricGroupRow(
                                        groupTitle = "إحصائيات العملات وطلبات الدفع الموثقة بالسيرفر",
                                        items = listOf(
                                            "مشتراة" to "${stats.coinsSoldTotal}🪙",
                                            "بانتظار التأكيد" to "${actionState.paymentPurchaseOrders.count { it.status == PaymentPurchaseOrderStatus.PENDING_ADMIN_CONFIRMATION }}⏳",
                                            "مؤكدة" to "${actionState.paymentPurchaseOrders.count { it.status == PaymentPurchaseOrderStatus.CONFIRMED_BY_ADMIN }}✅"
                                        )
                                    )

                                    // 💳 Purchase & Payment Confirmation Queue inside Owner Control Panel
                                    Surface(
                                        color = Color(0xFF241346),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.5.dp, WanasAmberGold),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("owner_payment_orders_confirmation_queue")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "💳 طلبات الشراء والدفع الواردة لتأكيد الدفع (${actionState.paymentPurchaseOrders.size}):",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = WanasAmberGold
                                            )
                                            actionState.paymentPurchaseOrders.forEach { order ->
                                                Surface(
                                                    color = Color.White.copy(alpha = 0.08f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        when (order.status) {
                                                            PaymentPurchaseOrderStatus.APPROVED -> WanasEmeraldOnline
                                                            PaymentPurchaseOrderStatus.PENDING_OWNER_APPROVAL -> WanasAmberGold
                                                            PaymentPurchaseOrderStatus.REJECTED -> WanasCoralWarm
                                                        }
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("owner_payment_order_card_${order.orderId}")
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "${order.status.emoji} ${order.buyerName} (${order.buyerMemberCode}) — ${order.packageOrPlanTitle}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            text = "💵 المبلغ: ${order.amountEgp} ج.م • الرصيد: +${order.coinsToCredit}🪙 • الوسيلة: ${order.paymentMethodAr} • المرجع: ${order.referenceNumber}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = WanasTealCalm
                                                        )
                                                        Text(
                                                            text = "الحالة: ${order.status.labelAr} • ${order.adminNoteAr}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = WanasAmberGold
                                                        )
                                                        if (order.status == PaymentPurchaseOrderStatus.PENDING_ADMIN_CONFIRMATION) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Button(
                                                                    onClick = {
                                                                        onApproveOrRejectPaymentOrder(
                                                                            order.orderId,
                                                                            true,
                                                                            "تم التحقق من مرجع الدفع واعتماد الرصيد والباقة من صاحب التطبيق ✅"
                                                                        )
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = WanasEmeraldOnline),
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .height(34.dp)
                                                                        .testTag("owner_approve_payment_order_${order.orderId}")
                                                                ) {
                                                                    Text("✅ تأكيد الدفع وشحن الرصيد", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                                                }
                                                                OutlinedButton(
                                                                    onClick = {
                                                                        onApproveOrRejectPaymentOrder(
                                                                            order.orderId,
                                                                            false,
                                                                            "رقم المرجع البنكي غير مطابق — يرجى مراجعة الإيصال ❌"
                                                                        )
                                                                    },
                                                                    border = BorderStroke(1.dp, WanasCoralWarm),
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .height(34.dp)
                                                                        .testTag("owner_reject_payment_order_${order.orderId}")
                                                                ) {
                                                                    Text("❌ رفض الطلب", style = MaterialTheme.typography.labelSmall, color = WanasCoralWarm, fontWeight = FontWeight.ExtraBold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = ledgerTargetInput,
                                            onValueChange = { ledgerTargetInput = it },
                                            label = { Text("اسم العضو") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = WanasAmberGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = ledgerAmountInput,
                                            onValueChange = { ledgerAmountInput = it },
                                            label = { Text("القيمة 🪙") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = WanasAmberGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.weight(0.7f)
                                        )
                                    }
                                    OutlinedTextField(
                                        value = ledgerReasonInput,
                                        onValueChange = { ledgerReasonInput = it },
                                        label = { Text("السبب الموثق في سجل Ledger (إلزامي)") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = WanasAmberGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = {
                                                val amt = ledgerAmountInput.toIntOrNull() ?: 250
                                                onExecuteServerLedgerOperation(ledgerTargetInput, amt, "منح إداري موثق (Ledger Credit)", ledgerReasonInput)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = WanasEmeraldOnline),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("owner_ledger_credit_button")
                                        ) {
                                            Text("➕ إضافة بعملية مسجلة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Button(
                                            onClick = {
                                                val amt = ledgerAmountInput.toIntOrNull() ?: 100
                                                onExecuteServerLedgerOperation(ledgerTargetInput, -amt, "خصم/استرداد موثق (Ledger Debit)", ledgerReasonInput)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = WanasCoralWarm),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("owner_ledger_debit_button")
                                        ) {
                                            Text("➖ خصم بعملية مسجلة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                    actionState.coinLedgerRecords.take(4).forEach { rec ->
                                        Text(
                                            text = "🧾 ${rec.ledgerId} • ${rec.targetMemberName}: ${if (rec.deltaCoins >= 0) "+${rec.deltaCoins}" else "${rec.deltaCoins}"}🪙 (${rec.operationTypeAr} • ${rec.auditReason})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WanasAmberGold
                                        )
                                    }
                                }

                                OwnerDashboardModuleTab.GIFTS_STORE.id -> {
                                    Text(
                                        text = "🎁 6. إدارة متجر الهدايا (${WanasCatalogData.digitalGifts.size} هدايا • تشغيل/إيقاف الهدايا عالمياً):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    Button(
                                        onClick = {
                                            onUpdateOwnerSystemSettings(
                                                sysAppNameInput,
                                                sysMaxRoomsInput.toIntOrNull() ?: 100,
                                                actionState.systemSettings.maxSeatsPerRoom,
                                                sysBannedWordsInput,
                                                false,
                                                true,
                                                false,
                                                false
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (actionState.systemSettings.giftsEnabledGlobally) WanasEmeraldOnline else WanasCoralWarm
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (actionState.systemSettings.giftsEnabledGlobally) "✅ نظام الهدايا مفعل في جميع الغرف" else "🚫 نظام الهدايا متوقف مؤقتاً",
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Text(
                                        text = "🏆 أكثر عضو أرسل هدايا: ${actionState.topGiftSenderBadge} • أكثر عضو استقبل: ${actionState.topGiftReceiverBadge}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }

                                OwnerDashboardModuleTab.SUPPORT_REPORTS.id -> {
                                    Text(
                                        text = "📩 7. الشكاوى والدعم الفني (جديد -> قيد المراجعة -> تم الحل -> مغلق):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    OutlinedTextField(
                                        value = supportReplyInput,
                                        onValueChange = { supportReplyInput = it },
                                        label = { Text("رسالة الرد المباشر من الدعم الفني للعضو المبلّغ") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = WanasAmberGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (actionState.safetyReports.isEmpty()) {
                                        Text(
                                            text = "✅ لا توجد شكاوى معلقة حالياً — جميع البلاغات تمت معالجتها.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WanasEmeraldOnline
                                        )
                                    } else {
                                        actionState.safetyReports.forEach { rep ->
                                            Surface(
                                                color = Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = "🎫 شكوى ضد «${rep.reportedUserName}» • الحالة: ${rep.status} • الأولوية: ${rep.priorityAr}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = WanasAmberGold
                                                    )
                                                    Text(
                                                        text = "السبب: ${rep.reasonAr} • المبلّغ: ${rep.reporterName}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.White
                                                    )
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        FilledTonalButton(
                                                            onClick = { onUpdateSupportTicketAndReply(rep.reportId, "قيد المراجعة ⏳", supportReplyInput) },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("قيد المراجعة", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                        Button(
                                                            onClick = { onUpdateSupportTicketAndReply(rep.reportId, "تم الحل ✅", supportReplyInput) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = WanasEmeraldOnline),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("تم الحل + رد", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                        OutlinedButton(
                                                            onClick = { onUpdateSupportTicketAndReply(rep.reportId, "مغلق 🔒", supportReplyInput) },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("مغلق", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                OwnerDashboardModuleTab.ANNOUNCEMENTS.id -> {
                                    Text(
                                        text = "📢 8. الإعلانات والتنبيهات العامة (إشعار لجميع المستخدمين • إشعار VIP • تنبيه صيانة):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = { onBroadcastGlobalAnnouncement("🎉 عرض خاص اليوم: شحن العملات بمكافأة مضاعفة في جميع الغرف!") },
                                            colors = ButtonDefaults.buttonColors(containerColor = WanasAmberGold, contentColor = Color(0xFF1A103C)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("🔥 إعلان عرض خاص", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Button(
                                            onClick = { onBroadcastGlobalAnnouncement("👑 تنبيه لأعضاء VIP: تم فتح صالون النخبة الصوتي!") },
                                            colors = ButtonDefaults.buttonColors(containerColor = WanasVioletAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("👑 إشعار لفئة VIP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }

                                OwnerDashboardModuleTab.SECURITY_ANTI_ABUSE.id -> {
                                    Text(
                                        text = "🚨 9. الأمان ومكافحة الإساءة (كشف التلاعب • الحسابات المتعددة • السبام):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    actionState.suspiciousBehaviorAlerts.forEach { alert ->
                                        Surface(
                                            color = Color.White.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (alert.riskLevel == SecurityRiskLevel.HIGH_RISK) WanasCoralWarm else WanasAmberGold
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "${alert.riskLevel.emoji} ${alert.categoryAr} — المشتبه: ${alert.suspectName} (${alert.riskLevel.labelAr})",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = WanasAmberGold
                                                )
                                                Text(
                                                    text = "${alert.descriptionAr} • الموقع: ${alert.roomOrTarget}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White
                                                )
                                                if (!alert.isResolved) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = { onResolveSuspiciousAlert(alert.alertId, true) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = WanasCoralWarm),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("🚫 حظر المشتبه فوراً", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                        FilledTonalButton(
                                                            onClick = { onResolveSuspiciousAlert(alert.alertId, false) },
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("✅ تأكيد السلامة", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                OwnerDashboardModuleTab.AUDIT_LOGS.id -> {
                                    Text(
                                        text = "📝 10. سجل العمليات الإدارية (من قام بها -> ماذا فعل -> على من -> أين -> متى -> النتيجة):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    OutlinedTextField(
                                        value = auditSearchQuery,
                                        onValueChange = { auditSearchQuery = it },
                                        label = { Text("بحث وتصفية في سجل العمليات...") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = WanasAmberGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("owner_audit_log_search_input")
                                    )
                                    val logs = actionState.adminAuditLogs.filter {
                                        auditSearchQuery.isBlank() ||
                                            it.actorName.contains(auditSearchQuery, ignoreCase = true) ||
                                            it.actionTaken.contains(auditSearchQuery, ignoreCase = true) ||
                                            it.targetName.contains(auditSearchQuery, ignoreCase = true)
                                    }
                                    logs.take(8).forEach { log ->
                                        Surface(
                                            color = Color.White.copy(alpha = 0.07f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "📝 ${log.actorName} (${log.actorRole}) ⬅️ ${log.actionTaken} ⬅️ على: ${log.targetName} ⬅️ في: ${log.locationRoomOrSection} ⬅️ ${log.timestampLabel} (${log.resultSummary})",
                                                modifier = Modifier.padding(8.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                OwnerDashboardModuleTab.ANALYTICS_REPORTS.id -> {
                                    Text(
                                        text = "📈 11. التقارير والإحصائيات المتقدمة (DAU / WAU / MAU • الاحتفاظ • ساعات الذروة):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    AdminMetricGroupRow(
                                        groupTitle = "نمو المستخدمين والنشاط (DAU / WAU / MAU)",
                                        items = listOf(
                                            "DAU اليومي" to "${stats.activeUsersToday}",
                                            "WAU الأسبوعي" to "${stats.weeklyActiveUsersWau}",
                                            "MAU الشهري" to "${stats.monthlyActiveUsersMau}",
                                            "معدل الاحتفاظ" to "${stats.retentionRatePercent}%"
                                        )
                                    )
                                    Text(
                                        text = "⏰ أكثر الأوقات نشاطاً (ساعات الذروة): ${stats.peakActivityHourLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WanasEmeraldOnline,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OwnerDashboardModuleTab.SYSTEM_SETTINGS.id -> {
                                    Text(
                                        text = "⚙️ 12. إعدادات النظام الشاملة + 🌙 المظهر العام + 🔧 وضع الصيانة والنسخ الاحتياطي:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.55f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("owner_system_settings_theme_switch_row")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                                    contentDescription = "تبديل المظهر العام",
                                                    tint = WanasAmberGold
                                                )
                                                Column {
                                                    Text(
                                                        text = "مفتاح المظهر العام (Dark / Light Theme)",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = if (isDarkMode) "🌙 الوضع الليلي الفاخر مفعّل" else "☀️ الوضع النهاري المشرق مفعّل",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = WanasEmeraldOnline
                                                    )
                                                }
                                            }
                                            Switch(
                                                checked = isDarkMode,
                                                onCheckedChange = { enabled -> onToggleGlobalTheme(enabled) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color(0xFF1A103C),
                                                    checkedTrackColor = WanasAmberGold,
                                                    uncheckedThumbColor = WanasAmberGold,
                                                    uncheckedTrackColor = WanasDayDarkHeader
                                                ),
                                                modifier = Modifier.testTag("owner_system_settings_theme_switch")
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = sysAppNameInput,
                                            onValueChange = { sysAppNameInput = it },
                                            label = { Text("اسم التطبيق") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = WanasAmberGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        OutlinedTextField(
                                            value = sysMaxRoomsInput,
                                            onValueChange = { sysMaxRoomsInput = it },
                                            label = { Text("الحد الأقصى للغرف") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = WanasAmberGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.weight(0.8f)
                                        )
                                    }
                                    OutlinedTextField(
                                        value = sysBannedWordsInput,
                                        onValueChange = { sysBannedWordsInput = it },
                                        label = { Text("الكلمات الممنوعة (مفصولة بفاصلة)") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = WanasAmberGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = {
                                                onUpdateOwnerSystemSettings(
                                                    sysAppNameInput,
                                                    sysMaxRoomsInput.toIntOrNull() ?: 100,
                                                    actionState.systemSettings.maxSeatsPerRoom,
                                                    sysBannedWordsInput,
                                                    true,
                                                    false,
                                                    false,
                                                    false
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (actionState.systemSettings.isMaintenanceMode) WanasCoralWarm else WanasTealCalm
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("owner_toggle_maintenance_mode_button")
                                        ) {
                                            Text(
                                                text = if (actionState.systemSettings.isMaintenanceMode) "🚧 وضع الصيانة: مفعّل" else "✅ وضع التشغيل العادي",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                onUpdateOwnerSystemSettings(
                                                    sysAppNameInput,
                                                    sysMaxRoomsInput.toIntOrNull() ?: 100,
                                                    actionState.systemSettings.maxSeatsPerRoom,
                                                    sysBannedWordsInput,
                                                    false,
                                                    false,
                                                    false,
                                                    true
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = WanasAmberGold,
                                                contentColor = Color(0xFF1A103C)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("owner_trigger_cloud_backup_button")
                                        ) {
                                            Text("💾 نسخ احتياطي وحفظ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                    Text(
                                        text = "🕒 آخر نسخة احتياطية: ${actionState.systemSettings.lastBackupTimestamp}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WanasEmeraldOnline
                                    )
                                }
                            }
                        }
                    }

                    // 1. المستخدمون
                    AdminMetricGroupRow(
                        groupTitle = "👥 المستخدمون",
                        items = listOf(
                            "إجمالي المستخدمين" to "${stats.totalUsers}",
                            "النشطون اليوم" to "${stats.activeUsersToday}",
                            "المستخدمون الجدد" to "+${stats.newUsersToday}"
                        )
                    )

                    // 2. المطابقة
                    AdminMetricGroupRow(
                        groupTitle = "⭐ المطابقة والجلسات",
                        items = listOf(
                            "عدد الجلسات" to "${stats.totalMatchSessions}",
                            "متوسط المدة" to "${stats.avgSessionMinutes} د",
                            "الجلسات المكتملة" to "${stats.completedSessionsRatePercent}%"
                        )
                    )

                    // 3. الغرف
                    AdminMetricGroupRow(
                        groupTitle = "🎙️ الغرف والمضيفون",
                        items = listOf(
                            "عدد الغرف" to "$totalRoomsCount",
                            "الأكثر نشاطاً" to stats.topActiveRoomName,
                            "المضيفون النشطون" to "${stats.activeHostsCount}"
                        )
                    )

                    // 4. الربح (4 مصادر الربح: الإعلانات، Premium، Coins، الغرف والهدايا)
                    AdminMetricGroupRow(
                        groupTitle = "💰 الربح (4 مصادر دخل)",
                        items = listOf(
                            "Coins المباعة" to "${stats.coinsSoldTotal} 🪙",
                            "الاشتراكات Premium" to "${stats.activePremiumSubscriptions} 💎",
                            "الإعلانات والهدايا" to "${stats.adRevenueEgp} ج.م"
                        )
                    )

                    // 5. الأمان
                    AdminMetricGroupRow(
                        groupTitle = "🛡️ الأمان والرقابة",
                        items = listOf(
                            "البلاغات" to "${actionState.safetyReports.size}",
                            "الحسابات المحظورة" to "${stats.bannedUsersCount}",
                            "المستخدمون الموقوفون" to "${stats.suspendedUsersCount}"
                        )
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    // 🔐 Admin Control for Fadfada Secret Code ("اجعل قسم الفضفضة بكود")
                    Text(
                        text = "🔐 إدارة كود الدخول لقسم «فضفضة بدون اسم»:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WanasAmberGold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { newPinInput = it },
                            label = { Text("كود قسم الفضفضة (الحالي: ${actionState.fadfadaRequiredPin})") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = WanasAmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_fadfada_pin_input")
                        )
                        Button(
                            onClick = { onUpdateFadfadaPin(newPinInput) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasAmberGold,
                                contentColor = Color(0xFF1A103C)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_fadfada_pin_button")
                        ) {
                            Text("حفظ الكود", fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    // 📢 6. Global App Broadcast Control ("بث رسالة/إعلان عام لكل التطبيق والغرف")
                    Text(
                        text = "📢 بث إعلان إداري عام يظهر في جميع أقسام وغرف التطبيق:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WanasAmberGold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = globalAnnouncementInput,
                            onValueChange = { globalAnnouncementInput = it },
                            label = { Text("نص الإعلان الإداري العام") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = WanasAmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_global_announcement_input")
                        )
                        Button(
                            onClick = { onBroadcastGlobalAnnouncement(globalAnnouncementInput) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasEmeraldOnline,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("admin_broadcast_announcement_button")
                        ) {
                            Text("نشر للجميع", fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    // ⚙️ 7. App-Wide System Control Switches ("تحكمات شاملة للتطبيق والاقتصاد والأمان")
                    Text(
                        text = "⚙️ مفاتيح التحكم المركزي في التطبيق والاقتصاد:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WanasAmberGold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onToggleGlobalAppControl(true, false, false) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (stats.isRoomCreationOpenForAll) WanasEmeraldOnline else WanasCoralWarm,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_toggle_room_creation_button")
                        ) {
                            Text(
                                text = if (stats.isRoomCreationOpenForAll) "✅ إنشاء الغرف: مفتوح" else "🔒 إنشاء الغرف: مقفول",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        FilledTonalButton(
                            onClick = { onToggleGlobalAppControl(false, true, false) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (stats.isStrictProfanityFilterActive) WanasTealCalm else Color.White.copy(alpha = 0.2f),
                                contentColor = if (stats.isStrictProfanityFilterActive) Color(0xFF00201D) else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_toggle_profanity_filter_button")
                        ) {
                            Text(
                                text = if (stats.isStrictProfanityFilterActive) "🛡️ فلتر الكلمات: نشط" else "⚠️ فلتر الكلمات: متوقف",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        FilledTonalButton(
                            onClick = { onToggleGlobalAppControl(false, false, true) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (stats.isDoubleCoinsFestivalActive) WanasAmberGold else Color.White.copy(alpha = 0.2f),
                                contentColor = if (stats.isDoubleCoinsFestivalActive) Color(0xFF1A103C) else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_toggle_double_coins_button")
                        ) {
                            Text(
                                text = if (stats.isDoubleCoinsFestivalActive) "🔥 مضاعفة Coins: مفعلة" else "🪙 مضاعفة Coins: عادي",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    // 🎁 8. Admin Member Rewards, VIP Grant & Super Owner Controls ("تحكمات قوية لصاحب التطبيق في لوحة التحكم + ربط الشراء لتأكيد الدفع + التحكم في الأعضاء والغرف")
                    Surface(
                        color = Color(0xFF241346),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, WanasAmberGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("owner_master_payment_and_super_controls_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "💳 مركز تأكيد عمليات الشراء والدفع المباشر (مربوط بمتجر Coins و VIP):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            val pendingPayments = actionState.paymentPurchaseOrders.filter {
                                it.status == PaymentPurchaseOrderStatus.PENDING_ADMIN_CONFIRMATION
                            }
                            if (pendingPayments.isEmpty()) {
                                Text(
                                    text = "✅ جميع طلبات الشراء والدفع الحالية تم تأكيدها واعتمادها بنجاح (${actionState.paymentPurchaseOrders.size} عملية).",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WanasEmeraldOnline,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                pendingPayments.forEach { order ->
                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.7f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_pending_payment_row_${order.orderId}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "⏳ طلب شراء من «${order.buyerName}» (${order.buyerMemberCode}) • ${order.packageOrPlanTitle}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "المبلغ: ${order.amountEgp} ج.م • الرصيد المطلوب: +${order.coinsToCredit}🪙 • الوسيلة: ${order.paymentMethodAr} • المرجع: ${order.referenceNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WanasTealCalm
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        onApproveOrRejectPaymentOrder(
                                                            order.orderId,
                                                            true,
                                                            "تم تأكيد الدفع وشحن الرصيد وتفعيل الباقة فوراً من لوحة التحكم ✅"
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = WanasEmeraldOnline,
                                                        contentColor = Color.White
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(38.dp)
                                                        .testTag("admin_quick_confirm_payment_${order.orderId}")
                                                ) {
                                                    Text("✅ تأكيد الدفع الآن", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onApproveOrRejectPaymentOrder(
                                                            order.orderId,
                                                            false,
                                                            "تم رفض الطلب لعدم مطابقة رقم المرجع ❌"
                                                        )
                                                    },
                                                    border = BorderStroke(1.dp, WanasCoralWarm),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(0.7f)
                                                        .height(38.dp)
                                                        .testTag("admin_quick_reject_payment_${order.orderId}")
                                                ) {
                                                    Text("❌ رفض", style = MaterialTheme.typography.labelSmall, color = WanasCoralWarm, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.14f))

                            Text(
                                text = "⚡ تحكمات قوية وشاملة لصاحب التطبيق في جميع الغرف والأعضاء:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { onOwnerQuickControlAllRooms("EXPAND_ALL_TO_8_SEATS") },
                                    colors = ButtonDefaults.buttonColors(containerColor = WanasTealCalm, contentColor = Color(0xFF00201D)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("owner_super_expand_all_rooms_btn")
                                ) {
                                    Text("🎙️ 8 مايكات للكل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                }
                                Button(
                                    onClick = { onOwnerQuickControlAllRooms("START_TIKTOK_PK_ALL_ROOMS") },
                                    colors = ButtonDefaults.buttonColors(containerColor = WanasCoralWarm, contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("owner_super_start_pk_all_rooms_btn")
                                ) {
                                    Text("⚔️ تحدي PK للكل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                }
                                Button(
                                    onClick = { onOwnerQuickControlAllRooms("ENABLE_GIFTS_ALL_ROOMS") },
                                    colors = ButtonDefaults.buttonColors(containerColor = WanasAmberGold, contentColor = Color(0xFF1A103C)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("owner_super_enable_gifts_all_rooms_btn")
                                ) {
                                    Text("🎁 تفعيل هدايا الكل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { onOwnerQuickControlAllRooms("UNLOCK_ALL_PASSWORDS") },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("owner_super_unlock_all_rooms_btn")
                                ) {
                                    Text("🔓 فك باسورد الغرف", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                }
                                FilledTonalButton(
                                    onClick = {
                                        onOwnerQuickControlAllMembers(
                                            grantTargetMemberInput,
                                            "REWARD_ALL_ACTIVE_MEMBERS",
                                            grantCoinsAmountInput.toIntOrNull() ?: 150
                                        )
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = WanasEmeraldOnline,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("owner_super_reward_all_members_btn")
                                ) {
                                    Text("🎉 مكافأة كل الأعضاء", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                }
                                FilledTonalButton(
                                    onClick = {
                                        onOwnerQuickControlAllMembers(
                                            grantTargetMemberInput,
                                            "UNBAN_ALL_SUSPENDED",
                                            0
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("owner_super_unban_all_members_btn")
                                ) {
                                    Text("✅ فك إيقاف الأعضاء", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }

                    Text(
                        text = "🎁 منح عملات مكافأة أو ترقية VIP أو تصفير مخالفات لعضو من الإدارة:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WanasAmberGold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = grantTargetMemberInput,
                            onValueChange = { grantTargetMemberInput = it },
                            label = { Text("اسم العضو (اتركه فارغاً لحسابك)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = WanasAmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("admin_grant_member_name_input")
                        )
                        OutlinedTextField(
                            value = grantCoinsAmountInput,
                            onValueChange = { grantCoinsAmountInput = it },
                            label = { Text("عدد Coins") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = WanasAmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(0.7f)
                                .testTag("admin_grant_coins_amount_input")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val amt = grantCoinsAmountInput.toIntOrNull() ?: 100
                                onGrantBonusCoinsByAdmin(grantTargetMemberInput, amt, false)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasAmberGold,
                                contentColor = Color(0xFF1A103C)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_grant_coins_only_button")
                        ) {
                            Text("🪙 منح العملات", fontWeight = FontWeight.ExtraBold)
                        }
                        Button(
                            onClick = {
                                val amt = grantCoinsAmountInput.toIntOrNull() ?: 250
                                onGrantBonusCoinsByAdmin(grantTargetMemberInput, amt, true)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasVioletAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_grant_vip_and_coins_button")
                        ) {
                            Text("👑 منح Coins + شارة VIP", fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // 🎙️ 9. Full Rooms Master Control Center inside Admin Dashboard ("تحكم كامل بجميع الغرف حتى لو بباسورد")
                    if (availableRooms.isNotEmpty()) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                        Text(
                            text = "🎙️ إدارة جميع الغرف الصوتية (${availableRooms.size} غرف — شاملة الغرف بباسورد):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        availableRooms.forEach { rm ->
                            Surface(
                                color = Color.White.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_room_control_item_${rm.roomId}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${if (rm.isPinnedByAdmin) "📌 " else ""}${rm.roomName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (rm.hasPassword) {
                                                    "🔒 محمية بباسورد (الكود الظاهر للإدارة: ${rm.roomPassword}) • ${rm.micSeatsCount} مايكات"
                                                } else {
                                                    "🌐 غرفة عامة مفتوحة • ${rm.micSeatsCount} مايكات"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WanasEmeraldOnline
                                            )
                                        }
                                        Button(
                                            onClick = { onAdminEnterRoomDirect(rm) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = WanasAmberGold,
                                                contentColor = Color(0xFF1A103C)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("admin_direct_enter_room_${rm.roomId}")
                                        ) {
                                            Text("دخول فوري", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        FilledTonalButton(
                                            onClick = { onAdminPinOrLockRoom(rm.roomId, true, false) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (rm.isPinnedByAdmin) "إلغاء التثبيت" else "📌 تثبيت بالصدارة",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                val nextPass = if (rm.hasPassword) "" else "2026"
                                                onAdminUpdateRoomPassword(rm.roomId, nextPass)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (rm.hasPassword) "🔓 فك الباسورد" else "🔒 قفل (2026)",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        Button(
                                            onClick = { onAdminDeleteRoom(rm.roomId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = WanasCoralWarm),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("🗑️ حذف", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🛡️ Safety Rules & Disclaimer (+18 & Non-therapy disclaimer)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔞 قواعد الأمان والاستخدام المحترم (+18):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary
                )
                WanasCatalogData.safetyRulesList.forEach { rule ->
                    Text(
                        text = "• $rule",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSub
                    )
                }

                HorizontalDivider(color = textSub.copy(alpha = 0.2f))

                Text(
                    text = "⚠️ تقديم بلاغ فوري للإدارة:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary
                )
                OutlinedTextField(
                    value = reportTargetInput,
                    onValueChange = { reportTargetInput = it },
                    label = { Text("اسم العضو أو الغرفة المخالفة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reportReasonInput,
                    onValueChange = { reportReasonInput = it },
                    label = { Text("سبب البلاغ (سلوك مخالف / إزعاج / كلمات غير لائقة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (reportTargetInput.isNotBlank()) {
                            onSubmitReport(reportTargetInput, reportReasonInput)
                            reportTargetInput = ""
                            reportReasonInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WanasCoralWarm,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_safety_report_button")
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إرسال البلاغ للإدارة", fontWeight = FontWeight.ExtraBold)
                }

                // Active Reports Queue for Admin Moderation (ONLY visible to App Owner/Admin)
                if (actionState.isAdminOwner && actionState.safetyReports.isNotEmpty()) {
                    HorizontalDivider(color = textSub.copy(alpha = 0.2f))
                    Text(
                        text = "👮 مراجعة البلاغات الواردة (${actionState.safetyReports.size}):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = textPrimary
                    )
                    actionState.safetyReports.forEach { rep ->
                        Surface(
                            color = if (isDarkMode) Color.White.copy(alpha = 0.06f) else WanasDaySurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "بلاغ ضد: ${rep.reportedUserName} (${rep.status})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "السبب: ${rep.reasonAr} • المبلّغ: ${rep.reporterName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textSub
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onResolveReportByAdmin(rep.reportId, "تم الحظر النهائي 🚫") },
                                        colors = ButtonDefaults.buttonColors(containerColor = WanasCoralWarm),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("🚫 حظر الحساب", style = MaterialTheme.typography.labelSmall)
                                    }
                                    FilledTonalButton(
                                        onClick = { onResolveReportByAdmin(rep.reportId, "تم الإيقاف المؤقت ⏸️") },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("⏸️ إيقاف مؤقت", style = MaterialTheme.typography.labelSmall)
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
private fun AdminMetricGroupRow(
    groupTitle: String,
    items: List<Pair<String, String>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = groupTitle,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = WanasTealCalm
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { (label, value) ->
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}
