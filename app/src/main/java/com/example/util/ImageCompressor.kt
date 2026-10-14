package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

data class CompressedPhotoResult(
    val filePath: String,
    val fileUri: Uri,
    val base64: String,
    val sizeKb: Long,
    val width: Int,
    val height: Int,
    val dimensionsText: String
)

object ImageCompressor {

    const val MAX_DIMENSION = 1080
    const val COMPRESSION_QUALITY = 78 // 75% - 80% specification (target ~150 KB - 400 KB)

    /**
     * Calculates proportional dimensions with maximum width or height constrained to maxDimension.
     */
    fun calculateTargetDimensions(srcWidth: Int, srcHeight: Int, maxDimension: Int = MAX_DIMENSION): Pair<Int, Int> {
        if (srcWidth <= 0 || srcHeight <= 0) return Pair(maxDimension, maxDimension)
        if (srcWidth <= maxDimension && srcHeight <= maxDimension) {
            return Pair(srcWidth, srcHeight)
        }
        val ratio = srcWidth.toFloat() / srcHeight.toFloat()
        return if (srcWidth >= srcHeight) {
            val targetWidth = maxDimension
            val targetHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
            Pair(targetWidth, targetHeight)
        } else {
            val targetHeight = maxDimension
            val targetWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
            Pair(targetWidth, targetHeight)
        }
    }

    /**
     * Calculate optimal inSampleSize for memory-efficient decoding.
     */
    private fun calculateInSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Creates a temporary file in filesDir/activity_photos and returns its FileProvider content Uri
     * for launching the Camera intent.
     */
    fun createTempCameraUri(context: Context): Pair<File, Uri> {
        val photosDir = File(context.filesDir, "activity_photos").apply {
            if (!exists()) mkdirs()
        }
        val tempFile = File(photosDir, "temp_camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return Pair(tempFile, uri)
    }

    /**
     * Reads image from Uri, handles EXIF orientation, downsamples with inSampleSize,
     * resizes to max 1080px (maintaining aspect ratio), compresses to JPEG 78% (75-80%),
     * recycles intermediate bitmaps (anti-OOM), saves to local storage, and returns Base64 and metadata.
     */
    suspend fun compressImage(
        context: Context,
        sourceUri: Uri
    ): Result<CompressedPhotoResult> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            // 1. Read EXIF rotation orientation
            var orientationDegrees = 0
            try {
                contentResolver.openInputStream(sourceUri)?.use { input ->
                    val exif = ExifInterface(input)
                    orientationDegrees = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } catch (_: Exception) {
                orientationDegrees = 0
            }

            // 2. Decode image bounds only
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val rawWidth = boundsOptions.outWidth
            val rawHeight = boundsOptions.outHeight
            if (rawWidth <= 0 || rawHeight <= 0) {
                return@withContext Result.failure(IllegalArgumentException("Gagal membaca dimensi file foto"))
            }

            // Adjust width/height if EXIF orientation swaps them
            val effectiveWidth = if (orientationDegrees == 90 || orientationDegrees == 270) rawHeight else rawWidth
            val effectiveHeight = if (orientationDegrees == 90 || orientationDegrees == 270) rawWidth else rawHeight

            val (targetWidth, targetHeight) = calculateTargetDimensions(effectiveWidth, effectiveHeight, MAX_DIMENSION)

            // 3. Decode bitmap with inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(rawWidth, rawHeight, targetWidth, targetHeight)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val rawBitmap: Bitmap = contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@withContext Result.failure(IllegalStateException("Gagal memuat data bitmap foto"))

            // 4. Apply rotation and fine scaling
            val matrix = Matrix()
            if (orientationDegrees != 0) {
                matrix.postRotate(orientationDegrees.toFloat())
            }

            // Scale to exact target dimensions
            val rotatedBitmap = if (orientationDegrees != 0) {
                Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
            } else {
                rawBitmap
            }

            val finalBitmap = if (rotatedBitmap.width != targetWidth || rotatedBitmap.height != targetHeight) {
                Bitmap.createScaledBitmap(rotatedBitmap, targetWidth, targetHeight, true)
            } else {
                rotatedBitmap
            }

            // 5. Compress to JPEG 72%
            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
            val compressedBytes = outputStream.toByteArray()

            // 6. Save compressed JPEG to persistent local directory
            val photosDir = File(context.filesDir, "activity_photos").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "report_photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destinationFile = File(photosDir, fileName)
            FileOutputStream(destinationFile).use { fos ->
                fos.write(compressedBytes)
                fos.flush()
            }

            val finalUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destinationFile
            )

            // 7. Generate Base64 string with complete Data URL format compatible with web browsers
            val rawBase64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                .replace("\n", "")
                .replace("\r", "")
            val dataUrlString = "data:image/jpeg;base64,$rawBase64"
            val sizeKb = (destinationFile.length() / 1024).coerceAtLeast(1)
            val dimensionsText = "${finalBitmap.width}x${finalBitmap.height} px"

            // Clean up intermediate bitmaps if needed
            if (rotatedBitmap != rawBitmap && !rotatedBitmap.isRecycled) {
                rotatedBitmap.recycle()
            }
            if (finalBitmap != rotatedBitmap && !finalBitmap.isRecycled) {
                finalBitmap.recycle()
            }
            if (!rawBitmap.isRecycled) {
                rawBitmap.recycle()
            }

            Result.success(
                CompressedPhotoResult(
                    filePath = destinationFile.absolutePath,
                    fileUri = finalUri,
                    base64 = dataUrlString,
                    sizeKb = sizeKb,
                    width = targetWidth,
                    height = targetHeight,
                    dimensionsText = dimensionsText
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
