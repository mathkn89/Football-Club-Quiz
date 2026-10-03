package com.makn.footballquiz.ui.picker

import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory

data class PickerUiState(
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
