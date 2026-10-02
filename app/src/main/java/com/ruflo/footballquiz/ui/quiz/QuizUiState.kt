package com.ruflo.footballquiz.ui.quiz

import com.ruflo.footballquiz.domain.model.QuizQuestion

sealed interface QuizUiState {

    data object Loading : QuizUiState

    /** No questions could be generated — e.g. the local database hasn't synced yet. */
    data object Empty : QuizUiState

    data class Finished(val score: Int, val total: Int) : QuizUiState

    data class InProgress(
        val questions: List<QuizQuestion>,
        val currentIndex: Int,
        val score: Int,
        val selectedOptionIndex: Int? = null,
        val isAnswerRevealed: Boolean = false,
        val secondsPerQuestion: Int,
        val timeRemainingSeconds: Int = secondsPerQuestion,
    ) : QuizUiState {
        val timeFraction: Float get() = timeRemainingSeconds / secondsPerQuestion.toFloat()
        val currentQuestion: QuizQuestion get() = questions[currentIndex]
        val questionNumber: Int get() = currentIndex + 1
        val totalQuestions: Int get() = questions.size
        val isLastQuestion: Boolean get() = questionNumber == totalQuestions
    }
}
