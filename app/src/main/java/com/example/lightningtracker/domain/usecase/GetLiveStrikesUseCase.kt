package com.example.lightningtracker.domain.usecase

import com.example.lightningtracker.domain.model.LightningStrike
import com.example.lightningtracker.domain.repository.LightningRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLiveStrikesUseCase @Inject constructor(
    private val repository: LightningRepository
) {
    operator fun invoke(): Flow<LightningStrike> {
        return repository.getLiveStrikes()
    }
} 