package com.ruflo.footballquiz.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruflo.footballquiz.data.local.dao.ClubDao
import com.ruflo.footballquiz.data.local.dao.CustomQuestionDao
import com.ruflo.footballquiz.domain.model.Leagues
import com.ruflo.footballquiz.domain.model.QuizCategory
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

    /** No-op if [category] is the last visible one selected — a round needs at least one category. */
    fun onCategoryToggled(category: QuizCategory) {
        _uiState.update { state ->
            val updated = when {
                category !in state.selectedCategories -> state.selectedCategories + category
                state.activeCategories.size > 1 -> state.selectedCategories - category
                else -> state.selectedCategories
            }
            state.copy(selectedCategories = updated)
        }
    }

    /** Null selects "all leagues". */
    fun onLeagueSelected(league: String?) {
        _uiState.update { it.copy(selectedLeague = league) }
    }
}
