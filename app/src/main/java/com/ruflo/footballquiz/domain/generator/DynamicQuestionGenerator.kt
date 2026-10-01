package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion

/** Builds one [QuizQuestion] about [target], drawing wrong-answer options from [distractorPool]. */
interface DynamicQuestionGenerator {

    val category: QuizCategory

    /** Null when [distractorPool] can't supply enough distinct wrong answers for [target]. */
    fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion?
}

/**
 * Builds a 4-option list from [correct] plus 3 distinct values from [candidates], shuffled.
 * Null when [correct] is blank (missing data) or there aren't 3 usable distractors.
 */
internal fun buildOptions(correct: String, candidates: List<String>): List<String>? {
    if (correct.isBlank()) return null
    val distractors = candidates.filter { it.isNotBlank() && it != correct }.distinct().shuffled().take(3)
    if (distractors.size < 3) return null
    return (distractors + correct).shuffled()
}

/** Words too common in club/ground names to count as giving the answer away. */
private val GENERIC_WORDS = setOf(
    "the", "city", "town", "united", "athletic", "rovers", "wanderers", "county", "albion", "football",
    "club", "stadium", "park", "road", "lane", "ground", "community", "arena", "international",
)

private fun distinctiveWords(text: String): Set<String> =
    text.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length >= 3 && it !in GENERIC_WORDS }.toSet()

/**
 * True when [answer] shares a distinctive word with the club's name — e.g. "Brentford Community
 * Stadium" for Brentford, or "Wigan" as the home town of Wigan Athletic — so the question would
 * answer itself.
 */
internal fun ClubEntity.giveaway(answer: String): Boolean =
    distinctiveWords(answer).any { it in distinctiveWords(shortName) || it in distinctiveWords(name) }

/** Punctuation/case-insensitive key, so "St James' Park" and "St James Park" count as the same name. */
internal fun nameKey(name: String): String = name.lowercase().filter { it.isLetterOrDigit() }

/** Case/article-insensitive key so "The U's" and "U's" count as the same nickname. */
internal fun nicknameKey(nickname: String): String =
    nickname.trim().lowercase().removePrefix("the ").replace('’', '\'')

/**
 * Club-name options: [target]'s short name plus 3 from [candidates] (excluding [target]), shuffled.
 * Null when there aren't 3 distinct names to pick from.
 */
internal fun buildClubOptions(target: ClubEntity, candidates: List<ClubEntity>): List<String>? =
    buildOptions(target.shortName, candidates.filter { it.id != target.id }.map { it.shortName })
