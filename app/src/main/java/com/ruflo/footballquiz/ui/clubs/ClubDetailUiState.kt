package com.ruflo.footballquiz.ui.clubs

import com.ruflo.footballquiz.domain.model.Club

sealed interface ClubDetailUiState {
    data object Loading : ClubDetailUiState
    data object NotFound : ClubDetailUiState
    data class Content(val club: Club) : ClubDetailUiState
}
