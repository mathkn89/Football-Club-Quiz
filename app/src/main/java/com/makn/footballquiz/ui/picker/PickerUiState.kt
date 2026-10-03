package com.makn.footballquiz.ui.picker

import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizMode

/** Today's daily challenge as shown on the Play screen. */
data class DailyStatus(
    val number: Int,
    val playedToday: Boolean,
    val score: Int,
    val total: Int,
    val streak: Int,
)

data class PickerUiState(
    val mode: QuizMode = QuizMode.STANDARD,
    val daily: DailyStatus? = null,
    val survivalBest: Int = 0,
    val roundSize: Int = DEFAULT_ROUND_SIZE,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val selectedCategories: Set<QuizCategory> = QuizCategory.entries.toSet(),
    /** Categories with questions behind them: the dynamic ones plus any custom ones synced. */
    val availableCategories: List<QuizCategory> = QuizCategory.DYNAMIC.toList(),
    /** In pyramid order (Premier League first). */
    val availableLeagues: List<String> = emptyList(),
    /** Null means "all leagues" — no filter. */
    val selectedLeague: String? = null,
    val clubCount: Int = 0,
) {
    /** Selected categories that are actually shown — what the round will be built from. */
    val activeCategories: Set<QuizCategory> get() = selectedCategories.intersect(availableCategories.toSet())

    val allCategoriesSelected: Boolean get() = activeCategories.size == availableCategories.size

    /** Club-data topics, shown as the first group. */
    val clubFactCategories: List<QuizCategory> get() = availableCategories.filter { it in QuizCategory.DYNAMIC }

    /** Hand-written trivia topics, shown as the second group. */
    val triviaCategories: List<QuizCategory> get() = availableCategories.filterNot { it in QuizCategory.DYNAMIC }

    val canStart: Boolean get() = clubCount > 0 && activeCategories.isNotEmpty()

    companion object {
        const val DEFAULT_ROUND_SIZE = 10
        val ROUND_SIZE_OPTIONS = listOf(5, 10, 15)
    }
}
