package com.dthurman.moviesaver.data.remote.firestore

import com.dthurman.moviesaver.data.local.database.MovieEntity
import com.dthurman.moviesaver.data.local.database.RecommendationEntity
import com.google.firebase.firestore.DocumentId

data class FirestoreMovie(
    @DocumentId val id: String = "",
    val movieId: Int = 0,
    val title: String = "",
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val releaseDate: String = "",
    val overview: String = "",
    val isSeen: Boolean = false,
    val isWatchlist: Boolean = false,
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val rating: Float? = null,
    val userId: String = "",
    val lastModified: Long = System.currentTimeMillis()
)

data class FirestoreRecommendation(
    @DocumentId val id: String = "",
    val movieId: Int = 0,
    val aiReason: String = "",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = "",
    val lastModified: Long = System.currentTimeMillis()
)

// Extension functions for conversion
fun MovieEntity.toFirestore(userId: String): FirestoreMovie {
    return FirestoreMovie(
        id = "${userId}_${this.id}",
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
        userId = userId,
        lastModified = System.currentTimeMillis()
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
        rating = this.rating
    )
}

fun RecommendationEntity.toFirestore(userId: String): FirestoreRecommendation {
    return FirestoreRecommendation(
        id = "${userId}_${this.id}",
        movieId = this.movieId,
        aiReason = this.aiReason,
        orderIndex = this.orderIndex,
        createdAt = this.createdAt,
        userId = userId,
        lastModified = System.currentTimeMillis()
    )
}

fun FirestoreRecommendation.toEntity(): RecommendationEntity {
    return RecommendationEntity(
        id = 0,
        movieId = this.movieId,
        aiReason = this.aiReason,
        orderIndex = this.orderIndex,
        createdAt = this.createdAt
    )
}

