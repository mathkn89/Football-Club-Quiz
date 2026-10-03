package com.makn.footballquiz.domain.mapper

import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.domain.model.Club
import com.makn.footballquiz.domain.model.Kit

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
