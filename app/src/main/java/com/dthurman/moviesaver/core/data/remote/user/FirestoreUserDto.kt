package com.dthurman.moviesaver.core.data.remote.user

import com.dthurman.moviesaver.core.domain.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class FirestoreUserDto(
    val id: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val credits: Int = 10,
    @ServerTimestamp
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Timestamp? = null,
    @ServerTimestamp
    @get:PropertyName("lastUpdated") @set:PropertyName("lastUpdated") var lastUpdated: Timestamp? = null
)

fun User.toFirestoreDto(): FirestoreUserDto {
    return FirestoreUserDto(
        id = this.id,
        email = this.email,
        displayName = this.displayName,
        photoUrl = this.photoUrl,
        credits = this.credits,
        createdAt = null,
        lastUpdated = null
    )
}

fun FirestoreUserDto.toDomain(): User {
    return User(
        id = this.id,
        email = this.email,
        displayName = this.displayName,
        photoUrl = this.photoUrl,
        credits = this.credits
    )
}

