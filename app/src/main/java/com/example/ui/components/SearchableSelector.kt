package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
                        .padding(bottom = 14.dp)
                        .testTag("search_selector_input")
                )

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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("operator_item_${operator.operatorId}"),
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
                        text = operator.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE0E7FF)
                    ) {
                        Text(
                            text = operator.badgeNumber,
                            color = Color(0xFF3730A3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kasie: ${operator.supervisorAssigned}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
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
 * Item for Lokasi Kerja Selector - Minimalis (Hanya Kode Lokasi)
 * Displays only:
 * - kode_lokasi (e.g., "001A", "002B", "100A")
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
            .padding(vertical = 14.dp, horizontal = 16.dp)
            .testTag("lokasi_item_${lokasi.kodeLokasi}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = lokasi.kodeLokasi,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = if (isSelected) Emerald700 else Slate900
        )
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
    onLokasiSelected: (LokasiEntity) -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery, lokasiList) {
        if (searchQuery.isBlank()) {
            lokasiList
        } else {
            val q = searchQuery.trim()
            lokasiList.filter {
                it.kodeLokasi.contains(q, ignoreCase = true)
            }
        }
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pilih Lokasi Kerja Lapangan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Pilih kode lokasi kerja",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari kode lokasi (misal: 001A)...", color = Slate400, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Slate400)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald600,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_lokasi")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ditemukan ${filteredList.size} Lokasi Kerja",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate400,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada lokasi yang cocok dengan '$searchQuery'",
                        color = Slate400,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(filteredList, key = { _, lokasi -> lokasi.kodeLokasi }) { index, lokasi ->
                        LokasiSelectorItemCard(
                            lokasi = lokasi,
                            isSelected = selectedLokasi?.kodeLokasi == lokasi.kodeLokasi,
                            onClick = {
                                onLokasiSelected(lokasi)
                                onDismissRequest()
                            }
                        )
                        if (index < filteredList.lastIndex) {
                            HorizontalDivider(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                thickness = 1.dp
                            )
                        }
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
        searchPlaceholder = "Cari nama operator atau NIK...",
        emptyMessage = "Operator tidak ditemukan",
        emptySubtitle = "Hanya operator binaan Kasie yang terdaftar di sistem",
        items = displayedOperators,
        filterPredicate = { op, query ->
            op.name.contains(query, ignoreCase = true) ||
            op.badgeNumber.contains(query, ignoreCase = true)
        },
        onItemSelected = onOperatorSelected,
        onDismissRequest = onDismissRequest
    ) { op, _, onClick ->
        OperatorSelectorItemCard(
            operator = op,
            isSelected = selectedOperator?.operatorId == op.operatorId,
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
    onActivitySelected: (ActivityMasterEntity) -> Unit
) {
    val cleanUnit = unitType?.trim()?.takeIf { it.isNotBlank() }
    SearchableSelectorSheet(
        isOpen = isOpen,
        title = "Pilih Aktivitas Kerja Lapangan",
        subtitle = cleanUnit?.let { "Sesuai Unit $it (${activities.size} aktivitas)" },
        searchPlaceholder = if (cleanUnit != null) "Cari pekerjaan untuk $cleanUnit..." else "Cari jenis pekerjaan atau kode SAP...",
        emptyMessage = if (cleanUnit != null) "Tidak ada aktivitas khusus unit $cleanUnit" else "Tidak ada hasil yang cocok",
        emptySubtitle = if (cleanUnit != null) "Pilih jenis unit lain atau daftarkan di Web Admin" else "Coba kata kunci pencarian yang lain",
        items = activities,
        filterPredicate = { act, query ->
            act.activityName.contains(query, ignoreCase = true) ||
            act.sapCode.contains(query, ignoreCase = true)
        },
        onItemSelected = onActivitySelected,
        onDismissRequest = onDismissRequest
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
        else -> Color.White
    }
    val borderColor = when {
        isSelected -> Emerald600
        status.isBacklog -> Color(0xFFFCA5A5)
        status.isSusulan -> Color(0xFF86EFAC)
        else -> Slate200
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

                // Badge status: Kasus A (Backlog), Kasus B (Belum Ada Rencana), Kasus C (Sudah Ada Rencana / Tambah Susulan)
                val badgeBg = when {
                    status.isBacklog -> Color(0xFFFEE2E2)
                    status.isSusulan -> Color(0xFFDCFCE7)
                    else -> Color(0xFFEFF6FF)
                }
                val badgeBorder = when {
                    status.isBacklog -> Color(0xFFEF4444)
                    status.isSusulan -> Emerald600
                    else -> Color(0xFF93C5FD)
                }
                val badgeTextCol = when {
                    status.isBacklog -> Color(0xFF991B1B)
                    status.isSusulan -> Emerald900
                    else -> Color(0xFF1E40AF)
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

    SearchableSelectorSheet(
        isOpen = isOpen,
        title = "Pilih Unit Alat Berat",
        subtitle = if (backlogCount > 0) "${displayedUnits.size} unit ($backlogCount backlog)" else "${displayedUnits.size} unit terdaftar",
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


