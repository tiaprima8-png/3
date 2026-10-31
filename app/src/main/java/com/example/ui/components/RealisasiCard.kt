package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserSessionData
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.MasterRealisasiEntity
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import java.util.Locale

/**
 * Helper untuk mengecek apakah laporan masih menunggu verifikasi Kasie.
 * Mengembalikan false jika laporan sudah dikunci permanen.
 */
fun isNeedApproval(item: MasterRealisasiEntity): Boolean {
    // Jika sudah dikunci permanen, jangan tampilkan tombol approve
    if (item.isLocked) return false

    val status = item.approvalStatus?.lowercase()?.trim() ?: ""
    return status.contains("menunggu") || status.isEmpty() || status != "disetujui kasie"
}

fun isNeedApproval(item: ActivityReportEntity): Boolean {
    if (item.isLocked) return false

    val status = item.approvalStatus.lowercase().trim()
    return status.contains("menunggu") || status.isEmpty() || status != "disetujui kasie"
}

/**
 * Komponen Kartu Realisasi Lapangan dengan tombol Koreksi & Setujui Realisasi (Approve).
 */
@Composable
fun RealisasiCard(
    item: MasterRealisasiEntity,
    currentUser: UserSessionData? = null,
    isKasie: Boolean = currentUser?.role?.contains("kasie", ignoreCase = true) == true,
    onKoreksiClick: (MasterRealisasiEntity) -> Unit = {},
    onApproveClick: (MasterRealisasiEntity) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val isKasieUser = isKasie || (currentUser?.role?.contains("kasie", ignoreCase = true) == true)
    val displaySpk = item.nomorSpk.ifBlank { "SPK Menyusul" }
    val isSpkOfficial = item.nomorSpk.isNotBlank() &&
            !item.nomorSpk.equals("null", ignoreCase = true) &&
            !item.nomorSpk.equals("SPK Menyusul", ignoreCase = true) &&
            item.nomorSpk != "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .testTag("realisasi_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Kode Unit, Shift, Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.kodeUnit.ifBlank { "HE-UNIT" },
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
                            text = item.jenisUnit?.ifBlank { "HE" } ?: "HE",
                            color = Slate700,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.tanggal,
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                    )
                }

                // Shift & SPK
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (item.shift.equals("MALAM", ignoreCase = true)) Color(0xFF1E293B) else Color(0xFFEFF6FF)
                ) {
                    Text(
                        text = item.shift.uppercase(),
                        color = if (item.shift.equals("MALAM", ignoreCase = true)) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-Header: SPK Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSpkOfficial) Emerald50 else Color(0xFFFEF3C7),
                    border = BorderStroke(
                        1.dp,
                        if (isSpkOfficial) Emerald200 else Color(0xFFFDE68A)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSpkOfficial) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isSpkOfficial) Emerald700 else Color(0xFFB45309),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSpkOfficial) "SPK: $displaySpk" else "SPK: Menyusul (Admin)",
                            color = if (isSpkOfficial) Emerald900 else Color(0xFF92400E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!item.wilayah.isNullOrBlank()) {
                    Text(
                        text = "Wilayah: ${item.wilayah}",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Box Konten Realisasi
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Aktivitas & Lokasi
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF009663), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = item.namaAktivitas.ifBlank { "Operasional Lapangan" },
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lokasi: ${item.lokasi.ifBlank { "Area Kerja" }}",
                            fontSize = 11.sp,
                            color = Slate700,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // HM Meteran & Durasi Running
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HM: ${String.format(Locale.US, "%.1f", item.hmAwal)} ➔ ${String.format(Locale.US, "%.1f", item.hmAkhir)}",
                            fontSize = 11.sp,
                            color = Slate600
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Emerald50,
                            border = BorderStroke(1.dp, Emerald100)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = Emerald700, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${String.format(Locale.US, "%.2f", item.totalHm)} HM",
                                    color = Emerald800,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Solar & Hasil Kerja
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color(0xFF009663), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Solar: ${String.format(Locale.US, "%.1f", item.konsumsiSolar)} L",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                        }

                        if (item.hasilKerja > 0.0) {
                            Text(
                                text = "Hasil: ${String.format(Locale.US, "%.1f", item.hasilKerja)} ${item.satuan ?: "ha"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald800
                            )
                        }
                    }

                    // Personel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kasie: ${item.kasie.ifBlank { "Kasie" }}",
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Operator: ${item.operator.ifBlank { "Operator" }}",
                                fontSize = 10.sp,
                                color = Slate700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Kendala jika ada
                    if (!item.kendala.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = item.kendala,
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge: Terkunci vs Menunggu
            if (item.isLocked || !isNeedApproval(item)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200),
                    modifier = Modifier.fillMaxWidth().testTag("badge_locked_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci",
                            tint = Emerald700,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✔ Disetujui oleh ${item.kasie.ifBlank { "Kasie" }} • Terkunci",
                            color = Emerald900,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth().testTag("badge_pending_approval_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Menunggu Verifikasi",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Menunggu Verifikasi Kasie",
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // TAMPILKAN TOMBOL KOREKSI & SETUJUI:
            if (isKasieUser && isNeedApproval(item)) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tombol Koreksi (OutlinedButton)
                    OutlinedButton(
                        onClick = { onKoreksiClick(item) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_koreksi_${item.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Koreksi", fontSize = 13.sp)
                    }

                    // Tombol Setujui Realisasi (Button)
                    Button(
                        onClick = { onApproveClick(item) },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("btn_approve_${item.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF009663) // Hijau M3 Forest
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("✔ Setujui Realisasi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Overload RealisasiCard untuk ActivityReportEntity agar kompatibel penuh.
 */
@Composable
fun RealisasiCard(
    report: ActivityReportEntity,
    currentUser: UserSessionData? = null,
    isKasie: Boolean = currentUser?.role?.contains("kasie", ignoreCase = true) == true,
    onKoreksiClick: (ActivityReportEntity) -> Unit = {},
    onApproveClick: (ActivityReportEntity) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val master = MasterRealisasiEntity.fromActivityReport(report)
    RealisasiCard(
        item = master,
        currentUser = currentUser,
        isKasie = isKasie,
        onKoreksiClick = { onKoreksiClick(report) },
        onApproveClick = { onApproveClick(report) },
        onClick = onClick
    )
}
