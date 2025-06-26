package com.example.lightningtracker.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lightningtracker.domain.repository.LightningRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LightningSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: LightningRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // TODO: The WebSocket-based client doesn't fit the one-off sync model of a Worker.
            // Re-evaluate if this worker is needed or find an HTTP endpoint for recent strikes.
            // repository.fetchAndStoreNewStrikes()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 