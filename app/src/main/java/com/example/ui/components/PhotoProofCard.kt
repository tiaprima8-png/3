package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.ImageCompressor
import java.io.File

@Composable
fun PhotoProofCard(
    photoPath: String?,
    photoSizeKb: Long?,
    photoDimensions: String?,
    isCompressing: Boolean,
    onPhotoSelected: (Uri) -> Unit,
    onRemovePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPreviewModal by remember { mutableStateOf(false) }

    // Launcher for Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { uri ->
                onPhotoSelected(uri)
            }
        }
    }

    // Launcher for Gallery Picker (Android Photo Picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onPhotoSelected(it) }
    }

    // Permission launcher for Camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (_, uri) = ImageCompressor.createTempCameraUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto bukti", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val (_, uri) = ImageCompressor.createTempCameraUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Compact container: Sleek, proportional, tidak makan tempat berlebihan
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("photo_proof_card"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            if (isCompressing) {
                // Compressing state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Emerald600,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mengompresi foto otomatis...",
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Optimasi JPEG (anti-OOM)",
                            color = Slate500,
                            fontSize = 10.sp
                        )
                    }
                }
            } else if (!photoPath.isNullOrBlank() && File(photoPath).exists()) {
                // Attached photo state: compact row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small thumbnail preview (click to full view)
                    Box(
                        modifier = Modifier
                            .size(width = 68.dp, height = 54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Emerald600.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { showPreviewModal = true }
                            .testTag("photo_thumbnail_preview")
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(photoPath))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Thumbnail Foto Bukti Lapangan",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            shape = RoundedCornerShape(bottomStart = 6.dp),
                            color = Emerald600,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Info: file size & dimensions
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Foto Terlampir",
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                fontSize = 12.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Emerald100
                            ) {
                                Text(
                                    text = "${photoSizeKb ?: 145} KB",
                                    color = Emerald900,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                        .testTag("label_photo_size")
                                )
                            }
                        }
                        Text(
                            text = photoDimensions ?: "Resolusi Terkompresi",
                            color = Slate500,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    // Action buttons: Ganti & Hapus
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { launchCamera() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_retake_photo")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(12.dp), tint = Emerald600)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Ganti", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onRemovePhoto,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRose),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_delete_photo")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp), tint = ErrorRose)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Hapus", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // Empty state: Sleek, satu baris proporsional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Emerald50, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Lampirkan Foto Bukti",
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Opsional • Kompresi otomatis",
                                color = Slate500,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { launchCamera() },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_take_photo")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kamera",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_pick_gallery")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Galeri",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal popup if thumbnail clicked
    if (showPreviewModal && !photoPath.isNullOrBlank()) {
        FullscreenPhotoPreviewDialog(
            photoPath = photoPath,
            photoBase64 = null,
            unitCode = "Unit Lapangan",
            reportDate = "Foto Bukti Aktual",
            nomorSpk = null,
            photoSizeKb = photoSizeKb,
            onDismiss = { showPreviewModal = false }
        )
    }
}
