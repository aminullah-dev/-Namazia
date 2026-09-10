# App Store Connect — App Information (English)

Copy each block into the matching field in **App Store Connect → your app → the version
under Distribution**. Character limits are noted; the counts given are the actual length
of the text below.

> **Why English?** App Store Connect offers neither Dari nor Pashto as a listing
> language. The app itself is entirely in those two — the listing is the only part of
> this that is in English, which is the same position every Afghan app is in.

---

## App Name
*Limit: 30 characters*

```
Namazia: Afghan Prayer Times
```
*(28 characters)*

---

## Subtitle
*Limit: 30 characters*

```
Azan, Qibla, Hijri calendar
```
*(27 characters)*

---

## Promotional Text
*Limit: 170 characters. Can be changed any time without a new build — use it for
Ramadan, Eid, or a new feature.*

```
Prayer times for 15 Afghan cities, the azan at every prayer, a Qibla compass and a Hijri calendar. In Dari and Pashto, Hanafi Asr by default, works offline.
```
*(156 characters)*

Not "the full azan", as on Play: on iOS the full azan plays only while the app is open.
A reviewer who hears a 24-second notification after reading "full" has a metadata
rejection (2.3) to write.

---

## Description
*Limit: 4000 characters*

```
Namazia gives you accurate daily prayer times for cities across Afghanistan, with the azan called at each prayer time — and it keeps working when you have no internet connection.

The app is built for Afghan users: the interface is in Dari and Pashto, switchable at any time, laid out right to left, with Perso-Arabic numerals throughout, and Asr calculated for the Hanafi school by default.

PRAYER TIMES
• Daily times for Fajr, sunrise, Dhuhr, Asr, Maghrib and Isha
• A live countdown to the next prayer, with a ring that closes as the time approaches
• The Hijri date shown alongside
• A monthly calendar with every day's times
• The coming week is stored on your device, so the app keeps working with no connection

THE AZAN
• The azan is called at each prayer time, with a separate recitation for Fajr
• Choose which prayers call the azan and which stay silent
• An optional reminder 5, 10, 15, 20 or 30 minutes before each prayer
• A test button, so you can confirm the sound works without waiting for a prayer time

QIBLA
• A compass pointing to the Kaaba, calculated from the city you choose
• The direction is shown in degrees as well, so it is usable even without a compass

DHIKR AND TASBIH
• Morning and evening adhkar, adhkar after prayer, before sleep, and for travel
• Each with its Arabic text and its meaning in your language
• A tasbih counter that remembers your count

WIDGETS
• Home screen widgets showing the next prayer and a live countdown
• Lock screen widgets, so the next prayer is there without unlocking

CITIES
Kabul, Herat, Mazar-i-Sharif, Kandahar, Jalalabad, Kunduz, Bamyan, Ghazni, Lashkar Gah, Taloqan, Pul-e-Khumri, Maimana, Sheberghan, Zaranj and Fayzabad.

LANGUAGE
Dari and Pashto, switched in Settings and applied immediately. The prayers carry their Pashto names — ماسپښين, مازديګر, ماخستن — rather than transliterated Dari ones.

CALCULATION
Choose between the Muslim World League, University of Islamic Sciences Karachi, Umm al-Qura Makkah, Islamic Society of North America, and the Egyptian General Authority of Survey. Asr can be set to the Hanafi school or to Shafi'i, Maliki and Hanbali.

PRIVACY
Namazia has no account, no advertising and no analytics. Nothing you do in the app is sent anywhere, and nothing about you is collected. Prayer times are requested by city name from a public prayer-times service; your device's location is never sent.

A NOTE ABOUT THE AZAN ON iPHONE
When a prayer arrives while the app is open, the full azan is played. When your phone is locked or the app is closed, iOS plays the opening of the azan as a notification sound, which the system limits to 30 seconds. This is a limit of iOS itself and applies to every prayer app on the App Store.

Fajr azan recording: "Morning call to prayer on Bodufolhudhoo, Maldives" by jrosin (freesound.org), CC BY 4.0.
```
*(about 2,300 characters)*

---

## Keywords
*Limit: 100 characters, comma-separated, no spaces after commas. Do not repeat words
already in the app name or subtitle — Apple indexes those separately.*

```
namaz,salah,adhan,athan,namaaz,kabul,afghanistan,dari,pashto,muslim,islam,ramadan,hanafi,tasbih
```
*(95 characters)* — "qibla" is left out because the subtitle already carries it.

---

## Support URL
*Required.*

```
https://aminullah-dev.github.io/-Namazia/
```

## Marketing URL
*Optional — the same page works.*

```
https://aminullah-dev.github.io/-Namazia/
```

## Privacy Policy URL
*Required.*

```
https://aminullah-dev.github.io/-Namazia/privacy-policy.html
```

---

## Category

| Field | Value |
|---|---|
| Primary | **Lifestyle** |
| Secondary | **Reference** |

Apple has no "Religion" category; Lifestyle is where prayer-time apps sit.

---

## Age Rating

Answer **None** to every question in the questionnaire. The result is **4+**.

There is one question worth reading carefully: *"Does your app contain religious
content?"* — this is asking about *contentious* religious content, and prayer times and
adhkar do not qualify. Answer **None**.

---

## App Privacy

This is the section Apple cross-checks against `PrivacyInfo.xcprivacy`, so the two must
agree. The manifest declares no collected data at all.

- **Data Collection:** answer **No, we do not collect data from this app.**

That single answer completes the section. Do not tick Location: the app never sends
location anywhere, and the compass reading is used on the device only — which is exactly
what Apple defines as "not collected".

---

## Export Compliance

The app makes HTTPS requests but contains no encryption of its own, which is the
standard exemption.

- *Does your app use encryption?* → **Yes** (HTTPS counts)
- *Does it qualify for any of the exemptions?* → **Yes**, the one for apps that only use
  standard encryption provided by the operating system

`ITSAppUsesNonExemptEncryption` is already set to `false` in `Info.plist`, so App Store
Connect should not ask at all.

---

## Version Information

| Field | Value |
|---|---|
| Version | 1.0 |
| Copyright | 2026 Aminullah Hashemi |
| Sign-in required | No |
| Contact email | aminhashemi979@gmail.com |

### What's New in This Version
*First release — this field is not shown for version 1.0, but App Store Connect may
still ask for it.*

```
First release.
```
