# Google Play — Store Listing (English)

Copy each block into the matching field in Play Console → **Grow → Store presence → Main store listing**.
Character limits are noted; the counts given are the actual length of the text below.

---

## App name
*Limit: 30 characters*

```
Namazia: Afghan Prayer Times
```
*(28 characters)*

Alternative, if you prefer the Dari name to appear in the listing:

```
اوقات نماز افغانستان
```
*(20 characters)*

---

## Short description
*Limit: 80 characters*

```
Accurate Afghan prayer times, full azan, Qibla compass and dhikr — offline.
```
*(75 characters)*

---

## Full description
*Limit: 4000 characters*

```
Namazia gives you accurate daily prayer times for cities across Afghanistan, with the full azan called at each prayer — even when your phone is offline.

The app is built for Afghan users: the interface is in Dari and Pashto, switchable at any time, laid out right to left, and Asr is calculated for the Hanafi school by default.

━━━━━━━━━━━━━━━━━━━━

PRAYER TIMES
• Daily times for Fajr, sunrise, Dhuhr, Asr, Maghrib and Isha
• A live countdown to the next prayer, with a ring that fills as the time approaches
• Hijri date shown alongside
• Monthly calendar view with every day's times and Hijri dates
• Times for the coming week are stored on your device, so the app keeps working with no internet connection

THE AZAN
• The complete azan is played at each prayer time
• A separate, dedicated recitation for the Fajr azan
• Choose which prayers call the azan and which stay silent
• Optional reminder 5, 10, 15, 20 or 30 minutes before each prayer
• Optional vibration
• A test button so you can confirm the sound works without waiting for a prayer time
• Times are rescheduled automatically after you restart your phone, and refreshed every night

QIBLA COMPASS
• Points to the Kaaba using your device compass
• Corrected for magnetic declination, so it follows true north rather than magnetic north
• Confirms with a vibration when you are facing the qibla
• Shows the exact bearing in degrees, and warns you when the compass needs recalibrating

DHIKR AND DUA
• Adhkar for morning, evening, after prayer, before sleep and for travel
• Arabic text with its meaning in your language, and the number of repetitions for each
• Tasbih counter with targets of 33, 99 or 100, vibration feedback, and your count saved between sessions

SETTINGS
• 15 cities: Kabul, Herat, Mazar-i-Sharif, Kandahar, Jalalabad, Kunduz, Bamyan, Ghazni, Lashkar Gah, Taloqan, Pul-e-Khumri, Maimana, Sheberghan, Zaranj and Fayzabad
• Five calculation methods: University of Islamic Sciences Karachi, Muslim World League, Umm al-Qura Makkah, Islamic Society of North America, and the Egyptian General Authority of Survey
• Madhab setting for the Asr time: Hanafi (default) or Shafi/Maliki/Hanbali
• Dark mode
• Home screen widget showing the next prayer

━━━━━━━━━━━━━━━━━━━━

PRIVACY
Namazia has no account, no advertising and no analytics. Nothing about you is collected or sent anywhere. The app contacts the prayer-times service only to fetch the times for the city you choose.

For the azan to be called on time, please allow notifications and turn off battery optimisation for the app when it asks — some phones stop background alarms otherwise.

Questions or problems: aminhashemi979@gmail.com
```

*(~2,650 characters — well inside the limit)*

---

## Categorisation

| Field | Value |
|---|---|
| App category | Lifestyle |
| Tags | Prayer times, Islam, Qibla, Azan, Quran & Islamic |
| Contact email | aminhashemi979@gmail.com |
| External marketing | Off, unless you have a website |

---

## What's new (release notes)
*Limit: 500 characters per language*

```
First release.

• Prayer times for 15 Afghan cities, with the full azan and a separate Fajr recitation
• Live countdown to the next prayer, and a monthly calendar
• Qibla compass corrected to true north
• Adhkar and dua with their meaning in Dari or Pashto, plus a tasbih counter
• Works offline, and reschedules itself after a restart
• Hanafi Asr by default, with five calculation methods to choose from
```
*(~370 characters)*

---

## Data safety form

Answer the questionnaire as follows. This matches what the code actually does — no data
leaves the device except the request for prayer times.

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data encrypted in transit? | Yes (requests use HTTPS) |
| Do you provide a way for users to request that their data is deleted? | Not applicable — no data is collected |
| Data collected | None |
| Data shared | None |

The prayer-times request sends only a latitude and longitude for the city the user picked from
a fixed list, and a date. It carries no identifier and nothing is stored server-side, so it is
not "collection" under Play's definition. If the form asks you to justify the location
permissions in the manifest, see the note below first.

---

## Content rating

Answer the IARC questionnaire with **no** to every content question (no violence, no
user-generated content, no purchases, no ads, no sharing of location or personal
information). The expected outcome is a rating suitable for all ages.

---

## Before you upload the bundle — three declarations Play will ask for

**1. Exact alarms.** The manifest declares `USE_EXACT_ALARM`. Play requires apps using it to be
an alarm, clock or calendar app. Declare it as: *the app calls the azan at precise prayer times
set by astronomical calculation; an inexact alarm would call the azan at the wrong moment, which
defeats the purpose of the app.* This is an accepted use case, but the declaration is mandatory.

**2. Foreground service.** The app declares a `mediaPlayback` foreground service. In Play Console
you must describe its use and may be asked for a short screen recording. Declare it as: *a
foreground service plays the azan recitation when a prayer time arrives, and shows a
notification with a button to stop it.*

**3. Location permissions — read this before uploading.** The manifest declares
`ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`, but **no code in the app ever reads the
device location** — the user picks a city from a fixed list of 15. Declaring a permission you do
not use means Play will require a location-use declaration you cannot truthfully complete, and
it is a common cause of review rejection.

**Recommendation: delete both lines from `app/src/main/AndroidManifest.xml` before you build the
bundle.** Nothing in the app breaks — the city list does not need them. Tell me if you would
like me to remove them, or if you would rather keep them because you plan to add automatic
location detection in this release.

---

## Notes on the build itself

- **Package name: `af.namazia.app`** — this is the app's permanent identity on Play and can
  never be changed after the first upload. Confirm it is what you want before you publish.
- **`versionCode` must go up on every upload.** Play retires a code the moment a bundle
  carrying it is uploaded — to any track, published or not — and will not accept it again.
  Bump `versionCode` in `app/build.gradle` before each new bundle you build.
  Currently at `versionCode 2`, `versionName "1.0.1"`.
- `minSdk 26` (Android 8.0), `targetSdk 34` (Android 14) — targetSdk 34 satisfies Play's current
  requirement.
- The app ships two azan recordings (~8 MB total), so expect a bundle of roughly 12–15 MB.
- There is no `buildTypes { release { ... } }` block, so the release build runs unminified.
  That is fine for a first release; enabling `minifyEnabled` later needs Room, Hilt, Retrofit and
  Gson keep rules, which I can add when you want it.
- The bundled font Vazirmatn is licensed under SIL OFL 1.1 (`licenses/Vazirmatn-OFL.txt`).
  Redistribution inside an app is permitted and needs no attribution in the listing.
