package com.example.lightningtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lightningtracker.domain.model.LightningStrike

@Entity(tableName = "lightning_strikes")
data class LightningStrikeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val intensity: Float
)

fun LightningStrikeEntity.toDomainModel(): LightningStrike {
    return LightningStrike(
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        intensity = intensity
    )
}

fun LightningStrike.toEntity(): LightningStrikeEntity {
    return LightningStrikeEntity(
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        intensity = intensity
    )
} 