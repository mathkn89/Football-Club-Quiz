package com.ruflo.footballquiz.ui.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Builds a [ViewModel] from a no-arg [creator] lambda, for manual (non-Hilt) DI. */
class ViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
