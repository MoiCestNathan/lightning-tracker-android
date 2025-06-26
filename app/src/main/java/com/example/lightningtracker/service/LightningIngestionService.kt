package com.example.lightningtracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.lightningtracker.R
import com.example.lightningtracker.domain.repository.LightningRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@AndroidEntryPoint
class LightningIngestionService : Service() {

    @Inject
    lateinit var repository: LightningRepository

    @Inject
    lateinit var proximityAlertManager: ProximityAlertManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Lightning Tracker")
            .setContentText("Actively monitoring for lightning strikes.")
            .setSmallIcon(R.drawable.ic_stat_notification)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        proximityAlertManager.startMonitoring()

        repository.getLiveStrikes().launchIn(serviceScope)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Lightning Ingestion Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "LightningIngestionServiceChannel"
        const val NOTIFICATION_ID = 1
        const val FETCH_INTERVAL_MS = 30_000L // 30 seconds
    }
} 