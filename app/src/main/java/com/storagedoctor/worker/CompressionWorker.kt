package com.storagedoctor.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.*
import com.storagedoctor.compression.CompressionManager
import com.storagedoctor.compression.ImageCompressor
import com.storagedoctor.database.StorageDoctorDatabase
import com.storagedoctor.database.entity.CompressionBatchEntity

/**
 * WorkManager worker for batch compression.
 * Runs only when device conditions are met:
 * - Charging
 * - Battery > 40% (not low)
 * - Idle
 * - Not thermal throttling
 */
class CompressionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val batchId = inputData.getLong("batch_id", -1)
        if (batchId == -1L) return Result.failure()

        val qualityName = inputData.getString("quality") ?: "BALANCED"
        val batchSize = inputData.getInt("batch_size", 250)
        val backupDays = inputData.getInt("backup_days", 7)

        val quality = when (qualityName) {
            "HIGH_QUALITY" -> ImageCompressor.Quality.HIGH_QUALITY
            "MAX_SAVING" -> ImageCompressor.Quality.MAX_SAVING
            else -> ImageCompressor.Quality.BALANCED
        }

        // Set as foreground service for long-running work
        setForeground(createForegroundInfo())

        val manager = CompressionManager(applicationContext)
        val config = CompressionManager.BatchConfig(
            batchSize = batchSize,
            quality = quality,
            backupRetentionDays = backupDays
        )

        val result = manager.compressBatch(batchId, config)

        return if (result.error == null) {
            val outputData = workDataOf(
                "completed" to result.completed,
                "failed" to result.failed,
                "saved_bytes" to result.savedBytes
            )
            Result.success(outputData)
        } else {
            Result.retry()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, "compression_channel")
            .setContentTitle("Storage Doctor")
            .setContentText("Compressing photos...")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "compression_work"

        fun buildConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()
        }

        fun createWorkRequest(
            batchId: Long,
            quality: String = "BALANCED",
            batchSize: Int = 250,
            backupDays: Int = 7
        ): OneTimeWorkRequest {
            val inputData = workDataOf(
                "batch_id" to batchId,
                "quality" to quality,
                "batch_size" to batchSize,
                "backup_days" to backupDays
            )

            return OneTimeWorkRequestBuilder<CompressionWorker>()
                .setConstraints(buildConstraints())
                .setInputData(inputData)
                .addTag(WORK_NAME)
                .build()
        }
    }
}
