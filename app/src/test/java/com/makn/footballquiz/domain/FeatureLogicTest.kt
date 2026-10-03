package com.makn.footballquiz.domain

import com.makn.footballquiz.data.local.PlayProgressPreferences
import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.domain.generator.KitQuestionGenerator
import com.makn.footballquiz.domain.generator.StadiumQuestionGenerator
import com.makn.footballquiz.domain.model.DailyChallenge
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizMode
import com.makn.footballquiz.domain.model.QuizQuestion
import com.makn.footballquiz.ui.history.HistoryUiState
import com.makn.footballquiz.ui.history.TopicStat
import com.makn.footballquiz.ui.quiz.QuizUiState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureLogicTest {

    private val clubs = ('a'..'h').mapIndexed { i, c ->
        ClubEntity(
            id = "$c", name = "Club $c F.C.", shortName = "Club $c", nickname = "Nick $c", stadiumName = "Ground $c",
            stadiumCapacity = 1000 * (i + 1), foundedYear = 1880 + i * 5, city = "Town $c", badgeDrawableName = null,
            badgeRemoteUrl = null, version = 1, manager = "Manager $c", league = "League One",
            kitPattern = "PLAIN", kitPrimary = listOf("RED", "BLUE", "WHITE", "BLACK", "YELLOW", "GREEN", "ORANGE", "CLARET")[i],
            kitShorts = "WHITE",
        )
    }

    @Test
    fun `same daily seed gives the same question and option order`() {
        val day = LocalDate.of(2026, 10, 12)
        fun build() = listOf(StadiumQuestionGenerator(EnglishQuizStrings), KitQuestionGenerator(EnglishQuizStrings)).map { generator ->
            val random = DailyChallenge.random(day)
            generator.generate(clubs[0], clubs.drop(1), Difficulty.MEDIUM, random)!!.let { it.prompt to it.options }
        }
        assertEquals(build(), build())
    }

    @Test
    fun `daily challenge numbering starts at 1 and seeds differ per day`() {
        assertEquals(1, DailyChallenge.number(LocalDate.of(2026, 10, 1)))
        assertEquals(12, DailyChallenge.number(LocalDate.of(2026, 10, 12)))
        val a = DailyChallenge.random(LocalDate.of(2026, 10, 12)).nextInt()
        val b = DailyChallenge.random(LocalDate.of(2026, 10, 13)).nextInt()
        assertTrue(a != b)
    }

    @Test
    fun `streak lapses after a skipped day`() {
        fun progress(lastDay: LocalDate) = PlayProgressPreferences.Progress(
            lastDailyDay = lastDay.toEpochDay(), dailyStreak = 4, bestDailyStreak = 4, lastDailyScore = 7,
            lastDailyTotal = 10, lastDailyPattern = "", survivalBest = 0, remindersEnabled = false,
        )
        val today = LocalDate.of(2026, 10, 12)
        assertEquals(4, progress(today).currentStreak(today))
        assertEquals(4, progress(today.minusDays(1)).currentStreak(today))
        assertEquals(0, progress(today.minusDays(2)).currentStreak(today))
        assertTrue(progress(today).playedDailyOn(today))
    }

    @Test
    fun `weak topics need enough answers and pick the lowest scores first`() {
        val stats = listOf(
            TopicStat(QuizCategory.STADIUM, answered = 20, correct = 18), // 90% — fine
            TopicStat(QuizCategory.KIT, answered = 10, correct = 3), // 30%
            TopicStat(QuizCategory.MANAGER, answered = 10, correct = 6), // 60%
            TopicStat(QuizCategory.LEAGUE, answered = 3, correct = 0), // too few answers
            TopicStat(QuizCategory.NICKNAME, answered = 10, correct = 7), // 70%
            TopicStat(QuizCategory.FOUNDED_YEAR, answered = 10, correct = 5), // 50%
        )
        assertEquals(
            setOf(QuizCategory.KIT, QuizCategory.FOUNDED_YEAR, QuizCategory.MANAGER),
            HistoryUiState.weakTopics(stats),
        )
    }

    @Test
    fun `survival ends on the first miss, classic only on the last question`() {
        val question = QuizQuestion("q", "?", listOf("a", "b", "c", "d"), correctOptionIndex = 0, category = QuizCategory.STADIUM)
        val survival = QuizUiState.InProgress(QuizMode.SURVIVAL, List(5) { question }, 0, 0, secondsPerQuestion = 15)
        assertFalse(survival.copy(selectedOptionIndex = 0).endsRound)
        assertTrue(survival.copy(selectedOptionIndex = 2).endsRound)
        assertTrue(survival.copy(selectedOptionIndex = null).endsRound)

        val classic = survival.copy(mode = QuizMode.STANDARD, selectedOptionIndex = 2)
        assertFalse(classic.endsRound)
        assertTrue(classic.copy(currentIndex = 4).endsRound)
    }
}
