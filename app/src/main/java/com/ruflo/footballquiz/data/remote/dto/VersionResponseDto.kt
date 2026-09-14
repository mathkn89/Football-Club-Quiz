package com.ruflo.footballquiz.data.remote.dto

import kotlinx.serialization.Serializable

/** `version.json` — sync metadata the client polls before deciding whether/what to download. */
@Serializable
data class VersionResponseDto(
    val latestVersion: Int,
    val minSupportedVersion: Int = 1,
    val updatedAt: String,
)
