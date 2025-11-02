package com.dthurman.moviesaver.data.remote.firestore

import com.dthurman.moviesaver.domain.model.User
import com.google.firebase.firestore.PropertyName

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
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("lastUpdated") @set:PropertyName("lastUpdated") var lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Convert domain User model to Firestore model
 */
fun User.toFirestoreUser(credits: Int = 10): FirestoreUser {
    return FirestoreUser(
        id = this.id,
        email = this.email,
        displayName = this.displayName,
        photoUrl = this.photoUrl,
        credits = credits,
        createdAt = System.currentTimeMillis(),
        lastUpdated = System.currentTimeMillis()
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
        photoUrl = this.photoUrl
    )
}

