package com.ruflo.footballquiz.ui.clubs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruflo.footballquiz.data.local.dao.ClubDao
import com.ruflo.footballquiz.domain.mapper.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ClubListViewModel(private val clubDao: ClubDao) : ViewModel() {

    private val _uiState = MutableStateFlow<ClubListUiState>(ClubListUiState.Loading)
    val uiState: StateFlow<ClubListUiState> = _uiState.asStateFlow()

    private val _selectedLeague = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            combine(clubDao.observeAll(), _selectedLeague) { entities, selectedLeague ->
                val clubs = entities.map { it.toDomain() }
                ClubListUiState.Content(
                    allClubs = clubs,
                    availableLeagues = clubs.map { it.league }.distinct().sorted(),
                    selectedLeague = selectedLeague,
                )
            }.collect { _uiState.value = it }
        }
    }

    /** Null selects "all leagues". */
    fun onLeagueSelected(league: String?) {
        _selectedLeague.value = league
    }
}
