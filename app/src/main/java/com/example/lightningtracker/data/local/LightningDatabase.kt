package com.example.lightningtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lightningtracker.data.local.dao.LightningReadDao
import com.example.lightningtracker.data.local.dao.LightningWriteDao
import com.example.lightningtracker.data.local.entity.LightningStrikeEntity

@Database(
    entities = [LightningStrikeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LightningDatabase : RoomDatabase() {

    abstract fun lightningReadDao(): LightningReadDao
    abstract fun lightningWriteDao(): LightningWriteDao

    companion object {
        const val DATABASE_NAME = "lightning_tracker_db"
    }
} 