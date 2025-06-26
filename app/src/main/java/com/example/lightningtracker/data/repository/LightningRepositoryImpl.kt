package com.example.lightningtracker.data.repository

import com.example.lightningtracker.data.local.dao.LightningReadDao
import com.example.lightningtracker.data.local.dao.LightningWriteDao
import com.example.lightningtracker.data.local.entity.toDomainModel
import com.example.lightningtracker.data.local.entity.toEntity
import com.example.lightningtracker.data.remote.BlitzortungFeedClient
import com.example.lightningtracker.data.remote.dto.toDomainModel
import com.example.lightningtracker.domain.model.LightningStrike
import com.example.lightningtracker.domain.repository.LightningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LightningRepositoryImpl @Inject constructor(
    private val feedClient: BlitzortungFeedClient,
    private val readDao: LightningReadDao,
    private val writeDao: LightningWriteDao
) : LightningRepository {

    override fun getHistoricalStrikes(): Flow<List<LightningStrike>> {
        return readDao.getStrikes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getLiveStrikes(): Flow<LightningStrike> {
        feedClient.connect()
        return feedClient.strikes.map { result ->
            result.getOrThrow()
        }.onEach { dto ->
            writeDao.insertStrike(dto.toDomainModel().toEntity())
        }.map { dto ->
            dto.toDomainModel()
        }
    }

    override suspend fun deleteOldStrikes(retentionPeriod: Long) {
        val oneDayInMillis = 24 * 60 * 60 * 1000
        val cutoff = System.currentTimeMillis() - (retentionPeriod * oneDayInMillis)
        writeDao.deleteStrikesOlderThan(cutoff)
    }
} 