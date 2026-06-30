package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_lessons")
data class CompletedLesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "Sözleýiş Medeniýeti", "Adamlara Täsir Ýetirmek", "Biznes Strategiýalary"
    val title: String,
    val introduction: String,
    val coreConceptsJson: String, // JSON list of core concepts
    val lessonText: String,
    val practicalAssignment: String,
    val quizJson: String, // JSON of quiz questions and user performance
    val quizScore: Int,
    val quizTotal: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val userId: String = "current_user",
    val name: String = "Merdan",
    val streak: Int = 0,
    val lastActiveDate: Long = 0,
    val xp: Int = 0,
    val completedLessonsCount: Int = 0,
    val sozleyisProgressCount: Int = 0,
    val tasirProgressCount: Int = 0,
    val biznesProgressCount: Int = 0
)
