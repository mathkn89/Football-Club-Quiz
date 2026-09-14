package com.ruflo.footballquiz.data.remote.dto

import kotlinx.serialization.Serializable

/** Payload of a single `deltas_v{N}.json` — records changed/added/removed to reach version [version]. */
@Serializable
data class DeltaResponseDto(
    val version: Int,
    val clubs: List<ClubDeltaDto> = emptyList(),
    val customQuestions: List<CustomQuestionDeltaDto> = emptyList(),
    val deletedClubIds: List<String> = emptyList(),
    val deletedQuestionIds: List<String> = emptyList(),
)
