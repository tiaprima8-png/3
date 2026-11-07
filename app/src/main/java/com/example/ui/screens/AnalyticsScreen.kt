package com.example.ui.screens

import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.components.RkceBrandEmblem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.viewmodel.HeavyTrackViewModel

private data class KasieUnitDisplayItem(
    val unitCode: String,
    val alatName: String,
    val operatorName: String,
    val totalHm: Double,
    val totalSolar: Double,
    val rasioBbm: Double,
    val totalHasil: Double,
    val satuanHasil: String
)

@Composable
fun AnalyticsScreen(
    viewModel: HeavyTrackViewModel
) {
    val activeSupervisor by viewModel.activeSupervisor.collectAsState()
    val isOperatorSession by viewModel.isOperatorSession.collectAsState()
    val currentSupervisor by viewModel.currentSupervisorSession.collectAsState()
    val analyticsState by viewModel.analyticsState.collectAsState()
    val units by viewModel.units.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val allRencanaList by viewModel.allRencanaList.collectAsState()
    val operators by viewModel.operators.collectAsState()
    val period by viewModel.analyticsPeriod.collectAsState()
    val unitFilter by viewModel.analyticsUnitFilter.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val customApkUrl by viewModel.customApkUrl.collectAsState()

    var isUnitDropdownExpanded by remember { mutableStateOf(false) }
    var isPeriodDropdownExpanded by remember { mutableStateOf(false) }
    var showApkDistributionDialog by remember { mutableStateOf(false) }
    var showConfirmLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val rawDisplayName = remember(currentSupervisor, activeSupervisor) {
        currentSupervisor?.namaLengkap?.takeIf { it.isNotBlank() }
            ?: currentSupervisor?.name?.takeIf { it.isNotBlank() }
            ?: activeSupervisor.takeIf { it.isNotBlank() }
            ?: "Kasie"
    }

    val areaOrKasieLabel = remember(currentSupervisor, rawDisplayName) {
        val wil = currentSupervisor?.wilayah?.takeIf { it.isNotBlank() }
        if (wil != null && !rawDisplayName.contains(wil, ignoreCase = true)) {
            rawDisplayName
        } else {
            rawDisplayName
        }
    }

    // Filter realisasi khusus Operator (HANYA yang pernah diisi realisasinya oleh operator aktif)
    val operatorRealizedReports = remember(reports, currentSupervisor, rawDisplayName, isOperatorSession) {
        if (!isOperatorSession) {
            reports
        } else {
            val opFull = currentSupervisor?.resolvedNamaLengkap?.trim().orEmpty()
            val opName = currentSupervisor?.name?.trim().orEmpty()
            val opUser = currentSupervisor?.username?.trim().orEmpty()
            val opNik = currentSupervisor?.nik?.trim().orEmpty()
            reports.filter { rep ->
                val hasRealization = rep.hmAkhir > 0.0 || rep.hmDailyRunning > 0.0 || rep.workOutput > 0.0 || rep.pemakaianSolar > 0.0 || rep.fuelLiters > 0.0
                val matchesOperator = (opFull.isNotBlank() && rep.operatorName.contains(opFull, ignoreCase = true)) ||
                    (opName.isNotBlank() && rep.operatorName.contains(opName, ignoreCase = true)) ||
                    (opUser.isNotBlank() && (rep.operatorName.contains(opUser, ignoreCase = true) || rep.operatorNik?.equals(opUser, ignoreCase = true) == true)) ||
                    (opNik.isNotBlank() && (rep.operatorNik?.equals(opNik, ignoreCase = true) == true || rep.operatorName.contains(opNik, ignoreCase = true))) ||
                    rep.operatorName.contains(rawDisplayName, ignoreCase = true)
                hasRealization && matchesOperator
            }
        }
    }

    // SELECT DISTINCT kodeUnit FROM realisasi WHERE operatorId/namaOperator = :currentOperator (untuk Operator)
    // atau seluruh unit aktif spesifik milik Kasie yang sedang login (untuk Kasie)
    val roleAvailableUnits = remember(isOperatorSession, units, currentSupervisor, activeSupervisor, operatorRealizedReports) {
        if (isOperatorSession) {
            val distinctCodes = operatorRealizedReports
                .map { it.unitCode.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.uppercase() }
            distinctCodes.map { code ->
                val rep = operatorRealizedReports.firstOrNull { it.unitCode.equals(code, ignoreCase = true) }
                units.find { it.unitCode.equals(code, ignoreCase = true) } ?: com.example.data.local.entity.UnitEntity(
                    unitCode = code,
                    brandModel = rep?.unitType ?: "Alat Berat",
                    unitType = rep?.unitType ?: "HE",
                    lastHm = rep?.hmAkhir ?: 0.0,
                    supervisorAssigned = rep?.supervisorName ?: "",
                    wilayah = currentSupervisor?.wilayah ?: "PG1"
                )
            }
        } else {
            val kasieName = (currentSupervisor?.namaLengkap?.ifBlank { currentSupervisor?.name } ?: activeSupervisor).trim().removePrefix("@")
            val username = (currentSupervisor?.username ?: "").trim().removePrefix("@")
            val wilayahKasie = (currentSupervisor?.wilayah ?: "").trim()
            val byKasie = units.filter { u ->
                val spv = u.supervisorAssigned.trim().removePrefix("@")
                spv.equals(kasieName, ignoreCase = true) ||
                    (username.isNotBlank() && spv.equals(username, ignoreCase = true)) ||
                    (kasieName.lowercase() in listOf("yono", "suyono", "yono1") && spv.lowercase() in listOf("yono", "suyono", "yono1")) ||
                    (username.lowercase() in listOf("yono", "suyono", "yono1") && spv.lowercase() in listOf("yono", "suyono", "yono1")) ||
                    (kasieName.lowercase() in listOf("jahidi", "jaidi") && spv.lowercase() in listOf("jahidi", "jaidi")) ||
                    (username.lowercase() in listOf("jahidi", "jaidi") && spv.lowercase() in listOf("jahidi", "jaidi"))
            }
            when {
                byKasie.isNotEmpty() -> byKasie
                wilayahKasie.isNotBlank() && !wilayahKasie.equals("Semua Wilayah", ignoreCase = true) -> {
                    units.filter { u ->
                        u.wilayah.equals(wilayahKasie, ignoreCase = true) ||
                            u.wilayah.replace(" PG", " ").equals(wilayahKasie.replace(" PG", " "), ignoreCase = true)
                    }
                }
                else -> units
            }
        }
    }

    // Filter laporan operator berdasarkan periode & unit yang dipilih untuk metrik KPI akurat
    val operatorFilteredReports = remember(operatorRealizedReports, period, unitFilter) {
        val now = System.currentTimeMillis()
        val currentMonth = java.text.SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(java.util.Date())
        operatorRealizedReports.filter { rep ->
            val matchPeriod = when (period) {
                "30 Hari" -> (now - rep.createdAt) <= (30L * 24 * 60 * 60 * 1000)
                "Bulan Ini" -> rep.reportDate.contains(currentMonth) || (now - rep.createdAt) <= (31L * 24 * 60 * 60 * 1000)
                "Custom Range", "Custom" -> (now - rep.createdAt) <= (14L * 24 * 60 * 60 * 1000)
                else -> true
            }
            val matchUnit = unitFilter == "Semua Unit" || unitFilter.isBlank() || rep.unitCode.equals(unitFilter, ignoreCase = true)
            matchPeriod && matchUnit
        }
    }

    val displayTotalHm = remember(isOperatorSession, analyticsState.totalHmRunning, operatorFilteredReports) {
        if (isOperatorSession) {
            operatorFilteredReports.sumOf {
                if (it.hmDailyRunning > 0.0) it.hmDailyRunning else (it.hmAkhir - it.hmAwal).coerceAtLeast(0.0)
            }
        } else {
            analyticsState.totalHmRunning
        }
    }

    val displayTotalSolar = remember(isOperatorSession, analyticsState.totalSolarLiters, operatorFilteredReports) {
        if (isOperatorSession) {
            operatorFilteredReports.sumOf {
                when {
                    it.pemakaianSolar > 0.0 -> it.pemakaianSolar
                    it.isRefueled -> it.fuelLiters
                    else -> 0.0
                }
            }
        } else {
            analyticsState.totalSolarLiters
        }
    }

    val displayAvgFuelRatio = remember(isOperatorSession, analyticsState.avgFuelConsumptionRatio, displayTotalHm, displayTotalSolar) {
        if (isOperatorSession) {
            if (displayTotalHm > 0.0) displayTotalSolar / displayTotalHm else 0.0
        } else {
            analyticsState.avgFuelConsumptionRatio
        }
    }

    val totalOutputVolumeAll = remember(isOperatorSession, analyticsState.activitySummaries, analyticsState.unitSummaries, operatorFilteredReports) {
        if (isOperatorSession) {
            operatorFilteredReports.sumOf { it.workOutput }
        } else {
            val fromAct = analyticsState.activitySummaries.sumOf { it.totalOutputVolume }
            if (fromAct > 0.0) fromAct else analyticsState.unitSummaries.sumOf { it.totalOutputVolume }
        }
    }

    val displayOpHours = remember(isOperatorSession, analyticsState.effectiveOperationHours, operatorFilteredReports) {
        if (isOperatorSession) operatorFilteredReports.filter { it.unitStatus == "OPERASI" }.sumOf { it.workDurationHours }
        else analyticsState.effectiveOperationHours
    }
    val displayStbHours = remember(isOperatorSession, analyticsState.standbyHours, operatorFilteredReports) {
        if (isOperatorSession) operatorFilteredReports.filter { it.unitStatus == "STANDBY" }.sumOf { it.workDurationHours }
        else analyticsState.standbyHours
    }
    val displayBdnHours = remember(isOperatorSession, analyticsState.breakdownHours, operatorFilteredReports) {
        if (isOperatorSession) operatorFilteredReports.filter { it.unitStatus == "BREAKDOWN" }.sumOf { it.workDurationHours }
        else analyticsState.breakdownHours
    }
    val totalDurHours = displayOpHours + displayStbHours + displayBdnHours
    val displayOpPct = if (isOperatorSession) {
        if (totalDurHours > 0) (displayOpHours / totalDurHours).toFloat() else 0.8f
    } else analyticsState.operationPercent
    val displayStbPct = if (isOperatorSession) {
        if (totalDurHours > 0) (displayStbHours / totalDurHours).toFloat() else 0.15f
    } else analyticsState.standbyPercent
    val displayBdnPct = if (isOperatorSession) {
        if (totalDurHours > 0) (displayBdnHours / totalDurHours).toFloat() else 0.05f
    } else analyticsState.breakdownPercent

    // Gabungkan daftar unit sesuai peran (Operator vs Kasie) dengan ringkasan metrik analitik
    val kasieUnitItems = remember(isOperatorSession, roleAvailableUnits, analyticsState.unitSummaries, reports, operatorFilteredReports, allRencanaList, operators, unitFilter, rawDisplayName) {
        val summaryByCode = analyticsState.unitSummaries.associateBy { it.unitCode.uppercase() }
        val filteredMasterUnits = if (unitFilter == "Semua Unit" || unitFilter.isBlank()) {
            roleAvailableUnits
        } else {
            roleAvailableUnits.filter { it.unitCode.equals(unitFilter, ignoreCase = true) }
        }

        val result = mutableListOf<KasieUnitDisplayItem>()
        val includedCodes = mutableSetOf<String>()

        filteredMasterUnits.forEach { u ->
            val codeKey = u.unitCode.uppercase()
            includedCodes.add(codeKey)

            if (isOperatorSession) {
                val unitOpsReports = operatorFilteredReports.filter { it.unitCode.equals(u.unitCode, ignoreCase = true) }
                val uHm = unitOpsReports.sumOf {
                    if (it.hmDailyRunning > 0.0) it.hmDailyRunning else (it.hmAkhir - it.hmAwal).coerceAtLeast(0.0)
                }
                val uSolar = unitOpsReports.sumOf {
                    when {
                        it.pemakaianSolar > 0.0 -> it.pemakaianSolar
                        it.isRefueled -> it.fuelLiters
                        else -> 0.0
                    }
                }
                val uRatio = if (uHm > 0.0) uSolar / uHm else 0.0
                val uHasil = unitOpsReports.sumOf { it.workOutput }
                val latestRep = unitOpsReports.firstOrNull()
                val alatLabel = u.brandModel.takeIf { it.isNotBlank() && !it.equals(u.unitType, ignoreCase = true) }
                    ?.let { "${u.unitType} • $it" }
                    ?: u.unitType.ifBlank { "Alat Berat" }

                result.add(
                    KasieUnitDisplayItem(
                        unitCode = u.unitCode,
                        alatName = alatLabel,
                        operatorName = latestRep?.operatorName?.ifBlank { rawDisplayName } ?: rawDisplayName,
                        totalHm = uHm,
                        totalSolar = uSolar,
                        rasioBbm = uRatio,
                        totalHasil = uHasil,
                        satuanHasil = (latestRep?.unitOfMeasure?.takeIf { it.isNotBlank() } ?: "ha").lowercase()
                    )
                )
            } else {
                val summary = summaryByCode[codeKey]
                val latestReport = reports.firstOrNull { it.unitCode.equals(u.unitCode, ignoreCase = true) }
                val latestRencana = allRencanaList.firstOrNull { it.kodeUnit.equals(u.unitCode, ignoreCase = true) }
                val resolvedOperator = latestReport?.operatorName?.takeIf { it.isNotBlank() }
                    ?: latestRencana?.operator?.takeIf { it.isNotBlank() }
                    ?: latestRencana?.namaOperator?.takeIf { it.isNotBlank() }
                    ?: operators.firstOrNull()?.namaOperator
                    ?: "Belum Ditugaskan"

                val alatLabel = u.brandModel.takeIf { it.isNotBlank() && !it.equals(u.unitType, ignoreCase = true) }
                    ?.let { "${u.unitType} • $it" }
                    ?: u.unitType.ifBlank { "Alat Berat" }

                result.add(
                    KasieUnitDisplayItem(
                        unitCode = u.unitCode,
                        alatName = alatLabel,
                        operatorName = resolvedOperator,
                        totalHm = summary?.totalHmRunning ?: 0.0,
                        totalSolar = summary?.totalFuelLiters ?: 0.0,
                        rasioBbm = summary?.efficiencyRatio ?: 0.0,
                        totalHasil = summary?.totalOutputVolume ?: 0.0,
                        satuanHasil = (summary?.unitOfMeasure?.takeIf { it.isNotBlank() } ?: latestReport?.unitOfMeasure?.takeIf { it.isNotBlank() } ?: "ha").lowercase()
                    )
                )
            }
        }

        // Hanya gunakan fallback unitSummaries untuk Kasie jika daftar master units Kasie masih kosong
        if (!isOperatorSession && roleAvailableUnits.isEmpty()) {
            analyticsState.unitSummaries.forEach { sum ->
                val codeKey = sum.unitCode.uppercase()
                val matchesFilter = unitFilter == "Semua Unit" || unitFilter.isBlank() || sum.unitCode.equals(unitFilter, ignoreCase = true)
                if (matchesFilter && !includedCodes.contains(codeKey)) {
                    includedCodes.add(codeKey)
                    val latestReport = reports.firstOrNull { it.unitCode.equals(sum.unitCode, ignoreCase = true) }
                    val latestRencana = allRencanaList.firstOrNull { it.kodeUnit.equals(sum.unitCode, ignoreCase = true) }
                    val resolvedOperator = latestReport?.operatorName?.takeIf { it.isNotBlank() }
                        ?: latestRencana?.operator?.takeIf { it.isNotBlank() }
                        ?: latestRencana?.namaOperator?.takeIf { it.isNotBlank() }
                        ?: "Operator Lapangan"

                    result.add(
                        KasieUnitDisplayItem(
                            unitCode = sum.unitCode,
                            alatName = sum.model.ifBlank { "Alat Berat" },
                            operatorName = resolvedOperator,
                            totalHm = sum.totalHmRunning,
                            totalSolar = sum.totalFuelLiters,
                            rasioBbm = sum.efficiencyRatio,
                            totalHasil = sum.totalOutputVolume,
                            satuanHasil = sum.unitOfMeasure.ifBlank { "ha" }.lowercase()
                        )
                    )
                }
            }
        }

        result.sortedWith(
            compareByDescending<KasieUnitDisplayItem> { it.totalHm }
                .thenBy { it.unitCode }
        )
    }

    val periodDisplayLabel = when (period) {
        "30 Hari" -> "30 Hari"
        "Custom Range", "Custom" -> "Custom"
        else -> "Bulan Ini"
    }

    val unitDropdownAllLabel = if (isOperatorSession) {
        "Pilih Unit (${roleAvailableUnits.size} Unit Tersedia)"
    } else {
        "Unit Kasie: $areaOrKasieLabel (${roleAvailableUnits.size} Unit)"
    }

    val unitDropdownButtonLabel = if (unitFilter == "Semua Unit" || unitFilter.isBlank()) {
        "$unitDropdownAllLabel ▾"
    } else if (isOperatorSession) {
        "Pilih Unit: $unitFilter (${kasieUnitItems.size} Unit Tersedia) ▾"
    } else {
        "Unit Kasie: $unitFilter (${kasieUnitItems.size} Unit) ▾"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("analytics_screen")
    ) {
        // ======================================================================
        // TOP APP BAR SAPPHIRE BLUE (#002677) & MODERN ICON BUTTONS
        // ======================================================================
        Surface(
            color = Color(0xFF002677),
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("rkce_analytics_topbar")
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
                        onClick = { viewModel.refreshAnalyticsData(context) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF163E9B), CircleShape)
                            .testTag("btn_refresh_analytics")
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
                            .testTag("btn_analytics_unduh_apk")
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
                            .testTag("btn_analytics_keluar")
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
        // 1. BARIS FILTER MINIMALIS SEBARIS (COMPACT FILTER BAR)
        // ======================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("card_analytics_filters"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Kiri (Modifier.weight(1f)): Surface putih (tinggi 38dp, rounded 10dp, border 1dp #E2E8F0, shadowElevation 1.5dp)
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isUnitDropdownExpanded = true }
                        .testTag("btn_filter_unit")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = unitDropdownButtonLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                DropdownMenu(
                    expanded = isUnitDropdownExpanded,
                    onDismissRequest = { isUnitDropdownExpanded = false },
                    modifier = Modifier.heightIn(max = 260.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = unitDropdownAllLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onClick = {
                            viewModel.setFilterKodeUnit("Semua Unit")
                            viewModel.setAnalyticsUnitFilter("Semua Unit")
                            isUnitDropdownExpanded = false
                        }
                    )
                    roleAvailableUnits.forEach { u ->
                        DropdownMenuItem(
                            text = { Text("${u.unitCode} - ${u.brandModel.ifBlank { u.unitType }}", fontSize = 12.sp) },
                            onClick = {
                                viewModel.setFilterKodeUnit(u.unitCode)
                                viewModel.setAnalyticsUnitFilter(u.unitCode)
                                isUnitDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Tombol Kanan (Modifier.weight(1f)): Surface putih (tinggi 38dp, rounded 10dp, border 1dp #E2E8F0, shadowElevation 1.5dp)
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isPeriodDropdownExpanded = true }
                        .testTag("btn_filter_period")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Periode: $periodDisplayLabel ▾",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                DropdownMenu(
                    expanded = isPeriodDropdownExpanded,
                    onDismissRequest = { isPeriodDropdownExpanded = false }
                ) {
                    listOf(
                        "Bulan Ini" to "Bulan Ini",
                        "30 Hari" to "30 Hari",
                        "Custom Range" to "Custom"
                    ).forEach { (periodVal, label) ->
                        DropdownMenuItem(
                            text = { Text("Periode: $label", fontSize = 12.sp) },
                            onClick = {
                                viewModel.setAnalyticsPeriod(periodVal)
                                isPeriodDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // ======================================================================
        // LAZYCOLUMN UTAMA (RAPI, PROPORSIONAL, LANGSUNG PAS DI LAYAR)
        // ======================================================================
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==================================================================
            // 2. GRID 2X2 KARTU SKOR KPI UTAMA (BERUKURAN PAS 44DP)
            // ==================================================================
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Kotak 1: "[TotalHM] Jam" (14sp Bold Emerald #009663), sublabel "TOTAL HM KERJA"
                        CompactKpiScoreCard44(
                            valueText = "${String.format(Locale.US, "%.1f", displayTotalHm)} Jam",
                            valueColor = Color(0xFF009663),
                            sublabel = "TOTAL HM KERJA",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_hm")
                        )

                        // Kotak 2: "[TotalSolar] L" (14sp Bold #0F172A), sublabel "TOTAL SOLAR"
                        CompactKpiScoreCard44(
                            valueText = "${String.format(Locale.US, "%.1f", displayTotalSolar)} L",
                            valueColor = Color(0xFF0F172A),
                            sublabel = "TOTAL SOLAR",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_solar")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Kotak 3: "[RasioBBM] L/HM" (14sp Bold Amber #D97706), sublabel "RASIO BBM/HM"
                        CompactKpiScoreCard44(
                            valueText = "${String.format(Locale.US, "%.2f", displayAvgFuelRatio)} L/HM",
                            valueColor = Color(0xFFD97706),
                            sublabel = "RASIO BBM/HM",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_rasio_solar")
                        )

                        // Kotak 4: "[TotalHasil]" (14sp Bold #0F172A), sublabel "TOTAL HASIL KERJA"
                        val totalHasilStr = if (totalOutputVolumeAll % 1.0 == 0.0) {
                            totalOutputVolumeAll.toInt().toString()
                        } else {
                            String.format(Locale.US, "%.1f", totalOutputVolumeAll)
                        }
                        CompactKpiScoreCard44(
                            valueText = totalHasilStr,
                            valueColor = Color(0xFF0F172A),
                            sublabel = "TOTAL HASIL KERJA",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_volume_output")
                        )
                    }
                }
            }

            // ==================================================================
            // 3A. CARD DISTRIBUSI WAKTU OPERASIONAL
            // ==================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_distribusi_waktu")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "DISTRIBUSI WAKTU KERJA OPERASIONAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (isOperatorSession) {
                                "Proporsi jam operasi efektif, standby, dan breakdown unit yang dikerjakan $rawDisplayName"
                            } else {
                                "Proporsi jam operasi efektif, standby, dan breakdown armada Kasie $areaOrKasieLabel"
                            },
                            fontSize = 9.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            val opWeight = displayOpPct.coerceAtLeast(0.02f)
                            val stbWeight = displayStbPct.coerceAtLeast(0.02f)
                            val bdnWeight = displayBdnPct.coerceAtLeast(0.02f)

                            Box(
                                modifier = Modifier
                                    .weight(opWeight)
                                    .fillMaxHeight()
                                    .background(Color(0xFF009663))
                            )
                            Box(
                                modifier = Modifier
                                    .weight(stbWeight)
                                    .fillMaxHeight()
                                    .background(Color(0xFFD97706))
                            )
                            Box(
                                modifier = Modifier
                                    .weight(bdnWeight)
                                    .fillMaxHeight()
                                    .background(Color(0xFFDC2626))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val opHoursStr = String.format(Locale.US, "%.1f", displayOpHours)
                        val opPctStr = String.format(Locale.US, "%.0f%%", displayOpPct * 100)
                        val stbHoursStr = String.format(Locale.US, "%.1f", displayStbHours)
                        val stbPctStr = String.format(Locale.US, "%.0f%%", displayStbPct * 100)
                        val bdnHoursStr = String.format(Locale.US, "%.1f", displayBdnHours)
                        val bdnPctStr = String.format(Locale.US, "%.0f%%", displayBdnPct * 100)

                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "● Operasi: ${opHoursStr}j ($opPctStr)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    maxLines = 1
                                )
                                Text(
                                    text = "● Standby: ${stbHoursStr}j ($stbPctStr)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    maxLines = 1
                                )
                                Text(
                                    text = "● Breakdown: ${bdnHoursStr}j ($bdnPctStr)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // ==================================================================
            // 3B. CARD DAFTAR UNIT OPERASIONAL / UNIT YANG DIKERJAKAN
            // ==================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_rangkuman_unit")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header Daftar Unit di Bawah: "UNIT YANG DIKERJAKAN" (Operator) atau "DAFTAR UNIT OPERASIONAL" (Kasie)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOperatorSession) "UNIT YANG DIKERJAKAN" else "DAFTAR UNIT OPERASIONAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "✓ ${kasieUnitItems.size} Unit Tervalidasi",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }

                        if (kasieUnitItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isOperatorSession) {
                                        "Belum ada unit yang dikerjakan oleh $rawDisplayName."
                                    } else {
                                        "Belum ada unit terdaftar untuk Kasie $areaOrKasieLabel."
                                    },
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                kasieUnitItems.forEach { item ->
                                    val hmStr = String.format(Locale.US, "%.1f", item.totalHm)
                                    val solarStr = if (item.totalSolar % 1.0 == 0.0) {
                                        item.totalSolar.toInt().toString()
                                    } else {
                                        String.format(Locale.US, "%.1f", item.totalSolar)
                                    }
                                    val rasioStr = String.format(Locale.US, "%.2f", item.rasioBbm)
                                    val hasilStr = if (item.totalHasil % 1.0 == 0.0) {
                                        item.totalHasil.toInt().toString()
                                    } else {
                                        String.format(Locale.US, "%.1f", item.totalHasil)
                                    }

                                    // Item Card per Unit (Surface putih mandiri, rounded 10dp, border 1dp #E2E8F0, shadowElevation 1.5dp, padding 12dp)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        shadowElevation = 1.5.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            // Baris 1: Kode Unit (Bold 12sp) + Badge Tervalidasi di kiri, Total HM (Bold #009663) di kanan
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.unitCode,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF0F172A),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Surface(
                                                        color = Color(0xFFE8F5E9),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "✓ Tervalidasi",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF2E7D32),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "+$hmStr HM",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF009663),
                                                    maxLines = 1
                                                )
                                            }

                                            // Baris 2: Nama Alat & Operator penanggung jawab
                                            Text(
                                                text = "${item.alatName} • Opr: ${item.operatorName}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF475569),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // Baris 3: Kotak Metrik Operasional (Micro-Pill Container #F1F5F9, rounded 6dp, border 1dp #E2E8F0)
                                            Surface(
                                                color = Color(0xFFF1F5F9),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Solar: $solarStr L",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF0F172A),
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = "Rasio: $rasioStr L/HM",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFD97706),
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = "Hasil: $hasilStr ${item.satuanHasil}",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF009663),
                                                        maxLines = 1
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

            item {
                Spacer(modifier = Modifier.height(8.dp))
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
}

/**
 * Kartu Skor KPI Simetris (Grid 2x2):
 * Surface putih bersih mandiri, rounded 10dp, border 1dp #E2E8F0, shadowElevation 1.5dp.
 */
@Composable
private fun CompactKpiScoreCard44(
    valueText: String,
    valueColor: Color,
    sublabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = valueText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sublabel,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 10.sp
            )
        }
    }
}

@Composable
fun KpiSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    CompactKpiScoreCard44(
        valueText = value,
        valueColor = iconColor,
        sublabel = title.uppercase(),
        modifier = modifier
    )
}

@Composable
fun TimeLegendItem(
    label: String,
    hours: Double,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: ${String.format(Locale.US, "%.1f", hours)}j",
            fontSize = 9.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusHoursMetricCard(
    title: String,
    hours: Double,
    subtitle: String,
    themeColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Slate700,
                maxLines = 1
            )
            Text(
                text = "${String.format(Locale.US, "%.1f", hours)} Jam",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = themeColor,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 8.sp,
                color = Slate500,
                maxLines = 1
            )
        }
    }
}
