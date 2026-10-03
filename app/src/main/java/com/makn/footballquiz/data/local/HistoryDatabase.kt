package com.makn.footballquiz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.makn.footballquiz.data.local.converter.Converters
import com.makn.footballquiz.data.local.dao.QuizAttemptDao
import com.makn.footballquiz.data.local.entity.AnswerRecordEntity
import com.makn.footballquiz.data.local.entity.QuizAttemptEntity

/**
 * Local user-generated data (quiz history), kept separate from [QuizDatabase] so it never touches
 * that database's createFromAsset identity-hash contract — this one is plain app-created Room,
 * no bundled seed, so ordinary migrations apply.
 */
@Database(entities = [QuizAttemptEntity::class, AnswerRecordEntity::class], version = 3, exportSchema = true)
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()

        /** Stores each question and answer so a round can be reviewed later from History. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE answer_records ADD COLUMN prompt TEXT")
                db.execSQL("ALTER TABLE answer_records ADD COLUMN options TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE answer_records ADD COLUMN correctOptionIndex INTEGER")
                db.execSQL("ALTER TABLE answer_records ADD COLUMN selectedOptionIndex INTEGER")
                db.execSQL("ALTER TABLE answer_records ADD COLUMN explanation TEXT")
                db.execSQL("ALTER TABLE answer_records ADD COLUMN kit TEXT")
            }
        }

        /** Adds the round's mode and per-answer records, keeping existing history. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quiz_attempts ADD COLUMN mode TEXT NOT NULL DEFAULT 'STANDARD'")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `answer_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`attemptId` TEXT NOT NULL, `category` TEXT NOT NULL, `correct` INTEGER NOT NULL)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_answer_records_category` ON `answer_records` (`category`)")
            }
        }
    }
}
