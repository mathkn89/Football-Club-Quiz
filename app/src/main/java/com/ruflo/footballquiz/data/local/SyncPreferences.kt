package com.ruflo.footballquiz.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sync_prefs")

/** Tracks the last locally-applied delta version, so [com.ruflo.footballquiz.data.repository.SyncRepository] knows where to resume. */
class SyncPreferences(private val context: Context) {

    private val lastSyncedVersionKey = intPreferencesKey("last_synced_version")

    val lastSyncedVersionFlow: Flow<Int> =
        context.dataStore.data.map { it[lastSyncedVersionKey] ?: 0 }

    suspend fun getLastSyncedVersion(): Int = lastSyncedVersionFlow.first()

    suspend fun setLastSyncedVersion(version: Int) {
        context.dataStore.edit { it[lastSyncedVersionKey] = version }
    }
}
