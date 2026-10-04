package com.makn.footballquiz.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makn.footballquiz.data.local.PlayProgressPreferences
import com.makn.footballquiz.monetization.BillingManager
import com.makn.footballquiz.monetization.ConsentManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val purchase: BillingManager.State = BillingManager.State(),
    val remindersEnabled: Boolean = false,
    /** Only shown where privacy law requires a way back into the consent form. */
    val privacyOptionsRequired: Boolean = false,
)

class SettingsViewModel(
    val billing: BillingManager,
    val consent: ConsentManager,
    private val progress: PlayProgressPreferences,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        billing.state,
        progress.progress,
        consent.privacyOptionsRequired,
    ) { purchase, saved, privacyRequired ->
        SettingsUiState(purchase, saved.remindersEnabled, privacyRequired)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onRemindersChanged(enabled: Boolean) {
        viewModelScope.launch { progress.setRemindersEnabled(enabled) }
    }
}
