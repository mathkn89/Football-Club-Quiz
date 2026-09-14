package com.ruflo.footballquiz.ui.picker

import com.ruflo.footballquiz.domain.model.QuizCategory

data class PickerUiState(
    val roundSize: Int = DEFAULT_ROUND_SIZE,
    val selectedCategories: Set<QuizCategory> = QuizCategory.entries.toSet(),
) {
    companion object {
        const val DEFAULT_ROUND_SIZE = 10
        val ROUND_SIZE_OPTIONS = listOf(5, 10, 15)
    }
}
