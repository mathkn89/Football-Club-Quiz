package com.makn.footballquiz.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.makn.footballquiz.R
import com.makn.footballquiz.domain.model.QuizCategory

@Composable
fun QuizCategory.displayName(): String = stringResource(nameRes())

@StringRes
fun QuizCategory.nameRes(): Int = when (this) {
    QuizCategory.STADIUM -> R.string.category_stadium
    QuizCategory.NICKNAME -> R.string.category_nickname
    QuizCategory.FOUNDED_YEAR -> R.string.category_founded
    QuizCategory.BADGE -> R.string.category_badge
    QuizCategory.KIT -> R.string.category_kit
    QuizCategory.LEAGUE -> R.string.category_league
    QuizCategory.LOCATION -> R.string.category_location
    QuizCategory.MANAGER -> R.string.category_manager
    QuizCategory.HISTORY -> R.string.category_history
    QuizCategory.TRANSFERS -> R.string.category_transfers
    QuizCategory.RIVALRIES -> R.string.category_rivalries
    QuizCategory.GENERAL -> R.string.category_general
}
