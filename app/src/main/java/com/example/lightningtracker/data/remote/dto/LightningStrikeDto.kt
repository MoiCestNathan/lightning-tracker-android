package com.example.lightningtracker.data.remote.dto

import com.example.lightningtracker.domain.model.LightningStrike
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LightningStrikeDto(
    @Json(name = "time")
    val timestamp: Long,
    @Json(name = "lat")
    val latitude: Double,
    @Json(name = "lon")
    val longitude: Double,
    @Json(name = "pol")
    val polarity: Int? = 0
)

fun LightningStrikeDto.toDomainModel(): LightningStrike {
    return LightningStrike(
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        intensity = polarity?.toFloat() ?: 0f
    )
} 