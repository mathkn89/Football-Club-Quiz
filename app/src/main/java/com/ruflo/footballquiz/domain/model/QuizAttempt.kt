package com.ruflo.footballquiz.domain.model

data class QuizAttempt(
    val id: String,
    val completedAtMillis: Long,
    val score: Int,
    val total: Int,
    val categories: Set<QuizCategory>,
) {
    val percentage: Float get() = if (total == 0) 0f else score.toFloat() / total
}
