package com.ruflo.footballquiz.domain.model

enum class QuizCategory {
    STADIUM,
    NICKNAME,
    FOUNDED_YEAR,
    BADGE,
    LEAGUE,
    LOCATION,
    MANAGER,
    HISTORY,
    TRANSFERS,
    RIVALRIES,
    GENERAL,
    ;

    companion object {
        /** Categories generated from club data — always playable once clubs exist. */
        val DYNAMIC: Set<QuizCategory> = setOf(STADIUM, NICKNAME, FOUNDED_YEAR, BADGE, LEAGUE, LOCATION, MANAGER)

        /** Maps a free-text [CustomQuestionEntity.category] value onto a known category. */
        fun fromRaw(raw: String): QuizCategory =
            entries.find { it.name.equals(raw.trim(), ignoreCase = true) } ?: GENERAL
    }
}
