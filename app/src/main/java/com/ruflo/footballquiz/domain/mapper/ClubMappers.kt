package com.ruflo.footballquiz.domain.mapper

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.generator.BadgeQuestionGenerator
import com.ruflo.footballquiz.domain.model.Club

fun ClubEntity.toDomain(): Club = Club(
    id = id,
    name = name,
    nickname = nickname,
    stadiumName = stadiumName,
    stadiumCapacity = stadiumCapacity,
    foundedYear = foundedYear,
    city = city,
    manager = manager,
    badgeImageRef = badgeDrawableName?.let { "${BadgeQuestionGenerator.DRAWABLE_SCHEME}$it" } ?: badgeRemoteUrl,
)
