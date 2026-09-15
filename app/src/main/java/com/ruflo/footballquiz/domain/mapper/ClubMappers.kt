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
    league = league,
    // No res/drawable/badge_*.xml files are bundled yet, so a badgeDrawableName never resolves —
    // prefer the network URL until real vector art ships.
    badgeImageRef = badgeRemoteUrl ?: badgeDrawableName?.let { "${BadgeQuestionGenerator.DRAWABLE_SCHEME}$it" },
)
