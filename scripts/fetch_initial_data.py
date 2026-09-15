#!/usr/bin/env python3
"""
Builds the initial clubs.db seed / deltas_v1.json for the Football Club Quiz app.

Sources:
  - Wikidata (SPARQL): official name, nickname, manager, stadium, capacity, founded year,
    city, and crest image (via Wikimedia Commons). This is the primary source.
  - TheSportsDB (REST, free tier): fallback for stadium/capacity/founded-year only — its
    free tier no longer returns badge or manager fields (Patreon-gated since 2024), and its
    `lookup_all_teams` league endpoint proved unreliable, so it is used defensively, per
    club, not as the roster source.

Roster: league membership changes every season (promotion/relegation), and Wikidata's per-club
"current league" property (P118) lags and is sometimes wrong or even attached to non-club items
(players, season articles). So the rosters below are hardcoded, manually-verified lists for the
2026-27 season (cross-checked against Wikipedia's 2026-27 Premier League / EFL Championship
season pages) — update them each summer after the transfer window, then rerun this script.

Output (matches DeltaResponseDto / ClubDeltaDto in
app/src/main/java/com/ruflo/footballquiz/data/remote/dto/):
  - docs/data/version.json
  - docs/data/deltas_v1.json

Run: python3 scripts/fetch_initial_data.py
"""

import json
import re
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

WIKIDATA_SPARQL_ENDPOINT = "https://query.wikidata.org/sparql"
WIKIDATA_SEARCH_ENDPOINT = "https://www.wikidata.org/w/api.php"
THESPORTSDB_SEARCH = "https://www.thesportsdb.com/api/v1/json/3/searchteams.php"
COMMONS_FILEPATH = "https://commons.wikimedia.org/wiki/Special:FilePath/"
USER_AGENT = "FootballClubQuizDataPipeline/1.0 (contact: mathkn@gmail.com)"

OUTPUT_DIR = Path(__file__).resolve().parent.parent / "docs" / "data"

# 2026-27 Premier League roster (20 clubs). Update after each promotion/relegation cycle.
PREMIER_LEAGUE_CLUBS = [
    "Arsenal F.C.",
    "Aston Villa F.C.",
    "AFC Bournemouth",
    "Brentford F.C.",
    "Brighton & Hove Albion F.C.",
    "Chelsea F.C.",
    "Coventry City F.C.",
    "Crystal Palace F.C.",
    "Everton F.C.",
    "Fulham F.C.",
    "Hull City A.F.C.",
    "Ipswich Town F.C.",
    "Leeds United F.C.",
    "Liverpool F.C.",
    "Manchester City F.C.",
    "Manchester United F.C.",
    "Newcastle United F.C.",
    "Nottingham Forest F.C.",
    "Sunderland A.F.C.",
    "Tottenham Hotspur F.C.",
]

# 2026-27 EFL Championship roster (24 clubs). Update after each promotion/relegation cycle.
CHAMPIONSHIP_CLUBS = [
    "Birmingham City F.C.",
    "Blackburn Rovers F.C.",
    "Bolton Wanderers F.C.",
    "Bristol City F.C.",
    "Burnley F.C.",
    "Cardiff City F.C.",
    "Charlton Athletic F.C.",
    "Derby County F.C.",
    "Lincoln City F.C.",
    "Middlesbrough F.C.",
    "Millwall F.C.",
    "Norwich City F.C.",
    "Portsmouth F.C.",
    "Preston North End F.C.",
    "Queens Park Rangers F.C.",
    "Sheffield United F.C.",
    "Southampton F.C.",
    "Stoke City F.C.",
    "Swansea City A.F.C.",
    "Watford F.C.",
    "West Bromwich Albion F.C.",
    "West Ham United F.C.",
    "Wolverhampton Wanderers F.C.",
    "Wrexham A.F.C.",
]

SPARQL_DETAILS_QUERY_TEMPLATE = """
SELECT ?club ?clubLabel ?venueLabel ?cityLabel ?logo
       (SAMPLE(?capacity) AS ?capacitySample)
       (SAMPLE(?founded) AS ?foundedSample)
       (GROUP_CONCAT(DISTINCT ?nickname; separator="|") AS ?nicknames)
WHERE {{
  VALUES ?club {{ {qids} }}
  OPTIONAL {{ ?club wdt:P154 ?logo . }}
  OPTIONAL {{ ?club wdt:P1449 ?nickname . FILTER(LANG(?nickname) = "en") }}
  OPTIONAL {{
    ?club wdt:P115 ?venue .
    OPTIONAL {{ ?venue wdt:P1083 ?capacity . }}
  }}
  OPTIONAL {{ ?club wdt:P571 ?founded . }}
  OPTIONAL {{ ?club wdt:P159 ?city . }}
  SERVICE wikibase:label {{ bd:serviceParam wikibase:language "en". }}
}}
GROUP BY ?club ?clubLabel ?venueLabel ?cityLabel ?logo
"""

