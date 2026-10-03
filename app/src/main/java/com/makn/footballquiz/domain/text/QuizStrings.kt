package com.makn.footballquiz.domain.text

import com.makn.footballquiz.domain.model.Kit
import com.makn.footballquiz.domain.model.KitColour
import com.makn.footballquiz.domain.model.KitPattern

/** Every piece of text the quiz engine shows to the player; implemented per language by the app. */
enum class QuizText {
    STADIUM_PROMPT, STADIUM_EXPLANATION,
    STADIUM_CLUB_PROMPT, STADIUM_CLUB_EXPLANATION,
    CAPACITY_PROMPT, CAPACITY_EXPLANATION,
    NICKNAME_PROMPT, NICKNAME_EXPLANATION,
    NICKNAME_CLUB_PROMPT, NICKNAME_CLUB_EXPLANATION,
    FOUNDED_PROMPT, FOUNDED_EXPLANATION,
    OLDEST_PROMPT,
    LEAGUE_PROMPT, LEAGUE_EXPLANATION,
    CITY_PROMPT, CITY_EXPLANATION,
    MANAGER_PROMPT, MANAGER_EXPLANATION,
    KIT_PROMPT, KIT_EXPLANATION,

    /**
     * Kit descriptions. Args: 1 = main colour, 2 = second colour, 3 = main colour plural form,
     * 4 = second colour plural form — each language's template picks the forms its grammar needs.
     */
    KIT_PLAIN, KIT_SLEEVES, KIT_STRIPES, KIT_HOOPS, KIT_HALVES, KIT_QUARTERS,
}

interface QuizStrings {
    /** ISO 639-1 code of the language the text comes out in, e.g. "en", "nb". */
    val languageCode: String

    fun text(key: QuizText, vararg args: Any): String

    /** Colour name; [plural] gives the form used before a plural noun ("white sleeves"). */
    fun colour(colour: KitColour, plural: Boolean = false): String

    /** Whether "<colour> shorts" uses the plural colour form in this language. */
    val shortsUsePluralColour: Boolean

    fun number(value: Int): String
}

/** "Red with white sleeves", "Rot mit Ärmeln in Weiß", "Rød med hvite ermer". */
fun QuizStrings.kitDescription(kit: Kit): String {
    val key = when (kit.pattern) {
        KitPattern.PLAIN -> QuizText.KIT_PLAIN
        KitPattern.SLEEVES -> QuizText.KIT_SLEEVES
        KitPattern.STRIPES -> QuizText.KIT_STRIPES
        KitPattern.HOOPS -> QuizText.KIT_HOOPS
        KitPattern.HALVES -> QuizText.KIT_HALVES
        KitPattern.QUARTERS -> QuizText.KIT_QUARTERS
    }
    val second = kit.secondary ?: kit.primary
    return text(key, colour(kit.primary), colour(second), colour(kit.primary, true), colour(second, true))
        .replaceFirstChar { it.uppercase() }
}

fun QuizStrings.shortsColour(kit: Kit): String = colour(kit.shorts, plural = shortsUsePluralColour)

/** English puts "the" before most league names ("the Premier League", but "League One"). */
fun QuizStrings.leagueInSentence(league: String): String =
    if (languageCode == "en" && !league.startsWith("League ")) "the $league" else league
