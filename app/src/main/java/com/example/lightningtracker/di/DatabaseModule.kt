package com.example.lightningtracker.di

import android.app.Application
import androidx.room.Room
import com.example.lightningtracker.data.local.LightningDatabase
import com.example.lightningtracker.data.local.dao.LightningReadDao
import com.example.lightningtracker.data.local.dao.LightningWriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLightningDatabase(app: Application): LightningDatabase {
        return Room.databaseBuilder(
            app,
            LightningDatabase::class.java,
            LightningDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideLightningReadDao(db: LightningDatabase): LightningReadDao {
        return db.lightningReadDao()
    }

    @Provides
    @Singleton
    fun provideLightningWriteDao(db: LightningDatabase): LightningWriteDao {
        return db.lightningWriteDao()
    }
} 