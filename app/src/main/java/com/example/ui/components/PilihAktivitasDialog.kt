package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.ActivityMasterEntity

/**
 * Dialog / Sheet Pemilihan Aktivitas Kerja Lapangan Dinamis.
 *
 * Mendukung:
 * 1. Sub-Judul Dinamis sesuai unit (misal: "Sesuai Unit MGK0003 (Komatsu GD511A) • 49 Aktivitas")
 * 2. 4 Quick Filter Chips: [Semua], [Operasi], [Standby], [Breakdown]
 * 3. Search Bar responsif: Placeholder "Cari nama aktivitas atau kode SAP..."
 * 4. Card Item dengan Monospace Kode SAP, Status Badge (#059669/#ECFDF5, #D97706/#FFFBEB, #DC2626/#FEF2F2),
 *    teks Satuan, dan Checkmark hijau saat dipilih.
 */
@Composable
fun PilihAktivitasContent(
    activities: List<ActivityMasterEntity>,
    selectedActivity: ActivityMasterEntity?,
    unitCode: String? = null,
    unitModel: String? = null,
    unitType: String? = null,
    onActivitySelected: (ActivityMasterEntity) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("SEMUA") }

    // Dynamic Sub-header label based on unit
    val unitLabel = remember(unitCode, unitModel, unitType) {
        val codePart = unitCode?.trim()?.takeIf { it.isNotBlank() }
        val modelPart = unitModel?.trim()?.takeIf { it.isNotBlank() }
        val typePart = unitType?.trim()?.takeIf { it.isNotBlank() }

        when {
            codePart != null && modelPart != null -> "$codePart ($modelPart)"
            codePart != null && typePart != null -> "$codePart ($typePart)"
            codePart != null -> codePart
            modelPart != null && typePart != null -> "$typePart - $modelPart"
            typePart != null -> typePart
            modelPart != null -> modelPart
            else -> "Alat Berat Lapangan"
        }
    }

    // Filter list real-time by category chip and search query
    val filteredActivities = remember(activities, searchQuery, selectedCategory) {
        activities.filter { act ->
            val rawKat = act.kategoriStatus.ifBlank { act.category }.trim().uppercase()
            val matchesCat = when (selectedCategory) {
                "OPERASI" -> rawKat.contains("OPERASI") || rawKat == "PRODUKSI" || rawKat == "SUPPORT" || rawKat == "HAULING" || rawKat == "INFRASTRUKTUR"
                "STANDBY" -> rawKat.contains("STANDBY")
                "BREAKDOWN" -> rawKat.contains("BREAKDOWN")
                else -> true
            }

            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                act.activityName.lowercase().contains(query) ||
                act.sapCode.lowercase().contains(query) ||
                act.description.lowercase().contains(query)

            matchesCat && matchesQuery
        }.sortedWith(
            compareBy<ActivityMasterEntity> { act ->
                val cat = act.kategoriStatus.ifBlank { act.category }.trim().uppercase()
                when {
                    cat.contains("OPERASI") || cat == "PRODUKSI" || cat == "SUPPORT" || cat == "HAULING" || cat == "INFRASTRUKTUR" -> 1
                    cat.contains("STANDBY") -> 2
                    cat.contains("BREAKDOWN") -> 3
                    else -> 4
                }
            }.thenBy { it.activityName }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 16.dp, bottom = 24.dp)
            .testTag("pilih_aktivitas_dialog_content")
    ) {
        // 1. Header with Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pilih Aktivitas Kerja Lapangan",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Sub-Judul Dinamis sesuai Unit (Bukan statis EXCAVATOR)
                Text(
                    text = "Sesuai Unit $unitLabel • ${filteredActivities.size} aktivitas",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("pilih_aktivitas_subtitle")
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_aktivitas_dialog_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Search Bar responsif
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("search_aktivitas_input"),
            placeholder = {
                Text(
                    text = "Cari nama aktivitas atau kode SAP...",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF64748B)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF059669),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Quick Filter Kategori Status Chips: [Semua], [Operasi], [Standby], [Breakdown]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf(
                "SEMUA" to "Semua",
                "OPERASI" to "Operasi",
                "STANDBY" to "Standby",
                "BREAKDOWN" to "Breakdown"
            )

            categories.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategory == catKey
                val (bgCol, txtCol, borderCol) = when {
                    isSelected && catKey == "OPERASI" -> Triple(Color(0xFFECFDF5), Color(0xFF059669), Color(0xFF059669))
                    isSelected && catKey == "STANDBY" -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFFD97706))
                    isSelected && catKey == "BREAKDOWN" -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFFDC2626))
                    isSelected -> Triple(Color(0xFF059669), Color.White, Color(0xFF059669))
                    else -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFFE2E8F0))
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = bgCol,
                    border = BorderStroke(1.dp, borderCol),
                    modifier = Modifier
                        .clickable { selectedCategory = catKey }
                        .testTag("filter_chip_$catKey")
                ) {
                    Text(
                        text = catLabel,
                        color = txtCol,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

        // 4. Daftar Item Aktivitas Lapangan
        if (filteredActivities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            "Tidak ada aktivitas yang cocok dengan '$searchQuery'"
                        else
                            "Tidak ada aktivitas untuk kategori $selectedCategory",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih kategori status lain atau periksa kata kunci",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 460.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredActivities,
                    key = { "${it.id}_${it.sapCode}_${it.activityName}" }
                ) { activity ->
                    val isSelected = (selectedActivity?.id != 0L && selectedActivity?.id == activity.id) ||
                        (selectedActivity?.sapCode?.isNotBlank() == true && selectedActivity.sapCode == activity.sapCode && selectedActivity.activityName == activity.activityName) ||
                        (selectedActivity?.activityName == activity.activityName && selectedActivity?.kategoriStatus == activity.kategoriStatus)
                    PilihAktivitasItemCard(
                        activity = activity,
                        isSelected = isSelected,
                        onClick = {
                            onActivitySelected(activity)
                            onClose()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Card Item Aktivitas dengan badge Monospace Kode SAP, Status Colors, dan Satuan.
 */
@Composable
fun PilihAktivitasItemCard(
    activity: ActivityMasterEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("pilih_aktivitas_item_${activity.sapCode}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFECFDF5) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFF059669) else Color(0xFFE2E8F0)
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
                        color = Color(0xFF0F172A)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chip Badge Kode SAP with Monospace font
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = activity.sapCode,
                            color = Color(0xFF334155),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    // Badge Status (OPERASI / STANDBY / BREAKDOWN)
                    // OPERASI: Hijau (#059669 text, #ECFDF5 bg)
                    // STANDBY: Kuning/Amber (#D97706 text, #FFFBEB bg)
                    // BREAKDOWN: Merah (#DC2626 text, #FEF2F2 bg)
                    val rawStatus = activity.kategoriStatus.ifBlank { activity.category }.ifBlank { "OPERASI" }.uppercase()
                    val (statusText, statusBg, statusTxt) = when {
                        rawStatus.contains("STANDBY") -> Triple("STANDBY", Color(0xFFFFFBEB), Color(0xFFD97706))
                        rawStatus.contains("BREAKDOWN") -> Triple("BREAKDOWN", Color(0xFFFEF2F2), Color(0xFFDC2626))
                        else -> Triple("OPERASI", Color(0xFFECFDF5), Color(0xFF059669))
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

                    // Teks Satuan: "Satuan: ${item.satuan ?: '-'}"
                    val satuanText = activity.unitOfMeasure.ifBlank { activity.satuan }.ifBlank { "-" }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF8FAFC)
                    ) {
                        Text(
                            text = "Satuan: $satuanText",
                            color = Color(0xFF475569),
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
                        .background(Color(0xFF059669), CircleShape),
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
 * Dialog Popup Pemilihan Aktivitas Kerja Lapangan
 */
@Composable
fun PilihAktivitasDialog(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    activities: List<ActivityMasterEntity>,
    selectedActivity: ActivityMasterEntity?,
    unitCode: String? = null,
    unitModel: String? = null,
    unitType: String? = null,
    onActivitySelected: (ActivityMasterEntity) -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            PilihAktivitasContent(
                activities = activities,
                selectedActivity = selectedActivity,
                unitCode = unitCode,
                unitModel = unitModel,
                unitType = unitType,
                onActivitySelected = onActivitySelected,
                onClose = onDismissRequest
            )
        }
    }
}

/**
 * Modal Bottom Sheet Pemilihan Aktivitas Kerja Lapangan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilihAktivitasBottomSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    activities: List<ActivityMasterEntity>,
    selectedActivity: ActivityMasterEntity?,
    unitCode: String? = null,
    unitModel: String? = null,
    unitType: String? = null,
    onActivitySelected: (ActivityMasterEntity) -> Unit
) {
    if (!isOpen) return

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(4.dp),
                shape = CircleShape,
                color = Color(0xFFCBD5E1)
            ) {}
        }
    ) {
        PilihAktivitasContent(
            activities = activities,
            selectedActivity = selectedActivity,
            unitCode = unitCode,
            unitModel = unitModel,
            unitType = unitType,
            onActivitySelected = onActivitySelected,
            onClose = onDismissRequest
        )
    }
}
