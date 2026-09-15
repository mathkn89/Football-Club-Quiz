package com.ruflo.footballquiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey
    val id: String,
    val completedAtMillis: Long,
    val score: Int,
    val total: Int,
    val categories: List<String>,
)
