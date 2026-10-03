package com.makn.footballquiz.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One answered question, kept for per-topic stats and the round details in History. The question
 * fields are null/empty for rounds recorded before details were stored (history v2).
 */
@Entity(tableName = "answer_records", indices = [Index("category")])
data class AnswerRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val attemptId: String,
    val category: String,
    val correct: Boolean,
    /** Question as shown to the player, in the language they played in. */
    val prompt: String? = null,
    @ColumnInfo(defaultValue = "")
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int? = null,
    /** Null = time ran out. */
    val selectedOptionIndex: Int? = null,
    val explanation: String? = null,
    /** Kit drawn above a kit question, as [com.makn.footballquiz.domain.model.Kit.encode]. */
    val kit: String? = null,
)

/** Answers and correct answers per topic, as read by the stats query. */
data class CategoryStatRow(
    val category: String,
    val answered: Int,
    val correct: Int,
)
