package com.makn.footballquiz.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.makn.footballquiz.data.local.QuizDatabase
import com.makn.footballquiz.data.local.SyncPreferences
import com.makn.footballquiz.data.remote.NetworkModule
import com.makn.footballquiz.data.repository.SyncRepository
import com.makn.footballquiz.data.repository.SyncResult

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = QuizDatabase.getInstance(applicationContext)
        val repository = SyncRepository(
            api = NetworkModule.syncApiService,
            clubDao = database.clubDao(),
            customQuestionDao = database.customQuestionDao(),
            syncPreferences = SyncPreferences(applicationContext),
        )

        return when (repository.sync()) {
            is SyncResult.UpToDate,
            is SyncResult.Updated,
            -> Result.success()

            is SyncResult.Error -> {
                if (runAttemptCount < MAX_RUN_ATTEMPTS) Result.retry() else Result.failure()
            }
        }
    }

    companion object {
        const val UNIQUE_PERIODIC_WORK_NAME = "football_quiz_periodic_sync"
        const val UNIQUE_ONE_TIME_WORK_NAME = "football_quiz_one_time_sync"
        private const val MAX_RUN_ATTEMPTS = 3
    }
}
