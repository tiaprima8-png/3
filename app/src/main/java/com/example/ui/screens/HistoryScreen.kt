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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import com.example.ui.components.RkceBrandEmblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.ui.components.FullscreenPhotoPreviewDialog
import com.example.ui.components.ReportPhotoThumbnail
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.HeavyTrackViewModel
import com.example.ui.viewmodel.formatTanggalHeaderLengkap
import com.example.ui.viewmodel.getShiftWeight
import com.example.ui.viewmodel.normalizeDateToIso

/**
 * Representasi gabungan data komprehensif pekerjaan operasional selesai:
 * Memadukan Rencana Awal Penugasan dengan Hasil Realisasi Aktual Lapangan.
 */
data class CompletedOperationalRecord(
    val report: ActivityReportEntity,
    val matchedRencana: RencanaKerjaEntity?,
    val nomorSpk: String,
    val isSpkOfficial: Boolean,
    val aktivitasRencana: String,
    val aktivitasAktual: String,
    val sapCode: String,
    val lokasiRencana: String,
    val lokasiAktual: String,
    val operatorRencana: String,
    val operatorAktual: String,
    val supervisorName: String,
    val jamKerja: Double,
    val hmAwal: Double,
    val hmAkhir: Double,
    val hmRunning: Double,
    val konsumsiSolar: Double,
    val isRefueled: Boolean,
    val refuelLiters: Double,
    val kendalaNama: String?,
    val isSynced: Boolean,
    val reportDate: String,
    val shift: String,
    val unitStatus: String,
    val photoPath: String?,
    val photoBase64: String?,
    val photoSizeKb: Long?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HeavyTrackViewModel,
    onNavigateToForm: () -> Unit
) {
    val isManagerSession by viewModel.isManagerSession.collectAsState()
    val isOperatorSession by viewModel.isOperatorSession.collectAsState()
    val isKasieSession by viewModel.isKasieSession.collectAsState()
    val currentSupervisor by viewModel.currentSupervisorSession.collectAsState()
    val myReports by viewModel.reports.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val myRencanaList by viewModel.allRencanaList.collectAsState()
    val allRencanaGlobal by viewModel.allRencanaGlobal.collectAsState()
    val myUnits by viewModel.units.collectAsState()
    val allUnitsGlobal by viewModel.allUnitsGlobal.collectAsState()
    val selectedWilayah by viewModel.filterWilayah.collectAsState()
    val selectedJenisUnit by viewModel.filterJenisUnit.collectAsState()
    val selectedKodeUnit by viewModel.filterKodeUnit.collectAsState()
    val hasilAktivitasList by viewModel.hasilAktivitasList.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val isSyncingPending by viewModel.isSyncingPending.collectAsState()
    val activeSupervisor by viewModel.activeSupervisor.collectAsState()
    val customApkUrl by viewModel.customApkUrl.collectAsState()

    // Cakupan data: hanya Manager/Super Admin yang boleh melihat seluruh input lintas Kasie
    var showAllDataScope by remember(isManagerSession) { mutableStateOf(isManagerSession) }

    // Filter status sinkron: ALL, PENDING, SYNCED
    var selectedFilter by remember { mutableStateOf("ALL") }
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    // Pencarian cepat (Unit, Operator, Aktivitas, Lokasi, Kasie, Tanggal, SPK)
    var searchQuery by remember { mutableStateOf("") }

    var selectedRecordForDetail by remember { mutableStateOf<CompletedOperationalRecord?>(null) }
    var photoPreviewReport by remember { mutableStateOf<ActivityReportEntity?>(null) }
    var showApkDistributionDialog by remember { mutableStateOf(false) }
    var showConfirmLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val rawDisplayName = remember(currentSupervisor, activeSupervisor) {
        currentSupervisor?.namaLengkap?.takeIf { it.isNotBlank() }
            ?: currentSupervisor?.name?.takeIf { it.isNotBlank() }
            ?: activeSupervisor.takeIf { it.isNotBlank() }
            ?: "User"
    }

    val baseReports = if (isManagerSession && showAllDataScope) allReports else myReports
    val baseRencana = if (isManagerSession && showAllDataScope) allRencanaGlobal else myRencanaList
    val baseUnits = if (isManagerSession && showAllDataScope) allUnitsGlobal else myUnits

    // Rekapitulasi & pemaduan data komprehensif Rencana Awal + Realisasi Aktual (HANYA yang SUDAH DISETUJUI Kasie)
    val completedRecords = remember(baseReports, baseRencana, hasilAktivitasList) {
        val uniqueReports = baseReports.distinctBy { rep ->
            rep.nomorSpk.trim().takeIf { it.isNotBlank() }?.uppercase() ?: "ID-${rep.id}"
        }

        // Lacak ID rencana & SPK dari realisasi yang masih berstatus REVIU / belum disetujui Kasie agar TIDAK bocor ke Riwayat
        val unapprovedRencanaIds = mutableSetOf<Long>()
        val unapprovedSpks = mutableSetOf<String>()

        val realizedReports = uniqueReports.filter { rep ->
            val isApproved = rep.isLocked ||
                rep.approvalStatus.equals("Disetujui Kasie", ignoreCase = true) ||
                rep.approvalStatus.equals("Approved", ignoreCase = true) ||
                (rep.approvalStatus.contains("Disetujui", ignoreCase = true) &&
                    !rep.approvalStatus.contains("Menunggu", ignoreCase = true))

            if (!isApproved) {
                if (rep.idRencanaKerja > 0) unapprovedRencanaIds.add(rep.idRencanaKerja)
                if (rep.nomorSpk.isNotBlank()) unapprovedSpks.add(rep.nomorSpk.trim().uppercase())
                baseRencana.forEach { ren ->
                    if ((rep.idRencanaKerja > 0 && ren.id == rep.idRencanaKerja) ||
                        (rep.nomorSpk.isNotBlank() && ren.nomorSpk.equals(rep.nomorSpk, ignoreCase = true)) ||
                        (ren.kodeUnit.equals(rep.unitCode, ignoreCase = true) &&
                            ren.shiftKerja.equals(rep.shift, ignoreCase = true) &&
                            (ren.tanggalFormat.isBlank() || ren.tanggalFormat == rep.reportDate))
                    ) {
                        unapprovedRencanaIds.add(ren.id)
                        if (ren.nomorSpk.isNotBlank()) unapprovedSpks.add(ren.nomorSpk.trim().uppercase())
                    }
                }
            }
            isApproved || rep.hmAkhir > 0.0 || rep.hmDailyRunning > 0.0 || rep.workOutput > 0.0
        }

        val matchedRencanaIds = mutableSetOf<Long>()
        val matchedSpks = mutableSetOf<String>()

        val reportRecords = realizedReports.map { rep ->
            val matchedRencana = baseRencana.find { ren ->
                (rep.idRencanaKerja > 0 && ren.id == rep.idRencanaKerja) ||
                    (rep.nomorSpk.isNotBlank() && ren.nomorSpk.equals(rep.nomorSpk, ignoreCase = true))
            } ?: baseRencana.find { ren ->
                ren.isRealized && ren.kodeUnit.equals(rep.unitCode, ignoreCase = true) &&
                    ren.shiftKerja.equals(rep.shift, ignoreCase = true) &&
                    (ren.tanggalFormat.isBlank() || ren.tanggalFormat == rep.reportDate)
            } ?: baseRencana.find { ren ->
                ren.isRealized && ren.kodeUnit.equals(rep.unitCode, ignoreCase = true) &&
                    (ren.tanggalFormat.isBlank() || ren.tanggalFormat == rep.reportDate)
            }

            if (matchedRencana != null) {
                matchedRencanaIds.add(matchedRencana.id)
                if (matchedRencana.nomorSpk.isNotBlank()) {
                    matchedSpks.add(matchedRencana.nomorSpk.trim().uppercase())
                }
            }
            if (rep.nomorSpk.isNotBlank()) {
                matchedSpks.add(rep.nomorSpk.trim().uppercase())
            }

            val matchedHasil = hasilAktivitasList.find { has ->
                (rep.nomorSpk.isNotBlank() && has.nomorSpk.equals(rep.nomorSpk, ignoreCase = true)) ||
                    (rep.idRencanaKerja > 0 && has.rencanaId == rep.idRencanaKerja)
            } ?: hasilAktivitasList.find { has ->
                has.kodeUnit.equals(rep.unitCode, ignoreCase = true) &&
                    has.hmAkhir == rep.hmAkhir
            }

            val rawSpk = rep.nomorSpk.takeIf { it.isNotBlank() }
                ?: matchedHasil?.nomorSpk?.takeIf { it.isNotBlank() }
                ?: matchedRencana?.nomorSpk?.takeIf { it.isNotBlank() }
                ?: ""

            val cleanSpk = rawSpk.trim()
            val isOfficial = cleanSpk.isNotBlank() &&
                    !cleanSpk.equals("null", ignoreCase = true) &&
                    !cleanSpk.equals("SPK Menyusul", ignoreCase = true) &&
                    !cleanSpk.equals("Belum Terbit (Menunggu Admin)", ignoreCase = true) &&
                    cleanSpk != "-" &&
                    !cleanSpk.equals("Menunggu SPK", ignoreCase = true) &&
                    !cleanSpk.equals("MENUNGGU", ignoreCase = true)

            val displaySpk = if (isOfficial) cleanSpk else "SPK Menyusul"

            val aktRencana = matchedRencana?.namaAktivitas?.ifBlank { null } ?: rep.activityName
            val aktAktual = rep.activityName
            val sap = rep.sapCode.ifBlank { matchedRencana?.sapCode ?: "" }
            val lokRencana = matchedRencana?.kodeLokasi?.ifBlank { null } ?: rep.workLocation
            val lokAktual = rep.workLocation
            val opRencana = matchedRencana?.operator?.ifBlank { null } ?: rep.operatorName
            val opAktual = rep.operatorName

            val effStikAwal = if (rep.stikSolarAwal > 0.0) rep.stikSolarAwal else (matchedHasil?.stikSolarAwal ?: 0.0)
            val effStikAkhir = if (rep.stikSolarAkhir > 0.0) rep.stikSolarAkhir else (matchedHasil?.stikSolarAkhir ?: 0.0)
            val effFuelLiters = if (rep.fuelLiters > 0.0) rep.fuelLiters else (matchedHasil?.jumlahLiterSolar ?: 0.0)
            val effIsRefueled = rep.isRefueled || (matchedHasil?.isIsiSolar == true) || effFuelLiters > 0.0
            val effKendalaJson = rep.daftarKendalaJson?.takeIf { it.isNotBlank() } ?: matchedHasil?.daftarKendalaJson
            val effKendalaNama = rep.kendalaNama?.takeIf { it.isNotBlank() } ?: matchedHasil?.kendalaNama

            val solarKonsumsi = if (effStikAwal > 0.0 || effStikAkhir > 0.0) {
                ((effStikAwal + effFuelLiters) - effStikAkhir).coerceAtLeast(0.0)
            } else if (rep.pemakaianSolar > 0.0) {
                rep.pemakaianSolar
            } else if ((matchedHasil?.pemakaianSolar ?: 0.0) > 0.0) {
                matchedHasil!!.pemakaianSolar
            } else if (effFuelLiters > 0.0) {
                effFuelLiters
            } else {
                0.0
            }

            val effPhotoPath = rep.photoPath?.takeIf { it.isNotBlank() } ?: matchedHasil?.photoPath
            val effPhotoBase64 = rep.photoBase64?.takeIf { it.isNotBlank() } ?: matchedHasil?.photoBase64
            val effPhotoSizeKb = rep.photoSizeKb?.takeIf { it > 0L }
                ?: matchedHasil?.photoSizeKb?.takeIf { it > 0L }
                ?: if (!effPhotoBase64.isNullOrBlank() || !effPhotoPath.isNullOrBlank()) {
                    com.example.ui.components.resolvePhotoSizeKb(null, effPhotoPath, effPhotoBase64)
                } else null
            val effPhotoDimensions = rep.photoDimensions?.takeIf { it.isNotBlank() } ?: matchedHasil?.photoDimensions

            val enrichedReport = rep.copy(
                stikSolarAwal = effStikAwal,
                stikSolarAkhir = effStikAkhir,
                fuelLiters = effFuelLiters,
                isRefueled = effIsRefueled,
                pemakaianSolar = solarKonsumsi,
                daftarKendalaJson = effKendalaJson,
                kendalaNama = effKendalaNama,
                photoPath = effPhotoPath,
                photoBase64 = effPhotoBase64,
                photoSizeKb = effPhotoSizeKb,
                photoDimensions = effPhotoDimensions
            )

            CompletedOperationalRecord(
                report = enrichedReport,
                matchedRencana = matchedRencana,
                nomorSpk = displaySpk,
                isSpkOfficial = isOfficial,
                aktivitasRencana = aktRencana,
                aktivitasAktual = aktAktual,
                sapCode = sap,
                lokasiRencana = lokRencana,
                lokasiAktual = lokAktual,
                operatorRencana = opRencana,
                operatorAktual = opAktual,
                supervisorName = rep.supervisorName,
                jamKerja = rep.workDurationHours,
                hmAwal = rep.hmAwal,
                hmAkhir = rep.hmAkhir,
                hmRunning = rep.hmDailyRunning,
                konsumsiSolar = solarKonsumsi,
                isRefueled = effIsRefueled,
                refuelLiters = effFuelLiters,
                kendalaNama = effKendalaNama,
                isSynced = rep.isSynced,
                reportDate = rep.reportDate,
                shift = rep.shift,
                unitStatus = rep.unitStatus,
                photoPath = effPhotoPath,
                photoBase64 = effPhotoBase64,
                photoSizeKb = effPhotoSizeKb
            )
        }

        // Satukan hanya Rencana Kerja arsip yang eksplisit berstatus Approved/Disetujui dan TIDAK memiliki laporan berstatus REVIU
        val standaloneRencanaRecords = baseRencana.filter { ren ->
            val spkKey = ren.nomorSpk.trim().uppercase()
            val matchingHasil = hasilAktivitasList.find { has ->
                has.rencanaId == ren.id || (spkKey.isNotBlank() && has.nomorSpk.equals(spkKey, ignoreCase = true))
            }
            val isHasilApproved = matchingHasil != null && (
                matchingHasil.isLocked ||
                    matchingHasil.approvalStatus.equals("Disetujui Kasie", ignoreCase = true) ||
                    matchingHasil.approvalStatus.equals("Approved", ignoreCase = true)
            )
            !matchedRencanaIds.contains(ren.id) &&
                !unapprovedRencanaIds.contains(ren.id) &&
                (spkKey.isBlank() || (!matchedSpks.contains(spkKey) && !unapprovedSpks.contains(spkKey))) &&
                (isHasilApproved ||
                    ren.statusSpk.contains("Approved", ignoreCase = true) ||
                    ren.statusSpk.contains("Disetujui", ignoreCase = true) ||
                    ren.statusRealisasi.contains("Approved", ignoreCase = true) ||
                    ren.statusRealisasi.contains("Disetujui", ignoreCase = true))
        }.map { ren ->
            val cleanSpk = ren.nomorSpk.trim()
            val isOfficial = cleanSpk.isNotBlank() &&
                !cleanSpk.equals("null", ignoreCase = true) &&
                !cleanSpk.equals("SPK Menyusul", ignoreCase = true) &&
                cleanSpk != "-"
            val displaySpk = if (isOfficial) cleanSpk else "SPK Menyusul"
            val matchedUnitType = baseUnits.find { it.unitCode.equals(ren.kodeUnit, ignoreCase = true) }?.unitType ?: "HE"

            val matchingHasil = hasilAktivitasList.find { has ->
                has.rencanaId == ren.id || (cleanSpk.isNotBlank() && has.nomorSpk.equals(cleanSpk, ignoreCase = true))
            }
            val syntheticReport = ActivityReportEntity(
                id = -ren.id,
                reportDate = ren.tanggalFormat,
                supervisorName = ren.supervisorName.ifBlank { ren.kasie },
                namaKasie = ren.kasie.ifBlank { ren.supervisorName },
                unitCode = ren.kodeUnit,
                unitType = matchedUnitType,
                activityName = ren.namaAktivitas,
                sapCode = ren.sapCode,
                unitOfMeasure = ren.satuanAktivitas.ifBlank { "Ha" },
                operatorName = ren.operator.ifBlank { ren.namaOperator },
                workLocation = ren.kodeLokasi,
                wilayah = ren.wilayah ?: "PG1",
                shift = ren.shiftKerja,
                workDurationHours = matchingHasil?.jamKerja ?: 7.0,
                hmAwal = matchingHasil?.hmAwal ?: 0.0,
                hmAkhir = matchingHasil?.hmAkhir ?: 0.0,
                hmDailyRunning = matchingHasil?.hmHarianBerjalan ?: 0.0,
                workOutput = matchingHasil?.hasilKerja ?: 0.0,
                notes = matchingHasil?.keterangan ?: "",
                unitStatus = ren.statusUnit,
                isSynced = true,
                approvalStatus = "Disetujui Kasie",
                isLocked = true,
                nomorSpk = displaySpk,
                idRencanaKerja = ren.id,
                photoPath = matchingHasil?.photoPath,
                photoBase64 = matchingHasil?.photoBase64,
                photoSizeKb = matchingHasil?.photoSizeKb,
                photoDimensions = matchingHasil?.photoDimensions
            )

            CompletedOperationalRecord(
                report = syntheticReport,
                matchedRencana = ren,
                nomorSpk = displaySpk,
                isSpkOfficial = isOfficial,
                aktivitasRencana = ren.namaAktivitas,
                aktivitasAktual = ren.namaAktivitas,
                sapCode = ren.sapCode,
                lokasiRencana = ren.kodeLokasi,
                lokasiAktual = ren.kodeLokasi,
                operatorRencana = ren.operator.ifBlank { ren.namaOperator },
                operatorAktual = ren.operator.ifBlank { ren.namaOperator },
                supervisorName = ren.supervisorName.ifBlank { ren.kasie },
                jamKerja = matchingHasil?.jamKerja ?: 7.0,
                hmAwal = matchingHasil?.hmAwal ?: 0.0,
                hmAkhir = matchingHasil?.hmAkhir ?: 0.0,
                hmRunning = matchingHasil?.hmHarianBerjalan ?: 0.0,
                konsumsiSolar = matchingHasil?.pemakaianSolar ?: 0.0,
                isRefueled = matchingHasil?.isIsiSolar ?: false,
                refuelLiters = matchingHasil?.jumlahLiterSolar ?: 0.0,
                kendalaNama = matchingHasil?.kendalaNama,
                isSynced = true,
                reportDate = ren.tanggalFormat,
                shift = ren.shiftKerja,
                unitStatus = ren.statusUnit,
                photoPath = matchingHasil?.photoPath,
                photoBase64 = matchingHasil?.photoBase64,
                photoSizeKb = matchingHasil?.photoSizeKb
            )
        }

        (reportRecords + standaloneRencanaRecords).sortedWith(
            compareByDescending<CompletedOperationalRecord> { normalizeDateToIso(it.reportDate) }
                .thenBy { getShiftWeight(it.shift) }
                .thenBy { it.report.unitCode.trim().uppercase() }
                .thenByDescending { it.report.createdAt }
        )
    }

    // Filter berdasarkan query pencarian cepat, status sinkronisasi, dan filter eksekutif
    val filteredRecords = remember(completedRecords, selectedFilter, searchQuery, selectedWilayah, selectedJenisUnit, selectedKodeUnit) {
        completedRecords.filter { rec ->
            val matchWilayah = selectedWilayah == "Semua Wilayah" || selectedWilayah.isBlank() || rec.report.wilayah.equals(selectedWilayah, ignoreCase = true)
            val matchKode = selectedKodeUnit == "Semua Unit" || selectedKodeUnit.isBlank() || rec.report.unitCode.equals(selectedKodeUnit, ignoreCase = true)
            val matchJenis = selectedJenisUnit == "Semua Jenis" || selectedJenisUnit.isBlank() || viewModel.matchesJenisUnit(rec.report.unitType, selectedJenisUnit)
            val matchesFilter = when (selectedFilter) {
                "PENDING" -> !rec.isSynced
                "SYNCED" -> rec.isSynced
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                rec.report.unitCode.lowercase().contains(q) ||
                    rec.operatorAktual.lowercase().contains(q) ||
                    rec.operatorRencana.lowercase().contains(q) ||
                    rec.aktivitasAktual.lowercase().contains(q) ||
                    rec.lokasiAktual.lowercase().contains(q) ||
                    rec.nomorSpk.lowercase().contains(q) ||
                    rec.supervisorName.lowercase().contains(q) ||
                    rec.reportDate.lowercase().contains(q) ||
                    rec.report.wilayah.lowercase().contains(q) ||
                    rec.report.unitType.lowercase().contains(q)
            }
            matchWilayah && matchKode && matchJenis && matchesFilter && matchesQuery
        }
    }

    val groupedRecords = remember(filteredRecords) {
        filteredRecords.groupBy { normalizeDateToIso(it.reportDate) }
    }

    val totalHm = remember(completedRecords) {
        completedRecords.sumOf { it.hmRunning }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("history_screen")
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
                .testTag("rkce_history_topbar")
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
                        onClick = {
                            if (unsyncedCount > 0) {
                                viewModel.syncPendingReports(context)
                            } else {
                                viewModel.forceSyncWithServer()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF163E9B), CircleShape)
                            .testTag("sync_pending_reports_button")
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
                            .testTag("btn_history_unduh_apk")
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
                            .testTag("btn_history_keluar")
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
        // 2. OMNI-SEARCH & FILTER TANPA TOMBOL SPLIT TAB
        // ======================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("card_history_filter"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kolom Cari: Surface setinggi 42dp dengan Modifier.weight(1f), shape RoundedCornerShape(10dp), border 1dp #E2E8F0, shadowElevation 1.5dp
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.5.dp
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                        .testTag("search_history_input"),
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
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari riwayat unit, SPK, tanggal...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Pencarian",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }
                )
            }

            // Tombol Filter: Surface kotak ukuran tepat 42dp x 42dp di kanan (shape RoundedCornerShape(10dp), border 1dp #E2E8F0, shadowElevation 1.5dp)
            val hasActiveFilter = selectedFilter != "ALL" || (isManagerSession && !showAllDataScope)
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isFilterSheetOpen = true }
                    .testTag("btn_history_filter_42dp"),
                shape = RoundedCornerShape(10.dp),
                color = if (hasActiveFilter) Color(0xFFE8F5E9) else Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.5.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter Riwayat",
                        tint = if (hasActiveFilter) Color(0xFF009663) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ======================================================================
        // 2B. KPI STRIP TERPADU (Setinggi ~44dp tepat di bawah search bar)
        // ======================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("card_history_header")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kolom 1 (weight 1f): Teks nilai Total HM (13sp Bold Emerald #009663) dan sublabel "TOTAL OPERASIONAL" (8sp Bold #64748B)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", totalHm)} HM",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF009663),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "TOTAL OPERASIONAL",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 10.sp
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = Color(0xFFE2E8F0)
                    )

                    // Kolom 2 (weight 1f, padding horizontal 8dp): Jumlah Arsip (13sp Bold #0F172A) dan sublabel "RENCANA & REALISASI" (8sp Bold #64748B)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${completedRecords.size} Arsip",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "RENCANA & REALISASI",
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
        }

        Spacer(modifier = Modifier.height(2.dp))

        // ======================================================================
        // 3. GRID 2 KOLOM KARTU RIWAYAT TERPADU (UKURAN PRESISI)
        // ======================================================================
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("history_unified_grid"),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredRecords.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    EmptyHistoryPlaceholder(
                        title = if (searchQuery.isNotBlank() || selectedFilter != "ALL") "Data Tidak Ditemukan" else "Belum Ada Arsip Riwayat",
                        message = if (searchQuery.isNotBlank()) {
                            "Tidak ada riwayat rencana atau realisasi yang cocok dengan pencarian '$searchQuery'."
                        } else if (isManagerSession) {
                            "Belum ada arsip rencana & realisasi kerja selesai yang disubmit oleh pengawas lapangan."
                        } else {
                            "Pekerjaan yang telah diselesaikan dan disetujui dari tab Formulir akan otomatis masuk ke arsip ini."
                        },
                        buttonText = if (isManagerSession) "Segarkan Data" else "Buka Tab Formulir",
                        onButtonClick = if (isManagerSession) {
                            { viewModel.refreshAnalyticsData(context) }
                        } else {
                            onNavigateToForm
                        }
                    )
                }
            } else {
                groupedRecords.forEach { (dateIso, recordsForDate) ->
                    item(
                        key = "history_date_header_$dateIso",
                        span = { GridItemSpan(2) }
                    ) {
                        val groupHm = recordsForDate.sumOf { it.hmRunning }
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF002677),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = formatTanggalHeaderLengkap(dateIso),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                                Surface(
                                    color = Color(0xFFE0E7FF),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${recordsForDate.size} Arsip • ${String.format(Locale.US, "%.1f", groupHm)} HM",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(
                        items = recordsForDate,
                        key = { rec -> "${rec.report.id}_${rec.nomorSpk}" }
                    ) { record ->
                        RkceUnifiedHistoryCard(
                            record = record,
                            onClick = { selectedRecordForDetail = record }
                        )
                    }
                }
            }
        }
    }

    // ======================================================================
    // MODAL FILTER RIWAYAT (TERPANGGIL SAAT TOMBOL FILTER 42x42 DIKLIK)
    // ======================================================================
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
                        text = "Filter Arsip Riwayat",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    TextButton(
                        onClick = {
                            selectedFilter = "ALL"
                            if (isManagerSession) showAllDataScope = true
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

                Text(
                    text = "Status Sinkronisasi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "Semua",
                        "SYNCED" to "✓ Tersinkron",
                        "PENDING" to "Tertunda"
                    ).forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        Button(
                            onClick = { selectedFilter = key },
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
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (isManagerSession) {
                    Text(
                        text = "Cakupan Pengawas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            true to "Semua Input",
                            false to "Hanya Saya"
                        ).forEach { (scopeAll, label) ->
                            val isSelected = showAllDataScope == scopeAll
                            Button(
                                onClick = { showAllDataScope = scopeAll },
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
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
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

    // ======================================================================
    // 4. POP-UP / MODAL DETAIL RIWAYAT AUDIT (SAAT KARTU ATAU "DETAIL >" DIKLIK)
    // ======================================================================
    selectedRecordForDetail?.let { rec ->
        RkceAuditDetailDialog(
            record = rec,
            onDismiss = { selectedRecordForDetail = null },
            onPhotoClick = { photoPreviewReport = rec.report },
            onShareClick = {
                shareReportDetails(context, rec.report, rec.nomorSpk)
            }
        )
    }

    // Fullscreen Photo Preview
    photoPreviewReport?.let { rep ->
        FullscreenPhotoPreviewDialog(
            photoPath = rep.photoPath,
            photoBase64 = rep.photoBase64,
            unitCode = rep.unitCode,
            reportDate = rep.reportDate,
            nomorSpk = rep.nomorSpk.takeIf { it.isNotBlank() && it != "SPK Menyusul" && it != "-" },
            photoSizeKb = rep.photoSizeKb,
            onDismiss = { photoPreviewReport = null }
        )
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
 * Kartu Riwayat Terpadu 2-Kolom (Ukuran Presisi - Enterprise RKCE):
 * - Surface putih, shape RoundedCornerShape(8dp), border 1dp #E2E8F0, klik kartu membuka Pop-up Detail.
 * - Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)):
 *   1. Baris 1 (Header Kartu): Kiri Kode Unit (13sp Bold #0F172A, maxLines 1), Kanan Tanggal ringkas (10sp SemiBold #64748B).
 *   2. Baris 2 (Aktivitas & SPK): Kiri (weight 1f) Nama Aktivitas (11sp Bold #0F172A, maxLines 1, Ellipsis), Kanan Nomor SPK pendek / 6 karakter terakhir (8sp Regular #94A3B8).
 *   3. Baris 3 (Operator & Lokasi): Teks "[Nama Operator] • [Lokasi]" (9sp Regular #64748B, maxLines 1, Ellipsis).
 *   4. Baris 4 (Kotak Metrik Terpadu): Surface(#F8FAFC, border 1dp #E2E8F0, rounded 4dp, fillMaxWidth):
 *      Teks satu baris "+[TotalHM] HM" (10sp Bold #009663) diikuti " • [Solar] L • [Hasil] [Satuan]" (9sp SemiBold #0F172A) dengan padding horizontal 6dp, vertical 4dp.
 *   5. Baris 5 (Footer Kartu): Kiri indikator status "✓ Tervalidasi" (8sp Bold #2E7D32) atau status sinkronisasi; Kanan Surface tombol mini "Detail >" (tinggi 20dp, padding horizontal 6dp, border 1dp #E2E8F0, rounded 4dp, teks 8sp Bold #64748B).
 */
@Composable
fun RkceUnifiedHistoryCard(
    record: CompletedOperationalRecord,
    onClick: () -> Unit
) {
    val tanggalRingkas = formatTanggalSingkat(record.reportDate)
    val aktivitasDisplay = record.aktivitasAktual.ifBlank { record.aktivitasRencana.ifBlank { "Operasional" } }
    val spkPendek = if (record.isSpkOfficial) {
        record.nomorSpk.takeLast(6)
    } else {
        "SPK-"
    }
    val operatorDisplay = record.operatorAktual.ifBlank { record.operatorRencana }.ifBlank { "-" }.uppercase()
    val lokasiDisplay = record.lokasiAktual.ifBlank { record.lokasiRencana }.ifBlank { "-" }.uppercase()
    val shiftDisplay = record.shift.ifBlank { record.matchedRencana?.shiftKerja ?: "SIANG" }.uppercase()

    val hmStr = String.format(Locale.US, "%.1f", record.hmRunning)
    val solarStr = if (record.konsumsiSolar % 1.0 == 0.0) {
        record.konsumsiSolar.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", record.konsumsiSolar)
    }
    val hasilVal = record.report.workOutput
    val hasilStr = if (hasilVal % 1.0 == 0.0) {
        hasilVal.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", hasilVal)
    }
    val satuanStr = record.report.unitOfMeasure.ifBlank { "Ha" }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("completed_record_card_${record.report.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // 1. Baris 1 (Header Kartu): Kiri Kode Unit (13sp Bold #0F172A, maxLines 1), Kanan Tanggal ringkas (10sp SemiBold #64748B)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.report.unitCode,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tanggalRingkas,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }

            // 2. Baris 2 (Aktivitas & SPK): Kiri (weight 1f) Nama Aktivitas (11sp Bold #0F172A, maxLines 1, Ellipsis), Kanan Nomor SPK pendek / 6 karakter terakhir (8sp Regular #94A3B8)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = aktivitasDisplay,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = spkPendek,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }

            // 3. Baris 3 (Operator, Lokasi & Shift): "[NAMA OPERATOR] • [LOKASI/BLOK] • [SHIFT]" (9sp #64748B, maxLines = 1, Ellipsis)
            Text(
                text = "$operatorDisplay • $lokasiDisplay • $shiftDisplay",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 4. Baris 4 (Kotak Metrik Operasional Micro-Pill): Surface(#F1F5F9, border = 1dp #E2E8F0, shape = RoundedCornerShape(6dp), fillMaxWidth)
            Surface(
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF009663)
                            )
                        ) {
                            append("+$hmStr HM")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        ) {
                            append(" • $solarStr L • $hasilStr $satuanStr")
                        }
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                )
            }

            // 5. Baris 5 (Footer Kartu): Kiri badge status 2 kondisi ("✓ Tervalidasi" hijau vs "✕ Belum Tervalidasi" merah), Kanan Surface tombol mini "Detail >"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isApproved = record.report.isLocked ||
                    record.report.approvalStatus.equals("Disetujui Kasie", ignoreCase = true) ||
                    record.report.approvalStatus.equals("Approved", ignoreCase = true) ||
                    (record.report.approvalStatus.contains("Disetujui", ignoreCase = true) &&
                        !record.report.approvalStatus.contains("Menunggu", ignoreCase = true))

                Surface(
                    color = if (isApproved) Color(0xFFE8F5E9) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isApproved) "✓ Tervalidasi" else "✕ Belum Tervalidasi",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isApproved) Color(0xFF2E7D32) else Color(0xFFDC2626),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onClick() }
                        .testTag("btn_detail_audit_${record.report.id}"),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = "Detail >",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4. POP-UP / MODAL DETAIL RIWAYAT AUDIT (SAAT "DETAIL >" ATAU KARTU DIKLIK)
 * Merangkum gabungan data Rencana Kerja & Hasil Realisasi item tersebut:
 * - Header Modal: Kode Unit (16sp Bold), Badge Shift (Siang/Malam), Badge Status ("✓ Tervalidasi"), dan tombol Tutup (X).
 *   Subheader: Nomor SPK Lengkap & Tanggal Pelaksanaan (11sp #64748B).
 * - Seksi 1 - Informasi Penugasan (Surface #F8FAFC, rounded 8dp, padding 10dp):
 *   Baris data rapi (Label di kiri, Nilai Bold di kanan): Nama Aktivitas & Kode SAP, Lokasi Blok, Kasie Penanggung Jawab, dan Nama Operator.
 * - Seksi 2 - Realisasi Operasional & BBM (Grid 3 Kotak Sejajar):
 *   Kotak 1 (HM Alat), Kotak 2 (Konsumsi Solar), Kotak 3 (Capaian Hasil).
 * - Seksi 3 - Catatan & Dokumentasi:
 *   Catatan lapangan / keterangan koreksi dan preview foto bukti kerja.
 * - Footer Modal:
 *   Tombol "Bagikan Laporan" (OutlinedButton) dan Tombol solid hijau "Tutup" (tinggi 40dp).
 */
@Composable
fun RkceAuditDetailDialog(
    record: CompletedOperationalRecord,
    onDismiss: () -> Unit,
    onPhotoClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val isSiang = record.shift.equals("SIANG", ignoreCase = true)
    val shiftLabel = if (isSiang) "Shift Siang" else "Shift Malam"
    val spkFull = if (record.isSpkOfficial) record.nomorSpk else "SPK Menyusul"
    val tanggalFull = formatTanggalIndonesia(record.reportDate)
    val aktivitasWithSap = buildString {
        append(record.aktivitasAktual.ifBlank { record.aktivitasRencana.ifBlank { "-" } })
        if (record.sapCode.isNotBlank()) {
            append(" (${record.sapCode})")
        }
    }
    val lokasiVal = record.lokasiAktual.ifBlank { record.lokasiRencana.ifBlank { "-" } }
    val kasieVal = record.supervisorName.ifBlank { record.report.namaKasie.ifBlank { "-" } }
    val operatorVal = record.operatorAktual.ifBlank { record.operatorRencana.ifBlank { "-" } }

    val hmTotalStr = String.format(Locale.US, "+%.1f HM", record.hmRunning)
    val hmAwalStr = String.format(Locale.US, "%.1f", record.hmAwal)
    val hmAkhirStr = String.format(Locale.US, "%.1f", record.hmAkhir)

    val solarTotalStr = String.format(Locale.US, "%.1f L", record.konsumsiSolar)
    val solarDetailStr = when {
        record.report.stikSolarAwal > 0.0 || record.report.stikSolarAkhir > 0.0 -> {
            val sAwal = if (record.report.stikSolarAwal % 1.0 == 0.0) record.report.stikSolarAwal.toInt().toString() else String.format(Locale.US, "%.1f", record.report.stikSolarAwal)
            val sAkhir = if (record.report.stikSolarAkhir % 1.0 == 0.0) record.report.stikSolarAkhir.toInt().toString() else String.format(Locale.US, "%.1f", record.report.stikSolarAkhir)
            val isiStr = if (record.refuelLiters > 0.0) {
                val lStr = if (record.refuelLiters % 1.0 == 0.0) record.refuelLiters.toInt().toString() else String.format(Locale.US, "%.1f", record.refuelLiters)
                " (+${lStr}L)"
            } else ""
            "Stik: $sAwal -> $sAkhir$isiStr"
        }
        record.isRefueled && record.refuelLiters > 0.0 -> {
            "Isi: +${String.format(Locale.US, "%.0f", record.refuelLiters)} L"
        }
        else -> "Tanpa Isi BBM"
    }

    val hasilVal = record.report.workOutput
    val hasilStr = if (hasilVal % 1.0 == 0.0) {
        hasilVal.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", hasilVal)
    }
    val satuanStr = record.report.unitOfMeasure.ifBlank { "Ha" }
    val jamKerjaStr = String.format(Locale.US, "%.1f Jam Kerja", record.jamKerja)

    val parsedKendalaItems = com.example.data.remote.KendalaJsonConverter.fromJson(record.report.daftarKendalaJson)
    val catatanGabungan = buildList {
        if (record.report.notes.isNotBlank()) add(record.report.notes)
        if (!record.report.alasanKoreksiHm.isNullOrBlank()) add("Koreksi HM: ${record.report.alasanKoreksiHm}")
        if (parsedKendalaItems.isNotEmpty()) {
            parsedKendalaItems.forEach { k ->
                val code = k.resolvedKode
                val name = k.resolvedNama
                val dur = if (k.resolvedDurasiJam > 0.0) " (${k.resolvedDurasiJam} Jam)" else ""
                add("Kendala: $name [$code] (${k.resolvedJamMulai}-${k.resolvedJamSelesai})$dur")
            }
        } else if (!record.kendalaNama.isNullOrBlank()) {
            val det = record.report.catatanKendala?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
            add("Kendala: ${record.kendalaNama}$det")
        }
    }.joinToString("\n")

    val resolvedDialogPhotoPath = record.photoPath?.takeIf { it.isNotBlank() } ?: record.report.photoPath
    val resolvedDialogPhotoBase64 = record.photoBase64?.takeIf { it.isNotBlank() } ?: record.report.photoBase64
    val resolvedDialogPhotoSizeKb = record.photoSizeKb ?: record.report.photoSizeKb
    val hasPhoto = !resolvedDialogPhotoPath.isNullOrBlank() || !resolvedDialogPhotoBase64.isNullOrBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("rkce_audit_detail_modal"),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // HEADER MODAL
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            Text(
                                text = record.report.unitCode,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            // Badge Shift (Siang/Malam)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSiang) Color(0xFFEFF6FF) else Color(0xFFF3E8FF),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSiang) Color(0xFFBFDBFE) else Color(0xFFE9D5FF)
                                )
                            ) {
                                Text(
                                    text = shiftLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSiang) Color(0xFF1D4ED8) else Color(0xFF6B21A8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Badge Status ("✓ Tervalidasi" vs "✕ Belum Tervalidasi")
                            val isModalApproved = record.report.isLocked ||
                                record.report.approvalStatus.equals("Disetujui Kasie", ignoreCase = true) ||
                                record.report.approvalStatus.equals("Approved", ignoreCase = true) ||
                                (record.report.approvalStatus.contains("Disetujui", ignoreCase = true) &&
                                    !record.report.approvalStatus.contains("Menunggu", ignoreCase = true))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isModalApproved) Color(0xFFE8F5E9) else Color(0xFFFEE2E2),
                                border = BorderStroke(1.dp, if (isModalApproved) Color(0xFFA7F3D0) else Color(0xFFFECACA))
                            ) {
                                Text(
                                    text = if (isModalApproved) "✓ Tervalidasi" else "✕ Belum Tervalidasi",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isModalApproved) Color(0xFF2E7D32) else Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_close_audit_modal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Subheader: Nomor SPK Lengkap & Tanggal Pelaksanaan (11sp #64748B)
                    Text(
                        text = "SPK: $spkFull  •  Tanggal: $tanggalFull",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                // SEKSI 1 - INFORMASI PENUGASAN (Surface #F8FAFC, rounded 8dp, padding 10dp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "INFORMASI PENUGASAN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AuditInfoRow(label = "Aktivitas & SAP", value = aktivitasWithSap)
                            AuditInfoRow(label = "Lokasi Blok", value = lokasiVal)
                            AuditInfoRow(label = "Kasie Penanggung Jawab", value = kasieVal)
                            AuditInfoRow(label = "Nama Operator", value = operatorVal)
                        }
                    }
                }

                // SEKSI 2 - REALISASI OPERASIONAL & BBM (Grid 3 Kotak Sejajar)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "REALISASI OPERASIONAL & BBM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Kotak 1 (HM Alat): Total HM (+X.X HM Bold Hijau) dan rincian "Awal: [hmAwal] ➔ Akhir: [hmAkhir]"
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "HM Alat",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    maxLines = 1
                                )
                                Text(
                                    text = hmTotalStr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF009663),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Awal: $hmAwalStr ➔ Akhir: $hmAkhirStr",
                                    fontSize = 8.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 2,
                                    lineHeight = 10.sp
                                )
                            }
                        }

                        // Kotak 2 (Konsumsi Solar): Total Liter (X.X L Bold) dan rincian stik/pengisian solar
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Konsumsi Solar",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    maxLines = 1
                                )
                                Text(
                                    text = solarTotalStr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = solarDetailStr,
                                    fontSize = 8.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 2,
                                    lineHeight = 10.sp
                                )
                            }
                        }

                        // Kotak 3 (Capaian Hasil): Volume Hasil Kerja ([hasil] [satuan] Bold) dan jam kerja operasional
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Capaian Hasil",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    maxLines = 1
                                )
                                Text(
                                    text = "$hasilStr $satuanStr",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = jamKerjaStr,
                                    fontSize = 8.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 2,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                }

                // SEKSI 3 - CATATAN & DOKUMENTASI
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CATATAN & DOKUMENTASI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasPhoto) {
                                ReportPhotoThumbnail(
                                    photoPath = resolvedDialogPhotoPath,
                                    photoBase64 = resolvedDialogPhotoBase64,
                                    photoSizeKb = resolvedDialogPhotoSizeKb,
                                    onClick = onPhotoClick
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (catatanGabungan.isNotBlank()) {
                                        catatanGabungan
                                    } else {
                                        "Tidak ada catatan kendala atau koreksi khusus pada penugasan ini."
                                    },
                                    fontSize = 11.sp,
                                    color = if (catatanGabungan.isNotBlank()) Color(0xFF0F172A) else Color(0xFF64748B),
                                    lineHeight = 15.sp
                                )
                                if (hasPhoto) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Ketuk foto untuk memperbesar dokumentasi kerja",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF009663)
                                    )
                                }
                            }
                        }
                    }
                }

                // FOOTER MODAL: Tombol "Bagikan Laporan" (OutlinedButton) dan Tombol solid hijau "Tutup" (tinggi 40dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_share_audit_modal"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF009663)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF009663)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan Laporan",
                            tint = Color(0xFF009663),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bagikan Laporan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF009663),
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_tutup_audit_modal"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF009663),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Tutup",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
    }
}

