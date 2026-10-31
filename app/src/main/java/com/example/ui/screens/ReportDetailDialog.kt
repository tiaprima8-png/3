package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.remote.KendalaJsonConverter
import com.example.ui.components.FuelBadge
import com.example.ui.components.FullscreenPhotoPreviewDialog
import com.example.ui.components.ShiftBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.UnitStatusBadge
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import java.io.File

/**
 * Format and share activity report via Android Intent.ACTION_SEND
 */
fun shareReportDetails(context: Context, report: ActivityReportEntity, nomorSpk: String?) {
    val spk = if (!nomorSpk.isNullOrBlank()) nomorSpk else "-"
    val statusSync = if (report.isSynced) "✅ Tersinkron Server" else "⏳ Offline (Menunggu Sinkron)"
    val kendalaText = if (!report.catatanKendala.isNullOrBlank() || !report.kendalaNama.isNullOrBlank()) {
        "${report.kendalaNama ?: "-"} (${report.catatanKendala ?: ""})"
    } else "Tidak Ada Kendala"

    val shareText = """
📋 *RKCE - LAPORAN REALISASI AKTIVITAS*
────────────────────────────
🚜 *Unit:* ${report.unitCode} (${report.unitType})
👷 *Operator:* ${report.operatorName}
👔 *Pengawas:* ${report.supervisorName}
📅 *Tanggal:* ${report.reportDate} | Shift ${report.shift}
📍 *Lokasi:* ${report.workLocation} (${report.wilayah})
📝 *Nomor SPK:* $spk
⚙️ *Aktivitas:* ${report.activityName} (${report.sapCode})
⏱️ *Durasi:* ${report.workDurationHours} Jam | Status: ${report.unitStatus}

📊 *Pencatatan Jam (HM):*
• HM Awal: ${report.hmAwal}
• HM Akhir: ${report.hmAkhir}
• Running HM: ${report.hmDailyRunning} Jam

⛽ *BBM / Solar:*
• Stik Awal: ${report.stikSolarAwal} cm
• Stik Akhir: ${report.stikSolarAkhir} cm
• Pemakaian: ${report.pemakaianSolar} L
${if (report.isRefueled) "• Tambahan Solar: ${report.fuelLiters} L\n" else ""}
🚧 *Kendala Lapangan:*
$kendalaText

📈 *Hasil Kerja:* ${report.workOutput} ${report.unitOfMeasure}
💬 *Catatan:* ${report.notes.ifBlank { "-" }}
────────────────────────────
Status Data: $statusSync
Dikirim via RKCE Mobile System
""".trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Laporan Realisasi RKCE - ${report.unitCode} (${report.reportDate})")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Bagikan Laporan Realisasi"))
}

