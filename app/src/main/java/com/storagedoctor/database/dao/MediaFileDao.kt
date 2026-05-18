package com.storagedoctor.database.dao

import androidx.room.*
import com.storagedoctor.database.entity.MediaFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFileDao {

    @Query("SELECT * FROM media_files ORDER BY originalSizeBytes DESC")
    fun getAllFiles(): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE status = :status ORDER BY originalSizeBytes DESC")
    fun getFilesByStatus(status: String): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE status = 'PENDING' ORDER BY originalSizeBytes DESC LIMIT :limit")
    suspend fun getPendingFiles(limit: Int): List<MediaFileEntity>

    @Query("SELECT * FROM media_files WHERE batchId = :batchId")
    fun getFilesForBatch(batchId: Long): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE id = :id")
    suspend fun getFileById(id: Long): MediaFileEntity?

    @Query("SELECT * FROM media_files WHERE originalPath = :path LIMIT 1")
    suspend fun getFileByPath(path: String): MediaFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: MediaFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<MediaFileEntity>)

    @Update
    suspend fun update(file: MediaFileEntity)

    @Delete
    suspend fun delete(file: MediaFileEntity)

    @Query("SELECT COUNT(*) FROM media_files WHERE status = 'COMPRESSED'")
    fun getCompressedCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(originalSizeBytes - compressedSizeBytes), 0) FROM media_files WHERE status = 'COMPRESSED' AND compressedSizeBytes IS NOT NULL")
    fun getTotalSavedBytes(): Flow<Long>

    @Query("SELECT COALESCE(SUM(originalSizeBytes), 0) FROM media_files WHERE status = 'PENDING'")
    fun getTotalPendingBytes(): Flow<Long>

    @Query("DELETE FROM media_files WHERE status = 'PENDING'")
    suspend fun clearPendingFiles()
}
