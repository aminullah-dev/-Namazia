# Namazia — iOS

SwiftUI app for iPhone, iOS 16+. Shares its identity, palette and audio with the
Android app in `../app`.

The `.xcodeproj` is **generated**, not committed. `project.yml` is the source of truth;
regenerate after pulling or after any change to it.

---

## First-time setup

```bash
brew install xcodegen        # once
cd ios
xcodegen generate
open Namazia.xcodeproj
```

In Xcode, the first time only: select the **Namazia** target → **Signing &
Capabilities** → pick your Team. That writes a local team ID that stays out of git
because `project.yml` leaves `DEVELOPMENT_TEAM` empty.

Then ⌘R to build and run on the simulator.

### After pulling changes

```bash
cd ios && xcodegen generate
```

Adding, renaming or deleting a Swift file needs no project edit — `project.yml` pulls
in the whole `Namazia` folder — but the project must be regenerated so Xcode sees it.

---

## Two languages

The app ships in **Dari** and **Pashto**, switchable in Settings, taking effect
immediately without a relaunch.

The language is the app's own setting, not the phone's. That is deliberate: an Afghan
phone is very often set to English, and someone who wants the app in Pashto should not
have to change their whole device to get it. `L10n` points at the chosen `.lproj`
bundle and `"key".localized` resolves through it; a settings change republishes, every
view re-reads its strings on the next render, and the UI switches in place.

They are still real `.lproj` resources, so nothing is given up — iOS can still pick a
sensible default on first launch, and the app lists its languages in iOS Settings.

Things worth knowing before touching the strings:

- **Pashto is not Dari with different words.** The prayers have their own names
  (ماسپښين, مازديګر, ماخستن), some cities are spelled differently (لښکرګاه), and past-tense
  transitive verbs agree with the *object*, not the subject — «سهار مو وکړ».
- **Both files must carry the same keys**, and matching format specifiers. Pashto
  reorders arguments in places, which is why the formats are positional (`%1$@`,
  `%2$@`) rather than bare `%@`.
- **The widget resolves its own strings.** It is a separate process with its own bundle,
  so it calls `L10n.use` from the timeline provider; nothing the app did carries over.
- **`L10n.swift` lives in `Utils/`, not beside the `.lproj` folders.** A directory named
  as a target's source is treated as a resource group once it contains `.lproj`
  subfolders, and a Swift file inside it is then never compiled into that target.
- Notifications are written at *schedule* time, so a language change re-arms the whole
  pending queue — which happens already, because any settings change does.

---

## The screens

Five tabs: **اوقات** (home), **قبله**, **تقویم**, **اذکار**, **تنظیمات**.

- **قبله** computes the great-circle bearing to the Kaaba from the *selected city*, so
  it needs no permission at all to tell you which way to face. Location is asked for
  only to rotate the dial with the phone; refuse it and the screen still works, it just
  stops turning. There is no compass in the simulator — the screen says so rather than
  showing a dial frozen at north.
- **تقویم** fetches a whole month in one request and writes it into the same cache the
  home screen reads, so browsing the calendar also warms tomorrow.
- **اذکار** carries the same adhkar as Android, plus a tasbih where the whole screen is
  the button — a counter you have to aim at is a counter you lose your place on.
- **تنظیمات** writes the method and school through `AppServices`, which clears the cache
  in the same call.

Two Android settings are deliberately absent: **vibration** (iOS does not let an app
control notification haptics) and **auto-location** (the city list is the whole model).
The settings screen says so where a user would otherwise wonder.

### The home screen

What to look for when it runs:

- **The hero card** counts down to the next prayer. The ring measures the *real* gap
  between the previous prayer and the next one, so it closes at a different rate at
  different times of day; before Fajr, with no earlier prayer, it falls back to a
  six-hour window.
- **The list** marks each prayer past / next / upcoming with a shape as well as a
  colour, so the state survives greyscale and colour-blindness.
- **Pull down** to refresh; the circular arrow in the header does the same.
- **Times stay on screen when a refresh fails**, with a red strip saying they came from
  the cache. An empty screen would be worse than slightly old times that cannot change.
- **The date rolls over at midnight** without a relaunch — a phone left open overnight
  reloads itself into the new day.

The countdown re-derives the list every second, so "next" moves to the following prayer
on its own, with no network call.

### Things that fail silently — check them once

