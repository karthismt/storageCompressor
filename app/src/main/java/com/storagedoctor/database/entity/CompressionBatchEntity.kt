package com.storagedoctor.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compression_batches")
data class CompressionBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalFiles: Int,
    val completedFiles: Int = 0,
    val failedFiles: Int = 0,
    val totalSizeBytes: Long,
    val savedSizeBytes: Long = 0,
    val status: String = Status.PENDING, // PENDING, RUNNING, PAUSED, COMPLETED, FAILED
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val compressionMode: String,
    val estimatedDurationMs: Long? = null
) {
    object Status {
        const val PENDING = "PENDING"
        const val RUNNING = "RUNNING"
        const val PAUSED = "PAUSED"
        const val COMPLETED = "COMPLETED"
        const val FAILED = "FAILED"
    }

    val progress: Float
        get() = if (totalFiles > 0) completedFiles.toFloat() / totalFiles else 0f
}
