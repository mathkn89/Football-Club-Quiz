package com.ruflo.footballquiz.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ruflo.footballquiz.data.local.entity.CustomQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomQuestionDao {

    @Upsert
    suspend fun upsertAll(questions: List<CustomQuestionEntity>)

    @Upsert
    suspend fun upsert(question: CustomQuestionEntity)

    @Query("SELECT * FROM custom_questions")
    fun observeAll(): Flow<List<CustomQuestionEntity>>

    @Query("SELECT * FROM custom_questions WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomByCategory(category: String, limit: Int): List<CustomQuestionEntity>

    @Query("SELECT * FROM custom_questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandom(limit: Int): List<CustomQuestionEntity>

    @Query("SELECT MAX(version) FROM custom_questions")
    suspend fun latestVersion(): Int?

    @Query("DELETE FROM custom_questions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM custom_questions")
    suspend fun clearAll()
}
