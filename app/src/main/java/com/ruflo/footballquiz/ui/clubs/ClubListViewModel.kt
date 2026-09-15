package com.ruflo.footballquiz.ui.clubs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruflo.footballquiz.data.local.dao.ClubDao
import com.ruflo.footballquiz.domain.mapper.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ClubListViewModel(private val clubDao: ClubDao) : ViewModel() {

    private val _uiState = MutableStateFlow<ClubListUiState>(ClubListUiState.Loading)
    val uiState: StateFlow<ClubListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            clubDao.observeAll()
                .map { clubs -> ClubListUiState.Content(clubs.map { it.toDomain() }) }
                .collect { _uiState.value = it }
        }
    }
}
