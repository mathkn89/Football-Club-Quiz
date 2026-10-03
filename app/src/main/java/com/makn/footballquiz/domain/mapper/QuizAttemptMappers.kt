package com.makn.footballquiz.domain.mapper

import com.makn.footballquiz.data.local.entity.QuizAttemptEntity
import com.makn.footballquiz.domain.model.QuizAttempt
import com.makn.footballquiz.domain.model.QuizCategory

fun QuizAttemptEntity.toDomain(): QuizAttempt = QuizAttempt(
    id = id,
    completedAtMillis = completedAtMillis,
    score = score,
    total = total,
    categories = categories.map { QuizCategory.fromRaw(it) }.toSet(),
)
