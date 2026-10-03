package com.makn.footballquiz.ui.clubs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makn.footballquiz.data.local.dao.ClubDao
import com.makn.footballquiz.domain.mapper.toDomain
import com.makn.footballquiz.domain.model.Club
import com.makn.footballquiz.domain.model.Leagues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ClubListViewModel(private val clubDao: ClubDao) : ViewModel() {

    private val _uiState = MutableStateFlow<ClubListUiState>(ClubListUiState.Loading)
    val uiState: StateFlow<ClubListUiState> = _uiState.asStateFlow()

    private val _selectedLeague = MutableStateFlow<String?>(null)
    private val _query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(clubDao.observeAll(), _selectedLeague, _query) { entities, selectedLeague, query ->
                val clubs = entities.map { it.toDomain() }
                val sections = clubs
                    .filter { (selectedLeague == null || it.league == selectedLeague) && it.matches(query) }
                    .groupBy { it.league }
                    .toSortedMap(Leagues.pyramidOrder)
                    .map { (league, leagueClubs) -> LeagueSection(league, leagueClubs.sortedBy { it.shortName }) }
                ClubListUiState.Content(
                    sections = sections,
                    availableLeagues = clubs.map { it.league }.distinct().sortedWith(Leagues.pyramidOrder),
                    selectedLeague = selectedLeague,
                    query = query,
                )
            }.collect { _uiState.value = it }
        }
    }

    /** Null selects "all leagues". */
    fun onLeagueSelected(league: String?) {
        _selectedLeague.value = league
    }

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    private fun Club.matches(query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        return listOf(name, nickname, city, stadiumName).any { it.contains(q, ignoreCase = true) }
    }
}
