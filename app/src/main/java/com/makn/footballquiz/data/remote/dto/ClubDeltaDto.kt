package com.makn.footballquiz.data.remote.dto

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
    val league: String,
    val badgeQuizUrl: String? = null,
    val kitPattern: String? = null,
    val kitPrimary: String? = null,
    val kitSecondary: String? = null,
    val kitShorts: String? = null,
)
