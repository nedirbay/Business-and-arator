package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM completed_lessons ORDER BY timestamp DESC")
    fun getAllCompletedLessons(): Flow<List<CompletedLesson>>

    @Query("SELECT title FROM completed_lessons WHERE category = :category")
    suspend fun getCompletedTitlesByCategory(category: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedLesson(lesson: CompletedLesson)

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    fun getUserProfile(userId: String = "current_user"): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfileDirect(userId: String = "current_user"): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("DELETE FROM completed_lessons")
    suspend fun clearAllProgress()
}
