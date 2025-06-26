package com.example.lightningtracker.di

import com.example.lightningtracker.data.repository.LightningRepositoryImpl
import com.example.lightningtracker.domain.repository.LightningRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLightningRepository(
        lightningRepositoryImpl: LightningRepositoryImpl
    ): LightningRepository
} 