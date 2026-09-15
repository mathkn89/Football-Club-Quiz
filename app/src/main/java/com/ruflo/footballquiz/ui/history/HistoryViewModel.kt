package com.ruflo.footballquiz.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruflo.footballquiz.data.local.UserProfilePreferences
import com.ruflo.footballquiz.data.local.dao.QuizAttemptDao
import com.ruflo.footballquiz.domain.mapper.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val quizAttemptDao: QuizAttemptDao,
    private val userProfilePreferences: UserProfilePreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { userProfilePreferences.getOrCreateCreatedAtMillis() }

        viewModelScope.launch {
            combine(
                quizAttemptDao.observeAll(),
                userProfilePreferences.displayNameFlow,
            ) { attempts, displayName ->
                HistoryUiState.Content(displayName = displayName, attempts = attempts.map { it.toDomain() })
            }.collect { _uiState.value = it }
        }
    }

    fun onRenameConfirmed(newName: String) {
        viewModelScope.launch { userProfilePreferences.setDisplayName(newName) }
    }
}
