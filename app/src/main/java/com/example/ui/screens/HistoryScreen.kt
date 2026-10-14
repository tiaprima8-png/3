package com.example.ui.screens

import java.util.Locale
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.ui.components.ExecutiveFilterPanel
import com.example.ui.components.FuelBadge
import com.example.ui.components.FullscreenPhotoPreviewDialog
import com.example.ui.components.ReportPhotoThumbnail
import com.example.ui.components.ShiftBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.UnitStatusBadge
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmber50
import com.example.ui.viewmodel.HeavyTrackViewModel

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

@Composable
fun HistoryScreen(
    viewModel: HeavyTrackViewModel,
    onNavigateToForm: () -> Unit
) {
    val isManagerSession by viewModel.isManagerSession.collectAsState()
    val myReports by viewModel.reports.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val allRencanaGlobal by viewModel.allRencanaGlobal.collectAsState()
    val allUnitsGlobal by viewModel.allUnitsGlobal.collectAsState()
    val selectedWilayah by viewModel.filterWilayah.collectAsState()
    val selectedJenisUnit by viewModel.filterJenisUnit.collectAsState()
    val selectedKodeUnit by viewModel.filterKodeUnit.collectAsState()
    val hasilAktivitasList by viewModel.hasilAktivitasList.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val isSyncingPending by viewModel.isSyncingPending.collectAsState()
    val activeSupervisor by viewModel.activeSupervisor.collectAsState()

    // Sub-tab: "HASIL" vs "RENCANA" untuk monitoring menyeluruh oleh Manager
    var historySubTab by remember { mutableStateOf("HASIL") }

    // Cakupan data: true = Semua Data Terinput Lapangan, false = Hanya Kasie Aktif
    var showAllDataScope by remember(isManagerSession) { mutableStateOf(isManagerSession || true) }

    // Filter status sinkron: ALL, PENDING, SYNCED
    var selectedFilter by remember { mutableStateOf("ALL") }

    // Pencarian cepat (Unit, Operator, Aktivitas, Lokasi, Kasie, Tanggal)
    var searchQuery by remember { mutableStateOf("") }

    var selectedRecordForDetail by remember { mutableStateOf<CompletedOperationalRecord?>(null) }
    var photoPreviewReport by remember { mutableStateOf<ActivityReportEntity?>(null) }

    val baseReports = if (showAllDataScope || isManagerSession) allReports else myReports

    // Filter Rencana Kerja Lapangan berdasarkan filter eksekutif dan pencarian cepat
    val filteredRencana = remember(allRencanaGlobal, searchQuery, selectedWilayah, selectedJenisUnit, selectedKodeUnit, allUnitsGlobal) {
        allRencanaGlobal.filter { ren ->
            val matchWilayah = selectedWilayah == "Semua Wilayah" || selectedWilayah.isBlank() || ren.wilayah.equals(selectedWilayah, ignoreCase = true)
            val matchKode = selectedKodeUnit == "Semua Unit" || selectedKodeUnit.isBlank() || ren.kodeUnit.equals(selectedKodeUnit, ignoreCase = true)
            val matchJenis = if (selectedJenisUnit == "Semua Jenis" || selectedJenisUnit.isBlank()) {
                true
            } else {
                val matchedUnit = allUnitsGlobal.find { it.unitCode.equals(ren.kodeUnit, ignoreCase = true) }
                matchedUnit != null && viewModel.matchesJenisUnit(matchedUnit.unitType, selectedJenisUnit)
            }
            val matchQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                ren.kodeUnit.lowercase().contains(q) ||
                        ren.operator.lowercase().contains(q) ||
                        ren.namaAktivitas.lowercase().contains(q) ||
                        ren.kodeLokasi.lowercase().contains(q) ||
                        ren.nomorSpk.lowercase().contains(q) ||
                        ren.supervisorName.lowercase().contains(q) ||
                        ren.tanggalFormat.lowercase().contains(q)
            }
            matchWilayah && matchKode && matchJenis && matchQuery
        }
    }

    // Rekapitulasi & pemaduan data komprehensif Rencana Awal + Realisasi Aktual
    val completedRecords = remember(baseReports, allRencanaGlobal, hasilAktivitasList) {
        baseReports.map { rep ->
            // Temukan rencana kerja terkait di SQLite
            val matchedRencana = allRencanaGlobal.find { ren ->
                ren.isRealized && ren.kodeUnit.equals(rep.unitCode, ignoreCase = true) &&
                        (ren.tanggalFormat.isBlank() || ren.tanggalFormat == rep.reportDate)
            } ?: allRencanaGlobal.find { ren ->
                ren.kodeUnit.equals(rep.unitCode, ignoreCase = true) && ren.isRealized
            } ?: allRencanaGlobal.find { ren ->
                ren.kodeUnit.equals(rep.unitCode, ignoreCase = true)
            }

            // Temukan entri hasil aktivitas terkait
            val matchedHasil = hasilAktivitasList.find { has ->
                has.kodeUnit.equals(rep.unitCode, ignoreCase = true) &&
                        has.hmAkhir == rep.hmAkhir
            }

            val rawSpk = matchedHasil?.nomorSpk?.takeIf { it.isNotBlank() }
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

            val solarKonsumsi = if (rep.pemakaianSolar > 0.0) {
                rep.pemakaianSolar
            } else if (rep.isRefueled && rep.fuelLiters > 0.0) {
                rep.fuelLiters
            } else {
                0.0
            }

            CompletedOperationalRecord(
                report = rep,
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
                isRefueled = rep.isRefueled,
                refuelLiters = rep.fuelLiters,
                kendalaNama = rep.kendalaNama,
                isSynced = rep.isSynced,
                reportDate = rep.reportDate,
                shift = rep.shift,
                unitStatus = rep.unitStatus,
                photoPath = rep.photoPath,
                photoBase64 = rep.photoBase64,
                photoSizeKb = rep.photoSizeKb
            )
        }
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
                        rec.reportDate.lowercase().contains(q)
            }
            matchWilayah && matchKode && matchJenis && matchesFilter && matchesQuery
        }
    }

    val totalHm = remember(completedRecords) {
        completedRecords.sumOf { it.hmRunning }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("history_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. HEADER CARD: RINGKASAN & STATISTIK ARSIP
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_history_header"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {


                    // Stats Grid Ringkas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total HM Berjalan", style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp))
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", totalHm)} Jam",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Emerald700)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (unsyncedCount > 0) WarningAmber50 else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (unsyncedCount > 0) WarningAmber.copy(alpha = 0.5f) else Slate200)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Tertunda Sync", style = MaterialTheme.typography.bodySmall.copy(color = if (unsyncedCount > 0) Color(0xFFB45309) else Slate500, fontSize = 11.sp))
                                Text(
                                    text = "$unsyncedCount Laporan",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (unsyncedCount > 0) Color(0xFFB45309) else Slate900
                                    )
                                )
                            }
                        }
                    }

                    // Tombol Sinkronisasi Cepat ke Server
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (unsyncedCount > 0) {
                                viewModel.syncPendingReports()
                            } else {
                                viewModel.refreshAnalyticsData()
                            }
                        },
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (unsyncedCount > 0) Emerald600 else Color(0xFF0F766E),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("sync_pending_reports_button")
                    ) {
                        if (isSyncingPending) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menyinkronkan ke Web Admin...", fontSize = 13.sp)
                        } else if (unsyncedCount > 0) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sinkronkan $unsyncedCount Laporan Tertunda", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF6EE7B7), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isManagerSession) "Tarik Pembaruan Lapangan (Live)" else "Semua Laporan Tersinkron Lengkap", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate200, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Sub-tab: Hasil Kerja (Realisasi) vs Rencana Kerja Lapangan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (historySubTab == "HASIL") Emerald600 else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (historySubTab == "HASIL") Emerald600 else Slate200),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { historySubTab = "HASIL" }
                                .testTag("tab_hasil_kerja")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "Hasil Realisasi (${completedRecords.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (historySubTab == "HASIL") Color.White else Slate700
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (historySubTab == "RENCANA") Emerald600 else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (historySubTab == "RENCANA") Emerald600 else Slate200),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { historySubTab = "RENCANA" }
                                .testTag("tab_rencana_kerja")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "Rencana Kerja (${allRencanaGlobal.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (historySubTab == "RENCANA") Color.White else Slate700
                                )
                            }
                        }
                    }
                }
            }
        }

        // PANEL FILTER EKSEKUTIF (Wilayah, Jenis Unit, Kode Unit)
        if (isManagerSession) {
            item {
                ExecutiveFilterPanel(
                    viewModel = viewModel,
                    isManagerSession = true
                )
            }
        }

        // ==========================================
        // 2. SEARCH & FILTER KONTROL CEPAT
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth().testTag("card_history_filter")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari unit, operator, aktivitas, lokasi, SPK...", fontSize = 12.sp, color = Slate400) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = Slate500, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Slate500, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("search_history_input")
                    )

                    // Filter Bar: Status Sinkron & Cakupan Pengawas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status Sinkron Filter Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "ALL" to "Semua",
                                "SYNCED" to "✓",
                                "PENDING" to "Tertunda"
                            ).forEach { (key, label) ->
                                val isSelected = selectedFilter == key
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) Emerald600 else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Emerald600 else Slate200),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedFilter = key }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Slate700,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Toggle Cakupan Data
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (showAllDataScope) Emerald50 else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (showAllDataScope) Emerald200 else Slate200),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showAllDataScope = !showAllDataScope }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = if (showAllDataScope) Emerald700 else Slate500,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showAllDataScope) "Semua Input" else "Hanya Saya",
                                    color = if (showAllDataScope) Emerald900 else Slate700,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. DAFTAR ARSIP OPERASIONAL (HASIL vs RENCANA)
        // ==========================================
        if (historySubTab == "HASIL") {
            if (filteredRecords.isEmpty()) {
                item {
                    EmptyHistoryPlaceholder(
                        title = if (searchQuery.isNotBlank()) "Data Tidak Ditemukan" else "Belum Ada Laporan Realisasi",
                        message = if (searchQuery.isNotBlank()) {
                            "Tidak ada laporan selesai yang cocok dengan pencarian '$searchQuery'."
                        } else if (isManagerSession) {
                            "Belum ada laporan realisasi kerja selesai yang disubmit oleh para Kasie lapangan."
                        } else {
                            "Pekerjaan yang telah dilaporkan melalui tab Formulir akan otomatis masuk ke arsip komprehensif ini."
                        },
                        buttonText = if (isManagerSession) "Segarkan Data" else "Buka Tab Formulir",
                        onButtonClick = if (isManagerSession) {
                            { viewModel.refreshAnalyticsData() }
                        } else {
                            onNavigateToForm
                        }
                    )
                }
            } else {
                items(filteredRecords, key = { it.report.id }) { record ->
                    CompletedOperationCard(
                        record = record,
                        onClick = { selectedRecordForDetail = record },
                        onPhotoClick = { photoPreviewReport = record.report }
                    )
                }
            }
        } else {
            // Tab Rencana Kerja Lapangan (Monitoring Eksekutif Read-Only)
            if (filteredRencana.isEmpty()) {
                item {
                    EmptyHistoryPlaceholder(
                        title = if (searchQuery.isNotBlank()) "Data Tidak Ditemukan" else "Belum Ada Rencana Kerja",
                        message = if (searchQuery.isNotBlank()) {
                            "Tidak ada rencana kerja yang cocok dengan pencarian '$searchQuery'."
                        } else {
                            "Seluruh penugasan rencana kerja lapangan yang dimasukkan oleh para pengawas akan dipantau di sini."
                        },
                        buttonText = "Segarkan Data Server",
                        onButtonClick = { viewModel.refreshAnalyticsData() }
                    )
                }
            } else {
                items(filteredRencana, key = { it.id }) { rencana ->
                    ExecutiveRencanaCard(rencana = rencana)
                }
            }
        }
    }

    // ==========================================
    // MODAL DIALOG DETAIL LAPORAN AKTIVITAS LENGKAP
    // ==========================================
    selectedRecordForDetail?.let { rec ->
        ReportDetailDialog(
            report = rec.report,
            nomorSpk = rec.nomorSpk,
            isReadOnly = isManagerSession,
            onDismiss = { selectedRecordForDetail = null },
            onDelete = { id -> viewModel.deleteReport(id) }
        )
    }

    // Fullscreen Photo Preview
    photoPreviewReport?.let { rep ->
        FullscreenPhotoPreviewDialog(
            photoPath = rep.photoPath,
            photoBase64 = rep.photoBase64,
            unitCode = rep.unitCode,
            reportDate = rep.reportDate,
            nomorSpk = null,
            photoSizeKb = rep.photoSizeKb,
            onDismiss = { photoPreviewReport = null }
        )
    }
}

