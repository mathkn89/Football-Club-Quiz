package com.ruflo.footballquiz.tools

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ruflo.footballquiz.data.local.QuizDatabase
import com.ruflo.footballquiz.data.remote.dto.DeltaResponseDto
import com.ruflo.footballquiz.data.remote.mapper.toEntity
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Not a real test — a one-time developer tool, run via
 * `./gradlew connectedAndroidTest --tests "*SeedDatabaseGenerator"`, that builds
 * `app/src/main/assets/database/clubs.db` the only way that produces a `room_master_table`
 * identity hash Room will accept at runtime: by having Room itself create the database (see
 * `scripts/build_clubs_db.py`'s docstring for why a hand-built SQLite file isn't enough on its
 * own — that script gets the schema and data right, but only Room can write the matching hash).
 *
 * After running, pull the result off the device/emulator and commit it as the asset:
 *   adb pull /storage/emulated/0/Android/data/com.ruflo.footballquiz/files/clubs_seed.db /tmp/clubs.db
 *   cp /tmp/clubs.db app/src/main/assets/database/clubs.db
 *
 * Reads `deltas_v1.json` from this module's androidTest assets (kept in sync with
 * `docs/data/deltas_v1.json` — copy it over again if that file changes).
 */
@RunWith(AndroidJUnit4::class)
class SeedDatabaseGenerator {

    @Test
    fun generate() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dbName = "clubs_seed_build.db"
        context.deleteDatabase(dbName)

        val database = Room.databaseBuilder(context, QuizDatabase::class.java, dbName).build()
        try {
            val deltasJson = context.assets.open("deltas_v1.json").bufferedReader().use { it.readText() }
            val deltas = Json { ignoreUnknownKeys = true }.decodeFromString<DeltaResponseDto>(deltasJson)
            database.clubDao().upsertAll(deltas.clubs.map { it.toEntity() })
        } finally {
            database.close()
        }

        val builtDbFile = context.getDatabasePath(dbName)
        val outputFile = File(context.getExternalFilesDir(null), "clubs_seed.db")
        builtDbFile.copyTo(outputFile, overwrite = true)

        println("Seed database written to: ${outputFile.absolutePath}")
    }
}
