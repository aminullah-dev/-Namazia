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

## What Phase 1 gives you

A single screen that verifies the four things that otherwise fail *silently*:

| Check | What a failure looks like |
|---|---|
| Vazirmatn loads | Text renders in the system font, slightly off, no error |
| Palette resolves | Wrong or washed-out colours in light or dark |
| Layout is right-to-left | Content sits left-aligned |
| Perso-Arabic renders | Latin digits, or disconnected letterforms |

If the screen reads right-to-left, the four weights look different from each other,
and the digits are ۰۱۲۳۴۵۶۷۸۹, Phase 1 is good.

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

macOS has the converter built in, so run this on your Mac from the repo root:

```bash
cd ios/Namazia/Resources/Sounds

# Regular azan — first 30 seconds
afconvert ../../../../app/src/main/res/raw/azan.mp3 \
  azan30.caf -f caff -d LEI16@44100 -t 0:30

# Fajr azan — first 30 seconds
afconvert ../../../../app/src/main/res/raw/azan_fajr.mp3 \
  azan_fajr30.caf -f caff -d LEI16@44100 -t 0:30
```

Check both are under 30 seconds and that they start on "الله اکبر" rather than a
silent lead-in — trim the start with `-s` if the recording opens with silence:

```bash
afinfo azan30.caf | grep duration
```

The full-length MP3s get bundled too, for in-app playback, in the phase that adds
notifications.

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
      Sounds/                  azan30.caf, azan_fajr30.caf  (you generate these)
```

---

## Before the first device build

The App Group in `Namazia.entitlements` must exist on the Apple Developer portal, or
signing fails on device (the simulator does not care):

**Certificates, Identifiers & Profiles → Identifiers → App Groups → +** and create
`group.af.namazia.app`.

It is declared now, before the widget exists, because adding it later forces
re-provisioning of both targets.
