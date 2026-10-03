package com.makn.footballquiz.data.remote.mapper

import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.data.local.entity.CustomQuestionEntity
import com.makn.footballquiz.data.remote.dto.ClubDeltaDto
import com.makn.footballquiz.data.remote.dto.CustomQuestionDeltaDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun ClubDeltaDto.toEntity(): ClubEntity = ClubEntity(
    id = id,
    name = name,
    shortName = shortName,
    nickname = nickname,
    stadiumName = stadiumName,
    stadiumCapacity = stadiumCapacity,
    foundedYear = foundedYear,
    city = city,
    badgeDrawableName = badgeDrawableName,
    badgeRemoteUrl = badgeRemoteUrl,
    version = version,
    manager = manager,
    league = league,
    badgeQuizUrl = badgeQuizUrl,
    kitPattern = kitPattern,
    kitPrimary = kitPrimary,
    kitSecondary = kitSecondary,
    kitShorts = kitShorts,
)

fun CustomQuestionDeltaDto.toEntity(): CustomQuestionEntity = CustomQuestionEntity(
    id = id,
    questionText = questionText,
    category = category,
    correctAnswer = correctAnswer,
    wrongAnswers = wrongAnswers,
    explanation = explanation,
    imageUriOrUrl = imageUriOrUrl,
    version = version,
    translations = translations.takeIf { it.isNotEmpty() }?.let { translationJson.encodeToString(it) },
)

internal val translationJson = Json { ignoreUnknownKeys = true }
