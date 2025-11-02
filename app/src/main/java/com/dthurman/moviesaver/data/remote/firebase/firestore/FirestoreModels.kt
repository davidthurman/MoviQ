package com.dthurman.moviesaver.data.remote.firebase.firestore

import com.dthurman.moviesaver.data.local.MovieEntity
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirestoreMovie(
    @DocumentId val id: String = "",
    val movieId: Int = 0,
    val title: String = "",
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val releaseDate: String = "",
    val overview: String = "",
    @get:PropertyName("isSeen") @set:PropertyName("isSeen") var isSeen: Boolean = false,
    @get:PropertyName("isWatchlist") @set:PropertyName("isWatchlist") var isWatchlist: Boolean = false,
    @get:PropertyName("isFavorite") @set:PropertyName("isFavorite") var isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val rating: Float? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val aiReason: String? = null,
    @get:PropertyName("notInterested") @set:PropertyName("notInterested") var notInterested: Boolean = false
)

fun MovieEntity.toFirestore(): FirestoreMovie {
    return FirestoreMovie(
        id = this.id.toString(),
        movieId = this.id,
        title = this.title,
        posterUrl = this.posterUrl,
        backdropUrl = this.backdropUrl,
        releaseDate = this.releaseDate,
        overview = this.overview,
        isSeen = this.isSeen,
        isWatchlist = this.isWatchlist,
        isFavorite = this.isFavorite,
        addedAt = this.addedAt,
        rating = this.rating,
        lastModified = System.currentTimeMillis(),
        aiReason = this.aiReason,
        notInterested = this.notInterested
    )
}

fun FirestoreMovie.toEntity(): MovieEntity {
    return MovieEntity(
        id = this.movieId,
        title = this.title,
        posterUrl = this.posterUrl,
        backdropUrl = this.backdropUrl,
        releaseDate = this.releaseDate,
        overview = this.overview,
        isSeen = this.isSeen,
        isWatchlist = this.isWatchlist,
        isFavorite = this.isFavorite,
        addedAt = this.addedAt,
        rating = this.rating,
        lastModified = this.lastModified,
        aiReason = this.aiReason,
        notInterested = this.notInterested
    )
}


