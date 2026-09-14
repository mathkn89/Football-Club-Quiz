package com.ruflo.footballquiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_questions")
data class CustomQuestionEntity(
    @PrimaryKey
    val id: String,
    val questionText: String,
    val category: String,
    val correctAnswer: String,
    val wrongAnswers: List<String>,
    val explanation: String?,
    val imageUriOrUrl: String?,
    val version: Int,
)
