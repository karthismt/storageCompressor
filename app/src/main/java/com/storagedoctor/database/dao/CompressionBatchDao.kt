package com.storagedoctor.database.dao

import androidx.room.*
import com.storagedoctor.database.entity.CompressionBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompressionBatchDao {

    @Query("SELECT * FROM compression_batches ORDER BY id DESC")
    fun getAllBatches(): Flow<List<CompressionBatchEntity>>

    @Query("SELECT * FROM compression_batches WHERE status = :status")
    fun getBatchesByStatus(status: String): Flow<List<CompressionBatchEntity>>

    @Query("SELECT * FROM compression_batches WHERE id = :id")
    suspend fun getBatchById(id: Long): CompressionBatchEntity?

    @Query("SELECT * FROM compression_batches WHERE status = 'RUNNING' LIMIT 1")
    suspend fun getRunningBatch(): CompressionBatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: CompressionBatchEntity): Long

    @Update
    suspend fun update(batch: CompressionBatchEntity)

    @Delete
    suspend fun delete(batch: CompressionBatchEntity)

    @Query("SELECT COALESCE(SUM(savedSizeBytes), 0) FROM compression_batches WHERE status = 'COMPLETED'")
    fun getTotalSavedFromBatches(): Flow<Long>
}
