package com.ruflo.footballquiz.ui.clubs

import com.ruflo.footballquiz.domain.model.Club

sealed interface ClubListUiState {
    data object Loading : ClubListUiState
    data class Content(val clubs: List<Club>) : ClubListUiState
}
