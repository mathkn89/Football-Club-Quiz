package com.ruflo.footballquiz.core

import android.content.Context
import com.ruflo.footballquiz.data.local.HistoryDatabase
import com.ruflo.footballquiz.data.local.QuizDatabase
import com.ruflo.footballquiz.data.local.UserProfilePreferences
import com.ruflo.footballquiz.domain.usecase.GetQuizRoundUseCase

/** Manual DI container — swap for Hilt/Koin providers if/when a DI framework is introduced. */
class AppContainer(context: Context) {

    private val database = QuizDatabase.getInstance(context)
    private val historyDatabase = HistoryDatabase.getInstance(context)

    val clubDao = database.clubDao()
    val quizAttemptDao = historyDatabase.quizAttemptDao()
    val userProfilePreferences = UserProfilePreferences(context)

    val getQuizRoundUseCase = GetQuizRoundUseCase(
        clubDao = database.clubDao(),
        customQuestionDao = database.customQuestionDao(),
    )
}
