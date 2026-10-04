package com.makn.footballquiz.core

import android.content.Context
import com.makn.footballquiz.data.local.HistoryDatabase
import com.makn.footballquiz.data.local.PlayProgressPreferences
import com.makn.footballquiz.data.local.QuizDatabase
import com.makn.footballquiz.data.local.UserProfilePreferences
import com.makn.footballquiz.domain.usecase.GetQuizRoundUseCase
import com.makn.footballquiz.monetization.AdsManager
import com.makn.footballquiz.monetization.BillingManager
import com.makn.footballquiz.monetization.ConsentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Manual DI container — swap for Hilt/Koin providers if/when a DI framework is introduced. */
class AppContainer(context: Context) {

    private val database = QuizDatabase.getInstance(context)
    private val historyDatabase = HistoryDatabase.getInstance(context)

    val clubDao = database.clubDao()
    val customQuestionDao = database.customQuestionDao()
    val quizAttemptDao = historyDatabase.quizAttemptDao()
    val userProfilePreferences = UserProfilePreferences(context)
    val playProgressPreferences = PlayProgressPreferences(context)

    /** Lives as long as the app process; for monetization state that outlives screens. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val consentManager = ConsentManager(context)
    val billingManager = BillingManager(context, appScope)
    val adsManager = AdsManager(context, consentManager, billingManager, appScope)

    val getQuizRoundUseCase = GetQuizRoundUseCase(
        clubDao = clubDao,
        customQuestionDao = customQuestionDao,
    )
}
