package com.makn.footballquiz.ui.quiz

import com.makn.footballquiz.domain.model.QuizMode
import com.makn.footballquiz.domain.model.QuizQuestion

/** One question as played: what was asked and what was picked (null = time ran out). */
data class AnsweredQuestion(
    val question: QuizQuestion,
    val selectedOptionIndex: Int?,
    /** Second player's pick in a duel; unused otherwise. */
    val secondSelectedOptionIndex: Int? = null,
) {
    val isCorrect: Boolean get() = selectedOptionIndex == question.correctOptionIndex
    val isSecondCorrect: Boolean get() = secondSelectedOptionIndex == question.correctOptionIndex
}

/** Whose turn it is in a duel, and whether the phone is being passed between turns. */
enum class DuelPhase { FIRST_PLAYER, HANDOFF, SECOND_PLAYER }

sealed interface QuizUiState {

    data object Loading : QuizUiState

    /** No questions could be generated — e.g. the local database hasn't synced yet. */
    data object Empty : QuizUiState

    data class InProgress(
        val mode: QuizMode,
        val questions: List<QuizQuestion>,
        val currentIndex: Int,
        val score: Int,
        val answered: List<AnsweredQuestion> = emptyList(),
        val selectedOptionIndex: Int? = null,
        val isAnswerRevealed: Boolean = false,
        val secondsPerQuestion: Int,
        val timeRemainingSeconds: Int = secondsPerQuestion,
        /** Duel only. */
        val duelPhase: DuelPhase = DuelPhase.FIRST_PLAYER,
        val firstPlayerPick: Int? = null,
        val secondScore: Int = 0,
    ) : QuizUiState {
        val timeFraction: Float get() = timeRemainingSeconds / secondsPerQuestion.toFloat()
        val currentQuestion: QuizQuestion get() = questions[currentIndex]
        val questionNumber: Int get() = currentIndex + 1
        val totalQuestions: Int get() = questions.size
        val isLastQuestion: Boolean get() = mode != QuizMode.SURVIVAL && questionNumber == totalQuestions

        /** Survival ends the round on the first miss. */
        val endsRound: Boolean
            get() = isLastQuestion || (mode == QuizMode.SURVIVAL && selectedOptionIndex != currentQuestion.correctOptionIndex)
    }

    data class Finished(
        val mode: QuizMode,
        val answered: List<AnsweredQuestion>,
        val score: Int,
        val total: Int,
        /** Daily: challenge number and streak after this round. */
        val dailyNumber: Int? = null,
        val streak: Int? = null,
        /** Survival: best run so far, including this one. */
        val survivalBest: Int? = null,
        /** Duel: second player's score. */
        val secondScore: Int? = null,
    ) : QuizUiState {
        val mistakes: List<AnsweredQuestion> get() = answered.filterNot { it.isCorrect }
    }
}
