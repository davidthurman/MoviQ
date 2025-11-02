package com.dthurman.moviesaver.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromSyncState(value: SyncState): String {
        return value.name
    }
    
    @TypeConverter
    fun toSyncState(value: String): SyncState {
        return try {
            SyncState.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SyncState.SYNCED
        }
    }
}

@Database(
    entities = [MovieEntity::class],
    version = 18,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
