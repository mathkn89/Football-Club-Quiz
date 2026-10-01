package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import java.util.UUID

/** "What is Barnsley's nickname?" */
class NicknameQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.NICKNAME

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        if (target.giveaway(target.nickname)) return null
        // Leave out other clubs that share this nickname, else two options would read the same.
        val candidates = distractorPool.filter { nicknameKey(it.nickname) != nicknameKey(target.nickname) }
        val options = buildOptions(target.nickname, candidates.map { it.nickname }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "What is the nickname of ${target.shortName}?",
            options = options,
            correctOptionIndex = options.indexOf(target.nickname),
            category = category,
            explanation = "${target.shortName} are nicknamed \"${target.nickname}\".",
        )
    }
}

/** "Which club is nicknamed 'The Tykes'?" — skipped for nicknames more than one club shares. */
class NicknameClubQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.NICKNAME

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        if (target.nickname.isBlank() || target.giveaway(target.nickname)) return null
        val key = nicknameKey(target.nickname)
        if (distractorPool.any { it.id != target.id && nicknameKey(it.nickname) == key }) return null
        val options = buildClubOptions(target, distractorPool) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "Which club is nicknamed \"${target.nickname}\"?",
            options = options,
            correctOptionIndex = options.indexOf(target.shortName),
            category = category,
            explanation = "\"${target.nickname}\" is the nickname of ${target.shortName}.",
        )
    }
}
