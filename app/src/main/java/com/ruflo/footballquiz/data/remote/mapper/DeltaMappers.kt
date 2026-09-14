package com.ruflo.footballquiz.data.remote.mapper

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.data.local.entity.CustomQuestionEntity
import com.ruflo.footballquiz.data.remote.dto.ClubDeltaDto
import com.ruflo.footballquiz.data.remote.dto.CustomQuestionDeltaDto

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
)
