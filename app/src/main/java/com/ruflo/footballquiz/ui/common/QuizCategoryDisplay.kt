package com.ruflo.footballquiz.ui.common

import com.ruflo.footballquiz.domain.model.QuizCategory

fun QuizCategory.displayName(): String = when (this) {
    QuizCategory.STADIUM -> "Stadiums"
    QuizCategory.NICKNAME -> "Nicknames"
    QuizCategory.FOUNDED_YEAR -> "Founded"
    QuizCategory.BADGE -> "Badges"
    QuizCategory.KIT -> "Kits"
    QuizCategory.LEAGUE -> "Leagues"
    QuizCategory.LOCATION -> "Cities"
    QuizCategory.MANAGER -> "Managers"
    QuizCategory.HISTORY -> "History"
    QuizCategory.TRANSFERS -> "Transfers"
    QuizCategory.RIVALRIES -> "Rivalries"
    QuizCategory.GENERAL -> "General"
}
