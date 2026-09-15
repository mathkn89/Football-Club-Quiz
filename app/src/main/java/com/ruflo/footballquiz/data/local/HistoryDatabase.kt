package com.ruflo.footballquiz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ruflo.footballquiz.data.local.converter.Converters
import com.ruflo.footballquiz.data.local.dao.QuizAttemptDao
import com.ruflo.footballquiz.data.local.entity.QuizAttemptEntity

/**
 * Local user-generated data (quiz history), kept separate from [QuizDatabase] so it never touches
 * that database's createFromAsset identity-hash contract — this one is plain app-created Room,
 * no bundled seed, so ordinary migrations apply.
 */
@Database(entities = [QuizAttemptEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class HistoryDatabase : RoomDatabase() {

    abstract fun quizAttemptDao(): QuizAttemptDao

    companion object {
        private const val DATABASE_NAME = "history.db"

        @Volatile
        private var instance: HistoryDatabase? = null

        fun getInstance(context: Context): HistoryDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): HistoryDatabase =
            Room.databaseBuilder(context.applicationContext, HistoryDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}
