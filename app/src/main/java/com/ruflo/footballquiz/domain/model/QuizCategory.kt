package com.ruflo.footballquiz.domain.model

enum class QuizCategory {
    STADIUM,
    NICKNAME,
    FOUNDED_YEAR,
    BADGE,
    HISTORY,
    TRANSFERS,
    RIVALRIES,
    GENERAL,
    ;

    companion object {
        /** Maps a free-text [CustomQuestionEntity.category] value onto a known category. */
        fun fromRaw(raw: String): QuizCategory =
            entries.find { it.name.equals(raw.trim(), ignoreCase = true) } ?: GENERAL
    }
}
