package com.example.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import com.example.ui.components.RkceBrandEmblem
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.ui.viewmodel.KendalaItemFormState
import com.example.ui.components.ActivitySearchableSelectorSheet
import com.example.ui.components.KendalaSearchableSelectorSheet
import com.example.ui.components.LokasiSearchableSelectorSheet
import com.example.ui.components.OperatorSearchableSelectorSheet
import com.example.ui.components.PhotoProofCard
import com.example.ui.components.SelectorFieldTrigger
import com.example.ui.components.UnitPlanSearchableSelectorSheet
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.viewmodel.HeavyTrackViewModel
import com.example.ui.viewmodel.isTaskOlder
import com.example.ui.viewmodel.isPriorTask
import com.example.ui.viewmodel.getShiftWeight
import com.example.ui.viewmodel.normalizeDateToIso
import com.example.ui.viewmodel.safeParseDouble
import android.widget.Toast

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FormReportScreen(
    viewModel: HeavyTrackViewModel,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val formSubTab by viewModel.formSubTab.collectAsState()
    val activeSupervisor by viewModel.activeSupervisor.collectAsState()

    val units by viewModel.units.collectAsState()
    val availableUnitsForRencana by viewModel.availableUnitsForRencana.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val operators by viewModel.operators.collectAsState()
    val lokasiList by viewModel.lokasiPilihan.collectAsState()
    val lokasiSearchQuery by viewModel.lokasiSearchQuery.collectAsState()
    val lokasiWilayahFilter by viewModel.lokasiWilayahFilter.collectAsState()
    val currentSupervisor by viewModel.currentSupervisorSession.collectAsState()
    val isOperatorSession by viewModel.isOperatorSession.collectAsState()
    val isKasieSession by viewModel.isKasieSession.collectAsState()
    val myReports by viewModel.reports.collectAsState()
    val kendalaList by viewModel.kendalaList.collectAsState()
    val activeRencanaList by viewModel.activeRencanaList.collectAsState()
    val allRencanaList by viewModel.allRencanaList.collectAsState()
    val allDraftsList by viewModel.allDrafts.collectAsState()

    val rencanaState by viewModel.rencanaFormState.collectAsState()
    val realisasiState by viewModel.realisasiFormState.collectAsState()
    val fleetProgress by viewModel.fleetShiftProgress.collectAsState()
    val isSocketConnected by viewModel.isSocketConnected.collectAsState()
    var reportPendingApproveOnHome by remember { mutableStateOf<com.example.data.local.entity.ActivityReportEntity?>(null) }

    // Cascading Filter Aktivitas sesuai Jenis Unit
    val selectedRencanaUnitType = remember(rencanaState.selectedUnit) {
        rencanaState.selectedUnit?.unitType ?: ""
    }
    val filteredActivitiesForRencana = remember(activities, selectedRencanaUnitType) {
        if (selectedRencanaUnitType.isBlank()) {
            activities
        } else {
            viewModel.filterActivitiesByUnitType(activities, selectedRencanaUnitType)
        }
    }

    val realisasiPlanUnitCode = realisasiState.selectedRencana?.kodeUnit
    val realisasiUnitType = remember(realisasiPlanUnitCode, units) {
        viewModel.getUnitTypeForUnitCode(realisasiPlanUnitCode)
    }
    val filteredActivitiesForRealisasi = remember(activities, realisasiUnitType) {
        if (realisasiUnitType.isBlank()) {
            activities
        } else {
            viewModel.filterActivitiesByUnitType(activities, realisasiUnitType)
        }
    }

    // 5. AUTO-FETCH DATA MASTER SAAT HALAMAN DIBUKA (ONRESUME / ONENTER)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.autoFetchMasterDataOnResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.autoFetchMasterDataOnResume()
    }

    // Selector Sheets State
    var isUnitSheetOpen by remember { mutableStateOf(false) }
    var isOperatorSheetOpen by remember { mutableStateOf(false) }
    var isRencanaActivitySheetOpen by remember { mutableStateOf(false) }
    var isLokasiSheetOpen by remember { mutableStateOf(false) }

    // Sub-Plan sheet routing: targetSubPlanId -> null for main form, or subPlan.id
    var activeSubPlanOperatorTarget by remember { mutableStateOf<String?>(null) }
    var activeSubPlanActivityTarget by remember { mutableStateOf<String?>(null) }
    var activeSubPlanLokasiTarget by remember { mutableStateOf<String?>(null) }

    // Realisasi Editable Searchable Sheets
    var isRealisasiActivitySheetOpen by remember { mutableStateOf(false) }
    var isRealisasiLokasiSheetOpen by remember { mutableStateOf(false) }
    var isKendalaSheetOpen by remember { mutableStateOf(false) }
    var activeKendalaIndex by remember { mutableStateOf<Int?>(null) }

    // Confirmation dialog for Over-HM (> max standard) & Extreme HM Warning (> 14 hours)
    var showOverHmConfirmDialog by remember { mutableStateOf(false) }
    var showExtremeHmWarningConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var backlogLockedAlertTask by remember { mutableStateOf<Pair<RencanaKerjaEntity, RencanaKerjaEntity>?>(null) }

    // State pemisahan alur: Tampilan Penugasan Berjalan vs Form Sub-Page Realisasi
    var isInputRealisasiOpen by remember { mutableStateOf(false) }
    var showNewPlanForm by remember { mutableStateOf(false) }

    LaunchedEffect(realisasiState.selectedRencana, activeRencanaList) {
        val target = realisasiState.selectedRencana
        if (target != null) {
            val olderTask = activeRencanaList.firstOrNull { other ->
                other.kodeUnit.equals(target.kodeUnit, ignoreCase = true) &&
                other.id != target.id &&
                isPriorTask(currentTask = target, otherTask = other)
            }
            if (olderTask != null) {
                backlogLockedAlertTask = Pair(target, olderTask)
                isInputRealisasiOpen = false
            } else {
                showNewPlanForm = false
                isInputRealisasiOpen = true
            }
        } else {
            isInputRealisasiOpen = false
        }
    }

    LaunchedEffect(rencanaState.selectedUnit, rencanaState.isSubmitting) {
        // Otomatis tutup formulir rencana baru setelah berhasil disimpan ke Room
        if (!rencanaState.isSubmitting && rencanaState.selectedUnit == null && showNewPlanForm) {
            showNewPlanForm = false
        }
    }

    var reportBeingCorrected by remember { mutableStateOf<com.example.data.local.entity.ActivityReportEntity?>(null) }

    androidx.activity.compose.BackHandler(enabled = isInputRealisasiOpen || showNewPlanForm) {
        if (isInputRealisasiOpen) {
            reportBeingCorrected = null
            isInputRealisasiOpen = false
            viewModel.clearSelectedRencanaForRealisasi()
        } else if (showNewPlanForm) {
            showNewPlanForm = false
        }
    }

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val keteranganBringIntoViewRequester = remember { BringIntoViewRequester() }

    // Image Picker for Realisasi Photo Proof
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.processAndCompressRealisasiPhoto(context, uri)
        }
    }

    // State tambahan untuk Enterprise RKCE Home UI (Omni-Search, ModalBottomSheet Filter, Koreksi Auto-Lock, APK & Logout)
    var homeSearchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Semua") } // "Semua", "Perlu Review"
    var selectedShiftFilter by remember { mutableStateOf("Semua Shift") } // "Semua Shift", "Shift Siang", "Shift Malam"
    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var showApkDistributionDialog by remember { mutableStateOf(false) }
    var showConfirmLogoutDialog by remember { mutableStateOf(false) }
    val customApkUrl by viewModel.customApkUrl.collectAsState()

    // Auto-lock & auto-dismiss saat Kasie selesai menyimpan Koreksi
    LaunchedEffect(realisasiState.selectedRencana, realisasiState.isSubmitting) {
        if (!realisasiState.isSubmitting && realisasiState.selectedRencana == null && reportBeingCorrected != null) {
            val corrected = reportBeingCorrected
            reportBeingCorrected = null
            if (corrected != null) {
                viewModel.approveRealisasi(corrected, context)
            }
        }
    }

    val rawDisplayName = remember(currentSupervisor, activeSupervisor) {
        currentSupervisor?.namaLengkap?.takeIf { it.isNotBlank() }
            ?: currentSupervisor?.name?.takeIf { it.isNotBlank() }
            ?: activeSupervisor.takeIf { it.isNotBlank() }
            ?: "User"
    }

    // ======================================================================
    // KOMPONEN DAFTAR KARTU GRID & PENGURUTAN TERKUNCI (TANGGAL ASC -> SHIFT SIANG/MALAM -> UNIT ASC)
    // ======================================================================
    val unapprovedReports = remember(myReports) {
        myReports.filter { rep ->
            !rep.isLocked &&
            !rep.approvalStatus.equals("Disetujui Kasie", ignoreCase = true) &&
            !rep.approvalStatus.equals("Approved", ignoreCase = true) &&
            rep.hmAkhir > 0.0 &&
            (rep.hmDailyRunning > 0.0 || rep.hmAkhir > rep.hmAwal || rep.workOutput > 0.0)
        }
    }

    val lockedReports = remember(myReports) {
        myReports.filter { rep ->
            rep.isLocked ||
            rep.approvalStatus.equals("Disetujui Kasie", ignoreCase = true) ||
            rep.approvalStatus.equals("Approved", ignoreCase = true)
        }
    }

    val allGridItems = remember(activeRencanaList, allRencanaList, unapprovedReports, lockedReports, isOperatorSession) {
        val list = mutableListOf<RkceGridCardItem>()

        if (isOperatorSession) {
            // Operator: HANYA menampilkan tugas yang belum memiliki data realisasi
            activeRencanaList.forEach { ren ->
                val alreadyRealized = (unapprovedReports + lockedReports).any { rep ->
                    (rep.idRencanaKerja > 0 && rep.idRencanaKerja == ren.id) ||
                    (rep.nomorSpk.isNotBlank() && rep.nomorSpk != "-" && rep.nomorSpk != "SPK Menyusul" && rep.nomorSpk.equals(ren.nomorSpk, ignoreCase = true)) ||
                    (rep.unitCode.equals(ren.kodeUnit, ignoreCase = true) &&
                     normalizeDateToIso(rep.reportDate) == normalizeDateToIso(ren.tanggalFormat) &&
                     rep.shift.equals(ren.shiftKerja, ignoreCase = true) &&
                     rep.activityName.equals(ren.namaAktivitas, ignoreCase = true))
                }
                if (!alreadyRealized && !ren.isRealized) {
                    list.add(
                        RkceGridCardItem(
                            key = "norm_${ren.id}",
                            rencana = ren,
                            reviewReport = null,
                            isReview = false
                        )
                    )
                }
            }
        } else {
            // Kasie: Gabungkan Kartu Normal (belum realisasi) + Kartu Perlu Review (sudah diisi operator, belum disetujui/terkunci)
            val matchedRencanaIds = mutableSetOf<Long>()
            val matchedSpks = mutableSetOf<String>()

            unapprovedReports.forEach { rep ->
                val matchedRencana = (allRencanaList + activeRencanaList).firstOrNull { ren ->
                    (rep.idRencanaKerja > 0 && ren.id == rep.idRencanaKerja) ||
                    (rep.nomorSpk.isNotBlank() && rep.nomorSpk != "-" && rep.nomorSpk != "SPK Menyusul" && ren.nomorSpk.equals(rep.nomorSpk, ignoreCase = true)) ||
                    (ren.kodeUnit.equals(rep.unitCode, ignoreCase = true) &&
                     normalizeDateToIso(ren.tanggalFormat) == normalizeDateToIso(rep.reportDate) &&
                     ren.shiftKerja.equals(rep.shift, ignoreCase = true))
                } ?: RencanaKerjaEntity(
                    id = if (rep.idRencanaKerja > 0) rep.idRencanaKerja else rep.id,
                    nomorSpk = rep.nomorSpk,
                    supervisorName = rep.supervisorName,
                    kasie = rep.namaKasie,
                    kodeUnit = rep.unitCode,
                    operator = rep.operatorName,
                    namaOperator = rep.operatorName,
                    operatorNik = rep.operatorNik ?: "",
                    kodeLokasi = rep.workLocation,
                    wilayah = rep.wilayah,
                    namaAktivitas = rep.activityName,
                    sapCode = rep.sapCode,
                    satuanAktivitas = rep.unitOfMeasure,
                    statusUnit = rep.unitStatus,
                    shiftKerja = rep.shift,
                    tanggalFormat = rep.reportDate
                )

                if (matchedRencana.id > 0) matchedRencanaIds.add(matchedRencana.id)
                if (matchedRencana.nomorSpk.isNotBlank()) matchedSpks.add(matchedRencana.nomorSpk.uppercase())

                list.add(
                    RkceGridCardItem(
                        key = "rev_${rep.id}_${matchedRencana.id}",
                        rencana = matchedRencana,
                        reviewReport = rep,
                        isReview = true
                    )
                )
            }

            activeRencanaList.forEach { ren ->
                val isAlreadyInReview = matchedRencanaIds.contains(ren.id) ||
                    (ren.nomorSpk.isNotBlank() && matchedSpks.contains(ren.nomorSpk.uppercase()))
                val isAlreadyLocked = lockedReports.any { rep ->
                    (rep.idRencanaKerja > 0 && rep.idRencanaKerja == ren.id) ||
                    (rep.nomorSpk.isNotBlank() && rep.nomorSpk != "-" && rep.nomorSpk != "SPK Menyusul" && rep.nomorSpk.equals(ren.nomorSpk, ignoreCase = true))
                }
                if (!isAlreadyInReview && !isAlreadyLocked && !ren.isRealized) {
                    list.add(
                        RkceGridCardItem(
                            key = "norm_${ren.id}",
                            rencana = ren,
                            reviewReport = null,
                            isReview = false
                        )
                    )
                }
            }
        }

        // Pengurutan Terkunci:
        // 1. Tanggal Pelaksanaan (ASC / tanggal terlama di atas)
        // 2. Shift Operasional (Shift SIANG terlebih dahulu, baru Shift MALAM)
        // 3. Kode Unit (ASC)
        list.sortedWith(
            compareBy<RkceGridCardItem> { normalizeDateToIso(it.rencana.tanggalFormat) }
                .thenBy { if (it.rencana.shiftKerja.equals("SIANG", ignoreCase = true)) 0 else 1 }
                .thenBy { it.rencana.kodeUnit.trim().uppercase() }
                .thenBy { it.rencana.subIndex }
                .thenBy { it.rencana.id }
        )
    }

    val filteredGridItems = remember(allGridItems, homeSearchQuery, selectedStatusFilter, selectedShiftFilter, isOperatorSession) {
        allGridItems.filter { item ->
            val q = homeSearchQuery.trim().lowercase()
            val matchesQuery = if (q.isEmpty()) {
                true
            } else {
                item.rencana.kodeUnit.lowercase().contains(q) ||
                item.rencana.operator.lowercase().contains(q) ||
                item.rencana.namaOperator.lowercase().contains(q) ||
                item.rencana.kodeLokasi.lowercase().contains(q) ||
                item.rencana.namaAktivitas.lowercase().contains(q) ||
                (item.reviewReport?.operatorName?.lowercase()?.contains(q) == true) ||
                (item.reviewReport?.workLocation?.lowercase()?.contains(q) == true)
            }

            val matchesStatus = if (isOperatorSession) {
                !item.isReview
            } else {
                when (selectedStatusFilter) {
                    "Perlu Review" -> item.isReview
                    else -> true
                }
            }

            val shiftStr = (item.reviewReport?.shift ?: item.rencana.shiftKerja).uppercase()
            val matchesShift = when (selectedShiftFilter) {
                "Shift Siang" -> shiftStr == "SIANG"
                "Shift Malam" -> shiftStr == "MALAM"
                else -> true
            }

            matchesQuery && matchesStatus && matchesShift
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("form_report_screen")
    ) {
        // ======================================================================
        // 1. TOP APP BAR SAPPHIRE BLUE (#002677) & MODERN ICON BUTTONS
        // ======================================================================
        Surface(
            color = Color(0xFF002677),
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("rkce_enterprise_topbar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Kiri (Emblem Vektor Native + Identitas Pengguna)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    RkceBrandEmblem(size = 38.dp)
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            text = if (isOperatorSession) {
                                "$rawDisplayName • Operator PG1"
                            } else {
                                "$rawDisplayName • RC"
                            },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "RC PG1 • Divisi Lapangan",
                            color = Color(0xFF93C5FD),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Kontrol Aksi (Icon Buttons)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Sinkronisasi
                    IconButton(
                        onClick = { viewModel.forceSyncWithServer() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF163E9B), CircleShape)
                            .testTag("btn_topbar_sinkron")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sinkron",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 2. Unduh APK
                    IconButton(
                        onClick = { showApkDistributionDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF163E9B), CircleShape)
                            .testTag("btn_topbar_unduh_apk")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Unduh",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 3. Logout
                    @Suppress("DEPRECATION")
                    IconButton(
                        onClick = { showConfirmLogoutDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF163E9B), CircleShape)
                            .testTag("btn_topbar_keluar")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Keluar",
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ======================================================================
        // TAMPILAN UTAMA TAB FORMULIR (SAAT SUB-FORM TIDAK SEDANG DIBUKA)
        // ======================================================================
        if (!isInputRealisasiOpen && !showNewPlanForm) {
            // 2. Omni-Search & Tombol Filter (Satu Baris Rapi, padding horizontal 12dp, vertical 6dp, spacedBy 8dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kiri: Kolom pencarian menggunakan Surface setinggi 42dp dengan Modifier.weight(1f), BasicTextField
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.5.dp
                ) {
                    BasicTextField(
                        value = homeSearchQuery,
                        onValueChange = { homeSearchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp)
                            .testTag("omni_search_basic_field"),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Cari",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (homeSearchQuery.isEmpty()) {
                                        Text(
                                            text = "Cari unit, operator, blok...",
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    innerTextField()
                                }
                                if (homeSearchQuery.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Reset Pencarian",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { homeSearchQuery = "" }
                                    )
                                }
                            }
                        }
                    )
                }

                // Kanan: Tombol ikon Filter menggunakan Surface kotak tepat 42dp x 42dp, rounded 10dp, border 1dp hijau #009663
                val hasActiveFilter = (!isOperatorSession && selectedStatusFilter != "Semua") || selectedShiftFilter != "Semua Shift"
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isFilterSheetOpen = true }
                        .testTag("btn_filter_42dp"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (hasActiveFilter) Color(0xFFE8F5E9) else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF009663)),
                    shadowElevation = 1.5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter Tugas",
                            tint = Color(0xFF009663),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (!isOperatorSession) {
                // 3A. KHUSUS KASIE: Baris Progres & Tombol "+ Buat Rencana" hijau solid (#009663)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 1.5.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "PROGRES HARI INI: ${fleetProgress.realizedUnitsCount}/${fleetProgress.totalUnitsCount} TERENCANA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1
                                    )
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "✓ Aktif",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                LinearProgressIndicator(
                                    progress = { fleetProgress.progressPercentage.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF009663),
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }

                            Button(
                                onClick = { showNewPlanForm = true },
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("btn_buat_rencana_utama")
                                    .testTag("btn_tambah_rencana_baru"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF009663),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "+ Buat Rencana",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            } else {
                // 3B. KHUSUS OPERATOR: Banner Header Ringkas & Kartu Skor KPI 2 Kolom Simetris
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 1.5.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "TUGAS BELUM REALISASI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                                Text(
                                    text = "Silakan input HM & Solar setelah jam kerja selesai",
                                    fontSize = 9.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Text(
                                    text = "● ${filteredGridItems.size} Unit Aktif",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Grid 2 Kolom Kartu Tugas (LazyVerticalGrid, GridCells.Fixed(2), contentPadding 12dp, spacedBy 8dp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("rkce_task_grid"),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredGridItems.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_pending_assignments_card"),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 1.5.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (homeSearchQuery.isNotBlank() || selectedStatusFilter != "Semua" || selectedShiftFilter != "Semua Shift")
                                        "Tidak Ada Kartu Sesuai Filter"
                                    else if (isOperatorSession)
                                        "Belum Ada Tugas Belum Realisasi"
                                    else
                                        "Semua Tugas Telah Selesai / Belum Ada Rencana",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (isOperatorSession)
                                        "Tugas akan otomatis muncul saat Kasie menerbitkan rencana kerja baru."
                                    else
                                        "Ketuk '+ Buat Rencana' untuk menambahkan penugasan unit atau 'Sinkron' untuk memperbarui data.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredGridItems, key = { it.key }) { cardItem ->
                        val ren = cardItem.rencana
                        val rep = cardItem.reviewReport
                        if (!isOperatorSession && cardItem.isReview && rep != null) {
                            RkceReviewGridCard(
                                rencana = ren,
                                report = rep,
                                onKoreksiClick = {
                                    reportBeingCorrected = rep
                                    viewModel.selectRencanaForKoreksi(ren, rep)
                                    isInputRealisasiOpen = true
                                },
                                onSetujuiClick = {
                                    viewModel.approveRealisasi(rep, context)
                                }
                            )
                        } else {
                            RkceNormalGridCard(
                                rencana = ren,
                                onInputRealisasiClick = {
                                    val olderTask = activeRencanaList.firstOrNull { other ->
                                        other.kodeUnit.equals(ren.kodeUnit, ignoreCase = true) &&
                                        other.id != ren.id &&
                                        isPriorTask(currentTask = ren, otherTask = other)
                                    }
                                    if (olderTask != null) {
                                        backlogLockedAlertTask = Pair(ren, olderTask)
                                    } else {
                                        reportBeingCorrected = null
                                        viewModel.selectRencanaForRealisasi(ren)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // SUB-PAGES: Buat Rencana Kerja & Input Realisasi Aktual (TIDAK DIUBAH)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

        // ==========================================
        // SUB-PAGE: FORMULIR BUAT RENCANA KERJA BARU
        // ==========================================
        if (showNewPlanForm) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showNewPlanForm = false },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Slate700
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate300),
                        modifier = Modifier.testTag("btn_back_from_rencana")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            modifier = Modifier.size(16.dp),
                            tint = Slate700
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kembali",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_rencana_kerja"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Header Persiapan Rencana Kerja Bersih (Sesuai Permintaan)
                        Text(
                            text = "Seluruh unit wajib dibuatkan rencana kerja",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Banner Info Kasus C: Unit Sudah Memiliki Rencana Hari Ini (Rencana Susulan)
                        if (rencanaState.isSusulan && rencanaState.selectedUnit != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFECFDF5), // Hijau lembut
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Emerald600,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Rencana Kerja Tambahan / Susulan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Emerald900
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Unit ini sudah memiliki rencana kerja hari ini. Anda sedang membuat Rencana Kerja Tambahan/Susulan.",
                                            fontSize = 12.sp,
                                            color = Emerald700,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }



                        // 2. Unit Alat Berat Searchable Selector (Otomatis Tanggal, Deteksi Backlog & Tambah Susulan)
                        SelectorFieldTrigger(
                            label = "Unit",
                            selectedValue = rencanaState.selectedUnit?.let { u ->
                                when {
                                    rencanaState.isBacklog -> "${u.unitCode} - ${u.brandModel} [Backlog Kemarin]"
                                    rencanaState.isSusulan -> "${u.unitCode} - ${u.brandModel} [Rencana Susulan]"
                                    else -> "${u.unitCode} - ${u.brandModel}"
                                }
                            },
                            placeholder = if (availableUnitsForRencana.isEmpty())
                                "Tidak ada unit terdaftar"
                            else
                                "Pilih Unit Alat Berat binaan pengawas...",
                            leadingIcon = Icons.Default.PrecisionManufacturing,
                            badgeText = rencanaState.selectedUnit?.let {
                                when {
                                    rencanaState.isBacklog -> "BACKLOG H-1"
                                    rencanaState.isSusulan -> "SUSULAN"
                                    else -> "HARI INI"
                                }
                            },
                            testTag = "selector_rencana_unit",
                            onClick = { isUnitSheetOpen = true }
                        )

                        // Info Tanggal Otomatis Target
                        if (rencanaState.selectedUnit != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    rencanaState.isBacklog -> Color(0xFFFEF2F2)
                                    rencanaState.isSusulan -> Color(0xFFECFDF5)
                                    else -> Color(0xFFF0FDF4)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when {
                                        rencanaState.isBacklog -> Color(0xFFFCA5A5)
                                        rencanaState.isSusulan -> Color(0xFFA7F3D0)
                                        else -> Color(0xFF86EFAC)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when {
                                            rencanaState.isBacklog -> Icons.Default.Warning
                                            rencanaState.isSusulan -> Icons.Default.Add
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            rencanaState.isBacklog -> Color(0xFFDC2626)
                                            rencanaState.isSusulan -> Emerald700
                                            else -> Emerald700
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = when {
                                                rencanaState.isBacklog -> "TARGET: TANGGAL KEMARIN (${rencanaState.targetDate}) - BACKLOG"
                                                rencanaState.isSusulan -> "TARGET: TANGGAL HARI INI (${rencanaState.targetDate}) - RENCANA SUSULAN"
                                                else -> "TARGET: TANGGAL HARI INI (${rencanaState.targetDate})"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = when {
                                                rencanaState.isBacklog -> Color(0xFF991B1B)
                                                rencanaState.isSusulan -> Emerald900
                                                else -> Emerald900
                                            }
                                        )
                                        if (rencanaState.isBacklog) {
                                            Text(
                                                text = "Tanggal Rencana Kerja Terkunci: ${rencanaState.targetDate} (Selesaikan Backlog Terlebih Dahulu)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB91C1C)
                                            )
                                        } else if (rencanaState.isSusulan) {
                                            Text(
                                                text = "Unit sudah ada rencana hari ini. Membuat rencana susulan tambahan.",
                                                fontSize = 10.sp,
                                                color = Emerald700
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Operator Pelaksana
                        SelectorFieldTrigger(
                            label = "Operator",
                            selectedValue = rencanaState.selectedOperator?.name,
                            placeholder = "Pilih Nama Operator Pelaksana...",
                            leadingIcon = Icons.Default.Person,
                            testTag = "selector_rencana_operator",
                            onClick = {
                                activeSubPlanOperatorTarget = null
                                isOperatorSheetOpen = true
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Aktivitas (Cascading sesuai Jenis Unit)
                        SelectorFieldTrigger(
                            label = "Aktivitas",
                            selectedValue = rencanaState.selectedActivity?.let { "${it.activityName} (${it.sapCode})" },
                            placeholder = if (rencanaState.selectedUnit == null)
                                "Pilih Unit terlebih dahulu..."
                            else
                                "Pilih Jenis Pekerjaan (${rencanaState.selectedUnit?.unitType ?: ""})...",
                            leadingIcon = Icons.Default.Assignment,
                            badgeText = rencanaState.selectedActivity?.unitOfMeasure ?: selectedRencanaUnitType.takeIf { it.isNotBlank() },
                            enabled = rencanaState.selectedUnit != null,
                            testTag = "selector_rencana_aktivitas",
                            onClick = {
                                activeSubPlanActivityTarget = null
                                isRencanaActivitySheetOpen = true
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Lokasi
                        SelectorFieldTrigger(
                            label = "Lokasi",
                            selectedValue = rencanaState.selectedLokasi?.kodeLokasi,
                            placeholder = "Pilih Kode Lokasi (misal: 001A)...",
                            leadingIcon = Icons.Default.LocationOn,
                            testTag = "selector_rencana_lokasi",
                            onClick = {
                                activeSubPlanLokasiTarget = null
                                isLokasiSheetOpen = true
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Shift Kerja Toggle Pill [SIANG] | [MALAM]
                        Text(
                            text = "Shift Kerja",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                "SIANG" to Icons.Default.WbSunny,
                                "MALAM" to Icons.Default.NightlightRound
                            ).forEach { (shift, icon) ->
                                val isSelected = rencanaState.shiftKerja == shift
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Emerald600 else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Emerald600 else Slate200),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setRencanaShift(shift) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Slate700,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SHIFT $shift",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else Slate700
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 7. Keterangan Rencana / Catatan Awal
                        Text(
                            text = "7. Keterangan Rencana Kerja",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = rencanaState.keterangan,
                            onValueChange = { viewModel.setRencanaKeterangan(it) },
                            placeholder = { Text("Instruksi kerja pengawas, target galian, rute hauling...", color = Slate400, fontSize = 13.sp) },
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_rencana_keterangan")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // DUKUNGAN MULTI-RENCANA (SUB-FORM)
                        // 1 Unit & 1 Tanggal bisa > 1 Rencana Kerja
                        // ==========================================
                        if (rencanaState.subPlans.isNotEmpty()) {
                            Text(
                                text = "Rencana Kerja Tambahan (${rencanaState.subPlans.size} Sub-Rencana)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            rencanaState.subPlans.forEachIndexed { index, sub ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("sub_rencana_card_${index + 1}")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Emerald600
                                                ) {
                                                    Text(
                                                        text = "#${index + 2}",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Rencana Tambahan #${index + 2}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Slate900
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.removeSubRencana(sub.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Hapus Sub Rencana",
                                                    tint = Color(0xFFDC2626)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Sub 1: Operator
                                        SelectorFieldTrigger(
                                            label = "Operator Pelaksana #${index + 2}",
                                            selectedValue = sub.selectedOperator?.name,
                                            placeholder = "Pilih Operator Pelaksana...",
                                            leadingIcon = Icons.Default.Person,
                                            testTag = "sub_operator_${index + 1}",
                                            onClick = {
                                                activeSubPlanOperatorTarget = sub.id
                                                isOperatorSheetOpen = true
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Sub 2: Aktivitas (Cascading sesuai Jenis Unit)
                                        SelectorFieldTrigger(
                                            label = "Aktivitas #${index + 2}",
                                            selectedValue = sub.selectedActivity?.let { "${it.activityName} (${it.sapCode})" },
                                            placeholder = if (rencanaState.selectedUnit == null)
                                                "Pilih Unit terlebih dahulu..."
                                            else
                                                "Pilih Aktivitas (${rencanaState.selectedUnit?.unitType ?: ""})...",
                                            leadingIcon = Icons.Default.Assignment,
                                            badgeText = sub.selectedActivity?.unitOfMeasure ?: selectedRencanaUnitType.takeIf { it.isNotBlank() },
                                            enabled = rencanaState.selectedUnit != null,
                                            testTag = "sub_aktivitas_${index + 1}",
                                            onClick = {
                                                activeSubPlanActivityTarget = sub.id
                                                isRencanaActivitySheetOpen = true
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Sub 3: Lokasi
                                        SelectorFieldTrigger(
                                            label = "Lokasi #${index + 2}",
                                            selectedValue = sub.selectedLokasi?.kodeLokasi,
                                            placeholder = "Pilih Kode Lokasi (misal: 001A)...",
                                            leadingIcon = Icons.Default.LocationOn,
                                            testTag = "sub_lokasi_${index + 1}",
                                            onClick = {
                                                activeSubPlanLokasiTarget = sub.id
                                                isLokasiSheetOpen = true
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Sub 4: Shift Kerja
                                        Text(
                                            text = "Shift Kerja #${index + 2}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate700
                                            ),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("SIANG", "MALAM").forEach { sft ->
                                                val isSel = sub.shiftKerja == sft
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSel) Emerald600 else Color.White,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Emerald600 else Slate200),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { viewModel.updateSubRencanaShift(sub.id, sft) }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                                        Text(
                                                            text = "SHIFT $sft",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSel) Color.White else Slate700
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Sub 5: Keterangan
                                        OutlinedTextField(
                                            value = sub.keterangan,
                                            onValueChange = { viewModel.updateSubRencanaKeterangan(sub.id, it) },
                                            placeholder = { Text("Keterangan sub-rencana #${index + 2}...", fontSize = 12.sp, color = Slate400) },
                                            minLines = 1,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald600,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("sub_keterangan_${index + 1}")
                                        )
                                    }
                                }
                            }
                        }

                        // Tombol "+ Tambah Rencana Kerja" (Sub-Form Multi-Rencana)
                        OutlinedButton(
                            onClick = { viewModel.addSubRencana() },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Emerald600),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald700),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_tambah_sub_rencana")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tambah Rencana Kerja",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Tombol Aksi: Kirim Rencana Kerja
                        Button(
                            onClick = { viewModel.submitRencanaKerja() },
                            enabled = !rencanaState.isSubmitting && rencanaState.selectedUnit != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_kirim_rencana_kerja")
                        ) {
                            if (rencanaState.isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Menyimpan Rencana Kerja...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                val totalToSubmit = 1 + rencanaState.subPlans.size
                                Text(
                                    text = if (totalToSubmit > 1)
                                        "Kirim $totalToSubmit Rencana Kerja Sekaligus"
                                    else
                                        "Kirim Rencana Kerja (Menunggu SPK)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SUB-PAGE: FORMULIR REALISASI KERJA AKTUAL
        // ==========================================
        if (isInputRealisasiOpen && realisasiState.selectedRencana != null) {
            item {
                val selRen = realisasiState.selectedRencana
                val isSpkIssued = realisasiState.isSpkTerbit
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth().testTag("header_input_realisasi_subpage")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    reportBeingCorrected = null
                                    isInputRealisasiOpen = false
                                    viewModel.clearSelectedRencanaForRealisasi()
                                },
                                modifier = Modifier.size(36.dp).testTag("btn_back_to_pending_list")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali ke Penugasan Berjalan",
                                    tint = Slate700
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Realisasi Unit: ${selRen?.kodeUnit ?: '-'}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Slate900
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isSpkIssued) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = if (isSpkIssued) "SPK: ${realisasiState.nomorSpk}" else "SPK Menyusul",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSpkIssued) Color(0xFF166534) else Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Opr: ${selRen?.operator ?: '-'} • Shift ${selRen?.shiftKerja ?: '-'} • Lok: ${selRen?.kodeLokasi ?: '-'}",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                reportBeingCorrected = null
                                isInputRealisasiOpen = false
                                viewModel.clearSelectedRencanaForRealisasi()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
                        ) {
                            Text("Tutup", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_realisasi_kerja"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Banner Informasi Tanggal Terkunci & Target Realisasi
                        val targetRen = realisasiState.selectedRencana
                        if (targetRen != null) {
                            val isBacklog = isBacklogDate(targetRen.tanggalFormat)
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                color = if (isBacklog) Color(0xFFFFFBEB) else Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isBacklog) Color(0xFFFDE68A) else Color(0xFFBBF7D0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isBacklog) Icons.Default.WarningAmber else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isBacklog) Color(0xFFD97706) else Color(0xFF16A34A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isBacklog) "Target Laporan Kerja Lapangan (BACKLOG):" else "Target Laporan Kerja Lapangan:",
                                            fontSize = 11.sp,
                                            color = if (isBacklog) Color(0xFFB45309) else Color(0xFF15803D),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${formatTanggalIndonesia(targetRen.tanggalFormat)} (Shift ${(targetRen.shiftKerja.ifBlank { "SIANG" }).uppercase()})",
                                            fontSize = 13.sp,
                                            color = if (isBacklog) Color(0xFF92400E) else Color(0xFF166534),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Ringkasan Kartu Rencana yang Sedang Direalisasikan
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = realisasiState.selectedRencana?.kodeUnit ?: "-",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Slate900
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (realisasiState.selectedRencana?.statusUnit == "OPERASI") Emerald100 else Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                text = realisasiState.selectedRencana?.statusUnit ?: "OPERASI",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (realisasiState.selectedRencana?.statusUnit == "OPERASI") Emerald900 else Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    androidx.compose.material3.OutlinedButton(
                                        onClick = { showResetConfirmDialog = true },
                                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFFDC2626)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.testTag("btn_reset_formulir")
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reset Input", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Akt: ${realisasiState.selectedRencana?.namaAktivitas?.ifBlank { realisasiState.selectedRencana?.sapCode } ?: '-'}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate800
                                )
                                Text(
                                    text = "Kasie: ${realisasiState.selectedRencana?.supervisorName?.ifBlank { currentSupervisor?.kasieAtasan ?: "-" } ?: "-"} • Opr: ${realisasiState.selectedRencana?.operator ?: "-"} • Shift ${realisasiState.selectedRencana?.shiftKerja ?: "-"} • Lok: ${realisasiState.selectedRencana?.kodeLokasi ?: "-"}",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                                if (isOperatorSession) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFF1D4ED8),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Data Kasie, Unit, Aktivitas & Lokasi terkunci otomatis sesuai SPK penugasan",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1E40AF)
                                            )
                                        }
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Emerald600,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Draft tersimpan otomatis",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Emerald800,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ==============================================================
                        // 1. FORM INPUT NOMOR SPK (Surat Perintah Kerja)
                        // Menerima penerbitan dari Web Admin secara real-time untuk masing-masing rencana kerja
                        // Serta mendukung input / koreksi manual oleh pengawas di lapangan.
                        // ==============================================================
                        val isSpkTerbit = realisasiState.isSpkTerbit
                        val currentUnitCode = realisasiState.selectedRencana?.kodeUnit ?: "-"

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSpkTerbit) Emerald500.copy(alpha = 0.5f) else Slate200
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("spk_form_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // 1. Header: Title, Unit Context, and Compact Status Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    if (isSpkTerbit) Emerald50 else Color(0xFFF1F5F9),
                                                    RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isSpkTerbit) Icons.Default.CheckCircle else Icons.Default.Assignment,
                                                contentDescription = null,
                                                tint = if (isSpkTerbit) Emerald700 else Slate600,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "1. Nomor SPK",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate800
                                            )
                                            Text(
                                                text = if (realisasiState.selectedRencana != null)
                                                    "Unit: ${realisasiState.selectedRencana?.kodeUnit}"
                                                else
                                                    "Pilih rencana unit di atas",
                                                fontSize = 10.sp,
                                                color = Slate500
                                            )
                                        }
                                    }

                                    // Compact Status Badge
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSpkTerbit) Emerald100 else Color(0xFFFEF3C7)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(
                                                        if (isSpkTerbit) Emerald600 else Color(0xFFD97706),
                                                        CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isSpkTerbit) "SPK Terbit" else "Menunggu Terbit",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSpkTerbit) Emerald900 else Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2. Clean Compact TextField Input
                                OutlinedTextField(
                                    value = realisasiState.nomorSpk,
                                    onValueChange = { newSpk ->
                                        if (!isOperatorSession) {
                                            viewModel.setRealisasiNomorSpk(newSpk)
                                        }
                                    },
                                    readOnly = isOperatorSession,
                                    enabled = !isOperatorSession,
                                    placeholder = {
                                        Text(
                                            text = "Nomor SPK dari Web Admin / Input manual...",
                                            fontSize = 13.sp,
                                            color = Slate400
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isOperatorSession) Icons.Default.Lock else Icons.Default.Assignment,
                                            contentDescription = null,
                                            tint = if (isSpkTerbit) Emerald600 else Slate400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (realisasiState.nomorSpk.isNotBlank() && !isOperatorSession) {
                                            IconButton(
                                                onClick = { viewModel.setRealisasiNomorSpk("") },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus input SPK",
                                                    tint = Slate400,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isSpkTerbit) Emerald600 else Slate400,
                                        unfocusedBorderColor = if (isSpkTerbit) Emerald500.copy(alpha = 0.5f) else Slate200,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        disabledContainerColor = Color(0xFFF1F5F9),
                                        disabledTextColor = Slate800,
                                        disabledBorderColor = Slate200
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("spk_input_field")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 3. Compact Footer: Live Status & Action Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status info
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (isSpkTerbit) Emerald500 else if (isSocketConnected) Color(0xFF3B82F6) else Slate400,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (isSpkTerbit)
                                                "Tersinkron otomatis dari Admin"
                                            else if (isSocketConnected)
                                                "Live Web Admin aktif"
                                            else
                                                "Ketik manual / tarik SPK",
                                            fontSize = 10.sp,
                                            color = if (isSpkTerbit) Emerald700 else Slate500,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Action Chips
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Tombol Tarik SPK
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSpkTerbit) Color(0xFFF1F5F9) else Color(0xFFEFF6FF),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSpkTerbit) Slate300 else Color(0xFF93C5FD)
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { viewModel.fetchLatestSpkForCurrentRencana() }
                                                .testTag("btn_pull_spk_admin")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = if (isSpkTerbit) Slate700 else Color(0xFF1D4ED8),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Tarik SPK",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isSpkTerbit) Slate700 else Color(0xFF1D4ED8)
                                                )
                                            }
                                        }

                                        // Opsi SPK Menyusul (jika belum terbit)
                                        if (!isSpkTerbit) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFFFFBEB),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { viewModel.setRealisasiNomorSpk("SPK Menyusul") }
                                                    .testTag("btn_spk_menyusul")
                                            ) {
                                                Text(
                                                    text = "SPK Menyusul",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFB45309),
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Editable Dropdown Aktivitas (Terkunci otomatis jika role == operator)
                        SelectorFieldTrigger(
                            label = if (isOperatorSession) "2. Aktivitas (Terkunci Sesuai SPK)" else "2. Aktivitas",
                            selectedValue = realisasiState.selectedActivity?.let { "${it.activityName} (${it.sapCode})" }
                                ?: realisasiState.selectedRencana?.namaAktivitas,
                            placeholder = if (realisasiState.selectedRencana == null)
                                "Pilih Rencana Unit terlebih dahulu..."
                            else
                                "Pilih Aktivitas Aktual Lapangan...",
                            leadingIcon = if (isOperatorSession) Icons.Default.Lock else Icons.Default.Assignment,
                            enabled = realisasiState.selectedRencana != null && !isOperatorSession,
                            testTag = "selector_realisasi_aktivitas_editable",
                            onClick = { if (!isOperatorSession) isRealisasiActivitySheetOpen = true }
                        )

                        // Status Unit Otomatis (Read-Only Badge Ringkas)
                        val currentUnitStatus = realisasiState.selectedActivity?.kategoriStatus?.ifBlank { "OPERASI" }
                            ?: realisasiState.selectedRencana?.statusUnit?.ifBlank { "OPERASI" }
                            ?: "OPERASI"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (currentUnitStatus) {
                                "OPERASI" -> Emerald50
                                "STANDBY" -> Color(0xFFFFFBEB)
                                else -> Color(0xFFFFF1F2)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (currentUnitStatus) {
                                    "OPERASI" -> Emerald200
                                    "STANDBY" -> Color(0xFFFDE68A)
                                    else -> Color(0xFFFECDD3)
                                }
                            ),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .testTag("badge_realisasi_status_otomatis")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            when (currentUnitStatus) {
                                                "OPERASI" -> Emerald600
                                                "STANDBY" -> WarningAmber
                                                else -> ErrorRose
                                            },
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Status: $currentUnitStatus",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = when (currentUnitStatus) {
                                        "OPERASI" -> Emerald800
                                        "STANDBY" -> Color(0xFF92400E)
                                        else -> Color(0xFF9F1239)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Editable Dropdown Lokasi (Terkunci otomatis jika role == operator)
                        SelectorFieldTrigger(
                            label = if (isOperatorSession) "Lokasi (Terkunci Sesuai SPK)" else "Lokasi",
                            selectedValue = realisasiState.selectedLokasi?.kodeLokasi
                                ?: realisasiState.selectedRencana?.kodeLokasi,
                            placeholder = if (realisasiState.selectedRencana == null)
                                "Pilih Rencana Unit terlebih dahulu..."
                            else
                                "Pilih Kode Lokasi (misal: 001A)...",
                            leadingIcon = if (isOperatorSession) Icons.Default.Lock else Icons.Default.LocationOn,
                            enabled = realisasiState.selectedRencana != null && !isOperatorSession,
                            testTag = "selector_realisasi_lokasi_editable",
                            onClick = { if (!isOperatorSession) isRealisasiLokasiSheetOpen = true }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Durasi Jam Kerja
                        Text(
                            text = "3. Durasi Jam Kerja",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = String.format(Locale.US, "%.1f", realisasiState.durasiJamKerja),
                            onValueChange = { input ->
                                val parsed = input.replace(',', '.').toDoubleOrNull()
                                if (parsed != null) viewModel.setRealisasiDurasiJam(parsed)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Emerald600)
                            },
                            trailingIcon = { Text("Jam", color = Slate500, fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_durasi_jam")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Preset Quick Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(4.0, 7.0, 8.0, 10.0, 12.0).forEach { hours ->
                                val isSelected = realisasiState.durasiJamKerja == hours
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Emerald600 else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.setRealisasiDurasiJam(hours) }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${hours.toInt()}j",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Slate700
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // 4. BLOK KALKULASI HOUR METER (HM) & VALIDASI STANDAR
                        // ==========================================
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header: Title & Subtitle (Standar HM ditampilkan pada bagian Total HM Berjalan)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Emerald50, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = Emerald600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "4. Hour Meter (HM) Mesin",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "Validasi operasi & status kesinambungan unit",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // KESINAMBUNGAN HM PADA MULTI-RENCANA DALAM 1 UNIT (SEQUENTIAL HM)
                                if (realisasiState.isSequentialHmFromPreviousPlan) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Emerald50,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("badge_sequential_hm")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TaskAlt,
                                                contentDescription = null,
                                                tint = Emerald700,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "HM Berantai Otomatis: HM Awal ${String.format(Locale.US, "%.1f", realisasiState.hmAwal)} tersambung langsung dari HM Akhir rencana sebelumnya.",
                                                fontSize = 11.sp,
                                                color = Emerald900,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // BARIS STATUS & TOMBOL KOREKSI HM
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (realisasiState.isHmAwalCorrected) Color(0xFFFFFBEB) else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (realisasiState.isHmAwalCorrected) Color(0xFFFDE68A) else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
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
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (realisasiState.isHmAwalCorrected) Icons.Default.EditNote else Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = if (realisasiState.isHmAwalCorrected) Color(0xFFD97706) else Slate500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (realisasiState.isHmAwalCorrected) "Mode Koreksi HM Awal Aktif" else "HM Awal Terkunci Otomatis",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (realisasiState.isHmAwalCorrected) Color(0xFF92400E) else Slate600
                                            )
                                        }

                                        // Tombol Koreksi HM
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (realisasiState.isHmAwalCorrected) Color(0xFFFEE2E2) else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (realisasiState.isHmAwalCorrected) Color(0xFFFCA5A5) else Emerald600
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { viewModel.toggleKoreksiHmAwal() }
                                                .testTag("btn_koreksi_hm_awal")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (realisasiState.isHmAwalCorrected) Icons.Default.Close else Icons.Default.Edit,
                                                    contentDescription = "Koreksi HM Awal",
                                                    tint = if (realisasiState.isHmAwalCorrected) Color(0xFFDC2626) else Emerald700,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (realisasiState.isHmAwalCorrected) "Batal Koreksi" else "Koreksi HM Awal",
                                                    fontSize = 11.sp,
                                                    color = if (realisasiState.isHmAwalCorrected) Color(0xFFDC2626) else Emerald800,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // DUAL ODOMETER INPUTS: HM AWAL ➔ HM AKHIR
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // HM Awal
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = "HM AWAL",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (realisasiState.isHmAwalCorrected || realisasiState.isEditKoreksiMode) Color(0xFFB45309) else Slate700
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            if (!realisasiState.isHmAwalCorrected && !realisasiState.isEditKoreksiMode) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Terkunci",
                                                    tint = Slate400,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }

                                        if (!realisasiState.isHmAwalCorrected && !realisasiState.isEditKoreksiMode) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF1F5F9),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                modifier = Modifier.fillMaxWidth().height(52.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = String.format(Locale.US, "%.1f", realisasiState.hmAwal),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = Slate800
                                                    )
                                                    Text(
                                                        text = "HM",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Slate500
                                                    )
                                                }
                                            }
                                        } else {
                                            OutlinedTextField(
                                                value = realisasiState.hmAwalInput,
                                                onValueChange = { viewModel.setRealisasiHmAwalManual(it) },
                                                placeholder = { Text("HM Baru", fontSize = 12.sp, color = Slate400) },
                                                trailingIcon = {
                                                    Text("HM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.padding(end = 8.dp))
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFFD97706),
                                                    unfocusedBorderColor = Color(0xFFFDE68A),
                                                    focusedContainerColor = Color(0xFFFFFBEB),
                                                    unfocusedContainerColor = Color(0xFFFFFBEB)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("input_hm_awal_manual")
                                            )
                                        }
                                    }

                                    // Flow Arrow Indicator
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 18.dp)
                                            .size(24.dp)
                                            .background(Color(0xFFE2E8F0), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Slate600,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    // HM Akhir
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = "HM AKHIR",
                                                fontSize = 11.sp,
                                                color = Slate900,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "*Wajib",
                                                fontSize = 10.sp,
                                                color = Emerald700,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedTextField(
                                            value = realisasiState.hmAkhirInput,
                                            onValueChange = { viewModel.setRealisasiHmAkhir(it) },
                                            placeholder = { Text("Contoh: 4132.5", fontSize = 12.sp, color = Slate400) },
                                            trailingIcon = {
                                                Text("HM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500, modifier = Modifier.padding(end = 8.dp))
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = !realisasiState.isHmValid && realisasiState.hmAkhirInput.isNotBlank(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald600,
                                                unfocusedBorderColor = Slate300,
                                                errorBorderColor = ErrorRose,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("input_hm_akhir")
                                        )
                                    }
                                }

                                // Quick Add Hours Buttons (+4j, +7j, +8j, +10j)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Cepat:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate500
                                    )
                                    listOf(4.0, 7.0, 8.0, 10.0).forEach { qh ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Emerald50,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald200),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { viewModel.quickAddRealisasiHours(qh) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                                                Text("+${qh.toInt()} Jam", fontSize = 11.sp, color = Emerald900, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Bagian Peringatan & Alasan jika HM Awal dikoreksi manual
                                if (realisasiState.isHmAwalCorrected) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    if (realisasiState.hmAwal != realisasiState.hmAwalOriginal) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.fillMaxWidth().testTag("peringatan_koreksi_hm")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Anda mengubah HM Awal dari nilai riwayat sistem (${String.format(Locale.US, "%.1f", realisasiState.hmAwalOriginal)} -> ${String.format(Locale.US, "%.1f", realisasiState.hmAwal)})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                        Text(
                                            text = "Alasan Koreksi HM Awal (misal: salah input shift sebelumnya / pergantian meteran alat):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (realisasiState.alasanKoreksiHm.isBlank()) Color(0xFFDC2626) else Slate700
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        OutlinedTextField(
                                            value = realisasiState.alasanKoreksiHm,
                                            onValueChange = { viewModel.setRealisasiAlasanKoreksiHm(it) },
                                            placeholder = { Text("misal: salah input shift sebelumnya / pergantian meteran alat", fontSize = 12.sp, color = Slate400) },
                                            singleLine = true,
                                            isError = realisasiState.alasanKoreksiHm.isBlank(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFFD97706),
                                                unfocusedBorderColor = if (realisasiState.alasanKoreksiHm.isBlank()) Color(0xFFFCA5A5) else Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("input_alasan_koreksi_hm")
                                        )
                                        if (realisasiState.alasanKoreksiHm.isBlank()) {
                                            Text(
                                                text = "Alasan Koreksi HM Awal wajib diisi saat koreksi manual aktif",
                                                color = Color(0xFFDC2626),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (!realisasiState.isHmValid && realisasiState.hmErrorMessage != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFEF2F2),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = ErrorRose,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = realisasiState.hmErrorMessage ?: "",
                                                color = ErrorRose,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // HASIL KALKULASI HM BERJALAN & STATUS EVALUASI STANDAR
                                val runningHm = realisasiState.hmHarianBerjalan
                                val isUnderMin = runningHm > 0.0 && runningHm < realisasiState.hmMinStandar
                                val isOverMax = runningHm > realisasiState.hmMaxStandar

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isUnderMin -> Color(0xFFFFF1F2)
                                        isOverMax -> Color(0xFFFFFBEB)
                                        runningHm > 0.0 -> Emerald50
                                        else -> Color(0xFFF1F5F9)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when {
                                            isUnderMin -> Color(0xFFFECDD3)
                                            isOverMax -> Color(0xFFFDE68A)
                                            runningHm > 0.0 -> Emerald200
                                            else -> Color(0xFFCBD5E1)
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "TOTAL HM BERJALAN",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        isUnderMin -> Color(0xFF9F1239)
                                                        isOverMax -> Color(0xFF92400E)
                                                        runningHm > 0.0 -> Emerald900
                                                        else -> Slate700
                                                    }
                                                )
                                                val endVal = realisasiState.hmAkhirInput.replace(',', '.').toDoubleOrNull() ?: realisasiState.hmAwal
                                                Text(
                                                    text = "Formula: ${String.format(Locale.US, "%.1f", endVal)} – ${String.format(Locale.US, "%.1f", realisasiState.hmAwal)} Jam",
                                                    fontSize = 10.sp,
                                                    color = when {
                                                        isUnderMin -> Color(0xFFBE123C)
                                                        isOverMax -> Color(0xFFB45309)
                                                        runningHm > 0.0 -> Emerald700
                                                        else -> Slate500
                                                    }
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = when {
                                                    isUnderMin -> Color(0xFFE11D48)
                                                    isOverMax -> Color(0xFFD97706)
                                                    runningHm > 0.0 -> Emerald600
                                                    else -> Slate500
                                                }
                                            ) {
                                                Text(
                                                    text = "${String.format(Locale.US, "%.1f", runningHm)} Jam",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(
                                            color = when {
                                                isUnderMin -> Color(0xFFFECDD3)
                                                isOverMax -> Color(0xFFFDE68A)
                                                runningHm > 0.0 -> Emerald200
                                                else -> Color(0xFFCBD5E1)
                                            },
                                            thickness = 0.5.dp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        val minStdLabel = if (realisasiState.hmMinStandar % 1.0 == 0.0) realisasiState.hmMinStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMinStandar)
                                        val maxStdLabel = if (realisasiState.hmMaxStandar % 1.0 == 0.0) realisasiState.hmMaxStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMaxStandar)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    imageVector = when {
                                                        isUnderMin -> Icons.Default.WarningAmber
                                                        isOverMax -> Icons.Default.Info
                                                        runningHm > 0.0 -> Icons.Default.CheckCircle
                                                        else -> Icons.Default.Speed
                                                    },
                                                    contentDescription = null,
                                                    tint = when {
                                                        isUnderMin -> Color(0xFFE11D48)
                                                        isOverMax -> Color(0xFFD97706)
                                                        runningHm > 0.0 -> Emerald700
                                                        else -> Slate500
                                                    },
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = when {
                                                        isUnderMin -> "Kurang dari standar minimal ($minStdLabel HM) • Wajib kendala"
                                                        isOverMax -> "Melebihi standar maksimal ($maxStdLabel HM) • Lembur"
                                                        runningHm > 0.0 -> "Sesuai standar operasional ($minStdLabel – $maxStdLabel HM)"
                                                        else -> "Standar target unit: $minStdLabel – $maxStdLabel HM"
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = when {
                                                        isUnderMin -> Color(0xFFBE123C)
                                                        isOverMax -> Color(0xFFB45309)
                                                        runningHm > 0.0 -> Emerald800
                                                        else -> Slate600
                                                    }
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when {
                                                    isUnderMin -> Color(0xFFFEE2E2)
                                                    isOverMax -> Color(0xFFFEF3C7)
                                                    runningHm > 0.0 -> Emerald100
                                                    else -> Color(0xFFE2E8F0)
                                                }
                                            ) {
                                                Text(
                                                    text = when {
                                                        isUnderMin -> "DI BAWAH STD"
                                                        isOverMax -> "LEBIH STD"
                                                        runningHm > 0.0 -> "SESUAI STD"
                                                        else -> "NORMAL"
                                                    },
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = when {
                                                        isUnderMin -> Color(0xFF9F1239)
                                                        isOverMax -> Color(0xFF92400E)
                                                        runningHm > 0.0 -> Emerald900
                                                        else -> Slate600
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // ==========================================
                                // VALIDASI HM KURANG DARI STANDAR MINIMAL
                                // DUKUNGAN MULTI-KENDALA & INPUT WAKTU KEJADIAN
                                // ==========================================
                                if (isUnderMin) {
                                    val minStdNotice = if (realisasiState.hmMinStandar % 1.0 == 0.0) realisasiState.hmMinStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMinStandar)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFFFBEB),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                        modifier = Modifier.fillMaxWidth().testTag("container_multi_kendala")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Wajib Mencatat Kendala Lapangan",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF92400E)
                                                    )
                                                }
                                                if (realisasiState.totalHambatanMenit > 0) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFDE68A)
                                                    ) {
                                                        Text(
                                                            text = realisasiState.totalHambatanLabel,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF92400E),
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "HM berjalan (${String.format(Locale.US, "%.1f", realisasiState.hmHarianBerjalan)} HM) kurang dari batas minimal ($minStdNotice HM). Catat jenis hambatan dan rentang waktu kejadian secara akurat:",
                                                fontSize = 11.sp,
                                                color = Color(0xFF78350F),
                                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                            )

                                            // Daftar Kartu Kendala (Multi-Kendala)
                                            realisasiState.daftarKendala.forEachIndexed { index, item ->
                                                val isRowOverlapping = realisasiState.isKendalaOverlapping(index)
                                                val isCardValid = item.isValid && !isRowOverlapping
                                                Card(
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isCardValid) Color(0xFFE2E8F0) else Color(0xFFFCA5A5)
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 10.dp)
                                                        .testTag("card_kendala_${index + 1}")
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        // Header Baris Kendala
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Surface(
                                                                    shape = CircleShape,
                                                                    color = Color(0xFFFEF3C7),
                                                                    modifier = Modifier.size(22.dp)
                                                                ) {
                                                                    Box(contentAlignment = Alignment.Center) {
                                                                        Text(
                                                                            text = "${index + 1}",
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color(0xFF92400E)
                                                                        )
                                                                    }
                                                                }
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "Kendala #${index + 1}",
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 12.sp,
                                                                    color = Slate900
                                                                )
                                                            }

                                                            // Tombol Hapus (Ikon Tempat Sampah) jika baris > 1
                                                            if (realisasiState.daftarKendala.size > 1) {
                                                                IconButton(
                                                                    onClick = { viewModel.removeKendalaRow(index) },
                                                                    modifier = Modifier
                                                                        .size(28.dp)
                                                                        .testTag("btn_hapus_kendala_${index + 1}")
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.DeleteOutline,
                                                                        contentDescription = "Hapus Kendala #${index + 1}",
                                                                        tint = Color(0xFFDC2626),
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        // a. Dropdown Penyebab Kendala (Searchable Selector)
                                                        SelectorFieldTrigger(
                                                            label = "Kendala",
                                                            selectedValue = item.selectedKendala?.let {
                                                                "${it.kodeKendala} - ${it.namaKendala} (${it.kategori})"
                                                            },
                                                            placeholder = "Pilih penyebab kendala (Hujan, Breakdown, dll)...",
                                                            leadingIcon = Icons.Default.ReportProblem,
                                                            isError = item.selectedKendala == null,
                                                            errorMessage = if (item.selectedKendala == null) "Penyebab kendala wajib dipilih" else null,
                                                            testTag = "selector_kendala_${index + 1}",
                                                            onClick = {
                                                                activeKendalaIndex = index
                                                                isKendalaSheetOpen = true
                                                            }
                                                        )

                                                        Spacer(modifier = Modifier.height(10.dp))

                                                        // b. Dua Kolom Waktu (Time Picker / Format JJ:MM)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            KendalaTimeInputField(
                                                                label = "Waktu Mulai Kendala",
                                                                value = item.waktuMulai,
                                                                onValueChange = { viewModel.updateKendalaWaktuMulai(index, it) },
                                                                placeholder = "08:00",
                                                                isError = item.waktuMulai.isBlank() || KendalaItemFormState.parseTimeToMinutes(item.waktuMulai) == null || isRowOverlapping,
                                                                errorMessage = if (item.waktuMulai.isNotBlank() && KendalaItemFormState.parseTimeToMinutes(item.waktuMulai) == null) "Format salah (JJ:MM)" else null,
                                                                modifier = Modifier.weight(1f),
                                                                testTag = "input_waktu_mulai_${index + 1}"
                                                            )

                                                            KendalaTimeInputField(
                                                                label = "Waktu Selesai Kendala",
                                                                value = item.waktuSelesai,
                                                                onValueChange = { viewModel.updateKendalaWaktuSelesai(index, it) },
                                                                placeholder = "10:15",
                                                                isError = item.waktuSelesai.isBlank() || KendalaItemFormState.parseTimeToMinutes(item.waktuSelesai) == null || (item.waktuMulai.isNotBlank() && !item.isTimeRangeValid) || isRowOverlapping,
                                                                errorMessage = if (item.waktuSelesai.isNotBlank() && KendalaItemFormState.parseTimeToMinutes(item.waktuSelesai) == null) "Format salah (JJ:MM)" else null,
                                                                modifier = Modifier.weight(1f),
                                                                testTag = "input_waktu_selesai_${index + 1}"
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        // c. Perhitungan Durasi Otomatis & Validasi Waktu / Overlapping
                                                        if (item.waktuMulai.isNotBlank() && item.waktuSelesai.isNotBlank()) {
                                                            if (!item.isTimeRangeValid) {
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = Color(0xFFFEF2F2),
                                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                                                    modifier = Modifier.fillMaxWidth().testTag("error_waktu_kendala_${index + 1}")
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Icon(
                                                                            Icons.Default.ErrorOutline,
                                                                            contentDescription = null,
                                                                            tint = Color(0xFFDC2626),
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = if (item.startMinutes == null || item.endMinutes == null) {
                                                                                "Format jam salah (gunakan format JJ:MM, misal 08:00)"
                                                                            } else {
                                                                                "Waktu Selesai harus selalu lebih besar dari Waktu Mulai pada baris ini"
                                                                            },
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Medium,
                                                                            color = Color(0xFFB91C1C)
                                                                        )
                                                                    }
                                                                }
                                                            } else if (isRowOverlapping) {
                                                                // Validasi Anti-Overlapping Jam Kendala
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = Color(0xFFFEF2F2),
                                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                                                    modifier = Modifier.fillMaxWidth().testTag("error_overlap_kendala_${index + 1}")
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Icon(
                                                                            Icons.Default.ErrorOutline,
                                                                            contentDescription = null,
                                                                            tint = Color(0xFFDC2626),
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = if (realisasiState.isKendalaOverlappingWithPrevious(index)) {
                                                                                "Jam kendala bertabrakan dengan kendala sebelumnya!"
                                                                            } else {
                                                                                "Jam kendala bertabrakan dengan kendala lainnya!"
                                                                            },
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color(0xFFB91C1C)
                                                                        )
                                                                    }
                                                                }
                                                            } else {
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = Color(0xFFFEF3C7),
                                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                                                    modifier = Modifier.fillMaxWidth().testTag("durasi_kendala_${index + 1}")
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Icon(
                                                                            Icons.Default.Schedule,
                                                                            contentDescription = null,
                                                                            tint = Color(0xFFD97706),
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = "Total Hambatan: ${item.durasiLabel} (${item.durasiMenit} Menit)",
                                                                            fontSize = 12.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            maxLines = 1,
                                                                            softWrap = false,
                                                                            color = Color(0xFF92400E)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Alert Banner jika Overlap terjadi di baris mana pun
                                            if (realisasiState.hasAnyKendalaOverlap) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFFEF2F2),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("banner_overlap_kendala")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Jam kendala bertabrakan dengan kendala sebelumnya! Perbaiki jam input kendala sebelum mengirim.",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFB91C1C)
                                                        )
                                                    }
                                                }
                                            }

                                            // Alert Banner jika Akumulasi Jam Kendala melebihi Shift
                                            if (realisasiState.isTotalKendalaExceedingShift) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFFEF2F2),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("banner_limit_shift_kendala")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Akumulasi total kendala (${realisasiState.totalHambatanLabel}) melebihi batas maksimal durasi jam kerja shift (${String.format(Locale.US, "%.1f", realisasiState.maxShiftHours)} Jam)!",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFB91C1C)
                                                        )
                                                    }
                                                }
                                            }

                                            // Tombol Tambah Kendala Lain
                                            OutlinedButton(
                                                onClick = { viewModel.addKendalaRow() },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = Color(0xFF92400E)
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("btn_tambah_kendala")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Tambah Kendala Lain",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // Warning jika over-standard
                                if (isOverMax) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFF7ED),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFB923C)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val maxStdNotice = if (realisasiState.hmMaxStandar % 1.0 == 0.0) realisasiState.hmMaxStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMaxStandar)
                                            Text(
                                                text = "HM Berjalan melebihi standar maksimal unit ($maxStdNotice HM). Dialog konfirmasi lembur operasional akan ditampilkan sebelum kirim.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF9A3412),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // 5. PARAMETER STOK / STIK SOLAR & BBM
                        // ==========================================
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "5. Stok Stik Solar (Solar)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    )
                                }

                                // Input Stik Solar Awal & Akhir
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Stik Solar Awal", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                                        OutlinedTextField(
                                            value = realisasiState.stikSolarAwalInput,
                                            onValueChange = { viewModel.setRealisasiStikAwal(it) },
                                            placeholder = { Text("Contoh: 85.0", fontSize = 12.sp, color = Slate400) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            trailingIcon = { Text("cm/L", color = Slate400, fontSize = 11.sp, modifier = Modifier.padding(end = 8.dp)) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald600,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("input_stik_solar_awal")
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Stik Solar Akhir", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                                        OutlinedTextField(
                                            value = realisasiState.stikSolarAkhirInput,
                                            onValueChange = { viewModel.setRealisasiStikAkhir(it) },
                                            placeholder = { Text("Contoh: 60.0", fontSize = 12.sp, color = Slate400) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            trailingIcon = { Text("cm/L", color = Slate400, fontSize = 11.sp, modifier = Modifier.padding(end = 8.dp)) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald600,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("input_stik_solar_akhir")
                                        )
                                    }
                                }

                                // 1. Peringatan Oranye: Stik Akhir > Stik Awal tanpa toggle Isi Solar
                                if (realisasiState.isStikAkhirHigherWithoutRefuel) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFFBEB),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        modifier = Modifier.fillMaxWidth().testTag("warning_stik_akhir_higher")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Stik akhir lebih tinggi dari stik awal. Apakah ada pengisian BBM lapangan? Aktifkan pengisian jika unit baru diisi solar.",
                                                fontSize = 12.sp,
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Toggle Switch Pengisian Solar BBM Tambahan
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (realisasiState.isRefuelLitersMissing) ErrorRose else Slate200),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Isi Solar Tambahan",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Slate900
                                                )
                                            }
                                            Switch(
                                                checked = realisasiState.isIsiSolar,
                                                onCheckedChange = { viewModel.setRealisasiIsiSolar(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = Emerald600
                                                ),
                                                modifier = Modifier.testTag("switch_isi_solar_tambahan")
                                            )
                                        }

                                        if (realisasiState.isIsiSolar) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Jumlah Liter Pengisian BBM * (Wajib Diisi)",
                                                fontSize = 11.sp,
                                                color = if (realisasiState.isRefuelLitersMissing) ErrorRose else Slate700,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            OutlinedTextField(
                                                value = realisasiState.jumlahLiterSolarInput,
                                                onValueChange = { viewModel.setRealisasiJumlahSolar(it) },
                                                placeholder = { Text("Contoh: 150 (Wajib diisi)", fontSize = 12.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                isError = realisasiState.isRefuelLitersMissing,
                                                trailingIcon = { Text("Liter", color = Slate500, fontSize = 12.sp, modifier = Modifier.padding(end = 12.dp)) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Emerald600,
                                                    unfocusedBorderColor = if (realisasiState.isRefuelLitersMissing) ErrorRose else Slate200,
                                                    focusedContainerColor = Color(0xFFF8FAFC),
                                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("input_jumlah_solar")
                                            )
                                            if (realisasiState.isRefuelLitersMissing) {
                                                Text(
                                                    text = "Wajib memasukkan jumlah liter pengisian BBM lapangan (> 0).",
                                                    color = ErrorRose,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Kalkulasi Visual Pemakaian Solar Otomatis:
                                // Formula: Konsumsi Solar = (Stik Solar Awal + Total Isi Solar Tambahan) - Stik Solar Akhir
                                val awalVal = realisasiState.parsedStikAwal ?: 0.0
                                val akhirVal = realisasiState.parsedStikAkhir ?: 0.0
                                val refuelVal = if (realisasiState.isIsiSolar) (realisasiState.parsedJumlahLiterSolar ?: 0.0) else 0.0
                                val konsumsiTotal = realisasiState.computedKonsumsiSolar

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (realisasiState.isSolarConsumptionNegative) Color(0xFFFEF2F2) else Emerald50,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (realisasiState.isSolarConsumptionNegative) Color(0xFFFECACA) else Emerald100),
                                    modifier = Modifier.fillMaxWidth().testTag("card_kalkulasi_solar")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Formula Konsumsi Solar Otomatis:",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (realisasiState.isSolarConsumptionNegative) Color(0xFF991B1B) else Emerald900
                                                )
                                                Text(
                                                    text = "(${String.format(Locale.US, "%.1f", awalVal)} Awal + ${String.format(Locale.US, "%.1f", refuelVal)} Isi) - ${String.format(Locale.US, "%.1f", akhirVal)} Akhir",
                                                    fontSize = 10.sp,
                                                    color = if (realisasiState.isSolarConsumptionNegative) Color(0xFFB91C1C) else Emerald700,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", konsumsiTotal)} Liter",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (realisasiState.isSolarConsumptionNegative) Color(0xFFB91C1C) else Emerald900
                                            )
                                        }

                                        // 3. Peringatan Konsumsi Negatif
                                        if (realisasiState.isSolarConsumptionNegative) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFFEE2E2),
                                                modifier = Modifier.fillMaxWidth().testTag("alert_solar_negatif")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ErrorOutline,
                                                        contentDescription = null,
                                                        tint = ErrorRose,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Konsumsi solar tidak boleh bernilai negatif (${String.format(Locale.US, "%.1f", konsumsiTotal)} L). Periksa kembali angka stik solar dan pengisian BBM.",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF991B1B),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 6. Hasil Kerja Lapangan (SELALU TERBUKA untuk Operasi, Standby, Breakdown)
                        val resolvedKategori = realisasiState.selectedActivity?.kategoriStatus?.ifBlank { null }
                            ?: realisasiState.selectedRencana?.kategori?.ifBlank { "OPERASI" }
                            ?: "OPERASI"
                        val isOperasi = resolvedKategori.equals("OPERASI", ignoreCase = true)
                        val dynamicUnitLabel = realisasiState.selectedActivity?.unitOfMeasure?.ifBlank { null }
                            ?: realisasiState.selectedRencana?.satuan?.ifBlank { "m3" }
                            ?: "m3"
                        val satuanFormatted = if (dynamicUnitLabel.contains("m3", ignoreCase = true)) "m³" else dynamicUnitLabel

                        Text(
                            text = if (isOperasi) "6. Hasil Kerja ($satuanFormatted) *" else "6. Hasil Kerja ($satuanFormatted) (Opsional)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = realisasiState.hasilKerjaInput,
                            onValueChange = {
                                viewModel.setRealisasiHasilKerja(it)
                            },
                            label = {
                                Text(
                                    if (isOperasi) "Hasil Kerja ($satuanFormatted) *"
                                    else "Hasil Kerja ($satuanFormatted) (Opsional)"
                                )
                            },
                            placeholder = { Text("Contoh: 10", color = Slate400, fontSize = 13.sp) },
                            isError = realisasiState.isHasilKerjaError,
                            supportingText = {
                                if (realisasiState.isHasilKerjaError) {
                                    Text(
                                        text = "Hasil kerja ($satuanFormatted) wajib diisi untuk aktivitas Operasi!",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = true,
                            readOnly = false,
                            trailingIcon = {
                                Text(
                                    text = satuanFormatted,
                                    color = if (realisasiState.isHasilKerjaError) MaterialTheme.colorScheme.error else Emerald700,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate200,
                                disabledContainerColor = Color(0xFFF1F5F9),
                                disabledTextColor = Slate500
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_hasil_kerja")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 7. Foto
                        Text(
                            text = "7. Foto",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        PhotoProofCard(
                            photoPath = realisasiState.photoPath,
                            photoSizeKb = realisasiState.photoSizeKb,
                            photoDimensions = realisasiState.photoDimensions,
                            isCompressing = realisasiState.isCompressingPhoto,
                            onPhotoSelected = { uri ->
                                viewModel.processAndCompressRealisasiPhoto(context, uri)
                            },
                            onRemovePhoto = { viewModel.removeRealisasiPhoto() }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 8. Keterangan (Anti-tertutup keyboard/QWERTY)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(keteranganBringIntoViewRequester)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = Emerald600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "8. Keterangan",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate800,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    if (realisasiState.keterangan.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Emerald100
                                        ) {
                                            Text(
                                                text = "${realisasiState.keterangan.length} Karakter",
                                                color = Emerald900,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = realisasiState.keterangan,
                                    onValueChange = { viewModel.setRealisasiKeterangan(it) },
                                    placeholder = { Text("Kondisi cuaca, slip, antrean disposal, catatan shift...", color = Slate400, fontSize = 13.sp) },
                                    minLines = 3,
                                    maxLines = 6,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Emerald600,
                                        unfocusedBorderColor = Slate200
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_realisasi_keterangan")
                                        .onFocusEvent { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    delay(250)
                                                    keteranganBringIntoViewRequester.bringIntoView()
                                                }
                                            }
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Validasi penguncian tombol kirim:
                        // 1. Standar HM minimal: jika HM kurang dari standar, kendala wajib diisi lengkap
                        val isUnderMinLimit = realisasiState.hmHarianBerjalan > 0.0 &&
                                realisasiState.hmHarianBerjalan < realisasiState.hmMinStandar

                        // 2. Anti-Overlapping & Batas Jam Shift
                        val hasAnyKendalaOverlap = realisasiState.hasAnyKendalaOverlap
                        val isTotalKendalaExceedingShift = realisasiState.isTotalKendalaExceedingShift
                        val hasIncompleteKendala = realisasiState.daftarKendala.any {
                            (it.selectedKendala != null || it.waktuMulai.isNotBlank() || it.waktuSelesai.isNotBlank()) && !it.isValid
                        }

                        val isLockedDueToKendala = if (isUnderMinLimit) {
                            realisasiState.daftarKendala.isEmpty() ||
                                    !realisasiState.daftarKendala.all { it.isValid } ||
                                    hasAnyKendalaOverlap ||
                                    isTotalKendalaExceedingShift
                        } else {
                            hasAnyKendalaOverlap || isTotalKendalaExceedingShift || hasIncompleteKendala
                        }

                        // 3. Koreksi Manual HM Awal: Alasan wajib diisi jika koreksi aktif (kecuali mode Koreksi Kasie)
                        val isLockedDueToHmAwalKoreksi = !realisasiState.isEditKoreksiMode && realisasiState.isHmAwalCorrected && realisasiState.alasanKoreksiHm.isBlank()

                        // 4. HM Akhir wajib lebih besar dari HM Awal
                        val isHmAkhirEntered = realisasiState.hmAkhirInput.isNotBlank()
                        val isHmAkhirGreater = realisasiState.isHmValid && ((realisasiState.hmAkhirInput.replace(',', '.').toDoubleOrNull() ?: 0.0) > realisasiState.hmAwal)

                        // 5. Validasi Solar BBM: Wajib isi liter jika toggle aktif & cegah konsumsi bernilai negatif
                        val isLockedDueToSolar = realisasiState.isRefuelLitersMissing || realisasiState.isSolarConsumptionNegative

                        // 6. Validasi Lonjakan HM Ekstrem (> 24.0 jam)
                        val isLockedDueToExtremeHm24h = realisasiState.isHmJumpExtreme24h

                        val isSubmitEnabled = !realisasiState.isSubmitting &&
                                realisasiState.selectedRencana != null &&
                                isHmAkhirEntered &&
                                isHmAkhirGreater &&
                                !isLockedDueToExtremeHm24h &&
                                !isLockedDueToKendala &&
                                !isLockedDueToHmAwalKoreksi &&
                                !isLockedDueToSolar

                        // Tombol Aksi: Kirim Realisasi Kerja (Selesai)
                        Button(
                            onClick = {
                                val totalHm = realisasiState.hmHarianBerjalan
                                val hasilKerja = safeParseDouble(realisasiState.hasilKerjaInput)

                                // VALIDASI ATURAN OPERASI:
                                if (isOperasi && totalHm > 0.0 && hasilKerja <= 0.0) {
                                    viewModel.setRealisasiHasilKerjaError(true)
                                    Toast.makeText(
                                        context,
                                        "Aktivitas OPERASI dengan HM berjalan wajib mengisi Hasil Kerja ($satuanFormatted)!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button // Stop proses simpan
                                }

                                if (realisasiState.isHmWarningExceed14h) {
                                    showExtremeHmWarningConfirmDialog = true
                                } else if (realisasiState.hmHarianBerjalan > realisasiState.hmMaxStandar) {
                                    showOverHmConfirmDialog = true
                                } else {
                                    viewModel.submitRealisasiKerja(context)
                                }
                            },
                            enabled = isSubmitEnabled,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald600,
                                disabledContainerColor = Slate200
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_kirim_realisasi_selesai")
                        ) {
                            if (realisasiState.isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mengirim Realisasi...")
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        realisasiState.selectedRencana == null -> "Pilih Unit / Rencana Kerja Terlebih Dahulu"
                                        !isHmAkhirEntered -> "Masukkan HM Akhir Lapangan"
                                        isLockedDueToExtremeHm24h -> "Lonjakan HM > 24 Jam! Periksa HM Akhir"
                                        !isHmAkhirGreater -> realisasiState.hmErrorMessage ?: "HM Akhir Wajib Lebih Besar dari HM Awal"
                                        isLockedDueToHmAwalKoreksi -> "Isi Alasan Koreksi HM Awal"
                                        hasAnyKendalaOverlap -> "Jam Kendala Bertabrakan!"
                                        isTotalKendalaExceedingShift -> "Durasi Kendala Melebihi Jam Shift!"
                                        isUnderMinLimit && realisasiState.daftarKendala.isEmpty() -> "Wajib Tambah Data Kendala"
                                        isUnderMinLimit && realisasiState.daftarKendala.any { it.selectedKendala == null } -> "Pilih Penyebab Kendala"
                                        isUnderMinLimit && realisasiState.daftarKendala.any { !it.isTimeRangeValid } -> "Lengkapi Jam Kendala (JJ:MM)"
                                        hasIncompleteKendala -> "Lengkapi Baris Kendala yang Diisi"
                                        realisasiState.isRefuelLitersMissing -> "Isi Jumlah Liter Pengisian BBM"
                                        realisasiState.isSolarConsumptionNegative -> "Konsumsi Solar Negatif (Periksa Stik)"
                                        !realisasiState.isSpkTerbit -> "Kirim Realisasi Kerja (SPK Menyusul)"
                                        else -> "Kirim Realisasi Kerja (Selesai)"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isSubmitEnabled) Color.White else Slate500
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            // Spacer bawah agar keyboard QWERTY tidak menutupi input keterangan & tombol
            item {
                Spacer(modifier = Modifier.height(180.dp).imePadding())
            }
            }
        }
        }
    }

    // ModalBottomSheet Filter (Menggabungkan Filter Status & Shift tanpa chip statis di bawah search bar)
    if (isFilterSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFilterSheetOpen = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Daftar Tugas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    TextButton(
                        onClick = {
                            selectedStatusFilter = "Semua"
                            selectedShiftFilter = "Semua Shift"
                            isFilterSheetOpen = false
                        }
                    ) {
                        Text(
                            text = "Reset",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF009663)
                        )
                    }
                }

                // Filter Status HANYA untuk Kasie (dihapus untuk Operator)
                if (!isOperatorSession) {
                    Text(
                        text = "Status Kartu",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Semua", "Perlu Review").forEach { statusOpt ->
                            val isSelected = selectedStatusFilter == statusOpt
                            Button(
                                onClick = { selectedStatusFilter = statusOpt },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF009663) else Color(0xFFF1F5F9),
                                    contentColor = if (isSelected) Color.White else Color(0xFF0F172A)
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = statusOpt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Shift Operasional",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semua Shift", "Shift Siang", "Shift Malam").forEach { shiftOpt ->
                        val isSelected = selectedShiftFilter == shiftOpt
                        Button(
                            onClick = { selectedShiftFilter = shiftOpt },
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF009663) else Color(0xFFF1F5F9),
                                contentColor = if (isSelected) Color.White else Color(0xFF0F172A)
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = shiftOpt,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Button(
                    onClick = { isFilterSheetOpen = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009663),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Terapkan Filter",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showApkDistributionDialog) {
        DistributionDialog(
            onDismiss = { showApkDistributionDialog = false },
            currentApkUrl = customApkUrl,
            appVersionName = viewModel.getAppVersionName(context),
            apkFileSize = viewModel.getApkFileSizeFormatted(context),
            onSaveApkLocally = {
                viewModel.downloadApk(context)
                showApkDistributionDialog = false
            },
            onShareWhatsApp = {
                viewModel.shareApkViaWhatsApp(context)
                showApkDistributionDialog = false
            },
            onShareGeneral = {
                viewModel.shareApkGeneral(context)
                showApkDistributionDialog = false
            },
            onInstallLocalApk = {
                viewModel.launchApkInstaller(context)
                showApkDistributionDialog = false
            },
            onOpenInBrowser = { url ->
                viewModel.openApkDownloadInBrowser(context, url)
            },
            onUpdateApkUrl = { newUrl ->
                viewModel.updateCustomApkUrl(newUrl)
            },
            onCopyLink = { url ->
                viewModel.copyApkUrlToClipboard(context, url)
            }
        )
    }

    if (showConfirmLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmLogoutDialog = false },
            title = {
                Text(
                    text = "Keluar Sesi?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin keluar dari sesi saat ini?",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Ya, Keluar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmLogoutDialog = false }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            }
        )
    }

    reportPendingApproveOnHome?.let { targetReport ->
        AlertDialog(
            onDismissRequest = { reportPendingApproveOnHome = null },
            title = {
                Text(
                    text = "Setujui Realisasi Lapangan?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Slate900
                )
            },
            text = {
                Text(
                    text = "Pastikan angka HM dan solar telah sesuai. Setelah disetujui, data akan terkunci permanen dan tidak dapat diubah lagi.",
                    fontSize = 13.sp,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveRealisasi(targetReport, context)
                        reportPendingApproveOnHome = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    modifier = Modifier.testTag("btn_confirm_approve_home")
                ) {
                    Text("Ya, Setujui & Kunci", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportPendingApproveOnHome = null }) {
                    Text("Batal", color = Slate700)
                }
            }
        )
    }

    // ==========================================
    // DIALOG KONFIRMASI RESET FORMULIR MANUAL
    // ==========================================
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Formulir Realisasi?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Seluruh draft inputan yang belum terkirim untuk unit ${realisasiState.selectedRencana?.kodeUnit ?: ""} akan dihapus dari cache lokal. Apakah Anda yakin?",
                    fontSize = 13.sp,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        viewModel.resetRealisasiFormManual()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Ya, Reset Formulir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Batal", color = Slate700)
                }
            }
        )
    }

    // ======================================================================
    // DIALOG KONFIRMASI LONJAKAN HM > 14.0 JAM (DI ATAS JAM KERJA WAJAR SHIFT)
    // ======================================================================
    if (showExtremeHmWarningConfirmDialog) {
        val durasiHmVal = String.format(Locale.US, "%.1f", realisasiState.hmHarianBerjalan)
        AlertDialog(
            onDismissRequest = { showExtremeHmWarningConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Konfirmasi Lonjakan HM Shift", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "HM berjalan terdeteksi $durasiHmVal jam. Apakah angka meteran sudah benar?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Durasi ini melebihi jam kerja normal shift (14 jam). Pastikan angka HM Akhir tidak salah ketik (typo).",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExtremeHmWarningConfirmDialog = false
                        viewModel.submitRealisasiKerja(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Ya, Angka Sudah Benar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtremeHmWarningConfirmDialog = false }) {
                    Text("Periksa Kembali", color = Slate700)
                }
            }
        )
    }

    // ==========================================
    // DIALOG KONFIRMASI OVER-HM (> STANDAR MAKSIMAL)
    // ==========================================
    if (showOverHmConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showOverHmConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Konfirmasi HM Melebihi Batas Standar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    val maxStdDialog = if (realisasiState.hmMaxStandar % 1.0 == 0.0) realisasiState.hmMaxStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMaxStandar)
                    Text(
                        text = "HM Berjalan yang diinput adalah ${String.format(Locale.US, "%.1f", realisasiState.hmHarianBerjalan)} Jam, melebihi standar maksimal unit ($maxStdDialog Jam).",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Apakah Anda mengonfirmasi pekerjaan lembur shift atau kebutuhan operasional tinggi ini telah disetujui?",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverHmConfirmDialog = false
                        viewModel.submitRealisasiKerja(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Ya, Kirim Realisasi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverHmConfirmDialog = false }) {
                    Text("Periksa Kembali", color = Slate700)
                }
            }
        )
    }

    // ==========================================
    // MODAL SEARCHABLE SELECTOR SHEETS
    // ==========================================

    // Sheet 1: Unit Selector with Backlog Detection
    UnitPlanSearchableSelectorSheet(
        isOpen = isUnitSheetOpen,
        onDismissRequest = { isUnitSheetOpen = false },
        availableUnits = availableUnitsForRencana,
        selectedStatus = rencanaState.selectedUnitStatus,
        kasieFilter = activeSupervisor,
        onStatusSelected = {
            viewModel.selectRencanaUnitStatus(it)
            isUnitSheetOpen = false
        }
    )

    // Sheet 2: Operator Selector (Main or Sub-Plan)
    OperatorSearchableSelectorSheet(
        isOpen = isOperatorSheetOpen,
        onDismissRequest = {
            isOperatorSheetOpen = false
            activeSubPlanOperatorTarget = null
        },
        operators = operators,
        selectedOperator = if (activeSubPlanOperatorTarget != null) {
            rencanaState.subPlans.find { it.id == activeSubPlanOperatorTarget }?.selectedOperator
        } else {
            rencanaState.selectedOperator
        },
        kasieFilter = activeSupervisor,
        onOperatorSelected = { op ->
            val subId = activeSubPlanOperatorTarget
            if (subId != null) {
                viewModel.updateSubRencanaOperator(subId, op)
            } else {
                viewModel.selectRencanaOperator(op)
            }
            activeSubPlanOperatorTarget = null
            isOperatorSheetOpen = false
        }
    )

    // Sheet 3: Activity Selector (Main or Sub-Plan)
    ActivitySearchableSelectorSheet(
        isOpen = isRencanaActivitySheetOpen,
        onDismissRequest = {
            isRencanaActivitySheetOpen = false
            activeSubPlanActivityTarget = null
        },
        activities = filteredActivitiesForRencana,
        selectedActivity = if (activeSubPlanActivityTarget != null) {
            rencanaState.subPlans.find { it.id == activeSubPlanActivityTarget }?.selectedActivity
        } else {
            rencanaState.selectedActivity
        },
        unitType = selectedRencanaUnitType.takeIf { it.isNotBlank() },
        unitCode = rencanaState.selectedUnit?.unitCode,
        modelTipe = rencanaState.selectedUnit?.brandModel ?: selectedRencanaUnitType.takeIf { it.isNotBlank() },
        onActivitySelected = { act ->
            val subId = activeSubPlanActivityTarget
            if (subId != null) {
                viewModel.updateSubRencanaActivity(subId, act)
            } else {
                viewModel.selectRencanaActivity(act)
            }
            activeSubPlanActivityTarget = null
            isRencanaActivitySheetOpen = false
        }
    )

    // Sheet 4: Lokasi Selector (Main or Sub-Plan)
    LokasiSearchableSelectorSheet(
        isOpen = isLokasiSheetOpen,
        onDismissRequest = {
            viewModel.onLokasiSearchQueryChanged("")
            isLokasiSheetOpen = false
            activeSubPlanLokasiTarget = null
        },
        lokasiList = lokasiList,
        selectedLokasi = if (activeSubPlanLokasiTarget != null) {
            rencanaState.subPlans.find { it.id == activeSubPlanLokasiTarget }?.selectedLokasi
        } else {
            rencanaState.selectedLokasi
        },
        onLokasiSelected = { lok ->
            val subId = activeSubPlanLokasiTarget
            if (subId != null) {
                viewModel.updateSubRencanaLokasi(subId, lok)
            } else {
                viewModel.selectRencanaLokasi(lok)
            }
            viewModel.onLokasiSearchQueryChanged("")
            activeSubPlanLokasiTarget = null
            isLokasiSheetOpen = false
        },
        searchQuery = lokasiSearchQuery,
        onSearchQueryChange = { viewModel.onLokasiSearchQueryChanged(it) },
        wilayahLabel = currentSupervisor?.wilayah,
        selectedWilayahFilter = lokasiWilayahFilter,
        onWilayahFilterChange = { viewModel.setLokasiWilayahFilter(it) }
    )

    // Sheet 5: Realisasi Editable Activity Sheet
    ActivitySearchableSelectorSheet(
        isOpen = isRealisasiActivitySheetOpen,
        onDismissRequest = { isRealisasiActivitySheetOpen = false },
        activities = filteredActivitiesForRealisasi,
        selectedActivity = realisasiState.selectedActivity,
        unitType = realisasiUnitType.takeIf { it.isNotBlank() },
        unitCode = realisasiPlanUnitCode,
        modelTipe = realisasiUnitType.takeIf { it.isNotBlank() },
        onActivitySelected = {
            viewModel.setRealisasiActivity(it)
            isRealisasiActivitySheetOpen = false
        }
    )

    // Sheet 6: Realisasi Editable Lokasi Sheet
    LokasiSearchableSelectorSheet(
        isOpen = isRealisasiLokasiSheetOpen,
        onDismissRequest = {
            viewModel.onLokasiSearchQueryChanged("")
            isRealisasiLokasiSheetOpen = false
        },
        lokasiList = lokasiList,
        selectedLokasi = realisasiState.selectedLokasi,
        onLokasiSelected = {
            viewModel.setRealisasiLokasi(it)
            viewModel.onLokasiSearchQueryChanged("")
            isRealisasiLokasiSheetOpen = false
        },
        searchQuery = lokasiSearchQuery,
        onSearchQueryChange = { viewModel.onLokasiSearchQueryChanged(it) },
        wilayahLabel = currentSupervisor?.wilayah,
        selectedWilayahFilter = lokasiWilayahFilter,
        onWilayahFilterChange = { viewModel.setLokasiWilayahFilter(it) }
    )

    // Sheet 7: Master Kendala Searchable Selector Sheet
    KendalaSearchableSelectorSheet(
        isOpen = isKendalaSheetOpen,
        onDismissRequest = { isKendalaSheetOpen = false },
        kendalaList = kendalaList,
        selectedKendala = activeKendalaIndex?.let { realisasiState.daftarKendala.getOrNull(it)?.selectedKendala },
        onKendalaSelected = { kendala ->
            activeKendalaIndex?.let { idx ->
                viewModel.updateKendalaSelection(idx, kendala)
            }
            isKendalaSheetOpen = false
        }
    )

    // Dialog 8: Peringatan Urutan Tugas (Strict FIFO Backlog Lock)
    backlogLockedAlertTask?.let { (currentTask, olderTask) ->
        val isDifferentDate = normalizeDateToIso(olderTask.tanggalFormat) < normalizeDateToIso(currentTask.tanggalFormat)
        val alasanDialog = if (isDifferentDate) {
            "Selesaikan tugas tanggal ${formatTanggalIndonesia(olderTask.tanggalFormat)} dulu"
        } else {
            "Selesaikan tugas Shift SIANG terlebih dahulu"
        }

        AlertDialog(
            onDismissRequest = {
                backlogLockedAlertTask = null
                viewModel.clearSelectedRencanaForRealisasi()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFEF3C7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isDifferentDate) "Peringatan Urutan Tanggal (Strict FIFO)" else "Peringatan Urutan Shift (Strict FIFO)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Slate900,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = if (isDifferentDate) {
                        "Unit ${currentTask.kodeUnit} memiliki tugas tanggal ${formatTanggalIndonesia(olderTask.tanggalFormat)} (Shift ${olderTask.shiftKerja.uppercase()}) yang belum dilaporkan.\n\nLaporan realisasi harus berurutan untuk menjaga keabsahan pembacaan HM."
                    } else {
                        "Unit ${currentTask.kodeUnit} memiliki tugas Shift SIANG pada tanggal ${formatTanggalIndonesia(olderTask.tanggalFormat)} yang belum dilaporkan.\n\nSelesaikan tugas Shift SIANG terlebih dahulu sebelum mengerjakan Shift MALAM."
                    },
                    fontSize = 13.sp,
                    color = Slate700,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        backlogLockedAlertTask = null
                        viewModel.selectRencanaForRealisasi(olderTask)
                        isInputRealisasiOpen = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009663),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isDifferentDate) "Selesaikan ${formatTanggalIndonesia(olderTask.tanggalFormat)} Sekarang" else "Selesaikan Shift SIANG Sekarang",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        backlogLockedAlertTask = null
                        viewModel.clearSelectedRencanaForRealisasi()
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tutup", color = Slate700)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun KendalaTimeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "JJ:MM",
    isError: Boolean = false,
    errorMessage: String? = null,
    testTag: String = ""
) {
    val context = LocalContext.current
    val parsedHour = value.split(":").getOrNull(0)?.toIntOrNull() ?: 8
    val parsedMinute = value.split(":").getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerDialog = remember(context, value) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formatted = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                onValueChange(formatted)
            },
            parsedHour.coerceIn(0, 23),
            parsedMinute.coerceIn(0, 59),
            true // 24-hour format
        )
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) Color(0xFFDC2626) else Slate700,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val filtered = input.take(5)
                onValueChange(filtered)
            },
            placeholder = { Text(placeholder, fontSize = 12.sp, color = Slate400) },
            singleLine = true,
            isError = isError,
            trailingIcon = {
                IconButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Pilih Waktu",
                        tint = if (isError) Color(0xFFDC2626) else Emerald600,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Emerald600,
                unfocusedBorderColor = if (isError) Color(0xFFEF4444) else Slate200,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 10.sp,
                color = Color(0xFFDC2626),
                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
            )
        }
    }
}

