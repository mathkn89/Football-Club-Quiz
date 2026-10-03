package com.makn.footballquiz.domain.generator

import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizQuestion
import com.makn.footballquiz.domain.text.QuizStrings
import com.makn.footballquiz.domain.text.QuizText
import java.util.UUID
import kotlin.random.Random

/** "What is Barnsley's nickname?" */
class NicknameQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.NICKNAME

    override fun generate(
        target: ClubEntity,
        distractorPool: List<ClubEntity>,
        difficulty: Difficulty,
        random: Random,
    ): QuizQuestion? {
        if (target.giveaway(target.nickname)) return null
        // Leave out other clubs that share this nickname, else two options would read the same.
        val candidates = distractorPool.filter { nicknameKey(it.nickname) != nicknameKey(target.nickname) }
        val options = buildOptions(target.nickname, candidates.map { it.nickname }, random) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.NICKNAME_PROMPT, target.shortName),
            options = options,
            correctOptionIndex = options.indexOf(target.nickname),
            category = category,
            explanation = strings.text(QuizText.NICKNAME_EXPLANATION, target.shortName, target.nickname),
        )
    }
}

/** "Which club is nicknamed 'The Tykes'?" — skipped for nicknames more than one club shares. */
class NicknameClubQuestionGenerator(private val strings: QuizStrings) : DynamicQuestionGenerator {

    override val category = QuizCategory.NICKNAME

    override fun generate(
        target: ClubEntity,
        distractorPool: List<ClubEntity>,
        difficulty: Difficulty,
        random: Random,
    ): QuizQuestion? {
        if (target.nickname.isBlank() || target.giveaway(target.nickname)) return null
        val key = nicknameKey(target.nickname)
        if (distractorPool.any { it.id != target.id && nicknameKey(it.nickname) == key }) return null
        val options = buildClubOptions(target, distractorPool, random) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = strings.text(QuizText.NICKNAME_CLUB_PROMPT, target.nickname),
            options = options,
            correctOptionIndex = options.indexOf(target.shortName),
            category = category,
            explanation = strings.text(QuizText.NICKNAME_CLUB_EXPLANATION, target.nickname, target.shortName),
        )
    }
}
