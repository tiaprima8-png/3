package com.example.ui.components

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.UnitEntity
import com.example.ui.viewmodel.UnitPlanStatus
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch

/**
 * Trigger Field displayed on the main form.
 * Minimum touch target 52dp with clear field labels, icons, and emerald focus state.
 */
@Composable
fun SelectorFieldTrigger(
    label: String,
    selectedValue: String?,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    badgeText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    testTag: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) Slate700 else Slate400
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (enabled) Emerald100 else Color(0xFFE2E8F0)
                ) {
                    Text(
                        text = badgeText,
                        color = if (enabled) Emerald900 else Slate500,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp, max = 56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isError) 1.5.dp else 1.dp,
                    color = when {
                        !enabled -> Slate200
                        isError -> MaterialTheme.colorScheme.error
                        !selectedValue.isNullOrEmpty() -> Emerald600
                        else -> Slate200
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(enabled = enabled) { onClick() }
                .testTag(testTag),
            color = if (enabled) Color.White else Color(0xFFF8FAFC),
            shadowElevation = if (enabled) 0.5.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = when {
                                !enabled -> Slate400
                                !selectedValue.isNullOrEmpty() -> Emerald600
                                else -> Slate400
                            },
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 6.dp)
                        )
                    }
                    if (!selectedValue.isNullOrEmpty()) {
                        Text(
                            text = selectedValue,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (enabled) Slate900 else Slate500
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate400
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Buka Pilihan",
                    tint = if (enabled) Slate500 else Slate400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

/**
 * Reusable Searchable Selector Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableSelectorSheet(
    isOpen: Boolean,
    title: String,
    searchPlaceholder: String,
    items: List<T>,
    filterPredicate: (item: T, query: String) -> Boolean,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    subtitle: String? = null,
    emptyMessage: String = "Tidak ada hasil yang cocok",
    emptySubtitle: String = "Coba kata kunci pencarian yang lain",
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    extraHeaderContent: (@Composable () -> Unit)? = null,
    itemContent: @Composable (item: T, isSelected: Boolean, onClick: () -> Unit) -> Unit
) {
    if (isOpen) {
        val scope = rememberCoroutineScope()
        var searchQuery by remember { mutableStateOf("") }

        val filteredItems = remember(items, searchQuery) {
            if (searchQuery.isBlank()) items
            else items.filter { filterPredicate(it, searchQuery.trim()) }
        }

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(44.dp)
                        .height(4.dp)
                        .background(Slate200, RoundedCornerShape(2.dp))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 560.dp)
                    .padding(horizontal = 20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = subtitle ?: "${filteredItems.size} opsi tersedia",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }

                    IconButton(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                onDismissRequest()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Slate500
                        )
                    }
                }

                // Instant Live Filter Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = searchPlaceholder,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = Emerald600
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus teks",
                                    tint = Slate400
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald600,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = Emerald50.copy(alpha = 0.4f),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("search_selector_input")
                )

                extraHeaderContent?.invoke()

                // List of filtered items
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = emptyMessage,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate700
                                )
                            )
                            Text(
                                text = emptySubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems) { item ->
                            itemContent(item, false) {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    onItemSelected(item)
                                    onDismissRequest()
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
 * Card Item for Unit Alat Berat Selector
 * Criteria: Matches unitCode OR unitType
 * Displays: Kode Unit tebal, badge jenis unit, nilai "HM Terakhir: X", status
 */
@Composable
fun UnitSelectorItemCard(
    unit: UnitEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("unit_item_${unit.unitCode}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Emerald50 else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Emerald600 else Slate200
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = unit.unitCode,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            unit.unitType.uppercase().contains("EXCAVATOR") -> Color(0xFFFEF3C7)
                            unit.unitType.uppercase().contains("GRADER") -> Color(0xFFEDE9FE)
                            unit.unitType.uppercase().contains("COMPACTOR") -> Color(0xFFE0F2FE)
                            else -> Color(0xFFE2E8F0)
                        }
                    ) {
                        Text(
                            text = unit.unitType,
                            color = when {
                                unit.unitType.uppercase().contains("EXCAVATOR") -> Color(0xFF92400E)
                                unit.unitType.uppercase().contains("GRADER") -> Color(0xFF5B21B6)
                                unit.unitType.uppercase().contains("COMPACTOR") -> Color(0xFF0369A1)
                                else -> Slate700
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${unit.brandModel} • Ditugaskan: ${unit.supervisorAssigned}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Emerald50
                    ) {
                        Text(
                            text = "HM Terakhir: ${String.format("%.1f", unit.lastHm)} Jam",
                            color = Emerald700,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Emerald600, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Terpilih",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card Item for Nama Aktivitas Kerja Selector
 * Criteria: Matches activityName OR sapCode (e.g., "gali" or "101")
 * Displays: Nama Aktivitas, Chip Badge Kode SAP (ACT-SAP-xxx), teks Satuan (m, m2, m3), checkmark hijau emerald
 */
@Composable
fun ActivitySelectorItemCard(
    activity: ActivityMasterEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("activity_item_${activity.sapCode}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Emerald50 else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Emerald600 else Slate200
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.activityName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chip Badge Kode SAP
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Emerald100
                    ) {
                        Text(
                            text = activity.sapCode,
                            color = Emerald900,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    // Chip Badge Status (OPERASI / STANDBY / BREAKDOWN)
                    val statusText = activity.kategoriStatus.ifBlank { "OPERASI" }
                    val (statusBg, statusTxt) = when (statusText) {
                        "OPERASI" -> Emerald100 to Emerald900
                        "STANDBY" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                        else -> Color(0xFFFFE4E6) to Color(0xFFE11D48)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusText,
                            color = statusTxt,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    // Teks Satuan
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "Satuan: ${activity.unitOfMeasure}",
                            color = Slate700,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Emerald600, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Terpilih",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card Item for Operator Pelaksana Selector
 * Filtered under active supervisor
 * Displays: Operator name, NIK badge, skill level
 */
@Composable
fun OperatorSelectorItemCard(
    operator: OperatorEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("operator_item_${operator.operatorId}"),
        color = if (isSelected) Color(0xFFF0FDF4) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFF009663) else Slate200
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = operator.name.uppercase(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                ),
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFF009663),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Item for Lokasi Kerja Selector - Minimalis & Lightweight (Anti-Lag 3.614 Data)
 * Displays:
 * - Kode Lokasi (Teks bold monospace)
 * - Subteks Wilayah & Luas
 * - Checkmark indicator bila terpilih
 */
@Composable
fun LokasiSelectorItemCard(
    lokasi: LokasiEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 18.dp)
            .testTag("lokasi_item_${lokasi.kodeLokasi}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lokasi.kodeLokasi,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 15.sp,
                color = if (isSelected) Emerald700 else Slate900
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "Wilayah: ${lokasi.wilayah}",
                    fontSize = 12.sp,
                    color = Slate500
                )
                if (lokasi.luasNetto > 0.0 || lokasi.luasBruto > 0.0) {
                    val luasText = if (lokasi.luasNetto > 0.0) "${lokasi.luasNetto} Ha" else "${lokasi.luasBruto} Ha"
                    Text(
                        text = "•  Luas: $luasText",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Terpilih",
                tint = Emerald600,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LokasiSearchableSelectorSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    lokasiList: List<LokasiEntity>,
    selectedLokasi: LokasiEntity?,
    onLokasiSelected: (LokasiEntity) -> Unit,
    searchQuery: String? = null,
    onSearchQueryChange: ((String) -> Unit)? = null,
    wilayahLabel: String? = null,
    selectedWilayahFilter: String? = null,
    onWilayahFilterChange: ((String) -> Unit)? = null
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localSearchQuery by remember { mutableStateOf("") }
    val activeQuery = searchQuery ?: localSearchQuery

    val defaultWilayah = remember(wilayahLabel) {
        when {
            wilayahLabel?.contains("PG1", ignoreCase = true) == true -> "PG1"
            wilayahLabel?.contains("PG2", ignoreCase = true) == true -> "PG2"
            else -> "Semua"
        }
    }
    var activeWilayahFilter by remember(selectedWilayahFilter, wilayahLabel) {
        mutableStateOf(selectedWilayahFilter ?: defaultWilayah)
    }

    val listLokasi = remember(activeQuery, lokasiList, activeWilayahFilter, onSearchQueryChange != null, onWilayahFilterChange != null) {
        val q = activeQuery.trim()
        val queryFiltered = if (onSearchQueryChange != null) {
            lokasiList
        } else if (q.isBlank()) {
            lokasiList
        } else {
            lokasiList.filter {
                it.kodeLokasi.contains(q, ignoreCase = true) || it.wilayah.contains(q, ignoreCase = true)
            }
        }

        if (onWilayahFilterChange != null) {
            queryFiltered
        } else {
            when (activeWilayahFilter) {
                "Semua", "" -> queryFiltered
                "PG1" -> queryFiltered.filter { it.wilayah.contains("PG1", ignoreCase = true) }
                "PG2" -> queryFiltered.filter { it.wilayah.contains("PG2", ignoreCase = true) }
                "OTHER" -> queryFiltered.filter { !it.wilayah.contains("PG1", ignoreCase = true) && !it.wilayah.contains("PG2", ignoreCase = true) }
                else -> queryFiltered.filter { it.wilayah.contains(activeWilayahFilter, ignoreCase = true) }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (onSearchQueryChange != null) {
                onSearchQueryChange("")
            } else {
                localSearchQuery = ""
            }
            onDismissRequest()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .background(Slate200, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 320.dp, max = 620.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                // Header & Indikator Data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pilih Lokasi Kerja Lapangan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val countFormatted = if (listLokasi.size == 3614) "3.614" else {
                            try {
                                java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(listLokasi.size)
                            } catch (_: Exception) {
                                listLokasi.size.toString()
                            }
                        }
                        Text(
                            text = "Menampilkan $countFormatted Lokasi Aktif",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate500,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    IconButton(
                        onClick = {
                            if (onSearchQueryChange != null) {
                                onSearchQueryChange("")
                            } else {
                                localSearchQuery = ""
                            }
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Slate500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chip Wilayah: [Semua], [PG1], [PG2], [OTHER]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Semua", "PG1", "PG2", "OTHER").forEach { chipWilayah ->
                        val isSelected = activeWilayahFilter.equals(chipWilayah, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                activeWilayahFilter = chipWilayah
                                onWilayahFilterChange?.invoke(chipWilayah)
                            },
                            label = {
                                Text(
                                    text = chipWilayah,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald600,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Slate700
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("filter_chip_$chipWilayah")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Kotak Pencarian Instan dengan Debounce
                OutlinedTextField(
                    value = activeQuery,
                    onValueChange = { newVal ->
                        if (onSearchQueryChange != null) {
                            onSearchQueryChange(newVal)
                        } else {
                            localSearchQuery = newVal
                        }
                    },
                    placeholder = { Text("Ketik kode lokasi (misal: 001 atau 081)...", color = Slate400, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (activeQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    if (onSearchQueryChange != null) {
                                        onSearchQueryChange("")
                                    } else {
                                        localSearchQuery = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Slate400)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        cursorColor = Emerald600,
                        focusedBorderColor = Emerald600,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_lokasi")
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            if (listLokasi.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada lokasi yang cocok dengan '$activeQuery'",
                        color = Slate400,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Daftar Virtualized (LazyColumn)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = listLokasi,
                        key = { it.kodeLokasi }
                    ) { lokasi ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLokasiSelected(lokasi)
                                    if (onSearchQueryChange != null) {
                                        onSearchQueryChange("")
                                    } else {
                                        localSearchQuery = ""
                                    }
                                    onDismissRequest()
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("lokasi_item_${lokasi.kodeLokasi}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lokasi.kodeLokasi,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Wilayah: ${lokasi.wilayah} • Bruto: ${lokasi.luasBruto} Ha",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // Badge status aktif kecil
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Aktif",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSearchableSelectorSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    units: List<UnitEntity>,
    selectedUnit: UnitEntity?,
    onUnitSelected: (UnitEntity) -> Unit
) {
    SearchableSelectorSheet(
        isOpen = isOpen,
        title = "Pilih Unit Alat Berat",
        searchPlaceholder = "Cari kode unit atau jenis (mis. HEH0004, Excavator, Grader)...",
        items = units,
        filterPredicate = { unit, query ->
            unit.unitCode.contains(query, ignoreCase = true) ||
            unit.unitType.contains(query, ignoreCase = true) ||
            unit.brandModel.contains(query, ignoreCase = true)
        },
        onItemSelected = onUnitSelected,
        onDismissRequest = onDismissRequest
    ) { unit, _, onClick ->
        UnitSelectorItemCard(
            unit = unit,
            isSelected = selectedUnit?.unitCode == unit.unitCode,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorSearchableSelectorSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    operators: List<OperatorEntity>,
    selectedOperator: OperatorEntity?,
    kasieFilter: String? = null,
    onOperatorSelected: (OperatorEntity) -> Unit
) {
    val displayedOperators = remember(operators, kasieFilter) {
        val cleanFilter = kasieFilter?.trim()?.removePrefix("@")?.lowercase()
        if (cleanFilter.isNullOrBlank()) {
            operators
        } else {
            val matched = operators.filter { op ->
                val spv = op.supervisorAssigned.trim().removePrefix("@").lowercase()
                val spv2 = op.kasiePenanggungJawab?.trim()?.removePrefix("@")?.lowercase() ?: ""
                spv == cleanFilter || spv.contains(cleanFilter) || cleanFilter.contains(spv) ||
                spv2 == cleanFilter || spv2.contains(cleanFilter) || cleanFilter.contains(spv2)
            }
            if (matched.isNotEmpty()) matched else operators
        }
    }

    SearchableSelectorSheet(
        isOpen = isOpen,
        title = "Pilih Operator Pelaksana",
        subtitle = "${displayedOperators.size} Operator Aktif",
        searchPlaceholder = "Cari nama operator...",
        emptyMessage = "Operator tidak ditemukan",
        emptySubtitle = "Hanya operator binaan Kasie yang terdaftar di sistem",
        items = displayedOperators,
        filterPredicate = { op, query ->
            op.name.contains(query, ignoreCase = true)
        },
        onItemSelected = onOperatorSelected,
        onDismissRequest = onDismissRequest
    ) { op, _, onClick ->
        OperatorSelectorItemCard(
            operator = op,
            isSelected = selectedOperator?.operatorId == op.operatorId || selectedOperator?.name?.equals(op.name, ignoreCase = true) == true,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitySearchableSelectorSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    activities: List<ActivityMasterEntity>,
    selectedActivity: ActivityMasterEntity?,
    unitType: String? = null,
    unitCode: String? = null,
    modelTipe: String? = null,
    onActivitySelected: (ActivityMasterEntity) -> Unit
) {
    var selectedCategoryTab by remember { mutableStateOf("Semua") }

    val cleanCode = unitCode?.trim()?.takeIf { it.isNotBlank() }
    val cleanModel = modelTipe?.trim()?.takeIf { it.isNotBlank() }
    val cleanType = unitType?.trim()?.takeIf { it.isNotBlank() }

    val unitLabel = remember(cleanCode, cleanModel, cleanType) {
        when {
            cleanCode != null && cleanModel != null && !cleanModel.equals(cleanCode, ignoreCase = true) ->
                "$cleanCode ($cleanModel)"
            cleanCode != null -> cleanCode
            cleanModel != null -> cleanModel
            cleanType != null -> cleanType
            else -> null
        }
    }

    // Filter by category tab: Semua, Operasi, Standby, Breakdown
    val categoryFilteredActivities = remember(activities, selectedCategoryTab) {
        val tab = selectedCategoryTab.trim().uppercase()
        val list = if (tab == "SEMUA" || tab == "ALL" || tab.isBlank()) {
            activities
        } else {
            activities.filter { act ->
                val status = act.kategoriStatus.trim().uppercase()
                val cat = act.category.trim().uppercase()
                status == tab || cat == tab || status.contains(tab)
            }
        }
        // Always sort OPERASI first (1), STANDBY second (2), BREAKDOWN third (3)
        list.sortedWith(
            compareBy(
                { act ->
                    when (act.kategoriStatus.trim().uppercase()) {
                        "OPERASI" -> 1
                        "STANDBY" -> 2
                        "BREAKDOWN" -> 3
                        else -> 4
                    }
                },
                { it.activityName }
            )
        )
    }

    val dynamicSubtitle = remember(unitLabel, categoryFilteredActivities.size) {
        if (unitLabel != null) {
            "Sesuai Unit $unitLabel • ${categoryFilteredActivities.size} aktivitas"
        } else {
            "${categoryFilteredActivities.size} Aktivitas Lapangan"
        }
    }

    SearchableSelectorSheet(
        isOpen = isOpen,
        title = "Pilih Aktivitas Kerja Lapangan",
        subtitle = dynamicSubtitle,
        searchPlaceholder = "Cari nama aktivitas atau kode SAP...",
        emptyMessage = if (unitLabel != null) "Tidak ada aktivitas untuk $unitLabel" else "Tidak ada hasil yang cocok",
        emptySubtitle = "Ganti tab filter kategori [Semua/Operasi/Standby/Breakdown] atau ubah pencarian",
        items = categoryFilteredActivities,
        filterPredicate = { act, query ->
            act.activityName.contains(query, ignoreCase = true) ||
            act.sapCode.contains(query, ignoreCase = true) ||
            act.category.contains(query, ignoreCase = true) ||
            act.kategoriStatus.contains(query, ignoreCase = true)
        },
        onItemSelected = onActivitySelected,
        onDismissRequest = onDismissRequest,
        extraHeaderContent = {
            // Quick Filter Chip Kategori Status: [Semua], [Operasi], [Standby], [Breakdown]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val categoryTabs = listOf("Semua", "Operasi", "Standby", "Breakdown")
                categoryTabs.forEach { tab ->
                    val isSelected = selectedCategoryTab.equals(tab, ignoreCase = true)
                    val (chipBg, chipText, chipBorder) = when {
                        isSelected && tab.equals("Operasi", ignoreCase = true) ->
                            Triple(Color(0xFFECFDF5), Color(0xFF059669), Color(0xFF059669))
                        isSelected && tab.equals("Standby", ignoreCase = true) ->
                            Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFFD97706))
                        isSelected && tab.equals("Breakdown", ignoreCase = true) ->
                            Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFFDC2626))
                        isSelected ->
                            Triple(Emerald600, Color.White, Emerald600)
                        else ->
                            Triple(Color(0xFFF8FAFC), Slate700, Slate200)
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedCategoryTab = tab }
                            .testTag("filter_chip_${tab.lowercase()}"),
                        shape = RoundedCornerShape(10.dp),
                        color = chipBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = chipText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    ) { act, _, onClick ->
        ActivitySelectorItemCard(
            activity = act,
            isSelected = selectedActivity?.sapCode == act.sapCode,
            onClick = onClick
        )
    }
}

/**
 * Card item for Unit Alat Berat with automatic Backlog detection & target date badge.
 */
@Composable
fun UnitPlanSelectorItemCard(
    status: UnitPlanStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val unit = status.unit
    val containerBg = when {
        isSelected -> Emerald50
        status.isBacklog -> Color(0xFFFEF2F2)
        status.isSusulan -> Color(0xFFF0FDF4)
        else -> Color(0xFFF0FDF4)
    }
    val borderColor = when {
        isSelected -> Emerald600
        status.isBacklog -> Color(0xFFFCA5A5)
        status.isSusulan -> Color(0xFF86EFAC)
        else -> Color(0xFF86EFAC)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("unit_plan_item_${unit.unitCode}"),
        colors = CardDefaults.cardColors(
            containerColor = containerBg
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = unit.unitCode,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            unit.unitType.uppercase().contains("EXCAVATOR") -> Color(0xFFFEF3C7)
                            unit.unitType.uppercase().contains("GRADER") -> Color(0xFFEDE9FE)
                            unit.unitType.uppercase().contains("COMPACTOR") -> Color(0xFFE0F2FE)
                            else -> Color(0xFFE2E8F0)
                        }
                    ) {
                        Text(
                            text = unit.unitType,
                            color = when {
                                unit.unitType.uppercase().contains("EXCAVATOR") -> Color(0xFF92400E)
                                unit.unitType.uppercase().contains("GRADER") -> Color(0xFF5B21B6)
                                unit.unitType.uppercase().contains("COMPACTOR") -> Color(0xFF0369A1)
                                else -> Slate700
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Badge status: Kasus A (Backlog), Kasus B (Tertib / Siap Hari Ini), Kasus C (Sudah Ada Rencana / Tambah Susulan)
                val badgeBg = when {
                    status.isBacklog -> Color(0xFFFEE2E2)
                    status.isSusulan -> Color(0xFFDCFCE7)
                    else -> Color(0xFFDCFCE7)
                }
                val badgeBorder = when {
                    status.isBacklog -> Color(0xFFEF4444)
                    status.isSusulan -> Emerald600
                    else -> Color(0xFF059669)
                }
                val badgeTextCol = when {
                    status.isBacklog -> Color(0xFF991B1B)
                    status.isSusulan -> Emerald900
                    else -> Color(0xFF065F46)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg,
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        badgeBorder
                    )
                ) {
                    Text(
                        text = status.labelText,
                        color = badgeTextCol,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${unit.brandModel} • Ditugaskan: ${unit.supervisorAssigned}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Emerald50
                    ) {
                        Text(
                            text = "HM Terakhir: ${String.format(java.util.Locale.US, "%.1f", unit.lastHm)} Jam (Std: ${unit.hmMinStandar}-${unit.hmMaxStandar} HM)",
                            color = Emerald700,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Emerald600, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Terpilih",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Searchable Selector Sheet for Unit with Backlog detection & automatic filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPlanSearchableSelectorSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    availableUnits: List<UnitPlanStatus>,
    selectedStatus: UnitPlanStatus?,
    kasieFilter: String? = null,
    onStatusSelected: (UnitPlanStatus) -> Unit
) {
    val displayedUnits = remember(availableUnits, kasieFilter) {
        val cleanFilter = kasieFilter?.trim()?.removePrefix("@")?.lowercase()
        if (cleanFilter.isNullOrBlank()) {
            availableUnits
        } else {
            val matched = availableUnits.filter { item ->
                val spv = item.unit.supervisorAssigned.trim().removePrefix("@").lowercase()
                spv == cleanFilter || spv.contains(cleanFilter) || cleanFilter.contains(spv)
            }
            if (matched.isNotEmpty()) matched else availableUnits
        }
    }
    val backlogCount = displayedUnits.count { it.isBacklog }
    val readyCount = displayedUnits.size - backlogCount
    val subtitleText = when {
        backlogCount > 0 && readyCount > 0 -> "${displayedUnits.size} unit ($backlogCount backlog, $readyCount siap hari ini)"
        backlogCount > 0 -> "${displayedUnits.size} unit ($backlogCount backlog)"
        else -> "${displayedUnits.size} unit (semua tertib & siap hari ini)"
    }

    SearchableSelectorSheet(
        isOpen = isOpen,
        title = "Pilih Unit Alat Berat",
        subtitle = subtitleText,
        searchPlaceholder = "Cari kode unit atau jenis (mis. EX-201, Dozer)...",
        emptyMessage = "Unit alat berat tidak ditemukan",
        emptySubtitle = "Cek filter pengawas atau hubungi Admin Alat Berat",
        items = displayedUnits,
        filterPredicate = { item, query ->
            item.unit.unitCode.contains(query, ignoreCase = true) ||
            item.unit.unitType.contains(query, ignoreCase = true) ||
            item.unit.brandModel.contains(query, ignoreCase = true) ||
            item.labelText.contains(query, ignoreCase = true)
        },
        onItemSelected = onStatusSelected,
        onDismissRequest = onDismissRequest
    ) { item, _, onClick ->
        UnitPlanSelectorItemCard(
            status = item,
            isSelected = selectedStatus?.unit?.unitCode == item.unit.unitCode,
            onClick = onClick
        )
    }
}

/**
 * Card item for Master Kendala Selector.
 */
@Composable
fun KendalaSelectorItemCard(
    kendala: KendalaEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("kendala_item_${kendala.kodeKendala}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Emerald50 else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Emerald600 else Slate200
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (kendala.kategori.uppercase()) {
                            "CUACA" -> Color(0xFFDBEAFE)
                            "MEKANIKAL" -> Color(0xFFFEE2E2)
                            "SAFETY" -> Color(0xFFFEF3C7)
                            else -> Color(0xFFE2E8F0)
                        }
                    ) {
                        Text(
                            text = kendala.kodeKendala,
                            color = when (kendala.kategori.uppercase()) {
                                "CUACA" -> Color(0xFF1E40AF)
                                "MEKANIKAL" -> Color(0xFF991B1B)
                                "SAFETY" -> Color(0xFF92400E)
                                else -> Slate700
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = kendala.kategori,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = kendala.namaKendala,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Emerald600, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Terpilih",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Searchable Selector Sheet for Master Kendala.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KendalaSearchableSelectorSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    kendalaList: List<KendalaEntity>,
    selectedKendala: KendalaEntity?,
    onKendalaSelected: (KendalaEntity) -> Unit
) {
    SearchableSelectorSheet(
        isOpen = isOpen,
        title = "Pilih Master Kendala Operasional",
        searchPlaceholder = "Cari kendala (hujan, breakdown, antrian, amblas)...",
        items = kendalaList,
        filterPredicate = { item, query ->
            item.namaKendala.contains(query, ignoreCase = true) ||
            item.kodeKendala.contains(query, ignoreCase = true) ||
            item.kategori.contains(query, ignoreCase = true)
        },
        onItemSelected = onKendalaSelected,
        onDismissRequest = onDismissRequest
    ) { item, _, onClick ->
        KendalaSelectorItemCard(
            kendala = item,
            isSelected = selectedKendala?.kodeKendala == item.kodeKendala,
            onClick = onClick
        )
    }
}


