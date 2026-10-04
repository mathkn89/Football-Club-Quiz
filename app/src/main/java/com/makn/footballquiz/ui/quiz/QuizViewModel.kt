package com.makn.footballquiz.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makn.footballquiz.data.local.PlayProgressPreferences
import com.makn.footballquiz.data.local.dao.QuizAttemptDao
import com.makn.footballquiz.data.local.entity.AnswerRecordEntity
import com.makn.footballquiz.data.local.entity.QuizAttemptEntity
import com.makn.footballquiz.domain.model.DailyChallenge
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizMode
import com.makn.footballquiz.domain.model.QuizQuestion
import com.makn.footballquiz.domain.text.QuizStrings
import com.makn.footballquiz.domain.usecase.GetQuizRoundUseCase
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val getQuizRoundUseCase: GetQuizRoundUseCase,
    private val quizAttemptDao: QuizAttemptDao,
    private val progress: PlayProgressPreferences,
    private val mode: QuizMode,
    private val roundSize: Int,
    private val categories: Set<QuizCategory>,
    private val league: String?,
    private val difficulty: Difficulty,
    /** Question text in the language the screen was opened in. */
    private val strings: QuizStrings,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var loadingMore = false

    /** Survival: where the run stopped, so a second chance can pick it up again. */
    private var stoppedAt: QuizUiState.InProgress? = null
    private var stoppedAnswers: List<AnsweredQuestion> = emptyList()
    private var lastAttemptId: String? = null
    private var secondChanceUsed = false

    init {
        loadRound()
    }

    private fun loadRound() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            val questions = fetchQuestions()
            if (questions.isEmpty()) {
                _uiState.value = QuizUiState.Empty
                return@launch
            }
            _uiState.value = QuizUiState.InProgress(
                mode = mode,
                questions = questions,
                currentIndex = 0,
                score = 0,
                secondsPerQuestion = if (mode == QuizMode.DAILY) Difficulty.MEDIUM.secondsPerQuestion else difficulty.secondsPerQuestion,
            )
            startTimer()
        }
    }

    private suspend fun fetchQuestions(): List<QuizQuestion> = when (mode) {
        QuizMode.DAILY -> getQuizRoundUseCase(
            roundSize = DailyChallenge.ROUND_SIZE,
            categories = QuizCategory.entries.toSet(),
            difficulty = Difficulty.MEDIUM,
            strings = strings,
            random = DailyChallenge.random(today),
            deterministic = true,
        )
        QuizMode.SURVIVAL -> getQuizRoundUseCase(
            roundSize = SURVIVAL_BATCH,
            categories = categories,
            leagues = league?.let { setOf(it) },
            difficulty = difficulty,
            strings = strings,
        )
        QuizMode.STANDARD, QuizMode.DUEL -> getQuizRoundUseCase(
            roundSize = roundSize,
            categories = categories,
            leagues = league?.let { setOf(it) },
            difficulty = difficulty,
            strings = strings,
        )
    }

    /** [optionIndex] null = time ran out. */
    fun selectOption(optionIndex: Int?) {
        val state = _uiState.value as? QuizUiState.InProgress ?: return
        if (state.isAnswerRevealed || state.duelPhase == DuelPhase.HANDOFF) return
        timerJob?.cancel()

        val correct = state.currentQuestion.correctOptionIndex
        _uiState.value = when {
            state.mode == QuizMode.DUEL && state.duelPhase == DuelPhase.FIRST_PLAYER ->
                // Hide the outcome until the second player has answered too.
                state.copy(firstPlayerPick = optionIndex, duelPhase = DuelPhase.HANDOFF)
            state.mode == QuizMode.DUEL -> state.copy(
                selectedOptionIndex = optionIndex,
                isAnswerRevealed = true,
                score = state.score + if (state.firstPlayerPick == correct) 1 else 0,
                secondScore = state.secondScore + if (optionIndex == correct) 1 else 0,
            )
            else -> state.copy(
                selectedOptionIndex = optionIndex,
                isAnswerRevealed = true,
                score = state.score + if (optionIndex == correct) 1 else 0,
            )
        }
    }

    /** Duel: the second player has the phone and is ready. */
    fun startSecondTurn() {
        val state = _uiState.value as? QuizUiState.InProgress ?: return
        if (state.duelPhase != DuelPhase.HANDOFF) return
        _uiState.value = state.copy(duelPhase = DuelPhase.SECOND_PLAYER, timeRemainingSeconds = state.secondsPerQuestion)
        startTimer()
    }

    fun nextQuestion() {
        val state = _uiState.value as? QuizUiState.InProgress ?: return
        if (!state.isAnswerRevealed) return
        val answered = state.answered + if (state.mode == QuizMode.DUEL) {
            AnsweredQuestion(state.currentQuestion, state.firstPlayerPick, state.selectedOptionIndex)
        } else {
            AnsweredQuestion(state.currentQuestion, state.selectedOptionIndex)
        }

        if (state.endsRound || state.currentIndex + 1 >= state.questions.size) {
            finish(state, answered)
            return
        }
        _uiState.value = state.copy(
            currentIndex = state.currentIndex + 1,
            answered = answered,
            selectedOptionIndex = null,
            isAnswerRevealed = false,
            timeRemainingSeconds = state.secondsPerQuestion,
            duelPhase = DuelPhase.FIRST_PLAYER,
            firstPlayerPick = null,
        )
        if (state.mode == QuizMode.SURVIVAL) topUpSurvival()
        startTimer()
    }

    /** Survival has no end, so fetch another batch before the current one runs out. */
    private fun topUpSurvival() {
        val state = _uiState.value as? QuizUiState.InProgress ?: return
        if (loadingMore || state.questions.size - state.currentIndex > SURVIVAL_REFILL_AT) return
        loadingMore = true
        viewModelScope.launch {
            val seen = state.questions.map { it.prompt to it.options.toSet() }.toSet()
            val more = fetchQuestions().filterNot { (it.prompt to it.options.toSet()) in seen }
            (_uiState.value as? QuizUiState.InProgress)?.let { current ->
                _uiState.value = current.copy(questions = current.questions + more)
            }
            loadingMore = false
        }
    }

    private fun finish(state: QuizUiState.InProgress, answered: List<AnsweredQuestion>) {
        timerJob?.cancel()
        viewModelScope.launch {
            val score = answered.count { it.isCorrect }
            var finished = QuizUiState.Finished(
                mode = state.mode,
                answered = answered,
                score = score,
                total = answered.size,
                secondScore = if (state.mode == QuizMode.DUEL) answered.count { it.isSecondCorrect } else null,
            )
            when (state.mode) {
                QuizMode.DAILY -> {
                    val pattern = answered.joinToString("") { if (it.isCorrect) "1" else "0" }
                    val streak = progress.recordDaily(today, score, answered.size, pattern)
                    finished = finished.copy(dailyNumber = DailyChallenge.number(today), streak = streak)
                }
                QuizMode.SURVIVAL -> {
                    stoppedAt = state
                    stoppedAnswers = answered
                    finished = finished.copy(
                        survivalBest = progress.recordSurvival(score),
                        canContinue = !secondChanceUsed && state.currentIndex + 1 < state.questions.size,
                    )
                }
                QuizMode.STANDARD, QuizMode.DUEL -> Unit
            }
            // A duel mixes two players' answers, so it stays out of history and topic stats.
            if (state.mode != QuizMode.DUEL) record(state.mode, answered, score)
            _uiState.value = finished
        }
    }

    /** Survival second chance (after a rewarded ad, or free for ad-free players): resume after the miss. */
    fun continueSurvival() {
        val state = stoppedAt ?: return
        if (secondChanceUsed || state.currentIndex + 1 >= state.questions.size) return
        secondChanceUsed = true
        stoppedAt = null
        viewModelScope.launch {
            lastAttemptId?.let { quizAttemptDao.deleteWithAnswers(it) }
            lastAttemptId = null
            _uiState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                answered = stoppedAnswers,
                score = stoppedAnswers.count { it.isCorrect },
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                timeRemainingSeconds = state.secondsPerQuestion,
            )
            topUpSurvival()
            startTimer()
        }
    }

    private suspend fun record(mode: QuizMode, answered: List<AnsweredQuestion>, score: Int) {
        val attemptId = UUID.randomUUID().toString()
        lastAttemptId = attemptId
        quizAttemptDao.insertWithAnswers(
            QuizAttemptEntity(
                id = attemptId,
                completedAtMillis = System.currentTimeMillis(),
                score = score,
                total = answered.size,
                categories = answered.map { it.question.category.name }.distinct(),
                mode = mode.name,
            ),
            answered.map {
                AnswerRecordEntity(
                    attemptId = attemptId,
                    category = it.question.category.name,
                    correct = it.isCorrect,
                    prompt = it.question.prompt,
                    options = it.question.options,
                    correctOptionIndex = it.question.correctOptionIndex,
                    selectedOptionIndex = it.selectedOptionIndex,
                    explanation = it.question.explanation,
                    kit = it.question.kit?.encode(),
                )
            },
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val start = (_uiState.value as? QuizUiState.InProgress)?.secondsPerQuestion ?: return@launch
            for (remaining in start downTo 0) {
                val current = _uiState.value as? QuizUiState.InProgress ?: return@launch
                _uiState.value = current.copy(timeRemainingSeconds = remaining)
                if (remaining == 0) {
                    selectOption(null)
                    return@launch
                }
                delay(1_000)
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
    }

    private companion object {
        const val SURVIVAL_BATCH = 25
        const val SURVIVAL_REFILL_AT = 5
    }
}
