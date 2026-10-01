#!/usr/bin/env python3
"""
Builds app/src/main/assets/database/clubs.db for
QuizDatabase.createFromAsset("database/clubs.db").

Applies every docs/data/deltas_v{N}.json in version order (upserts, then deletions), so the
bundled seed matches what a device would hold after syncing up to latestVersion.

Schema and identity hash come straight from Room's exported schema JSON
(app/schemas/com.ruflo.footballquiz.data.local.QuizDatabase/<version>.json, written by KSP on
every build). Using Room's own createSql + setupQueries means the tables match exactly and
`room_master_table` carries the identity hash Room validates on open — so this file is
shippable as-is, no device/emulator round-trip needed.

Prerequisite: build the app once after any schema change (`./gradlew assembleDebug`) so the
schema JSON for the current QuizDatabase version exists.

Run: python3 scripts/build_clubs_db.py
"""

import json
import re
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = ROOT / "docs" / "data"
SCHEMA_DIR = ROOT / "app" / "schemas" / "com.ruflo.footballquiz.data.local.QuizDatabase"
OUTPUT_PATH = ROOT / "app" / "src" / "main" / "assets" / "database" / "clubs.db"

DATABASE_VERSION = 3

CLUB_COLUMNS = [
    "id", "name", "shortName", "nickname", "stadiumName", "stadiumCapacity",
    "foundedYear", "city", "badgeDrawableName", "badgeRemoteUrl", "version", "manager", "league", "badgeQuizUrl",
]

QUESTION_COLUMNS = [
    "id", "questionText", "category", "correctAnswer", "wrongAnswers",
    "explanation", "imageUriOrUrl", "version",
]


def load_schema() -> dict:
    schema_path = SCHEMA_DIR / f"{DATABASE_VERSION}.json"
    if not schema_path.exists():
        raise SystemExit(f"{schema_path} not found — build the app once so KSP exports it.")
    database = json.loads(schema_path.read_text())["database"]
    if database["version"] != DATABASE_VERSION:
        raise SystemExit(f"Schema version {database['version']} != DATABASE_VERSION {DATABASE_VERSION}")
    return database


def delta_files() -> list[Path]:
    latest = json.loads((DATA_DIR / "version.json").read_text())["latestVersion"]
    files = [DATA_DIR / f"deltas_v{n}.json" for n in range(1, latest + 1)]
    missing = [f.name for f in files if not f.exists()]
    if missing:
        raise SystemExit(f"Missing delta files: {missing}")
    return files


def upsert(conn: sqlite3.Connection, table: str, columns: list[str], rows: list[tuple]) -> None:
    conn.executemany(
        f"INSERT OR REPLACE INTO {table} ({', '.join(columns)}) VALUES ({', '.join('?' * len(columns))})",
        rows,
    )


def main() -> None:
    schema = load_schema()

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.unlink(missing_ok=True)

    conn = sqlite3.connect(OUTPUT_PATH)
    try:
        for entity in schema["entities"]:
            conn.execute(entity["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
            for index in entity.get("indices", []):
                conn.execute(index["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
        for query in schema["setupQueries"]:
            conn.execute(query)

        for path in delta_files():
            deltas = json.loads(path.read_text())
            if deltas["version"] != int(re.search(r"\d+", path.stem).group()):
                raise SystemExit(f"{path.name}: version field doesn't match filename")

            upsert(conn, "clubs", CLUB_COLUMNS,
                   [tuple(club.get(col) for col in CLUB_COLUMNS) for club in deltas["clubs"]])
            upsert(conn, "custom_questions", QUESTION_COLUMNS, [
                tuple(json.dumps(q[col]) if col == "wrongAnswers" else q[col] for col in QUESTION_COLUMNS)
                for q in deltas["customQuestions"]
            ])
            conn.executemany("DELETE FROM clubs WHERE id = ?", [(i,) for i in deltas["deletedClubIds"]])
            conn.executemany("DELETE FROM custom_questions WHERE id = ?",
                             [(i,) for i in deltas["deletedQuestionIds"]])
            print(f"Applied {path.name}")

        conn.execute(f"PRAGMA user_version = {DATABASE_VERSION}")
        conn.commit()

        clubs = conn.execute("SELECT COUNT(*) FROM clubs").fetchone()[0]
        questions = conn.execute("SELECT COUNT(*) FROM custom_questions").fetchone()[0]
    finally:
        conn.close()

    print(f"Wrote {clubs} clubs and {questions} custom questions to {OUTPUT_PATH} "
          f"(identity hash {schema['identityHash']})")


if __name__ == "__main__":
    main()
