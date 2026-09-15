package com.ruflo.footballquiz.domain.mapper

import com.ruflo.footballquiz.data.local.entity.QuizAttemptEntity
import com.ruflo.footballquiz.domain.model.QuizAttempt
import com.ruflo.footballquiz.domain.model.QuizCategory

fun QuizAttemptEntity.toDomain(): QuizAttempt = QuizAttempt(
    id = id,
    completedAtMillis = completedAtMillis,
    score = score,
    total = total,
    categories = categories.map { QuizCategory.fromRaw(it) }.toSet(),
)
