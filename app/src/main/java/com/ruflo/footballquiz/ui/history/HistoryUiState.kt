package com.ruflo.footballquiz.ui.history

import com.ruflo.footballquiz.domain.model.QuizAttempt
import kotlin.math.roundToInt

sealed interface HistoryUiState {

    data object Loading : HistoryUiState

    data class Content(
        val displayName: String,
        val attempts: List<QuizAttempt>,
    ) : HistoryUiState {
        val isEmpty: Boolean get() = attempts.isEmpty()

        /** Mean score across all rounds, 0–100; null before the first round. */
        val averagePercent: Int?
            get() = attempts.takeIf { it.isNotEmpty() }?.let { list -> (list.map { it.percentage }.average() * 100).roundToInt() }

        val bestPercent: Int?
            get() = attempts.maxOfOrNull { it.percentage }?.let { (it * 100).roundToInt() }
    }
}
