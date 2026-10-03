package com.makn.footballquiz.ui.clubs

import com.makn.footballquiz.domain.model.Club

sealed interface ClubDetailUiState {
    data object Loading : ClubDetailUiState
    data object NotFound : ClubDetailUiState
    data class Content(val club: Club) : ClubDetailUiState
}
