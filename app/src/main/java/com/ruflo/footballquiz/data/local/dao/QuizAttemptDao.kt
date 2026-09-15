package com.ruflo.footballquiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ruflo.footballquiz.data.local.entity.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {

    @Insert
    suspend fun insert(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM quiz_attempts ORDER BY completedAtMillis DESC")
    fun observeAll(): Flow<List<QuizAttemptEntity>>
}
