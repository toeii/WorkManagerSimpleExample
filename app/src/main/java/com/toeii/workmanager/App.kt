package com.toeii.workmanager

import android.app.Application
import android.util.Log
import androidx.work.Configuration

internal class App : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.VERBOSE)
            .build()

    override fun onCreate() {
        super.onCreate()

    }

}