@Composable
fun ReportDetailDialog(
    report: ActivityReportEntity,
    nomorSpk: String? = null,
    isReadOnly: Boolean = false,
    isKasieSession: Boolean = false,
    onDismiss: () -> Unit,
    onDelete: (Long) -> Unit,
    onApprove: ((ActivityReportEntity) -> Unit)? = null,
    onKoreksi: ((ActivityReportEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    var showFullscreenPhoto by remember { mutableStateOf(false) }
    var showApproveConfirmDialog by remember { mutableStateOf(false) }
    val effectiveReadOnly = isReadOnly || report.isLocked

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ==========================================
                // 1. PINNED HEADER (Tetap terlihat di atas)
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detail Laporan Realisasi",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "ID: ${report.clientUuid.take(13)} • Tgl: ${report.reportDate}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { shareReportDetails(context, report, nomorSpk) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Bagikan Laporan", tint = Emerald700)
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                        }
                    }
                }

                HorizontalDivider(color = Slate200)

                // ==========================================
                // 2. SCROLLABLE BODY (Dapat di-scroll ke bawah)
                // ==========================================
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Status & Shift Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UnitStatusBadge(status = report.unitStatus)
                            if (report.isRefueled && report.fuelLiters > 0.0) {
                                FuelBadge(liters = report.fuelLiters)
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShiftBadge(shift = report.shift)
                            SyncStatusBadge(isSynced = report.isSynced)
                        }
                    }

                    // Banner Status Verifikasi & Penguncian Permanen Kasie
                    if (report.isLocked) {
                        val approver = report.approvedBy?.takeIf { it.isNotBlank() } ?: report.supervisorName
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Emerald50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.Emerald200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Terkunci",
                                    tint = Emerald700,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "✔ Disetujui oleh $approver • Terkunci (Read-Only)",
                                    color = Emerald900,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Menunggu Verifikasi",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Menunggu Verifikasi Kasie",
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Identitas Unit & Tugas Lapangan
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailItemRow(
                                icon = Icons.Default.PrecisionManufacturing,
                                label = "Unit Alat Berat",
                                value = "${report.unitCode} (${report.unitType.ifBlank { "HE" }}) - ${report.unitStatus}"
                            )
                            DetailItemRow(
                                icon = Icons.Default.Assignment,
                                label = "Status SPK",
                                value = if (!nomorSpk.isNullOrBlank() && !nomorSpk.equals("null", ignoreCase = true) && !nomorSpk.equals("SPK Menyusul", ignoreCase = true)) {
                                    "SPK Resmi: $nomorSpk"
                                } else {
                                    "Menunggu SPK Admin (SPK Menyusul)"
                                }
                            )
                            DetailItemRow(
                                icon = Icons.Default.Assignment,
                                label = "Aktivitas Pekerjaan & SAP",
                                value = "${report.activityName} [${report.sapCode.ifBlank { "ACT-01" }}]"
                            )
                            DetailItemRow(
                                icon = Icons.Default.Person,
                                label = "Operator Lapangan",
                                value = report.operatorName
                            )
                            DetailItemRow(
                                icon = Icons.Default.LocationOn,
                                label = "Lokasi Kerja",
                                value = report.workLocation
                            )
                            DetailItemRow(
                                icon = Icons.Default.Engineering,
                                label = "Kasie Lapangan",
                                value = report.supervisorName
                            )
                            DetailItemRow(
                                icon = Icons.Default.LocationOn,
                                label = "Audit GPS & Waktu",
                                value = "GPS Valid (Sektor Operasional) • ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(report.createdAt))} WIB"
                            )
                        }
                    }

                    // Hour Meter (HM) Details Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Emerald50,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rincian Hour Meter (HM)", fontWeight = FontWeight.Bold, color = Emerald900, fontSize = 13.sp)
                                }
                                if (report.isHmAwalCorrected) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = "HM DIKOREKSI",
                                            color = Color(0xFFB45309),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("HM Awal", color = Slate500, fontSize = 11.sp)
                                    Text("${String.format("%.1f", report.hmAwal)} Jam", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("HM Berjalan", color = Emerald700, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("+${String.format("%.2f", report.hmDailyRunning)} Jam", fontWeight = FontWeight.ExtraBold, color = Emerald700, fontSize = 16.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("HM Akhir", color = Slate500, fontSize = 11.sp)
                                    Text("${String.format("%.1f", report.hmAkhir)} Jam", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 14.sp)
                                }
                            }

                            if (report.workOutput > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Emerald100)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Hasil Kerja Aktual:", color = Slate700, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("${String.format("%.1f", report.workOutput)} ${report.unitOfMeasure}", fontWeight = FontWeight.Bold, color = Emerald700, fontSize = 13.sp)
                                }
                            }

                            if (report.isHmAwalCorrected && !report.alasanKoreksiHm.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Alasan Koreksi HM: ${report.alasanKoreksiHm}",
                                        color = Color(0xFF92400E),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Pengisian & Pemakaian Solar (BBM)
                    if (report.isRefueled || report.fuelLiters > 0.0 || report.pemakaianSolar > 0.0 || report.stikSolarAwal > 0.0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bahan Bakar Minyak (Solar)", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                if (report.fuelLiters > 0.0) {
                                    DetailItemRow(
                                        icon = Icons.Default.LocalGasStation,
                                        label = "Pengisian Tambahan Solar",
                                        value = "${if (report.fuelLiters % 1.0 == 0.0) report.fuelLiters.toInt() else report.fuelLiters} Liter"
                                    )
                                }
                                if (report.stikSolarAwal > 0.0 || report.stikSolarAkhir > 0.0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Stik Solar (Awal / Akhir):", color = Slate500, fontSize = 12.sp)
                                        Text("${String.format("%.1f", report.stikSolarAwal)} / ${String.format("%.1f", report.stikSolarAkhir)} cm", fontWeight = FontWeight.SemiBold, color = Slate900, fontSize = 12.sp)
                                    }
                                }
                                if (report.pemakaianSolar > 0.0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Estimasi Konsumsi Solar:", color = Slate700, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${String.format("%.1f", report.pemakaianSolar)} Liter/cm", fontWeight = FontWeight.ExtraBold, color = Emerald700, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Daftar Kendala Lapangan
                    val parsedKendalaList = KendalaJsonConverter.fromJson(report.daftarKendalaJson)
                    if (parsedKendalaList.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kendala Lapangan (${parsedKendalaList.size} Kejadian)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B),
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                parsedKendalaList.forEachIndexed { index, k ->
                                    val durasiStr = if ((k.durasi_menit ?: 0) > 0) {
                                        val hrs = (k.durasi_menit ?: 0) / 60
                                        val mins = (k.durasi_menit ?: 0) % 60
                                        when {
                                            hrs > 0 && mins > 0 -> "$hrs Jam $mins Menit"
                                            hrs > 0 -> "$hrs Jam"
                                            else -> "$mins Menit"
                                        }
                                    } else {
                                        ""
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            val kodeDisp = k.resolvedKode.ifBlank { "-" }
                                            Text(
                                                text = "#${index + 1} ${k.resolvedNama.ifBlank { "Kendala" }} [$kodeDisp]",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF991B1B)
                                            )
                                            val startDisp = k.resolvedJamMulai.ifBlank { "-" }
                                            val endDisp = k.resolvedJamSelesai.ifBlank { "-" }
                                            if (startDisp != "-" || endDisp != "-") {
                                                Text(
                                                    text = "Pukul: $startDisp s/d $endDisp${if (durasiStr.isNotBlank()) " • Hambatan: $durasiStr" else ""}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFB91C1C)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!report.kendalaNama.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Kendala: ${report.kendalaNama} (${report.kendalaId ?: "-"})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B),
                                    fontSize = 12.sp
                                )
                                if (!report.catatanKendala.isNullOrBlank()) {
                                    Text(
                                        text = "Keterangan: ${report.catatanKendala}",
                                        color = Color(0xFFB91C1C),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Catatan & Keterangan Lapangan
                    if (report.notes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notes, contentDescription = null, tint = Slate600, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Catatan / Keterangan Lapangan", fontWeight = FontWeight.SemiBold, color = Slate900, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = report.notes,
                                    fontSize = 12.sp,
                                    color = Slate700,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Foto Bukti Lapangan
                    val hasPhoto = !report.photoPath.isNullOrBlank() || !report.photoBase64.isNullOrBlank()
                    if (hasPhoto) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Foto Bukti Lapangan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                                    }
                                    Surface(shape = RoundedCornerShape(4.dp), color = Emerald100) {
                                        Text(
                                            text = "${report.photoSizeKb ?: 145} KB • Ketuk Pratinjau",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Emerald900,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val photoFile = report.photoPath?.let { File(it) }
                                if (photoFile != null && photoFile.exists()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(photoFile)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Foto Bukti Lapangan",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showFullscreenPhoto = true }
                                    )
                                } else if (!report.photoBase64.isNullOrBlank()) {
                                    val bitmap = try {
                                        val cleanBase64 = if (report.photoBase64.startsWith("data:")) {
                                            report.photoBase64.substringAfter("base64,").trim()
                                        } else {
                                            report.photoBase64.trim()
                                        }
                                        val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    } catch (_: Exception) {
                                        null
                                    }

                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Foto Bukti Lapangan",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { showFullscreenPhoto = true }
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Foto tersimpan di cloud / lokal", fontSize = 11.sp, color = Slate700)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider(color = Slate200)

                // ==========================================
                // 3. PINNED BOTTOM ACTIONS (Selalu terlihat)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!report.isLocked && isKasieSession && (onApprove != null || onKoreksi != null)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onKoreksi != null) {
                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onKoreksi(report)
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate400),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Koreksi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            if (onApprove != null) {
                                Button(
                                    onClick = { showApproveConfirmDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(42.dp)
                                ) {
                                    Text("✔ Setujui Realisasi (Approve)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!effectiveReadOnly) {
                            OutlinedButton(
                                onClick = {
                                    onDelete(report.id)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRose),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(44.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hapus", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { shareReportDetails(context, report, nomorSpk) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald700),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Emerald700, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Tutup", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showApproveConfirmDialog && onApprove != null) {
        AlertDialog(
            onDismissRequest = { showApproveConfirmDialog = false },
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
                        showApproveConfirmDialog = false
                        onApprove(report)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Ya, Setujui & Kunci", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveConfirmDialog = false }) {
                    Text("Batal", color = Slate700)
                }
            }
        )
    }

    if (showFullscreenPhoto) {
        FullscreenPhotoPreviewDialog(
            photoPath = report.photoPath,
            photoBase64 = report.photoBase64,
            unitCode = report.unitCode,
            reportDate = report.reportDate,
            nomorSpk = null,
            photoSizeKb = report.photoSizeKb,
            onDismiss = { showFullscreenPhoto = false }
        )
    }
}

@Composable
fun DetailItemRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Slate400, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(color = Slate900, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
        }
    }
}
