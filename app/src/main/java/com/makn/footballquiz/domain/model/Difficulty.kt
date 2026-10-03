package com.makn.footballquiz.domain.model

/**
 * How hard a round is: how long each question runs, which clubs are asked about, and how close
 * the wrong answers sit to the right one.
 */
enum class Difficulty(val secondsPerQuestion: Int) {
    /** Top two tiers only; wrong answers far apart (e.g. founding years decades away). */
    EASY(20),

    /** Every club; wrong answers moderately close. */
    MEDIUM(15),

    /** Every club, wrong answers drawn from the same league and very close (years ±4). */
    HARD(10),
}
