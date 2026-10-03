#!/usr/bin/env python3
"""
Builds the initial clubs.db seed / deltas_v1.json for the Football Club Quiz app.

Sources:
  - Wikidata (SPARQL): official name, nickname, manager, stadium, capacity, founded year
    and city. This is the primary source.
  - scripts/club_kits.py: hand-curated home kit colours (the app draws kits and shields from
    these; club crests are never used, as they're trademarked).
  - TheSportsDB (REST, free tier): fallback for stadium/capacity/founded-year only — its
    free tier no longer returns badge or manager fields (Patreon-gated since 2024), and its
    `lookup_all_teams` league endpoint proved unreliable, so it is used defensively, per
    club, not as the roster source.

Roster: league membership changes every season (promotion/relegation), and Wikidata's per-club
"current league" property (P118) lags and is sometimes wrong or even attached to non-club items
(players, season articles). So the rosters below are hardcoded, manually-verified lists for the
2026-27 season (cross-checked against the stadium tables on Wikipedia's 2026-27 Premier League,
EFL Championship, EFL League One, EFL League Two and National League season pages) — update them each summer after the transfer window, then rerun this script.

Output (matches DeltaResponseDto / ClubDeltaDto in
app/src/main/java/com/makn/footballquiz/data/remote/dto/):
  - docs/data/version.json
  - docs/data/deltas_v1.json

Run: python3 scripts/fetch_initial_data.py
     python3 scripts/fetch_initial_data.py --leagues "League One,League Two" --clubs-only out.json
       (fetches just those leagues and writes only the club list to out.json — for building an
       incremental deltas_v{N}.json by hand without overwriting the v1 bootstrap)
"""

import argparse
import json
import re
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

from club_kits import KITS

WIKIDATA_SPARQL_ENDPOINT = "https://query.wikidata.org/sparql"
WIKIDATA_SEARCH_ENDPOINT = "https://www.wikidata.org/w/api.php"
THESPORTSDB_SEARCH = "https://www.thesportsdb.com/api/v1/json/3/searchteams.php"
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

# 2026-27 EFL League One roster (24 clubs). Update after each promotion/relegation cycle.
LEAGUE_ONE_CLUBS = [
    "AFC Wimbledon",
    "Barnsley F.C.",
    "Blackpool F.C.",
    "Bradford City A.F.C.",
    "Bromley F.C.",
    "Burton Albion F.C.",
    "Cambridge United F.C.",
    "Doncaster Rovers F.C.",
    "Huddersfield Town A.F.C.",
    "Leicester City F.C.",
    "Leyton Orient F.C.",
    "Luton Town F.C.",
    "Mansfield Town F.C.",
    "Milton Keynes Dons F.C.",
    "Notts County F.C.",
    "Oxford United F.C.",
    "Peterborough United F.C.",
    "Plymouth Argyle F.C.",
    "Reading F.C.",
    "Sheffield Wednesday F.C.",
    "Stevenage F.C.",
    "Stockport County F.C.",
    "Wigan Athletic F.C.",
    "Wycombe Wanderers F.C.",
]

# 2026-27 EFL League Two roster (24 clubs). Update after each promotion/relegation cycle.
LEAGUE_TWO_CLUBS = [
    "Accrington Stanley F.C.",
    "Barnet F.C.",
    "Bristol Rovers F.C.",
    "Cheltenham Town F.C.",
    "Chesterfield F.C.",
    "Colchester United F.C.",
    "Crawley Town F.C.",
    "Crewe Alexandra F.C.",
    "Exeter City F.C.",
    "Fleetwood Town F.C.",
    "Gillingham F.C.",
    "Grimsby Town F.C.",
    "Newport County A.F.C.",
    "Northampton Town F.C.",
    "Oldham Athletic A.F.C.",
    "Port Vale F.C.",
    "Rochdale A.F.C.",
    "Rotherham United F.C.",
    "Salford City F.C.",
    "Shrewsbury Town F.C.",
    "Swindon Town F.C.",
    "Tranmere Rovers F.C.",
    "Walsall F.C.",
    "York City F.C.",
]