# P286 (head coach) accumulates every past manager with no "preferred rank" set on most clubs,
# so a plain wdt:P286 + SAMPLE() picks an arbitrary one from the club's entire managerial
# history. Querying the statement node directly and keeping only entries with no P582 (end
# time) qualifier isolates the still-current appointment.
SPARQL_CURRENT_MANAGER_QUERY_TEMPLATE = """
SELECT ?club ?managerLabel ?startTime WHERE {{
  VALUES ?club {{ {qids} }}
  ?club p:P286 ?coachStatement .
  ?coachStatement ps:P286 ?manager .
  FILTER NOT EXISTS {{ ?coachStatement pq:P582 ?endTime . }}
  OPTIONAL {{ ?coachStatement pq:P580 ?startTime . }}
  SERVICE wikibase:label {{ bd:serviceParam wikibase:language "en". }}
}}
ORDER BY ?club DESC(?startTime)
"""

CLUB_SUFFIX_RE = re.compile(r"\s+(A\.?F\.?C\.?|F\.?C\.?)$", re.IGNORECASE)


def http_get_json(url: str, timeout: float = 10, retries: int = 3) -> dict:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT, "Accept": "application/json"})
    last_error: Exception | None = None
    for attempt in range(1, retries + 1):
        try:
            with urllib.request.urlopen(request, timeout=timeout) as response:
                return json.loads(response.read().decode("utf-8"))
        except (urllib.error.HTTPError, urllib.error.URLError, TimeoutError) as exc:
            last_error = exc
            if attempt < retries:
                time.sleep(2 * attempt)
    raise last_error


def resolve_qid(club_name: str) -> str | None:
    params = {
        "action": "wbsearchentities",
        "search": club_name,
        "language": "en",
        "type": "item",
        "format": "json",
        "limit": "1",
    }
    data = http_get_json(f"{WIKIDATA_SEARCH_ENDPOINT}?{urllib.parse.urlencode(params)}")
    hits = data.get("search") or []
    return hits[0]["id"] if hits else None


def slugify(name: str) -> str:
    slug = name.lower().replace(".", "")
    slug = re.sub(r"[^a-z0-9]+", "-", slug).strip("-")
    return slug


def short_name_from_official(official_name: str) -> str:
    return CLUB_SUFFIX_RE.sub("", official_name).strip()


def value_or_none(binding: dict, key: str) -> str | None:
    entry = binding.get(key)
    return entry["value"] if entry else None


def parse_year(iso_date: str | None) -> int:
    if not iso_date:
        return 0
    return int(iso_date[:4])


def commons_filename_to_url(file_uri: str | None) -> str | None:
    if not file_uri:
        return None
    filename = urllib.parse.unquote(file_uri.rsplit("/", 1)[-1])
    return COMMONS_FILEPATH + urllib.parse.quote(filename)


def fetch_sportsdb_team(name: str) -> dict:
    # TheSportsDB's search is a literal-ish match: it fails outright on the full official name
    # (e.g. "Sunderland A.F.C." -> no results, "Sunderland" -> match) and "&" throws it onto an
    # unrelated team (Brighton's women's side), so normalize before searching.
    search_term = name.replace("&", "and")
    url = f"{THESPORTSDB_SEARCH}?{urllib.parse.urlencode({'t': search_term})}"
    try:
        data = http_get_json(url, timeout=10, retries=2)
    except Exception as exc:  # noqa: BLE001 - fallback source, must not abort the run
        print(f"  [warn] TheSportsDB lookup failed for {name!r}: {exc}")
        return {}
    teams = data.get("teams") or []
    for team in teams:
        if team.get("strSport") == "Soccer" and team.get("strGender", "Male") == "Male":
            return team
    return {}


def sportsdb_city(team: dict) -> str:
    location = team.get("strLocation") or ""
    parts = [p.strip() for p in location.split(",") if p.strip()]
    if len(parts) >= 2:
        return parts[-2] if parts[-1].lower() == "england" else parts[-1]
    return parts[0] if parts else ""


def fetch_wikidata_details(qids: list[str]) -> dict[str, dict]:
    query = SPARQL_DETAILS_QUERY_TEMPLATE.format(qids=" ".join(f"wd:{qid}" for qid in qids))
    url = f"{WIKIDATA_SPARQL_ENDPOINT}?{urllib.parse.urlencode({'query': query, 'format': 'json'})}"
    data = http_get_json(url, timeout=60, retries=3)
    by_qid = {}
    for row in data["results"]["bindings"]:
        qid = row["club"]["value"].rsplit("/", 1)[-1]
        by_qid[qid] = row
    return by_qid


