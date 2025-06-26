package com.example.lightningtracker.domain.repository

import com.example.lightningtracker.domain.model.LightningStrike
import kotlinx.coroutines.flow.Flow

interface LightningRepository {

    fun getHistoricalStrikes(): Flow<List<LightningStrike>>

    fun getLiveStrikes(): Flow<LightningStrike>

    suspend fun deleteOldStrikes(retentionPeriod: Long)
} 