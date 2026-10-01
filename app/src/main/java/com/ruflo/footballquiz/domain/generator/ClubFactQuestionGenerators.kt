package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import java.util.UUID

/** "Which league does Barnsley play in?" Needs clubs from at least 4 leagues in the pool. */
class LeagueQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.LEAGUE

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        val options = buildOptions(target.league, distractorPool.map { it.league }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "Which league does ${target.shortName} play in this season?",
            options = options,
            correctOptionIndex = options.indexOf(target.league),
            category = category,
            explanation = "${target.shortName} play in ${target.league.withArticle()}.",
        )
    }
}

/** "the Premier League" / "the Championship", but just "League One" / "League Two". */
private fun String.withArticle(): String = if (startsWith("League ")) this else "the $this"

/** "Where are Tranmere Rovers based?" — only asked when the town isn't in the club's name. */
class CityQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.LOCATION

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        if (target.giveaway(target.city)) return null
        val options = buildOptions(target.city, distractorPool.map { it.city }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "Which town or city is ${target.shortName} based in?",
            options = options,
            correctOptionIndex = options.indexOf(target.city),
            category = category,
            explanation = "${target.shortName} play at ${target.stadiumName} in ${target.city}.",
        )
    }
}

/** "Who is the manager of Wrexham?" — skipped while a club has caretakers or no one in charge. */
class ManagerQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.MANAGER

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        if (!target.manager.isSingleNamedManager()) return null
        val candidates = distractorPool.map { it.manager }.filter { it.isSingleNamedManager() }
        val options = buildOptions(target.manager, candidates) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "Who is the manager of ${target.shortName}?",
            options = options,
            correctOptionIndex = options.indexOf(target.manager),
            category = category,
            explanation = "${target.manager} manages ${target.shortName}.",
        )
    }

    private fun String.isSingleNamedManager(): Boolean {
        val lower = lowercase()
        return isNotBlank() && PLACEHOLDERS.none { it in lower }
    }

    private companion object {
        val PLACEHOLDERS = listOf("caretaker", "interim", "vacant", "unknown", "&", " and ")
    }
}
