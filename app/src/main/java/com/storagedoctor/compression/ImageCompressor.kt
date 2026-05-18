package com.storagedoctor.compression

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.File
import java.io.FileOutputStream

/**
 * Core image compression engine.
 * Converts JPG → WebP and PNG → WebP Lossless.
 * No ZIP/unZIP — uses native compressed formats directly.
 */
class ImageCompressor(private val context: Context) {

    data class CompressionResult(
        val success: Boolean,
        val originalSize: Long,
        val compressedSize: Long,
        val outputPath: String,
        val error: String? = null
    ) {
        val savedBytes: Long get() = originalSize - compressedSize
        val savedPercentage: Float get() = if (originalSize > 0) savedBytes.toFloat() / originalSize * 100 else 0f
    }

    enum class Quality(val jpgQuality: Int, val webpQuality: Int) {
        HIGH_QUALITY(90, 90),
        BALANCED(80, 80),
        MAX_SAVING(65, 65)
    }

    fun compressImage(inputPath: String, outputDir: String, quality: Quality): CompressionResult {
        val inputFile = File(inputPath)
        if (!inputFile.exists()) {
            return CompressionResult(false, 0, 0, "", "Input file not found")
        }

        val originalSize = inputFile.length()

        return try {
            // Decode bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(inputPath, options)

            // Calculate sample size for very large images
            val sampleSize = calculateSampleSize(options, 4096, 4096)

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = BitmapFactory.decodeFile(inputPath, decodeOptions)
                ?: return CompressionResult(false, originalSize, 0, "", "Failed to decode image")

            // Determine output format
            val mimeType = getMimeType(inputPath)
            val isLossless = mimeType == "image/png"

            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (isLossless) Bitmap.CompressFormat.WEBP_LOSSLESS
                else Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }

            // Create output file
            val outputFileName = inputFile.nameWithoutExtension + ".webp"
            val outputFile = File(outputDir, outputFileName)
            outputFile.parentFile?.mkdirs()

            // Compress
            FileOutputStream(outputFile).use { fos ->
                val compressionQuality = if (isLossless) 100 else quality.webpQuality
                bitmap.compress(format, compressionQuality, fos)
                fos.flush()
            }

            bitmap.recycle()

            val compressedSize = outputFile.length()

            // Verify compression actually saved space
            if (compressedSize >= originalSize) {
                outputFile.delete()
                return CompressionResult(false, originalSize, compressedSize, "", "Compression did not save space")
            }

            // Verify output file is valid
            val verification = verifyCompressedImage(outputFile.absolutePath, options.outWidth, options.outHeight)
            if (!verification) {
                outputFile.delete()
                return CompressionResult(false, originalSize, compressedSize, "", "Verification failed")
            }

            CompressionResult(
                success = true,
                originalSize = originalSize,
                compressedSize = compressedSize,
                outputPath = outputFile.absolutePath
            )
        } catch (e: Exception) {
            CompressionResult(false, originalSize, 0, "", e.message ?: "Unknown error")
        }
    }

    private fun verifyCompressedImage(path: String, expectedWidth: Int, expectedHeight: Int): Boolean {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            // Check dimensions are valid
            options.outWidth > 0 && options.outHeight > 0 &&
                    // File exists and has content
                    File(path).length() > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateSampleSize(options: BitmapFactory.Options, maxWidth: Int, maxHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var sampleSize = 1

        if (height > maxHeight || width > maxWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / sampleSize >= maxHeight && halfWidth / sampleSize >= maxWidth) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }

    private fun getMimeType(path: String): String {
        return when (path.substringAfterLast('.').lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
    }
}
