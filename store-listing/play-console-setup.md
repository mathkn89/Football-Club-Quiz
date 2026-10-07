# Play Console setup – Football Club Quiz

Work through the sections in order. Texts for the store listing are in [listing.md](listing.md),
graphics in [graphics/](graphics) and [screenshots/](screenshots).

**Upload file:** `app/build/outputs/bundle/release/app-release.aab` (version 1.0.0, code 1),
signed with the upload key in `release-signing/`. From Windows:
`\\wsl$\<distro>\home\mathias\projects\Football Club Quiz\app\build\outputs\bundle\release\`
Rebuild with `./gradlew bundleRelease`; bump `versionCode` in `app/build.gradle.kts` for every new upload.

---

## 1. Create app (Home → Create app)
| Field | Value |
|---|---|
| App name | `Football Club Quiz` |
| Default language | **English (United Kingdom) – en-GB** |
| App or game | **Game** |
| Free or paid | **Free** (the "Remove ads" purchase is an in-app product) |
| Declarations | Tick both: Developer Program Policies, US export laws |

The package name `com.makn.footballquiz` is fixed by the first upload and can never change.

## 2. App content (Policy → App content)
| Section | Answer |
|---|---|
| Privacy policy | `https://mathkn89.github.io/Football-Club-Quiz/privacy.html` |
| App access | **All functionality is available without any access restrictions** |
| Ads | **Yes, my app contains ads** |
| Content rating | See 2a |
| Target audience | See 2b |
| Data safety | See 2c |
| Advertising ID | **Yes**. Purposes: Advertising or marketing, Analytics, Fraud prevention/security |
| News app | **No** |
| Government app | **No** |
| Financial features | **My app doesn't provide any financial features** |
| Health | **My app does not have any health features** |

