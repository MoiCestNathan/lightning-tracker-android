package com.example.lightningtracker

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lightningtracker.presentation.map.MapScreen
import com.example.lightningtracker.service.CleanupWorker
import com.example.lightningtracker.service.LightningIngestionService
import com.example.lightningtracker.service.LightningSyncWorker
import com.example.lightningtracker.ui.theme.LightningTrackerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LightningTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionWrapper {
                        MapScreen()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun PermissionWrapper(content: @Composable () -> Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val permissionState = rememberMultiplePermissionsState(permissions)

        if (permissionState.allPermissionsGranted) {
            // Start services only when permissions are granted
            startIngestionService()
            scheduleWorkers()
            content()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "This app needs location permissions to track lightning and show alerts. Please grant the necessary permissions.",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                    Text("Grant Permissions")
                }
            }
        }
    }

    private fun startIngestionService() {
        Intent(this, LightningIngestionService::class.java).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
            } else {
                startService(it)
            }
        }
    }

    private fun scheduleWorkers() {
        val syncRequest =
            PeriodicWorkRequestBuilder<LightningSyncWorker>(15, TimeUnit.MINUTES)
                .build()

        val cleanupRequest =
            PeriodicWorkRequestBuilder<CleanupWorker>(1, TimeUnit.DAYS)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cleanup",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
}