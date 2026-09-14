package com.ruflo.footballquiz.core

import android.content.Context
import com.ruflo.footballquiz.data.local.QuizDatabase
import com.ruflo.footballquiz.domain.usecase.GetQuizRoundUseCase

/** Manual DI container — swap for Hilt/Koin providers if/when a DI framework is introduced. */
class AppContainer(context: Context) {

    private val database = QuizDatabase.getInstance(context)

    val getQuizRoundUseCase = GetQuizRoundUseCase(
        clubDao = database.clubDao(),
        customQuestionDao = database.customQuestionDao(),
    )
}
