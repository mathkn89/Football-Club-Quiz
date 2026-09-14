package com.ruflo.footballquiz.data.repository

sealed interface SyncResult {
    data object UpToDate : SyncResult
    data class Updated(val appliedDeltas: Int, val newVersion: Int) : SyncResult
    data class Error(val throwable: Throwable) : SyncResult
}
