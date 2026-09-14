package com.ruflo.footballquiz.data.repository

import com.ruflo.footballquiz.data.local.SyncPreferences
import com.ruflo.footballquiz.data.local.dao.ClubDao
import com.ruflo.footballquiz.data.local.dao.CustomQuestionDao
import com.ruflo.footballquiz.data.remote.SyncApiService
import com.ruflo.footballquiz.data.remote.dto.DeltaResponseDto
import com.ruflo.footballquiz.data.remote.mapper.toEntity

/**
 * Polls `version.json`, then walks `deltas_v{N}.json` one version at a time from the last
 * locally-applied version up to latest, upserting each delta as it lands so a failure mid-walk
 * resumes cleanly on the next run instead of re-downloading everything already applied.
 */
class SyncRepository(
    private val api: SyncApiService,
    private val clubDao: ClubDao,
    private val customQuestionDao: CustomQuestionDao,
    private val syncPreferences: SyncPreferences,
) {

    suspend fun sync(): SyncResult = try {
        val remoteVersion = api.getVersion().latestVersion
        var localVersion = syncPreferences.getLastSyncedVersion()

        if (remoteVersion <= localVersion) {
            SyncResult.UpToDate
        } else {
            var appliedDeltas = 0
            while (localVersion < remoteVersion) {
                val nextVersion = localVersion + 1
                applyDelta(api.getDelta(nextVersion))
                syncPreferences.setLastSyncedVersion(nextVersion)
                localVersion = nextVersion
                appliedDeltas++
            }
            SyncResult.Updated(appliedDeltas = appliedDeltas, newVersion = localVersion)
        }
    } catch (t: Throwable) {
        SyncResult.Error(t)
    }

    private suspend fun applyDelta(delta: DeltaResponseDto) {
        if (delta.deletedClubIds.isNotEmpty()) {
            clubDao.deleteByIds(delta.deletedClubIds)
        }
        if (delta.deletedQuestionIds.isNotEmpty()) {
            customQuestionDao.deleteByIds(delta.deletedQuestionIds)
        }
        if (delta.clubs.isNotEmpty()) {
            clubDao.upsertAll(delta.clubs.map { it.toEntity() })
        }
        if (delta.customQuestions.isNotEmpty()) {
            customQuestionDao.upsertAll(delta.customQuestions.map { it.toEntity() })
        }
    }
}
