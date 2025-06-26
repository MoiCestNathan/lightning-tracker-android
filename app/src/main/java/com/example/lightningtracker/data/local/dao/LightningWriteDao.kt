package com.example.lightningtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lightningtracker.data.local.entity.LightningStrikeEntity

@Dao
interface LightningWriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrike(strike: LightningStrikeEntity)

    @Query("DELETE FROM lightning_strikes WHERE timestamp < :timestamp")
    suspend fun deleteStrikesOlderThan(timestamp: Long)
} 