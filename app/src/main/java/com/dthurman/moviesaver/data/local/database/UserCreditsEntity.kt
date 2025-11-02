package com.dthurman.moviesaver.data.local.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_credits")
data class UserCreditsEntity(
    @PrimaryKey val userId: String,
    val credits: Int = 10,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Dao
interface UserCreditsDao {
    @Query("SELECT * FROM user_credits WHERE userId = :userId")
    fun getCreditsFlow(userId: String): Flow<UserCreditsEntity?>
    
    @Query("SELECT * FROM user_credits WHERE userId = :userId")
    suspend fun getCredits(userId: String): UserCreditsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(userCredits: UserCreditsEntity)
    
    @Query("UPDATE user_credits SET credits = credits - 1, lastUpdated = :timestamp WHERE userId = :userId AND credits > 0")
    suspend fun deductCredit(userId: String, timestamp: Long = System.currentTimeMillis()): Int
    
    @Query("UPDATE user_credits SET credits = :credits, lastUpdated = :timestamp WHERE userId = :userId")
    suspend fun updateCredits(userId: String, credits: Int, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM user_credits WHERE userId = :userId")
    suspend fun deleteCredits(userId: String)
    
    @Query("DELETE FROM user_credits")
    suspend fun clearAllCredits()
}

