package com.ruflo.footballquiz.domain.model

/** League names are free text in the data; this orders the known English tiers top-down. */
object Leagues {

    private val PYRAMID = listOf(
        "Premier League",
        "Championship",
        "League One",
        "League Two",
        "National League",
    )

    /** Known tiers first in pyramid order, then any unrecognized leagues alphabetically. */
    val pyramidOrder: Comparator<String> = compareBy<String> { league ->
        PYRAMID.indexOf(league).takeIf { it >= 0 } ?: PYRAMID.size
    }.thenBy { it }
}
