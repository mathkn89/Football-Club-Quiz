package com.makn.footballquiz.data.local.entity

import androidx.room.ColumnInfo
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
    /** [com.makn.footballquiz.domain.model.QuizMode] name. */
    @ColumnInfo(defaultValue = "STANDARD")
    val mode: String = "STANDARD",
)
