package com.ruflo.footballquiz.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruflo.footballquiz.data.local.dao.ClubDao
import com.ruflo.footballquiz.domain.model.QuizCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PickerViewModel(private val clubDao: ClubDao) : ViewModel() {

    private val _uiState = MutableStateFlow(PickerUiState())
    val uiState: StateFlow<PickerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val leagues = clubDao.getDistinctLeagues()
            _uiState.update { it.copy(availableLeagues = leagues) }
        }
    }

    fun onRoundSizeSelected(size: Int) {
        _uiState.update { it.copy(roundSize = size) }
    }

    /** No-op if [category] is the last one selected — a round needs at least one category. */
    fun onCategoryToggled(category: QuizCategory) {
        _uiState.update { state ->
            val selected = state.selectedCategories
            val updated = when {
                category in selected && selected.size > 1 -> selected - category
                category in selected -> selected
                else -> selected + category
            }
            state.copy(selectedCategories = updated)
        }
    }

    /** Null selects "all leagues". */
    fun onLeagueSelected(league: String?) {
        _uiState.update { it.copy(selectedLeague = league) }
    }
}
