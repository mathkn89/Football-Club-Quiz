package com.makn.footballquiz.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makn.footballquiz.data.local.dao.QuizAttemptDao
import com.makn.footballquiz.data.local.entity.AnswerRecordEntity
import com.makn.footballquiz.domain.mapper.toDomain
import com.makn.footballquiz.domain.model.Kit
import com.makn.footballquiz.domain.model.QuizAttempt
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizQuestion
import com.makn.footballquiz.ui.quiz.AnsweredQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface AttemptDetailUiState {
    data object Loading : AttemptDetailUiState
    data object NotFound : AttemptDetailUiState

    data class Content(
        val attempt: QuizAttempt,
        /** Questions with stored details, in the order they were asked. */
        val answered: List<AnsweredQuestion>,
        /** True for rounds played before question details were saved. */
        val detailsMissing: Boolean,
    ) : AttemptDetailUiState {
        val mistakes: List<AnsweredQuestion> get() = answered.filterNot { it.isCorrect }
    }
}

class AttemptDetailViewModel(quizAttemptDao: QuizAttemptDao, attemptId: String) : ViewModel() {

    private val _uiState = MutableStateFlow<AttemptDetailUiState>(AttemptDetailUiState.Loading)
    val uiState: StateFlow<AttemptDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(quizAttemptDao.observeAttempt(attemptId), quizAttemptDao.observeAnswers(attemptId)) { attempt, records ->
                if (attempt == null) {
                    AttemptDetailUiState.NotFound
                } else {
                    val answered = records.mapNotNull { it.toAnsweredQuestion() }
                    AttemptDetailUiState.Content(
                        attempt = attempt.toDomain(),
                        answered = answered,
                        detailsMissing = answered.size < attempt.total,
                    )
                }
            }.collect { _uiState.value = it }
        }
    }
}

private fun AnswerRecordEntity.toAnsweredQuestion(): AnsweredQuestion? {
    val text = prompt ?: return null
    val correctIndex = correctOptionIndex?.takeIf { it in options.indices } ?: return null
    return AnsweredQuestion(
        question = QuizQuestion(
            id = id.toString(),
            prompt = text,
            options = options,
            correctOptionIndex = correctIndex,
            category = QuizCategory.fromRaw(category),
            explanation = explanation,
            kit = Kit.decode(kit),
        ),
        selectedOptionIndex = selectedOptionIndex,
    )
}
