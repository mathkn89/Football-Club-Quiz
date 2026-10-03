package com.ruflo.footballquiz.domain.usecase

import com.ruflo.footballquiz.data.local.dao.ClubDao
import com.ruflo.footballquiz.data.local.dao.CustomQuestionDao
import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.generator.CapacityQuestionGenerator
import com.ruflo.footballquiz.domain.generator.CityQuestionGenerator
import com.ruflo.footballquiz.domain.generator.LeagueQuestionGenerator
import com.ruflo.footballquiz.domain.generator.ManagerQuestionGenerator
import com.ruflo.footballquiz.domain.generator.NicknameClubQuestionGenerator
import com.ruflo.footballquiz.domain.generator.OldestClubQuestionGenerator
import com.ruflo.footballquiz.domain.generator.StadiumClubQuestionGenerator
import com.ruflo.footballquiz.domain.generator.DynamicQuestionGenerator
import com.ruflo.footballquiz.domain.generator.FoundedYearQuestionGenerator
import com.ruflo.footballquiz.domain.generator.KitQuestionGenerator
import com.ruflo.footballquiz.domain.generator.NicknameQuestionGenerator
import com.ruflo.footballquiz.domain.generator.StadiumQuestionGenerator
import com.ruflo.footballquiz.domain.mapper.toDomain
import com.ruflo.footballquiz.domain.model.Difficulty
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
        StadiumClubQuestionGenerator(),
        CapacityQuestionGenerator(),
        NicknameQuestionGenerator(),
        NicknameClubQuestionGenerator(),
        FoundedYearQuestionGenerator(),
        OldestClubQuestionGenerator(),
        KitQuestionGenerator(),
        LeagueQuestionGenerator(),
        CityQuestionGenerator(),
        ManagerQuestionGenerator(),
    ),
) {

    suspend operator fun invoke(
        roundSize: Int = DEFAULT_ROUND_SIZE,
        customRatio: Float = DEFAULT_CUSTOM_RATIO,
        categories: Set<QuizCategory> = QuizCategory.entries.toSet(),
        leagues: Set<String>? = null,
        difficulty: Difficulty = Difficulty.MEDIUM,
    ): List<QuizQuestion> {
        // Curated questions aren't tied to a league, so a single-league round leaves them out.
        val customCount = if (leagues.isNullOrEmpty()) {
            (roundSize * customRatio).roundToInt().coerceIn(0, roundSize)
        } else {
            0
        }
        val customQuestions = if (customCount == 0) {
            emptyList()
        } else {
            customQuestionDao.getRandom(customCount * CUSTOM_OVERFETCH_FACTOR)
                .map { it.toDomain() }
                .filter { it.category in categories }
                .take(customCount)
        }

        val dynamicCount = roundSize - customQuestions.size
        val dynamicQuestions = buildDynamicQuestions(dynamicCount, categories, leagues, difficulty)

        return (customQuestions + dynamicQuestions).shuffled()
    }

    /**
     * Cycles through the selected categories so a round mixes topics evenly (rather than favouring
     * categories with more generators), and asks about each club at most once.
     */
    private suspend fun buildDynamicQuestions(
        count: Int,
        categories: Set<QuizCategory>,
        leagues: Set<String>?,
        difficulty: Difficulty,
    ): List<QuizQuestion> {
        if (count <= 0) return emptyList()

        val generatorsByCategory = generators.filter { it.category in categories }.groupBy { it.category }
        if (generatorsByCategory.isEmpty()) return emptyList()

        // A chosen league always wins; otherwise Easy sticks to the best-known clubs.
        val poolLeagues = leagues?.takeIf { it.isNotEmpty() }
            ?: EASY_LEAGUES.takeIf { difficulty == Difficulty.EASY }
        val pool = if (poolLeagues == null) {
            clubDao.getRandomClubs(excludeIds = emptyList(), limit = DYNAMIC_POOL_SIZE)
        } else {
            clubDao.getRandomClubsInLeagues(poolLeagues, limit = DYNAMIC_POOL_SIZE)
        }
        if (pool.size < MIN_POOL_SIZE) return emptyList()

        val usedClubIds = mutableSetOf<String>()
        val questions = mutableListOf<QuizQuestion>()
        val categoryCycle = generatorsByCategory.keys.shuffled()
        var madeProgress = true
        while (questions.size < count && madeProgress) {
            madeProgress = false
            for (category in categoryCycle) {
                if (questions.size >= count) break
                val question = generateOne(generatorsByCategory.getValue(category), pool, usedClubIds, difficulty) ?: continue
                questions += question
                madeProgress = true
            }
        }
        return questions
    }

    private fun generateOne(
        generators: List<DynamicQuestionGenerator>,
        pool: List<ClubEntity>,
        usedClubIds: MutableSet<String>,
        difficulty: Difficulty,
    ): QuizQuestion? {
        for (target in pool.shuffled()) {
            if (target.id in usedClubIds) continue
            val others = pool.filter { it.id != target.id }
            // Hard: wrong answers from the same league are much harder to rule out.
            val sameLeague = others.filter { it.league == target.league }
            val distractors = if (difficulty == Difficulty.HARD && sameLeague.size >= MIN_SAME_LEAGUE) sameLeague else others
            for (generator in generators.shuffled()) {
                val question = generator.generate(target, distractors, difficulty)
                    ?: generator.takeIf { distractors !== others }?.generate(target, others, difficulty)
                    ?: continue
                usedClubIds += target.id
                return question
            }
        }
        return null
    }

    private companion object {
        const val DEFAULT_ROUND_SIZE = 10
        const val DEFAULT_CUSTOM_RATIO = 0.3f
        const val CUSTOM_OVERFETCH_FACTOR = 4
        const val DYNAMIC_POOL_SIZE = 100
        const val MIN_POOL_SIZE = 4
        const val MIN_SAME_LEAGUE = 8
        val EASY_LEAGUES = setOf("Premier League", "Championship")
    }
}
