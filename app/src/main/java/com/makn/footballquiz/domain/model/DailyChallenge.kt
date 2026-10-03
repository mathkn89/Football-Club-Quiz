package com.makn.footballquiz.domain.model

import java.time.LocalDate
import kotlin.random.Random

/** Day-based numbering and seeding for the daily challenge ("Daily #12"). */
object DailyChallenge {

    const val ROUND_SIZE = 10

    /** Daily #1. */
    private val FIRST_DAY: LocalDate = LocalDate.of(2026, 10, 1)

    fun number(day: LocalDate): Int = (day.toEpochDay() - FIRST_DAY.toEpochDay() + 1).toInt()

    /** The same seed on every device for the same calendar day. */
    fun random(day: LocalDate): Random = Random(day.toEpochDay() * 7_919 + 104_729)
}
