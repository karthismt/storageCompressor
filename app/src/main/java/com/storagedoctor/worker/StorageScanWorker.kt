package com.storagedoctor.worker

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storagedoctor.database.StorageDoctorDatabase
import com.storagedoctor.database.entity.MediaFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Scans device storage for compressible media files.
 * Prioritizes:
 * - Large files first
 * - WhatsApp media
 * - Screenshots
 * - Files older than configured threshold (default 15 days)
 */
class StorageScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val minAgeDays = inputData.getInt("min_age_days", 15)
        val minAgeMs = minAgeDays * 24 * 60 * 60 * 1000L
        val cutoffTime = (System.currentTimeMillis() - minAgeMs) / 1000 // MediaStore uses seconds

        val database = StorageDoctorDatabase.getInstance(applicationContext)
        val mediaFileDao = database.mediaFileDao()

        // Clear previous pending scans
        mediaFileDao.clearPendingFiles()

        val files = mutableListOf<MediaFileEntity>()

        // Query images
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Images.Media.DATE_MODIFIED} < ? AND ${MediaStore.Images.Media.MIME_TYPE} IN (?, ?)"
        val selectionArgs = arrayOf(cutoffTime.toString(), "image/jpeg", "image/png")
        val sortOrder = "${MediaStore.Images.Media.SIZE} DESC"

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        applicationContext.contentResolver.query(
            collection,
            imageProjection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val modifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val path = cursor.getString(pathCol) ?: continue
                val size = cursor.getLong(sizeCol)

                // Skip very small files (< 100KB)
                if (size < 100 * 1024) continue

                // Skip already tracked files
                val existing = mediaFileDao.getFileByPath(path)
                if (existing != null) continue

                files.add(
                    MediaFileEntity(
                        originalPath = path,
                        fileName = cursor.getString(nameCol) ?: "unknown",
                        mimeType = cursor.getString(mimeCol) ?: "image/jpeg",
                        originalSizeBytes = size,
                        width = cursor.getInt(widthCol),
                        height = cursor.getInt(heightCol),
                        lastModified = cursor.getLong(modifiedCol) * 1000,
                        dateAdded = cursor.getLong(addedCol) * 1000,
                        status = MediaFileEntity.Status.PENDING
                    )
                )
            }
        }

        // Insert all found files
        if (files.isNotEmpty()) {
            mediaFileDao.insertAll(files)
        }

        Result.success(
            androidx.work.workDataOf("files_found" to files.size)
        )
    }
}
