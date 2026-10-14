package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.io.File

/**
 * Parses image source model from either a local file path or base64 (raw or data:image/jpeg;base64, URL).
 */
@Composable
fun rememberImageModel(photoPath: String?, photoBase64: String?): Any? {
    val context = LocalContext.current
    return remember(photoPath, photoBase64) {
        val file = photoPath?.let { File(it) }
        if (file != null && file.exists()) {
            file
        } else if (!photoBase64.isNullOrBlank()) {
            try {
                val cleanBase64 = if (photoBase64.startsWith("data:")) {
                    photoBase64.substringAfter("base64,").trim()
                } else {
                    photoBase64.trim()
                }
                val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }
}

/**
 * 64x64 dp photo thumbnail displayed inside the report history card.
 * Shows rounded corners, center-crop thumbnail, or a clean neutral placeholder if no photo.
 */
@Composable
fun ReportPhotoThumbnail(
    photoPath: String?,
    photoBase64: String?,
    photoSizeKb: Long? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageModel = rememberImageModel(photoPath, photoBase64)
    val hasPhoto = imageModel != null

    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (hasPhoto) Color(0xFFF1F5F9) else Color(0xFFF8FAFC))
            .border(1.dp, if (hasPhoto) Emerald100 else Slate200, RoundedCornerShape(10.dp))
            .then(
                if (onClick != null && hasPhoto) {
                    Modifier
                        .clickable { onClick() }
                        .testTag("report_photo_thumbnail")
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (hasPhoto) {
            when (imageModel) {
                is File -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto Bukti Lapangan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is android.graphics.Bitmap -> {
                    androidx.compose.foundation.Image(
                        bitmap = imageModel.asImageBitmap(),
                        contentDescription = "Foto Bukti Lapangan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Small badge in thumbnail corner if size is known
            if (photoSizeKb != null && photoSizeKb > 0) {
                Surface(
                    shape = RoundedCornerShape(topStart = 6.dp),
                    color = Color(0xCC059669),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = "${photoSizeKb}K",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        } else {
            // Neutral placeholder icon if report has no photo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HideImage,
                    contentDescription = "Tidak ada foto bukti",
                    tint = Slate400,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "No Foto",
                    color = Slate400,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Fullscreen / modal dialog preview for photo proof with Unit Code, Report Date, and SPK number.
 */
@Composable
fun FullscreenPhotoPreviewDialog(
    photoPath: String?,
    photoBase64: String?,
    unitCode: String,
    reportDate: String,
    nomorSpk: String?,
    photoSizeKb: Long? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val imageModel = rememberImageModel(photoPath, photoBase64)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE60F172A)) // 90% dark slate backdrop
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {} // Prevent closing when tapping inside dialog card
                    .testTag("fullscreen_photo_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header: Title & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Emerald100,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Emerald700,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pratinjau Foto Bukti Lapangan",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                                Text(
                                    text = "Tersimpan terkompresi (JPEG Base64 Data URL)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                                .testTag("btn_close_photo_preview")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Pratinjau",
                                tint = Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Main Image Container (Max 360dp height, preserve aspect ratio)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 380.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Slate200, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (imageModel) {
                            is File -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imageModel)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto Bukti Layar Penuh",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is android.graphics.Bitmap -> {
                                androidx.compose.foundation.Image(
                                    bitmap = imageModel.asImageBitmap(),
                                    contentDescription = "Foto Bukti Layar Penuh",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HideImage,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Data foto tidak dapat dimuat",
                                        color = Slate400,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metadata Information Box (Kode Unit, Tanggal, SPK)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Kode Unit
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PrecisionManufacturing,
                                        contentDescription = null,
                                        tint = Emerald600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kode Unit:",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = unitCode,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                    )
                                }

                                // Photo Size Badge
                                if (photoSizeKb != null && photoSizeKb > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Emerald100
                                    ) {
                                        Text(
                                            text = "$photoSizeKb KB",
                                            color = Emerald900,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 2. Tanggal Laporan
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tanggal Laporan:",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = reportDate,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate800
                                        )
                                    )
                                }

                                // 3. Nomor SPK
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = if (!nomorSpk.isNullOrBlank()) Emerald700 else Slate400,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SPK:",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (!nomorSpk.isNullOrBlank()) Emerald100 else Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = if (!nomorSpk.isNullOrBlank()) nomorSpk else "Tanpa SPK",
                                            color = if (!nomorSpk.isNullOrBlank()) Emerald900 else Slate500,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
