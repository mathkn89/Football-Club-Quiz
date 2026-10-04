# AdMob consent message (GDPR)

The consent form players see in the EEA, the UK and Switzerland is created in AdMob, not in the
app. The app already shows whatever is published here (Google UMP SDK), and Settings → Privacy
settings reopens it.

Where: **AdMob → Privacy & messaging → European regulations → Create message**
(direct link: https://apps.admob.com/v2/privacymessaging)

## 1. App
| Field | Value |
|---|---|
| App | **Football Club Quiz** (Android, app ID `ca-app-pub-3922912607913463~9680233170`) |
| Privacy policy URL | `https://mathkn89.github.io/Football-Club-Quiz/privacy.html` |
| App name shown in the form | Football Club Quiz |
| App logo | Upload `store-listing/graphics/icon-512.png` (optional, makes the form look like part of the app) |

## 2. Languages
Default language **English**, then add **German, French, Norwegian (Bokmål) and Swedish**.
Google translates its own template text; you don't write the legal text yourself.

## 3. User consent options
Turn on all three — this is what the EU expects and what the app was tested with:

- [x] **Consent**
- [x] **Do not consent**
- [x] **Manage options**
- [ ] Close (do not consent) — leave off; the explicit "Do not consent" button already covers it

## 4. Targeting
**Countries subject to GDPR (EEA and UK)** — the default. Also tick **Switzerland** if offered.

## 5. Styling (optional — matches the app)
| Element | Value |
|---|---|
| Primary/button colour | `#1B7F3B` (pitch green) |
| Button text | `#FFFFFF` |
| Background | `#FBFBFA` |
| Text | `#17191A` |
| Corner style | Rounded |

## 6. Ad partners
Keep **"Commonly used ad partners"** (the default list). Only change this if you add other ad
networks through mediation later.

## 7. Publish
Click **Publish**. It can take up to an hour before the app picks it up. Test it with the
**release** build (debug builds show Google's test form instead).

---

## Optional: US state regulations message
Same page → **US state regulations** → Create message, for the same app.
This adds a "Do not sell or share my personal information" option for players in US states with
privacy laws (California and others). The app's Settings → Privacy settings shows it automatically
when it applies. Recommended if you publish worldwide; harmless otherwise.

## Optional: IDFA message
Skip — that's for iOS only.
