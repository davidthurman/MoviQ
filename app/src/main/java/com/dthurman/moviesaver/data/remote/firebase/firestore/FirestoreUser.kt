package com.dthurman.moviesaver.data.remote.firebase.firestore

import com.dthurman.moviesaver.domain.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore user document model
 * Stored at: users/{userId}
 */
data class FirestoreUser(
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

/**
 * Convert domain User model to Firestore model
 */
fun User.toFirestoreUser(): FirestoreUser {
    return FirestoreUser(
        id = this.id,
        email = this.email,
        displayName = this.displayName,
        photoUrl = this.photoUrl,
        credits = this.credits,
        createdAt = null,
        lastUpdated = null
    )
}

/**
 * Convert Firestore model to domain User model
 */
fun FirestoreUser.toUser(): User {
    return User(
        id = this.id,
        email = this.email,
        displayName = this.displayName,
        photoUrl = this.photoUrl,
        credits = this.credits
    )
}

