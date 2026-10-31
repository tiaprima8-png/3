package com.example.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
    var selectedFormFilter by remember { mutableStateOf("SEMUA") }

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

    androidx.activity.compose.BackHandler(enabled = isInputRealisasiOpen || showNewPlanForm) {
        if (isInputRealisasiOpen) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BackgroundGreyBlue)
            .testTag("form_report_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ======================================================================
        // 4. KARTU RINGKASAN PROGRES HARIAN DI BERANDA (SHIFT PROGRESS TRACKER)
        // ======================================================================
        if (!isInputRealisasiOpen && !showNewPlanForm) {
            item {
                val progress = if (fleetProgress.totalUnitsCount > 0) fleetProgress.realizedUnitsCount.toFloat() / fleetProgress.totalUnitsCount.toFloat() else 0f
                val percentInt = (progress * 100).toInt()

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth().testTag("card_shift_progress_tracker")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Emerald100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PrecisionManufacturing,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Progres Input Unit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Slate900
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${fleetProgress.realizedUnitsCount}/${fleetProgress.totalUnitsCount} Unit ($percentInt%)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Emerald600,
                                trackColor = Slate200,
                            )
                        }
                    }
                }
            }
        }

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
                    IconButton(
                        onClick = { showNewPlanForm = false },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_back_from_rencana")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Slate700,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Buat Rencana Kerja",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_rencana_kerja"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Banner Info Susulan
                        if (rencanaState.isSusulan && rencanaState.selectedUnit != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFECFDF5),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Emerald600,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Rencana Kerja Susulan / Tambahan Hari Ini",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Emerald900
                                    )
                                }
                            }
                        }

                        // 1. UNIT
                        SelectorFieldTrigger(
                            label = "Unit",
                            selectedValue = rencanaState.selectedUnit?.let { u ->
                                when {
                                    rencanaState.isBacklog -> "${u.unitCode} - ${u.brandModel}"
                                    rencanaState.isSusulan -> "${u.unitCode} - ${u.brandModel}"
                                    else -> "${u.unitCode} - ${u.brandModel}"
                                }
                            },
                            placeholder = if (availableUnitsForRencana.isEmpty())
                                "Tidak ada unit"
                            else
                                "Pilih Unit...",
                            leadingIcon = Icons.Default.PrecisionManufacturing,
                            badgeText = rencanaState.selectedUnit?.let {
                                when {
                                    rencanaState.isSusulan -> "SUSULAN"
                                    else -> null
                                }
                            },
                            testTag = "selector_rencana_unit",
                            onClick = { isUnitSheetOpen = true }
                        )

                        if (rencanaState.selectedUnit != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
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
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when {
                                            rencanaState.isBacklog -> "TARGET: TANGGAL KEMARIN (${rencanaState.targetDate}) - BACKLOG"
                                            rencanaState.isSusulan -> "TARGET: TANGGAL HARI INI (${rencanaState.targetDate}) - SUSULAN"
                                            else -> "TARGET: TANGGAL HARI INI (${rencanaState.targetDate})"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = when {
                                            rencanaState.isBacklog -> Color(0xFF991B1B)
                                            rencanaState.isSusulan -> Emerald900
                                            else -> Emerald900
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 2. OPERATOR
                        SelectorFieldTrigger(
                            label = "Operator",
                            selectedValue = rencanaState.selectedOperator?.name,
                            placeholder = "Pilih Operator...",
                            leadingIcon = Icons.Default.Person,
                            testTag = "selector_rencana_operator",
                            onClick = {
                                activeSubPlanOperatorTarget = null
                                isOperatorSheetOpen = true
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3. AKTIVITAS
                        SelectorFieldTrigger(
                            label = "Aktivitas",
                            selectedValue = rencanaState.selectedActivity?.let { "${it.activityName} (${it.sapCode})" },
                            placeholder = if (rencanaState.selectedUnit == null)
                                "Pilih Unit dulu..."
                            else
                                "Pilih Aktivitas...",
                            leadingIcon = Icons.Default.Assignment,
                            badgeText = rencanaState.selectedActivity?.unitOfMeasure ?: selectedRencanaUnitType.takeIf { it.isNotBlank() },
                            enabled = rencanaState.selectedUnit != null,
                            testTag = "selector_rencana_aktivitas",
                            onClick = {
                                activeSubPlanActivityTarget = null
                                isRencanaActivitySheetOpen = true
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 4. LOKASI & SHIFT (BERDAMPINGAN 50% - 50%)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Kolom Kiri: Lokasi (Weight 1)
                            Box(modifier = Modifier.weight(1f)) {
                                SelectorFieldTrigger(
                                    label = "Lokasi",
                                    selectedValue = rencanaState.selectedLokasi?.kodeLokasi,
                                    placeholder = "Pilih Lokasi...",
                                    leadingIcon = Icons.Default.LocationOn,
                                    testTag = "selector_rencana_lokasi",
                                    onClick = {
                                        activeSubPlanLokasiTarget = null
                                        isLokasiSheetOpen = true
                                    }
                                )
                            }

                            // Kolom Kanan: Shift Toggle Pill (Weight 1)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Shift",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate700
                                    ),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        "SIANG" to Icons.Default.WbSunny,
                                        "MALAM" to Icons.Default.NightlightRound
                                    ).forEach { (shift, icon) ->
                                        val isSelected = rencanaState.shiftKerja == shift
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Emerald600 else Color(0xFFF1F5F9),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Emerald600 else Slate200),
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { viewModel.setRencanaShift(shift) }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.White else Slate700,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = shift,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isSelected) Color.White else Slate700
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 5. KETERANGAN (Single Line Kompak)
                        Text(
                            text = "Keterangan",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = rencanaState.keterangan,
                            onValueChange = { viewModel.setRencanaKeterangan(it) },
                            placeholder = { 
                                Text(
                                    text = "Instruksi / catatan kerja (opsional)...", 
                                    fontSize = 12.sp, 
                                    color = Color(0xFF94A3B8)
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .testTag("input_rencana_keterangan"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = false,
                            maxLines = 2,
                            textStyle = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A)
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF009663),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

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

                        // DUAL ACTION BUTTONS (BERDAMPINGAN DI UNDER FORM)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tombol 1: + Sub-Rencana
                            OutlinedButton(
                                onClick = { viewModel.addSubRencana() },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald700),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_tambah_sub_rencana")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sub-Rencana",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            // Tombol 2: Kirim Rencana Kerja
                            Button(
                                onClick = { viewModel.submitRencanaKerja() },
                                enabled = !rencanaState.isSubmitting && rencanaState.selectedUnit != null,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(42.dp)
                                    .testTag("btn_kirim_rencana_kerja")
                            ) {
                                if (rencanaState.isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Menyimpan...", fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val totalToSubmit = 1 + rencanaState.subPlans.size
                                    Text(
                                        text = if (totalToSubmit > 1)
                                            "Kirim $totalToSubmit Rencana"
                                        else
                                            "Kirim Rencana Kerja",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
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
                val targetRen = realisasiState.selectedRencana!!
                val existingMasterRel = remember(myReports, targetRen) {
                    val existingRep = myReports.firstOrNull { rep ->
                        (rep.nomorSpk.isNotBlank() && rep.nomorSpk.equals(targetRen.nomorSpk, ignoreCase = true)) ||
                        (rep.unitCode.equals(targetRen.kodeUnit, ignoreCase = true) &&
                         rep.shift.equals(targetRen.shiftKerja, ignoreCase = true))
                    }
                    existingRep?.let { com.example.data.local.entity.MasterRealisasiEntity.fromActivityReport(it) }
                }

                com.example.ui.screens.InputRealisasiScreen(
                    rencana = targetRen,
                    existingRealisasi = existingMasterRel,
                    isKasie = !isOperatorSession,
                    onNavigateBack = {
                        isInputRealisasiOpen = false
                        viewModel.clearSelectedRencanaForRealisasi()
                    },
                    onSubmitSuccess = {
                        isInputRealisasiOpen = false
                        viewModel.clearSelectedRencanaForRealisasi()
                    },
                    onSaveRealisasi = { masterRel ->
                        viewModel.saveRealisasiFromMasterEntity(masterRel, context)
                    }
                )
            }
        }

        // ==========================================
        // TAMPILAN AWAL TAB FORMULIR: TOMBOL BUAT RENCANA & DAFTAR RENCANA KERJA YANG SUDAH DIBUAT
        // ==========================================
        if (!isInputRealisasiOpen && !showNewPlanForm) {
            val kasieAtasanLabel = currentSupervisor?.kasieAtasan?.takeIf { it.isNotBlank() }
                ?: activeRencanaList.firstOrNull()?.supervisorName?.takeIf { it.isNotBlank() }
                ?: "Kasie Lapangan"

            // 1. TOMBOL AKSI UTAMA & FILTER CHIPS HORIZONTAL
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isOperatorSession) {
                        // TOMBOL UTAMA BESAR: "+ Buat Rencana Kerja Baru" (Hanya untuk Kasie)
                        Button(
                            onClick = { showNewPlanForm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald600,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .testTag("btn_buat_rencana_utama")
                                .testTag("btn_tambah_rencana_baru")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Buat Rencana Kerja Baru",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Filter Chips Horizontal
                    val countSemua = activeRencanaList.size
                    val countSiang = activeRencanaList.count { it.shiftKerja.equals("SIANG", true) }
                    val countMalam = activeRencanaList.count { it.shiftKerja.equals("MALAM", true) }
                    val unapprovedReportsList = myReports.filter { rep ->
                        !rep.isLocked &&
                        (rep.approvalStatus.equals("Menunggu Approval", ignoreCase = true) ||
                         rep.approvalStatus.equals("Menunggu Verifikasi", ignoreCase = true) ||
                         rep.approvalStatus.contains("Menunggu", ignoreCase = true)) &&
                        rep.hmAkhir > 0.0 &&
                        (rep.hmDailyRunning > 0.0 || rep.hmAkhir > rep.hmAwal || rep.workOutput > 0.0)
                    }
                    val countApprove = activeRencanaList.count { ren ->
                        unapprovedReportsList.any { rep -> rep.nomorSpk.equals(ren.nomorSpk, ignoreCase = true) }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("SEMUA", "Semua", countSemua),
                            Triple("SIANG", "Siang", countSiang),
                            Triple("MALAM", "Malam", countMalam),
                            Triple("APPROVE", "Siap Approve", countApprove)
                        ).forEach { (key, label, count) ->
                            val isSelected = selectedFormFilter == key
                            val isHighlight = key == "APPROVE" && count > 0

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) (if (isHighlight) WarningAmber else Emerald600) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color.Transparent else if (isHighlight) WarningAmber else Slate200),
                                modifier = Modifier.clickable { selectedFormFilter = key }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Slate900
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFF1F5F9))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Slate600
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. SEKSI DAFTAR RENCANA KERJA / PENUGASAN SPK
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOperatorSession) {
                            "Tugas Kerja Dari Kasie ($kasieAtasanLabel)"
                        } else {
                            "Rencana Kerja Menunggu Realisasi"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Slate900,
                        modifier = Modifier.weight(1f).testTag("header_tugas_rencana")
                    )

                    val waitingCount = if (activeRencanaList.isNotEmpty()) activeRencanaList.size else 0
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "$waitingCount Menunggu",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 3. DAFTAR KARTU RENCANA KERJA MENUNGGU REALISASI (Reaktif membaca Room SQLite)
            if (activeRencanaList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("empty_pending_assignments_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(
                                        if (fleetProgress.realizedUnitsCount > 0) Emerald50 else Color(0xFFF1F5F9),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (fleetProgress.realizedUnitsCount > 0) Icons.Default.CheckCircle else Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = if (fleetProgress.realizedUnitsCount > 0) Emerald600 else Slate400,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Text(
                                text = if (fleetProgress.realizedUnitsCount > 0)
                                    "Semua Tugas Telah Direalisasikan!"
                                else if (isOperatorSession)
                                    "Belum Ada Penugasan SPK Aktif"
                                else
                                    "Tidak Ada Rencana Menunggu Realisasi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Slate900,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = if (fleetProgress.realizedUnitsCount > 0)
                                    "Seluruh unit penugasan hari ini telah selesai dilaporkan. Rangkuman data tersimpan di Tab Riwayat & Analitik."
                                else if (isOperatorSession)
                                    "Belum ada SPK penugasan baru dari Kasie ($kasieAtasanLabel). Ketuk Sinkron Web untuk menarik penugasan terbaru."
                                else
                                    "Tidak ada rencana kerja aktif yang menunggu realisasi. Data master selalu mutakhir dengan Web Admin.",
                                fontSize = 12.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.triggerHeaderSync() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(42.dp).testTag("btn_tarik_dari_web_admin")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Slate700, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sinkron Web", fontSize = 12.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                                }

                                if (!isOperatorSession) {
                                    Button(
                                        onClick = { showNewPlanForm = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(42.dp).testTag("btn_buat_rencana_pertama")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Buat Rencana", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (fleetProgress.realizedUnitsCount > 0) {
                                Button(
                                    onClick = onNavigateToHistory,
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_buka_tab_riwayat")
                                ) {
                                    Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buka Tab Riwayat", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                val unapprovedReports = myReports.filter { rep ->
                    !rep.isLocked &&
                    (rep.approvalStatus.equals("Menunggu Approval", ignoreCase = true) ||
                     rep.approvalStatus.equals("Menunggu Verifikasi", ignoreCase = true) ||
                     rep.approvalStatus.contains("Menunggu", ignoreCase = true)) &&
                    rep.hmAkhir > 0.0 &&
                    (rep.hmDailyRunning > 0.0 || rep.hmAkhir > rep.hmAwal || rep.workOutput > 0.0)
                }
                val filteredActiveList = when (selectedFormFilter) {
                    "SIANG" -> activeRencanaList.filter { it.shiftKerja.equals("SIANG", true) }
                    "MALAM" -> activeRencanaList.filter { it.shiftKerja.equals("MALAM", true) }
                    "APPROVE" -> activeRencanaList.filter { ren ->
                        unapprovedReports.any { rep -> rep.nomorSpk.equals(ren.nomorSpk, ignoreCase = true) }
                    }
                    else -> activeRencanaList
                }
                items(filteredActiveList, key = { it.id }) { ren ->
                    val hasDraft = allDraftsList.any { it.rencanaId == ren.id }
                    PendingAssignmentCard(
                        rencana = ren,
                        allPendingTasks = activeRencanaList,
                        unapprovedReports = unapprovedReports,
                        myReports = myReports,
                        hasDraft = hasDraft,
                        isOperator = isOperatorSession,
                        onInputRealisasi = {
                            viewModel.selectRencanaForRealisasi(ren)
                        },
                        onKoreksiRealisasi = { renItem, repItem ->
                            viewModel.selectRencanaForKoreksi(renItem, repItem)
                            isInputRealisasiOpen = true
                        },
                        onApproveRealisasi = { repItem ->
                            viewModel.approveRealisasi(repItem, context)
                            android.widget.Toast.makeText(
                                context,
                                "Realisasi unit ${ren.kodeUnit} berhasil disetujui!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        onShowBacklogAlert = { olderTask ->
                            backlogLockedAlertTask = Pair(ren, olderTask)
                        }
                    )
                }
            }



            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
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
 * Komponen Compact Task Card untuk Rencana Kerja di Tab Formulir (CompactTaskCard / TaskCard / RencanaKerjaCard).
 * Desain ringkas, padat, dan hemat ruang vertikal dengan tombol aksi 38.dp.
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
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
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
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFFFAFAFA) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
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
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
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
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFF1F5F9),
                            disabledContentColor = Color(0xFF94A3B8)
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (canApprove) Color.White else Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Setujui",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canApprove) Color.White else Color(0xFF94A3B8)
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
