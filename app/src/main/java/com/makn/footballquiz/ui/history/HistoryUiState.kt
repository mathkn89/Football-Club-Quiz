package com.makn.footballquiz.ui.history

import com.makn.footballquiz.domain.model.QuizAttempt
import com.makn.footballquiz.domain.model.QuizCategory
import kotlin.math.roundToInt

/** How a player does on one topic across all recorded rounds. */
data class TopicStat(val category: QuizCategory, val answered: Int, val correct: Int) {
    val percent: Int get() = if (answered == 0) 0 else (correct * 100f / answered).roundToInt()
}

sealed interface HistoryUiState {

    data object Loading : HistoryUiState

    data class Content(
        val displayName: String,
        val attempts: List<QuizAttempt>,
        /** Best topic first. */
        val topicStats: List<TopicStat> = emptyList(),
        /** Up to three topics with enough answers and the lowest scores; empty = nothing to practise yet. */
        val weakTopics: Set<QuizCategory> = emptySet(),
        val remindersEnabled: Boolean = false,
    ) : HistoryUiState {
        val isEmpty: Boolean get() = attempts.isEmpty()

        /** Mean score across all rounds, 0–100; null before the first round. */
        val averagePercent: Int?
            get() = attempts.takeIf { it.isNotEmpty() }?.let { list -> (list.map { it.percentage }.average() * 100).roundToInt() }

        val bestPercent: Int?
            get() = attempts.maxOfOrNull { it.percentage }?.let { (it * 100).roundToInt() }
    }

    companion object {
        /** A topic needs this many answers before it can count as weak. */
        const val MIN_ANSWERS_FOR_WEAK = 5
        const val WEAK_BELOW_PERCENT = 80
        const val MAX_WEAK_TOPICS = 3

        fun weakTopics(stats: List<TopicStat>): Set<QuizCategory> = stats
            .filter { it.answered >= MIN_ANSWERS_FOR_WEAK && it.percent < WEAK_BELOW_PERCENT }
            .sortedBy { it.percent }
            .take(MAX_WEAK_TOPICS)
            .map { it.category }
            .toSet()
    }
}
