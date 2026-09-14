package com.ruflo.footballquiz.data.remote.dto

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
)
