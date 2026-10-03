package com.makn.footballquiz.domain.mapper

import com.makn.footballquiz.data.local.entity.CustomQuestionEntity
import com.makn.footballquiz.data.remote.dto.QuestionTranslationDto
import com.makn.footballquiz.data.remote.mapper.translationJson
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizQuestion
import kotlinx.serialization.decodeFromString

/** The question in [languageCode] when a translation exists, otherwise the English original. */
fun CustomQuestionEntity.toDomain(languageCode: String = "en"): QuizQuestion {
    val translation = translations
        ?.let { runCatching { translationJson.decodeFromString<Map<String, QuestionTranslationDto>>(it) }.getOrNull() }
        ?.get(languageCode)
        ?.takeIf { it.wrongAnswers.size == wrongAnswers.size }
    val correct = translation?.correctAnswer ?: correctAnswer
    val options = ((translation?.wrongAnswers ?: wrongAnswers) + correct).shuffled()
    return QuizQuestion(
        id = id,
        prompt = translation?.questionText ?: questionText,
        options = options,
        correctOptionIndex = options.indexOf(correct),
        category = QuizCategory.fromRaw(category),
        explanation = translation?.explanation ?: explanation,
        imageUrl = imageUriOrUrl,
    )
}
