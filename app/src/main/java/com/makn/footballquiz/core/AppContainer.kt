package com.makn.footballquiz.core

import android.content.Context
import com.makn.footballquiz.data.local.HistoryDatabase
import com.makn.footballquiz.data.local.QuizDatabase
import com.makn.footballquiz.data.local.UserProfilePreferences
import com.makn.footballquiz.domain.usecase.GetQuizRoundUseCase

/** Manual DI container — swap for Hilt/Koin providers if/when a DI framework is introduced. */
class AppContainer(context: Context) {

    private val database = QuizDatabase.getInstance(context)
    private val historyDatabase = HistoryDatabase.getInstance(context)

    val clubDao = database.clubDao()
    val customQuestionDao = database.customQuestionDao()
    val quizAttemptDao = historyDatabase.quizAttemptDao()
    val userProfilePreferences = UserProfilePreferences(context)

    val getQuizRoundUseCase = GetQuizRoundUseCase(
        clubDao = clubDao,
        customQuestionDao = customQuestionDao,
    )
}
