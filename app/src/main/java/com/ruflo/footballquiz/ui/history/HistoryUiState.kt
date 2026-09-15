package com.ruflo.footballquiz.ui.history

import com.ruflo.footballquiz.domain.model.QuizAttempt

sealed interface HistoryUiState {

    data object Loading : HistoryUiState

    data class Content(
        val displayName: String,
        val attempts: List<QuizAttempt>,
    ) : HistoryUiState {
        val isEmpty: Boolean get() = attempts.isEmpty()
    }
}
