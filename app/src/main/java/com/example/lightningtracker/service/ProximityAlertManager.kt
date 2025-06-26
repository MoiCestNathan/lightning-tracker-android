package com.example.lightningtracker.service

import com.example.lightningtracker.domain.repository.LightningRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProximityAlertManager @Inject constructor(
    private val repository: LightningRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startMonitoring() {
        scope.launch {
            repository.getLiveStrikes().collectLatest { strike ->
                // In a real app, this is where you would:
                // 1. Get the user's current location.
                // 2. Check if the new strike is within the configured radius.
                // 3. Trigger a notification if it is.
                println("New strike received: ${strike.latitude}, ${strike.longitude}")
            }
        }
    }
} 