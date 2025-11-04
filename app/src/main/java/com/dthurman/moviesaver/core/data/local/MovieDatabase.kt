package com.dthurman.moviesaver.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.model.SyncState

@Database(
    entities = [Movie::class],
    version = 18,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}

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