### 2a. Content rating (IARC questionnaire)
- Email: `mathias@riebe.no`
- Category: **Game** (all other categories → trivia/quiz is a game)
- Violence, fear, sexuality, language, controlled substances, crude humour, gambling (real or simulated): **No** to all
- Users interact or exchange content: **No** (2-player mode is on one phone, sharing is a text via Android's share sheet)
- Shares user's location: **No**
- Purchase of digital goods: **Yes**
- Web browser / search engine: **No**

Expected result: PEGI 3 / Everyone / USK 0.

### 2b. Target audience and content
- Age groups: **13–15, 16–17, 18 and over**. Do **not** tick under 13 – that puts the app under
  the Families policy, which needs child-directed ad settings the app doesn't use.
- Could the app unintentionally appeal to children: **No** (plain quiz about real football clubs, no cartoon characters).

### 2c. Data safety
Data the app keeps on the phone (name, history, settings) is not "collected" in Play's sense.
Only the AdMob SDK collects data. Matches Google's own guidance:
https://developers.google.com/admob/android/privacy/play-data-disclosure

| Question | Answer |
|---|---|
| Collects or shares required user data | **Yes** |
| All data encrypted in transit | **Yes** |
| Account creation | **My app does not allow users to create an account** |
| Users can request data deletion | **No** (no accounts; all app data is on the phone and deleted on uninstall) |

Data types – tick these four, each **Collected: Yes, Shared: Yes, not ephemeral, required**:

| Data type | Purposes (collected and shared) |
|---|---|
| Location → **Approximate location** | Advertising or marketing, Analytics, Fraud prevention/security/compliance |
| App activity → **App interactions** | Advertising or marketing, Analytics, Fraud prevention/security/compliance |
| App info and performance → **Diagnostics** | Analytics, Fraud prevention/security/compliance |
| Device or other IDs → **Device or other IDs** | Advertising or marketing, Analytics, Fraud prevention/security/compliance |

Leave everything else (personal info, financial info, messages, photos, contacts, etc.) unticked.
Payments are handled by Google Play, so purchase history is not declared.

## 3. Store settings (Grow → Store presence → Store settings)
| Field | Value |
|---|---|
| App category | **Game → Trivia** |
| Tags (up to 5) | Trivia, Sports, Football/Soccer (pick the closest ones Play offers) |
| Email | `mathias@riebe.no` |
| Phone | leave empty |
| Website | leave empty for now (see 8, app-ads.txt) |
| External marketing | On |

## 4. Main store listing (Grow → Store presence → Main store listing)
- App name, short and full description per language: copy from [listing.md](listing.md).
  Add German, French, Norwegian, Swedish via **Manage translations → Add your own translations**.
- App icon: `graphics/icon-512.png`
- Feature graphic: `graphics/feature-graphic-<lang>.png` (one per language)
- Phone screenshots: `screenshots/<lang>/1.png … 6.png` (other languages: `1_DE.png`, `1_FR.png`, `1_NO.png`, `1_SE.png` …), in that order
- Tablet screenshots, video: skip

## 5. Closed test (Test and release → Testing → Closed testing)
New personal accounts must run a closed test with **at least 12 testers opted in for 14 days in a row**
before production access can be requested.

1. **Create track** (or use the default "Closed testing – Alpha").
2. **Countries/regions:** all countries (or at least where your testers live).
3. **Testers:** Create email list "Football Club Quiz testers" with 12+ Gmail addresses
   (aim for 15–20 so dropouts don't reset the count). Feedback URL or email: `mathias@riebe.no`.
4. **Create release:**
   - App signing: accept **Google-generated app signing key** (default). Your `release-signing/`
     key stays the upload key – back it up, you need it for every update.
   - Upload `app-release.aab`.
   - Release name: `1.0.0 (1)`
   - Release notes: copy per language from [listing.md](listing.md).
   - Upload the deobfuscation file when Play warns about it:
     `app/build/outputs/mapping/release/mapping.txt` (App bundle explorer → the version → Downloads/Assets).
5. **Review and roll out**, then **Publishing overview → Send for review**. First review can take a few days.
6. When approved, copy the **opt-in link** from the Testers tab and send it to the testers. They must
   accept with the Gmail address on the list, install from Play and keep it installed for the 14 days.
7. After 14 days: **Dashboard → Apply for production**, answer the questions about the test.

## 6. "Remove ads" product (Monetize with Play → Products → One-time products)
Needs a **payments profile** first (Setup → Payments profile) and the AAB uploaded to a track
(step 5.4), because the product page only opens once Play has seen the billing library.

| Field | Value |
|---|---|
| Product ID | `remove_ads` (must be exactly this – the app looks it up) |
| Name (en) | `Remove ads` |
| Description (en) | `Removes all banner and full-screen ads, for good. The optional Survival second-chance video stays available.` |
| Price | e.g. **USD 2.99** → "Update exchange rates" to set the other countries (≈ NOK 35, EUR 2.99, SEK 35) |
| Purchase option | Buy, not consumable (one-time) |
| Status | **Active** |

Translations:

| Language | Name | Description |
|---|---|---|
| German | `Werbung entfernen` | `Entfernt alle Banner- und Vollbildanzeigen dauerhaft. Das optionale Video für eine zweite Chance im Überleben-Modus bleibt verfügbar.` |
| French | `Supprimer les pubs` | `Supprime définitivement toutes les bannières et publicités plein écran. La vidéo facultative de seconde chance en mode Survie reste disponible.` |
| Norwegian | `Fjern reklame` | `Fjerner alle banner- og fullskjermannonser for godt. Den valgfrie videoen for en ny sjanse i Overlevelse er fortsatt tilgjengelig.` |
| Swedish | `Ta bort reklam` | `Tar bort alla banner- och helskärmsannonser för gott. Den valfria videon för en andra chans i Överlevnad finns kvar.` |

To test purchases without being charged: Setup → **License testing** → add your own Gmail
(and testers who should test buying).

## 7. AdMob
- Publish the GDPR message: [admob-consent-message.md](admob-consent-message.md)
- After the app is live in Play (also works for closed testing once listed): AdMob → Apps →
  Football Club Quiz → **App settings → Link to app store**, so AdMob can verify the app.

## 8. Later: app-ads.txt (recommended, not blocking)
AdMob checks an `app-ads.txt` file on the website in the Play listing; without it some ad demand is
lower. It must be at the **root** of a domain, e.g. `https://mathkn89.github.io/app-ads.txt`
(a GitHub repo named `mathkn89.github.io`). Content:
```
google.com, pub-3922912607913463, DIRECT, f08c47fec0942fa0
```
Then set Store settings → Website to `https://mathkn89.github.io`.
