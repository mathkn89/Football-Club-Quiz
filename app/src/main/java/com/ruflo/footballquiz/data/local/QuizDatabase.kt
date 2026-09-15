package com.ruflo.footballquiz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ruflo.footballquiz.data.local.converter.Converters
import com.ruflo.footballquiz.data.local.dao.ClubDao
import com.ruflo.footballquiz.data.local.dao.CustomQuestionDao
import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.data.local.entity.CustomQuestionEntity

@Database(
    entities = [ClubEntity::class, CustomQuestionEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun clubDao(): ClubDao
    abstract fun customQuestionDao(): CustomQuestionDao

    companion object {
        private const val DATABASE_NAME = "quiz.db"
        private const val ASSET_DATABASE_PATH = "database/clubs.db"

        @Volatile
        private var instance: QuizDatabase? = null

        fun getInstance(context: Context): QuizDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): QuizDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                QuizDatabase::class.java,
                DATABASE_NAME,
            )
                .createFromAsset(ASSET_DATABASE_PATH)
                .fallbackToDestructiveMigration()
                .build()
    }
}
