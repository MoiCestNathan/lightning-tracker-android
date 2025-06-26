package com.example.lightningtracker.domain.model

data class LightningStrike(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val intensity: Float
) 