/**
 * Komponen kompatibilitas kartu riwayat (mendelegasikan ke RkceUnifiedHistoryCard tanpa tombol Koreksi/Setujui).
 */
@Composable
fun CompletedOperationCard(
    record: CompletedOperationalRecord,
    isKasieSession: Boolean = false,
    onClick: () -> Unit,
    onPhotoClick: () -> Unit,
    onApproveClick: (() -> Unit)? = null,
    onKoreksiClick: (() -> Unit)? = null
) {
    RkceUnifiedHistoryCard(
        record = record,
        onClick = onClick
    )
}

@Composable
fun EmptyHistoryPlaceholder(
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate500,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009663)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Kartu Rencana Kerja Lapangan untuk Monitoring Eksekutif (Read-Only)
 */
@Composable
fun ExecutiveRencanaCard(
    rencana: RencanaKerjaEntity
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rencana_card_${rencana.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Emerald50, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = rencana.kodeUnit,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Kasie: ${rencana.supervisorName.ifBlank { "Semua" }} • Shift ${rencana.shiftKerja}",
                            fontSize = 10.sp,
                            color = Slate500
                        )
                    }
                }

                val isRealized = rencana.isRealized ||
                    rencana.statusSpk.contains("Terealisasi", ignoreCase = true) ||
                    rencana.statusSpk.contains("Approved", ignoreCase = true) ||
                    rencana.statusRealisasi.contains("Terealisasi", ignoreCase = true) ||
                    rencana.statusRealisasi.contains("Approved", ignoreCase = true) ||
                    rencana.statusRealisasi.equals("SUDAH", ignoreCase = true) ||
                    rencana.statusTahap.equals("REALISASI_SELESAI", ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isRealized) Emerald100 else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (isRealized) "✔ Terealisasi" else "Rencana Aktif",
                        color = if (isRealized) Emerald900 else Color(0xFF92400E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate200, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Engineering, contentDescription = null, tint = Emerald600, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = rencana.namaAktivitas.ifBlank { "Aktivitas Standar" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Lokasi: ${rencana.kodeLokasi}", fontSize = 10.sp, color = Slate700)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Operator: ${rencana.operator}", fontSize = 10.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
