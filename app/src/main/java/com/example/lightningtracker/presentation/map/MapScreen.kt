package com.example.lightningtracker.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(5.0)
                    controller.setCenter(GeoPoint(48.85, 2.35)) // Paris
                }
            },
            update = { mapView ->
                // Clear old markers to prevent duplicates
                mapView.overlays.filterIsInstance<Marker>().forEach {
                    mapView.overlays.remove(it)
                }

                // Add new markers
                state.strikes.forEach { strike ->
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(strike.latitude, strike.longitude)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Strike"
                    marker.snippet = "Intensity: ${strike.intensity}"
                    mapView.overlays.add(marker)
                }
                mapView.invalidate() // Redraw the map
            }
        )

        Text(
            text = "${state.strikes.size} strikes loaded",
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
} 