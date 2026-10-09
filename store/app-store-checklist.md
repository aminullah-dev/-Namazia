# App Store — submission checklist

Everything here is done on a Mac or in a browser; none of it can be done from the repo.
Work top to bottom — each step depends on the one above it.

---

## 1. Apple Developer Program

The App Store requires a paid membership: **$99 a year**, at
[developer.apple.com/programs](https://developer.apple.com/programs/). A free Apple ID
can run the app on your own iPhone for seven days at a time, but cannot publish, and
cannot use App Groups on a device.

Enrolment as an individual usually clears in a day or two. Apple may ask for photo ID.

---

## 2. Identifiers, before the first device build

At [developer.apple.com](https://developer.apple.com/account/resources/identifiers/list)
→ **Certificates, Identifiers & Profiles → Identifiers**:

| Create | Identifier | Notes |
|---|---|---|
| App IDs → App | `af.namazia.app` | Enable **App Groups** |
| App IDs → App | `af.namazia.app.widget` | Enable **App Groups** |
| App Groups | `group.af.namazia.app` | Exactly this string |

Then edit both App IDs and tick `group.af.namazia.app` under App Groups.

**If you skip this, the widget shows «برنامه را باز کنید» forever on a real device** —
the app and the widget end up with separate private containers and cannot see each
other's data. The simulator is more forgiving, which is what makes this easy to miss.

### Optional: Time Sensitive Notifications

On the `af.namazia.app` App ID, enable **Time Sensitive Notifications**. The azan is
already marked time-sensitive in code; with this enabled it can break through a Focus
mode, which is exactly right for a prayer time. Without it, iOS quietly treats the
notification as ordinary — nothing breaks.

---

## 3. In Xcode, once

```bash
cd ~/StudioProjects/-Namazia && git pull
cd ios && xcodegen generate && open Namazia.xcodeproj
```

For **each** target — `Namazia` **and** `NamaziaWidget` — go to **Signing &
Capabilities** and pick your Team. `project.yml` deliberately leaves the team empty so a
personal team ID never lands in git; you set it once locally and it stays out of commits.

Check that both targets show **App Groups → group.af.namazia.app** ticked.

---

## 4. Screenshots

Apple requires **iPhone 6.9-inch** screenshots. Your simulator (iPhone 17 Pro Max)
produces exactly that size — 1320 × 2868 — so nothing needs resizing.

In the simulator, **⌘S** saves a screenshot to the Desktop.

Take at least four, ideally these:

1. **Home** — mid-countdown, with the ring part-way round. Best just before a prayer.
2. **Calendar** — a month of times.
3. **Qibla** — on a real device, so the compass is live.
4. **Dhikr** — an adhkar card with its Arabic and Dari.
5. **Settings** — showing the cities and the calculation options.

Upload them in that order; the first is what people see in search results.

A note that saves an hour: the App Store shows screenshots right-to-left for nobody —
the *order* you upload is the order shown, left to right. Put the home screen first.

---

## 5. Create the app record

At [appstoreconnect.apple.com](https://appstoreconnect.apple.com) → **My Apps → +**:

| Field | Value |
|---|---|
| Platform | iOS |
| Name | Namazia: Afghan Prayer Times |
| Primary Language | English (U.S.) |
| Bundle ID | `af.namazia.app` |
| SKU | `namazia-ios-1` (any private string) |
| User Access | Full Access |

Then fill in every field from **`app-store-listing-en.md`**.

---

## 6. Archive and upload

1. In Xcode's destination menu (top bar), choose **Any iOS Device (arm64)** — not a
   simulator; you cannot archive for a simulator.
2. **Product → Archive**. This takes a few minutes.
3. When the Organizer opens: **Distribute App → App Store Connect → Upload**.
4. Leave the default options ticked and let it upload.

The build then takes 10–30 minutes to finish processing before it can be selected in
App Store Connect. You will get an email if it is rejected at this stage — usually for a
missing icon size or a bad Info.plist value.

**Every upload needs a higher build number.** Bump `CURRENT_PROJECT_VERSION` in
`project.yml` (1 → 2 → 3) and re-run `xcodegen generate`. `MARKETING_VERSION` is the
version people see (1.0) and only changes for a real release.

---

## 7. TestFlight first

Before submitting for review, add yourself under **TestFlight → Internal Testing** and
install the build through the TestFlight app. This is the only way to see the app
signed, on a real device, exactly as a reviewer will.

Test these four things on a real iPhone, because none of them can be trusted from a
simulator:

- [ ] The azan actually sounds at a prayer time with the phone locked
- [ ] The Qibla compass turns, and points somewhere plausible
- [ ] The widget shows real times (not «برنامه را باز کنید»)
- [ ] Locking the phone mid-azan does not cut it off, and the lock screen shows a stop
      button

---

## 8. App Review notes

Paste this into **App Review Information → Notes**. It answers, in advance, the two
questions this app will raise:

```
Namazia is a prayer-times app for Afghanistan. No account or sign-in is required; all features are available immediately.

Background audio: the app declares the `audio` background mode because the call to prayer (azan) runs three to five minutes, and locking the phone part-way through would cut it off. Audio plays only at a prayer time the user has enabled, or when the user presses the test button in Settings. While playing, the app publishes now-playing information and a stop control to the lock screen and Control Center.

Location: used only to rotate the Qibla compass with the device heading. The direction to the Kaaba is computed from the city the user selects, so the feature works with location permission denied. No location data is stored or transmitted.

To see the azan without waiting for a prayer time: Settings tab → "پخش اذان (آزمایش صدا)".
```

---

## 9. Submit

**Add for Review → Submit**. First reviews typically take 24–48 hours.

If it comes back rejected, the message names the specific guideline. The two most likely
here are **2.5.4** (background audio without a user-facing reason — answered by the note
above) and **5.1.1** (a permission string that does not explain itself — the location
string in `Info.plist` already does).

---

## Differences from Google Play, so you are not surprised

| | Play | App Store |
|---|---|---|
| Cost | $25 once | $99 a year |
| Testing before release | 12 testers, 14 continuous days | TestFlight, no minimum |
| Review | Mostly automated, hours | A person, 24–48 hours |
| Azan length | Full, at the prayer time | 30 seconds when closed, full when open |
| Rejections | Rare, usually policy forms | More common, usually specific and fixable |
