package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MasterRealisasiEntity
import com.example.data.local.entity.RencanaKerjaEntity

// Palet Warna Enterprise RKCE
private val BrandEmerald = Color(0xFF009663)
private val BrandEmeraldLight = Color(0xFFE8F5E9)
private val DarkSlate = Color(0xFF0F172A)
private val MutedSlate = Color(0xFF64748B)
private val SoftBorder = Color(0xFFE2E8F0)
private val BackgroundCanvas = Color(0xFFF8FAFC)
private val AmberBacklog = Color(0xFFF59E0B)
private val AmberBacklogBg = Color(0xFFFEF3C7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputRealisasiScreen(
    rencana: RencanaKerjaEntity,
    existingRealisasi: MasterRealisasiEntity? = null, // null jika Input Baru, ada isinya jika Koreksi Kasie
    isKasie: Boolean = false,
    onNavigateBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    onSaveRealisasi: (MasterRealisasiEntity) -> Unit
) {
    val context = LocalContext.current
    val isKoreksiMode = existingRealisasi != null

    // ======================================================================
    // LOGIKA PENETAPAN NILAI AWAL (KUNCI UTAMA KOREKSI VS INPUT BARU)
    // ======================================================================
    // 1. HM Awal: Jika koreksi, WAJIB ambil dari existingRealisasi.hmAwal
    //    Jika input baru, ambil dari rencana.hmAwal / default sistem
    var hmAwalText by remember(existingRealisasi, rencana) {
        val initialVal = if (isKoreksiMode) {
            existingRealisasi?.hmAwal ?: 0.0
        } else {
            0.0
        }
        mutableStateOf(if (initialVal > 0) initialVal.toString() else "0.0")
    }

    // 2. HM Akhir: Jika koreksi, WAJIB ambil dari existingRealisasi.hmAkhir (Bukan HM unit terbaru)
    var hmAkhirText by remember(existingRealisasi) {
        val initialVal = if (isKoreksiMode) {
            existingRealisasi?.hmAkhir?.let { if (it > 0) it.toString() else "" } ?: ""
        } else ""
        mutableStateOf(initialVal)
    }

    // 3. Stik Solar & Konsumsi
    var stikAwalText by remember(existingRealisasi) {
        mutableStateOf(if (isKoreksiMode) existingRealisasi?.stikSolarAwal?.toString() ?: "85.0" else "85.0")
    }
    var stikAkhirText by remember(existingRealisasi) {
        mutableStateOf(if (isKoreksiMode && (existingRealisasi?.stikSolarAkhir ?: 0.0) > 0) existingRealisasi?.stikSolarAkhir?.toString() ?: "" else "")
    }
    var solarKonsumsiText by remember(existingRealisasi) {
        val initialVal = if (isKoreksiMode && (existingRealisasi?.konsumsiSolar ?: 0.0) > 0.0) {
            existingRealisasi?.konsumsiSolar?.toString() ?: ""
        } else ""
        mutableStateOf(initialVal)
    }

    // 4. Hasil Kerja, Satuan & Keterangan
    var hasilKerjaText by remember(existingRealisasi) {
        val initialVal = if (isKoreksiMode && (existingRealisasi?.hasilKerja ?: 0.0) > 0.0) {
            existingRealisasi?.hasilKerja?.toString() ?: ""
        } else ""
        mutableStateOf(initialVal)
    }
    var keteranganText by remember(existingRealisasi) {
        mutableStateOf(existingRealisasi?.keterangan ?: "")
    }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    // ======================================================================
    // KALKULASI TOTAL SECARA REAKTIF
    // ======================================================================
    val hmAwal = hmAwalText.toDoubleOrNull() ?: 0.0
    val hmAkhir = hmAkhirText.toDoubleOrNull() ?: 0.0
    val totalHm = if (hmAkhir >= hmAwal) (hmAkhir - hmAwal) else 0.0

    // Auto hitung solar jika stik akhir diisi: (Stik Awal - Stik Akhir) * rasio tangki
    val stikAwal = stikAwalText.toDoubleOrNull() ?: 0.0
    val stikAkhir = stikAkhirText.toDoubleOrNull() ?: 0.0
    LaunchedEffect(stikAwal, stikAkhir) {
        if (stikAkhir > 0.0 && stikAwal >= stikAkhir && solarKonsumsiText.isEmpty()) {
            solarKonsumsiText = String.format("%.1f", (stikAwal - stikAkhir))
        }
    }

    val isInputValid = hmAkhir > hmAwal && totalHm > 0.0

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF1B3A57), tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isKoreksiMode) "Koreksi Realisasi Kasie" else "Input Realisasi Lapangan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isKoreksiMode) "Memperbaiki & memvalidasi data operator" else "Catat operasional unit harian",
                            fontSize = 10.sp,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }
            }
        },
        containerColor = BackgroundCanvas
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. HEADER PENUGASAN COMPACT (~56dp)
            CompactTaskHeader(rencana = rencana)

            // 2. KARTU HOUR METER (HM) - GRID 50% / 50%
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, SoftBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1. Hour Meter (HM) Mesin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        if (isKoreksiMode) {
                            Text("Mode Koreksi", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AmberBacklog)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kolom HM Awal (Read-only dengan style jelas)
                        OutlinedTextField(
                            value = hmAwalText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("HM Awal", fontSize = 10.sp) },
                            trailingIcon = { Text("HM", fontSize = 10.sp, color = MutedSlate, modifier = Modifier.padding(end = 8.dp)) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(6.dp),
                            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = SoftBorder,
                                disabledContainerColor = Color(0xFFF1F5F9),
                                disabledTextColor = DarkSlate
                            ),
                            enabled = false
                        )

                        Text("➔", fontSize = 14.sp, color = MutedSlate)

                        // Kolom HM Akhir (Aktif Diisi)
                        OutlinedTextField(
                            value = hmAkhirText,
                            onValueChange = { hmAkhirText = it },
                            label = { Text("HM Akhir *", fontSize = 10.sp) },
                            placeholder = { Text("Contoh: 21.0", fontSize = 11.sp) },
                            trailingIcon = { Text("HM", fontSize = 10.sp, color = BrandEmerald, modifier = Modifier.padding(end = 8.dp)) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(6.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandEmerald,
                                unfocusedBorderColor = if (hmAkhirText.isNotEmpty() && !isInputValid) Color.Red else SoftBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Shortcut Chips Cepat (+4j, +7j, +8j, +10j)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(4.0, 7.0, 8.0, 10.0).forEach { hours ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandEmeraldLight,
                                border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    hmAkhirText = String.format("%.1f", hmAwal + hours)
                                }
                            ) {
                                Text(
                                    text = "+${hours.toInt()} Jam",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Total HM Inline Banner
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (totalHm in 6.0..10.0) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (totalHm in 6.0..10.0) BrandEmerald.copy(alpha = 0.3f) else SoftBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total: +${String.format("%.1f", totalHm)} HM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalHm > 0) BrandEmerald else MutedSlate
                            )
                            Text(
                                text = if (totalHm in 6.0..10.0) "Normal (6 - 10 HM)" else "Standar: 6-10 HM",
                                fontSize = 9.sp,
                                color = MutedSlate
                            )
                        }
                    }
                }
            }

            // 3. KARTU BAHAN BAKAR & HASIL KERJA (2 KOLOM SEJAJAR)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, SoftBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sisi Kiri: Solar
                    Column(modifier = Modifier.weight(1f)) {
                        Text("2. Konsumsi Solar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = stikAwalText,
                                onValueChange = { stikAwalText = it },
                                label = { Text("Stik Awal", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f).height(46.dp),
                                textStyle = TextStyle(fontSize = 11.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = stikAkhirText,
                                onValueChange = { stikAkhirText = it },
                                label = { Text("Stik Akhir", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f).height(46.dp),
                                textStyle = TextStyle(fontSize = 11.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = solarKonsumsiText,
                            onValueChange = { solarKonsumsiText = it },
                            label = { Text("Total Solar (Liter)", fontSize = 9.sp) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            textStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    // Sisi Kanan: Hasil Kerja
                    Column(modifier = Modifier.weight(1f)) {
                        Text("3. Hasil Operasional", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = hasilKerjaText,
                            onValueChange = { hasilKerjaText = it },
                            label = { Text("Hasil Kerja *", fontSize = 9.sp) },
                            placeholder = { Text("Contoh: 5.0", fontSize = 10.sp) },
                            trailingIcon = { Text(rencana.satuan, fontSize = 10.sp, color = MutedSlate, modifier = Modifier.padding(end = 6.dp)) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Satuan tugas: ${rencana.satuan}",
                            fontSize = 9.sp,
                            color = MutedSlate
                        )
                    }
                }
            }

            // 4. DOKUMENTASI & CATATAN SLIM
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, SoftBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kolom Keterangan (Tinggi lega 58dp agar teks tidak terpotong)
                    OutlinedTextField(
                        value = keteranganText,
                        onValueChange = { keteranganText = it },
                        placeholder = { Text("Catatan kendala / cuaca (opsional)...", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                        modifier = Modifier.weight(2.5f).height(58.dp),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 11.sp),
                        maxLines = 2
                    )

                    // Tombol Foto Ringkas
                    OutlinedButton(
                        onClick = { photoLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).height(58.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (selectedImageUri != null) BrandEmerald else SoftBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedImageUri != null) BrandEmeraldLight else Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedImageUri != null) Icons.Default.Check else Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = if (selectedImageUri != null) BrandEmerald else MutedSlate,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (selectedImageUri != null) "Terlampir" else "Foto",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedImageUri != null) BrandEmerald else MutedSlate
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. TOMBOL SUBMIT SOLID TUNGGAL (Tinggi 42dp)
            Button(
                onClick = {
                    if (!isInputValid) {
                        Toast.makeText(context, "HM Akhir harus lebih besar dari HM Awal!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val updatedRealisasi = MasterRealisasiEntity(
                        id = existingRealisasi?.id ?: 0L,
                        nomorSpk = rencana.nomorSpk,
                        tanggal = rencana.tanggal,
                        shift = rencana.shift,
                        kodeUnit = rencana.kodeUnit,
                        jenisUnit = rencana.kategori,
                        operator = rencana.operator,
                        kasie = rencana.kasie,
                        wilayah = rencana.wilayah,
                        namaAktivitas = rencana.namaAktivitas,
                        lokasi = rencana.lokasi,
                        hmAwal = hmAwal,
                        hmAkhir = hmAkhir,
                        totalHm = totalHm,
                        stikSolarAwal = stikAwalText.toDoubleOrNull() ?: 0.0,
                        stikSolarAkhir = stikAkhirText.toDoubleOrNull() ?: 0.0,
                        konsumsiSolar = solarKonsumsiText.toDoubleOrNull() ?: 0.0,
                        hasilKerja = hasilKerjaText.toDoubleOrNull() ?: 0.0,
                        satuan = rencana.satuan,
                        keterangan = keteranganText.ifBlank { null },
                        approvalStatus = if (isKasie) "Disetujui Kasie" else "Menunggu",
                        isLocked = isKasie
                    )

                    onSaveRealisasi(updatedRealisasi)
                    onSubmitSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                enabled = isInputValid,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandEmerald,
                    disabledContainerColor = Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8)
                )
            ) {
                Icon(
                    imageVector = if (isKoreksiMode) Icons.Outlined.Edit else Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isKoreksiMode) "✔ Simpan Koreksi & Validasi" else "✔ Kirim Realisasi Lapangan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ======================================================================
// KOMPONEN RANGKUMAN PENUGASAN (COMPACT STICKY HEADER ~56dp)
// ======================================================================
@Composable
fun CompactTaskHeader(rencana: RencanaKerjaEntity) {
    val isSiang = rencana.shift.equals("SIANG", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SoftBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            // Baris 1: Unit • Shift • Kategori • SPK Mini
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(rencana.kodeUnit, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSiang) BrandEmeraldLight else Color(0xFFEDE7F6))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isSiang) "SIANG" else "MALAM",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSiang) BrandEmerald else Color(0xFF512DA8)
                        )
                    }
                    if (rencana.isBacklog || rencana.targetTerkunci) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(AmberBacklogBg)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("BACKLOG", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AmberBacklog)
                        }
                    }
                }
                Text(rencana.nomorSpk.takeLast(12), fontSize = 9.sp, color = Color(0xFF94A3B8))
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Baris 2: Nama Aktivitas
            Text(
                text = rencana.namaAktivitas,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Baris 3: Operator • Lokasi • Tanggal
            Text(
                text = "👤 ${rencana.operator} • 📍 ${rencana.lokasi} • 📅 ${rencana.tanggal}",
                fontSize = 9.sp,
                color = MutedSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
