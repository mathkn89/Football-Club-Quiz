package com.makn.footballquiz.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.progressStore by preferencesDataStore(name = "play_progress")

/** Daily-challenge streak, survival best and reminder setting — device-only. */
class PlayProgressPreferences(private val context: Context) {

    data class Progress(
        val lastDailyDay: Long?,
        val dailyStreak: Int,
        val bestDailyStreak: Int,
        val lastDailyScore: Int,
        val lastDailyTotal: Int,
        /** One char per question: '1' right, '0' wrong — for the share card. */
        val lastDailyPattern: String,
        val survivalBest: Int,
        val remindersEnabled: Boolean,
    ) {
        fun playedDailyOn(day: LocalDate): Boolean = lastDailyDay == day.toEpochDay()

        /** The streak as it stands on [today]: it lapses once a whole day is skipped. */
        fun currentStreak(today: LocalDate): Int {
            val last = lastDailyDay ?: return 0
            return if (today.toEpochDay() - last <= 1) dailyStreak else 0
        }
    }

    private val lastDailyDayKey = longPreferencesKey("last_daily_day")
    private val dailyStreakKey = intPreferencesKey("daily_streak")
    private val bestDailyStreakKey = intPreferencesKey("best_daily_streak")
    private val lastDailyScoreKey = intPreferencesKey("last_daily_score")
    private val lastDailyTotalKey = intPreferencesKey("last_daily_total")
    private val lastDailyPatternKey = stringPreferencesKey("last_daily_pattern")
    private val survivalBestKey = intPreferencesKey("survival_best")
    private val remindersEnabledKey = booleanPreferencesKey("reminders_enabled")

    val progress: Flow<Progress> = context.progressStore.data.map {
        Progress(
            lastDailyDay = it[lastDailyDayKey],
            dailyStreak = it[dailyStreakKey] ?: 0,
            bestDailyStreak = it[bestDailyStreakKey] ?: 0,
            lastDailyScore = it[lastDailyScoreKey] ?: 0,
            lastDailyTotal = it[lastDailyTotalKey] ?: 0,
            lastDailyPattern = it[lastDailyPatternKey].orEmpty(),
            survivalBest = it[survivalBestKey] ?: 0,
            remindersEnabled = it[remindersEnabledKey] ?: false,
        )
    }

    suspend fun current(): Progress = progress.first()

    /** Records today's daily result once; returns the streak after it. */
    suspend fun recordDaily(day: LocalDate, score: Int, total: Int, pattern: String): Int {
        var streak = 0
        context.progressStore.edit { prefs ->
            val last = prefs[lastDailyDayKey]
            if (last == day.toEpochDay()) {
                streak = prefs[dailyStreakKey] ?: 1
                return@edit
            }
            streak = if (last == day.toEpochDay() - 1) (prefs[dailyStreakKey] ?: 0) + 1 else 1
            prefs[lastDailyDayKey] = day.toEpochDay()
            prefs[dailyStreakKey] = streak
            prefs[bestDailyStreakKey] = maxOf(prefs[bestDailyStreakKey] ?: 0, streak)
            prefs[lastDailyScoreKey] = score
            prefs[lastDailyTotalKey] = total
            prefs[lastDailyPatternKey] = pattern
        }
        return streak
    }

    /** Returns the best survival run after recording [run]. */
    suspend fun recordSurvival(run: Int): Int {
        var best = run
        context.progressStore.edit { prefs ->
            best = maxOf(prefs[survivalBestKey] ?: 0, run)
            prefs[survivalBestKey] = best
        }
        return best
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.progressStore.edit { it[remindersEnabledKey] = enabled }
    }
}
