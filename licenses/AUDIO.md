# Audio provenance

Every audio file shipped in either app must be recorded here, with its source and its
licence, at the moment it is added. Apple asks for this under Guideline 2.1, Google can
ask under the same heading, and a recording of the call to prayer is a performance —
someone holds the rights to it.

---

## Current status: unknown, and must be replaced

| File | Used for | Source | Licence |
|---|---|---|---|
| `app/src/main/res/raw/azan.mp3` | Dhuhr, Asr, Maghrib, Isha | **unrecorded** | **unknown** |
| `app/src/main/res/raw/azan_fajr.mp3` | Fajr | **unrecorded** | **unknown** |

Both were added in commit `3a76d7e` (2026-06-25) by a Claude session, which recorded
only the internal filenames — "SunniAzan1" and "FajrAzan" — and neither the source nor
the licence. Nothing else survives:

- no URL anywhere in the git history
- no ID3 tags: `azan.mp3` carries only `TSSE: Lavf58.20.100`, an ffmpeg encoder stamp,
  so the file was re-encoded and its original tags were stripped; `azan_fajr.mp3` has no
  ID3 header at all

The origin cannot be reconstructed, so it cannot be answered for honestly. **These two
files need to be replaced with recordings whose licence can be named.**

Note this is not only an App Store question: the same two files are in the build already
published on Google Play.

## Replacing them

Two files are needed, full length (3–5 minutes is normal):

- a regular azan
- a Fajr azan — the Fajr call adds «الصلاة خير من النوم», so it has to be a separate
  recording, not the same file

Either of these gives an answer that can be written down:

- **Record one, or use a recording made with the muezzin's permission.** Best option: the
  rights are unambiguous, and an Afghan azan suits an Afghan app.
- **Take one from a source that states a licence** — Wikimedia Commons and the Internet
  Archive both carry adhan recordings. Read the licence on the file's own page; "it was
  free to download" is not a licence.

Then, in the same change:

1. Replace `app/src/main/res/raw/azan.mp3` and `azan_fajr.mp3`
2. Regenerate the 30-second iOS notification sounds — the `afconvert` commands are in
   `ios/README.md`
3. Fill in the table above with the real source and licence
4. Bump `versionCode` in `app/build.gradle` and let `ios/scripts/upload.sh` bump the iOS
   build, since both stores need a new binary

---

## Fonts, for comparison — this is what a recorded provenance looks like

| File | Source | Licence |
|---|---|---|
| `Vazirmatn-{Light,Regular,Medium,Bold}.ttf` | Vazirmatn by Saber Rastikerdar | SIL OFL 1.1 — `licenses/Vazirmatn-OFL.txt`; bundling in an app is permitted |
