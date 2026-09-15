package com.ruflo.footballquiz.domain.model

data class Club(
    val id: String,
    val name: String,
    val nickname: String,
    val stadiumName: String,
    val stadiumCapacity: Int,
    val foundedYear: Int,
    val city: String,
    val manager: String,
    val badgeImageRef: String?,
)
