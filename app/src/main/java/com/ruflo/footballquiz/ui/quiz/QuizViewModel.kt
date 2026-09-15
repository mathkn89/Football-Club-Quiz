package com.ruflo.footballquiz.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.usecase.GetQuizRoundUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val getQuizRoundUseCase: GetQuizRoundUseCase,
    private val roundSize: Int,
    private val categories: Set<QuizCategory>,
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadRound()
    }

    private fun loadRound() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            val questions = getQuizRoundUseCase(roundSize = roundSize, categories = categories)
            _uiState.value = if (questions.isEmpty()) {
                QuizUiState.Empty
            } else {
                QuizUiState.InProgress(questions = questions, currentIndex = 0, score = 0)
            }
            if (questions.isNotEmpty()) startTimer()
        }
    }

    fun selectOption(optionIndex: Int) {
        val state = _uiState.value as? QuizUiState.InProgress ?: return
        if (state.isAnswerRevealed) return
        timerJob?.cancel()

        val isCorrect = optionIndex == state.currentQuestion.correctOptionIndex
        _uiState.value = state.copy(
            selectedOptionIndex = optionIndex,
            isAnswerRevealed = true,
            score = if (isCorrect) state.score + 1 else state.score,
        )
    }

    fun nextQuestion() {
        val state = _uiState.value as? QuizUiState.InProgress ?: return
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.totalQuestions) {
            _uiState.value = QuizUiState.Finished(score = state.score, total = state.totalQuestions)
            return
        }
        _uiState.value = state.copy(
            currentIndex = nextIndex,
            selectedOptionIndex = null,
            isAnswerRevealed = false,
            timeRemainingSeconds = QUESTION_TIME_SECONDS,
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (remaining in QUESTION_TIME_SECONDS downTo 0) {
                val current = _uiState.value as? QuizUiState.InProgress ?: return@launch
                _uiState.value = current.copy(timeRemainingSeconds = remaining)
                if (remaining == 0) {
                    if (!current.isAnswerRevealed) {
                        _uiState.value = current.copy(isAnswerRevealed = true, timeRemainingSeconds = 0)
                    }
                    return@launch
                }
                delay(1_000)
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
    }
}
