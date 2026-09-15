package com.ruflo.footballquiz.ui.clubs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruflo.footballquiz.data.local.dao.ClubDao
import com.ruflo.footballquiz.domain.mapper.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClubDetailViewModel(clubDao: ClubDao, clubId: String) : ViewModel() {

    private val _uiState = MutableStateFlow<ClubDetailUiState>(ClubDetailUiState.Loading)
    val uiState: StateFlow<ClubDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val club = clubDao.getById(clubId)
            _uiState.value = if (club == null) ClubDetailUiState.NotFound else ClubDetailUiState.Content(club.toDomain())
        }
    }
}
