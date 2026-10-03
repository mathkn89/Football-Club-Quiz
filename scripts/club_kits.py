"""
Home kit of every club, as drawn by the app (shirt pattern + colours + shorts). Hand-curated from
each club's traditional home colours, cross-checked against the kit colours in the Wikipedia
club infoboxes. These are plain colour descriptions, not the clubs' kit designs or crests.

Each entry: (pattern, primary, secondary, shorts)
  pattern:   plain | sleeves | stripes | hoops | halves | quarters
             (sleeves = primary body with secondary sleeves)
  colours:   one of COLOURS below — the app maps each name to a fixed RGB value.
  secondary: None for a plain one-colour shirt.

Used by the pipeline to fill ClubDeltaDto.kitPattern / kitPrimary / kitSecondary / kitShorts.
Update when a club changes its home colours (rare; sponsors and trim don't matter here).
"""

COLOURS = {
    "red", "white", "black", "navy", "blue", "sky", "claret",
    "amber", "gold", "yellow", "orange", "green", "lime",
}
PATTERNS = {"plain", "sleeves", "stripes", "hoops", "halves", "quarters"}

KITS: dict[str, tuple[str, str, str | None, str]] = {
    # Premier League
    "arsenal-fc": ("sleeves", "red", "white", "white"),
    "aston-villa-fc": ("sleeves", "claret", "sky", "sky"),
    "afc-bournemouth": ("stripes", "red", "black", "black"),
    "brentford-fc": ("stripes", "red", "white", "black"),
    "brighton-hove-albion-fc": ("stripes", "blue", "white", "blue"),
    "chelsea-fc": ("plain", "blue", None, "blue"),
    "coventry-city-fc": ("plain", "sky", None, "sky"),
    "crystal-palace-fc": ("stripes", "red", "blue", "blue"),
    "everton-fc": ("plain", "blue", None, "white"),
    "fulham-fc": ("plain", "white", None, "black"),
    "hull-city-afc": ("stripes", "amber", "black", "black"),
    "ipswich-town-fc": ("plain", "blue", None, "white"),
    "leeds-united-fc": ("plain", "white", None, "white"),
    "liverpool-fc": ("plain", "red", None, "red"),
    "manchester-city-fc": ("plain", "sky", None, "white"),
    "manchester-united-fc": ("plain", "red", None, "white"),
    "newcastle-united-fc": ("stripes", "black", "white", "black"),
    "nottingham-forest-fc": ("plain", "red", None, "white"),
    "sunderland-afc": ("stripes", "red", "white", "black"),
    "tottenham-hotspur-fc": ("plain", "white", None, "navy"),
    # Championship
    "birmingham-city-fc": ("plain", "blue", None, "blue"),
    "blackburn-rovers-fc": ("halves", "blue", "white", "white"),
    "bolton-wanderers-fc": ("plain", "white", None, "white"),
    "bristol-city-fc": ("plain", "red", None, "white"),
    "burnley-fc": ("sleeves", "claret", "sky", "white"),
    "cardiff-city-fc": ("plain", "blue", None, "white"),
    "charlton-athletic-fc": ("plain", "red", None, "white"),
    "derby-county-fc": ("plain", "white", None, "black"),
    "lincoln-city-fc": ("stripes", "red", "white", "black"),
    "middlesbrough-fc": ("plain", "red", None, "red"),
    "millwall-fc": ("plain", "navy", None, "white"),
    "norwich-city-fc": ("plain", "yellow", None, "green"),
    "portsmouth-fc": ("plain", "blue", None, "white"),
    "preston-north-end-fc": ("plain", "white", None, "navy"),
    "queens-park-rangers-fc": ("hoops", "blue", "white", "white"),
    "sheffield-united-fc": ("stripes", "red", "white", "black"),
    "southampton-fc": ("stripes", "red", "white", "black"),
    "stoke-city-fc": ("stripes", "red", "white", "white"),
    "swansea-city-afc": ("plain", "white", None, "white"),
    "watford-fc": ("plain", "yellow", None, "red"),
    "west-bromwich-albion-fc": ("stripes", "navy", "white", "white"),
    "west-ham-united-fc": ("sleeves", "claret", "sky", "white"),
    "wolverhampton-wanderers-fc": ("plain", "gold", None, "black"),
    "wrexham-afc": ("plain", "red", None, "red"),
    # League One
    "afc-wimbledon": ("plain", "blue", None, "blue"),
    "barnsley-fc": ("plain", "red", None, "red"),
    "blackpool-fc": ("plain", "orange", None, "white"),
    "bradford-city-afc": ("stripes", "claret", "amber", "black"),
    "bromley-fc": ("plain", "white", None, "white"),
    "burton-albion-fc": ("plain", "amber", None, "black"),
    "cambridge-united-fc": ("plain", "amber", None, "black"),
    "doncaster-rovers-fc": ("hoops", "red", "white", "black"),
    "huddersfield-town-afc": ("stripes", "blue", "white", "white"),
    "leicester-city-fc": ("plain", "blue", None, "blue"),
    "leyton-orient-fc": ("plain", "red", None, "red"),
    "luton-town-fc": ("plain", "orange", None, "navy"),
    "mansfield-town-fc": ("plain", "amber", None, "blue"),
    "milton-keynes-dons-fc": ("plain", "white", None, "white"),
    "notts-county-fc": ("stripes", "black", "white", "black"),
    "oxford-united-fc": ("plain", "yellow", None, "navy"),
    "peterborough-united-fc": ("plain", "blue", None, "blue"),
    "plymouth-argyle-fc": ("plain", "green", None, "white"),
    "reading-fc": ("hoops", "blue", "white", "blue"),
    "sheffield-wednesday-fc": ("stripes", "blue", "white", "black"),
    "stevenage-fc": ("sleeves", "white", "red", "red"),
    "stockport-county-fc": ("plain", "blue", None, "white"),
    "wigan-athletic-fc": ("stripes", "blue", "white", "blue"),
    "wycombe-wanderers-fc": ("quarters", "navy", "sky", "navy"),
    # League Two
    "accrington-stanley-fc": ("plain", "red", None, "red"),
    "barnet-fc": ("plain", "orange", None, "black"),
    "bristol-rovers-fc": ("quarters", "blue", "white", "blue"),
    "cheltenham-town-fc": ("stripes", "red", "white", "red"),
    "chesterfield-fc": ("plain", "blue", None, "white"),
    "colchester-united-fc": ("stripes", "blue", "white", "white"),
    "crawley-town-fc": ("plain", "red", None, "red"),
    "crewe-alexandra-fc": ("plain", "red", None, "white"),
    "exeter-city-fc": ("stripes", "red", "white", "black"),
    "fleetwood-town-fc": ("sleeves", "red", "white", "white"),
    "gillingham-fc": ("plain", "blue", None, "white"),
    "grimsby-town-fc": ("stripes", "black", "white", "black"),
    "newport-county-afc": ("plain", "amber", None, "amber"),
    "northampton-town-fc": ("plain", "claret", None, "white"),
    "oldham-athletic-afc": ("plain", "blue", None, "blue"),
    "port-vale-fc": ("plain", "white", None, "black"),
    "rochdale-afc": ("plain", "blue", None, "white"),
    "rotherham-united-fc": ("sleeves", "red", "white", "white"),
    "salford-city-fc": ("plain", "orange", None, "orange"),
    "shrewsbury-town-fc": ("plain", "blue", None, "blue"),
    "swindon-town-fc": ("plain", "red", None, "white"),
    "tranmere-rovers-fc": ("plain", "white", None, "white"),
    "walsall-fc": ("plain", "red", None, "white"),
    "york-city-fc": ("plain", "red", None, "navy"),
    # National League
    "afc-fylde": ("plain", "white", None, "white"),
    "aldershot-town-fc": ("plain", "red", None, "red"),
    "altrincham-fc": ("plain", "red", None, "black"),
    "barrow-afc": ("plain", "white", None, "blue"),
    "boreham-wood-fc": ("plain", "white", None, "white"),
    "boston-united-fc": ("stripes", "amber", "black", "black"),
    "carlisle-united-fc": ("plain", "blue", None, "blue"),
    "eastleigh-fc": ("sleeves", "white", "blue", "blue"),
    "fc-halifax-town": ("plain", "blue", None, "blue"),
    "forest-green-rovers-fc": ("plain", "lime", None, "black"),
    "gateshead-fc": ("plain", "white", None, "black"),
    "harrogate-town-afc": ("sleeves", "yellow", "black", "black"),
    "hartlepool-united-fc": ("plain", "blue", None, "blue"),
    "hornchurch-fc": ("stripes", "red", "white", "red"),
    "kidderminster-harriers-fc": ("halves", "red", "white", "red"),
    "scunthorpe-united-fc": ("sleeves", "claret", "sky", "sky"),
    "solihull-moors-fc": ("sleeves", "yellow", "blue", "blue"),
    "southend-united-fc": ("plain", "navy", None, "navy"),
    "sutton-united-fc": ("plain", "amber", None, "amber"),
    "tamworth-fc": ("stripes", "red", "black", "black"),
    "wealdstone-fc": ("plain", "blue", None, "white"),
    "woking-fc": ("halves", "red", "white", "black"),
    "worthing-fc": ("plain", "red", None, "red"),
    "yeovil-town-fc": ("plain", "green", None, "green"),
}


def validate(club_ids: set[str]) -> None:
    missing = club_ids - KITS.keys()
    extra = KITS.keys() - club_ids
    assert not missing and not extra, f"missing={missing} extra={extra}"
    for club_id, (pattern, primary, secondary, shorts) in KITS.items():
        assert pattern in PATTERNS, club_id
        assert primary in COLOURS and shorts in COLOURS, club_id
        assert (secondary is None) == (pattern == "plain"), club_id
        assert secondary is None or secondary in COLOURS, club_id
