package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import java.util.UUID

/** "What is Arsenal's home stadium?" */
class StadiumQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.STADIUM

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        if (target.giveaway(target.stadiumName)) return null
        val candidates = distractorPool.filter { nameKey(it.stadiumName) != nameKey(target.stadiumName) }
        val options = buildOptions(target.stadiumName, candidates.map { it.stadiumName }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "What is the name of ${target.shortName}'s home stadium?",
            options = options,
            correctOptionIndex = options.indexOf(target.stadiumName),
            category = category,
            explanation = "${target.shortName} play their home games at ${target.stadiumName} in ${target.city}.",
        )
    }
}

/** "Which club plays at Anfield?" — stadium names are unique, so there's exactly one right club. */
class StadiumClubQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.STADIUM

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        if (target.stadiumName.isBlank() || target.giveaway(target.stadiumName)) return null
        // Near-identical ground names (St James' Park / St James Park) would make two answers right.
        if (distractorPool.any { it.id != target.id && nameKey(it.stadiumName) == nameKey(target.stadiumName) }) return null
        val options = buildClubOptions(target, distractorPool) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "Which club plays its home games at ${target.stadiumName}?",
            options = options,
            correctOptionIndex = options.indexOf(target.shortName),
            category = category,
            explanation = "${target.stadiumName} is the home of ${target.shortName}.",
        )
    }
}

/** "Which of these grounds holds the most fans?" — [target] is the biggest of the four. */
class CapacityQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.STADIUM

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        if (target.stadiumCapacity <= 0 || target.stadiumName.isBlank()) return null
        // At least 15% smaller: published capacities vary between sources, so close calls aren't fair.
        val smaller = distractorPool
            .filter { it.stadiumCapacity in 1..(target.stadiumCapacity * 85 / 100) && it.stadiumName.isNotBlank() }
            .distinctBy { nameKey(it.stadiumName) }
            .shuffled()
            .take(3)
        if (smaller.size < 3) return null
        val options = (smaller.map { it.stadiumName } + target.stadiumName).shuffled()
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "Which of these grounds has the largest capacity?",
            options = options,
            correctOptionIndex = options.indexOf(target.stadiumName),
            category = category,
            explanation = "${target.stadiumName} (${target.shortName}) holds about ${"%,d".format(target.stadiumCapacity)}.",
        )
    }
}
