package com.makn.footballquiz.domain.usecase

import com.makn.footballquiz.data.local.dao.ClubDao
import com.makn.footballquiz.data.local.dao.CustomQuestionDao
import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.domain.generator.CapacityQuestionGenerator
import com.makn.footballquiz.domain.generator.CityQuestionGenerator
import com.makn.footballquiz.domain.generator.LeagueQuestionGenerator
import com.makn.footballquiz.domain.generator.ManagerQuestionGenerator
import com.makn.footballquiz.domain.generator.NicknameClubQuestionGenerator
import com.makn.footballquiz.domain.generator.OldestClubQuestionGenerator
import com.makn.footballquiz.domain.generator.StadiumClubQuestionGenerator
import com.makn.footballquiz.domain.generator.DynamicQuestionGenerator
import com.makn.footballquiz.domain.generator.FoundedYearQuestionGenerator
import com.makn.footballquiz.domain.generator.KitQuestionGenerator
import com.makn.footballquiz.domain.generator.NicknameQuestionGenerator
import com.makn.footballquiz.domain.generator.StadiumQuestionGenerator
import com.makn.footballquiz.domain.mapper.toDomain
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizQuestion
import com.makn.footballquiz.domain.text.QuizStrings
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Builds a quiz round by interleaving dynamically-generated questions (drawn from [ClubEntity]
 * data) with hand-curated [com.makn.footballquiz.data.local.entity.CustomQuestionEntity] rows.
 */
class GetQuizRoundUseCase(
    private val clubDao: ClubDao,
    private val customQuestionDao: CustomQuestionDao,
    private val generatorsFor: (QuizStrings) -> List<DynamicQuestionGenerator> = ::defaultGenerators,
) {

    suspend operator fun invoke(
        roundSize: Int = DEFAULT_ROUND_SIZE,
        customRatio: Float = DEFAULT_CUSTOM_RATIO,
        categories: Set<QuizCategory> = QuizCategory.entries.toSet(),
        leagues: Set<String>? = null,
        difficulty: Difficulty = Difficulty.MEDIUM,
        strings: QuizStrings,
        random: Random = Random.Default,
        /** Same data + same [random] seed → the same round on every device (daily challenge). */
        deterministic: Boolean = false,
    ): List<QuizQuestion> {
        // Curated questions aren't tied to a league, so a single-league round leaves them out.
        // The table is small (~100 rows), so take it all in a stable order and shuffle with [random]:
        // that keeps the daily challenge identical everywhere and other rounds properly random.
        val customPool = if (leagues.isNullOrEmpty()) {
            customQuestionDao.getAllOrdered()
                .shuffled(random)
                .map { it.toDomain(strings.languageCode, random) }
                .filter { it.category in categories }
        } else {
            emptyList()
        }
        val customShare = (roundSize * customRatio).roundToInt().coerceIn(0, roundSize)
        val customQuestions = customPool.take(customShare)

        val dynamicQuestions = buildDynamicQuestions(
            roundSize - customQuestions.size, categories, leagues, difficulty, generatorsFor(strings), random, deterministic,
        )
        // Topics without generated questions (History, Rivalries, General) top up from the
        // curated pool, so a trivia-only round is still full length.
        val topUp = customPool.drop(customQuestions.size).take(roundSize - customQuestions.size - dynamicQuestions.size)

        return (customQuestions + topUp + dynamicQuestions).shuffled(random)
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
        generators: List<DynamicQuestionGenerator>,
        random: Random,
        deterministic: Boolean,
    ): List<QuizQuestion> {
        if (count <= 0) return emptyList()

        val generatorsByCategory = generators.filter { it.category in categories }.groupBy { it.category }
        if (generatorsByCategory.isEmpty()) return emptyList()

        // A chosen league always wins; otherwise Easy sticks to the best-known clubs.
        val poolLeagues = leagues?.takeIf { it.isNotEmpty() }
            ?: EASY_LEAGUES.takeIf { difficulty == Difficulty.EASY }
        val pool = if (deterministic) {
            clubDao.getAllOrdered()
                .filter { poolLeagues == null || it.league in poolLeagues }
                .shuffled(random)
                .take(DYNAMIC_POOL_SIZE)
        } else if (poolLeagues == null) {
            clubDao.getRandomClubs(excludeIds = emptyList(), limit = DYNAMIC_POOL_SIZE)
        } else {
            clubDao.getRandomClubsInLeagues(poolLeagues, limit = DYNAMIC_POOL_SIZE)
        }
        if (pool.size < MIN_POOL_SIZE) return emptyList()

        val usedClubIds = mutableSetOf<String>()
        val questions = mutableListOf<QuizQuestion>()
        val categoryCycle = generatorsByCategory.keys.toList().shuffled(random)
        var madeProgress = true
        while (questions.size < count && madeProgress) {
            madeProgress = false
            for (category in categoryCycle) {
                if (questions.size >= count) break
                val question = generateOne(generatorsByCategory.getValue(category), pool, usedClubIds, difficulty, random) ?: continue
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
        random: Random,
    ): QuizQuestion? {
        for (target in pool.shuffled(random)) {
            if (target.id in usedClubIds) continue
            val others = pool.filter { it.id != target.id }
            // Hard: wrong answers from the same league are much harder to rule out.
            val sameLeague = others.filter { it.league == target.league }
            val distractors = if (difficulty == Difficulty.HARD && sameLeague.size >= MIN_SAME_LEAGUE) sameLeague else others
            for (generator in generators.shuffled(random)) {
                val question = generator.generate(target, distractors, difficulty, random)
                    ?: generator.takeIf { distractors !== others }?.generate(target, others, difficulty, random)
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
        const val DYNAMIC_POOL_SIZE = 100
        const val MIN_POOL_SIZE = 4
        const val MIN_SAME_LEAGUE = 8
        val EASY_LEAGUES = setOf("Premier League", "Championship")
    }
}

/** Every club-data question type, with its text in the language of [strings]. */
fun defaultGenerators(strings: QuizStrings): List<DynamicQuestionGenerator> = listOf(
    StadiumQuestionGenerator(strings),
    StadiumClubQuestionGenerator(strings),
    CapacityQuestionGenerator(strings),
    NicknameQuestionGenerator(strings),
    NicknameClubQuestionGenerator(strings),
    FoundedYearQuestionGenerator(strings),
    OldestClubQuestionGenerator(strings),
    KitQuestionGenerator(strings),
    LeagueQuestionGenerator(strings),
    CityQuestionGenerator(strings),
    ManagerQuestionGenerator(strings),
)
