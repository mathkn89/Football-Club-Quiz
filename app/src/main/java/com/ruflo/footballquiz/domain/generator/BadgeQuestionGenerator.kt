package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import java.util.UUID

class BadgeQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.BADGE

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        val badge = target.badgeDrawableName?.let { "$DRAWABLE_SCHEME$it" }
            ?: target.badgeRemoteUrl
            ?: return null
        val options = buildOptions(target.name, distractorPool.map { it.name }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "Which club does this badge belong to?",
            options = options,
            correctOptionIndex = options.indexOf(target.name),
            category = QuizCategory.BADGE,
            imageUrl = badge,
        )
    }

    companion object {
        /** Marks [target.badgeDrawableName] as a local drawable resource name, not a URL. */
        const val DRAWABLE_SCHEME = "drawable://"
    }
}
