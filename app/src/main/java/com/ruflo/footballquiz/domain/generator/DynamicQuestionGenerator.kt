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

/** Builds a 4-option list from [correct] plus up to 3 distinct values from [candidates], shuffled. */
internal fun buildOptions(correct: String, candidates: List<String>): List<String>? {
    val distractors = candidates.filter { it != correct }.distinct().shuffled().take(3)
    if (distractors.size < 3) return null
    return (distractors + correct).shuffled()
}
