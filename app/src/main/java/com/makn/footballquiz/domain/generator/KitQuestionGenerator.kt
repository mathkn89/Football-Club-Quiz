package com.makn.footballquiz.domain.generator

import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.domain.mapper.kit
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizQuestion
import com.makn.footballquiz.domain.text.QuizStrings
import com.makn.footballquiz.domain.text.QuizText
import com.makn.footballquiz.domain.text.kitDescription
import com.makn.footballquiz.domain.text.shortsColour
import java.util.UUID

/**
 * "Whose home kit is this?" with the kit drawn from plain colours. No other option shares any
 * shirt colour family with the answer — if it's Arsenal (red and white), no option plays in red
 * or white — and the wrong options differ from each other too, so the kit identifies one club.
 */
class KitQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.KIT

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>, difficulty: Difficulty): QuizQuestion? {
        val kit = target.kit() ?: return null
        val distractors = distractorPool
            .filter { it.id != target.id }
            .mapNotNull { club -> club.kit()?.let { club to it } }
            .filter { (_, other) -> other.shirtFamilies.none { it in kit.shirtFamilies } }
            .shuffled()
            .distinctBy { (_, other) -> other.shirtFamilies }
            .take(3)
            .map { (club, _) -> club.shortName }
        if (distractors.size < 3) return null
        val options = (distractors + target.shortName).shuffled()
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.KIT_PROMPT),
            options = options,
            correctOptionIndex = options.indexOf(target.shortName),
            category = category,
            explanation = strings.text(QuizText.KIT_EXPLANATION, target.shortName, strings.kitDescription(kit), strings.shortsColour(kit)),
            kit = kit,
        )
    }
}
