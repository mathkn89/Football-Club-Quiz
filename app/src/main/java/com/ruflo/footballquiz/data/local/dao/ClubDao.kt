package com.ruflo.footballquiz.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ruflo.footballquiz.data.local.entity.ClubEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDao {

    @Upsert
    suspend fun upsertAll(clubs: List<ClubEntity>)

    @Upsert
    suspend fun upsert(club: ClubEntity)

    @Query("SELECT * FROM clubs ORDER BY name ASC")
    fun observeAll(): Flow<List<ClubEntity>>

    @Query("SELECT * FROM clubs WHERE id = :id")
    suspend fun getById(id: String): ClubEntity?

    @Query("SELECT COUNT(*) FROM clubs")
    suspend fun count(): Int

    @Query("SELECT MAX(version) FROM clubs")
    suspend fun latestVersion(): Int?

    @Query("SELECT DISTINCT league FROM clubs ORDER BY league ASC")
    suspend fun getDistinctLeagues(): List<String>

    /** Random clubs for a quiz round; [excludeIds] keeps the correct-answer club out of the pool. */
    @Query("SELECT * FROM clubs WHERE id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomClubs(excludeIds: List<String>, limit: Int): List<ClubEntity>

    /** Distractor names for a given club field, excluding the correct club's own value. */
    @Query(
        """
        SELECT name FROM clubs
        WHERE id != :excludeClubId
        ORDER BY RANDOM()
        LIMIT :limit
        """
    )
    suspend fun getRandomClubNames(excludeClubId: String, limit: Int): List<String>

    @Query("DELETE FROM clubs WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM clubs")
    suspend fun clearAll()
}
