package com.dthurman.moviesaver.data.local.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "recommendations")
data class RecommendationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val movieId: Int,
    val aiReason: String,
    val orderIndex: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM recommendations ORDER BY orderIndex ASC")
    fun getAllRecommendations(): Flow<List<RecommendationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: RecommendationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<RecommendationEntity>)
    
    @Query("DELETE FROM recommendations")
    suspend fun clearAllRecommendations()
    
    @Query("DELETE FROM recommendations WHERE id = :id")
    suspend fun deleteRecommendation(id: Int)
}


