package com.makn.footballquiz.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CustomQuestionDeltaDto(
    val id: String,
    val questionText: String,
    val category: String,
    val correctAnswer: String,
    val wrongAnswers: List<String>,
    val explanation: String? = null,
    val imageUriOrUrl: String? = null,
    val version: Int,
    /** Language code ("de", "fr", "nb", "sv") → the question in that language. Missing = English. */
    val translations: Map<String, QuestionTranslationDto> = emptyMap(),
)

@Serializable
data class QuestionTranslationDto(
    val questionText: String,
    val correctAnswer: String,
    val wrongAnswers: List<String>,
    val explanation: String? = null,
)
