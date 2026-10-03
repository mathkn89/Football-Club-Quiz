package com.makn.footballquiz.domain.generator

import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizQuestion
import com.makn.footballquiz.domain.text.QuizStrings
import com.makn.footballquiz.domain.text.QuizText
import java.util.UUID

/** "What is Arsenal's home stadium?" */
class StadiumQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.STADIUM

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>, difficulty: Difficulty): QuizQuestion? {
        if (target.giveaway(target.stadiumName)) return null
        val candidates = distractorPool.filter { nameKey(it.stadiumName) != nameKey(target.stadiumName) }
        val options = buildOptions(target.stadiumName, candidates.map { it.stadiumName }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.STADIUM_PROMPT, target.shortName),
            options = options,
            correctOptionIndex = options.indexOf(target.stadiumName),
            category = category,
            explanation = strings.text(QuizText.STADIUM_EXPLANATION, target.shortName, target.stadiumName, target.city),
        )
    }
}

/** "Which club plays at Anfield?" — stadium names are unique, so there's exactly one right club. */
class StadiumClubQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.STADIUM

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>, difficulty: Difficulty): QuizQuestion? {
        if (target.stadiumName.isBlank() || target.giveaway(target.stadiumName)) return null
        // Near-identical ground names (St James' Park / St James Park) would make two answers right.
        if (distractorPool.any { it.id != target.id && nameKey(it.stadiumName) == nameKey(target.stadiumName) }) return null
        val options = buildClubOptions(target, distractorPool) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.STADIUM_CLUB_PROMPT, target.stadiumName),
            options = options,
            correctOptionIndex = options.indexOf(target.shortName),
            category = category,
            explanation = strings.text(QuizText.STADIUM_CLUB_EXPLANATION, target.stadiumName, target.shortName),
        )
    }
}

/** "Which of these grounds holds the most fans?" — [target] is the biggest of the four. */
class CapacityQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.STADIUM

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>, difficulty: Difficulty): QuizQuestion? {
        if (target.stadiumCapacity <= 0 || target.stadiumName.isBlank()) return null
        // Published capacities vary between sources, so always leave a margin; widest on Easy.
        val maxPercent = when (difficulty) {
            Difficulty.EASY -> 60
            Difficulty.MEDIUM -> 85
            Difficulty.HARD -> 90
        }
        val smaller = distractorPool
            .filter { it.stadiumCapacity in 1..(target.stadiumCapacity * maxPercent / 100) && it.stadiumName.isNotBlank() }
            .distinctBy { nameKey(it.stadiumName) }
            .shuffled()
            .take(3)
        if (smaller.size < 3) return null
        val options = (smaller.map { it.stadiumName } + target.stadiumName).shuffled()
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.CAPACITY_PROMPT),
            options = options,
            correctOptionIndex = options.indexOf(target.stadiumName),
            category = category,
            explanation = strings.text(
                QuizText.CAPACITY_EXPLANATION, target.stadiumName, target.shortName, strings.number(target.stadiumCapacity),
            ),
        )
    }
}
