package com.storagedoctor.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storagedoctor.compression.CompressionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Cleans up expired backup files to reclaim space.
 * Runs periodically in the background.
 */
class BackupCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val manager = CompressionManager(applicationContext)
        manager.cleanExpiredBackups()
        Result.success()
    }
}
