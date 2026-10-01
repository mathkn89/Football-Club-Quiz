package com.ruflo.footballquiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clubs")
data class ClubEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val shortName: String,
    val nickname: String,
    val stadiumName: String,
    val stadiumCapacity: Int,
    val foundedYear: Int,
    val city: String,
    val badgeDrawableName: String?,
    val badgeRemoteUrl: String?,
    val version: Int,
    val manager: String,
    val league: String,
    /** Badge with its lettering painted out, for badge questions. Null until one is published. */
    val badgeQuizUrl: String? = null,
)
