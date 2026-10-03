package com.makn.footballquiz.data.local.entity

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
    /** JSON object of language code → [com.makn.footballquiz.data.remote.dto.QuestionTranslationDto]; null = English only. */
    val translations: String? = null,
)
