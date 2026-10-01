package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import java.util.UUID

/**
 * "Which club does this badge belong to?" Uses [ClubEntity.badgeQuizUrl] — the crest with its
 * lettering painted out — and skips clubs that don't have one yet, since the regular badge
 * usually spells out the answer.
 */
class BadgeQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.BADGE

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        val badge = target.badgeQuizUrl ?: return null
        val options = buildClubOptions(target, distractorPool) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "Which club does this badge belong to?",
            options = options,
            correctOptionIndex = options.indexOf(target.shortName),
            category = category,
            imageUrl = badge,
        )
    }

    companion object {
        /** Marks [ClubEntity.badgeDrawableName] as a local drawable resource name, not a URL. */
        const val DRAWABLE_SCHEME = "drawable://"
    }
}
