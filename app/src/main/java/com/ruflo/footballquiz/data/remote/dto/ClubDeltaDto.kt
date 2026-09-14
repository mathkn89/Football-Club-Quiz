package com.ruflo.footballquiz.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClubDeltaDto(
    val id: String,
    val name: String,
    val shortName: String,
    val nickname: String,
    val stadiumName: String,
    val stadiumCapacity: Int,
    val foundedYear: Int,
    val city: String,
    val badgeDrawableName: String? = null,
    val badgeRemoteUrl: String? = null,
    val version: Int,
    val manager: String,
)
