package com.makn.footballquiz.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makn.footballquiz.data.local.PlayProgressPreferences
import com.makn.footballquiz.data.local.UserProfilePreferences
import com.makn.footballquiz.data.local.dao.QuizAttemptDao
import com.makn.footballquiz.domain.mapper.toDomain
import com.makn.footballquiz.domain.model.QuizCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val quizAttemptDao: QuizAttemptDao,
    private val userProfilePreferences: UserProfilePreferences,
    private val progress: PlayProgressPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { userProfilePreferences.getOrCreateCreatedAtMillis() }

        viewModelScope.launch {
            combine(
                quizAttemptDao.observeAll(),
                userProfilePreferences.displayNameFlow,
                quizAttemptDao.observeCategoryStats(),
                progress.progress,
            ) { attempts, displayName, statRows, saved ->
                val stats = statRows
                    .map { TopicStat(QuizCategory.fromRaw(it.category), it.answered, it.correct) }
                    .sortedByDescending { it.percent }
                HistoryUiState.Content(
                    displayName = displayName,
                    attempts = attempts.map { it.toDomain() },
                    topicStats = stats,
                    weakTopics = HistoryUiState.weakTopics(stats),
                    remindersEnabled = saved.remindersEnabled,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onRenameConfirmed(newName: String) {
        viewModelScope.launch { userProfilePreferences.setDisplayName(newName) }
    }

    fun onRemindersChanged(enabled: Boolean) {
        viewModelScope.launch { progress.setRemindersEnabled(enabled) }
    }
}
