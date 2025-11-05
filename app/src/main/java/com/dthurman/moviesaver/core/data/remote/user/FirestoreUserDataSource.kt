package com.dthurman.moviesaver.core.data.remote.user

import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val errorLogger: ErrorLogger
) : UserRemoteDataSource {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun fetchUserProfile(userId: String): Result<User?> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val user = snapshot.toObject(FirestoreUserDto::class.java)?.toDomain()
            Result.success(user)
        } catch (e: Exception) {
            errorLogger.logNetworkError("fetchUserProfile", e)
            Result.failure(e)
        }
    }

    override suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            val firestoreUser = user.toFirestoreDto()
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(firestoreUser)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            errorLogger.logNetworkError("createUserProfile", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any?>(
                "email" to user.email,
                "displayName" to user.displayName,
                "photoUrl" to user.photoUrl,
                "credits" to user.credits,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            errorLogger.logNetworkError("updateUserProfile", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUserCredits(userId: String, credits: Int): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "credits" to credits,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            errorLogger.logNetworkError("updateUserCredits", e)
            Result.failure(e)
        }
    }
}

