#!/usr/bin/env python3
"""
Paints the lettering out of club badges for "Which club does this badge belong to?" questions —
most crests spell out the club name, which gives the answer away.

Text regions per club live in scripts/badge_regions.json, set by eye against a 10% grid:

  "arsenal-fc":  [{"rect": [x0, y0, x1, y1]}]                  0..1 badge coordinates
  "aldershot":   [{"ring": [inner, outer]}]                     fractions of the badge radius
                 [{"ring": [inner, outer, start, end]}]         arc only, degrees clockwise from 3 o'clock
                 [{"ring": [...], "center": [cx, cy], "radius": r}]  off-centre circle (0..1 coords)
  any region:    "small_parts": N  → only touch separate shapes under N px (loose letters)
  "bolton":      "skip"                                         badge *is* lettering → no badge question

Each region is filled with a single colour: the dominant colour inside it (lettering is the
minority, so a red ring with white letters becomes a plain red ring), or made transparent when
the lettering sits on transparency. Add "fill": "#rrggbb" or "fill": "clear" to a region to force it.
Clubs missing from the file fall back to automatic detection with easyocr — review those.

Output: docs/data/badges/<club-id>.png (served by GitHub Pages, referenced by
ClubDeltaDto.badgeQuizUrl) and side-by-side review sheets in build/badge-review/.

Needs: pip install opencv-python-headless (+ easyocr, CPU torch is fine, only for the fallback).
Run:   python3 scripts/redact_badges.py [club-id ...]
"""

import json
import sqlite3
import sys
import urllib.request
from pathlib import Path

import cv2
import numpy as np

ROOT = Path(__file__).resolve().parent.parent
DB_PATH = ROOT / "app" / "src" / "main" / "assets" / "database" / "clubs.db"
REGIONS_PATH = Path(__file__).resolve().parent / "badge_regions.json"
OUTPUT_DIR = ROOT / "docs" / "data" / "badges"
CACHE_DIR = ROOT / "build" / "badge-cache"
REVIEW_DIR = ROOT / "build" / "badge-review"
USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/120 Safari/537.36"
SIZE = 512


def load_badge(club_id: str, url: str) -> np.ndarray:
    cached = CACHE_DIR / f"{club_id}.png"
    if not cached.exists():
        CACHE_DIR.mkdir(parents=True, exist_ok=True)
        request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        with urllib.request.urlopen(request, timeout=30) as response:
            cached.write_bytes(response.read())
    image = cv2.imread(str(cached), cv2.IMREAD_UNCHANGED)
    if image.ndim == 2:
        image = cv2.cvtColor(image, cv2.COLOR_GRAY2BGRA)
    elif image.shape[2] == 3:
        image = cv2.cvtColor(image, cv2.COLOR_BGR2BGRA)
    return cv2.resize(image, (SIZE, SIZE), interpolation=cv2.INTER_AREA)


def region_mask(region: dict) -> np.ndarray:
    mask = np.zeros((SIZE, SIZE), bool)
    if "rect" in region:
        x0, y0, x1, y1 = (int(v * SIZE) for v in region["rect"])
        mask[y0:y1, x0:x1] = True
    if "ring" in region:
        inner, outer, *arc = region["ring"]
        start, end = arc or (0, 360)
        cx, cy = (v * SIZE for v in region.get("center", (0.5, 0.5)))
        radius = region.get("radius", 0.5) * SIZE
        yy, xx = np.mgrid[0:SIZE, 0:SIZE]
        dist = np.hypot(xx - cx, yy - cy) / radius
        angle = np.degrees(np.arctan2(yy - cy, xx - cx)) % 360
        in_arc = (angle >= start) & (angle <= end) if start <= end else (angle >= start) | (angle <= end)
        mask |= (dist >= inner) & (dist <= outer) & in_arc
    return mask