def fetch_current_managers(qids: list[str]) -> dict[str, str]:
    query = SPARQL_CURRENT_MANAGER_QUERY_TEMPLATE.format(qids=" ".join(f"wd:{qid}" for qid in qids))
    url = f"{WIKIDATA_SPARQL_ENDPOINT}?{urllib.parse.urlencode({'query': query, 'format': 'json'})}"
    data = http_get_json(url, timeout=60, retries=3)
    current_manager_by_qid: dict[str, str] = {}
    for row in data["results"]["bindings"]:
        qid = row["club"]["value"].rsplit("/", 1)[-1]
        # Rows arrive sorted by startTime desc within each club, so the first hit per club wins.
        current_manager_by_qid.setdefault(qid, row["managerLabel"]["value"])
    return current_manager_by_qid


def build_club(official_name: str, wikidata_row: dict | None, manager: str, league: str) -> dict:
    sportsdb_team = fetch_sportsdb_team(short_name_from_official(official_name))
    row = wikidata_row or {}

    club_label = value_or_none(row, "clubLabel") or official_name

    nicknames_raw = value_or_none(row, "nicknames") or ""
    nickname = next((n.strip() for n in nicknames_raw.split("|") if n.strip()), "")
    if not nickname:
        keywords = sportsdb_team.get("strKeywords") or ""
        nickname = next((n.strip() for n in keywords.split(",") if n.strip()), "")

    stadium_name = value_or_none(row, "venueLabel") or sportsdb_team.get("strStadium") or ""

    capacity_raw = value_or_none(row, "capacitySample")
    sportsdb_capacity = sportsdb_team.get("intStadiumCapacity")
    stadium_capacity = (
        int(float(capacity_raw)) if capacity_raw else (int(sportsdb_capacity) if sportsdb_capacity else 0)
    )

    founded_year = parse_year(value_or_none(row, "foundedSample"))
    if not founded_year and sportsdb_team.get("intFormedYear"):
        founded_year = int(sportsdb_team["intFormedYear"])

    city = value_or_none(row, "cityLabel") or sportsdb_city(sportsdb_team)

    # Club crests are trademarked, so Wikimedia Commons (the only free-licensed image source
    # queried here) hosts almost none of them — badgeRemoteUrl is a legitimate best-effort,
    # expected to stay null for most clubs. badgeDrawableName instead names the bundled vector
    # drawable an artist is expected to add at res/drawable/<name>.xml (see App Architecture spec).
    club_id = slugify(official_name)
    badge_remote_url = commons_filename_to_url(value_or_none(row, "logo"))
    badge_drawable_name = f"badge_{club_id.replace('-', '_')}"

    return {
        "id": club_id,
        "name": club_label,
        "shortName": short_name_from_official(official_name),
        "nickname": nickname,
        "stadiumName": stadium_name,
        "stadiumCapacity": stadium_capacity,
        "foundedYear": founded_year,
        "city": city,
        "badgeDrawableName": badge_drawable_name,
        "badgeRemoteUrl": badge_remote_url,
        "version": 1,
        "manager": manager or "Unknown",
        "league": league,
    }


def main() -> None:
    roster: list[tuple[str, str]] = (
        [(name, "Premier League") for name in PREMIER_LEAGUE_CLUBS]
        + [(name, "Championship") for name in CHAMPIONSHIP_CLUBS]
    )
    league_by_name = dict(roster)

    print(f"Resolving Wikidata QIDs for {len(roster)} clubs...", flush=True)
    qids: dict[str, str | None] = {}
    for name, _league in roster:
        qid = resolve_qid(name)
        qids[name] = qid
        print(f"  {name} -> {qid}", flush=True)
        time.sleep(0.2)

    resolved_qids = [qid for qid in qids.values() if qid]
    print(f"\nFetching Wikidata details for {len(resolved_qids)} resolved clubs...", flush=True)
    details_by_qid = fetch_wikidata_details(resolved_qids) if resolved_qids else {}

    print(f"Fetching current managers for {len(resolved_qids)} resolved clubs...", flush=True)
    manager_by_qid = fetch_current_managers(resolved_qids) if resolved_qids else {}

    clubs = []
    for i, (name, _league) in enumerate(roster, start=1):
        qid = qids[name]
        print(f"[{i}/{len(roster)}] building {name!r} (enriching via TheSportsDB)...", flush=True)
        clubs.append(
            build_club(
                name,
                details_by_qid.get(qid) if qid else None,
                manager_by_qid.get(qid, ""),
                league_by_name[name],
            )
        )
        time.sleep(0.2)

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    version_payload = {
        "latestVersion": 1,
        "minSupportedVersion": 1,
        "updatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
    }
    (OUTPUT_DIR / "version.json").write_text(json.dumps(version_payload, indent=2, ensure_ascii=False))

    deltas_payload = {
        "version": 1,
        "clubs": clubs,
        "customQuestions": [],
        "deletedClubIds": [],
        "deletedQuestionIds": [],
    }
    (OUTPUT_DIR / "deltas_v1.json").write_text(json.dumps(deltas_payload, indent=2, ensure_ascii=False))

    print(f"\nWrote {len(clubs)} clubs to {OUTPUT_DIR / 'deltas_v1.json'}")
    print(f"Wrote {OUTPUT_DIR / 'version.json'}")


if __name__ == "__main__":
    main()
