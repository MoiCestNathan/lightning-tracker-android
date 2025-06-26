package com.example.lightningtracker.data.repository

import com.example.lightningtracker.data.local.dao.LightningReadDao
import com.example.lightningtracker.data.local.dao.LightningWriteDao
import com.example.lightningtracker.data.remote.LightningApiClient
import com.example.lightningtracker.data.remote.dto.LightningStrikeDto
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LightningRepositoryImplTest {

    private lateinit var repository: LightningRepositoryImpl
    private val apiClient: LightningApiClient = mockk(relaxed = true)
    private val readDao: LightningReadDao = mockk(relaxed = true)
    private val writeDao: LightningWriteDao = mockk(relaxed = true)

    @Before
    fun setUp() {
        repository = LightningRepositoryImpl(apiClient, readDao, writeDao)
    }

    @Test
    fun `fetchAndStoreNewStrikes fetches from api and inserts into dao`() = runTest {
        // When
        repository.fetchAndStoreNewStrikes()

        // Then
        coVerify { apiClient.getRecentStrikes() }
        coVerify { writeDao.insertStrike(any()) }
    }

    @Test
    fun `deleteOldStrikes calls dao with correct cutoff`() = runTest {
        // Given
        val retentionPeriodDays = 7L

        // When
        repository.deleteOldStrikes(retentionPeriodDays)

        // Then
        coVerify { writeDao.deleteStrikesOlderThan(any()) }
    }
} 