/**
 * Data holder untuk item kartu di Grid 2 Kolom Tab Formulir Enterprise RKCE.
 */
data class RkceGridCardItem(
    val key: String,
    val rencana: RencanaKerjaEntity,
    val reviewReport: com.example.data.local.entity.ActivityReportEntity?,
    val isReview: Boolean
)

/**
 * A. Kartu Normal (Belum Input Realisasi) - Standar Desain Enterprise RKCE
 * Dibungkus dalam Surface(shape = RoundedCornerShape(10.dp), color = Color.White, border = 1dp #E2E8F0, shadowElevation = 1.5.dp)
 * serta kotak micro-pill (#F1F5F9, rounded 6.dp, border 1dp #E2E8F0) agar informasi unit tidak mengambang.
 */
@Composable
fun RkceNormalGridCard(
    rencana: RencanaKerjaEntity,
    onInputRealisasiClick: () -> Unit
) {
    val opName = rencana.operator.ifBlank { rencana.namaOperator }.ifBlank { "-" }
    val blokName = rencana.kodeLokasi.ifBlank { "-" }
    val tanggalDisplay = formatTanggalSingkat(rencana.tanggalFormat)
    val aktivitasDisplay = rencana.namaAktivitas.ifBlank { rencana.sapCode.ifBlank { "Operasional Lapangan" } }
    val isSiang = rencana.shiftKerja.equals("SIANG", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rkce_normal_card_${rencana.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Baris 1: Kode Unit (Kiri, Bold 13sp #0F172A) & Badge Tanggal/Shift (Kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rencana.kodeUnit,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSiang) Color(0xFFE8F5E9) else Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSiang) Color(0xFFA7F3D0) else Color(0xFFE2E8F0)
                    )
                ) {
                    Text(
                        text = tanggalDisplay,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSiang) Color(0xFF2E7D32) else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }

            // Baris 2: Nama Aktivitas (Bold 11sp #0F172A, maxLines = 1, TextOverflow.Ellipsis)
            Text(
                text = aktivitasDisplay,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Baris 3 & 4: Kotak Micro-Pill Informasi Operator & Lokasi (#F1F5F9, rounded 6dp, border 1dp #E2E8F0)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 7.dp, vertical = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Opr: $opName",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Lokasi: $blokName • ${rencana.shiftKerja.uppercase()}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tombol Bawah: 1 tombol penuh "Input Realisasi"
            Button(
                onClick = onInputRealisasiClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .testTag("btn_input_realisasi_${rencana.id}"),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF009663),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Input Realisasi",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * B. Kartu Perlu Review (Sudah Diisi Operator) - Standar Desain Enterprise RKCE
 * Dibungkus dalam Surface(shape = RoundedCornerShape(10.dp), color = Color.White, border = 1dp #E2E8F0, shadowElevation = 1.5.dp)
 * dengan micro-pill container (#F1F5F9, rounded 6.dp, border 1dp #E2E8F0) untuk metrik HM & Solar.
 */
@Composable
fun RkceReviewGridCard(
    rencana: RencanaKerjaEntity,
    report: com.example.data.local.entity.ActivityReportEntity,
    onKoreksiClick: () -> Unit,
    onSetujuiClick: () -> Unit
) {
    val opName = report.operatorName.ifBlank { rencana.operator.ifBlank { rencana.namaOperator } }.ifBlank { "-" }
    val blokName = report.workLocation.ifBlank { rencana.kodeLokasi }.ifBlank { "-" }
    val shiftName = report.shift.ifBlank { rencana.shiftKerja }.ifBlank { "SIANG" }.uppercase()
    val tanggalDisplay = formatTanggalSingkat(report.reportDate.ifBlank { rencana.tanggalFormat })
    val aktivitasDisplay = report.activityName.ifBlank { rencana.namaAktivitas.ifBlank { rencana.sapCode.ifBlank { "Operasional Lapangan" } } }

    val totalHmVal = if (report.hmDailyRunning > 0.0) {
        report.hmDailyRunning
    } else {
        (report.hmAkhir - report.hmAwal).coerceAtLeast(0.0)
    }
    val solarVal = if (report.pemakaianSolar > 0.0) report.pemakaianSolar else report.fuelLiters
    val hmStr = String.format(Locale.US, "%.1f", totalHmVal)
    val solarStr = if (solarVal % 1.0 == 0.0) solarVal.toInt().toString() else String.format(Locale.US, "%.1f", solarVal)
    val hasilVal = report.workOutput
    val hasilStr = if (hasilVal % 1.0 == 0.0) hasilVal.toInt().toString() else String.format(Locale.US, "%.1f", hasilVal)
    val satuanStr = report.unitOfMeasure.ifBlank { rencana.satuanAktivitas }.ifBlank { "ha" }.lowercase()

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rkce_review_card_${report.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Baris 1: Kode Unit (Bold 13sp) & Tanggal (10sp) secara bersih tanpa badge REVIU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rencana.kodeUnit.ifBlank { report.unitCode },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tanggalDisplay,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }

            // Baris 2: Nama Aktivitas/Pekerjaan (Bold 11sp #0F172A, maxLines = 1)
            Text(
                text = aktivitasDisplay,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Baris 3: "Opr: [Nama Operator]" (9sp)
            Text(
                text = "Opr: $opName",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Baris 4: "Lokasi: [Blok] • [SHIFT]" (9sp, abu-abu #64748B)
            Text(
                text = "Lokasi: $blokName • $shiftName",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Baris 5: "+[X.X] HM • [X] L • [X] [Satuan Hasil]" (Bold 10sp, warna #009663 di dalam Surface tipis)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "+$hmStr HM • $solarStr L • $hasilStr $satuanStr",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF009663),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                )
            }

            // Baris 6: Tombol aksi horizontal "Koreksi" (border kuning/oranye) dan "Setujui" (hijau solid #009663)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onKoreksiClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .testTag("btn_koreksi_${report.id}"),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAB308)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFEFCE8),
                        contentColor = Color(0xFF92400E)
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Koreksi",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onSetujuiClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .testTag("btn_setujui_${report.id}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009663),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Setujui",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Komponen Compact Task Card untuk Rencana Kerja di Tab Formulir (CompactTaskCard / TaskCard / RencanaKerjaCard).
 * Desain ringkas, padat, dan hemat ruang vertikal.
 */
@Composable
fun CompactTaskCard(
    item: RencanaKerjaEntity,
    isLocked: Boolean = false,
    lockReason: String? = null,
    hasDraft: Boolean = false,
    existingRealisasi: com.example.data.local.entity.ActivityReportEntity? = null,
    isKasie: Boolean = true,
    onInputClick: (RencanaKerjaEntity) -> Unit = {},
    onKoreksiClick: (RencanaKerjaEntity, com.example.data.local.entity.ActivityReportEntity) -> Unit = { _, _ -> },
    onApproveClick: (com.example.data.local.entity.ActivityReportEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val hasOperatorInput = existingRealisasi != null && (
        existingRealisasi.hmAkhir > 0.0 ||
        existingRealisasi.hmDailyRunning > 0.0 ||
        existingRealisasi.fuelLiters > 0.0 ||
        existingRealisasi.pemakaianSolar > 0.0 ||
        existingRealisasi.workOutput > 0.0 ||
        existingRealisasi.unitStatus.isNotBlank()
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                if (isLocked) {
                    android.widget.Toast.makeText(
                        context,
                        "Terkunci: ${lockReason ?: "Selesaikan tugas sebelumnya"}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } else if (isKasie && hasOperatorInput && existingRealisasi != null) {
                    onKoreksiClick(item, existingRealisasi)
                } else {
                    onInputClick(item)
                }
            }
            .testTag("compact_task_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFFFAFAFA) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 1.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // BARIS 1: Header (Kode Unit, Badge Shift, Status Operasi, Nomor SPK ringkas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.kodeUnit,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )

                    // Badge Shift
                    val isSiang = item.shiftKerja.equals("SIANG", ignoreCase = true)
                    Surface(
                        color = if (isSiang) Color(0xFFE8F5E9) else Color(0xFFEDE7F6),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isSiang) "SIANG" else "MALAM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSiang) Color(0xFF2E7D32) else Color(0xFF512DA8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Badge Kategori (Operasi / Standby / Breakdown)
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.kategori.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (item.subIndex > 1) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#${item.subIndex}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (hasDraft) {
                        Surface(
                            color = Emerald50,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DRAFT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald800,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Nomor SPK Mini di pojok kanan
                val spkDisplay = if (item.isSpkTerbit) item.nomorSpk.takeLast(12) else "SPK Menyusul"
                Text(
                    text = spkDisplay,
                    fontSize = 11.sp,
                    color = if (item.isSpkTerbit) Color(0xFF94A3B8) else Color(0xFFB45309),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // BARIS 2: Aktivitas Utama
            Text(
                text = item.namaAktivitas.ifBlank { item.sapCode.ifBlank { "Operasional Lapangan" } },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // BARIS 3: Metadata Gabungan (Operator • Lokasi • Tanggal • Backlog)
            val isBacklog = isBacklogDate(item.tanggalFormat)
            val tglDisplay = formatTanggalSingkat(item.tanggalFormat)
            val opDisplay = item.operator.ifBlank { item.namaOperator }
            val locDisplay = item.kodeLokasi

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 $opDisplay  •  📍 $locDisplay  •  📅 $tglDisplay",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isBacklog) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "BACKLOG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BARIS 4: Action Buttons Berdasarkan Role
            if (isLocked) {
                Button(
                    onClick = {
                        android.widget.Toast.makeText(
                            context,
                            "Terkunci: ${lockReason ?: "Selesaikan tugas sebelumnya"}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("btn_locked_realisasi_${item.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lockReason ?: "Terkunci (Selesaikan tugas sebelumnya)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else if (isKasie) {
                // --- TAMPILAN KHUSUS KASIE: 2 TOMBOL BERDAMPINGAN ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol 1: Koreksi / Input
                    if (hasOperatorInput && existingRealisasi != null) {
                        OutlinedButton(
                            onClick = { onKoreksiClick(item, existingRealisasi) },
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("btn_koreksi_${item.id}"),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF009663)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF009663)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF009663))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Koreksi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF009663)
                            )
                        }
                    } else {
                        Button(
                            onClick = { onInputClick(item) },
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("btn_input_realisasi_${item.id}"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009663)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (hasDraft) "Lanjutkan Draft" else "Input Realisasi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Tombol 2: Setujui (Hanya Kasie)
                    val canApprove = hasOperatorInput && existingRealisasi != null
                    Button(
                        onClick = {
                            if (canApprove && existingRealisasi != null) {
                                onApproveClick(existingRealisasi)
                            }
                        },
                        enabled = canApprove,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("btn_setujui_${item.id}"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF009663),
                            disabledContainerColor = Color(0xFFF1F5F9),
                            disabledContentColor = Color(0xFFCBD5E1)
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Setujui",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // --- TAMPILAN KHUSUS OPERATOR: HANYA 1 TOMBOL LEBAR (INPUT REALISASI) ---
                Button(
                    onClick = { onInputClick(item) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .padding(top = 2.dp)
                        .testTag("btn_input_realisasi_op_${item.id}"),
                    enabled = !isLocked,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009663),
                        disabledContainerColor = Color(0xFFE2E8F0)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasDraft) "Lanjutkan Draft Realisasi Lapangan" else "Input Realisasi Lapangan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * TaskCard & RencanaKerjaCard alias untuk kemudahan impor.
 */
@Composable
fun TaskCard(
    item: RencanaKerjaEntity,
    isLocked: Boolean = false,
    lockReason: String? = null,
    onInputClick: (RencanaKerjaEntity) -> Unit = {}
) {
    CompactTaskCard(item = item, isLocked = isLocked, lockReason = lockReason, onInputClick = onInputClick)
}

@Composable
fun RencanaKerjaCard(
    item: RencanaKerjaEntity,
    isLocked: Boolean = false,
    lockReason: String? = null,
    onInputClick: (RencanaKerjaEntity) -> Unit = {}
) {
    CompactTaskCard(item = item, isLocked = isLocked, lockReason = lockReason, onInputClick = onInputClick)
}

/**
 * Pembungkus PendingAssignmentCard untuk kecocokan penuh dengan alur Tab Formulir.
 */
@Composable
fun PendingAssignmentCard(
    rencana: RencanaKerjaEntity,
    allPendingTasks: List<RencanaKerjaEntity> = emptyList(),
    unapprovedReports: List<com.example.data.local.entity.ActivityReportEntity> = emptyList(),
    myReports: List<com.example.data.local.entity.ActivityReportEntity> = emptyList(),
    hasDraft: Boolean = false,
    isOperator: Boolean = false,
    onInputRealisasi: () -> Unit,
    onKoreksiRealisasi: (RencanaKerjaEntity, com.example.data.local.entity.ActivityReportEntity) -> Unit = { _, _ -> },
    onApproveRealisasi: (com.example.data.local.entity.ActivityReportEntity) -> Unit = {},
    onShowBacklogAlert: (olderTask: RencanaKerjaEntity) -> Unit = {}
) {
    val existingRealisasi = remember(unapprovedReports, myReports, rencana) {
        unapprovedReports.firstOrNull { rep ->
            (rep.nomorSpk.isNotBlank() && rep.nomorSpk.equals(rencana.nomorSpk, ignoreCase = true)) ||
            (rep.unitCode.equals(rencana.kodeUnit, ignoreCase = true) &&
             normalizeDateToIso(rep.reportDate) == normalizeDateToIso(rencana.tanggalFormat) &&
             rep.shift.equals(rencana.shiftKerja, ignoreCase = true))
        } ?: myReports.firstOrNull { rep ->
            (rep.nomorSpk.isNotBlank() && rep.nomorSpk.equals(rencana.nomorSpk, ignoreCase = true)) ||
            (rep.unitCode.equals(rencana.kodeUnit, ignoreCase = true) &&
             normalizeDateToIso(rep.reportDate) == normalizeDateToIso(rencana.tanggalFormat) &&
             rep.shift.equals(rencana.shiftKerja, ignoreCase = true))
        }
    }

    // Cek apakah ada tugas lain untuk unit yang sama yang mendahului tugas saat ini (Strict FIFO)
    val blockingTask = remember(allPendingTasks, rencana) {
        allPendingTasks.firstOrNull { other ->
            other.kodeUnit.equals(rencana.kodeUnit, ignoreCase = true) &&
            other.id != rencana.id &&
            isPriorTask(currentTask = rencana, otherTask = other)
        }
    }

    // Cek juga apakah ada laporan realisasi sebelumnya pada unit yang sama yang masih menunggu verifikasi Kasie
    val blockingUnapprovedReport = remember(unapprovedReports, rencana) {
        val currentDate = normalizeDateToIso(rencana.tanggalFormat)
        val currentWeight = getShiftWeight(rencana.shiftKerja)
        unapprovedReports.firstOrNull { rep ->
            if (!rep.unitCode.equals(rencana.kodeUnit, ignoreCase = true)) return@firstOrNull false
            val repDate = normalizeDateToIso(rep.reportDate)
            val repWeight = getShiftWeight(rep.shift)
            (repDate.isNotBlank() && currentDate.isNotBlank() && repDate < currentDate) ||
                (repDate == currentDate && repWeight < currentWeight)
        }
    }

    val isLocked = blockingTask != null || blockingUnapprovedReport != null

    val alasanKunci = remember(blockingTask, blockingUnapprovedReport, rencana) {
        when {
            blockingTask != null -> {
                val currDate = normalizeDateToIso(rencana.tanggalFormat)
                val blockDate = normalizeDateToIso(blockingTask.tanggalFormat)
                if (blockDate < currDate) {
                    "Selesaikan tugas tanggal ${formatTanggalIndonesia(blockingTask.tanggalFormat)} dulu"
                } else {
                    "Selesaikan tugas Shift SIANG terlebih dahulu"
                }
            }
            blockingUnapprovedReport != null -> {
                val currDate = normalizeDateToIso(rencana.tanggalFormat)
                val repDate = normalizeDateToIso(blockingUnapprovedReport.reportDate)
                if (repDate < currDate) {
                    "Selesaikan & setujui tugas tanggal ${formatTanggalIndonesia(blockingUnapprovedReport.reportDate)} dulu"
                } else {
                    "Selesaikan tugas Shift SIANG terlebih dahulu"
                }
            }
            else -> null
        }
    }

    CompactTaskCard(
        item = rencana,
        isLocked = isLocked,
        lockReason = alasanKunci,
        hasDraft = hasDraft,
        existingRealisasi = existingRealisasi,
        isKasie = !isOperator,
        onInputClick = {
            if (isLocked) {
                if (blockingTask != null) {
                    onShowBacklogAlert(blockingTask)
                }
            } else {
                onInputRealisasi()
            }
        },
        onKoreksiClick = { ren, rep ->
            if (isLocked) {
                if (blockingTask != null) {
                    onShowBacklogAlert(blockingTask)
                }
            } else {
                onKoreksiRealisasi(ren, rep)
            }
        },
        onApproveClick = { rep ->
            onApproveRealisasi(rep)
        }
    )
}

/**
 * Kartu Rencana Kerja yang Sudah Selesai Direalisasikan.
 * Menampilkan ringkasan unit, badge status realisasi selesai, dan tombol untuk melihat rekapitulasi di Tab Riwayat.
 */
@Composable
fun CompletedRencanaCard(
    rencana: RencanaKerjaEntity,
    onLihatRiwayat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onLihatRiwayat() }
            .testTag("completed_rencana_card_${rencana.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Kode Unit, Model, Badge Selesai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rencana.kodeUnit,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900,
                            fontSize = 18.sp
                        )
                    )
                    if (rencana.subIndex > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Text(
                                text = "Rencana #${rencana.subIndex}",
                                color = Color(0xFF1D4ED8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "HE",
                            color = Slate700,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF166534),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "REALISASI SELESAI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            // Status SPK Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (rencana.isSpkTerbit) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (rencana.isSpkTerbit) Color(0xFF86EFAC) else Slate300
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = if (rencana.isSpkTerbit) Color(0xFF166534) else Slate600,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (rencana.isSpkTerbit)
                            "SPK: ${rencana.nomorSpk} (Terbit Resmi)"
                        else if (rencana.nomorSpk.isNotBlank())
                            "SPK: ${rencana.nomorSpk}"
                        else
                            "SPK Menyusul (Tersimpan)",
                        color = if (rencana.isSpkTerbit) Color(0xFF166534) else Slate700,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info Penugasan Grid
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Emerald600, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Operator: ${rencana.operator}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate800,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Emerald50) {
                            Text(
                                text = "Shift ${rencana.shiftKerja}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald900,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Emerald600, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lokasi: ${rencana.kodeLokasi}",
                                fontSize = 11.sp,
                                color = Slate600,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = rencana.tanggalFormat,
                            fontSize = 10.sp,
                            color = Slate500
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Emerald600, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Aktivitas: ${rencana.namaAktivitas.ifBlank { rencana.sapCode }}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        if (rencana.sapCode.isNotBlank()) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF1F5F9)) {
                                Text(
                                    text = "SAP: ${rencana.sapCode}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate600,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Tombol Lihat Detail di Tab Riwayat
            OutlinedButton(
                onClick = onLihatRiwayat,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Emerald700
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("btn_detail_riwayat_${rencana.id}")
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = Emerald700, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lihat Rekapitulasi di Tab Riwayat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun formatTanggalIndonesia(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "-"
    val clean = dateStr.trim()
    return try {
        if (clean.contains("/")) {
            clean
        } else {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(clean.take(10))
            if (date != null) outputFormat.format(date) else clean
        }
    } catch (e: Exception) {
        clean
    }
}

fun formatTanggalSingkat(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "-"
    val clean = dateStr.trim()
    return try {
        if (clean.contains("/")) {
            val parts = clean.split("/")
            if (parts.size >= 2) "${parts[0]}/${parts[1]}" else clean
        } else {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
            val date = inputFormat.parse(clean.take(10))
            if (date != null) outputFormat.format(date) else clean
        }
    } catch (e: Exception) {
        clean
    }
}

fun isBacklogDate(dateStr: String?): Boolean {
    if (dateStr.isNullOrBlank()) return false
    val clean = dateStr.trim()
    return try {
        val todayYmd = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val todayDmy = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        if (clean.contains("/")) {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val date = sdf.parse(clean)
            val todayDate = sdf.parse(todayDmy)
            date != null && todayDate != null && date.before(todayDate)
        } else {
            val datePart = clean.take(10)
            datePart < todayYmd
        }
    } catch (e: Exception) {
        false
    }
}
