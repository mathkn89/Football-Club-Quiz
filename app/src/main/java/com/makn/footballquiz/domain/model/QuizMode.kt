package com.makn.footballquiz.domain.model

enum class QuizMode {
    /** A fixed-length round with the player's own options. */
    STANDARD,

    /** Today's 10 questions — the same for every player — once per day, with a streak. */
    DAILY,

    /** Questions keep coming until the first wrong answer (or time running out). */
    SURVIVAL,

    /** Two players take turns answering the same questions on one phone. */
    DUEL,
}
