package com.storagedoctor.compression

import android.content.Context
import com.storagedoctor.database.StorageDoctorDatabase
import com.storagedoctor.database.entity.CompressionBatchEntity
import com.storagedoctor.database.entity.MediaFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

/**
 * Manages batch compression workflow:
 * 1. Read original file
 * 2. Create temporary compressed file
 * 3. Verify compressed file
 * 4. Replace original only after success
 * 5. Keep restore backup for configurable days
 */
class CompressionManager(private val context: Context) {

    private val imageCompressor = ImageCompressor(context)
    private val database = StorageDoctorDatabase.getInstance(context)
    private val mediaFileDao = database.mediaFileDao()
    private val batchDao = database.compressionBatchDao()

    private val backupDir: File
        get() = File(context.filesDir, "restore_backups").also { it.mkdirs() }

    private val tempDir: File
        get() = File(context.cacheDir, "compression_temp").also { it.mkdirs() }

    data class BatchConfig(
        val batchSize: Int = 250,
        val quality: ImageCompressor.Quality = ImageCompressor.Quality.BALANCED,
        val backupRetentionDays: Int = 7
    )

    suspend fun compressBatch(batchId: Long, config: BatchConfig): BatchResult = withContext(Dispatchers.IO) {
        val batch = batchDao.getBatchById(batchId)
            ?: return@withContext BatchResult(0, 0, 0, "Batch not found")

        batchDao.update(batch.copy(status = CompressionBatchEntity.Status.RUNNING, startedAt = System.currentTimeMillis()))

        val files = mediaFileDao.getPendingFiles(config.batchSize)
        var completed = 0
        var failed = 0
        var totalSaved = 0L

        for (file in files) {
            if (!coroutineContext.isActive) break

            // Check if batch is paused
            val currentBatch = batchDao.getBatchById(batchId)
            if (currentBatch?.status == CompressionBatchEntity.Status.PAUSED) break

            val result = compressSingleFile(file, config)
            if (result) {
                completed++
                val updatedFile = mediaFileDao.getFileById(file.id)
                totalSaved += updatedFile?.savedBytes ?: 0
            } else {
                failed++
            }

            // Update batch progress
            batchDao.update(
                batch.copy(
                    completedFiles = completed,
                    failedFiles = failed,
                    savedSizeBytes = totalSaved
                )
            )
        }

        // Mark batch complete
        batchDao.update(
            batch.copy(
                status = CompressionBatchEntity.Status.COMPLETED,
                completedFiles = completed,
                failedFiles = failed,
                savedSizeBytes = totalSaved,
                completedAt = System.currentTimeMillis()
            )
        )

        BatchResult(completed, failed, totalSaved)
    }

    private suspend fun compressSingleFile(file: MediaFileEntity, config: BatchConfig): Boolean {
        try {
            // Mark as compressing
            mediaFileDao.update(file.copy(status = MediaFileEntity.Status.COMPRESSING))

            val originalFile = File(file.originalPath)
            if (!originalFile.exists()) {
                mediaFileDao.update(file.copy(status = MediaFileEntity.Status.FAILED, errorMessage = "Original file not found"))
                return false
            }

            // Step 1: Compress to temp directory
            val result = imageCompressor.compressImage(
                inputPath = file.originalPath,
                outputDir = tempDir.absolutePath,
                quality = config.quality
            )

            if (!result.success) {
                mediaFileDao.update(file.copy(status = MediaFileEntity.Status.FAILED, errorMessage = result.error))
                return false
            }

            // Step 2: Create backup of original
            val backupFile = File(backupDir, "${file.id}_${originalFile.name}")
            originalFile.copyTo(backupFile, overwrite = true)

            // Step 3: Replace original with compressed file
            val compressedTempFile = File(result.outputPath)
            val finalPath = originalFile.parent + "/" + originalFile.nameWithoutExtension + ".webp"
            val finalFile = File(finalPath)

            compressedTempFile.copyTo(finalFile, overwrite = true)
            compressedTempFile.delete()

            // Step 4: Delete original if different extension
            if (originalFile.absolutePath != finalFile.absolutePath) {
                originalFile.delete()
            }

            // Step 5: Update database
            val backupExpiry = System.currentTimeMillis() + (config.backupRetentionDays * 24 * 60 * 60 * 1000L)
            mediaFileDao.update(
                file.copy(
                    status = MediaFileEntity.Status.COMPRESSED,
                    compressedPath = finalFile.absolutePath,
                    compressedSizeBytes = result.compressedSize,
                    backupPath = backupFile.absolutePath,
                    compressedAt = System.currentTimeMillis(),
                    backupExpiresAt = backupExpiry
                )
            )

            return true
        } catch (e: Exception) {
            mediaFileDao.update(file.copy(status = MediaFileEntity.Status.FAILED, errorMessage = e.message))
            return false
        }
    }

    suspend fun restoreFile(fileId: Long): Boolean = withContext(Dispatchers.IO) {
        val file = mediaFileDao.getFileById(fileId) ?: return@withContext false
        val backupPath = file.backupPath ?: return@withContext false
        val backupFile = File(backupPath)

        if (!backupFile.exists()) return@withContext false

        try {
            // Restore original
            val compressedFile = File(file.compressedPath ?: return@withContext false)
            compressedFile.delete()

            backupFile.copyTo(File(file.originalPath), overwrite = true)
            backupFile.delete()

            mediaFileDao.update(
                file.copy(
                    status = MediaFileEntity.Status.RESTORED,
                    compressedPath = null,
                    compressedSizeBytes = null,
                    backupPath = null,
                    compressedAt = null,
                    backupExpiresAt = null
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cleanExpiredBackups() = withContext(Dispatchers.IO) {
        val backupFiles = backupDir.listFiles() ?: return@withContext
        val now = System.currentTimeMillis()

        for (backup in backupFiles) {
            if (backup.lastModified() + (7 * 24 * 60 * 60 * 1000L) < now) {
                backup.delete()
            }
        }
    }

    data class BatchResult(
        val completed: Int,
        val failed: Int,
        val savedBytes: Long,
        val error: String? = null
    )
}
