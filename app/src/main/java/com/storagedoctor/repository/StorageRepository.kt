package com.storagedoctor.repository

import android.content.Context
import com.storagedoctor.database.StorageDoctorDatabase
import com.storagedoctor.database.entity.CompressionBatchEntity
import com.storagedoctor.database.entity.MediaFileEntity
import com.storagedoctor.scheduler.CompressionScheduler
import kotlinx.coroutines.flow.Flow

class StorageRepository(context: Context) {

    private val database = StorageDoctorDatabase.getInstance(context)
    private val mediaFileDao = database.mediaFileDao()
    private val batchDao = database.compressionBatchDao()
    private val scheduler = CompressionScheduler(context)

    // Media Files
    fun getAllMediaFiles(): Flow<List<MediaFileEntity>> = mediaFileDao.getAllFiles()
    fun getPendingFiles(): Flow<List<MediaFileEntity>> = mediaFileDao.getFilesByStatus(MediaFileEntity.Status.PENDING)
    fun getCompressedFiles(): Flow<List<MediaFileEntity>> = mediaFileDao.getFilesByStatus(MediaFileEntity.Status.COMPRESSED)
    fun getCompressedCount(): Flow<Int> = mediaFileDao.getCompressedCount()
    fun getTotalSavedBytes(): Flow<Long> = mediaFileDao.getTotalSavedBytes()
    fun getTotalPendingBytes(): Flow<Long> = mediaFileDao.getTotalPendingBytes()

    // Batches
    fun getAllBatches(): Flow<List<CompressionBatchEntity>> = batchDao.getAllBatches()

    suspend fun createBatch(
        fileCount: Int,
        totalSize: Long,
        compressionMode: String
    ): Long {
        val batch = CompressionBatchEntity(
            totalFiles = fileCount,
            totalSizeBytes = totalSize,
            compressionMode = compressionMode
        )
        return batchDao.insert(batch)
    }

    // Scheduling
    fun startScan(minAgeDays: Int = 15) {
        scheduler.scheduleScan(minAgeDays)
    }

    fun startCompression(batchId: Long, quality: String, batchSize: Int, backupDays: Int) {
        scheduler.scheduleCompression(batchId, quality, batchSize, backupDays)
    }

    fun cancelCompression() {
        scheduler.cancelCompression()
    }

    fun initPeriodicCleanup() {
        scheduler.schedulePeriodicBackupCleanup()
    }
}
