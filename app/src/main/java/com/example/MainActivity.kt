package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Star
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.example.data.ChatRoomMetadata
import com.example.data.ChatRoomRepository
import com.example.data.LuckyCoinBoxState
import com.example.data.MatchQueueRequest
import com.example.data.MatchRole
import com.example.data.MatchTopicType
import com.example.data.PushNotificationEvent
import com.example.data.RoomActiveMember
import com.example.data.RoomAdminModuleTab
import com.example.data.RoomChatMessage
import com.example.data.RoomGiftTransactionDocument
import com.example.data.RoomMicRequestItem
import com.example.data.RoomPresenceBannerEvent
import com.example.data.RoomTopSupporterItem
import com.example.data.MicRequestStatus
import com.example.data.VoiceRoomSeatSlot
import com.example.data.VoiceSeatRoleBadge
import com.example.data.SupabaseAccountService
import com.example.data.SupabaseMemberAccount
import com.example.data.WanasAppSection
import com.example.data.WanasCatalogData
import com.example.data.WanasGiftItem
import com.example.data.WanasRoomCategoryFilter
import com.example.notifications.PushNotificationHelper
import com.example.voice.RealVoiceRoomEngine
import com.example.voice.VoiceEngineState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.AlertDialog
import com.example.ui.screens.ActiveTenMinuteMatchCallCard
import com.example.ui.screens.FadfadaAnonymousScreen
import com.example.ui.screens.PushNotificationsAndPaymentScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.WanasCoinsAndGiftsScreen
import com.example.ui.screens.WanasFourHeroChoicesSection
import com.example.ui.screens.WanasFriendsHubScreen
import com.example.ui.screens.WanasModernSectionDirectoryCard
import com.example.ui.screens.WanasModernWelcomeDialog
import com.example.ui.screens.WanasSafetyAndAdminDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WanasAmberGold
import com.example.ui.theme.WanasCardBg
import com.example.ui.theme.WanasCoralWarm
import com.example.ui.theme.WanasDayDarkEmerald
import com.example.ui.theme.WanasDayDarkHeader
import com.example.ui.theme.WanasDayLightBg
import com.example.ui.theme.WanasDayLightSurface
import com.example.ui.theme.WanasDaySurfaceVariant
import com.example.ui.theme.WanasDayTextPrimary
import com.example.ui.theme.WanasDayTextSecondary
import com.example.ui.theme.WanasDeepBg
import com.example.ui.theme.WanasEmeraldOnline
import com.example.ui.theme.WanasTealCalm
import com.example.ui.theme.WanasTealDeep
import com.example.ui.theme.WanasVioletAccent
import com.example.viewmodel.ChatRoomViewModel
import com.example.viewmodel.UiState
import com.example.viewmodel.attemptAutoSignIn
import com.example.viewmodel.onGoogleSignInClicked
import com.example.viewmodel.signOut
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("wanas_theme_prefs", MODE_PRIVATE)
        setContent {
            var isDarkMode by rememberSaveable {
                mutableStateOf(prefs.getBoolean("is_dark_mode", true))
            }
            val updateTheme: (Boolean) -> Unit = { dark ->
                isDarkMode = dark
                prefs.edit().putBoolean("is_dark_mode", dark).apply()
            }
            MyApplicationTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    isDarkMode = isDarkMode,
                    onToggleTheme = { updateTheme(!isDarkMode) },
                    onSetDarkMode = updateTheme
                )
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(
    isDarkMode: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onSetDarkMode: (Boolean) -> Unit = { onToggleTheme() },
    auth: FirebaseAuth = Firebase.auth
) {
    val context = LocalContext.current
    val supabaseService = remember(context) { SupabaseAccountService(context) }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    // Start on the Wanas v3.1 Welcome & Instant Entry Screen (matching the user's v3.1 screenshot)
    var savedMemberAccount by remember {
        mutableStateOf<SupabaseMemberAccount?>(null)
    }

    val activeUserId = savedMemberAccount?.userId
    val activeDisplayName = savedMemberAccount?.displayName
        ?: currentUser?.displayName
        ?: supabaseService.getLastSavedNickname()
    val activeEmail = savedMemberAccount?.email
        ?: currentUser?.email
        ?: supabaseService.getLastSavedEmail()

    if (activeUserId == null) {
        AuthGateScreen(
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            supabaseService = supabaseService,
            onMemberAuthenticated = { memberAccount ->
                savedMemberAccount = memberAccount
            }
        )
    } else {
        ChatRoomMetadataScreen(
            currentUserId = activeUserId,
            currentUserDisplayName = activeDisplayName,
            currentUserEmail = activeEmail,
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            onSetDarkMode = onSetDarkMode,
            supabaseService = supabaseService,
            onSignedOut = {
                savedMemberAccount = null
            }
        )
    }
}

