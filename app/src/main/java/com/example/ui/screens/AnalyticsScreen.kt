package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.ExecutiveFilterPanel
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.HeavyTrackViewModel
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: HeavyTrackViewModel
) {
    val activeSupervisor by viewModel.activeSupervisor.collectAsState()
    val isManagerSession by viewModel.isManagerSession.collectAsState()
    val analyticsState by viewModel.analyticsState.collectAsState()
    val units by viewModel.units.collectAsState()
    val period by viewModel.analyticsPeriod.collectAsState()
    val unitFilter by viewModel.analyticsUnitFilter.collectAsState()

    var isUnitDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Clean Slate-50 background
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. HEADER & FILTER PERIODE + UNIT
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth().testTag("card_analytics_filters")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Actions: Filter Unit (Hanya untuk Kasie/Supervisor) & Refresh Button
                    if (!isManagerSession) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Unit Filter Dropdown Trigger
                            Box {
                                OutlinedButton(
                                    onClick = { isUnitDropdownExpanded = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_filter_unit")
                                ) {
                                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Emerald600, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = unitFilter,
                                        color = Emerald700,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }

                                DropdownMenu(
                                    expanded = isUnitDropdownExpanded,
                                    onDismissRequest = { isUnitDropdownExpanded = false },
                                    modifier = Modifier.heightIn(max = 240.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Semua Unit (${units.size})") },
                                        onClick = {
                                            viewModel.setAnalyticsUnitFilter("Semua Unit")
                                            isUnitDropdownExpanded = false
                                        }
                                    )
                                    units.forEach { u ->
                                        DropdownMenuItem(
                                            text = { Text("${u.unitCode} - ${u.brandModel}") },
                                            onClick = {
                                                viewModel.setAnalyticsUnitFilter(u.unitCode)
                                                isUnitDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Tombol Refresh / Sinkronkan data terbaru dari server
                            IconButton(
                                onClick = { viewModel.refreshAnalyticsData() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Emerald50, RoundedCornerShape(10.dp))
                                    .testTag("btn_refresh_analytics")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sinkronkan Data Analitik",
                                    tint = Emerald600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    } else {
                        // Pada User Manager: Filter unit di atas Bulan Ini dihapus total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Periode Analitik",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Slate900
                            )

                            // Tombol Refresh / Sinkronkan data terbaru dari server
                            IconButton(
                                onClick = { viewModel.refreshAnalyticsData() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Emerald50, RoundedCornerShape(10.dp))
                                    .testTag("btn_refresh_analytics")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sinkronkan Data Analitik",
                                    tint = Emerald600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Periode Filter Pills [Bulan Ini] | [30 Hari] | [Custom Range]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Bulan Ini", "30 Hari", "Custom Range").forEach { p ->
                            val isSelected = period == p
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Emerald600 else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Emerald600 else Slate200
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setAnalyticsPeriod(p) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = p,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else Slate700
                                    )
                                }
                            }
                        }
                    }

                    if (period == "Custom Range") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Emerald50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Emerald700, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Rentang Kustom (14 Hari Terakhir)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900
                                    )
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = Color.White) {
                                    Text(
                                        text = "Sinkron Otomatis",
                                        fontSize = 10.sp,
                                        color = Emerald700,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
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

        // 2. TOP KPI CARDS (Agregasi Sesuai Filter)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val totalOutputVolumeAll = analyticsState.activitySummaries.sumOf { it.totalOutputVolume }

                // 3 KARTU METRIK RINGKASAN STATUS JAM KERJA BERDAMPINGAN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Hijau: Total Jam Operasi
                    StatusHoursMetricCard(
                        title = "Jam Operasi",
                        hours = analyticsState.effectiveOperationHours,
                        subtitle = "Operasi Efektif",
                        themeColor = Emerald600,
                        backgroundColor = Emerald50,
                        borderColor = Emerald200,
                        icon = Icons.Default.PrecisionManufacturing,
                        modifier = Modifier.weight(1f).testTag("card_jam_operasi")
                    )

                    // Kuning: Total Jam Standby (Cuaca/Tunggu Trailer)
                    StatusHoursMetricCard(
                        title = "Jam Standby",
                        hours = analyticsState.standbyHours,
                        subtitle = "Cuaca / Trailler",
                        themeColor = Color(0xFFD97706),
                        backgroundColor = Color(0xFFFFFBEB),
                        borderColor = Color(0xFFFDE68A),
                        icon = Icons.Default.PauseCircle,
                        modifier = Modifier.weight(1f).testTag("card_jam_standby")
                    )

                    // Merah: Total Jam Breakdown (Perbaikan Unit)
                    StatusHoursMetricCard(
                        title = "Jam Breakdown",
                        hours = analyticsState.breakdownHours,
                        subtitle = "Perbaikan Unit",
                        themeColor = Color(0xFFE11D48),
                        backgroundColor = Color(0xFFFFF1F2),
                        borderColor = Color(0xFFFECDD3),
                        icon = Icons.Default.Build,
                        modifier = Modifier.weight(1f).testTag("card_jam_breakdown")
                    )
                }

                // Baris 2: Total Solar & Total HM Berjalan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 1: Total Penggunaan Seluruh Solar
                    KpiSummaryCard(
                        title = "Total Solar Digunakan",
                        value = "${String.format(Locale.US, "%.1f", analyticsState.totalSolarLiters)} L",
                        subtitle = if (isManagerSession) "Akumulasi BBM Seluruh Fleet" else "Akumulasi BBM Kasie",
                        icon = Icons.Default.LocalGasStation,
                        iconColor = Color(0xFFD97706),
                        iconBg = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f).testTag("kpi_solar")
                    )

                    // KPI 2: Total Seluruh HM Berjalan
                    KpiSummaryCard(
                        title = "Total HM Berjalan",
                        value = "${String.format(Locale.US, "%.1f", analyticsState.totalHmRunning)} Jam",
                        subtitle = "Jam Kerja Alat Berat",
                        icon = Icons.Default.Speed,
                        iconColor = Emerald600,
                        iconBg = Emerald50,
                        modifier = Modifier.weight(1f).testTag("kpi_hm")
                    )
                }

                // Baris 2: Panel Terpadu Rata-Rata Rasio Solar & Total Hasil Kerja
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth().testTag("card_rasio_solar_hasil_kerja")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Efisiensi BBM & Total Hasil Kerja",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Slate900
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald50
                            ) {
                                Text(
                                    text = "${analyticsState.activitySummaries.size} Aktivitas",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Box Left: Rata-Rata Rasio Solar
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFFBEB),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.weight(1f).testTag("kpi_rasio_solar")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Rasio Konsumsi Solar",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.2f", analyticsState.avgFuelConsumptionRatio)} L/HM",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF78350F),
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Rumus: ${String.format(Locale.US, "%.0f", analyticsState.totalSolarLiters)} L ÷ ${String.format(Locale.US, "%.1f", analyticsState.totalHmRunning)} HM",
                                        fontSize = 10.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }

                            // Box Right: Total Hasil Kerja
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Emerald50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100),
                                modifier = Modifier.weight(1f).testTag("kpi_volume_output")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.PrecisionManufacturing,
                                            contentDescription = null,
                                            tint = Emerald700,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Total Hasil Kerja",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald900
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", totalOutputVolumeAll)} Vol",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Emerald900,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Total Capaian Hasil Produksi",
                                        fontSize = 10.sp,
                                        color = Emerald700
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. DISTRIBUSI WAKTU KERJA OPERASIONAL (Visual Progress Bar)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth().testTag("card_distribusi_waktu")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Distribusi Waktu Kerja Operasional",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = "Perbandingan Jam Operasi Efektif, Standby, dan Breakdown",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stacked Visual Progress Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        val opWeight = analyticsState.operationPercent.coerceAtLeast(0.02f)
                        val stbWeight = analyticsState.standbyPercent.coerceAtLeast(0.02f)
                        val bdnWeight = analyticsState.breakdownPercent.coerceAtLeast(0.02f)

                        Box(
                            modifier = Modifier
                                .weight(opWeight)
                                .fillMaxHeight()
                                .background(Emerald600) // Hijau Operasi
                        )
                        Box(
                            modifier = Modifier
                                .weight(stbWeight)
                                .fillMaxHeight()
                                .background(WarningAmber) // Oranye Standby
                        )
                        Box(
                            modifier = Modifier
                                .weight(bdnWeight)
                                .fillMaxHeight()
                                .background(ErrorRose) // Merah Breakdown
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend & Metric details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TimeLegendItem(
                            label = "Operasi (${String.format(Locale.US, "%.1f%%", analyticsState.operationPercent * 100)})",
                            hours = analyticsState.effectiveOperationHours,
                            color = Emerald600
                        )
                        TimeLegendItem(
                            label = "Standby (${String.format(Locale.US, "%.1f%%", analyticsState.standbyPercent * 100)})",
                            hours = analyticsState.standbyHours,
                            color = WarningAmber
                        )
                        TimeLegendItem(
                            label = "Breakdown (${String.format(Locale.US, "%.1f%%", analyticsState.breakdownPercent * 100)})",
                            hours = analyticsState.breakdownHours,
                            color = ErrorRose
                        )
                    }
                }
            }
        }

        // 4. PANEL RANGKUMAN PER UNIT (Card List dengan Footer Total)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth().testTag("card_rangkuman_unit")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Rangkuman Kinerja Per Unit",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF1F5F9)) {
                            Text(
                                text = "${analyticsState.unitSummaries.size} Unit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (analyticsState.unitSummaries.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(18.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada data aktivitas unit pada periode ini.", color = Slate400, fontSize = 12.sp)
                        }
                    } else {
                        analyticsState.unitSummaries.forEach { row ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = row.unitCode,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Slate900
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(shape = RoundedCornerShape(4.dp), color = Emerald100) {
                                                Text(
                                                    text = row.model,
                                                    color = Emerald900,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", row.totalHmRunning)} HM",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Emerald700,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Solar: ${String.format(Locale.US, "%.0f", row.totalFuelLiters)} L (Rasio: ${String.format(Locale.US, "%.2f", row.efficiencyRatio)} L/HM)",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                        Text(
                                            text = "Kendala: ${row.standbyBreakdownCount}x",
                                            fontSize = 11.sp,
                                            color = if (row.standbyBreakdownCount > 0) WarningAmber else Slate400,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (row.totalOutputVolume > 0) {
                                        Text(
                                            text = "Hasil Produksi: ${String.format(Locale.US, "%.1f", row.totalOutputVolume)} ${row.unitOfMeasure}",
                                            fontSize = 11.sp,
                                            color = Emerald900,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Baris Footer Total Ringkasan Armada Aktif di Paling Bawah
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Emerald50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("TOTAL RINGKASAN ARMADA", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Emerald900)
                                    Text("${analyticsState.unitSummaries.size} Unit Aktif Terlibat", fontSize = 10.sp, color = Emerald700)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", analyticsState.totalHmRunning)} HM Total",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Emerald900
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.0f", analyticsState.totalSolarLiters)} Liter Solar",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = Emerald700
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. PANEL RANGKUMAN PER AKTIVITAS (Kode SAP, Volume, Unit)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth().testTag("card_rangkuman_aktivitas")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rangkuman Kinerja Per Aktivitas SAP",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = "Akumulasi volume hasil kerja dan alokasi jam alat per jenis pekerjaan",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (analyticsState.activitySummaries.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(18.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada data aktivitas kerja pada periode ini.", color = Slate400, fontSize = 12.sp)
                        }
                    } else {
                        analyticsState.activitySummaries.forEach { actRow ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = actRow.activityName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Slate900
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = Emerald100) {
                                                Text(
                                                    text = actRow.sapCode,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Emerald900,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            // Badge Kategori Status Aktivitas
                                            val (statusBg, statusTxt) = when (actRow.kategoriStatus) {
                                                "OPERASI" -> Emerald100 to Emerald900
                                                "STANDBY" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                                                else -> Color(0xFFFFE4E6) to Color(0xFFE11D48)
                                            }
                                            Surface(shape = RoundedCornerShape(4.dp), color = statusBg) {
                                                Text(
                                                    text = actRow.kategoriStatus,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusTxt,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${actRow.unitInvolvedCount} Unit",
                                                fontSize = 11.sp,
                                                color = Slate500
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", actRow.totalOutputVolume)} ${actRow.unitOfMeasure}",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Emerald700,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", actRow.totalHm)} HM",
                                            fontSize = 11.sp,
                                            color = Slate500
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Slate900, fontFamily = FontFamily.Monospace)
            Text(subtitle, fontSize = 10.sp, color = Slate400)
        }
    }
}

@Composable
fun TimeLegendItem(
    label: String,
    hours: Double,
    color: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = "${String.format(Locale.US, "%.1f", hours)}j",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.padding(start = 12.dp)
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate700,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${String.format(Locale.US, "%.1f", hours)} Jam",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = themeColor,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Slate500,
                maxLines = 1
            )
        }
    }
}
