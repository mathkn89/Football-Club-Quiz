package com.ruflo.footballquiz.data.remote

import com.ruflo.footballquiz.data.remote.dto.DeltaResponseDto
import com.ruflo.footballquiz.data.remote.dto.VersionResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

/** Static JSON endpoints served from GitHub Pages, rooted at `BuildConfig.DATA_SYNC_BASE_URL`. */
interface SyncApiService {

    @GET("version.json")
    suspend fun getVersion(): VersionResponseDto

    @GET("deltas_v{version}.json")
    suspend fun getDelta(@Path("version") version: Int): DeltaResponseDto
}