@Composable
fun AuthGateScreen(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    supabaseService: SupabaseAccountService,
    onMemberAuthenticated: (SupabaseMemberAccount) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isRegisterTab by rememberSaveable { mutableStateOf(true) }
    var nicknameInput by rememberSaveable { mutableStateOf(supabaseService.getLastSavedNickname()) }
    var emailInput by rememberSaveable { mutableStateOf(supabaseService.getLastSavedEmail()) }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var confirmPasswordInput by rememberSaveable { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = scope
        )
    }

    val backgroundBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF120826),
            Color(0xFF1A0E36),
            Color(0xFF140A2C)
        )
    )

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = WanasAmberGold,
        unfocusedBorderColor = Color(0xFF5B4982),
        focusedLabelColor = WanasAmberGold,
        unfocusedLabelColor = Color.White.copy(alpha = 0.78f),
        cursorColor = WanasAmberGold
    )

    val triggerInstantQuickEntry: () -> Unit = {
        val quickName = nicknameInput.trim().ifBlank {
            supabaseService.getLastSavedNickname().ifBlank { "أحمد المصري" }
        }
        val quickEmail = emailInput.trim().ifBlank {
            supabaseService.getLastSavedEmail().ifBlank { "hamadanagy1979@gmail.com" }
        }
        val quickAccount = SupabaseMemberAccount(
            userId = "usr_wanas_quick_${System.currentTimeMillis()}",
            memberIdCode = "WNS-777777",
            displayName = quickName,
            email = quickEmail,
            roleBadge = "👑 عضو ونس المميز",
            supabaseSynced = true
        )
        supabaseService.saveAccountForAutoLogin(quickAccount)
        onMemberAuthenticated(quickAccount)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("auth_screen_root"),
        containerColor = Color(0xFF120826),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Header Strip: "☀️ الوضع النهاري" on left, "وَنَس — الناس للناس" on right
            Surface(
                color = Color(0xFF21153B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clickable { onToggleTheme() }
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .testTag("auth_theme_toggle_button")
                    ) {
                        Text(
                            text = if (isDarkMode) "الوضع النهاري ☀️" else "الوضع الليلي 🌙",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "وَنَس — الناس للناس",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = WanasAmberGold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Hero Welcome Card with Gold Border & Egyptian Night Illustration
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wanas_v31_hero_welcome_card"),
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF191229),
                    border = BorderStroke(2.dp, WanasAmberGold)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background Egyptian Night Artwork
                        Image(
                            painter = painterResource(id = R.drawable.img_wanas_egypt_night_hero_1791481821178),
                            contentDescription = "ليالي ونس الدافئة",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(28.dp))
                        )

                        // Dark gradient scrim so Arabic typography pops clearly like the v3.1 screenshot
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF180E2E).copy(alpha = 0.45f),
                                            Color(0xFF180E2E).copy(alpha = 0.68f),
                                            Color(0xFF19141E).copy(alpha = 0.94f)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Top Gold Pill: "✨ أهلاً ومرحباً بك في وَنَس — الناس للناس ★"
                            Surface(
                                color = WanasAmberGold,
                                shape = RoundedCornerShape(50),
                                shadowElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("✨", fontSize = 15.sp)
                                    Text(
                                        text = "أهلاً ومرحباً بك في وَنَس — الناس للناس",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1A103C)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFF1A103C),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Center Glowing Circular Microphone Emblem
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(WanasAmberGold.copy(alpha = 0.18f))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = CircleShape,
                                    color = Color(0xFF1D1138),
                                    border = BorderStroke(2.5.dp, WanasAmberGold)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "مايك ونس",
                                            tint = WanasAmberGold,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            // Main Bold White Headline
                            Text(
                                text = "نورت بيتك الدافئ للصحبة الطيبة\nوالفضفضة الصادقة 🌙🎙️",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 34.sp
                            )

                            // Subtitle Paragraph
                            Text(
                                text = "في «وَنَس» الكلمة الحلوة بتجمعنا والقلوب بتسمع بعض..\nاتكلم براحتك، اسمع من قلبك، وشارك أجمل السهرات\nالصوتية مع أصدقاء حقيقيين في أمان وخصوصية تامة.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.92f),
                                textAlign = TextAlign.Center,
                                lineHeight = 23.sp
                            )

                            // 3 Feature Pills Row: أعضاء حقيقيون 🤝 | غرف وسهرات 🌙 | جلسات ١٠ دقائق 🎙️
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF2B2438).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.55f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "أعضاء حقيقيون 🤝",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Surface(
                                    color = Color(0xFF2B2438).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.55f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "غرف وسهرات 🌙",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Surface(
                                    color = Color(0xFF2B2438).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.55f)),
                                    modifier = Modifier.weight(1.1f)
                                ) {
                                    Text(
                                        text = "جلسات ١٠ دقائق 🎙️",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Instant One-Tap Entry Card ("⚡ دخول فوري بضغطة واحدة — بدون تعقيد وبدون تأكيد")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instant_one_tap_entry_card"),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF231542),
                    border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "دخول فوري بضغطة واحدة — بدون تعقيد وبدون تأكيد",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⚡", fontSize = 18.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Button: دخول بـ Google
                            Surface(
                                color = Color(0xFF144B46),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .clickable(enabled = !isLoading) {
                                        isLoading = true
                                        authError = null
                                        onGoogleSignInClicked(
                                            context = context,
                                            credentialManager = credentialManager,
                                            onAuthSuccess = {
                                                isLoading = false
                                                val fbUser = Firebase.auth.currentUser
                                                if (fbUser != null) {
                                                    val googleAccount = SupabaseMemberAccount(
                                                        userId = fbUser.uid,
                                                        displayName = fbUser.displayName?.ifBlank { null }
                                                            ?: nicknameInput.ifBlank { "عضو ونس" },
                                                        email = fbUser.email ?: emailInput,
                                                        supabaseSynced = true
                                                    )
                                                    supabaseService.saveAccountForAutoLogin(googleAccount)
                                                    onMemberAuthenticated(googleAccount)
                                                } else {
                                                    triggerInstantQuickEntry()
                                                }
                                            },
                                            onAuthError = {
                                                isLoading = false
                                                triggerInstantQuickEntry()
                                            },
                                            scope = scope,
                                            onAuthCancelled = { isLoading = false }
                                        )
                                    }
                                    .testTag("google_sign_in_button")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "دخول بـ\nGoogle",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Google",
                                        tint = WanasAmberGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Right Button: دخول سريع فوري
                            Button(
                                onClick = { triggerInstantQuickEntry() },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .testTag("instant_quick_entry_button")
                            ) {
                                Text(
                                    text = "دخول سريع\nفوري",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Register / Login Card ("تسجيل الدخول 🔑 | تسجيل حساب جديد ✨")
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_card"),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color(0xFF21143D)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pill Switcher Container
                        Surface(
                            color = Color(0xFF160D2B),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Tab: تسجيل الدخول 🔑
                                Surface(
                                    color = if (!isRegisterTab) WanasAmberGold else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clickable {
                                            isRegisterTab = false
                                            authError = null
                                        }
                                        .testTag("tab_login_button")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "تسجيل الدخول",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (!isRegisterTab) Color(0xFF1A103C) else Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = if (!isRegisterTab) Color(0xFF1A103C) else WanasAmberGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Right Tab: تسجيل حساب جديد ✨
                                Surface(
                                    color = if (isRegisterTab) WanasAmberGold else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clickable {
                                            isRegisterTab = true
                                            authError = null
                                        }
                                        .testTag("tab_register_button")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "تسجيل حساب جديد ✨",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isRegisterTab) Color(0xFF1A103C) else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "اختار اسمك أو لقبك المفضل في «وَنَس» وادخل فوراً بدون أي كود تفعيل أو تعقيد",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )

                        // 1. Name or Nickname field ("اسمك أو لقبك في وَنَس")
                        if (isRegisterTab) {
                            OutlinedTextField(
                                value = nicknameInput,
                                onValueChange = { nicknameInput = it },
                                label = { Text("اسمك أو لقبك في وَنَس") },
                                placeholder = { Text("مثال: أحمد المصري") },
                                trailingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = WanasAmberGold)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = textFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_nickname_input")
                            )
                        }

                        // 2. Email field (optional/quick)
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("البريد الإلكتروني (اختياري للدخول السريع)") },
                            placeholder = { Text("example@email.com") },
                            trailingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = WanasAmberGold)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input")
                        )

                        // 3. Password field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("كلمة المرور (الباسورد)") },
                            trailingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = WanasAmberGold)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input")
                        )

                        if (isRegisterTab) {
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = { confirmPasswordInput = it },
                                label = { Text("تأكيد كلمة المرور") },
                                trailingIcon = {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WanasTealCalm)
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = textFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_confirm_password_input")
                            )
                        }

                        // Submit Register / Login Button (also supports quick entry if fields are left blank)
                        Button(
                            onClick = {
                                if (emailInput.isBlank() && passwordInput.isBlank()) {
                                    triggerInstantQuickEntry()
                                    return@Button
                                }
                                isLoading = true
                                authError = null
                                scope.launch {
                                    val result = if (isRegisterTab) {
                                        supabaseService.registerMemberAccount(
                                            nickname = nicknameInput.ifBlank { "عضو ونس" },
                                            email = emailInput.ifBlank { "user_${System.currentTimeMillis()}@wanas.app" },
                                            password = passwordInput.ifBlank { "123456" },
                                            confirmPassword = confirmPasswordInput.ifBlank { passwordInput.ifBlank { "123456" } },
                                            preferredUid = Firebase.auth.currentUser?.uid
                                        )
                                    } else {
                                        supabaseService.loginMemberAccount(
                                            email = emailInput,
                                            password = passwordInput,
                                            fallbackNickname = nicknameInput
                                        )
                                    }
                                    isLoading = false
                                    result.fold(
                                        onSuccess = { account ->
                                            onMemberAuthenticated(account)
                                        },
                                        onFailure = { err ->
                                            authError = err.localizedMessage ?: "تعذر إتمام العملية"
                                        }
                                    )
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_member_account_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasEmeraldOnline,
                                contentColor = Color.White
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isRegisterTab) Icons.Default.PersonAdd else Icons.AutoMirrored.Filled.Login,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isRegisterTab) {
                                        "دخول فوري وحفظ الحساب تلقائياً ✨"
                                    } else {
                                        "تسجيل الدخول الآن"
                                    },
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (authError != null) {
                            Surface(
                                color = WanasCoralWarm.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_error_banner")
                            ) {
                                Text(
                                    text = authError!!,
                                    modifier = Modifier.padding(12.dp),
                                    color = WanasCoralWarm,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRoomMetadataScreen(
    currentUserId: String,
    currentUserDisplayName: String = "",
    currentUserEmail: String = "",
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onSetDarkMode: (Boolean) -> Unit = { onToggleTheme() },
    supabaseService: SupabaseAccountService = SupabaseAccountService(LocalContext.current),
    onSignedOut: () -> Unit = {},
    viewModel: ChatRoomViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ChatRoomViewModel(
                    repository = ChatRoomRepository(db),
                    currentUserId = currentUserId,
                    currentUserDisplayName = currentUserDisplayName,
                    currentUserEmail = currentUserEmail,
                    supabaseService = SupabaseAccountService(app),
                    pushNotificationHelper = PushNotificationHelper(app)
                )
            }
        }
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val roomsState by viewModel.roomsState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    var roomNameInput by remember { mutableStateOf("") }
    var memberNameInput by remember(actionState.memberDisplayName) {
        mutableStateOf(actionState.memberDisplayName)
    }
    var showProfileView by rememberSaveable { mutableStateOf(false) }
    var showFadfadaView by rememberSaveable { mutableStateOf(false) }
    var showFadfadaPinDialog by rememberSaveable { mutableStateOf(false) }
    var fadfadaDialogPinInput by rememberSaveable { mutableStateOf("") }
    var showNotificationsPaymentView by rememberSaveable { mutableStateOf(false) }
    var notificationsPaymentInitialTab by rememberSaveable { mutableStateOf(0) }
    var showFriendsView by rememberSaveable { mutableStateOf(false) }
    var showCoinsAndGiftsView by rememberSaveable { mutableStateOf(false) }
    var showSafetyAndAdminView by rememberSaveable { mutableStateOf(false) }
    var currentMainSectionId by rememberSaveable { mutableStateOf(WanasAppSection.MATCHING_HUB.id) }
    var selectedRoomFilterId by rememberSaveable { mutableStateOf(WanasRoomCategoryFilter.ALL.id) }

    val activeMainSection = remember(
        currentMainSectionId,
        showProfileView,
        showFadfadaView,
        showFriendsView,
        showCoinsAndGiftsView,
        showSafetyAndAdminView,
        showNotificationsPaymentView
    ) {
        when {
            showFriendsView -> WanasAppSection.FRIENDS_HUB
            showCoinsAndGiftsView -> WanasAppSection.STORE_AND_GIFTS
            showFadfadaView -> WanasAppSection.FADFADA_SECRET
            showProfileView || showSafetyAndAdminView || showNotificationsPaymentView -> WanasAppSection.MY_ACCOUNT
            else -> WanasAppSection.entries.find { it.id == currentMainSectionId } ?: WanasAppSection.MATCHING_HUB
        }
    }

    fun navigateToAppSection(section: WanasAppSection) {
        showProfileView = false
        showFadfadaView = false
        showNotificationsPaymentView = false
        showFriendsView = false
        showCoinsAndGiftsView = false
        showSafetyAndAdminView = false

        when (section) {
            WanasAppSection.MATCHING_HUB -> {
                if (actionState.activeRoom != null) {
                    viewModel.leaveCurrentRoom()
                }
                currentMainSectionId = WanasAppSection.MATCHING_HUB.id
            }
            WanasAppSection.VOICE_ROOMS -> {
                if (actionState.activeRoom != null) {
                    viewModel.leaveCurrentRoom()
                }
                currentMainSectionId = WanasAppSection.VOICE_ROOMS.id
            }
            WanasAppSection.FRIENDS_HUB -> {
                showFriendsView = true
            }
            WanasAppSection.STORE_AND_GIFTS -> {
                showCoinsAndGiftsView = true
            }
            WanasAppSection.FADFADA_SECRET -> {
                if (actionState.isFadfadaPinUnlocked || actionState.isAdminOwner) {
                    showFadfadaView = true
                } else {
                    showFadfadaPinDialog = true
                }
            }
            WanasAppSection.MY_ACCOUNT -> {
                showProfileView = true
            }
        }
    }

    // Modern Welcome Dialog upon entering the app
    if (actionState.showModernWelcomeModal) {
        WanasModernWelcomeDialog(
            memberName = actionState.memberDisplayName,
            serverStatusLabel = actionState.serverConnectionLabel,
            onStartExploring = { viewModel.dismissModernWelcomeModal() }
        )
    }

    // Secret Code PIN Gate Dialog before entering Fadfada Section ("اجعل قسم الفضفضة بكود")
    if (showFadfadaPinDialog) {
        AlertDialog(
            onDismissRequest = { showFadfadaPinDialog = false },
            containerColor = Color(0xFF1A103C),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "🔐 كود الدخول لقسم الفضفضة السري",
                    color = WanasAmberGold,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "لحماية خصوصية الأعضاء، يرجى إدخال كود قسم الفضفضة المعتمد (${actionState.fadfadaRequiredPin}):",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = fadfadaDialogPinInput,
                        onValueChange = { fadfadaDialogPinInput = it },
                        label = { Text("كود الفضفضة (${actionState.fadfadaRequiredPin})") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = WanasAmberGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fadfada_gate_pin_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val unlocked = viewModel.verifyAndUnlockFadfadaPin(
                            fadfadaDialogPinInput.ifBlank { actionState.fadfadaRequiredPin }
                        )
                        if (unlocked) {
                            showFadfadaPinDialog = false
                            showFadfadaView = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WanasAmberGold,
                        contentColor = Color(0xFF1A103C)
                    ),
                    modifier = Modifier.testTag("confirm_fadfada_pin_button")
                ) {
                    Text("🔓 دخول قسم الفضفضة", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showFadfadaPinDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // Handle system Back press when inside an active room
    if (actionState.activeRoom != null &&
        !showProfileView &&
        !showFadfadaView &&
        !showNotificationsPaymentView &&
        !showFriendsView &&
        !showCoinsAndGiftsView &&
        !showSafetyAndAdminView
    ) {
        BackHandler {
            viewModel.leaveCurrentRoom()
        }
    }

    val screenBackgroundBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(WanasDeepBg, Color(0xFF171033)))
    } else {
        Brush.verticalGradient(
            listOf(
                WanasDayLightBg,
                Color(0xFFEDE9FE),
                Color(0xFFFFFBEB)
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_room_screen_root"),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkMode) Color(0xFF140C2E) else WanasDayDarkHeader,
                contentColor = Color.White,
                tonalElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wanas_bottom_navigation_bar")
            ) {
                val navSections = listOf(
                    WanasAppSection.MATCHING_HUB,
                    WanasAppSection.VOICE_ROOMS,
                    WanasAppSection.FRIENDS_HUB,
                    WanasAppSection.STORE_AND_GIFTS,
                    WanasAppSection.FADFADA_SECRET,
                    WanasAppSection.MY_ACCOUNT
                )
                navSections.forEach { sec ->
                    val selected = activeMainSection == sec
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navigateToAppSection(sec) },
                        icon = {
                            Text(sec.emoji, style = MaterialTheme.typography.titleMedium)
                        },
                        label = {
                            Text(
                                text = sec.titleAr.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1A103C),
                            selectedTextColor = WanasAmberGold,
                            indicatorColor = WanasAmberGold,
                            unselectedIconColor = Color.White.copy(alpha = 0.8f),
                            unselectedTextColor = Color.White.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier.testTag("bottom_nav_item_${sec.id}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackgroundBrush)
                .padding(innerPadding)
        ) {
            if (showProfileView) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    UserProfileScreen(
                        userId = currentUserId,
                        actionState = actionState,
                        isDarkMode = isDarkMode,
                        onToggleTheme = { dark -> onSetDarkMode(dark) },
                        onUpdateNotificationSettings = { notifyFriendRoom, notifyFollowedRoomLive, notifyPrivateInvite ->
                            viewModel.updateInAppNotificationPreferences(
                                notifyFavoriteLive = notifyFollowedRoomLive ?: actionState.notifyOnFavoriteRoomLive,
                                notifyPrivateInvites = notifyPrivateInvite ?: actionState.notifyOnPrivateInvite,
                                notifyFriendStartsRoom = notifyFriendRoom ?: actionState.notifyOnFriendStartsRoom
                            )
                        },
                        onOpenNotificationsCenter = {
                            showProfileView = false
                            notificationsPaymentInitialTab = 0
                            showNotificationsPaymentView = true
                        },
                        onSaveProfile = { name, uri, emoji, bio ->
                            viewModel.updateFullMemberProfile(name, uri, emoji, bio)
                        },
                        onSendQuickChatMessage = { msg ->
                            viewModel.sendChatMessageInActiveRoom(msg)
                        },
                        onBackToRooms = { showProfileView = false }
                    )
                }
            } else if (showFadfadaView) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    FadfadaAnonymousScreen(
                        currentUserId = currentUserId,
                        actionState = actionState,
                        isDarkMode = isDarkMode,
                        onPublishFadfada = { content, moodTag ->
                            viewModel.publishAnonymousFadfada(content, moodTag)
                        },
                        onSendHeart = { post ->
                            viewModel.sendHeartToFadfada(post)
                        },
                        onSendSupportReaction = { post, badge ->
                            viewModel.sendFadfadaSupportReaction(post, badge)
                        },
                        onAddSupportComment = { postId, commentText ->
                            viewModel.addFadfadaSupportComment(postId, commentText)
                        },
                        onOpenDiscussionRoomForPost = { post ->
                            showFadfadaView = false
                            currentMainSectionId = WanasAppSection.VOICE_ROOMS.id
                            viewModel.openVoiceDiscussionRoomFromFadfada(post)
                        },
                        onDeletePost = { postId ->
                            viewModel.deleteFadfadaPost(postId)
                        },
                        onToggleAdminOwnerMode = { enabled ->
                            viewModel.setAdminOwnerViewMode(enabled)
                        },
                        onBackToRooms = { showFadfadaView = false }
                    )
                }
            } else if (showFriendsView) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    WanasFriendsHubScreen(
                        actionState = actionState,
                        isDarkMode = isDarkMode,
                        onAddFriendByName = { friendName ->
                            viewModel.addPartnerAsFriend(
                                userId = "friend_${System.currentTimeMillis()}",
                                userName = friendName,
                                userEmoji = "🌟",
                                level = 2
                            )
                        },
                        onRespondToFriendRequest = { friendId, accept ->
                            viewModel.respondToFriendRequest(friendId, accept)
                        },
                        onRemoveFriend = { friendId ->
                            viewModel.removeFriendFromList(friendId)
                        },
                        onCycleFriendPresence = { friendId ->
                            viewModel.cycleFriendPresenceState(friendId)
                        },
                        onTogglePinOrMuteChat = { friendId, togglePin, toggleMute ->
                            viewModel.togglePinOrMuteFriendConversation(friendId, togglePin, toggleMute)
                        },
                        onDeleteDirectMessage = { friendId, messageId ->
                            viewModel.deleteDirectMessageWithFriend(friendId, messageId)
                        },
                        onEnterFriendRoomDirect = { roomId ->
                            val availableRooms = (roomsState as? UiState.Success<List<ChatRoomMetadata>>)?.data.orEmpty()
                            val targetRoom = availableRooms.find { it.roomId == roomId } ?: availableRooms.firstOrNull()
                            if (targetRoom != null) {
                                showFriendsView = false
                                currentMainSectionId = WanasAppSection.VOICE_ROOMS.id
                                viewModel.enterChatRoom(targetRoom)
                            }
                        },
                        onNotifyFriendStartedNewRoom = { friend ->
                            viewModel.notifyFriendStartedNewRoom(friend = friend)
                        },
                        onToggleNotifyFriendStartsRoom = { enabled ->
                            viewModel.updateInAppNotificationPreferences(
                                notifyFavoriteLive = actionState.notifyOnFavoriteRoomLive,
                                notifyPrivateInvites = actionState.notifyOnPrivateInvite,
                                notifyFriendStartsRoom = enabled
                            )
                        },
                        onSendDirectMessage = { friendId, text, isVoice, imageLabel, isBuzz ->
                            viewModel.sendDirectMessageToFriend(friendId, text, isVoice, imageLabel, isBuzz)
                        },
                        onStartVoiceCallWithFriend = { friend ->
                            showFriendsView = false
                            viewModel.startVoiceCallWithMatchedCandidate(
                                MatchQueueRequest(
                                    requestId = "friend_call_${friend.friendUserId}",
                                    userId = friend.friendUserId,
                                    userName = friend.friendName,
                                    userEmoji = friend.friendEmoji,
                                    userLevel = friend.friendLevel,
                                    role = MatchRole.WANT_TO_LISTEN,
                                    topic = MatchTopicType.CASUAL_TALK
                                )
                            )
                        },
                        onBlockFriend = { friend ->
                            viewModel.blockUser(friend.friendUserId, friend.friendName)
                        },
                        onBackToHome = { showFriendsView = false }
                    )
                }
            } else if (showCoinsAndGiftsView) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    WanasCoinsAndGiftsScreen(
                        actionState = actionState,
                        isDarkMode = isDarkMode,
                        onClaimDailyCoins = { viewModel.claimDailyFreeCoins() },
                        onClaimDailyStreakDay = { dayNum -> viewModel.claimDailyFreeCoins(dayNum) },
                        onClaimDailyMission = { missionId -> viewModel.claimCompletedDailyMission(missionId) },
                        onSendFreeDailyGift = {
                            viewModel.claimDailyFreeGift()
                        },
                        onWatchRewardedAd = { viewModel.watchRewardedAdForFreeCoins() },
                        onSpinWheelOfFortune = { viewModel.spinDailyWheelOfFortune() },
                        onActivateFeature = { title, cost, isBoost, isPriority ->
                            viewModel.activateCoinFeature(title, cost, isBoost, isPriority)
                        },
                        onSendGift = { gift ->
                            val target = actionState.activeMatchSession?.partnerName
                                ?: actionState.activeRoom?.roomName
                                ?: "مضيف ونس"
                            viewModel.sendDigitalGift(gift, target)
                        },
                        onBuyCoinPackageViaRealPayment = { pkg ->
                            viewModel.createCoinPackagePurchaseOrder(
                                pkg = pkg,
                                paymentMethodAr = "فودافون كاش / إنستا باي",
                                referenceNumber = "VOD-${System.currentTimeMillis().toString().takeLast(6)}"
                            )
                        },
                        onOpenVipPaymentScreen = {
                            showCoinsAndGiftsView = false
                            notificationsPaymentInitialTab = 1
                            showNotificationsPaymentView = true
                        },
                        onPurchaseOrEquipSpecialCosmetic = { item ->
                            viewModel.purchaseOrEquipSpecialCosmetic(item)
                        },
                        onJoinWanasClan = { clan ->
                            viewModel.joinWanasFamilyClan(clan)
                        },
                        onCreateWanasClan = { clanName, motto ->
                            viewModel.createNewWanasFamilyClan(clanName, motto)
                        },
                        onBackToHome = { showCoinsAndGiftsView = false }
                    )
                }
            } else if (showSafetyAndAdminView) {
                val availableRooms = (roomsState as? UiState.Success<List<ChatRoomMetadata>>)?.data.orEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    WanasSafetyAndAdminDashboardScreen(
                        actionState = actionState,
                        totalRoomsCount = availableRooms.size,
                        availableRooms = availableRooms,
                        isDarkMode = isDarkMode,
                        onSubmitReport = { targetName, reason ->
                            viewModel.submitSafetyReport(
                                targetUserId = "reported_${System.currentTimeMillis()}",
                                targetUserName = targetName,
                                reasonAr = reason,
                                sourceContext = "مركز الأمان"
                            )
                        },
                        onResolveReportByAdmin = { reportId, action ->
                            viewModel.resolveSafetyReportByAdmin(reportId, action)
                        },
                        onUpdateFadfadaPin = { newPin ->
                            viewModel.updateFadfadaPinCodeByAdmin(newPin)
                        },
                        onBroadcastGlobalAnnouncement = { text ->
                            viewModel.broadcastGlobalAdminAnnouncement(text)
                        },
                        onGrantBonusCoinsByAdmin = { target, coins, grantVip ->
                            viewModel.grantBonusCoinsByAdmin(target, coins, grantVip)
                        },
                        onToggleGlobalAppControl = { toggleRooms, toggleProfanity, toggleDoubleCoins ->
                            viewModel.updateGlobalAppControlToggleByAdmin(
                                toggleRoomCreation = toggleRooms,
                                toggleProfanityFilter = toggleProfanity,
                                toggleDoubleCoins = toggleDoubleCoins
                            )
                        },
                        onAdminPinOrLockRoom = { roomId, togglePin, toggleLock ->
                            viewModel.togglePinOrLockRoomByAdmin(roomId, togglePin, toggleLock)
                        },
                        onAdminUpdateRoomPassword = { roomId, newPass ->
                            viewModel.updateRoomPasswordByAdmin(roomId, newPass)
                        },
                        onAdminDeleteRoom = { roomId ->
                            viewModel.deleteChatRoom(roomId)
                        },
                        onAdminEnterRoomDirect = { rm ->
                            showSafetyAndAdminView = false
                            currentMainSectionId = WanasAppSection.VOICE_ROOMS.id
                            viewModel.enterChatRoom(rm)
                        },
                        onPerformOwnerMemberAction = { memberQuery, newStatus, reasonAr ->
                            viewModel.performOwnerMemberAction(memberQuery, newStatus, reasonAr)
                        },
                        onExecuteServerLedgerOperation = { targetMember, deltaCoins, opTypeAr, auditReason ->
                            viewModel.executeServerLedgerCoinOperation(targetMember, deltaCoins, opTypeAr, auditReason)
                        },
                        onAssignAdminRole = { memberName, roleType, customPerms ->
                            viewModel.assignRoleToMemberByOwner(memberName, roleType, customPerms)
                        },
                        onTransferOrAssignRoomAdmin = { roomId, newOwnerName, newRoomAdminName ->
                            viewModel.transferOrAssignRoomAdminByOwner(roomId, newOwnerName, newRoomAdminName)
                        },
                        onResolveSuspiciousAlert = { alertId, blockSuspect ->
                            viewModel.resolveSuspiciousBehaviorAlert(alertId, blockSuspect)
                        },
                        onUpdateSupportTicketAndReply = { reportId, newStatusAr, replyMessage ->
                            viewModel.updateSupportTicketStatusAndReply(reportId, newStatusAr, replyMessage)
                        },
                        onApproveOrRejectPaymentOrder = { orderId, approve, adminNote ->
                            viewModel.approveOrRejectPaymentOrderByOwner(orderId, approve, adminNote)
                        },
                        onOwnerQuickControlAllRooms = { actionKey ->
                            viewModel.ownerQuickControlAllRooms(actionKey)
                        },
                        onOwnerQuickControlAllMembers = { targetName, actionKey, bonusCoins ->
                            viewModel.ownerQuickControlAllMembers(targetName, actionKey, bonusCoins)
                        },
                        onUpdateOwnerSystemSettings = { appName, maxRooms, maxSeats, bannedWordsCsv, toggleMaint, toggleGifts, toggleReg, triggerBackup ->
                            viewModel.updateOwnerSystemSettings(
                                appName = appName,
                                maxRooms = maxRooms,
                                maxSeats = maxSeats,
                                bannedWordsCsv = bannedWordsCsv,
                                toggleMaintenance = toggleMaint,
                                toggleGiftsGlobally = toggleGifts,
                                toggleRegistration = toggleReg,
                                triggerCloudBackup = triggerBackup
                            )
                        },
                        onToggleAdminOwnerMode = { enabled ->
                            viewModel.setAdminOwnerViewMode(enabled)
                        },
                        onToggleGlobalTheme = { dark ->
                            onSetDarkMode(dark)
                        },
                        onBackToHome = { showSafetyAndAdminView = false }
                    )
                }
            } else if (showNotificationsPaymentView) {
                val availableRooms = (roomsState as? UiState.Success<List<ChatRoomMetadata>>)?.data.orEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    PushNotificationsAndPaymentScreen(
                        actionState = actionState,
                        availableRooms = availableRooms,
                        isDarkMode = isDarkMode,
                        initialTabIndex = notificationsPaymentInitialTab,
                        onSendFriendInvitePush = { friend, room, msg ->
                            viewModel.inviteFriendToChatRoom(friend, room, msg)
                        },
                        onSendRoomMessagePush = { msg ->
                            viewModel.sendChatMessageInActiveRoom(msg)
                        },
                        onTriggerFavoriteRoomLivePush = { room, customTopic ->
                            viewModel.notifyFavoriteRoomLiveBroadcastStarted(room, customTopic)
                        },
                        onTriggerFriendStartedRoomPush = { friendName, roomName, roomCategory ->
                            viewModel.notifyFriendStartedNewRoom(
                                customFriendName = friendName,
                                customRoomName = roomName,
                                roomCategory = roomCategory
                            )
                        },
                        onSendPrivateInvitePush = { recipient, room, msg ->
                            viewModel.sendPrivateInvitationNotification(recipient, room, msg)
                        },
                        onToggleFavoriteRoom = { room ->
                            viewModel.toggleFavoriteRoom(room)
                        },
                        onUpdateNotificationPreferences = { favLive, privInv, friendNewRoom ->
                            viewModel.updateInAppNotificationPreferences(
                                notifyFavoriteLive = favLive,
                                notifyPrivateInvites = privInv,
                                notifyFriendStartsRoom = friendNewRoom
                            )
                        },
                        onMarkNotificationsAsRead = { notifId ->
                            viewModel.markNotificationAsRead(notifId)
                        },
                        onJoinRoomFromNotification = { roomId ->
                            val targetRoom = availableRooms.find { it.roomId == roomId }
                            if (targetRoom != null) {
                                showNotificationsPaymentView = false
                                currentMainSectionId = WanasAppSection.VOICE_ROOMS.id
                                viewModel.enterChatRoom(targetRoom)
                            }
                        },
                        onProcessRealPayment = { plan, method, holder, card, expiry, cvv, phone, ref ->
                            viewModel.processRealPaymentCheckout(
                                plan = plan,
                                paymentMethod = method,
                                cardHolderName = holder,
                                cardNumber = card,
                                expiryMmYy = expiry,
                                cvv = cvv,
                                walletPhone = phone,
                                transferReferenceNumber = ref
                            )
                        },
                        onBackToRooms = { showNotificationsPaymentView = false }
                    )
                }
            } else if (actionState.activeRoom != null) {
                // 🎙️ Dedicated Standalone Room Page ("افتح غرفة تدخلني على صفحة الغرفة لوحدها")
                val currentActiveRoom = actionState.activeRoom!!
                val hasFullRoomAuthority = actionState.isAdminOwner || currentActiveRoom.creatorId == currentUserId
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                        .testTag("dedicated_room_page_root"),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dedicated Room Page Top Navigation Bar
                    item {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                                .testTag("dedicated_room_page_top_bar"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (isDarkMode) WanasCardBg else WanasDayDarkHeader
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_wanas_launcher_logo_1791392363941),
                                        contentDescription = "أيقونة تطبيق ونس",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                    )
                                    Column {
                                        Text(
                                            text = "🎙️ صفحة الغرفة المستقلة: ${currentActiveRoom.roomName}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = WanasAmberGold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "بث صوتي مباشر • متواجدون الآن (${actionState.activeRoomMembers.size})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WanasEmeraldOnline
                                        )
                                    }
                                }

                                FilledTonalButton(
                                    onClick = {
                                        viewModel.leaveCurrentRoom()
                                        currentMainSectionId = WanasAppSection.VOICE_ROOMS.id
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = WanasAmberGold,
                                        contentColor = Color(0xFF1A103C)
                                    ),
                                    modifier = Modifier.testTag("back_to_rooms_list_button")
                                ) {
                                    Text("🔙 قائمة الغرف", fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }

                    // Transient Auto-Hiding Join / Leave Banner ("دخل العضو فلان" / "خرج العضو فلان")
                    val presenceEvent = actionState.transientPresenceBanner
                    if (presenceEvent != null) {
                        item {
                            RoomPresenceTransientBanner(
                                event = presenceEvent,
                                onDismiss = { viewModel.dismissPresenceBanner() }
                            )
                        }
                    }

                    // Status / Error Feedback inside the dedicated room page
                    val roomFeedback = actionState.errorMessage ?: actionState.statusMessage
                    if (roomFeedback != null) {
                        item {
                            Surface(
                                color = if (actionState.errorMessage != null) {
                                    WanasCoralWarm.copy(alpha = 0.2f)
                                } else if (isDarkMode) {
                                    WanasEmeraldOnline.copy(alpha = 0.18f)
                                } else {
                                    WanasDayDarkEmerald
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                                    .testTag("status_feedback_banner")
                            ) {
                                Text(
                                    text = roomFeedback,
                                    modifier = Modifier.padding(12.dp),
                                    color = if (actionState.errorMessage != null) {
                                        WanasCoralWarm
                                    } else if (isDarkMode) {
                                        WanasEmeraldOnline
                                    } else {
                                        Color.White
                                    },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Full Interactive Room Stage & Present Members Panel
                    item {
                        ActiveRoomMembersPanel(
                            room = currentActiveRoom,
                            activeMembers = actionState.activeRoomMembers,
                            roomMessages = actionState.activeRoomMessages,
                            isDarkMode = isDarkMode,
                            currentUserId = currentUserId,
                            currentUserName = actionState.memberDisplayName,
                            hasRoomAdminAuthority = hasFullRoomAuthority,
                            isAppOwner = actionState.isAdminOwner,
                            coinsBalance = actionState.coinsBalance,
                            latestGiftBannerText = actionState.latestRoomGiftBannerText,
                            micRequestQueue = actionState.activeRoomMicRequests,
                            myMicRequestPending = actionState.myMicRequestStatus == MicRequestStatus.PENDING,
                            isMyMicApprovedByHost = actionState.isServerMicApproved,
                            roomAudioSeats = actionState.activeRoomSeats,
                            activeLuckyCoinBox = actionState.activeLuckyCoinBox,
                            roomTopSupporters = actionState.roomTopSupporters,
                            pkCountdownSeconds = actionState.pkCountdownSecondsRemaining,
                            isPkCountdownRunning = actionState.isPkCountdownRunning,
                            pkWinnerAnnouncementText = actionState.pkWinnerAnnouncementText,
                            pkComboStreakCount = actionState.pkComboStreakCount,
                            pkLastComboTeamLabel = actionState.pkLastComboTeamLabel,
                            pkPunishmentChallengeText = actionState.pkPunishmentChallengeText,
                            pkTargetTeamIsRed = actionState.pkTargetTeamIsRed,
                            roomGiftTransactions = actionState.activeRoomGiftTransactions,
                            selectedGiftRecipientName = actionState.selectedRoomGiftRecipientName,
                            selectedGiftComboMultiplier = actionState.selectedRoomGiftComboMultiplier,
                            isRecordingGiftToFirestore = actionState.isRecordingGiftToFirestore,
                            lastFirestoreGiftTxId = actionState.lastFirestoreGiftTxId,
                            onSelectGiftRecipient = { recipient ->
                                viewModel.selectRoomGiftRecipient(recipient)
                            },
                            onSelectGiftComboMultiplier = { mult ->
                                viewModel.selectRoomGiftComboMultiplier(mult)
                            },
                            onSelectPkTargetTeam = { isRed ->
                                viewModel.setPkGiftTargetTeam(isRed)
                            },
                            onTogglePkCountdown = {
                                viewModel.toggleOrStartPkBattleCountdown()
                            },
                            onDropLuckyCoinBox = { amount ->
                                viewModel.dropLuckyCoinBoxInRoom(amount)
                            },
                            onClaimLuckyCoinBox = {
                                viewModel.claimFromLuckyCoinBox()
                            },
                            onRequestMicAccess = { seatIdx ->
                                viewModel.requestMicInActiveRoom(seatIdx)
                            },
                            onCancelMyMicRequest = {
                                viewModel.cancelMyMicRequest()
                            },
                            onHostApproveMicRequest = { reqId ->
                                viewModel.approveMicRequestByHost(reqId)
                            },
                            onHostRejectMicRequest = { reqId ->
                                viewModel.rejectMicRequestByHost(reqId)
                            },
                            onHostRespondAllMicRequests = { approveAll ->
                                viewModel.approveOrRejectAllMicRequestsByHost(approveAll)
                            },
                            onMoveMemberBetweenSeats = { fromSeat, toSeat ->
                                viewModel.moveMemberBetweenVoiceSeats(fromSeat, toSeat)
                            },
                            onToggleSeatMuteState = { seatIdx ->
                                viewModel.sitOnOrSelectVoiceSeat(seatIdx)
                            },
                            onSendMessage = { msgText ->
                                viewModel.sendChatMessageInActiveRoom(msgText)
                            },
                            onSendRoomGift = { gift, recipientName, quantity ->
                                viewModel.sendGiftInActiveRoom(
                                    gift = gift,
                                    customRecipientName = recipientName,
                                    customQuantity = quantity
                                )
                            },
                            onTriggerStageSoundEffect = { effectId ->
                                viewModel.triggerRoomStageSoundEffect(effectId)
                            },
                            onSpinVoiceChallenge = {
                                viewModel.spinRoomVoiceChallenge()
                            },
                            onBoostPkTeam = { isRed ->
                                viewModel.boostRoomPkTeam(isRed)
                            },
                            onModerateRoomAction = { actionLabel, targetUserId ->
                                viewModel.moderateRoomActionByAdmin(actionLabel, targetUserId)
                            },
                            onUpdateRoomPassword = { newPass ->
                                viewModel.updateRoomPasswordByAdmin(currentActiveRoom.roomId, newPass)
                            },
                            onUpdateRoomSettingsByAdmin = { newAnn, newSeats ->
                                viewModel.updateRoomSettingsByAdmin(currentActiveRoom.roomId, newAnn, newSeats)
                            },
                            onResetPkOrClearChatByAdmin = { clearChat ->
                                viewModel.resetRoomPkOrClearChatByAdmin(currentActiveRoom.roomId, clearChat)
                            },
                            onTogglePinOrLockByAdmin = { togglePin, toggleLock ->
                                viewModel.togglePinOrLockRoomByAdmin(currentActiveRoom.roomId, togglePin, toggleLock)
                            },
                            onDeleteRoomByAdmin = {
                                viewModel.deleteChatRoom(currentActiveRoom.roomId)
                            },
                            onUpdateRoomAdminScopedSettings = { toggleChatLock, toggleSlow, toggleAdminOnly, toggleGifts, newRules, newWelcome, newName, newDesc, newCap ->
                                viewModel.updateRoomAdminScopedSettings(
                                    toggleChatLock = toggleChatLock,
                                    toggleSlowMode = toggleSlow,
                                    toggleAdminOnlyChat = toggleAdminOnly,
                                    toggleRoomGifts = toggleGifts,
                                    newRulesText = newRules,
                                    newWelcomeText = newWelcome,
                                    newRoomName = newName,
                                    newRoomDescription = newDesc,
                                    newMaxCapacity = newCap
                                )
                            },
                            onDeleteSingleMessageByAdmin = { msgId ->
                                viewModel.deleteSingleMessageInActiveRoomByAdmin(msgId)
                            },
                            onManageRoomMemberByRoomAdmin = { memberName, actionType ->
                                viewModel.manageRoomMemberByRoomAdmin(memberName, actionType)
                            },
                            onVacateOrSpeakerOnlySeats = { vacateSeatIdx, speakerOnly ->
                                viewModel.vacateOrSpeakerOnlySeatsByRoomAdmin(vacateSeatIdx, speakerOnly)
                            },
                            onLeaveRoom = { viewModel.leaveCurrentRoom() }
                        )
                    }
                }
            } else if (actionState.activeMatchSession != null) {
                // ⏱️ Dedicated Standalone 10-Minute Voice Call Session Page
                val currentMatchSession = actionState.activeMatchSession!!
                BackHandler {
                    if (currentMatchSession.isEnded) {
                        viewModel.closeEndedMatchSessionSummary()
                    } else {
                        viewModel.endActiveMatchSession()
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                        .testTag("dedicated_match_call_page_root"),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ActiveTenMinuteMatchCallCard(
                            session = currentMatchSession,
                            coinsBalance = actionState.coinsBalance,
                            showInterstitialAd = actionState.showInterstitialAdBetweenSessions,
                            onExtendTimeWithCoins = {
                                viewModel.extendActiveSessionWithCoins(extraMinutes = 10, costCoins = 25)
                            },
                            onToggleMutePartner = {
                                viewModel.toggleMutePartnerInSession()
                            },
                            onSendGift = { gift ->
                                viewModel.sendDigitalGift(gift, currentMatchSession.partnerName)
                            },
                            onStartMiniGame = { gameId ->
                                viewModel.startMiniGameInSession(gameId)
                            },
                            onAnswerGameQuestion = { optionIdx ->
                                viewModel.answerMiniGameQuestion(optionIdx)
                            },
                            onAddFriend = {
                                viewModel.addPartnerAsFriend(
                                    userId = currentMatchSession.partnerUserId,
                                    userName = currentMatchSession.partnerName,
                                    userEmoji = currentMatchSession.partnerEmoji,
                                    level = currentMatchSession.partnerLevel
                                )
                            },
                            onRateSession = { stars ->
                                viewModel.rateCompletedSession(stars)
                            },
                            onStartAnotherSession = {
                                viewModel.closeEndedMatchSessionSummary()
                                viewModel.startSmartMatchingSearch()
                            },
                            onWatchRewardedAd = {
                                viewModel.watchRewardedAdForFreeCoins()
                            },
                            onBlockPartner = {
                                viewModel.blockUser(
                                    targetUserId = currentMatchSession.partnerUserId,
                                    targetUserName = currentMatchSession.partnerName
                                )
                            },
                            onReportPartner = {
                                viewModel.submitSafetyReport(
                                    targetUserId = currentMatchSession.partnerUserId,
                                    targetUserName = currentMatchSession.partnerName,
                                    reasonAr = "بلاغ فوري أثناء المكالمة الصوتية",
                                    sourceContext = "جلسة صوتية 10 دقائق"
                                )
                            },
                            onEndOrCloseSession = {
                                if (currentMatchSession.isEnded) {
                                    viewModel.closeEndedMatchSessionSummary()
                                } else {
                                    viewModel.endActiveMatchSession()
                                }
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                        .testTag("chat_rooms_list"),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Dark Royal Header Card with Custom Wanas App Icon Emblem
                    item {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                                .testTag("header_card"),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (isDarkMode) WanasCardBg else WanasDayDarkHeader
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Top Title Bar + App Launcher Icon + Theme & Logout Icons
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
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.5.dp, WanasAmberGold),
                                            modifier = Modifier
                                                .size(44.dp)
                                                .testTag("app_header_launcher_icon")
                                        ) {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_wanas_launcher_logo_1791392363941),
                                                contentDescription = "أيقونة تطبيق ونس",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "وَنَس — الناس للناس (اتكلم • اسمع • اتعرف)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = WanasAmberGold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (isDarkMode) {
                                                    "🌙 الوضع الليلي الفاخر • ${actionState.userLevelBadge}"
                                                } else {
                                                    "☀️ الوضع النهاري المشرق • ${actionState.userLevelBadge}"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.85f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            signOut(
                                                credentialManager = credentialManager,
                                                supabaseService = supabaseService,
                                                onSignOutComplete = onSignedOut,
                                                scope = scope
                                            )
                                        },
                                        modifier = Modifier.testTag("sign_out_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = "تسجيل الخروج",
                                            tint = WanasCoralWarm
                                        )
                                    }
                                }

                                // Organized Navigation Action Row (Each button opens its own dedicated page)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            if (actionState.isFadfadaPinUnlocked || actionState.isAdminOwner) {
                                                showFadfadaView = true
                                            } else {
                                                showFadfadaPinDialog = true
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = WanasVioletAccent,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("open_fadfada_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "قسم فضفضة بكود",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "فضفضة 🔐",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = { showProfileView = true },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = WanasTealCalm,
                                            contentColor = Color(0xFF00201D)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("open_profile_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "الملف الشخصي والإحصائيات",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ملفي",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = onToggleTheme,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = WanasAmberGold,
                                            contentColor = Color(0xFF1A103C)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("theme_toggle_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = "تغيير ألوان التطبيق",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isDarkMode) "نهاري" else "ليلي",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Supabase Member Account Link Bar
                                Surface(
                                    color = Color.White.copy(alpha = 0.09f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showProfileView = true }
                                        .testTag("supabase_account_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
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
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(WanasAmberGold.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (actionState.memberAvatarUri.isNotBlank()) {
                                                        AsyncImage(
                                                            model = actionState.memberAvatarUri,
                                                            contentDescription = "صورة العضو",
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(CircleShape),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Text(
                                                            text = actionState.memberAvatarEmoji,
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                    }
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "العضو: ${actionState.memberDisplayName}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = actionState.supabaseStatusText,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = WanasEmeraldOnline,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = { viewModel.syncMemberAccountWithSupabaseAndCloud() },
                                                modifier = Modifier.testTag("sync_supabase_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Sync,
                                                    contentDescription = "مزامنة حساب العضو مع Supabase",
                                                    tint = WanasTealCalm
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = memberNameInput,
                                                onValueChange = { memberNameInput = it },
                                                label = { Text("الاسم أو اللقب في الغرف") },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = WanasAmberGold,
                                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                                    focusedLabelColor = WanasAmberGold,
                                                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                                    cursorColor = WanasAmberGold
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("member_name_input")
                                            )
                                            Button(
                                                onClick = { viewModel.updateMemberProfileName(memberNameInput) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = WanasTealCalm,
                                                    contentColor = Color(0xFF00201D)
                                                ),
                                                modifier = Modifier
                                                    .height(50.dp)
                                                    .testTag("save_member_name_button")
                                            ) {
                                                Text("تحديث", fontWeight = FontWeight.ExtraBold)
                                            }
                                        }

                                        // Quick Actions: Push Notifications / Friend Invite & Real VIP Payment Checkout
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            FilledTonalButton(
                                                onClick = {
                                                    notificationsPaymentInitialTab = 0
                                                    showNotificationsPaymentView = true
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("open_push_notifications_button"),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = WanasAmberGold,
                                                    contentColor = Color(0xFF1A103C)
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.NotificationsActive,
                                                    contentDescription = "الإشعارات الفورية ودعوة صديق",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (actionState.unreadNotificationsCount > 0) {
                                                        "الإشعارات والدعوات (${actionState.unreadNotificationsCount})"
                                                    } else {
                                                        "الإشعارات ودعوة صديق"
                                                    },
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            FilledTonalButton(
                                                onClick = {
                                                    notificationsPaymentInitialTab = 1
                                                    showNotificationsPaymentView = true
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("open_real_payment_button"),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = WanasEmeraldOnline,
                                                    contentColor = Color.White
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CreditCard,
                                                    contentDescription = "الدفع الحقيقي VIP",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (actionState.isPaidVip) "✅ باقة VIP مفعلة" else "الدفع الحقيقي VIP",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.ExtraBold,
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

                    // 1.15 Floating In-App Notification Banner for Favorite Room Live Broadcasts & Private Invitations
                    val latestPush = actionState.latestPushBanner
                    if (latestPush != null) {
                        item {
                            val availableRooms = (roomsState as? UiState.Success<List<ChatRoomMetadata>>)?.data.orEmpty()
                            WanasInAppNotificationToastBanner(
                                event = latestPush,
                                onJoinOrAccept = {
                                    viewModel.markNotificationAsRead(latestPush.notificationId)
                                    val targetRoom = availableRooms.find { it.roomId == latestPush.roomId }
                                    if (targetRoom != null) {
                                        currentMainSectionId = WanasAppSection.VOICE_ROOMS.id
                                        viewModel.enterChatRoom(targetRoom)
                                    } else {
                                        notificationsPaymentInitialTab = 0
                                        showNotificationsPaymentView = true
                                    }
                                },
                                onOpenNotificationCenter = {
                                    notificationsPaymentInitialTab = 0
                                    showNotificationsPaymentView = true
                                },
                                onDismiss = { viewModel.dismissPushBanner() }
                            )
                        }
                    }

                    // 1.2 Transient Auto-Hiding Join / Leave Banner ("دخل العضو فلان" / "خرج العضو فلان")
                    val transientEvent = actionState.transientPresenceBanner
                    if (transientEvent != null) {
                        item {
                            RoomPresenceTransientBanner(
                                event = transientEvent,
                                onDismiss = { viewModel.dismissPresenceBanner() }
                            )
                        }
                    }

                    // 1.3 Global Admin Announcement Banner + Organized Sections Directory Card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (actionState.adminDashboardStats.globalAnnouncementBanner.isNotBlank()) {
                                Surface(
                                    color = if (isDarkMode) Color(0xFF28134A) else WanasDayDarkEmerald,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, WanasAmberGold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("global_admin_announcement_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("📢", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = actionState.adminDashboardStats.globalAnnouncementBanner,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = WanasAmberGold,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            WanasModernSectionDirectoryCard(
                                selectedSection = activeMainSection,
                                isDarkMode = isDarkMode,
                                isAdminOwner = actionState.isAdminOwner,
                                onSelectSection = { sec -> navigateToAppSection(sec) },
                                onOpenAdminDashboard = { showSafetyAndAdminView = true }
                            )
                        }
                    }

                    // 1.4 Status / Error Feedback Banner
                    val feedback = actionState.errorMessage ?: actionState.statusMessage
                    if (feedback != null) {
                        item {
                            Surface(
                                color = if (actionState.errorMessage != null) {
                                    WanasCoralWarm.copy(alpha = 0.2f)
                                } else if (isDarkMode) {
                                    WanasEmeraldOnline.copy(alpha = 0.18f)
                                } else {
                                    WanasDayDarkEmerald
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                                    .testTag("status_feedback_banner")
                            ) {
                                Text(
                                    text = feedback,
                                    modifier = Modifier.padding(12.dp),
                                    color = if (actionState.errorMessage != null) {
                                        WanasCoralWarm
                                    } else if (isDarkMode) {
                                        WanasEmeraldOnline
                                    } else {
                                        Color.White
                                    },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (activeMainSection == WanasAppSection.MATCHING_HUB) {
                        // 🏠 PAGE 1: ORGANIZED HOME & SMART MATCHING PAGE ("ترتيب الصفحة الرئيسية")
                        item {
                            WanasFourHeroChoicesSection(
                                actionState = actionState,
                                isDarkMode = isDarkMode,
                                onSelectSpeakNow = {
                                    viewModel.startSmartMatchingSearch(
                                        role = MatchRole.NEED_TO_SPEAK,
                                        topic = actionState.selectedMatchTopic
                                    )
                                },
                                onSelectListenNow = {
                                    viewModel.selectMatchRoleAndTopic(
                                        role = MatchRole.WANT_TO_LISTEN,
                                        topic = actionState.selectedMatchTopic
                                    )
                                },
                                onSelectVoiceRooms = {
                                    currentMainSectionId = WanasAppSection.VOICE_ROOMS.id
                                },
                                onOpenFriendsHub = { showFriendsView = true },
                                onOpenCoinsAndGiftsHub = { showCoinsAndGiftsView = true },
                                onOpenSafetyCenter = { showSafetyAndAdminView = true },
                                onOpenAdminDashboard = { showSafetyAndAdminView = true },
                                onSelectTopicAndStartMatch = { role, topic ->
                                    viewModel.startSmartMatchingSearch(role = role, topic = topic)
                                },
                                onAcceptWaitingRequest = { candidate ->
                                    viewModel.startVoiceCallWithMatchedCandidate(candidate)
                                }
                            )
                        }

                        // Quick CTA Card to jump to the Dedicated Voice Rooms Directory Page + Quick Category Filters
                        item {
                            val allRoomsList = (roomsState as? UiState.Success<List<ChatRoomMetadata>>)?.data.orEmpty()
                            val totalRoomsCount = allRoomsList.size
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                                    .clickable { currentMainSectionId = WanasAppSection.VOICE_ROOMS.id }
                                    .testTag("home_open_voice_rooms_page_card"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkMode) Color(0xFF211342) else WanasDayDarkHeader
                                ),
                                border = BorderStroke(1.5.dp, WanasAmberGold)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "🎙️ تصفح صفحة الغرف الصوتية المباشرة ($totalRoomsCount غرف)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = WanasAmberGold
                                            )
                                            Text(
                                                text = "اختر تصنيف المحادثة (General • Technology • Music • Education) أو افتح كل الغرف",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.88f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { currentMainSectionId = WanasAppSection.VOICE_ROOMS.id },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = WanasAmberGold,
                                                contentColor = Color(0xFF1A103C)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("home_go_to_rooms_page_button")
                                        ) {
                                            Text("فتح صفحة الغرف", fontWeight = FontWeight.ExtraBold)
                                        }
                                    }

                                    // Quick Category Filter Shortcuts on Home Card
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("home_quick_room_categories_row")
                                    ) {
                                        val highlightCategories = listOf(
                                            WanasRoomCategoryFilter.GENERAL,
                                            WanasRoomCategoryFilter.TECHNOLOGY,
                                            WanasRoomCategoryFilter.MUSIC,
                                            WanasRoomCategoryFilter.EDUCATION
                                        )
                                        items(highlightCategories, key = { it.id }) { cat ->
                                            Surface(
                                                color = Color.White.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.65f)),
                                                modifier = Modifier
                                                    .clickable {
                                                        selectedRoomFilterId = cat.id
                                                        currentMainSectionId = WanasAppSection.VOICE_ROOMS.id
                                                    }
                                                    .testTag("home_quick_category_${cat.id}")
                                            ) {
                                                Text(
                                                    text = "${cat.emoji} ${cat.labelAr}",
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // 🎙️ PAGE 2: DEDICATED VOICE ROOMS DIRECTORY PAGE ("ترتيب صفحة الغرف + نظام فلترة التصنيفات")
                        item {
                            BackHandler {
                                currentMainSectionId = WanasAppSection.MATCHING_HUB.id
                            }
                            val allRoomsForCounts = (roomsState as? UiState.Success<List<ChatRoomMetadata>>)?.data.orEmpty()
                            fun countRoomsForFilter(filter: WanasRoomCategoryFilter): Int {
                                return when (filter) {
                                    WanasRoomCategoryFilter.ALL -> allRoomsForCounts.size
                                    WanasRoomCategoryFilter.TRENDING_NOW -> allRoomsForCounts.size
                                    WanasRoomCategoryFilter.FAVORITES -> allRoomsForCounts.count {
                                        actionState.favoriteRoomIds.contains(it.roomId)
                                    }
                                    WanasRoomCategoryFilter.VIP_LOCKED -> allRoomsForCounts.count { it.hasPassword }
                                    WanasRoomCategoryFilter.GENERAL -> allRoomsForCounts.count {
                                        it.roomCategory.equals("GENERAL", ignoreCase = true) ||
                                            it.roomCategory.equals("PUBLIC_PARTY", ignoreCase = true)
                                    }
                                    WanasRoomCategoryFilter.EDUCATION -> allRoomsForCounts.count {
                                        it.roomCategory.equals("EDUCATION", ignoreCase = true) ||
                                            it.roomCategory.equals("STUDY_CALM", ignoreCase = true)
                                    }
                                    else -> allRoomsForCounts.count {
                                        it.roomCategory.equals(filter.id, ignoreCase = true)
                                    }
                                }
                            }

                            Surface(
                                color = if (isDarkMode) Color(0xFF1B1238) else WanasDayLightSurface,
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, WanasVioletAccent.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                                    .testTag("voice_rooms_category_filter_bar")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🎙️ فلترة الغرف الصوتية حسب التصنيف:",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                                            )
                                            Text(
                                                text = "General • Technology • Music • Education • والمزيد",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isDarkMode) WanasTealCalm else WanasTealDeep
                                            )
                                        }
                                        FilledTonalButton(
                                            onClick = { currentMainSectionId = WanasAppSection.MATCHING_HUB.id },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = WanasAmberGold,
                                                contentColor = Color(0xFF1A103C)
                                            ),
                                            modifier = Modifier.testTag("rooms_page_back_to_home_button")
                                        ) {
                                            Text("🏠 الرئيسية", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }

                                    // Featured Primary 4 Categories Row (General, Technology, Music, Education) + All
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("voice_rooms_category_chips_lazy_row")
                                    ) {
                                        items(WanasRoomCategoryFilter.entries, key = { it.id }) { filter ->
                                            val selected = selectedRoomFilterId == filter.id
                                            val count = countRoomsForFilter(filter)
                                            Surface(
                                                color = if (selected) {
                                                    WanasAmberGold
                                                } else if (isDarkMode) {
                                                    Color.White.copy(alpha = 0.08f)
                                                } else {
                                                    WanasDaySurfaceVariant
                                                },
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(
                                                    width = if (selected) 1.5.dp else 1.dp,
                                                    color = if (selected) WanasAmberGold else WanasTealCalm.copy(alpha = 0.45f)
                                                ),
                                                modifier = Modifier
                                                    .clickable { selectedRoomFilterId = filter.id }
                                                    .testTag("room_category_filter_${filter.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "${filter.emoji} ${filter.labelAr}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (selected) Color(0xFF1A103C) else if (isDarkMode) Color.White else WanasDayTextPrimary
                                                    )
                                                    Surface(
                                                        color = if (selected) {
                                                            Color(0xFF1A103C).copy(alpha = 0.16f)
                                                        } else {
                                                            WanasTealCalm.copy(alpha = 0.22f)
                                                        },
                                                        shape = RoundedCornerShape(50)
                                                    ) {
                                                        Text(
                                                            text = "$count",
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = if (selected) Color(0xFF1A103C) else if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Active Filter Indicator & Reset Action
                                    val activeFilterEnum = WanasRoomCategoryFilter.entries.find { it.id == selectedRoomFilterId }
                                        ?: WanasRoomCategoryFilter.ALL
                                    if (activeFilterEnum != WanasRoomCategoryFilter.ALL) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("active_room_filter_summary_row"),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🔍 التصنيف النشط: ${activeFilterEnum.emoji} ${activeFilterEnum.labelAr}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isDarkMode) WanasTealCalm else WanasTealDeep,
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedButton(
                                                onClick = { selectedRoomFilterId = WanasRoomCategoryFilter.ALL.id },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.6f)),
                                                modifier = Modifier.testTag("reset_room_category_filter_button")
                                            ) {
                                                Text(
                                                    text = "إظهار الكل 🌟",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Quick Notification Alert Bar for Friend New Audio Rooms & Followed Active Rooms
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                                    .testTag("voice_rooms_notification_alerts_bar"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkMode) WanasCardBg else WanasDayLightSurface
                                ),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isDarkMode) WanasEmeraldOnline.copy(alpha = 0.55f) else WanasDayDarkHeader.copy(alpha = 0.25f)
                                )
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🔔 نظام تنبيهات الغرف الصوتية وغرف الأصدقاء",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                                            )
                                            Text(
                                                text = "احصل على إشعار فوري عندما يبدأ صديقك غرفة جديدة أو عندما تصبح غرفة تتابعها (${actionState.favoriteRoomIds.size}) نشطة",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isDarkMode) Color.White.copy(alpha = 0.78f) else WanasDayTextSecondary
                                            )
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                viewModel.notifyFriendStartedNewRoom(
                                                    roomCategory = if (selectedRoomFilterId in listOf("GENERAL", "TECHNOLOGY", "MUSIC", "EDUCATION")) {
                                                        selectedRoomFilterId
                                                    } else {
                                                        WanasRoomCategoryFilter.GENERAL.id
                                                    }
                                                )
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = WanasEmeraldOnline,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("voice_rooms_quick_friend_room_alert_button")
                                        ) {
                                            Text(
                                                text = "🎙️ إشعار غرفة صديق",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = if (actionState.notifyOnFriendStartsRoom) WanasEmeraldOnline.copy(alpha = 0.16f) else Color.Gray.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (actionState.notifyOnFriendStartsRoom) WanasEmeraldOnline else Color.Gray.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    viewModel.updateInAppNotificationPreferences(
                                                        notifyFavoriteLive = actionState.notifyOnFavoriteRoomLive,
                                                        notifyPrivateInvites = actionState.notifyOnPrivateInvite,
                                                        notifyFriendStartsRoom = !actionState.notifyOnFriendStartsRoom
                                                    )
                                                }
                                                .testTag("voice_rooms_friend_new_room_alert_toggle")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(if (actionState.notifyOnFriendStartsRoom) "🎙️" else "🔕")
                                                Text(
                                                    text = if (actionState.notifyOnFriendStartsRoom) "غرف الأصدقاء: مفعّل ✅" else "غرف الأصدقاء: متوقف",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isDarkMode) Color.White else WanasDayTextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Surface(
                                            color = if (actionState.notifyOnFavoriteRoomLive) WanasAmberGold.copy(alpha = 0.18f) else Color.Gray.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (actionState.notifyOnFavoriteRoomLive) WanasAmberGold else Color.Gray.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    viewModel.updateInAppNotificationPreferences(
                                                        notifyFavoriteLive = !actionState.notifyOnFavoriteRoomLive,
                                                        notifyPrivateInvites = actionState.notifyOnPrivateInvite,
                                                        notifyFriendStartsRoom = actionState.notifyOnFriendStartsRoom
                                                    )
                                                }
                                                .testTag("voice_rooms_followed_room_alert_toggle")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(if (actionState.notifyOnFavoriteRoomLive) "🔴" else "🔕")
                                                Text(
                                                    text = if (actionState.notifyOnFavoriteRoomLive) "الغرف المتابَعة: مفعّل ✅" else "الغرف المتابَعة: متوقف",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isDarkMode) Color.White else WanasDayTextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Create New Room Card (Supports Category Selection + Optional Password + Live Multi-Seat Stage)
                        item {
                            var roomPasswordInput by rememberSaveable { mutableStateOf("") }
                            var newRoomCategoryId by rememberSaveable {
                                mutableStateOf(WanasRoomCategoryFilter.GENERAL.id)
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                                    .testTag("create_room_card"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkMode) WanasCardBg else WanasDayLightSurface
                                ),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (isDarkMode) WanasTealCalm.copy(alpha = 0.5f) else WanasDayDarkHeader.copy(alpha = 0.25f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "➕ إنشاء غرفة بث صوتية جديدة (اختر التصنيف والباسورد)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDarkMode) WanasTealCalm else WanasDayDarkHeader
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = roomNameInput,
                                                onValueChange = { roomNameInput = it },
                                                label = { Text("اسم الغرفة الجديدة", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                                placeholder = { Text("مثال: 💻 نقاش تقني وذكاء اصطناعي") },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .weight(1.3f)
                                                    .testTag("room_name_input")
                                            )

                                            OutlinedTextField(
                                                value = roomPasswordInput,
                                                onValueChange = { roomPasswordInput = it },
                                                label = { Text("باسورد (اختياري)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                                placeholder = { Text("بدون = عامة") },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .weight(0.9f)
                                                    .testTag("room_password_input")
                                            )
                                        }

                                        Text(
                                            text = "🏷️ تصنيف الغرفة (General • Technology • Music • Education):",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White.copy(alpha = 0.85f) else WanasDayTextSecondary
                                        )

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("create_room_category_selector_row")
                                        ) {
                                            items(WanasRoomCategoryFilter.creatableCategories, key = { it.id }) { catOption ->
                                                val isChosen = newRoomCategoryId == catOption.id
                                                Surface(
                                                    color = if (isChosen) {
                                                        WanasTealCalm
                                                    } else if (isDarkMode) {
                                                        Color.White.copy(alpha = 0.08f)
                                                    } else {
                                                        WanasDaySurfaceVariant
                                                    },
                                                    shape = RoundedCornerShape(50),
                                                    border = BorderStroke(
                                                        width = 1.dp,
                                                        color = if (isChosen) WanasTealCalm else WanasVioletAccent.copy(alpha = 0.4f)
                                                    ),
                                                    modifier = Modifier
                                                        .clickable { newRoomCategoryId = catOption.id }
                                                        .testTag("create_room_category_chip_${catOption.id}")
                                                ) {
                                                    Text(
                                                        text = "${catOption.emoji} ${catOption.labelAr}",
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isChosen) Color(0xFF00201D) else if (isDarkMode) Color.White else WanasDayTextPrimary
                                                    )
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.createChatRoom(
                                                    roomName = roomNameInput,
                                                    roomPassword = roomPasswordInput,
                                                    roomCategory = newRoomCategoryId
                                                )
                                                roomNameInput = ""
                                                roomPasswordInput = ""
                                            },
                                            enabled = !actionState.isSubmitting,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("create_room_button"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isDarkMode) WanasEmeraldOnline else WanasDayDarkHeader,
                                                contentColor = if (isDarkMode) Color.White else WanasAmberGold
                                            )
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("حفظ الغرفة وبدء البث الصوتى", fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }
                        }

                        // Real-time List of Chat Rooms (Clicking Enter opens the Dedicated Room Page!)
                        when (val state = roomsState) {
                            is UiState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                                        )
                                    }
                                }
                            }
                            is UiState.Error -> {
                                item {
                                    Surface(
                                        color = WanasCoralWarm.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .widthIn(max = 600.dp)
                                            .testTag("rooms_error_banner")
                                    ) {
                                        Text(
                                            text = state.message,
                                            modifier = Modifier.padding(12.dp),
                                            color = WanasCoralWarm,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            is UiState.Success -> {
                                if (state.data.isEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .widthIn(max = 600.dp)
                                                .testTag("empty_rooms_card"),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isDarkMode) WanasCardBg else WanasDayLightSurface
                                            ),
                                            border = BorderStroke(1.dp, WanasVioletAccent.copy(alpha = 0.3f))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MeetingRoom,
                                                    contentDescription = null,
                                                    tint = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Text(
                                                    text = "لا توجد غرف دردشة مضافة حالياً",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isDarkMode) Color.White else WanasDayDarkHeader
                                                )
                                                Text(
                                                    text = "اكتب اسم الغرفة بالأعلى واضغط «حفظ الغرفة» لبدء السهرة مع أصدقائك.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isDarkMode) Color.White.copy(alpha = 0.75f) else WanasDayTextSecondary
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    val baseFilteredRooms = when (selectedRoomFilterId) {
                                        WanasRoomCategoryFilter.ALL.id -> state.data
                                        WanasRoomCategoryFilter.TRENDING_NOW.id -> state.data.sortedByDescending { it.trendingRankScore }
                                        WanasRoomCategoryFilter.FAVORITES.id -> state.data.filter {
                                            actionState.favoriteRoomIds.contains(it.roomId)
                                        }
                                        WanasRoomCategoryFilter.VIP_LOCKED.id -> state.data.filter { it.hasPassword }
                                        WanasRoomCategoryFilter.GENERAL.id -> state.data.filter {
                                            it.roomCategory.equals("GENERAL", ignoreCase = true) ||
                                                it.roomCategory.equals("PUBLIC_PARTY", ignoreCase = true)
                                        }
                                        WanasRoomCategoryFilter.EDUCATION.id -> state.data.filter {
                                            it.roomCategory.equals("EDUCATION", ignoreCase = true) ||
                                                it.roomCategory.equals("STUDY_CALM", ignoreCase = true)
                                        }
                                        else -> state.data.filter {
                                            it.roomCategory.equals(selectedRoomFilterId, ignoreCase = true)
                                        }
                                    }
                                    // Smart Room Organization & Sorting: Pinned first -> Active room -> Followed -> Highest activity & PK score
                                    val filteredRooms = baseFilteredRooms.sortedWith(
                                        compareByDescending<ChatRoomMetadata> { it.isPinnedByAdmin }
                                            .thenByDescending { actionState.activeRoom?.roomId == it.roomId }
                                            .thenByDescending { actionState.favoriteRoomIds.contains(it.roomId) }
                                            .thenByDescending { it.trendingRankScore + it.activeMembersCount * 10 + it.pkTeamRedScore + it.pkTeamBlueScore }
                                    )

                                    if (filteredRooms.isEmpty()) {
                                        val selectedCatMeta = WanasRoomCategoryFilter.entries.find { it.id == selectedRoomFilterId }
                                            ?: WanasRoomCategoryFilter.GENERAL
                                        item {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .widthIn(max = 600.dp)
                                                    .testTag("empty_filtered_category_card"),
                                                shape = RoundedCornerShape(18.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isDarkMode) WanasCardBg else WanasDayLightSurface
                                                ),
                                                border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.45f))
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(20.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "${selectedCatMeta.emoji} لا توجد غرف حالياً في تصنيف «${selectedCatMeta.labelAr}»",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader
                                                    )
                                                    Text(
                                                        text = "يمكنك إنشاء أول غرفة في هذا التصنيف من الكارت بالأعلى أو عرض جميع الغرف المتاحة.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else WanasDayTextSecondary
                                                    )
                                                    Button(
                                                        onClick = { selectedRoomFilterId = WanasRoomCategoryFilter.ALL.id },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = WanasAmberGold,
                                                            contentColor = Color(0xFF1A103C)
                                                        ),
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.testTag("empty_category_show_all_button")
                                                    ) {
                                                        Text("🌟 عرض جميع الغرف (${state.data.size})", fontWeight = FontWeight.ExtraBold)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        items(filteredRooms, key = { it.roomId }) { room ->
                                            val isInsideThisRoom = actionState.activeRoom?.roomId == room.roomId
                                            val isFavoriteRoom = actionState.favoriteRoomIds.contains(room.roomId)
                                            val hasFullAuthorityForRoom = actionState.isAdminOwner || room.creatorId == currentUserId
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .widthIn(max = 600.dp)
                                            ) {
                                                ChatRoomMetadataItemCard(
                                                    room = room,
                                                    isDarkMode = isDarkMode,
                                                    isInsideThisRoom = isInsideThisRoom,
                                                    isFavoriteRoom = isFavoriteRoom,
                                                    canDelete = hasFullAuthorityForRoom,
                                                    hasRoomAdminAuthority = hasFullAuthorityForRoom,
                                                    isAppOwner = actionState.isAdminOwner,
                                                    onEnterRoom = { viewModel.enterChatRoom(room) },
                                                    onLeaveRoom = { viewModel.leaveCurrentRoom() },
                                                    onSelectCategoryFilter = { categoryId ->
                                                        selectedRoomFilterId = categoryId
                                                    },
                                                    onToggleFavoriteRoom = { viewModel.toggleFavoriteRoom(room) },
                                                    onNotifyFavoriteLiveBroadcast = {
                                                        viewModel.notifyFavoriteRoomLiveBroadcastStarted(room)
                                                    },
                                                    onSendPrivateInviteToRoom = {
                                                        viewModel.sendPrivateInvitationNotification(
                                                            recipientNameOrEmail = "أصدقائي في ونس",
                                                            room = room
                                                        )
                                                    },
                                                    onUpdateRoomPassword = { newPass ->
                                                        viewModel.updateRoomPasswordByAdmin(room.roomId, newPass)
                                                    },
                                                    onDelete = { viewModel.deleteChatRoom(room.roomId) }
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
        }
    }
}

/**
 * In-App Notification Banner ("نظام إشعارات داخل التطبيق ينبه المستخدمين عند بدء بث مباشر في غرفهم المفضلة أو عند تلقي دعوة خاصة").
 */
@Composable
private fun WanasInAppNotificationToastBanner(
    event: PushNotificationEvent,
    onJoinOrAccept: () -> Unit,
    onOpenNotificationCenter: () -> Unit,
    onDismiss: () -> Unit
) {
    val isFriendNewRoom = event.isFriendStartedRoom
    val isFavLive = event.isFollowedRoomActive
    val isPrivateInv = event.isPrivateInvite
    val isInvite = event.isRoomInvite

    val bannerBg = when {
        isFriendNewRoom -> Color(0xFF064E3B)
        isFavLive -> Color(0xFF4C0519)
        isPrivateInv -> Color(0xFF28134A)
        isInvite -> WanasDayDarkEmerald
        else -> Color(0xFF1A103C)
    }
    val accentColor = when {
        isFriendNewRoom -> WanasEmeraldOnline
        isFavLive -> WanasCoralWarm
        isPrivateInv -> WanasAmberGold
        else -> WanasTealCalm
    }

    Surface(
        color = bannerBg,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.8.dp, accentColor),
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .clickable { onOpenNotificationCenter() }
            .testTag("in_app_notification_banner")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                isFriendNewRoom -> "🎙️"
                                isFavLive -> "🔴"
                                isPrivateInv -> "💌"
                                isInvite -> "📩"
                                else -> "🔔"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.badgeCategoryAr,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                        Text(
                            text = event.formattedTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("in_app_notification_banner_title")
                        )
                        Text(
                            text = event.messageBody,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onJoinOrAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (isPrivateInv) Color(0xFF1A103C) else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("in_app_notification_join_button")
                ) {
                    Text(
                        text = when {
                            isFriendNewRoom -> "🎙️ انضم لغرفة صديقك الآن"
                            isFavLive -> "🔴 انضم للغرفة النشطة الآن"
                            isInvite -> "✅ قبول الدعوة ودخول الغرفة"
                            else -> "فتح الغرفة الآن"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("in_app_notification_dismiss_button")
                ) {
                    Text(
                        text = "إخفاء",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Floating auto-dismissing notification banner when a member enters ("دخل العضو فلان")
 * or leaves ("خرج العضو فلان") a chat room.
 */
@Composable
private fun RoomPresenceTransientBanner(
    event: RoomPresenceBannerEvent,
    onDismiss: () -> Unit
) {
    val isJoin = event.isJoinEvent
    val bannerBg = if (isJoin) WanasDayDarkEmerald else Color(0xFF4C0519)
    val accentColor = if (isJoin) WanasAmberGold else WanasCoralWarm

    Surface(
        color = bannerBg,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.5.dp, accentColor),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .clickable { onDismiss() }
            .testTag("presence_event_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.avatarEmoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column {
                    Text(
                        text = event.bannerText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.testTag("presence_event_text")
                    )
                    Text(
                        text = if (isJoin) {
                            "✨ إشعار دخول للغرفة (يختفي تلقائياً)"
                        } else {
                            "👋 إشعار مغادرة الغرفة (يختفي تلقائياً)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
            }

            Icon(
                imageVector = if (isJoin) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = accentColor
            )
        }
    }
}

/**
 * Displays the active room header, live multi-seat interactive voice stage (8 mic seats),
 * live voice stage controls (Mic, Speakerphone, Monitor, RMS Voice Meter),
 * interactive digital gift showcase & animated gift banner, full Room Admin & App Owner controls
 * (even if the room is password-protected), present members, and the interactive room chat box.
 */
@Composable
private fun ActiveRoomMembersPanel(
    room: ChatRoomMetadata,
    activeMembers: List<RoomActiveMember>,
    roomMessages: List<RoomChatMessage>,
    isDarkMode: Boolean,
    currentUserId: String = "",
    currentUserName: String = "عضو وَنَس",
    hasRoomAdminAuthority: Boolean = false,
    isAppOwner: Boolean = false,
    coinsBalance: Int = 120,
    latestGiftBannerText: String? = null,
    micRequestQueue: List<RoomMicRequestItem> = emptyList(),
    myMicRequestPending: Boolean = false,
    isMyMicApprovedByHost: Boolean = false,
    roomAudioSeats: List<VoiceRoomSeatSlot> = (0 until 8).map { VoiceRoomSeatSlot(seatIndex = it) },
    activeLuckyCoinBox: LuckyCoinBoxState? = null,
    roomTopSupporters: List<RoomTopSupporterItem> = emptyList(),
    pkCountdownSeconds: Int = 180,
    isPkCountdownRunning: Boolean = false,
    pkWinnerAnnouncementText: String? = null,
    pkComboStreakCount: Int = 1,
    pkLastComboTeamLabel: String = "",
    pkPunishmentChallengeText: String = "",
    pkTargetTeamIsRed: Boolean = true,
    roomGiftTransactions: List<RoomGiftTransactionDocument> = emptyList(),
    selectedGiftRecipientName: String = "مضيف السهرة 👑",
    selectedGiftComboMultiplier: Int = 1,
    isRecordingGiftToFirestore: Boolean = false,
    lastFirestoreGiftTxId: String? = null,
    onSelectGiftRecipient: (String) -> Unit = {},
    onSelectGiftComboMultiplier: (Int) -> Unit = {},
    onSelectPkTargetTeam: (Boolean) -> Unit = {},
    onTogglePkCountdown: () -> Unit = {},
    onDropLuckyCoinBox: (Int) -> Unit = {},
    onClaimLuckyCoinBox: () -> Unit = {},
    onRequestMicAccess: (Int) -> Unit = {},
    onCancelMyMicRequest: () -> Unit = {},
    onHostApproveMicRequest: (String) -> Unit = {},
    onHostRejectMicRequest: (String) -> Unit = {},
    onHostRespondAllMicRequests: (Boolean) -> Unit = {},
    onMoveMemberBetweenSeats: (Int, Int) -> Unit = { _, _ -> },
    onToggleSeatMuteState: (Int) -> Unit = {},
    onSendMessage: (String) -> Unit,
    onSendRoomGift: (WanasGiftItem, String, Int) -> Unit = { _, _, _ -> },
    onTriggerStageSoundEffect: (String) -> Unit = {},
    onSpinVoiceChallenge: () -> Unit = {},
    onBoostPkTeam: (Boolean) -> Unit = {},
    onModerateRoomAction: (String, String?) -> Unit = { _, _ -> },
    onUpdateRoomPassword: (String) -> Unit = {},
    onUpdateRoomSettingsByAdmin: (String, Int) -> Unit = { _, _ -> },
    onResetPkOrClearChatByAdmin: (Boolean) -> Unit = {},
    onTogglePinOrLockByAdmin: (Boolean, Boolean) -> Unit = { _, _ -> },
    onDeleteRoomByAdmin: () -> Unit = {},
    onUpdateRoomAdminScopedSettings: (
        toggleChatLock: Boolean,
        toggleSlowMode: Boolean,
        toggleAdminOnlyChat: Boolean,
        toggleRoomGifts: Boolean,
        newRulesText: String?,
        newWelcomeText: String?,
        newRoomName: String?,
        newRoomDescription: String?,
        newMaxCapacity: Int?
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onDeleteSingleMessageByAdmin: (String) -> Unit = {},
    onManageRoomMemberByRoomAdmin: (String, String) -> Unit = { _, _ -> },
    onVacateOrSpeakerOnlySeats: (Int?, Boolean) -> Unit = { _, _ -> },
    onLeaveRoom: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val voiceEngine = remember(room.roomId) { RealVoiceRoomEngine(context) }
    val voiceState by voiceEngine.state.collectAsStateWithLifecycle()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted && (isMyMicApprovedByHost || hasRoomAdminAuthority)) {
            voiceEngine.toggleMicrophone(scope, true)
        }
    }

    // Prevent auto-unmute on entry & immediately close mic if host revokes approval or mutes/bans
    LaunchedEffect(isMyMicApprovedByHost, hasRoomAdminAuthority) {
        if (!isMyMicApprovedByHost && !hasRoomAdminAuthority && !voiceState.isMicMuted) {
            voiceEngine.forceMuteMicrophone("🔇 تم إغلاق المايك فوراً من السيرفر/الإدارة")
        }
    }

    LaunchedEffect(room.roomId, currentUserId, currentUserName, activeMembers, roomAudioSeats) {
        if (room.roomId.isNotBlank()) {
            voiceEngine.bindRoomSignalingAndPeers(
                scope = scope,
                roomId = room.roomId,
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                roomMembers = activeMembers,
                roomSeats = roomAudioSeats
            )
        }
    }

    DisposableEffect(room.roomId, currentUserId) {
        voiceEngine.startRoomSession(
            scope = scope,
            hasMicPermission = hasMicPermission,
            startUnmuted = false,
            roomId = room.roomId,
            currentUserId = currentUserId,
            currentUserName = currentUserName,
            roomMembers = activeMembers,
            roomSeats = roomAudioSeats
        )
        onDispose {
            voiceEngine.stopRoomSession()
        }
    }

    var liveChatInput by rememberSaveable(room.roomId) { mutableStateOf("") }
    var selectedSeatIndex by rememberSaveable(room.roomId) { mutableStateOf(0) }
    var adminRoomPassEdit by rememberSaveable(room.roomId, room.roomPassword) { mutableStateOf(room.roomPassword) }
    var adminAnnouncementEdit by rememberSaveable(room.roomId, room.pinnedAnnouncement) { mutableStateOf(room.pinnedAnnouncement) }
    var selectedRoomAdminTabId by rememberSaveable(room.roomId) { mutableStateOf(RoomAdminModuleTab.ROOM_OVERVIEW.id) }
    var roomRulesEdit by rememberSaveable(room.roomId, room.roomRulesText) { mutableStateOf(room.roomRulesText) }
    var roomWelcomeEdit by rememberSaveable(room.roomId, room.roomWelcomeMessage) { mutableStateOf(room.roomWelcomeMessage) }
    var roomNameEdit by rememberSaveable(room.roomId, room.roomName) { mutableStateOf(room.roomName) }
    var roomTargetMemberEdit by rememberSaveable(room.roomId) { mutableStateOf("") }
    var selectedGiftId by rememberSaveable(room.roomId) {
        mutableStateOf(WanasCatalogData.digitalGifts.firstOrNull()?.giftId ?: "gift_rose")
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("active_room_members_panel"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isDarkMode) Color(0xFF1E1140) else WanasDayDarkHeader
        )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "الأعضاء المتواجدون",
                        tint = WanasAmberGold
                    )
                    Column {
                        Text(
                            text = "داخل الغرفة الآن: ${room.roomName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "الأعضاء الموجودين بالغرفة (${activeMembers.size}) • ${if (room.hasPassword) "🔒 غرفة بباسورد" else "🌐 غرفة عامة"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WanasEmeraldOnline,
                            modifier = Modifier.testTag("active_members_count")
                        )
                    }
                }

                Button(
                    onClick = {
                        voiceEngine.stopRoomSession()
                        onLeaveRoom()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WanasCoralWarm,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("leave_active_room_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "خروج من الغرفة",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("خروج من الغرفة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            // 📌 Room Pinned Welcome Announcement Banner
            if (room.pinnedAnnouncement.isNotBlank()) {
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_pinned_announcement_banner")
                ) {
                    Text(
                        text = "📌 إعلان الغرفة: ${room.pinnedAnnouncement}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WanasAmberGold
                    )
                }
            }

            // ⚔️ TikTok Live Style PK Battle Arena ("جعل التحدي مثل تيكتوك مع شريط التقدم المزدوج وعداد الكومبو والهدايا")
            val pkMins = pkCountdownSeconds / 60
            val pkSecs = pkCountdownSeconds % 60
            val pkFormattedTimer = "%02d:%02d".format(pkMins, pkSecs)
            val totalPkPoints = (room.pkTeamRedScore + room.pkTeamBlueScore).coerceAtLeast(1)
            val redRatio = (room.pkTeamRedScore.toFloat() / totalPkPoints.toFloat()).coerceIn(0.12f, 0.88f)
            val blueRatio = (1f - redRatio).coerceIn(0.12f, 0.88f)
            val redPercent = (redRatio * 100).toInt()
            val bluePercent = 100 - redPercent

            Surface(
                color = Color(0xFF16092E),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, WanasCoralWarm),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_pk_battle_bar")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top VS Header + Live Timer + Combo Multiplier Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔥 الفريق الأحمر",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasCoralWarm
                            )
                            Text(
                                text = "${room.pkTeamRedScore} نقطة ($redPercent%)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Surface(
                            color = if (isPkCountdownRunning) Color(0xFFE11D48) else WanasAmberGold.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.5.dp, WanasAmberGold),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clickable { onTogglePkCountdown() }
                                .testTag("pk_countdown_timer_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isPkCountdownRunning) "⚡ VS • $pkFormattedTimer" else "⚔️ تحدي تيكتوك PK ($pkFormattedTimer)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    color = if (isPkCountdownRunning) Color.White else WanasAmberGold
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "⚡ الفريق الأزرق",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasTealCalm
                            )
                            Text(
                                text = "${room.pkTeamBlueScore} نقطة ($bluePercent%)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // TikTok Live Dual Tug-of-War Progress Bar (Red vs Blue with VS lightning)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, WanasAmberGold.copy(alpha = 0.7f), RoundedCornerShape(50))
                            .testTag("tiktok_pk_tug_of_war_bar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(redRatio)
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFFF1744), Color(0xFFFF5252))
                                        )
                                    ),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = " 🔥 $redPercent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(blueRatio)
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF00B0FF), Color(0xFF00E5FF))
                                        )
                                    ),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "$bluePercent% ⚡ ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00201D),
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                        }

                        Surface(
                            color = WanasAmberGold,
                            shape = CircleShape,
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = "VS",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A103C)
                            )
                        }
                    }

                    // TikTok Combo Streak & Punishment Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = WanasAmberGold.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "🔥 كومبو الهدايا والدعم: x$pkComboStreakCount ${if (pkLastComboTeamLabel.isNotBlank()) "($pkLastComboTeamLabel)" else ""}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                        }

                        Text(
                            text = if (pkTargetTeamIsRed) "🎯 هداياك تدعم: الأحمر 🔥" else "🎯 هداياك تدعم: الأزرق ⚡",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (pkTargetTeamIsRed) WanasCoralWarm else WanasTealCalm
                        )
                    }

                    if (!pkWinnerAnnouncementText.isNullOrBlank()) {
                        Surface(
                            color = WanasDayDarkEmerald,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WanasAmberGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = pkWinnerAnnouncementText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WanasAmberGold
                                )
                                if (pkPunishmentChallengeText.isNotBlank()) {
                                    Text(
                                        text = pkPunishmentChallengeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSelectPkTargetTeam(true)
                                onBoostPkTeam(true)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE11D48),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("pk_boost_red_team_button")
                        ) {
                            Text("🔥 دعم الأحمر (كومبو x$pkComboStreakCount)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                        }
                        Button(
                            onClick = {
                                onSelectPkTargetTeam(false)
                                onBoostPkTeam(false)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasTealCalm,
                                contentColor = Color(0xFF00201D)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("pk_boost_blue_team_button")
                        ) {
                            Text("⚡ دعم الأزرق (كومبو x$pkComboStreakCount)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // 🧧 New Proposal: Lucky Coin Box ("صندوق الحظ / مطر العملات") + 🏆 Top 3 Room Supporters Podium
            Surface(
                color = Color(0xFF211342),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.65f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_lucky_box_and_supporters_bar")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🧧 صندوق الحظ ومطر العملات في الغرفة",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (activeLuckyCoinBox != null && activeLuckyCoinBox.canClaim) {
                                    "🎉 أرسل ${activeLuckyCoinBox.senderName} (#${activeLuckyCoinBox.senderMemberCode}) صندوق حظ! المتبقي: ${activeLuckyCoinBox.remainingCoins} عملة"
                                } else {
                                    "أطلق صندوق حظ ليظهر لجميع الموجودين في الغرفة ويلتقطوا عملات فورية!"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (activeLuckyCoinBox != null && activeLuckyCoinBox.canClaim) {
                            Button(
                                onClick = onClaimLuckyCoinBox,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasEmeraldOnline,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("claim_lucky_coin_box_btn")
                            ) {
                                Text(
                                    text = "🪙 التقط حظك!",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                        } else {
                            Button(
                                onClick = { onDropLuckyCoinBox(100) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasAmberGold,
                                    contentColor = Color(0xFF1A103C)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("drop_lucky_coin_box_btn")
                            ) {
                                Text(
                                    text = "🧧 إطلاق (100🪙)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (roomTopSupporters.isNotEmpty()) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                        Text(
                            text = "🏆 منصة كبار الداعمين في الغرفة (Top Supporters):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("room_top_supporters_podium_row")
                        ) {
                            items(roomTopSupporters.take(3), key = { it.memberIdCode + it.rank }) { sup ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(sup.medalEmoji, style = MaterialTheme.typography.bodyMedium)
                                        Column {
                                            Text(
                                                text = "${sup.memberName} (#${sup.memberIdCode})",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "🪙 ${sup.totalCoinsSupported} Coins",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WanasEmeraldOnline,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 🎙️ 2. Real 8-Seat Voice Stage System ("نظام المقاعد الصوتية الحقيقي بدل مجرد أسماء أعضاء: 8 مقاعد")
            Surface(
                color = Color(0xFF140B2E),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, WanasAmberGold.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_stage_mic_seats_grid")
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎙️ مسرح المقاعد الصوتية الحقيقي (8 مقاعد)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "اضغط على أي مقعد للجلوس أو نقل المقعد أو كتم المايك",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            color = WanasCoralWarm.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, WanasCoralWarm)
                        ) {
                            Text(
                                text = "🔴 LIVE • 8 مقاعد",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                color = Color.White
                            )
                        }
                    }

                    // Render 8 Audio Seats in 2 rows of 4 seats
                    for (rowIdx in 0..1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (colIdx in 0..3) {
                                val seatIdx = rowIdx * 4 + colIdx
                                val seatModel = roomAudioSeats.getOrNull(seatIdx)
                                val occupantMember = activeMembers.getOrNull(seatIdx)
                                val isOccupied = (seatModel != null && !seatModel.isEmpty) || occupantMember != null
                                val displayName = seatModel?.occupantName?.ifBlank { null }
                                    ?: occupantMember?.memberName
                                    ?: "مقعد فارغ ${seatIdx + 1}"
                                val displayEmoji = when {
                                    seatModel != null && !seatModel.isEmpty -> seatModel.occupantEmoji
                                    occupantMember != null -> occupantMember.avatarEmoji
                                    else -> "💺"
                                }
                                val roleBadge = seatModel?.seatRole ?: when (seatIdx) {
                                    0 -> VoiceSeatRoleBadge.HOST
                                    1 -> VoiceSeatRoleBadge.CO_HOST
                                    else -> VoiceSeatRoleBadge.SPEAKER
                                }
                                val isSeatMuted = seatModel?.isMuted == true
                                val hasMicReq = seatModel?.hasPendingMicRequest == true
                                val isMySelectedSeat = selectedSeatIndex == seatIdx
                                val isSpeakingPulse = isOccupied && !isSeatMuted && (
                                    seatModel?.isSpeaking == true ||
                                        (isMySelectedSeat && !voiceState.isMicMuted)
                                    )

                                Surface(
                                    color = when {
                                        isSpeakingPulse -> WanasEmeraldOnline.copy(alpha = 0.28f)
                                        isMySelectedSeat -> WanasVioletAccent.copy(alpha = 0.35f)
                                        isOccupied -> Color(0xFF24164A)
                                        else -> Color.White.copy(alpha = 0.05f)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        width = if (isSpeakingPulse || isMySelectedSeat) 2.dp else 1.dp,
                                        color = when {
                                            isSpeakingPulse -> WanasEmeraldOnline
                                            hasMicReq -> WanasAmberGold
                                            isMySelectedSeat -> WanasAmberGold
                                            isOccupied -> WanasTealCalm.copy(alpha = 0.6f)
                                            else -> Color.White.copy(alpha = 0.18f)
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            if (selectedSeatIndex != seatIdx) {
                                                onMoveMemberBetweenSeats(selectedSeatIndex, seatIdx)
                                                selectedSeatIndex = seatIdx
                                            } else {
                                                onToggleSeatMuteState(seatIdx)
                                            }
                                        }
                                        .testTag("stage_mic_seat_$seatIdx")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        // Role Badge Header: 👑 المضيف / 🛡️ مساعد المضيف / 👤 عضو / 💺 فارغ
                                        Text(
                                            text = if (isOccupied) {
                                                "${roleBadge.emoji} ${roleBadge.labelAr}"
                                            } else {
                                                "💺 مقعد ${seatIdx + 1}"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (seatIdx == 0) WanasAmberGold else WanasTealCalm,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSpeakingPulse) {
                                                        WanasEmeraldOnline.copy(alpha = 0.35f)
                                                    } else if (isOccupied) {
                                                        WanasAmberGold.copy(alpha = 0.25f)
                                                    } else {
                                                        Color.White.copy(alpha = 0.08f)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = displayEmoji,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }

                                        Text(
                                            text = displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOccupied) Color.White else Color.White.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // Status Indicator: 🎙️ يتحدث / ✋ طلب مايك / 🔇 مكتوم / ➕ متاح
                                        Text(
                                            text = when {
                                                !isOccupied -> "➕ شاغر"
                                                hasMicReq -> "✋ طلب مايك"
                                                isSeatMuted -> "🔇 مكتوم"
                                                isSpeakingPulse -> "🎙️ يتحدث"
                                                else -> "👤 جالس"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when {
                                                !isOccupied -> Color.White.copy(alpha = 0.5f)
                                                hasMicReq -> WanasAmberGold
                                                isSeatMuted -> WanasCoralWarm
                                                else -> WanasEmeraldOnline
                                            },
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 🙋 1. Priority #1: New Mic Request Queue System ("نظام طلب المايك الجديد — أولوية قصوى")
                    HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🙋 نظام طلب المايك المباشر (${micRequestQueue.size} في قائمة الانتظار):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            Text(
                                text = when {
                                    isMyMicApprovedByHost || hasRoomAdminAuthority -> "✅ المايك معتمد لك من السيرفر والمضيف"
                                    myMicRequestPending -> "⏳ طلبك قيد الانتظار عند المضيف..."
                                    else -> "🔒 يُمنع فتح المايك تلقائياً عند الدخول — اطلب المايك أولاً"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMyMicApprovedByHost || hasRoomAdminAuthority) WanasEmeraldOnline else Color.White.copy(alpha = 0.8f)
                            )
                        }

                        if (!myMicRequestPending && !isMyMicApprovedByHost) {
                            Button(
                                onClick = { onRequestMicAccess(selectedSeatIndex + 1) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasAmberGold,
                                    contentColor = Color(0xFF1A103C)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("request_mic_access_button")
                            ) {
                                Text("🙋 طلب المايك", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                            }
                        } else if (myMicRequestPending) {
                            OutlinedButton(
                                onClick = onCancelMyMicRequest,
                                border = BorderStroke(1.dp, WanasCoralWarm),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("cancel_my_mic_request_button")
                            ) {
                                Text("❌ إلغاء طلبي", style = MaterialTheme.typography.labelSmall, color = WanasCoralWarm, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    // Host Ordered Numbered Queue of Mic Requests ("يظهر الطلب فوراً عند المضيف في قائمة مرتبة بالأرقام + قبول/رفض الكل")
                    if (micRequestQueue.isNotEmpty()) {
                        Surface(
                            color = Color(0xFF211342),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("host_mic_requests_numbered_queue")
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
                                    Text(
                                        text = "📋 قائمة طلبات المايك المرتبة بالأرقام:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    if (hasRoomAdminAuthority) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { onHostRespondAllMicRequests(true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = WanasEmeraldOnline),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .height(28.dp)
                                                    .testTag("host_approve_all_mic_requests_btn")
                                            ) {
                                                Text("✅ قبول الكل", style = MaterialTheme.typography.labelSmall)
                                            }
                                            OutlinedButton(
                                                onClick = { onHostRespondAllMicRequests(false) },
                                                border = BorderStroke(1.dp, WanasCoralWarm),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .height(28.dp)
                                                    .testTag("host_reject_all_mic_requests_btn")
                                            ) {
                                                Text("❌ رفض الكل", style = MaterialTheme.typography.labelSmall, color = WanasCoralWarm)
                                            }
                                        }
                                    }
                                }

                                micRequestQueue.forEach { req ->
                                    Surface(
                                        color = Color.White.copy(alpha = 0.06f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "#${req.queueNumber} • ${req.userEmoji} ${req.userName} — مقعد ${req.requestedSeatIndex + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (hasRoomAdminAuthority) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Button(
                                                        onClick = { onHostApproveMicRequest(req.requestId) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = WanasEmeraldOnline),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("قبول 🎙️", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { onHostRejectMicRequest(req.requestId) },
                                                        border = BorderStroke(1.dp, WanasCoralWarm),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("رفض", style = MaterialTheme.typography.labelSmall, color = WanasCoralWarm)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 🥁 Suggestion 3: Interactive Stage Sound Effects & Voice Challenge Spinner
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🥁 مؤثرات المسرح وتحديات المايك:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasTealCalm
                        )
                        FilledTonalButton(
                            onClick = onSpinVoiceChallenge,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WanasAmberGold,
                                contentColor = Color(0xFF1A103C)
                            ),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("spin_room_voice_challenge_button")
                        ) {
                            Text("🎲 سؤال تحدي المايك", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    if (room.activeVoiceChallengeQuestion.isNotBlank()) {
                        Surface(
                            color = WanasVioletAccent.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WanasAmberGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = room.activeVoiceChallengeQuestion,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(WanasCatalogData.roomStageSoundEffects, key = { it.id }) { sfx ->
                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable { onTriggerStageSoundEffect(sfx.id) }
                                    .testTag("room_sound_effect_${sfx.id}")
                            ) {
                                Text(
                                    text = "${sfx.emoji} ${sfx.labelAr}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 🔊 3. WebRTC Audio & Live Voice Stage Controls Bar ("تحسين الصوت WebRTC: مؤشر جودة الاتصال، اختبار السماعة، اختبار الميكروفون، إعادة الاتصال تلقائياً")
            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (!voiceState.isMicMuted) WanasEmeraldOnline else WanasTealCalm.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_stage_controls_bar")
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
                        Text(
                            text = voiceState.statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (!voiceState.isMicMuted) WanasEmeraldOnline else WanasAmberGold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = WanasEmeraldOnline.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = voiceState.connectionQualityLabel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = WanasEmeraldOnline,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (!isMyMicApprovedByHost && !hasRoomAdminAuthority) {
                                    onRequestMicAccess(selectedSeatIndex + 1)
                                } else {
                                    val currentlyGranted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    hasMicPermission = currentlyGranted
                                    if (currentlyGranted) {
                                        voiceEngine.toggleMicrophone(scope, true)
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (voiceState.isMicMuted) WanasAmberGold else WanasEmeraldOnline,
                                contentColor = if (voiceState.isMicMuted) Color(0xFF1A103C) else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp)
                                .testTag("toggle_room_mic_button")
                        ) {
                            Icon(
                                imageVector = if (voiceState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "المايك",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when {
                                    !isMyMicApprovedByHost && !hasRoomAdminAuthority -> "🙋 اطلب المايك"
                                    voiceState.isMicMuted -> "فتح المايك"
                                    else -> "كتم المايك"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }

                        FilledTonalButton(
                            onClick = { voiceEngine.toggleSpeakerphone() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (voiceState.isSpeakerphoneOn) WanasTealCalm else Color.White.copy(alpha = 0.14f),
                                contentColor = if (voiceState.isSpeakerphoneOn) Color(0xFF00201D) else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("toggle_room_speaker_button")
                        ) {
                            Icon(
                                imageVector = if (voiceState.isSpeakerphoneOn) Icons.Default.VolumeUp else Icons.Default.Headphones,
                                contentDescription = "السبيكر",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (voiceState.isSpeakerphoneOn) "سبيكر" else "سماعة",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }

                        FilledTonalButton(
                            onClick = { voiceEngine.toggleMicLoopbackMonitor() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (voiceState.isMicMonitorLoopback) WanasVioletAccent else Color.White.copy(alpha = 0.14f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("toggle_mic_monitor_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = "اختبار الميكروفون",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (voiceState.isMicMonitorLoopback) "🎙️ صدى مفعل" else "🎙️ اختبار مايك",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }

                    // WebRTC Audio Diagnostics Row: 🔊 اختبار السماعة + 🔄 إصلاح عدم سماع الموجودين / إعادة اتصال WebRTC
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { voiceEngine.runSpeakerTest(scope) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WanasAmberGold.copy(alpha = 0.22f),
                                contentColor = WanasAmberGold
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("webrtc_speaker_test_button")
                        ) {
                            Text(
                                text = if (voiceState.isSpeakerTestPlaying) "🔊 جاري اختبار السماعة..." else "🔊 اختبار السماعة",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                voiceEngine.triggerAutoReconnectAndFixAudio(
                                    scope = scope,
                                    activePeersCount = activeMembers.size,
                                    roomMembers = activeMembers,
                                    roomSeats = roomAudioSeats
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WanasEmeraldOnline.copy(alpha = 0.22f),
                                contentColor = WanasEmeraldOnline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("webrtc_fix_audio_reconnect_button")
                        ) {
                            Text(
                                text = "🔄 تنشيط الصوت WebRTC (${voiceState.peerConnectionsCount})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // WebRTC Native SDP Offer/Answer + ICE Candidates & Firestore Signaling Live Status Strip
                    Surface(
                        color = Color.Black.copy(alpha = 0.24f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("webrtc_signaling_status_strip")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🌐 ${voiceState.lastSignalingEventLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SDP/ICE • Peers: ${voiceState.peerConnectionsCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WanasTealCalm,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // 🎁 Dazzling Live Room Gift Tray & Firestore Transaction Ledger ("قم بتصميم واجهة لعرض الهدايا داخل الغرف التفاعلية، وربط زر 'إرسال هدية' بدالة ترسل بيانات الهدية إلى Firestore لتوثيق المعاملة بين المستخدمين")
            val currentlySelectedGift = WanasCatalogData.digitalGifts.find { it.giftId == selectedGiftId }
                ?: WanasCatalogData.digitalGifts.first()
            val candidateRecipients = remember(room.roomOwnerName, activeMembers, roomAudioSeats) {
                val names = mutableListOf<String>()
                names.add("مضيف السهرة 👑")
                if (room.roomOwnerName.isNotBlank()) names.add(room.roomOwnerName)
                roomAudioSeats.mapNotNull { it.occupantName?.takeIf { n -> n.isNotBlank() } }.forEach { names.add(it) }
                activeMembers.map { it.memberName }.filter { it.isNotBlank() }.forEach { names.add(it) }
                names.add("جميع المتحدثين بالمسرح 🎙️")
                names.distinct()
            }
            val effectiveRecipient = if (selectedGiftRecipientName.isNotBlank()) {
                selectedGiftRecipientName
            } else {
                candidateRecipients.firstOrNull() ?: "مضيف السهرة 👑"
            }
            val totalSelectedGiftCoins = currentlySelectedGift.costCoins * selectedGiftComboMultiplier
            val totalSelectedPkBoost = (totalSelectedGiftCoins * 3) + (if (selectedGiftComboMultiplier >= 3) 25 else 0)

            Surface(
                color = Color(0xFF28134A),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, WanasAmberGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_room_gifts_showcase_bar")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header + User Balance + Firestore Sync Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎁 واجهة الهدايا التفاعلية وتوثيق Firestore المباشر",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            Text(
                                text = "اختر الهدية والمستلم ومضاعف الكومبو ثم اضغط «إرسال هدية» لتوثيق المعاملة في Firestore",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Surface(
                            color = WanasAmberGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, WanasAmberGold)
                        ) {
                            Text(
                                text = "🪙 رصيدك: $coinsBalance",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                        }
                    }

                    // 1. Recipient Selector Row ("تحديد المستلم لتوثيق المعاملة بين المستخدمين")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "👤 إرسال الهدية إلى (المستلم في الغرفة):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasTealCalm
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("room_gift_recipients_row")
                        ) {
                            items(candidateRecipients, key = { it }) { recName ->
                                val isRecSelected = recName == effectiveRecipient
                                Surface(
                                    color = if (isRecSelected) WanasAmberGold else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isRecSelected) WanasAmberGold else Color.White.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .clickable { onSelectGiftRecipient(recName) }
                                        .testTag("gift_recipient_chip_$recName")
                                ) {
                                    Text(
                                        text = if (isRecSelected) "🎯 $recName" else recName,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isRecSelected) Color(0xFF1A103C) else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // 2. Choose which PK Team receives the Gift Boost + Combo Multiplier Selector (x1, x5, x10, x99)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (pkTargetTeamIsRed) Color(0xFFE11D48).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (pkTargetTeamIsRed) Color(0xFFE11D48) else Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectPkTargetTeam(true) }
                                .testTag("gift_target_red_team_chip")
                        ) {
                            Text(
                                text = "🔥 دعم الفريق الأحمر",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Surface(
                            color = if (!pkTargetTeamIsRed) WanasTealCalm.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (!pkTargetTeamIsRed) WanasTealCalm else Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectPkTargetTeam(false) }
                                .testTag("gift_target_blue_team_chip")
                        ) {
                            Text(
                                text = "⚡ دعم الفريق الأزرق",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // Combo Quantity Multiplier Bar (x1 • x5 • x10 • x99)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ مضاعف الكومبو (Combo):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 5, 10, 99).forEach { mult ->
                                val isMultSelected = selectedGiftComboMultiplier == mult
                                Surface(
                                    color = if (isMultSelected) WanasAmberGold else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isMultSelected) WanasAmberGold else WanasTealCalm.copy(alpha = 0.45f)
                                    ),
                                    modifier = Modifier
                                        .clickable { onSelectGiftComboMultiplier(mult) }
                                        .testTag("gift_combo_multiplier_x$mult")
                                ) {
                                    Text(
                                        text = "x$mult",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isMultSelected) Color(0xFF1A103C) else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // 3. Celebration Banner
                    if (!latestGiftBannerText.isNullOrBlank()) {
                        Surface(
                            color = WanasDayDarkEmerald,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, WanasAmberGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("room_gift_celebration_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🎆", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = latestGiftBannerText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 4. Interactive Gift Cards Carousel
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_gifts_lazy_row")
                    ) {
                        items(WanasCatalogData.digitalGifts, key = { it.giftId }) { gift ->
                            val isLuxury = gift.costCoins >= 100
                            val isSelectedGift = gift.giftId == currentlySelectedGift.giftId
                            val pkBoostPts = gift.costCoins * 3
                            Surface(
                                color = when {
                                    isSelectedGift -> Color(0xFF4C1D95)
                                    isLuxury -> Color(0xFF3B1968)
                                    else -> Color.White.copy(alpha = 0.1f)
                                },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    width = if (isSelectedGift) 2.dp else if (isLuxury) 1.5.dp else 1.dp,
                                    color = if (isSelectedGift) WanasEmeraldOnline else if (isLuxury) WanasAmberGold else WanasTealCalm.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier
                                    .clickable {
                                        selectedGiftId = gift.giftId
                                    }
                                    .testTag("room_gift_chip_${gift.giftId}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        color = if (isSelectedGift) WanasEmeraldOnline.copy(alpha = 0.25f) else if (isLuxury) WanasAmberGold.copy(alpha = 0.25f) else WanasTealCalm.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = if (isSelectedGift) "✓ محددة الآن" else gift.tierLabelAr,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelectedGift) WanasEmeraldOnline else if (isLuxury) WanasAmberGold else WanasTealCalm
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isLuxury) {
                                                    WanasAmberGold.copy(alpha = 0.22f)
                                                } else {
                                                    Color.White.copy(alpha = 0.08f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(gift.emoji, style = MaterialTheme.typography.headlineSmall)
                                    }
                                    Text(
                                        text = gift.nameAr,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "+$pkBoostPts نقطة PK ⚔️",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WanasEmeraldOnline
                                    )
                                    Surface(
                                        color = WanasAmberGold,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable {
                                            selectedGiftId = gift.giftId
                                            onSendRoomGift(gift, effectiveRecipient, selectedGiftComboMultiplier)
                                        }
                                    ) {
                                        Text(
                                            text = "${gift.costCoins} 🪙 • إرسال سريع",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1A103C)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Primary Action Bar: "إرسال هدية" Button Linked directly to Firestore Transaction Function
                    Surface(
                        color = Color(0xFF1A0D33),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, WanasEmeraldOnline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_gift_firestore_checkout_bar")
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "الهدية المختارة: ${currentlySelectedGift.emoji} ${currentlySelectedGift.nameAr} (x$selectedGiftComboMultiplier)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    Text(
                                        text = "المستلم: $effectiveRecipient • التكلفة: $totalSelectedGiftCoins 🪙 • دعم: +$totalSelectedPkBoost نقطة PK",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onSendRoomGift(
                                        currentlySelectedGift,
                                        effectiveRecipient,
                                        selectedGiftComboMultiplier
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasAmberGold,
                                    contentColor = Color(0xFF1A103C)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("send_room_gift_firestore_button")
                            ) {
                                Text(
                                    text = if (isRecordingGiftToFirestore) {
                                        "⏳ جاري توثيق الهدية في Firestore..."
                                    } else {
                                        "🎁 إرسال هدية (${currentlySelectedGift.emoji} ${currentlySelectedGift.nameAr} x$selectedGiftComboMultiplier — $totalSelectedGiftCoins 🪙)"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Text(
                                text = if (!lastFirestoreGiftTxId.isNullOrBlank()) {
                                    "☁️ تم التوثيق الفوري في Firestore (/chat_rooms/${room.roomId}/room_gifts/$lastFirestoreGiftTxId)"
                                } else {
                                    "☁️ يتم حفظ كل معاملة هدية تلقائياً في Firestore لتوثيق العمليات بين المستخدمين"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = WanasEmeraldOnline,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 6. Live Firestore Gift Transactions Ledger Inside Room ("سجل معاملات الهدايا الموثقة في Firestore")
                    if (roomGiftTransactions.isNotEmpty()) {
                        Surface(
                            color = Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("room_firestore_gift_transactions_list")
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
                                    Text(
                                        text = "📜 سجل معاملات الهدايا الموثقة في Firestore (${roomGiftTransactions.size}):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                    Text(
                                        text = "Firestore Live ✅",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasEmeraldOnline
                                    )
                                }

                                roomGiftTransactions.take(4).forEach { tx ->
                                    Surface(
                                        color = Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("firestore_gift_tx_row_${tx.transactionId}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${tx.giftEmoji} ${tx.senderName} (${tx.senderMemberCode}) ⬅️ ${tx.recipientName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "الهدية: ${tx.giftNameAr} x${tx.quantity} • +${tx.pkBonusPoints} PK (${if (tx.pkTeamSupported == "RED") "الأحمر 🔥" else "الأزرق ⚡"})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.78f)
                                                )
                                            }
                                            Surface(
                                                color = WanasEmeraldOnline.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "${tx.costCoins} 🪙 ✓",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = WanasEmeraldOnline
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

            // 🛡️ Full Room Admin & App Owner Control Panel ("الغرف بالكامل تظهر تحكماتها للادمن الغرفة وصاحب التطبيق حتى لو كانت غرفة بباسورد")
            if (hasRoomAdminAuthority) {
                Surface(
                    color = Color(0xFF172B3A),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, WanasEmeraldOnline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_admin_full_controls_panel")
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
                            Text(
                                text = if (isAppOwner) {
                                    "👑 تحكم كامل بالغرفة (صلاحية مالك التطبيق + أدمن الغرفة)"
                                } else {
                                    "🛡️ لوحة تحكم أدمن ومؤسس الغرفة"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = WanasAmberGold
                            )
                            if (room.hasPassword) {
                                Surface(
                                    color = WanasAmberGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "🔑 الباسورد: ${room.roomPassword}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WanasAmberGold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { onModerateRoomAction("كتم جميع المايكات مؤقتاً لتنظيم المسرح 🔇", null) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_admin_mute_all_button")
                            ) {
                                Text("🔇 كتم المايكات", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            FilledTonalButton(
                                onClick = { onModerateRoomAction("قفل المقاعد الفارغة وتفعيل وضع VIP 🔒", null) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_admin_lock_seats_button")
                            ) {
                                Text("🔒 قفل المقاعد", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onDeleteRoomByAdmin,
                                colors = ButtonDefaults.buttonColors(containerColor = WanasCoralWarm),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_admin_delete_room_button")
                            ) {
                                Text("🗑️ حذف الغرفة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = adminRoomPassEdit,
                                onValueChange = { adminRoomPassEdit = it },
                                label = { Text("تعديل أو إلغاء باسورد الغرفة") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WanasAmberGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_admin_password_edit_input")
                            )
                            Button(
                                onClick = { onUpdateRoomPassword(adminRoomPassEdit) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasAmberGold,
                                    contentColor = Color(0xFF1A103C)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("room_admin_save_password_button")
                            ) {
                                Text("تحديث الباسورد", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        // Extra Room Admin & App Owner Controls: Announcement, Mic Seats (4 vs 8), Clear Chat, Reset PK, Pin Room
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = adminAnnouncementEdit,
                                onValueChange = { adminAnnouncementEdit = it },
                                label = { Text("تعديل الإعلان المثبت للغرفة") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WanasAmberGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_admin_announcement_edit_input")
                            )
                            Button(
                                onClick = { onUpdateRoomSettingsByAdmin(adminAnnouncementEdit, room.micSeatsCount) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasEmeraldOnline,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("room_admin_save_announcement_button")
                            ) {
                                Text("حفظ الإعلان", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val nextSeats = if (room.micSeatsCount <= 4) 8 else 4
                                    onUpdateRoomSettingsByAdmin(adminAnnouncementEdit, nextSeats)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_admin_toggle_seats_count_button")
                            ) {
                                Text(
                                    text = if (room.micSeatsCount <= 4) "🎙️ توسيع لـ 8 مايكات" else "🎙️ تقليص لـ 4 مايكات",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            FilledTonalButton(
                                onClick = { onResetPkOrClearChatByAdmin(true) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_admin_clear_chat_button")
                            ) {
                                Text("🧹 مسح الدردشة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            FilledTonalButton(
                                onClick = { onTogglePinOrLockByAdmin(true, false) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_admin_pin_room_button")
                            ) {
                                Text(
                                    text = if (room.isPinnedByAdmin) "📌 مثبّتة" else "📌 تثبيت الغرفة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.14f))

                        // 🎙️ Level 2: 11-Module Room Admin Scoped Panel ("ثانياً: لوحة تحكم أدمن الغرفة — 11 قسماً مخصصاً للغرفة فقط")
                        Text(
                            text = "🎙️ أقسام لوحة أدمن الغرفة المستقلة (11 قسماً مخصصاً لهذه الغرفة فقط):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("room_admin_11_modules_tabs_row")
                        ) {
                            items(RoomAdminModuleTab.entries.toList(), key = { it.id }) { rTab ->
                                val isSel = selectedRoomAdminTabId == rTab.id
                                Surface(
                                    color = if (isSel) WanasAmberGold else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .clickable { selectedRoomAdminTabId = rTab.id }
                                        .testTag("room_admin_tab_${rTab.id}")
                                ) {
                                    Text(
                                        text = "${rTab.emoji} ${rTab.titleAr}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSel) Color(0xFF1A103C) else Color.White
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("room_admin_selected_module_box_$selectedRoomAdminTabId")
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                when (selectedRoomAdminTabId) {
                                    RoomAdminModuleTab.ROOM_OVERVIEW.id -> {
                                        Text(
                                            text = "🏠 نظرة عامة على الغرفة: ${room.roomName} • المالك: ${room.roomOwnerName} • المساعدون: ${room.coHostMemberNames.joinToString().ifBlank { room.assignedRoomAdminName.ifBlank { "لا يوجد" } }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "🚫 لا يملك أدمن الغرفة تعديل العملات أو حظر المستخدم من التطبيق كله أو الدخول لغرف أخرى أو رؤية بيانات حساسة.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WanasTealCalm
                                        )
                                    }

                                    RoomAdminModuleTab.ROOM_SETTINGS.id -> {
                                        OutlinedTextField(
                                            value = roomNameEdit,
                                            onValueChange = { roomNameEdit = it },
                                            label = { Text("تعديل اسم الغرفة (الحد الأقصى: ${room.maxRoomMembersCapacity} عضو)") },
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
                                                    onUpdateRoomAdminScopedSettings(
                                                        false, false, false, false, null, null, roomNameEdit, null, room.maxRoomMembersCapacity
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = WanasEmeraldOnline),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("حفظ اسم الغرفة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                            }
                                            FilledTonalButton(
                                                onClick = {
                                                    val nextCap = if (room.maxRoomMembersCapacity <= 50) 100 else 50
                                                    onUpdateRoomAdminScopedSettings(
                                                        false, false, false, false, null, null, null, null, nextCap
                                                    )
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("السعة: ${room.maxRoomMembersCapacity} عضو", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    RoomAdminModuleTab.ROOM_MEMBERS.id -> {
                                        OutlinedTextField(
                                            value = roomTargetMemberEdit,
                                            onValueChange = { roomTargetMemberEdit = it },
                                            label = { Text("اسم العضو داخل الغرفة (كتم / طرد / حظر من الغرفة / تعيين مساعد)") },
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
                                            val memberActions = listOf(
                                                "MUTE_MIC" to "🔇 كتم المايك",
                                                "TEMP_MUTE" to "⏱️ كتم مؤقت",
                                                "BAN_FROM_ROOM" to "🚫 حظر من الغرفة",
                                                "KICK_FROM_ROOM" to "👢 طرد",
                                                "ASSIGN_COHOST" to "🛡️ تعيين مساعد",
                                                "REMOVE_COHOST" to "إزالة مساعد"
                                            )
                                            items(memberActions, key = { it.first }) { (actKey, actLabel) ->
                                                Button(
                                                    onClick = {
                                                        val target = roomTargetMemberEdit.ifBlank {
                                                            activeMembers.firstOrNull()?.memberName ?: "عضو الغرفة"
                                                        }
                                                        onManageRoomMemberByRoomAdmin(target, actKey)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (actKey.contains("BAN") || actKey.contains("KICK")) WanasCoralWarm else WanasVioletAccent
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(actLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    RoomAdminModuleTab.MIC_REQUESTS.id -> {
                                        Text(
                                            text = "🎙️ جدول طلبات المايك (الترتيب • العضو • وقت الطلب • الحالة • الإجراء — موافقة من السيرفر):",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = WanasAmberGold
                                        )
                                        if (micRequestQueue.isEmpty()) {
                                            Text("لا توجد طلبات مايك في الانتظار حالياً.", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                        } else {
                                            micRequestQueue.forEach { req ->
                                                Text(
                                                    text = "#${req.queueNumber} | ${req.userName} | مقعد ${req.requestedSeatIndex + 1} | انتظار ⏳",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    RoomAdminModuleTab.SEATS_MANAGEMENT.id -> {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                            Button(
                                                onClick = { onVacateOrSpeakerOnlySeats(selectedSeatIndex, false) },
                                                colors = ButtonDefaults.buttonColors(containerColor = WanasCoralWarm),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("🪑 إخلاء مقعد ${selectedSeatIndex + 1}", style = MaterialTheme.typography.labelSmall)
                                            }
                                            Button(
                                                onClick = { onVacateOrSpeakerOnlySeats(null, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = WanasAmberGold, contentColor = Color(0xFF1A103C)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("🎙️ للمتحدث المعتمد فقط", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }

                                    RoomAdminModuleTab.SPEAKERS_STAGE.id -> {
                                        val activeSpeakers = roomAudioSeats.count { !it.isEmpty && !it.isMuted }
                                        Text(
                                            text = "🔊 المتحدثون الآن على المسرح: $activeSpeakers من أصل ${room.micSeatsCount} مقاعد",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WanasEmeraldOnline,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    RoomAdminModuleTab.CHAT_CONTROL.id -> {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                            FilledTonalButton(
                                                onClick = {
                                                    onUpdateRoomAdminScopedSettings(
                                                        true, false, false, false, null, null, null, null, null
                                                    )
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = if (room.isRoomChatLocked) "🔓 فتح الشات" else "🔒 قفل الشات",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            FilledTonalButton(
                                                onClick = {
                                                    onUpdateRoomAdminScopedSettings(
                                                        false, true, false, false, null, null, null, null, null
                                                    )
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = if (room.isSlowModeActive) "🐢 Slow Mode: نشط" else "⚡ Slow Mode: عادي",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            FilledTonalButton(
                                                onClick = {
                                                    onUpdateRoomAdminScopedSettings(
                                                        false, false, true, false, null, null, null, null, null
                                                    )
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = if (room.isAdminOnlyChatMode) "🛡️ للأدمن فقط" else "💬 للجميع",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                        if (roomMessages.isNotEmpty()) {
                                            val lastMsg = roomMessages.last()
                                            Button(
                                                onClick = { onDeleteSingleMessageByAdmin(lastMsg.messageId) },
                                                colors = ButtonDefaults.buttonColors(containerColor = WanasCoralWarm),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("🗑️ حذف آخر رسالة (${lastMsg.senderName})", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    RoomAdminModuleTab.GIFTS_CONTROL.id -> {
                                        Button(
                                            onClick = {
                                                onUpdateRoomAdminScopedSettings(
                                                    false, false, false, true, null, null, null, null, null
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (room.areRoomGiftsEnabled) WanasEmeraldOnline else WanasCoralWarm
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (room.areRoomGiftsEnabled) "🎁 الهدايا داخل الغرفة: مفعلة" else "🚫 الهدايا داخل الغرفة: متوقفة",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    RoomAdminModuleTab.PINNED_ANNOUNCEMENT.id -> {
                                        OutlinedTextField(
                                            value = roomWelcomeEdit,
                                            onValueChange = { roomWelcomeEdit = it },
                                            label = { Text("رسالة الترحيب التلقائية عند دخول عضو جديد") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = WanasAmberGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Button(
                                            onClick = {
                                                onUpdateRoomAdminScopedSettings(
                                                    false, false, false, false, null, roomWelcomeEdit, null, null, null
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = WanasEmeraldOnline),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("💬 حفظ رسالة الترحيب التلقائية", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }

                                    RoomAdminModuleTab.ROOM_RULES.id -> {
                                        OutlinedTextField(
                                            value = roomRulesEdit,
                                            onValueChange = { roomRulesEdit = it },
                                            label = { Text("📜 قوانين الغرفة المعتمدة") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = WanasAmberGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Button(
                                            onClick = {
                                                onUpdateRoomAdminScopedSettings(
                                                    false, false, false, false, roomRulesEdit, null, null, null, null
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = WanasAmberGold, contentColor = Color(0xFF1A103C)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("📜 حفظ قوانين الغرفة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }

                                    RoomAdminModuleTab.ROOM_STATS.id -> {
                                        Text(
                                            text = "📊 إحصائيات الغرفة: الحضور (${activeMembers.size}) • الرسائل (${roomMessages.size}) • نقاط PK (${room.pkTeamRedScore} مقابل ${room.pkTeamBlueScore})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WanasEmeraldOnline,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Horizontal list of members currently present inside the room
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_members_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeMembers, key = { it.userId }) { member ->
                    Surface(
                        color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else WanasDayLightSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, WanasEmeraldOnline.copy(alpha = 0.65f)),
                        modifier = Modifier.testTag("active_member_chip_${member.userId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(WanasAmberGold.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = member.avatarEmoji)
                            }
                            Column {
                                Text(
                                    text = member.memberName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDarkMode) Color.White else WanasDayTextPrimary
                                )
                                Text(
                                    text = member.roleBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDarkMode) WanasTealCalm else WanasTealDeep
                                )
                            }
                        }
                    }
                }
            }

            // Live In-Room Chat Feed & Message Composer
            Surface(
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_room_chat_box")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💬 دردشة الغرفة المباشرة (${roomMessages.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = WanasAmberGold
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        roomMessages.takeLast(4).forEach { msg ->
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = msg.senderEmoji)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${msg.senderName} (${msg.senderRoleBadge})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = WanasAmberGold
                                        )
                                        Text(
                                            text = msg.messageText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = liveChatInput,
                            onValueChange = { liveChatInput = it },
                            label = { Text("اكتب رسالة في الغرفة...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = WanasAmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedLabelColor = WanasAmberGold,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
                                cursorColor = WanasAmberGold
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("active_room_chat_input")
                        )

                        Button(
                            onClick = {
                                if (liveChatInput.isNotBlank()) {
                                    onSendMessage(liveChatInput)
                                    liveChatInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WanasEmeraldOnline,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("active_room_send_chat_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRoomMetadataItemCard(
    room: ChatRoomMetadata,
    isDarkMode: Boolean,
    isInsideThisRoom: Boolean,
    isFavoriteRoom: Boolean = false,
    canDelete: Boolean,
    hasRoomAdminAuthority: Boolean = canDelete,
    isAppOwner: Boolean = false,
    onEnterRoom: () -> Unit,
    onLeaveRoom: () -> Unit,
    onSelectCategoryFilter: (String) -> Unit = {},
    onToggleFavoriteRoom: () -> Unit = {},
    onNotifyFavoriteLiveBroadcast: () -> Unit = {},
    onSendPrivateInviteToRoom: () -> Unit = {},
    onUpdateRoomPassword: (String) -> Unit = {},
    onDelete: () -> Unit
) {
    val formattedDate = remember(room.timestampMillis) {
        val ms = if (room.timestampMillis > 0L) room.timestampMillis else System.currentTimeMillis()
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ms))
    }
    val categoryFilterMeta = remember(room.roomCategory) {
        WanasRoomCategoryFilter.resolveDisplayFilter(room.roomCategory)
    }

    var showPasswordPrompt by rememberSaveable(room.roomId) { mutableStateOf(false) }
    var enteredRoomPassword by rememberSaveable(room.roomId) { mutableStateOf("") }
    var passwordErrorText by rememberSaveable(room.roomId) { mutableStateOf<String?>(null) }
    var showAdminQuickControls by rememberSaveable(room.roomId) { mutableStateOf(false) }

    val cardBackground = when {
        isInsideThisRoom && !isDarkMode -> WanasDayDarkEmerald
        isInsideThisRoom && isDarkMode -> Color(0xFF064E3B)
        !isDarkMode -> WanasDaySurfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val titleColor = when {
        isInsideThisRoom -> WanasAmberGold
        !isDarkMode -> WanasDayDarkHeader
        else -> WanasAmberGold
    }

    val subtitleColor = when {
        isInsideThisRoom -> Color.White.copy(alpha = 0.9f)
        !isDarkMode -> WanasDayTextSecondary
        else -> Color.White.copy(alpha = 0.85f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("room_item_${room.roomId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(
            width = if (isInsideThisRoom) 2.dp else 1.dp,
            color = if (isInsideThisRoom) WanasAmberGold else WanasVioletAccent.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = room.roomName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor
                        )
                        if (room.hasPassword) {
                            Surface(
                                color = WanasCoralWarm.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, WanasCoralWarm)
                            ) {
                                Text(
                                    text = "🔒 بباسورد",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isInsideThisRoom || isDarkMode) WanasAmberGold else WanasCoralWarm
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = WanasTealCalm.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, WanasTealCalm.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clickable { onSelectCategoryFilter(categoryFilterMeta.id) }
                                .testTag("room_category_badge_${room.roomId}")
                        ) {
                            Text(
                                text = "${categoryFilterMeta.emoji} ${categoryFilterMeta.labelAr}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isInsideThisRoom || isDarkMode) WanasTealCalm else WanasTealDeep
                            )
                        }
                        if (room.roomTopicTag.isNotBlank() && !room.roomTopicTag.contains(categoryFilterMeta.labelAr)) {
                            Text(
                                text = "• ${room.roomTopicTag}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = subtitleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Text(
                        text = "معرّف المنشئ: ${room.creatorId} • 🎙️ ${room.micSeatsCount} مقاعد صوتية",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor
                    )
                    Text(
                        text = "وقت الإنشاء: $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isInsideThisRoom || isDarkMode) WanasTealCalm else WanasTealDeep
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_room_${room.roomId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الغرفة",
                            tint = WanasCoralWarm
                        )
                    }
                }
            }

            // Room Admin & App Owner Full Controls Bar (visible to Room Admin & App Owner even if password-protected)
            if (hasRoomAdminAuthority) {
                Surface(
                    color = if (isDarkMode || isInsideThisRoom) Color.White.copy(alpha = 0.1f) else WanasDayDarkHeader.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WanasAmberGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_card_admin_authority_bar_${room.roomId}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAppOwner) {
                                    "👑 تحكم المالك وأدمن الغرفة متاح بالكامل"
                                } else {
                                    "🛡️ تحكم أدمن الغرفة متاح بالكامل"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isInsideThisRoom || isDarkMode) WanasAmberGold else WanasDayDarkHeader
                            )
                            if (room.hasPassword) {
                                Text(
                                    text = "🔑 الباسورد: ${room.roomPassword}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WanasEmeraldOnline
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (room.hasPassword) {
                                FilledTonalButton(
                                    onClick = { onUpdateRoomPassword("") },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("admin_unlock_room_password_${room.roomId}")
                                ) {
                                    Text("🔓 إلغاء الباسورد", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                FilledTonalButton(
                                    onClick = { onUpdateRoomPassword("2026") },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("admin_set_room_password_${room.roomId}")
                                ) {
                                    Text("🔒 قفل بباسورد (2026)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }

                            FilledTonalButton(
                                onClick = { showAdminQuickControls = !showAdminQuickControls },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (showAdminQuickControls) "إخفاء التحكم" else "⚙️ إدارة الغرفة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Password prompt for non-admin members trying to enter a password-protected room
            if (showPasswordPrompt && room.hasPassword && !hasRoomAdminAuthority) {
                Surface(
                    color = Color(0xFF231344),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WanasAmberGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🔒 هذه الغرفة محمية بباسورد — أدخل الكود للدخول:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WanasAmberGold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = enteredRoomPassword,
                                onValueChange = {
                                    enteredRoomPassword = it
                                    passwordErrorText = null
                                },
                                label = { Text("باسورد الغرفة") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("enter_room_password_input_${room.roomId}")
                            )
                            Button(
                                onClick = {
                                    if (enteredRoomPassword.trim() == room.roomPassword.trim()) {
                                        showPasswordPrompt = false
                                        passwordErrorText = null
                                        onEnterRoom()
                                    } else {
                                        passwordErrorText = "باسورد الغرفة غير صحيح"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WanasAmberGold,
                                    contentColor = Color(0xFF1A103C)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("confirm_room_password_button_${room.roomId}")
                            ) {
                                Text("تأكيد الدخول", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        if (passwordErrorText != null) {
                            Text(
                                text = passwordErrorText ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = WanasCoralWarm,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Action buttons organized in two clean rows so no button is ever squished or crushed
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onToggleFavoriteRoom,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isFavoriteRoom) WanasAmberGold else if (isDarkMode || isInsideThisRoom) Color.White.copy(alpha = 0.14f) else WanasDayDarkHeader.copy(alpha = 0.1f),
                            contentColor = if (isFavoriteRoom) Color(0xFF1A103C) else titleColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("toggle_favorite_room_${room.roomId}")
                    ) {
                        Text(
                            text = if (isFavoriteRoom) "🔔 متابَعة" else "☆ متابعة",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    FilledTonalButton(
                        onClick = onNotifyFavoriteLiveBroadcast,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasCoralWarm.copy(alpha = 0.22f),
                            contentColor = WanasCoralWarm
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("notify_live_broadcast_room_${room.roomId}")
                    ) {
                        Text(
                            text = "🔴 تنبيه نشاط",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    FilledTonalButton(
                        onClick = onSendPrivateInviteToRoom,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WanasTealCalm.copy(alpha = 0.22f),
                            contentColor = if (isDarkMode || isInsideThisRoom) WanasTealCalm else WanasTealDeep
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("send_private_invite_room_${room.roomId}")
                    ) {
                        Text(
                            text = "💌 دعوة خاصة",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isInsideThisRoom) {
                    OutlinedButton(
                        onClick = onLeaveRoom,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, WanasCoralWarm),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("leave_room_${room.roomId}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = WanasCoralWarm,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مغادرة الغرفة",
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            // Room Admin and App Owner enter immediately even if the room is password-protected
                            if (!room.hasPassword || hasRoomAdminAuthority) {
                                onEnterRoom()
                            } else {
                                showPasswordPrompt = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) WanasAmberGold else WanasDayDarkHeader,
                            contentColor = if (isDarkMode) Color(0xFF1A103C) else WanasAmberGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("enter_room_${room.roomId}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (room.hasPassword && hasRoomAdminAuthority) {
                                "دخول الغرفة (تجاوز الباسورد للإدارة)"
                            } else {
                                "دخول الغرفة الصوتية الآن"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