/**
 * Kartu Riwayat Komprehensif:
 * Menggabungkan informasi Rencana Awal Penugasan vs Realisasi Aktual Lapangan
 */
@Composable
fun CompletedOperationCard(
    record: CompletedOperationalRecord,
    onClick: () -> Unit,
    onPhotoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("completed_record_card_${record.report.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // ==========================================
            // HEADER: Kode Unit, Tanggal, Shift, Status SPK & Status Sinkron
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.report.unitCode,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900,
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = record.report.unitType.ifBlank { "HE" },
                            color = Slate700,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.reportDate,
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShiftBadge(shift = record.shift)
                    SyncStatusBadge(isSynced = record.isSynced)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-Header: Status SPK & Status Unit Operasi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status SPK Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (record.isSpkOfficial) Emerald50 else Color(0xFFFEF3C7),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (record.isSpkOfficial) Emerald200 else Color(0xFFFDE68A)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (record.isSpkOfficial) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (record.isSpkOfficial) Emerald700 else Color(0xFFB45309),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (record.isSpkOfficial) "SPK: ${record.nomorSpk}" else "SPK: Menyusul (Admin)",
                            color = if (record.isSpkOfficial) Emerald900 else Color(0xFF92400E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnitStatusBadge(status = record.unitStatus)
                    if (record.report.isHmAwalCorrected) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEF3C7)) {
                            Text(
                                text = "HM Koreksi",
                                color = Color(0xFFB45309),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // KOMPARASI RENCANA VS REALISASI LAPANGAN
            // ==========================================
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Pekerjaan & Lokasi (Aktual vs Rencana Awal)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Foto Thumbnail jika ada
                        if (!record.photoPath.isNullOrBlank() || !record.photoBase64.isNullOrBlank()) {
                            ReportPhotoThumbnail(
                                photoPath = record.photoPath,
                                photoBase64 = record.photoBase64,
                                photoSizeKb = record.photoSizeKb,
                                onClick = onPhotoClick
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            // Aktivitas Aktual & SAP
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = Emerald600,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = record.aktivitasAktual,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (record.sapCode.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "[${record.sapCode}]",
                                        fontSize = 10.sp,
                                        color = Emerald700,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Info Rencana Awal jika berbeda
                            if (record.aktivitasRencana.isNotBlank() &&
                                !record.aktivitasRencana.equals(record.aktivitasAktual, ignoreCase = true)
                            ) {
                                Text(
                                    text = "Rencana Awal: ${record.aktivitasRencana}",
                                    fontSize = 10.sp,
                                    color = Slate500,
                                    modifier = Modifier.padding(start = 19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // Lokasi Aktual & Rencana
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lokasi: ${record.lokasiAktual}",
                                    fontSize = 11.sp,
                                    color = Slate700,
                                    fontWeight = FontWeight.Medium
                                )
                                if (record.lokasiRencana.isNotBlank() &&
                                    !record.lokasiRencana.equals(record.lokasiAktual, ignoreCase = true)
                                ) {
                                    Text(
                                        text = " (Rencana: ${record.lokasiRencana})",
                                        fontSize = 10.sp,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    }

                    // 2. Jam & Operasional (Durasi Jam Kerja vs Durasi HM Berjalan)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Slate500, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Jam Kerja: ${String.format(Locale.US, "%.1f", record.jamKerja)} Jam",
                                fontSize = 11.sp,
                                color = Slate700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Durasi HM Berjalan
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Emerald50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = Emerald700, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${String.format(Locale.US, "%.2f", record.hmRunning)} HM",
                                    color = Emerald800,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Meteran HM Awal ➔ HM Akhir
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HM: ${String.format(Locale.US, "%.1f", record.hmAwal)} ➔ ${String.format(Locale.US, "%.1f", record.hmAkhir)}",
                            fontSize = 11.sp,
                            color = Slate600
                        )

                        if (record.report.workOutput > 0) {
                            Text(
                                text = "Hasil: ${String.format(Locale.US, "%.1f", record.report.workOutput)} ${record.report.unitOfMeasure}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald800
                            )
                        }
                    }

                    // 3. Bahan Bakar & Kendala Lapangan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Emerald600, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Solar: ${String.format(Locale.US, "%.1f", record.konsumsiSolar)} Liter",
                                fontSize = 11.sp,
                                color = Slate700,
                                fontWeight = FontWeight.Medium
                            )
                            if (record.isRefueled && record.refuelLiters > 0.0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                FuelBadge(liters = record.refuelLiters)
                            }
                        }

                        // Kendala Lapangan jika ada
                        if (!record.kendalaNama.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF2F2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = record.kendalaNama,
                                        color = Color(0xFFDC2626),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // 4. Personel: Kasie dan Operator Pelaksana
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kasie: ${record.supervisorName}",
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Operator: ${record.operatorAktual}",
                                fontSize = 10.sp,
                                color = Slate700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer Petunjuk Aksi Klik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ketuk untuk audit lengkap, status GPS & bukti foto ➔",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Emerald700
                )
            }
        }
    }
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
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(buttonText)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Header baris: Kode Unit + Status Realisasi + Status Operasi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Emerald50, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = rencana.kodeUnit,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Kasie: ${rencana.supervisorName.ifBlank { "Semua" }} • Shift ${rencana.shiftKerja}",
                            fontSize = 10.sp,
                            color = Slate500
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Status Realisasi
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (rencana.isRealized) Emerald100 else Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = if (rencana.isRealized) "Terealisasi" else "Rencana Aktif",
                            color = if (rencana.isRealized) Emerald900 else Color(0xFF92400E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Status Unit
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (rencana.statusUnit) {
                            "OPERASI" -> Color(0xFFDCFCE7)
                            "STANDBY" -> Color(0xFFFEF9C3)
                            else -> Color(0xFFFEE2E2)
                        }
                    ) {
                        Text(
                            text = rencana.statusUnit,
                            color = when (rencana.statusUnit) {
                                "OPERASI" -> Color(0xFF166534)
                                "STANDBY" -> Color(0xFF854D0E)
                                else -> Color(0xFF991B1B)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Body: Aktivitas, Lokasi, Operator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Engineering, contentDescription = null, tint = Emerald600, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = rencana.namaAktivitas.ifBlank { "Aktivitas Standar" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                if (rencana.sapCode.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "[${rencana.sapCode}]", fontSize = 10.sp, color = Emerald700, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Lokasi: ${rencana.kodeLokasi}", fontSize = 11.sp, color = Slate700)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Operator: ${rencana.operator}", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SPK Baris
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (rencana.isSpkTerbit) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (rencana.isSpkTerbit) Emerald200 else Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = if (rencana.isSpkTerbit) Emerald700 else Color(0xFFD97706),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (rencana.isSpkTerbit) "SPK: ${rencana.nomorSpk}" else "SPK: Menunggu Diterbitkan Web Admin",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (rencana.isSpkTerbit) Emerald900 else Color(0xFF92400E)
                        )
                    }

                    Text(
                        text = "Read-Only",
                        fontSize = 9.sp,
                        color = Slate400,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
