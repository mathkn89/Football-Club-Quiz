package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import java.util.UUID

class StadiumQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.STADIUM

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        val options = buildOptions(target.stadiumName, distractorPool.map { it.stadiumName }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "What is the name of ${target.name}'s home stadium?",
            options = options,
            correctOptionIndex = options.indexOf(target.stadiumName),
            category = QuizCategory.STADIUM,
            explanation = "${target.name} play their home games at ${target.stadiumName} in ${target.city}.",
        )
    }
}
