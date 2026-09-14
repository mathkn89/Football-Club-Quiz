package com.ruflo.footballquiz.domain.generator

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion
import java.util.UUID

class NicknameQuestionGenerator : DynamicQuestionGenerator {

    override val category = QuizCategory.NICKNAME

    override fun generate(target: ClubEntity, distractorPool: List<ClubEntity>): QuizQuestion? {
        val options = buildOptions(target.nickname, distractorPool.map { it.nickname }) ?: return null
        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            prompt = "What is the nickname of ${target.name}?",
            options = options,
            correctOptionIndex = options.indexOf(target.nickname),
            category = QuizCategory.NICKNAME,
            explanation = "${target.name} are nicknamed \"${target.nickname}\".",
        )
    }
}
