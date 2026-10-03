package com.makn.footballquiz.ui.clubs

import com.makn.footballquiz.domain.model.Club

sealed interface ClubListUiState {
    data object Loading : ClubListUiState

    data class Content(
        /** Clubs matching the current filter + query, grouped by league in pyramid order. */
        val sections: List<LeagueSection>,
        val availableLeagues: List<String>,
        /** Null means "all leagues" — no filter. */
        val selectedLeague: String?,
        val query: String,
    ) : ClubListUiState {
        val isEmpty: Boolean get() = sections.isEmpty()
    }
}

data class LeagueSection(val league: String, val clubs: List<Club>)
