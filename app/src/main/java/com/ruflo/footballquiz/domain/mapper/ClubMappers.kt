package com.ruflo.footballquiz.domain.mapper

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.Club
import com.ruflo.footballquiz.domain.model.Kit

fun ClubEntity.toDomain(): Club = Club(
    id = id,
    name = name,
    shortName = shortName,
    nickname = nickname,
    stadiumName = stadiumName,
    stadiumCapacity = stadiumCapacity,
    foundedYear = foundedYear,
    city = city,
    manager = manager,
    league = league,
    kit = kit(),
)

fun ClubEntity.kit(): Kit? = Kit.from(kitPattern, kitPrimary, kitSecondary, kitShorts)
