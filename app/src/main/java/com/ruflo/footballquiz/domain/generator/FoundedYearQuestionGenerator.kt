package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import java.util.UUID
import kotlin.random.Random

/** "In what year was Arsenal founded?" — wrong years are close to the real one, so it can't be guessed by era. */
class FoundedYearQuestionGenerator(private val random: Random = Random.Default) : DynamicQuestionGenerator {

    override val category = QuizCategory.FOUNDED_YEAR

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        if (target.foundedYear <= 0) return null
        val correct = target.foundedYear
        val nearbyYears = ((correct - MAX_OFFSET)..(correct + MAX_OFFSET))
            .filter { it != correct && it <= LATEST_YEAR }
            .shuffled(random)
            .take(3)
        val options = (nearbyYears + correct).shuffled(random).map { it.toString() }
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "In what year was ${target.shortName} founded?",
            options = options,
            correctOptionIndex = options.indexOf(correct.toString()),
            category = category,
            explanation = "${target.name} was founded in $correct.",
        )
    }

    private companion object {
        const val MAX_OFFSET = 12
        const val LATEST_YEAR = 2026
    }
}

/** "Which of these clubs was founded first?" — [target] is the oldest of the four. */
class OldestClubQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.FOUNDED_YEAR

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        if (target.foundedYear <= 0) return null
        // At least 3 years younger, so disputed founding dates can't flip the answer.
        val younger = distractorPool
            .filter { it.id != target.id && it.foundedYear >= target.foundedYear + 3 }
            .distinctBy { it.shortName }
            .shuffled()
            .take(3)
        if (younger.size < 3) return null
        val options = (younger.map { it.shortName } + target.shortName).shuffled()
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "Which of these clubs was founded first?",
            options = options,
            correctOptionIndex = options.indexOf(target.shortName),
            category = category,
            explanation = younger.plus(target).sortedBy { it.foundedYear }
                .joinToString(", ") { "${it.shortName} ${it.foundedYear}" },
        )
    }
}