# 2026-27 National League roster (24 clubs, tier 5). Update after each promotion/relegation cycle.
NATIONAL_LEAGUE_CLUBS = [
    "AFC Fylde",
    "Aldershot Town F.C.",
    "Altrincham F.C.",
    "Barrow A.F.C.",
    "Boreham Wood F.C.",
    "Boston United F.C.",
    "Carlisle United F.C.",
    "Eastleigh F.C.",
    "FC Halifax Town",
    "Forest Green Rovers F.C.",
    "Gateshead F.C.",
    "Harrogate Town A.F.C.",
    "Hartlepool United F.C.",
    "Hornchurch F.C.",
    "Kidderminster Harriers F.C.",
    "Scunthorpe United F.C.",
    "Solihull Moors F.C.",
    "Southend United F.C.",
    "Sutton United F.C.",
    "Tamworth F.C.",
    "Wealdstone F.C.",
    "Woking F.C.",
    "Worthing F.C.",
    "Yeovil Town F.C.",
]

ROSTERS: dict[str, list[str]] = {
    "Premier League": PREMIER_LEAGUE_CLUBS,
    "Championship": CHAMPIONSHIP_CLUBS,
    "League One": LEAGUE_ONE_CLUBS,
    "League Two": LEAGUE_TWO_CLUBS,
    "National League": NATIONAL_LEAGUE_CLUBS,
}

SPARQL_DETAILS_QUERY_TEMPLATE = """
SELECT ?club ?clubLabel ?venueLabel ?cityLabel
       (SAMPLE(?capacity) AS ?capacitySample)
       (SAMPLE(?founded) AS ?foundedSample)
       (GROUP_CONCAT(DISTINCT ?nickname; separator="|") AS ?nicknames)
WHERE {{
  VALUES ?club {{ {qids} }}
  OPTIONAL {{ ?club wdt:P1449 ?nickname . FILTER(LANG(?nickname) = "en") }}
  OPTIONAL {{
    ?club wdt:P115 ?venue .
    OPTIONAL {{ ?venue wdt:P1083 ?capacity . }}
  }}
  OPTIONAL {{ ?club wdt:P571 ?founded . }}
  OPTIONAL {{ ?club wdt:P159 ?city . }}
  SERVICE wikibase:label {{ bd:serviceParam wikibase:language "en". }}
}}
GROUP BY ?club ?clubLabel ?venueLabel ?cityLabel
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

    # Club crests are trademarked, so the app never shows them: it draws its own shield and kit
    # from the plain colours in club_kits.py instead.
    club_id = slugify(official_name)
    pattern, primary, secondary, shorts = KITS[club_id]

    return {
        "id": club_id,
        "name": club_label,
        "shortName": short_name_from_official(official_name),
        "nickname": nickname,
        "stadiumName": stadium_name,
        "stadiumCapacity": stadium_capacity,
        "foundedYear": founded_year,
        "city": city,
        "badgeDrawableName": None,
        "badgeRemoteUrl": None,
        "version": 1,
        "manager": manager or "Unknown",
        "league": league,
        "kitPattern": pattern,
        "kitPrimary": primary,
        "kitSecondary": secondary,
        "kitShorts": shorts,
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--leagues", help="Comma-separated subset of ROSTERS keys (default: all)")
    parser.add_argument("--clubs-only", type=Path, help="Write just the clubs list here instead of v1")
    args = parser.parse_args()

    leagues = [l.strip() for l in args.leagues.split(",")] if args.leagues else list(ROSTERS)
    unknown = [l for l in leagues if l not in ROSTERS]
    if unknown:
        parser.error(f"unknown league(s): {unknown}; expected any of {list(ROSTERS)}")
    roster: list[tuple[str, str]] = [(name, league) for league in leagues for name in ROSTERS[league]]
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

    if args.clubs_only:
        args.clubs_only.write_text(json.dumps(clubs, indent=2, ensure_ascii=False))
        print(f"\nWrote {len(clubs)} clubs to {args.clubs_only}")
        return

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
