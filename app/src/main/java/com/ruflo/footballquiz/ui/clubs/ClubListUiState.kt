package com.ruflo.footballquiz.ui.clubs

import com.ruflo.footballquiz.domain.model.Club

sealed interface ClubListUiState {
    data object Loading : ClubListUiState

    data class Content(
        val allClubs: List<Club>,
        val availableLeagues: List<String>,
        /** Null means "all leagues" — no filter. */
        val selectedLeague: String?,
    ) : ClubListUiState {
        val clubs: List<Club> get() = if (selectedLeague == null) allClubs else allClubs.filter { it.league == selectedLeague }
    }
}
