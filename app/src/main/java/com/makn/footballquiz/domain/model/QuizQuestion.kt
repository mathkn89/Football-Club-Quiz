package com.makn.footballquiz.domain.model

data class QuizQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val category: QuizCategory,
    val explanation: String? = null,
    val imageUrl: String? = null,
    /** Kit to draw above the question (kit questions). */
    val kit: Kit? = null,
)
