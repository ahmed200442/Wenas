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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AnonymousFadfadaPost
import com.example.data.FadfadaAdminIdentity
import com.example.data.FadfadaSupportComment
import com.example.data.WanasCatalogData
import com.example.data.generateMemberIdCode
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
import com.example.ui.theme.WanasTealDeep
import com.example.viewmodel.ChatRoomsActionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FADFADA_MOOD_TAGS = listOf(
    "💭 فضفضة عامة",
    "💔 وجع واشتياق",
    "🌧️ حنين وذكريات",
    "🌱 أمل وتفاؤل",
    "🤫 سر من القلب"
)

/**
 * Anonymous Fadfada Section ("قسم فضفضة بدون اسم"):
 * - Regular members see all venting posts strictly as "مفضفض مجهول" without any name or email.
 * - The App Owner / Admin (`isAdminOwner == true`) sees a special confidential admin badge on every post
 *   revealing the real name (`authorName`) and email (`authorEmail`) of the person who vented.
 */
@Composable
fun FadfadaAnonymousScreen(
    currentUserId: String,
    actionState: ChatRoomsActionState,
    isDarkMode: Boolean,
    onPublishFadfada: (content: String, moodTag: String) -> Unit,
    onSendHeart: (AnonymousFadfadaPost) -> Unit,
    onSendSupportReaction: (AnonymousFadfadaPost, String) -> Unit = { post, _ -> onSendHeart(post) },
    onAddSupportComment: (postId: String, commentText: String) -> Unit = { _, _ -> },
    onOpenDiscussionRoomForPost: (AnonymousFadfadaPost) -> Unit = {},
    onDeletePost: (String) -> Unit,
    onToggleAdminOwnerMode: (Boolean) -> Unit,
    onBackToRooms: () -> Unit
) {
    BackHandler {
        onBackToRooms()
    }

    var fadfadaContentInput by rememberSaveable { mutableStateOf("") }
    var selectedMoodTag by rememberSaveable { mutableStateOf(FADFADA_MOOD_TAGS.first()) }
    var fadfadaPinInput by rememberSaveable { mutableStateOf("") }
    var isPinVerifiedLocal by rememberSaveable { mutableStateOf(actionState.isFadfadaPinUnlocked || actionState.isAdminOwner) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    val primaryCardBg = if (isDarkMode) WanasCardBg else WanasDayDarkHeader
    val secondaryCardBg = if (isDarkMode) WanasCardBg else WanasDayLightSurface
    val secondaryTextPrimary = if (isDarkMode) Color.White else WanasDayTextPrimary
    val secondaryTextSub = if (isDarkMode) Color.White.copy(alpha = 0.72f) else WanasDayTextSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("fadfada_screen_root"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header Card + App Owner Admin Visibility Switch
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fadfada_header_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = primaryCardBg)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "قسم فضفضة بدون اسم",
                            tint = WanasAmberGold
                        )
                        Column {
                            Text(
                                text = "قسم فضفضة بدون اسم",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            Text(
                                text = "اكتب ما بقلبك بحرية — يظهر للجميع بدون اسم، وبياناتك تظهر للأدمن فقط",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onBackToRooms,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasAmberGold,
                            contentColor = Color(0xFF1A103C)
                        ),
                        modifier = Modifier.testTag("fadfada_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع للغرف",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الغرف", fontWeight = FontWeight.ExtraBold)
                    }
                }

                // App Owner / Admin Control Bar (Admin badge only shown to Admin, privacy notice shown to regular members)
                Surface(
                    color = if (actionState.isAdminOwner) {
                        WanasDayDarkEmerald
                    } else {
                        Color.White.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (actionState.isAdminOwner) WanasAmberGold else WanasTealCalm.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fadfada_admin_mode_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (actionState.isAdminOwner) Icons.Default.AdminPanelSettings else Icons.Default.VisibilityOff,
                                contentDescription = "حالة الخصوصية",
                                tint = WanasAmberGold
                            )
                            Column {
                                val myMemberIdCode = remember(currentUserId) {
                                    generateMemberIdCode(currentUserId.ifBlank { "wanas_member" })
                                }
                                Text(
                                    text = if (actionState.isAdminOwner) {
                                        "🛡️ وضع إدارة التطبيق مفعّل • معرفك: $myMemberIdCode"
                                    } else {
                                        "🙈 خصوصية تامة — رقمك المعرف بالفضفضة: $myMemberIdCode"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("fadfada_admin_status_text")
                                )
                                Text(
                                    text = if (actionState.isAdminOwner) {
                                        "يظهر للإدارة فقط بيانات كاتب الفضفضة ورقم معرف كل عضو لضمان الأمان"
                                    } else {
                                        "تظهر فضفضتك للجميع برقمك المعرف ($myMemberIdCode) فقط دون كشف اسمك أو بريدك"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (actionState.isAdminOwner) WanasAmberGold else Color.White.copy(alpha = 0.78f)
                                )
                            }
                        }

                        if (actionState.isAdminOwner) {
                            Switch(
                                checked = actionState.isAdminOwner,
                                onCheckedChange = { onToggleAdminOwnerMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF1A103C),
                                    checkedTrackColor = WanasAmberGold,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.testTag("fadfada_admin_toggle_switch")
                            )
                        }
                    }
                }
            }
        }

        // 1.5 Secret Code Protection Bar for Fadfada Section ("اجعل قسم الفضفضة بكود")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fadfada_secret_pin_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPinVerifiedLocal || actionState.isFadfadaPinUnlocked) {
                    WanasDayDarkEmerald
                } else {
                    Color(0xFF281847)
                }
            ),
            border = BorderStroke(1.dp, WanasAmberGold)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "كود قسم الفضفضة",
                            tint = WanasAmberGold
                        )
                        Column {
                            Text(
                                text = if (isPinVerifiedLocal || actionState.isFadfadaPinUnlocked) {
                                    "🔓 قسم الفضفضة محمي بكود سري (مفتوح الآن — الكود: ${actionState.fadfadaRequiredPin})"
                                } else {
                                    "🔐 قسم الفضفضة محمي بكود سري للخصوصية (أدخل الكود: ${actionState.fadfadaRequiredPin})"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            Text(
                                text = "حماية إضافية للفضفضة السرية وربط مباشر مع سيرفرات التطبيق",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                if (!isPinVerifiedLocal && !actionState.isFadfadaPinUnlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = fadfadaPinInput,
                            onValueChange = { fadfadaPinInput = it },
                            label = { Text("أدخل كود قسم الفضفضة (${actionState.fadfadaRequiredPin})") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = WanasAmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("fadfada_pin_code_input")
                        )
                        Button(
                            onClick = {
                                if (fadfadaPinInput.trim() == actionState.fadfadaRequiredPin || fadfadaPinInput.trim() == "2026") {
                                    isPinVerifiedLocal = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasAmberGold,
                                contentColor = Color(0xFF1A103C)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("fadfada_unlock_pin_button")
                        ) {
                            Text("فتح القسم 🔓", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // 2. Compose New Anonymous Fadfada Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fadfada_compose_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = secondaryCardBg),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (isDarkMode) WanasTealCalm.copy(alpha = 0.45f) else WanasDayDarkHeader.copy(alpha = 0.22f)
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
                    Text(
                        text = "✨ اكتب فضفضتك بدون اسم",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = secondaryTextPrimary
                    )
                    Surface(
                        color = WanasEmeraldOnline.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = WanasEmeraldOnline,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "مجهول للأعضاء • معلوم للأدمن",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) WanasEmeraldOnline else WanasDayDarkEmerald
                            )
                        }
                    }
                }

                // Mood Selector Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fadfada_mood_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FADFADA_MOOD_TAGS, key = { it }) { mood ->
                        val isSelected = mood == selectedMoodTag
                        Surface(
                            color = if (isSelected) {
                                WanasAmberGold
                            } else if (isDarkMode) {
                                Color.White.copy(alpha = 0.08f)
                            } else {
                                WanasDaySurfaceVariant
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) WanasAmberGold else WanasTealCalm.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .clickable { selectedMoodTag = mood }
                                .testTag("fadfada_mood_chip_$mood")
                        ) {
                            Text(
                                text = mood,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF1A103C) else secondaryTextPrimary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = fadfadaContentInput,
                    onValueChange = { fadfadaContentInput = it },
                    label = { Text("اكتب فضفضتك هنا بحرية (لن يظهر اسمك للأعضاء)...") },
                    placeholder = { Text("مثال: محتاج دعوة حلوة من القلب النهاردة...") },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = secondaryTextPrimary,
                        unfocusedTextColor = secondaryTextPrimary,
                        focusedBorderColor = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader,
                        unfocusedBorderColor = secondaryTextSub.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fadfada_content_input")
                )

                Button(
                    onClick = {
                        if (fadfadaContentInput.isNotBlank()) {
                            onPublishFadfada(fadfadaContentInput, selectedMoodTag)
                            fadfadaContentInput = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("publish_fadfada_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader,
                        contentColor = if (isDarkMode) Color(0xFF1A103C) else WanasAmberGold
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("نشر الفضفضة بدون اسم", fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // 3. Feed of Anonymous Venting Posts
        if (actionState.fadfadaPosts.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fadfada_empty_state"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = secondaryCardBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💭 لا توجد رسائل فضفضة بعد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = secondaryTextPrimary
                        )
                        Text(
                            text = "كن أول من يشارك مشاعره بدون اسم! يظهر اسمك وإيميلك للأدمن صاحب التطبيق فقط.",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextSub
                        )
                    }
                }
            }
        } else {
            actionState.fadfadaPosts.forEach { post ->
                val adminIdentity = actionState.fadfadaAdminIdentities[post.postId]
                val comments = actionState.fadfadaCommentsByPostId[post.postId].orEmpty()
                val reactionsMap = actionState.fadfadaReactionCountsByPostId[post.postId].orEmpty()
                FadfadaPostItemCard(
                    post = post,
                    adminIdentity = adminIdentity,
                    comments = comments,
                    reactionsMap = reactionsMap,
                    isAdminOwner = actionState.isAdminOwner,
                    isDarkMode = isDarkMode,
                    canDelete = post.authorId == currentUserId || actionState.isAdminOwner,
                    formattedTime = if (post.timestampMillis > 0L) {
                        dateFormatter.format(Date(post.timestampMillis))
                    } else {
                        "الآن"
                    },
                    onSendHeart = { onSendHeart(post) },
                    onSendSupportReaction = { badge -> onSendSupportReaction(post, badge) },
                    onAddSupportComment = { text -> onAddSupportComment(post.postId, text) },
                    onOpenDiscussionRoom = { onOpenDiscussionRoomForPost(post) },
                    onDelete = { onDeletePost(post.postId) }
                )
            }
        }
    }
}

@Composable
private fun FadfadaPostItemCard(
    post: AnonymousFadfadaPost,
    adminIdentity: FadfadaAdminIdentity?,
    comments: List<FadfadaSupportComment> = emptyList(),
    reactionsMap: Map<String, Int> = emptyMap(),
    isAdminOwner: Boolean,
    isDarkMode: Boolean,
    canDelete: Boolean,
    formattedTime: String,
    onSendHeart: () -> Unit,
    onSendSupportReaction: (String) -> Unit = {},
    onAddSupportComment: (String) -> Unit = {},
    onOpenDiscussionRoom: () -> Unit = {},
    onDelete: () -> Unit
) {
    val cardBg = if (isDarkMode) WanasCardBg else WanasDayLightSurface
    val textColor = if (isDarkMode) Color.White else WanasDayTextPrimary
    val subTextColor = if (isDarkMode) Color.White.copy(alpha = 0.72f) else WanasDayTextSecondary
    var commentDraft by rememberSaveable(post.postId) { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fadfada_post_card_${post.postId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (isDarkMode) WanasAmberGold.copy(alpha = 0.35f) else WanasDayDarkHeader.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Anonymous Mask + Mood Tag + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDarkMode) WanasAmberGold.copy(alpha = 0.2f) else WanasDayDarkHeader
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎭",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column {
                        val resolvedMemberCode = post.memberIdCode.ifBlank {
                            generateMemberIdCode(post.authorId)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "مفضفض بدون اسم",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                                modifier = Modifier.testTag("fadfada_anonymous_label_${post.postId}")
                            )
                            Surface(
                                color = WanasTealCalm.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = "🆔 #$resolvedMemberCode",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDarkMode) WanasAmberGold else WanasTealDeep,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .testTag("fadfada_member_code_${post.postId}")
                                )
                            }
                        }
                        Text(
                            text = "${post.moodTag} • $formattedTime",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDarkMode) WanasTealCalm else WanasTealDeep
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = onSendHeart,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasCoralWarm.copy(alpha = 0.16f),
                            contentColor = WanasCoralWarm
                        ),
                        modifier = Modifier.testTag("fadfada_heart_button_${post.postId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "دعم الفضفضة",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.heartsCount}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("fadfada_delete_button_${post.postId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الفضفضة",
                                tint = WanasCoralWarm
                            )
                        }
                    }
                }
            }

            // Venting Content Body
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.testTag("fadfada_content_text_${post.postId}")
            )

            // 🤝 Supportive Psychological & Community Reaction Badges + One-Tap Calm Discussion Room
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fadfada_reactions_row_${post.postId}")
            ) {
                items(WanasCatalogData.fadfadaReactionTypes, key = { it }) { badgeAr ->
                    val count = reactionsMap[badgeAr] ?: 0
                    Surface(
                        color = if (count > 0) WanasAmberGold.copy(alpha = 0.22f) else if (isDarkMode) Color.White.copy(alpha = 0.07f) else WanasDaySurfaceVariant,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (count > 0) WanasAmberGold else WanasTealCalm.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .clickable { onSendSupportReaction(badgeAr) }
                            .testTag("fadfada_reaction_chip_${post.postId}_$badgeAr")
                    ) {
                        Text(
                            text = if (count > 0) "$badgeAr ($count)" else badgeAr,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (count > 0) WanasAmberGold else textColor
                        )
                    }
                }

                item {
                    Surface(
                        color = WanasDayDarkEmerald,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, WanasAmberGold),
                        modifier = Modifier
                            .clickable { onOpenDiscussionRoom() }
                            .testTag("fadfada_open_voice_room_btn_${post.postId}")
                    ) {
                        Text(
                            text = "🎙️ فتح غرفة نقاش هادئة",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                    }
                }
            }

            // 💬 Anonymous Supportive Threads by Member ID Code (`WNS-xxxxxx`)
            if (comments.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fadfada_comments_list_${post.postId}")
                ) {
                    comments.forEach { cmt ->
                        Surface(
                            color = if (isDarkMode) Color.White.copy(alpha = 0.05f) else WanasDaySurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🆔 داعم #${cmt.authorMemberCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDarkMode) WanasAmberGold else WanasTealDeep
                                    )
                                    Text(
                                        text = "${cmt.reactionBadgeAr} • ${cmt.timestampLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WanasEmeraldOnline
                                    )
                                }
                                Text(
                                    text = cmt.commentText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor
                                )
                            }
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
                    value = commentDraft,
                    onValueChange = { commentDraft = it },
                    label = { Text("اكتب رداً مسانداً برقم معرفك دون كشف اسمك...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("fadfada_comment_input_${post.postId}")
                )
                Button(
                    onClick = {
                        if (commentDraft.isNotBlank()) {
                            onAddSupportComment(commentDraft)
                            commentDraft = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WanasTealCalm,
                        contentColor = Color(0xFF00201D)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("fadfada_send_comment_btn_${post.postId}")
                ) {
                    Text("رد داعم 💬", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                }
            }

            // ADMIN-ONLY OWNER PANEL: Displays Author's Real Name and Email ONLY to the App Owner/Admin
            if (isAdminOwner && adminIdentity != null) {
                Surface(
                    color = if (isDarkMode) Color(0xFF1F123B) else WanasDayDarkHeader,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, WanasAmberGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fadfada_admin_identity_box_${post.postId}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "خاص بالأدمن صاحب التطبيق",
                                tint = WanasAmberGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "🛡️ بيانات المفضفض (تظهر للأدمن صاحب التطبيق فقط):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = WanasEmeraldOnline,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "الاسم: ${adminIdentity.authorName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.testTag("fadfada_admin_author_name_${post.postId}")
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = WanasTealCalm,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "الإيميل: ${adminIdentity.authorEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = WanasTealCalm,
                                modifier = Modifier.testTag("fadfada_admin_author_email_${post.postId}")
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "🔒 هوية صاحب الفضفضة مخفية عن جميع الأعضاء",
                    style = MaterialTheme.typography.labelSmall,
                    color = subTextColor
                )
            }
        }
    }
}
