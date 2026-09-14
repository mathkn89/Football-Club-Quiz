package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import java.util.UUID

class FoundedYearQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.FOUNDED_YEAR

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        val correct = target.foundedYear.toString()
        val options = buildOptions(correct, distractorPool.map { it.foundedYear.toString() }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "In what year was ${target.name} founded?",
            options = options,
            correctOptionIndex = options.indexOf(correct),
            category = QuizCategory.FOUNDED_YEAR,
            explanation = "${target.name} was founded in ${target.foundedYear}.",
        )
    }
}
