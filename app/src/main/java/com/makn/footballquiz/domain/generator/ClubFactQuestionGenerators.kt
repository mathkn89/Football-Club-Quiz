package com.makn.footballquiz.domain.generator

import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizQuestion
import com.makn.footballquiz.domain.text.QuizStrings
import com.makn.footballquiz.domain.text.QuizText
import com.makn.footballquiz.domain.text.leagueInSentence
import java.util.UUID

/** "Which league does Barnsley play in?" Needs clubs from at least 4 leagues in the pool. */
class LeagueQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.LEAGUE

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>, difficulty: Difficulty): QuizQuestion? {
        val options = buildOptions(target.league, distractorPool.map { it.league }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.LEAGUE_PROMPT, target.shortName),
            options = options,
            correctOptionIndex = options.indexOf(target.league),
            category = category,
            explanation = strings.text(QuizText.LEAGUE_EXPLANATION, target.shortName, strings.leagueInSentence(target.league)),
        )
    }
}

/** "Where are Tranmere Rovers based?" — only asked when the town isn't in the club's name. */
class CityQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.LOCATION

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>, difficulty: Difficulty): QuizQuestion? {
        if (target.giveaway(target.city)) return null
        val options = buildOptions(target.city, distractorPool.map { it.city }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.CITY_PROMPT, target.shortName),
            options = options,
            correctOptionIndex = options.indexOf(target.city),
            category = category,
            explanation = strings.text(QuizText.CITY_EXPLANATION, target.shortName, target.stadiumName, target.city),
        )
    }
}

/** "Who is the manager of Wrexham?" — skipped while a club has caretakers or no one in charge. */
class ManagerQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.MANAGER

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>, difficulty: Difficulty): QuizQuestion? {
        if (!target.manager.isSingleNamedManager()) return null
        val candidates = distractorPool.map { it.manager }.filter { it.isSingleNamedManager() }
        val options = buildOptions(target.manager, candidates) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.MANAGER_PROMPT, target.shortName),
            options = options,
            correctOptionIndex = options.indexOf(target.manager),
            category = category,
            explanation = strings.text(QuizText.MANAGER_EXPLANATION, target.manager, target.shortName),
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
