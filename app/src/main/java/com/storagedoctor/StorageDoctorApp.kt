package com.storagedoctor

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class StorageDoctorApp : Application(), Configuration.Provider {

    lateinit var database: com.storagedoctor.database.StorageDoctorDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = com.storagedoctor.database.StorageDoctorDatabase.getInstance(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    companion object {
        lateinit var instance: StorageDoctorApp
            private set
    }
}
