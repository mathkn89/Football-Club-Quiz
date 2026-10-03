package com.makn.footballquiz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.makn.footballquiz.BuildConfig
import com.makn.footballquiz.data.local.converter.Converters
import com.makn.footballquiz.data.local.dao.ClubDao
import com.makn.footballquiz.data.local.dao.CustomQuestionDao
import com.makn.footballquiz.data.local.entity.ClubEntity
import com.makn.footballquiz.data.local.entity.CustomQuestionEntity
import kotlinx.coroutines.runBlocking

@Database(
    entities = [ClubEntity::class, CustomQuestionEntity::class],
    version = 5,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun clubDao(): ClubDao
    abstract fun customQuestionDao(): CustomQuestionDao

    companion object {
        private const val DATABASE_NAME = "quiz.db"
        private const val ASSET_DATABASE_PATH = "database/clubs.db"

        /** Adds [ClubEntity.badgeQuizUrl]; existing rows get it on the next delta sync. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clubs ADD COLUMN badgeQuizUrl TEXT")
            }
        }

        /** Adds the home-kit columns; existing rows get them on the next delta sync. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf("kitPattern", "kitPrimary", "kitSecondary", "kitShorts").forEach { column ->
                    db.execSQL("ALTER TABLE clubs ADD COLUMN $column TEXT")
                }
            }
        }

        /** Adds translations for curated questions; existing rows get them on the next delta sync. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE custom_questions ADD COLUMN translations TEXT")
            }
        }

        /**
         * Runs only when Room copies the bundled clubs.db (first launch, or after a destructive
         * migration): the copy already contains every delta up to [BuildConfig.SEED_DATA_VERSION],
         * so sync resumes after it instead of replaying older deltas over newer seed data.
         * Upgraded installs keep their own last-synced version and still fetch the newer deltas.
         */
        private class SeedVersionCallback(private val context: Context) : RoomDatabase.PrepackagedDatabaseCallback() {
            override fun onOpenPrepackagedDatabase(db: SupportSQLiteDatabase) {
                runBlocking {
                    val preferences = SyncPreferences(context)
                    if (preferences.getLastSyncedVersion() < BuildConfig.SEED_DATA_VERSION) {
                        preferences.setLastSyncedVersion(BuildConfig.SEED_DATA_VERSION)
                    }
                }
            }
        }

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
                .createFromAsset(ASSET_DATABASE_PATH, SeedVersionCallback(context.applicationContext))
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
    }
}