| Check | What a failure looks like |
|---|---|
| Vazirmatn loads | Text renders in the system font, slightly off, no error |
| Layout is right-to-left | Content sits left-aligned |
| Perso-Arabic renders | Latin digits, or disconnected letterforms |
| Kabul time | Countdown and "next" are right for your timezone, not Kabul's |
| Hanafi Asr | Asr about an hour earlier than the local mosque |

---

## The widget

Home screen (small and medium) and lock screen (rectangular and circular).

**The widget never touches the network.** It reads the prayer times the app already
cached in the App Group container. It cannot ask the user for anything, it runs on a
budget the system controls, and a cached day's times never change — so reading is both
cheaper and more predictable than fetching.

Two details worth knowing before changing anything here:

- **The countdown is not a timeline entry.** `Text(timerInterval:countsDown:)` is
  redrawn by WidgetKit itself every second. Entries exist only at the moments the *next
  prayer* changes — one per prayer time — which is why the timeline is a few dozen
  entries rather than thousands.
- **Perso-Arabic digits in that countdown come from the locale**, not from
  `persianDigits`: the system formats the text, so the widget sets the locale from the
  chosen language. Both Afghan locales use the same numerals.

If the widget shows «برنامه را باز کنید», it means it found no cached day — either the
app has never run, or the App Group is not in place, in which case the app and the
widget are each reading their own private container.

The widget target compiles `Data/`, `Utils/` and `DesignSystem/` but deliberately not
`Screens/`: those use `UIApplication`, which is unavailable in an app extension and
would not build.

---

## Layout

```
App/
  NamaziaApp.swift          entry point, theme container, RTL
  AppServices.swift         the shared repository and settings store
  RootTabView.swift         the five tabs
Screens/
  HomeView.swift            the home screen
  HomeViewModel.swift       its state
  QiblaView.swift           compass + bearing to the Kaaba
  CalendarView.swift        a month of times
  DhikrView.swift           adhkar and the tasbih
  SettingsView.swift        city, method, madhab, azans, reminder, theme
Components/
  CountdownRing.swift       the hero ring
  PrayerRowView.swift       one line of the list
  StateViews.swift          skeleton, error state, stale-data notice
Notifications/
  NotificationScheduler.swift   the rolling window of pending azans
  NotificationDelegate.swift    foreground behaviour + the UIKit app delegate
  BackgroundRefresh.swift       tops the queue up while the app is closed
  AzanPlayer.swift              the full azan, in-app
Data/
  Models.swift              API payloads, cities, settings, calculation methods
  AppError.swift            four failure codes, worded at the UI edge
  AppGroup.swift            shared container, with an app-private fallback
  PrayerTimesAPI.swift      aladhan client (URLSession, async/await)
  PrayerTimesCache.swift    JSON cache in the App Group, an actor
  PrayerTimesRepository.swift   cache-first reads, week prefetch, month fetch
  SettingsStore.swift       UserDefaults, one key per setting
Localization/
  fa.lproj/, ps.lproj/      Dari and Pashto, 155 keys each
Utils/
  L10n.swift                the chosen-language lookup
  AppTime.swift             Kabul timezone, POSIX formatters, HH:mm → instant
  PrayerCalc.swift          schedule, next prayer, countdown text
  Qibla.swift               great-circle bearing to the Kaaba

../NamaziaWidget/
  NamaziaWidget.swift       widget definition + timeline provider
  PrayerWidgetView.swift    the four families
  Info.plist                extension point + its own font registration
```

Two rules that the Android app learned the hard way and that this port keeps:

- **`school = 1` (Hanafi) is the default.** It moves Asr by about an hour.
- **Changing the calculation method or the school must clear the cache**, or
  differently-calculated times keep being served as if still valid. Use
  `AppServices.setCalculationMethod` / `setAsrSchool`, which do both together — that is
  why they exist rather than writing the setting directly.

---

## Audio: the 30-second azan

**This is the one place iOS cannot match Android.** Android plays the full 3–5 minute
azan through a foreground service at the exact prayer time. iOS has no equivalent: with
the app closed, the only way to make sound at a scheduled moment is a local
notification, and a notification sound is capped at **30 seconds** and must be
CAF/WAV/AIFF in the app bundle — not MP3.

So the plan is the one every iOS prayer app uses:

