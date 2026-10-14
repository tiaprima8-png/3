package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WilayahConstants
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.HeavyTrackViewModel

@Composable
fun ExecutiveFilterPanel(
    viewModel: HeavyTrackViewModel,
    modifier: Modifier = Modifier,
    isManagerSession: Boolean = true
) {
    val selectedWilayah by viewModel.filterWilayah.collectAsState()
    val selectedJenisUnit by viewModel.filterJenisUnit.collectAsState()
    val selectedKodeUnit by viewModel.filterKodeUnit.collectAsState()
    val allUnits by viewModel.allUnitsGlobal.collectAsState()

    var isWilayahDropdownExpanded by remember { mutableStateOf(false) }
    var isKodeUnitDropdownExpanded by remember { mutableStateOf(false) }

    val isFilterActive = selectedWilayah != "Semua Wilayah" ||
            selectedJenisUnit != "Semua Jenis" ||
            selectedKodeUnit != "Semua Unit"

    // Filter available unit codes for dropdown based on currently chosen wilayah & jenis
    val candidateUnits = remember(allUnits, selectedWilayah, selectedJenisUnit) {
        allUnits.filter { u ->
            val matchWilayah = selectedWilayah == "Semua Wilayah" || WilayahConstants.matches(u.wilayah, selectedWilayah)
            val matchJenis = selectedJenisUnit == "Semua Jenis" || viewModel.matchesJenisUnit(u.unitType, selectedJenisUnit)
            matchWilayah && matchJenis
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isFilterActive) Color(0xFFFDE68A) else Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("executive_filter_panel")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Bar: Title, Badge, & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (isManagerSession) Color(0xFFFEF3C7) else Emerald50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (isManagerSession) Color(0xFFB45309) else Emerald600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filter Wilayah & Unit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isFilterActive) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "TERFILTER",
                                color = Color(0xFF92400E),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isFilterActive) {
                    TextButton(
                        onClick = { viewModel.resetExecutiveFilters() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("btn_reset_filter")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            color = Color(0xFFB45309),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 1. FILTER WILAYAH (Dropdown 8 Wilayah Baru + Semua Wilayah)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Filter Wilayah Kerja:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }
                    if (selectedWilayah != "Semua Wilayah") {
                        Text(
                            text = "Aktif: $selectedWilayah",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald700
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isWilayahDropdownExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (selectedWilayah != "Semua Wilayah") Emerald600 else Slate200),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedWilayah != "Semua Wilayah") Emerald50 else Color(0xFFF8FAFC)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_dropdown_filter_wilayah")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (selectedWilayah != "Semua Wilayah") Emerald600 else Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedWilayah == "Semua Wilayah") "Semua Wilayah" else "Wilayah: $selectedWilayah",
                                    color = if (selectedWilayah != "Semua Wilayah") Emerald900 else Slate800,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedWilayah != "Semua Wilayah") FontWeight.Bold else FontWeight.Medium
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (selectedWilayah != "Semua Wilayah") {
                                    IconButton(
                                        onClick = {
                                            viewModel.setFilterWilayah("Semua Wilayah")
                                            if (selectedKodeUnit != "Semua Unit") {
                                                viewModel.setFilterKodeUnit("Semua Unit")
                                            }
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Wilayah", tint = Slate500, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate500)
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = isWilayahDropdownExpanded,
                        onDismissRequest = { isWilayahDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f).heightIn(max = 280.dp)
                    ) {
                        WilayahConstants.LIST_WILAYAH_MANAGER_FILTER.forEach { wilayahOpt ->
                            val isChosen = selectedWilayah.equals(wilayahOpt, ignoreCase = true) ||
                                    (wilayahOpt == "Semua Wilayah" && (selectedWilayah.isBlank() || selectedWilayah == "Semua Wilayah"))
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isChosen) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = wilayahOpt,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isChosen) Emerald900 else Slate800,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setFilterWilayah(wilayahOpt)
                                    if (selectedKodeUnit != "Semua Unit") {
                                        viewModel.setFilterKodeUnit("Semua Unit")
                                    }
                                    isWilayahDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("item_filter_wilayah_${wilayahOpt.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // Quick scrollable chips for fast one-tap switching
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(WilayahConstants.LIST_WILAYAH_MANAGER_FILTER.size) { idx ->
                        val wilayahChip = WilayahConstants.LIST_WILAYAH_MANAGER_FILTER[idx]
                        val isSelected = selectedWilayah.equals(wilayahChip, ignoreCase = true) ||
                                (wilayahChip == "Semua Wilayah" && (selectedWilayah.isBlank() || selectedWilayah == "Semua Wilayah"))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Emerald600 else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) Emerald600 else Slate200),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setFilterWilayah(wilayahChip)
                                    if (selectedKodeUnit != "Semua Unit") {
                                        viewModel.setFilterKodeUnit("Semua Unit")
                                    }
                                }
                                .testTag("chip_filter_wilayah_${wilayahChip.lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = wilayahChip,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Slate700,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // 2. FILTER JENIS UNIT (Semua Jenis, Excavator Long Arm, Excavator Mini, Excavator Standar, Motor Grader, Compactor)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Jenis Unit:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                }
                val jenisOptions = listOf(
                    "Semua Jenis" to "Semua",
                    "Excavator Long Arm" to "Excavator Long Arm",
                    "Excavator Mini" to "Excavator Mini",
                    "Excavator Standar" to "Excavator Standar",
                    "Motor Grader" to "Motor Grader",
                    "Compactor" to "Compactor"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    jenisOptions.forEach { (key, label) ->
                        val isSelected = selectedJenisUnit.equals(key, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF0F766E) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F766E) else Slate200),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setFilterJenisUnit(key)
                                    if (selectedKodeUnit != "Semua Unit") {
                                        viewModel.setFilterKodeUnit("Semua Unit")
                                    }
                                }
                                .testTag("chip_filter_jenis_${key.lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Slate700,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 3. FILTER KODE UNIT (Search / Dropdown spesifik nomor lambung unit)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kode Unit:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }
                    Text(
                        text = "${candidateUnits.size} Unit Tersedia",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isKodeUnitDropdownExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (selectedKodeUnit != "Semua Unit") Emerald600 else Slate200),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedKodeUnit != "Semua Unit") Emerald50 else Color(0xFFF8FAFC)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_dropdown_kode_unit")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (selectedKodeUnit == "Semua Unit") "Semua Unit" else "Unit: $selectedKodeUnit",
                                    color = if (selectedKodeUnit != "Semua Unit") Emerald900 else Slate800,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedKodeUnit != "Semua Unit") FontWeight.Bold else FontWeight.Medium
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (selectedKodeUnit != "Semua Unit") {
                                    IconButton(
                                        onClick = { viewModel.setFilterKodeUnit("Semua Unit") },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Unit", tint = Slate500, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate500)
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = isKodeUnitDropdownExpanded,
                        onDismissRequest = { isKodeUnitDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f).heightIn(max = 240.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (selectedKodeUnit == "Semua Unit") {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text("Semua Unit (${candidateUnits.size})", fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                viewModel.setFilterKodeUnit("Semua Unit")
                                isKodeUnitDropdownExpanded = false
                            }
                        )

                        candidateUnits.forEach { unit ->
                            val isUnitSelected = selectedKodeUnit.equals(unit.unitCode, ignoreCase = true)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isUnitSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Column {
                                                Text(
                                                    text = unit.unitCode,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Slate900
                                                )
                                                Text(
                                                    text = "${unit.unitType} • ${unit.brandModel}",
                                                    fontSize = 10.sp,
                                                    color = Slate500
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Emerald50
                                        ) {
                                            Text(
                                                text = unit.wilayah,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald700,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.setFilterKodeUnit(unit.unitCode)
                                    isKodeUnitDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
