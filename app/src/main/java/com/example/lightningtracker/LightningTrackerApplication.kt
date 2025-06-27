package com.example.lightningtracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import org.osmdroid.config.Configuration as OsmdroidConfiguration
import timber.log.Timber
import com.example.lightningtracker.BuildConfig

@HiltAndroidApp
class LightningTrackerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        OsmdroidConfiguration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        OsmdroidConfiguration.getInstance().userAgentValue = "com.example.lightningtracker"
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
} 