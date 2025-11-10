package com.dthurman.moviesaver.core.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

// Normally I'd separate data classes and map them when using, but I'm
// using a single data class for Movie Entity, domain, and DTO for simplicity
// since they're essentially all the same and mapping logic made things needlessly complicated
@Entity(tableName = "movie")
data class Movie(
    @PrimaryKey 
    val id: Int,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val releaseDate: String,
    val overview: String,

    @get:PropertyName("isSeen") @set:PropertyName("isSeen") 
    var isSeen: Boolean = false,

    @get:PropertyName("isWatchlist") @set:PropertyName("isWatchlist")
    var isWatchlist: Boolean = false,

    @get:PropertyName("isFavorite") @set:PropertyName("isFavorite")
    var isFavorite: Boolean = false,

    val rating: Float? = null,
    val aiReason: String? = null,
    
    @get:PropertyName("notInterested") @set:PropertyName("notInterested")
    var notInterested: Boolean = false,

    val addedAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val syncState: SyncState = SyncState.SYNCED
)
