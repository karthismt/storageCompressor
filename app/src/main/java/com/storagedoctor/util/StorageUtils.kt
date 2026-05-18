package com.storagedoctor.util

import java.util.Locale

object StorageUtils {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(
            Locale.getDefault(),
            "%.1f %s",
            bytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }

    fun calculateEstimatedTime(
        totalFiles: Int,
        processedFiles: Int,
        elapsedMs: Long
    ): Long {
        if (processedFiles == 0) return 0
        val avgTimePerFile = elapsedMs / processedFiles
        val remaining = totalFiles - processedFiles
        return avgTimePerFile * remaining
    }

    fun isCompressibleImage(mimeType: String): Boolean {
        return mimeType in listOf("image/jpeg", "image/png", "image/jpg")
    }

    fun getOutputFormat(mimeType: String): String {
        return when (mimeType) {
            "image/png" -> "webp_lossless"
            else -> "webp_lossy"
        }
    }
}
