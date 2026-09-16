package com.ruflo.footballquiz.domain.usecase

import com.ruflo.footballquiz.data.local.dao.ClubDao
import com.ruflo.footballquiz.data.local.dao.CustomQuestionDao
import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.generator.BadgeQuestionGenerator
import com.ruflo.footballquiz.domain.generator.DynamicQuestionGenerator
import com.ruflo.footballquiz.domain.generator.FoundedYearQuestionGenerator
import com.ruflo.footballquiz.domain.generator.NicknameQuestionGenerator
import com.ruflo.footballquiz.domain.generator.StadiumQuestionGenerator
import com.ruflo.footballquiz.domain.mapper.toDomain
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import kotlin.math.roundToInt

/**
 * Builds a quiz round by interleaving dynamically-generated questions (drawn from [ClubEntity]
 * data) with hand-curated [com.ruflo.footballquiz.data.local.entity.CustomQuestionEntity] rows.
 */
class GetQuizRoundUseCase(
    private val clubDao: ClubDao,
    private val customQuestionDao: CustomQuestionDao,
    private val generators: List<DynamicQuestionGenerator> = listOf(
        StadiumQuestionGenerator(),
        NicknameQuestionGenerator(),
        FoundedYearQuestionGenerator(),
        BadgeQuestionGenerator(),
    ),
) {

    suspend operator fun invoke(
        roundSize: Int = DEFAULT_ROUND_SIZE,
        customRatio: Float = DEFAULT_CUSTOM_RATIO,
        categories: Set<QuizCategory> = QuizCategory.entries.toSet(),
        leagues: Set<String>? = null,
    ): List<QuizQuestion> {
        val customCount = (roundSize * customRatio).roundToInt().coerceIn(0, roundSize)
        val customPoolSize = customCount * CUSTOM_OVERFETCH_FACTOR
        val customQuestions = customQuestionDao.getRandom(customPoolSize)
            .map { it.toDomain() }
            .filter { it.category in categories }
            .take(customCount)

        val dynamicCount = roundSize - customQuestions.size
        val dynamicQuestions = buildDynamicQuestions(dynamicCount, categories, leagues)

        return (customQuestions + dynamicQuestions).shuffled()
    }

    private suspend fun buildDynamicQuestions(
        count: Int,
        categories: Set<QuizCategory>,
        leagues: Set<String>?,
    ): List<QuizQuestion> {
        if (count <= 0) return emptyList()

        val activeGenerators = generators.filter { it.category in categories }
        if (activeGenerators.isEmpty()) return emptyList()

        val pool = clubDao.getRandomClubs(excludeIds = emptyList(), limit = DYNAMIC_POOL_SIZE)
            .let { clubs -> if (leagues.isNullOrEmpty()) clubs else clubs.filter { it.league in leagues } }
        if (pool.size < MIN_POOL_SIZE) return emptyList()

        val combos = pool.flatMap { target -> activeGenerators.map { target to it } }.shuffled()
        val questions = mutableListOf<QuizQuestion>()
        for ((target, generator) in combos) {
            if (questions.size >= count) break
            val distractors = pool.filter { it.id != target.id }
            generator.generate(target, distractors)?.let { questions += it }
        }
        return questions
    }

    private companion object {
        const val DEFAULT_ROUND_SIZE = 10
        const val DEFAULT_CUSTOM_RATIO = 0.3f
        const val CUSTOM_OVERFETCH_FACTOR = 4
        const val DYNAMIC_POOL_SIZE = 100
        const val MIN_POOL_SIZE = 4
    }
}
