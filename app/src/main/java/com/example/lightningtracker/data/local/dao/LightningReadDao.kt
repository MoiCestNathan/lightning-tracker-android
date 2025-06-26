package com.example.lightningtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.lightningtracker.data.local.entity.LightningStrikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LightningReadDao {

    @Query("SELECT * FROM lightning_strikes ORDER BY timestamp DESC")
    fun getStrikes(): Flow<List<LightningStrikeEntity>>

    @Query("SELECT * FROM lightning_strikes WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getStrikesSince(sinceTimestamp: Long): Flow<List<LightningStrikeEntity>>
} 