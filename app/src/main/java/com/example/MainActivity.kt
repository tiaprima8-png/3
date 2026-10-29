package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.remote.ApiClientManager
import com.example.data.repository.HeavyTrackRepository
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DistributionDialog
import com.example.ui.screens.FormReportScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.ErrorRose50
import com.example.ui.theme.HeavyTrackTheme
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.HeavyTrackViewModel
import com.example.ui.viewmodel.HeavyTrackViewModelFactory
import com.example.ui.viewmodel.UiMessage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Schedule WorkManager background auto-retry synchronization
        com.example.worker.OfflineSyncWorker.scheduleAutoSync(this)
        setContent {
            HeavyTrackTheme {
                HeavyTrackApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeavyTrackApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val apiClientManager = remember { ApiClientManager(context) }
    val repository = remember { HeavyTrackRepository(database, apiClientManager) }
    val viewModel: HeavyTrackViewModel = viewModel(
        factory = HeavyTrackViewModelFactory(repository)
    )

    var currentTab by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }
    var showDistributionDialog by remember { mutableStateOf(false) }
    val customApkUrl by viewModel.customApkUrl.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isManagerSession by viewModel.isManagerSession.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val isSyncingMaster by viewModel.isSyncingMaster.collectAsState()
    val isSyncingPending by viewModel.isSyncingPending.collectAsState()
    val isBootstrapping by viewModel.isBootstrapping.collectAsState()
    val isOfflineSimulated by viewModel.isOfflineSimulated.collectAsState()
    val isSocketConnected by viewModel.isSocketConnected.collectAsState()
    val masterUsers by viewModel.masterUsers.collectAsState()
    val isSyncingUsers by viewModel.isSyncingUsers.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle back button: kembali ke tab utama (Tab 0 - Formulir) jika sedang berada di tab lain
    BackHandler(enabled = isLoggedIn && currentTab != 0) {
        currentTab = 0
    }

    // Reset tab ke posisi awal saat role berganti atau login baru
    LaunchedEffect(isManagerSession, isLoggedIn) {
        currentTab = 0
    }

    // Collect UI messages / notifications
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiMessage.Success -> snackbarHostState.showSnackbar(event.message)
                is UiMessage.Error -> snackbarHostState.showSnackbar(event.message)
                is UiMessage.Info -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (!isLoggedIn) {
        Box(modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                masterUsers = masterUsers,
                isSyncingUsers = isSyncingUsers,
                onRefreshUsers = { viewModel.syncMasterUsers() },
                onLoginSuccess = { supervisor ->
                    viewModel.login(supervisor)
                },
                onLoginWithApi = { username, password ->
                    viewModel.loginWithApi(username, password)
                }
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = Slate900,
                            contentColor = Color.White,
                            actionColor = Emerald600
                        )
                    }
                )
            },
            topBar = {
                val supervisorSession by viewModel.currentSupervisorSession.collectAsState()
                val activeSupervisorName by viewModel.activeSupervisor.collectAsState()

                Column {
                    // Header (Top App Bar) Deep Blue (#1E3A8A)
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.testTag("app_header_logo_title")
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Emerald500,
                                    modifier = Modifier.size(36.dp),
                                    shadowElevation = 2.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PrecisionManufacturing,
                                            contentDescription = "Logo RKCE",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "RKCE",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            letterSpacing = (-0.3).sp
                                        )
                                    )
                                    Text(
                                        text = "Rencana Kerja Civil Engineering",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        },
                        actions = {
                            // 1. Tombol "Unduh APK" (Hijau Emerald, rounded-lg, dengan ikon download putih kecil)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Emerald600,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showDistributionDialog = true }
                                    .testTag("appbar_download_installer_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Unduh APK",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Unduh APK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // 2. Tombol "Keluar" (Putih transparan dengan ikon logout)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showLogoutDialog = true }
                                    .testTag("appbar_logout_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Keluar",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Keluar",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.ui.theme.DeepBlue),
                        modifier = Modifier.testTag("main_app_bar")
                    )

                    // 3. DATA PENGGUNA (USER CARD) - Tampil terpisah tepat di bawah Top Bar
                    val rawSpvName = supervisorSession?.namaLengkap?.ifBlank { null }
                        ?: supervisorSession?.name
                        ?: activeSupervisorName
                    val initials = getInitials(rawSpvName)
                    val wilayahDisplay = supervisorSession?.wilayah ?: "Geodesi PG1"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(com.example.ui.theme.BackgroundGreyBlue)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("active_supervisor_identity_bar")
                    ) {
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { showProfilePopup = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Avatar bulat hijau dengan inisial "AG" (teks putih)
                                    Surface(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("avatar_initials_circle"),
                                        shape = CircleShape,
                                        color = Emerald600,
                                        shadowElevation = 1.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = initials.ifBlank { "AG" },
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // "Agung • Geodesi PG1" (Teks gelap, medium)
                                    Text(
                                        text = "$rawSpvName • $wilayahDisplay",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    )
                                }

                                // Badge "Aktif" (Hijau emerald lembut, teks hijau gelap)
                                Surface(
                                    shape = CircleShape,
                                    color = Emerald50,
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Emerald500, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Aktif",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Navigation Bar: Berlatar putih bersih dengan garis pembatas atas sangat tipis
                Column {
                    androidx.compose.material3.HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Slate200
                    )
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                        modifier = Modifier.testTag("main_navigation_bar")
                    ) {
                        // 1. [Formulir] (Ikon clipboard aktif hijau, dengan background chip bulat tipis)
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = {
                                currentTab = 0
                                viewModel.autoFetchMasterDataOnResume()
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = "Formulir",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Formulir",
                                    fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald700,
                                selectedTextColor = Slate900,
                                indicatorColor = Emerald100,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate500
                            ),
                            modifier = Modifier.testTag("nav_item_form")
                        )

                        // 2. [Riwayat] (Ikon jam)
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (unsyncedCount > 0) {
                                            Badge(
                                                containerColor = WarningAmber,
                                                contentColor = Slate900
                                            ) {
                                                Text("$unsyncedCount", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Riwayat",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = "Riwayat",
                                    fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald700,
                                selectedTextColor = Slate900,
                                indicatorColor = Emerald100,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate500
                            ),
                            modifier = Modifier.testTag("nav_item_history")
                        )

                        // 3. [Analitik] (Ikon grafik batang)
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Analitik",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Analitik",
                                    fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald700,
                                selectedTextColor = Slate900,
                                indicatorColor = Emerald100,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate500
                            ),
                            modifier = Modifier.testTag("nav_item_analytics")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {
                when (currentTab) {
                    0 -> FormReportScreen(
                        viewModel = viewModel,
                        onNavigateToHistory = { currentTab = 1 }
                    )
                    1 -> HistoryScreen(
                        viewModel = viewModel,
                        onNavigateToForm = { currentTab = 0 }
                    )
                    2 -> AnalyticsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }

        if (showDistributionDialog) {
            DistributionDialog(
                onDismiss = { showDistributionDialog = false },
                currentApkUrl = customApkUrl,
                appVersionName = viewModel.getAppVersionName(context),
                onSaveApkLocally = {
                    viewModel.downloadApk(context)
                    showDistributionDialog = false
                },
                onShareWhatsApp = {
                    viewModel.shareApkViaWhatsApp(context)
                    showDistributionDialog = false
                },
                onShareGeneral = {
                    viewModel.shareApkGeneral(context)
                    showDistributionDialog = false
                },
                onInstallLocalApk = {
                    viewModel.launchApkInstaller(context)
                    showDistributionDialog = false
                },
                onOpenInBrowser = { url ->
                    viewModel.openApkDownloadInBrowser(context, url)
                },
                onUpdateApkUrl = { newUrl ->
                    viewModel.updateCustomApkUrl(newUrl)
                }
            )
        }

        if (showProfilePopup) {
            val supervisorSession by viewModel.currentSupervisorSession.collectAsState()
            val activeSupervisorName by viewModel.activeSupervisor.collectAsState()
            val spvName = supervisorSession?.namaLengkap?.ifBlank { null } ?: supervisorSession?.name ?: activeSupervisorName
            val initials = getInitials(spvName)
            val isSuperAdmin = supervisorSession?.isSuperAdmin == true
            val isMgr = supervisorSession?.isManager == true
            val avatarBg = when {
                isSuperAdmin -> Color(0xFF6366F1)
                isMgr -> Color(0xFFF59E0B)
                else -> Emerald600
            }

            AlertDialog(
                onDismissRequest = { showProfilePopup = false },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = avatarBg,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = spvName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isSuperAdmin -> Color(0xFFEEF2FF)
                                isMgr -> Color(0xFFFEF3C7)
                                else -> Emerald100
                            }
                        ) {
                            Text(
                                text = supervisorSession?.role ?: if (isManagerSession) "MANAGER" else "KASIE",
                                color = when {
                                    isSuperAdmin -> Color(0xFF3730A3)
                                    isMgr -> Color(0xFF92400E)
                                    else -> Emerald900
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                text = {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileDetailRow("Nama Lengkap", spvName)
                            ProfileDetailRow("Username", supervisorSession?.username ?: spvName)
                            ProfileDetailRow("User ID", supervisorSession?.userId ?: "1")
                            ProfileDetailRow("Jabatan / Role", supervisorSession?.roleTitle ?: if (isManagerSession) "Manager Operasional" else "Kasie Lapangan")
                            ProfileDetailRow("Wilayah Kerja", supervisorSession?.wilayah ?: if (isManagerSession) "Semua Wilayah" else "Geodesi 1")
                            ProfileDetailRow("Status Akun", "Aktif", isStatusSuccess = true)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showProfilePopup = false
                            showLogoutDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_popup_logout")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Keluar / Logout", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showProfilePopup = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tutup", color = Slate700)
                    }
                }
            )
        }

        if (showLogoutDialog) {
            val supervisorSession by viewModel.currentSupervisorSession.collectAsState()
            val activeSupervisorName by viewModel.activeSupervisor.collectAsState()
            val spvName = supervisorSession?.namaLengkap?.ifBlank { null } ?: supervisorSession?.name ?: activeSupervisorName

            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(ErrorRose50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = ErrorRose,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Konfirmasi Log Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Slate900
                    )
                },
                text = {
                    Text(
                        text = "Apakah Anda yakin ingin keluar dari sesi pengawas ($spvName)? Anda dapat masuk kembali dengan memilih nama atau memasukkan NIK/PIN pengawas.",
                        fontSize = 13.sp,
                        color = Slate600
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_confirm_logout")
                    ) {
                        Text(
                            text = "Ya, Log Out",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showLogoutDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Slate700
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate300),
                        modifier = Modifier.testTag("btn_cancel_logout")
                    ) {
                        Text(text = "Batal")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    // OVERLAY RINGAN: SINKRONISASI DATA OPERASIONAL & RIWAYAT (DATA HYDRATION)
    if (isBootstrapping) {
        Dialog(
            onDismissRequest = { /* Non-dismissible during initial hydration */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .testTag("bootstrap_loading_overlay")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Emerald50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Emerald600,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Menyinkronkan data operasional & riwayat...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mengunduh rencana kerja SPK, riwayat aktivitas terverifikasi, dan master data dari server admin...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate500,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Database Hydration • SQLite Room",
                                fontSize = 11.sp,
                                color = Slate700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getInitials(name: String): String {
    val clean = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        clean.isEmpty() -> "U"
        clean.size == 1 -> clean[0].take(2).uppercase()
        else -> (clean[0].take(1) + clean[1].take(1)).uppercase()
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    isStatusSuccess: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.5.sp, color = Slate500, fontWeight = FontWeight.Medium)
        if (isStatusSuccess) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Emerald100
            ) {
                Text(
                    text = "● $value",
                    color = Emerald900,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 11.5.sp,
                color = Slate900,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
