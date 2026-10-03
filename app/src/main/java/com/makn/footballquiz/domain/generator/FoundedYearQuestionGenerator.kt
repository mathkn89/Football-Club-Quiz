package com.makn.footballquiz.domain.generator

import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizQuestion
import com.makn.footballquiz.domain.text.QuizStrings
import com.makn.footballquiz.domain.text.QuizText
import java.util.UUID
import kotlin.random.Random

/** "In what year was Arsenal founded?" — wrong years are close to the real one, so it can't be guessed by era. */
class FoundedYearQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.FOUNDED_YEAR

    override fun generate(
        target: ClubEntity,
        distractorPool: List<ClubEntity>,
        difficulty: Difficulty,
        random: Random,
    ): QuizQuestion? {
        if (target.foundedYear <= 0) return null
        val correct = target.foundedYear
        val maxOffset = when (difficulty) {
            Difficulty.EASY -> 40
            Difficulty.MEDIUM -> 12
            Difficulty.HARD -> 4
        }
        // On Easy, keep wrong years at least a decade away so the era alone gives it away.
        val minOffset = if (difficulty == Difficulty.EASY) 10 else 1
        val nearbyYears = ((correct - maxOffset)..(correct + maxOffset))
            .filter { kotlin.math.abs(it - correct) >= minOffset && it <= LATEST_YEAR }
            .shuffled(random)
            .take(3)
        val options = (nearbyYears + correct).shuffled(random).map { it.toString() }
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.FOUNDED_PROMPT, target.shortName),
            options = options,
            correctOptionIndex = options.indexOf(correct.toString()),
            category = category,
            explanation = strings.text(QuizText.FOUNDED_EXPLANATION, target.shortName, correct.toString()),
        )
    }

    private companion object {
        const val LATEST_YEAR = 2026
    }
}

/** "Which of these clubs was founded first?" — [target] is the oldest of the four. */
class OldestClubQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.FOUNDED_YEAR

    override fun generate(
        target: ClubEntity,
        distractorPool: List<ClubEntity>,
        difficulty: Difficulty,
        random: Random,
    ): QuizQuestion? {
        if (target.foundedYear <= 0) return null
        // A minimum gap so disputed founding dates can't flip the answer; wider on Easy.
        val minGap = when (difficulty) {
            Difficulty.EASY -> 25
            Difficulty.MEDIUM -> 8
            Difficulty.HARD -> 3
        }
        val younger = distractorPool
            .filter { it.id != target.id && it.foundedYear >= target.foundedYear + minGap }
            .distinctBy { it.shortName }
            .shuffled(random)
            .take(3)
        if (younger.size < 3) return null
        val options = (younger.map { it.shortName } + target.shortName).shuffled(random)
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.OLDEST_PROMPT),
            options = options,
            correctOptionIndex = options.indexOf(target.shortName),
            category = category,
            explanation = younger.plus(target).sortedBy { it.foundedYear }
                .joinToString(", ") { "${it.shortName} ${it.foundedYear}" },
        )
    }
}
