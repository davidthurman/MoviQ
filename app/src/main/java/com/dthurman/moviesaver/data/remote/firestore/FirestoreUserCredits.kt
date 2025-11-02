package com.dthurman.moviesaver.data.remote.firestore

import com.dthurman.moviesaver.data.local.database.UserCreditsEntity
import com.google.firebase.firestore.PropertyName

data class FirestoreUserCredits(
    val userId: String = "",
    val credits: Int = 10,
    @get:PropertyName("lastUpdated") @set:PropertyName("lastUpdated") var lastUpdated: Long = System.currentTimeMillis()
)

fun UserCreditsEntity.toFirestore(): FirestoreUserCredits {
    return FirestoreUserCredits(
        userId = this.userId,
        credits = this.credits,
        lastUpdated = this.lastUpdated
    )
}

fun FirestoreUserCredits.toEntity(): UserCreditsEntity {
    return UserCreditsEntity(
        userId = this.userId,
        credits = this.credits,
        lastUpdated = this.lastUpdated
    )
}

