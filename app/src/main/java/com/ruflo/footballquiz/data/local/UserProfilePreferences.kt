package com.ruflo.footballquiz.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_profile")

/** Local, device-only profile — no login/backend. */
class UserProfilePreferences(private val context: Context) {

    private val displayNameKey = stringPreferencesKey("display_name")
    private val createdAtMillisKey = longPreferencesKey("created_at_millis")

    val displayNameFlow: Flow<String> =
        context.dataStore.data.map { it[displayNameKey] ?: DEFAULT_DISPLAY_NAME }

    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { it[displayNameKey] = name.ifBlank { DEFAULT_DISPLAY_NAME } }
    }

    /** First read creates the profile by stamping the current time; later reads are stable. */
    suspend fun getOrCreateCreatedAtMillis(): Long {
        val existing = context.dataStore.data.map { it[createdAtMillisKey] }.first()
        if (existing != null) return existing

        val now = System.currentTimeMillis()
        context.dataStore.edit { it[createdAtMillisKey] = now }
        return now
    }

    private companion object {
        const val DEFAULT_DISPLAY_NAME = "Player"
    }
}