- a **30-second azan** as the notification sound, and
- the **full azan** played only while the app is open.

Both short files are committed in `Namazia/Resources/Sounds/`, cut from the Android MP3s:

| File | Length | Where it ends |
|---|---|---|
| `azan30.caf` | 24.35 s | a silent pause between phrases |
| `azan_fajr30.caf` | 14.5 s | the pause after the first phrase — Fajr is recited slowly, and the next one runs past 30 s |

Each starts at the first sound (the MP3s open with 0.2 s and 0.7 s of silence) and ends
in a pause rather than mid-word, which is why neither is a flat 30 seconds. They are
mono IMA4, which notification sounds support, so together they weigh under 1 MB.

To redo them, decode with `afconvert in.mp3 out.wav -f WAVE -d LEI16@44100`, cut the
WAV at a pause (any tool), then encode:

```bash
afconvert cut.wav azan30.caf -f caff -d ima4 -c 1 --mix
afinfo azan30.caf | grep duration     # must be under 30
```

`afconvert` has no option to trim by time, so the cutting has to happen in between.

The full-length MP3s are bundled too — `project.yml` references them where they already
live in `app/src/main/res/raw/` rather than keeping a second 8 MB copy under `ios/`.
`AzanPlayer` plays those when a prayer arrives while the app is open, and the
notification's own short sound is suppressed so the two do not overlap.

**If the two `.caf` files go missing, notifications fall back to the default iOS
chime.** Nothing breaks; the azan just is not the azan. In a debug build the speaker button in the header
fires a test notification a few seconds out, and its text says which sound you are
getting.

---

## How the azan actually gets called

This is the part that differs most from Android, and the part most likely to be
misunderstood later:

- iOS cannot wake the app at a prayer time. Everything must be **scheduled in advance**
  as local notifications, and the system keeps at most **64 pending** ones.
- So the app arms a rolling window: the next seven days of prayers, azan plus reminder,
  taken in time order until the budget of 60 runs out — about six days. Nearest first,
  because a reminder eight days out is worthless if it costs tomorrow's Fajr.
- The window is re-armed on every launch, on returning to the foreground, and whenever
  the settings change.
- `BackgroundRefresh` asks iOS to top the queue up while the app is closed. iOS decides
  whether to honour that, so it is a safety net, not the mechanism.

Triggers are `UNCalendarNotificationTrigger` with the timezone pinned to Kabul, so a
phone that travels still fires at the right wall-clock time.

### Optional: Time Sensitive

The notifications are marked `.timeSensitive` so they can break through a Focus mode —
which is exactly right for a prayer time. It only takes effect with the **Time Sensitive
Notifications** capability enabled for the App ID on the developer portal. Without it
iOS quietly treats them as ordinary notifications; nothing fails to build.

---

## Structure

```
ios/
  project.yml                  XcodeGen spec — the project's source of truth
  Namazia/
    App/                       entry point, root view
    DesignSystem/              palette, typography, spacing
    Resources/
      Info.plist
      Namazia.entitlements     App Group for the widget
      Assets.xcassets/         app icon, launch background
      Fonts/                   Vazirmatn, 4 weights
      Sounds/                  azan30.caf, azan_fajr30.caf  (notification azans)
```

---

## Shipping

```bash
cd ios && ./scripts/upload.sh
```

Archives and uploads in one command, bumping the build number on the way — see
`scripts/README.md` for the one-time API key setup. `../store/app-store-checklist.md`
is the wider step-by-step: membership, identifiers, screenshots, TestFlight, review
notes. `../store/app-store-listing-en.md` has
every field of the listing itself.

Two things in the repo that exist for the store and are easy to break:

- **`PrivacyInfo.xcprivacy`**, in both the app and the widget. Apple cross-checks it
  against the answers given in App Store Connect, so if the app ever starts collecting
  something, this file has to say so.
- **The app icon** is the same artwork as the Play icon (`store/reference/`), at
  1024×1024 with **no alpha channel** — App Store Connect rejects a transparent icon
  outright.

---

## Before the first device build

The App Group in `Namazia.entitlements` must exist on the Apple Developer portal, or
signing fails on device (the simulator does not care):

**Certificates, Identifiers & Profiles → Identifiers → App Groups → +** and create
`group.af.namazia.app`.

It is declared now, before the widget exists, because adding it later forces
re-provisioning of both targets.
