package com.ruflo.footballquiz.ui.picker

import com.ruflo.footballquiz.domain.model.QuizCategory

data class PickerUiState(
    val roundSize: Int = DEFAULT_ROUND_SIZE,
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

    companion object {
        const val DEFAULT_ROUND_SIZE = 10
        val ROUND_SIZE_OPTIONS = listOf(5, 10, 15)
    }
}
