package com.makn.footballquiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.makn.footballquiz.data.local.entity.AnswerRecordEntity
import com.makn.footballquiz.data.local.entity.CategoryStatRow
import com.makn.footballquiz.data.local.entity.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {

    @Insert
    suspend fun insert(attempt: QuizAttemptEntity)

    @Insert
    suspend fun insertAnswers(answers: List<AnswerRecordEntity>)

    @Transaction
    suspend fun insertWithAnswers(attempt: QuizAttemptEntity, answers: List<AnswerRecordEntity>) {
        insert(attempt)
        if (answers.isNotEmpty()) insertAnswers(answers)
    }

    @Query(
        """
        SELECT category, COUNT(*) AS answered, SUM(correct) AS correct
        FROM answer_records GROUP BY category
        """
    )
    fun observeCategoryStats(): Flow<List<CategoryStatRow>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    fun observeAttempt(id: String): Flow<QuizAttemptEntity?>

    /** In the order they were asked. */
    @Query("SELECT * FROM answer_records WHERE attemptId = :attemptId ORDER BY id")
    fun observeAnswers(attemptId: String): Flow<List<AnswerRecordEntity>>

    @Query("SELECT * FROM quiz_attempts ORDER BY completedAtMillis DESC")
    fun observeAll(): Flow<List<QuizAttemptEntity>>
}
