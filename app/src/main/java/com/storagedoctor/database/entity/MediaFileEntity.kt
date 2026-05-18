package com.storagedoctor.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_files")
data class MediaFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalPath: String,
    val compressedPath: String? = null,
    val backupPath: String? = null,
    val fileName: String,
    val mimeType: String,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long? = null,
    val width: Int = 0,
    val height: Int = 0,
    val lastModified: Long,
    val dateAdded: Long,
    val status: String = Status.PENDING, // PENDING, COMPRESSING, COMPRESSED, FAILED, RESTORED
    val batchId: Long? = null,
    val compressionMode: String = CompressionMode.BALANCED,
    val errorMessage: String? = null,
    val compressedAt: Long? = null,
    val backupExpiresAt: Long? = null
) {
    object Status {
        const val PENDING = "PENDING"
        const val COMPRESSING = "COMPRESSING"
        const val COMPRESSED = "COMPRESSED"
        const val FAILED = "FAILED"
        const val RESTORED = "RESTORED"
        const val SKIPPED = "SKIPPED"
    }

    object CompressionMode {
        const val HIGH_QUALITY = "HIGH_QUALITY"
        const val BALANCED = "BALANCED"
        const val MAX_SAVING = "MAX_SAVING"
    }

    val savedBytes: Long
        get() = if (compressedSizeBytes != null) originalSizeBytes - compressedSizeBytes else 0
}
