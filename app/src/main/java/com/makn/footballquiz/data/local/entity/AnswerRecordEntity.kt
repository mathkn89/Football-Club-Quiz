package com.makn.footballquiz.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One answered question, kept for per-topic stats. */
@Entity(tableName = "answer_records", indices = [Index("category")])
data class AnswerRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val attemptId: String,
    val category: String,
    val correct: Boolean,
)

/** Answers and correct answers per topic, as read by the stats query. */
data class CategoryStatRow(
    val category: String,
    val answered: Int,
    val correct: Int,
)
