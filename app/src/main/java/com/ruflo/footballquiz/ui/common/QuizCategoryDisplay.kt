package com.ruflo.footballquiz.ui.common

import com.ruflo.footballquiz.domain.model.QuizCategory

fun QuizCategory.displayName(): String = when (this) {
    QuizCategory.STADIUM -> "Stadiums"
    QuizCategory.NICKNAME -> "Nicknames"
    QuizCategory.FOUNDED_YEAR -> "Founded"
    QuizCategory.BADGE -> "Badges"
    QuizCategory.HISTORY -> "History"
    QuizCategory.TRANSFERS -> "Transfers"
    QuizCategory.RIVALRIES -> "Rivalries"
    QuizCategory.GENERAL -> "General"
}

fun QuizCategory.emoji(): String = when (this) {
    QuizCategory.STADIUM -> "🏟️"
    QuizCategory.NICKNAME -> "🎽"
    QuizCategory.FOUNDED_YEAR -> "📅"
    QuizCategory.BADGE -> "🛡️"
    QuizCategory.HISTORY -> "📜"
    QuizCategory.TRANSFERS -> "🔄"
    QuizCategory.RIVALRIES -> "⚔️"
    QuizCategory.GENERAL -> "⭐"
}