def dominant_colour(pixels: np.ndarray) -> np.ndarray:
    """Most common colour among [pixels] (BGR rows), after coarse quantisation."""
    quantised = (pixels // 32).astype(np.int32)
    keys = quantised[:, 0] * 64 + quantised[:, 1] * 8 + quantised[:, 2]
    return pixels[keys == np.bincount(keys).argmax()].mean(axis=0)


def fill(out: np.ndarray, original: np.ndarray, mask: np.ndarray, forced: str | None = None) -> None:
    if not mask.any():
        return
    alpha = original[:, :, 3][mask]
    if forced == "clear" or (forced is None and np.mean(alpha < 128) > 0.5):
        out[mask, 3] = 0
        return
    if forced:
        colour = np.array([int(forced[i:i + 2], 16) for i in (5, 3, 1)], np.float64)  # #rrggbb -> BGR
    else:
        colour = dominant_colour(original[:, :, :3][mask & (original[:, :, 3] >= 128)])
    out[mask, :3] = colour.astype(np.uint8)
    # Lettering is sometimes cut out of the crest (transparent), so also close small holes in
    # the alpha channel — without growing the badge's outer edge.
    closed = cv2.morphologyEx(original[:, :, 3], cv2.MORPH_CLOSE, np.ones((41, 41), np.uint8))
    out[mask, 3] = np.maximum(original[:, :, 3], closed)[mask]


def auto_mask(bgra: np.ndarray) -> np.ndarray:
    """Fallback for clubs without hand-set regions: easyocr text boxes, grown slightly."""
    import easyocr  # only needed here; heavy import

    reader = auto_mask.reader = getattr(auto_mask, "reader", None) or easyocr.Reader(["en"], gpu=False, verbose=False)
    alpha = bgra[:, :, 3:4].astype(np.float32) / 255
    mask = np.zeros((SIZE, SIZE), np.uint8)
    for backdrop in (255, 0):
        flat = (bgra[:, :, :3] * alpha + backdrop * (1 - alpha)).astype(np.uint8)
        horizontal, free = reader.detect(flat, text_threshold=0.6, low_text=0.35, mag_ratio=1.5)
        boxes = [np.array([[x0, y0], [x1, y0], [x1, y1], [x0, y1]]) for x0, x1, y0, y1 in horizontal[0]]
        boxes += [np.array(p) for p in free[0]]
        for box in boxes:
            if cv2.contourArea(box.astype(np.float32)) <= 0.15 * SIZE * SIZE:  # huge = curved-text chord
                cv2.fillPoly(mask, [box.astype(np.int32)], 255)
    return cv2.dilate(mask, np.ones((7, 7), np.uint8)) > 0


def redact(club_id: str, original: np.ndarray, regions: list[dict] | None) -> np.ndarray:
    out = original.copy()
    if regions is None:
        print(f"  {club_id}: no regions in {REGIONS_PATH.name}, using automatic detection — review it")
        count, labels = cv2.connectedComponents(auto_mask(original).astype(np.uint8))
        for label in range(1, count):
            fill(out, original, labels == label)
        return out
    for region in regions:
        mask = region_mask(region)
        if "small_parts" in region:
            # Only separate shapes under N pixels — loose letters, not the artwork they sit near.
            count, labels, stats, _ = cv2.connectedComponentsWithStats((original[:, :, 3] > 40).astype(np.uint8))
            small = np.isin(labels, [i for i in range(1, count) if stats[i, cv2.CC_STAT_AREA] < region["small_parts"]])
            mask &= cv2.dilate(small.astype(np.uint8), np.ones((3, 3), np.uint8)).astype(bool)
        fill(out, original, mask, region.get("fill"))
    return out


def on_checkerboard(bgra: np.ndarray) -> np.ndarray:
    tiles = (np.indices(bgra.shape[:2]).sum(axis=0) // 16 % 2) * 40 + 200
    board = np.repeat(tiles[:, :, None], 3, axis=2).astype(np.float32)
    alpha = bgra[:, :, 3:4].astype(np.float32) / 255
    return (bgra[:, :, :3] * alpha + board * (1 - alpha)).astype(np.uint8)


def write_review_sheets(pairs: list[tuple[str, np.ndarray, np.ndarray]], per_sheet: int = 12) -> None:
    REVIEW_DIR.mkdir(parents=True, exist_ok=True)
    for old in REVIEW_DIR.glob("sheet_*.png"):
        old.unlink()
    thumb = 200
    for start in range(0, len(pairs), per_sheet):
        cells = []
        for club_id, original, redacted in pairs[start:start + per_sheet]:
            pair = np.hstack([cv2.resize(on_checkerboard(img), (thumb, thumb)) for img in (original, redacted)])
            label = np.full((20, 2 * thumb, 3), 255, np.uint8)
            cv2.putText(label, club_id, (4, 15), cv2.FONT_HERSHEY_SIMPLEX, 0.45, (0, 0, 0), 1)
            cells.append(np.vstack([label, pair, np.full((6, 2 * thumb, 3), 255, np.uint8)]))
        while len(cells) % 2:
            cells.append(np.full_like(cells[0], 255))
        rows = [np.hstack([cells[i], np.full((cells[i].shape[0], 12, 3), 255, np.uint8), cells[i + 1]])
                for i in range(0, len(cells), 2)]
        cv2.imwrite(str(REVIEW_DIR / f"sheet_{start // per_sheet + 1:02d}.png"), np.vstack(rows))


def main() -> None:
    only = set(sys.argv[1:])
    all_regions = json.loads(REGIONS_PATH.read_text())
    clubs = sqlite3.connect(DB_PATH).execute("SELECT id, badgeRemoteUrl FROM clubs ORDER BY id").fetchall()
    clubs = [(cid, url) for cid, url in clubs if url and (not only or cid in only)]

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    pairs = []
    for club_id, url in clubs:
        regions = all_regions.get(club_id)
        target = OUTPUT_DIR / f"{club_id}.png"
        if regions == "skip":
            target.unlink(missing_ok=True)
            continue
        original = load_badge(club_id, url)
        redacted = redact(club_id, original, regions)
        cv2.imwrite(str(target), redacted, [cv2.IMWRITE_PNG_COMPRESSION, 9])
        pairs.append((club_id, original, redacted))
    write_review_sheets(pairs)
    print(f"Wrote {len(pairs)} badges to {OUTPUT_DIR}; review sheets in {REVIEW_DIR}")


if __name__ == "__main__":
    main()
