#!/usr/bin/env python3
"""
Builds app/src/main/assets/database/clubs.db from docs/data/deltas_v1.json, for
QuizDatabase.createFromAsset("database/clubs.db").

Table layout mirrors ClubEntity / CustomQuestionEntity + Converters exactly (see
app/src/main/java/com/ruflo/footballquiz/data/local/{entity,converter}/).

CAVEAT: this writes real schema + data but does NOT (cannot, without Room itself) write a
correct `room_master_table` identity hash. Room validates that hash on every open, including
for a createFromAsset-copied file, so this file alone is expected to fail that check at runtime
(IllegalStateException: "Pre-packaged database has an invalid schema"). To get a hash Room will
accept, regenerate the asset the standard way once Android tooling is available:

  1. Run the app/src/androidTest/.../SeedDatabaseGenerator instrumented test on a device/emulator
     — it builds the same QuizDatabase via plain Room.databaseBuilder (no createFromAsset), so
     Room itself writes a correct room_master_table, then upserts this same deltas_v1.json data
     and copies the finished file to external cache storage.
  2. `adb pull /sdcard/Android/data/com.ruflo.footballquiz/cache/clubs_seed.db <tmp>`
  3. `cp <tmp> app/src/main/assets/database/clubs.db`

Until step 1-3 is done, this script's output is still useful for inspecting the data/schema by
hand (`sqlite3 clubs.db .dump`), just not for shipping.

Run: python3 scripts/build_clubs_db.py
"""

import json
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DELTAS_PATH = ROOT / "docs" / "data" / "deltas_v1.json"
OUTPUT_PATH = ROOT / "app" / "src" / "main" / "assets" / "database" / "clubs.db"

DATABASE_VERSION = 1

CREATE_CLUBS_TABLE = """
CREATE TABLE `clubs` (
    `id` TEXT NOT NULL,
    `name` TEXT NOT NULL,
    `shortName` TEXT NOT NULL,
    `nickname` TEXT NOT NULL,
    `stadiumName` TEXT NOT NULL,
    `stadiumCapacity` INTEGER NOT NULL,
    `foundedYear` INTEGER NOT NULL,
    `city` TEXT NOT NULL,
    `badgeDrawableName` TEXT,
    `badgeRemoteUrl` TEXT,
    `version` INTEGER NOT NULL,
    `manager` TEXT NOT NULL,
    PRIMARY KEY(`id`)
)
"""

CREATE_CUSTOM_QUESTIONS_TABLE = """
CREATE TABLE `custom_questions` (
    `id` TEXT NOT NULL,
    `questionText` TEXT NOT NULL,
    `category` TEXT NOT NULL,
    `correctAnswer` TEXT NOT NULL,
    `wrongAnswers` TEXT NOT NULL,
    `explanation` TEXT,
    `imageUriOrUrl` TEXT,
    `version` INTEGER NOT NULL,
    PRIMARY KEY(`id`)
)
"""

CLUB_COLUMNS = [
    "id", "name", "shortName", "nickname", "stadiumName", "stadiumCapacity",
    "foundedYear", "city", "badgeDrawableName", "badgeRemoteUrl", "version", "manager",
]

QUESTION_COLUMNS = [
    "id", "questionText", "category", "correctAnswer", "wrongAnswers",
    "explanation", "imageUriOrUrl", "version",
]


def main() -> None:
    deltas = json.loads(DELTAS_PATH.read_text())

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.unlink(missing_ok=True)

    conn = sqlite3.connect(OUTPUT_PATH)
    try:
        conn.execute(CREATE_CLUBS_TABLE)
        conn.execute(CREATE_CUSTOM_QUESTIONS_TABLE)

        conn.executemany(
            f"INSERT INTO clubs ({', '.join(CLUB_COLUMNS)}) VALUES ({', '.join('?' * len(CLUB_COLUMNS))})",
            [tuple(club[col] for col in CLUB_COLUMNS) for club in deltas["clubs"]],
        )

        conn.executemany(
            f"INSERT INTO custom_questions ({', '.join(QUESTION_COLUMNS)}) "
            f"VALUES ({', '.join('?' * len(QUESTION_COLUMNS))})",
            [
                tuple(
                    json.dumps(question[col]) if col == "wrongAnswers" else question[col]
                    for col in QUESTION_COLUMNS
                )
                for question in deltas["customQuestions"]
            ],
        )

        conn.execute(f"PRAGMA user_version = {DATABASE_VERSION}")
        conn.commit()
    finally:
        conn.close()

    print(f"Wrote {len(deltas['clubs'])} clubs and {len(deltas['customQuestions'])} custom "
          f"questions to {OUTPUT_PATH}")
    print("Reminder: this file still needs a Room-generated room_master_table identity hash "
          "before it will pass createFromAsset validation on device — see this script's "
          "docstring.")


if __name__ == "__main__":
    main()
