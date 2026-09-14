package com.ruflo.footballquiz.domain.mapper

import com.ruflo.footballquiz.data.local.entity.CustomQuestionEntity
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.domain.model.QuizQuestion

fun CustomQuestionEntity.toDomain(): QuizQuestion {
    val options = (wrongAnswers + correctAnswer).shuffled()
    return QuizQuestion(
        id = id,
        prompt = questionText,
        options = options,
        correctOptionIndex = options.indexOf(correctAnswer),
        category = QuizCategory.fromRaw(category),
        explanation = explanation,
        imageUrl = imageUriOrUrl,
    )
}
