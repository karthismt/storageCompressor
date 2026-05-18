package com.storagedoctor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.storagedoctor.database.dao.CompressionBatchDao
import com.storagedoctor.database.dao.MediaFileDao
import com.storagedoctor.database.entity.CompressionBatchEntity
import com.storagedoctor.database.entity.MediaFileEntity

@Database(
    entities = [MediaFileEntity::class, CompressionBatchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StorageDoctorDatabase : RoomDatabase() {

    abstract fun mediaFileDao(): MediaFileDao
    abstract fun compressionBatchDao(): CompressionBatchDao

    companion object {
        @Volatile
        private var INSTANCE: StorageDoctorDatabase? = null

        fun getInstance(context: Context): StorageDoctorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StorageDoctorDatabase::class.java,
                    "storage_doctor_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
