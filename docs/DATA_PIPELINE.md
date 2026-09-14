# Data Pipeline Spec

Static JSON served from GitHub Pages (`docs/` on the default branch). The app polls
`version.json`, then walks `deltas_v{N}.json` one version at a time from its last-applied
version up to `latestVersion`, upserting as it goes (`SyncRepository.sync()`). Endpoints are
consumed by `SyncApiService` and deserialized into `VersionResponseDto` / `DeltaResponseDto`.

Base URL: `BuildConfig.DATA_SYNC_BASE_URL` (`https://<username>.github.io/<repo>/data/`), so these
files must live at `docs/data/version.json` and `docs/data/deltas_v{N}.json`.

## `version.json`

```json
{
  "latestVersion": 1,
  "minSupportedVersion": 1,
  "updatedAt": "2026-09-14T13:25:22Z"
}
```

| Field                | Type   | Notes                                                                 |
|-----------------------|--------|------------------------------------------------------------------------|
| `latestVersion`       | Int    | Highest `N` for which `deltas_v{N}.json` exists.                     |
| `minSupportedVersion` | Int    | Reserved for a future forced-upgrade check; the client doesn't enforce it yet. Only raise it if old deltas are being retired. |
| `updatedAt`            | String | ISO-8601 UTC, `%Y-%m-%dT%H:%M:%SZ`.                                   |

## `deltas_v{N}.json`

```json
{
  "version": 1,
  "clubs": [ ClubDeltaDto, ... ],
  "customQuestions": [ CustomQuestionDeltaDto, ... ],
  "deletedClubIds": ["some-club-id"],
  "deletedQuestionIds": ["some-question-id"]
}
```

`version` must equal the `N` in the filename. `clubs` / `customQuestions` are **upserts** —
rows the client should `@Upsert` by `id`, whether new or changed. Only include rows that
actually changed for this version; this is a diff, not a full snapshot (v1 is the one exception,
since it's the bootstrap payload — see below).

### `ClubDeltaDto`

| Field               | Type     | Notes                                                          |
|---------------------|----------|------------------------------------------------------------------|
| `id`                 | String   | Primary key, slug (`slugify(officialName)`, e.g. `arsenal-fc`). Stable — never change it for an existing club. |
| `name`               | String   | Official name, e.g. `Arsenal F.C.`                              |
| `shortName`          | String   | Official name minus the `F.C./A.F.C.` suffix.                    |
| `nickname`           | String   |                                                                    |
| `stadiumName`        | String   |                                                                    |
| `stadiumCapacity`    | Int      |                                                                    |
| `foundedYear`        | Int      |                                                                    |
| `city`               | String   |                                                                    |
| `badgeDrawableName`  | String?  | Name of a bundled `res/drawable/<name>.xml` an artist adds locally. Prefer this over `badgeRemoteUrl` when both exist — bundled assets don't need network. |
| `badgeRemoteUrl`     | String?  | Fallback when no bundled drawable exists yet. Fetched via Coil (SVG-capable).       |
| `version`            | Int      | Per-row revision counter — bump it whenever you touch this row. Informational only; the client always upserts regardless of this value, so it does not gate whether an update is applied. |
| `manager`            | String   | Current head coach.                                              |

### `CustomQuestionDeltaDto`

| Field           | Type          | Notes                                                                 |
|------------------|---------------|--------------------------------------------------------------------------|
| `id`             | String        | Primary key — UUID or a stable slug.                                    |
| `questionText`   | String        |                                                                            |
| `category`       | String        | Free text. The client maps it onto `QuizCategory` case-insensitively (`HISTORY`, `TRANSFERS`, `RIVALRIES`, `GENERAL`, or any of the dynamic categories); anything unrecognized falls back to `GENERAL`. |
| `correctAnswer`  | String        |                                                                            |
| `wrongAnswers`   | List\<String> | Exactly 3, so the round-trip UI shows 4 options total.                    |
| `explanation`    | String?       | Shown after the player answers.                                          |
| `imageUriOrUrl`  | String?       | Full `http(s)://` URL — rendered as-is, no drawable-name scheme.          |
| `version`        | Int           | Same semantics as `ClubEntity.version` above.                            |

### Deletions

`deletedClubIds` / `deletedQuestionIds` — rows the client should hard-delete. Once an id has
been deleted, don't reuse it for a different club/question.

## Versioning rules

- Versions are a strictly increasing integer sequence starting at `1`. Never renumber or delete
  a previously-published `deltas_v{N}.json` — clients resume mid-walk from whatever version they
  last applied (`SyncPreferences.lastSyncedVersion`), so an old client may fetch `deltas_v3.json`
  long after `v7` is current.
- `deltas_v1.json` is the only full-snapshot delta (it's what a fresh install upserts against an
  empty database on top of the bundled `clubs.db` asset). Every version after that should contain
  only the rows that changed.
- A version bump to `latestVersion` in `version.json` must not go out before the matching
  `deltas_v{N}.json` is already live — clients poll `version.json` first and will 404 on the delta
  if it's missing.

## Publishing an update

1. Pick the next version: `N = current latestVersion + 1`.
2. Write `docs/data/deltas_v{N}.json`:
   - `version: N`
   - only the clubs/questions that changed, each with its own `version` field incremented
   - any ids to remove in `deletedClubIds` / `deletedQuestionIds`
3. Update `docs/data/version.json`: `latestVersion: N`, `updatedAt` to the current UTC time.
4. Validate before pushing:
   - both files are valid JSON
   - `deltas_v{N}.json`'s `version` field matches `N` in its filename
   - updated club/question `id`s match existing rows (or are new, unique, slugified)
   - no id appears in both an upsert list and the matching deleted-ids list
   - every previously-published `deltas_v{M}.json` (`M < N`) is still present and untouched
5. Commit and push to the branch GitHub Pages serves from this repo's `docs/` folder. Pages
   redeploys automatically (usually within a few minutes).
6. Clients pick it up on the next periodic sync (`SyncScheduler.schedulePeriodicSync`, every 12h)
   or immediately if something calls `SyncScheduler.syncNow()`.

### Bootstrapping vs. incremental updates

`scripts/fetch_initial_data.py` regenerates the **v1 bootstrap** payload (full roster, always
writes `version: 1`) from Wikidata/TheSportsDB — it's meant for the initial genesis of the
dataset, not for pushing routine updates. For an incremental update (new season, manager change,
new curated question), hand-edit a new `deltas_v{N}.json` following the diff rules above instead
of rerunning the script.

## Known gap

`app/src/main/assets/database/clubs.db` exists (built from `docs/data/deltas_v1.json` via
`scripts/build_clubs_db.py`), but it's not yet safe to ship: Room validates a `room_master_table`
identity hash on every open, including for a `createFromAsset`-copied file, and only Room itself
can write a hash it will accept. Before a real device/emulator run, regenerate it properly using
`app/src/androidTest/java/.../tools/SeedDatabaseGenerator.kt` — see that file's doc comment for
the exact `connectedAndroidTest` + `adb pull` steps. Not part of this spec; flagging so it doesn't
get missed.
