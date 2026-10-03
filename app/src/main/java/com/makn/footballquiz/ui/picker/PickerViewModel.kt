package com.makn.footballquiz.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makn.footballquiz.data.local.dao.ClubDao
import com.makn.footballquiz.data.local.dao.CustomQuestionDao
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.Leagues
import com.makn.footballquiz.domain.model.QuizCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PickerViewModel(
    private val clubDao: ClubDao,
    private val customQuestionDao: CustomQuestionDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PickerUiState())
    val uiState: StateFlow<PickerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                clubDao.observeAll(),
                customQuestionDao.observeDistinctCategories(),
            ) { clubs, customCategories ->
                val custom = customCategories.map(QuizCategory::fromRaw).toSet()
                Triple(
                    clubs.map { it.league }.distinct().sortedWith(Leagues.pyramidOrder),
                    QuizCategory.entries.filter { it in QuizCategory.DYNAMIC || it in custom },
                    clubs.size,
                )
            }.collect { (leagues, categories, clubCount) ->
                _uiState.update { state ->
                    state.copy(
                        availableLeagues = leagues,
                        availableCategories = categories,
                        clubCount = clubCount,
                        selectedLeague = state.selectedLeague?.takeIf { it in leagues },
                    )
                }
            }
        }
    }

    fun onRoundSizeSelected(size: Int) {
        _uiState.update { it.copy(roundSize = size) }
    }

    fun onDifficultySelected(difficulty: Difficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    /** Can leave nothing selected — the screen then disables Play and says why. */
    fun onCategoryToggled(category: QuizCategory) {
        _uiState.update { state ->
            val selected = state.selectedCategories
            state.copy(selectedCategories = if (category in selected) selected - category else selected + category)
        }
    }

    fun onSelectAllCategories() {
        _uiState.update { it.copy(selectedCategories = QuizCategory.entries.toSet()) }
    }

    fun onClearCategories() {
        _uiState.update { it.copy(selectedCategories = emptySet()) }
    }

    /** Null selects "all leagues". */
    fun onLeagueSelected(league: String?) {
        _uiState.update { it.copy(selectedLeague = league) }
    }
}
