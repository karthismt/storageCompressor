package com.storagedoctor.scheduler

import android.content.Context
import androidx.work.*
import com.storagedoctor.worker.BackupCleanupWorker
import com.storagedoctor.worker.CompressionWorker
import com.storagedoctor.worker.StorageScanWorker
import java.util.concurrent.TimeUnit

/**
 * Schedules background work using WorkManager.
 * Compression only runs when:
 * - Phone is charging
 * - Battery > 40%
 * - Screen off / device idle
 * - Thermal state normal
 */
class CompressionScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleScan(minAgeDays: Int = 15) {
        val inputData = workDataOf("min_age_days" to minAgeDays)

        val scanRequest = OneTimeWorkRequestBuilder<StorageScanWorker>()
            .setInputData(inputData)
            .addTag("storage_scan")
            .build()

        workManager.enqueueUniqueWork(
            "storage_scan",
            ExistingWorkPolicy.REPLACE,
            scanRequest
        )
    }

    fun scheduleCompression(
        batchId: Long,
        quality: String = "BALANCED",
        batchSize: Int = 250,
        backupDays: Int = 7
    ) {
        val request = CompressionWorker.createWorkRequest(batchId, quality, batchSize, backupDays)

        workManager.enqueueUniqueWork(
            CompressionWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun schedulePeriodicBackupCleanup() {
        val constraints = Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .build()

        val cleanupRequest = PeriodicWorkRequestBuilder<BackupCleanupWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag("backup_cleanup")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "backup_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }

    fun cancelCompression() {
        workManager.cancelUniqueWork(CompressionWorker.WORK_NAME)
    }

    fun cancelAll() {
        workManager.cancelAllWork()
    }

    fun getCompressionWorkInfo() = workManager.getWorkInfosForUniqueWorkLiveData(CompressionWorker.WORK_NAME)

    fun getScanWorkInfo() = workManager.getWorkInfosForUniqueWorkLiveData("storage_scan")
}
