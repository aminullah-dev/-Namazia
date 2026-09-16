# App Review — reply to Guideline 2.1 (Information Needed)

A first submission from a new developer account almost always draws this. It asks for
information, not for a change to the app.

**Both fields cap at 4000 characters** — the reply box and App Review Information →
Notes. The text below is 3809, so the same text goes in both places:

1. The reply in App Store Connect → the review message thread, with the screen recording
   attached
2. **App Review Information → Notes**, so future submissions already carry it

---

## The text

```
Thank you for reviewing Namazia.

1. SCREEN RECORDING
Attached, captured on a physical iPhone on the latest iOS, starting with the app launching. None of the listed features exist in this app: no account registration, no login, no account deletion (there are no accounts), no user-generated content, and no paid content or in-app purchases. Every feature is available immediately on first launch.

2. PURPOSE AND TARGET AUDIENCE
Namazia shows Islamic prayer times for 15 cities in Afghanistan and calls the azan (call to prayer) at each prayer time. It also gives the Qibla direction, a Hijri calendar and traditional supplications.

The audience is Afghans, at home and in the diaspora, who are poorly served by existing prayer apps in three ways this app is built around: language (the interface is entirely in Dari and Pashto, right to left, with Perso-Arabic numerals, switchable inside the app because an Afghan phone is often set to English); calculation (Asr uses the Hanafi school by default, which most Afghans follow — apps defaulting to Shafi'i put Asr about an hour early for them); and connectivity (a week of times is stored on the device, so it works through the intermittent connections normal in Afghanistan).

No age-restricted or contentious content. Suitable for all ages.

3. SETUP AND ACCESS
No setup, no credentials, no account, no purchases. The five tabs, right to left as they appear:
  اوقات  Times — today's times and a countdown
  قبله   Qibla — compass to the Kaaba
  تقویم  Calendar — a month of times
  اذکار  Dhikr — supplications and a tasbih
  تنظیمات Settings — city, calculation, azan, language

To test quickly, all in the Settings tab (gear icon, far left):
- Hear the azan without waiting for a prayer: "پخش اذان (آزمایش صدا)" = Play azan (sound test). It plays at once, continues with the phone locked, and shows a stop control on the Lock Screen.
- Switch language: first section, "پښتو" or "دری". The whole UI changes at once.
- Change city: "موقعیت" then "شهر".
Home Screen and Lock Screen widgets show the next prayer and a live countdown.

Permissions: notifications, asked on launch, because without it the azan cannot sound; if refused, all times still show and the app says the azan is off. Location, asked only on the Qibla tab and only to rotate the compass dial — the direction is computed from the selected city, so the screen still works if refused. No location data is stored or sent.

4. EXTERNAL SERVICES
One only: the Aladhan Prayer Times API (aladhan.com/prayer-times-api), free and public, no key or account. The app sends the selected city's coordinates, a calculation method and a fiqh school, and receives that day's times. Nothing identifying the user or device is sent. No analytics, advertising, authentication, payment, AI services or third-party SDKs. Nothing is collected, matching the privacy manifest and our App Privacy answers.

5. REGIONAL DIFFERENCES
None. Identical features and content in every region and storefront. No geo-gating, no server-side configuration.

6. REGULATED INDUSTRY AND THIRD-PARTY MATERIAL
Not a regulated industry; no claims requiring a licence. Third-party material:
- Regular azan: "Beautiful adhan" by Adam-synagda, Wikimedia Commons, CC0 1.0 (public domain dedication).
- Fajr azan: "Morning call to prayer on Bodufolhudhoo, Maldives" by jrosin, Freesound, CC BY 4.0. The required credit appears in the app's Settings screen and in the App Store description.
- Vazirmatn typeface by Saber Rastikerdar, SIL Open Font License 1.1, which permits bundling in an app.
- The supplications are Qur'anic verses and well-known supplications from the standard hadith collections, public domain religious texts; their Dari and Pashto renderings were written for this app.
Both recordings were trimmed for length only.
```

---

## The screen recording

On a real iPhone, not a simulator — Apple asks for this explicitly.

1. Settings → Control Center → add **Screen Recording**
2. Start recording, then go to the Home Screen
3. **Begin by launching the app from the Home Screen** — Apple asks for this specifically
4. Then, unhurried, about two minutes:
   - Times tab: the countdown, then scroll the list of prayers
   - Qibla tab: allow location, turn the phone so the needle moves
   - Calendar tab: scroll, step to the next month
   - Dhikr tab: open a supplication, then the tasbih, tap it a few times
   - Settings tab: switch to Pashto and back, change the city, then press "پخش اذان" so
     the azan is heard
   - While the azan is still playing, swipe down from the top-right corner to open
     Control Center: the Now Playing card shows the azan with a stop button. That is the
     same evidence as the Lock Screen — audio continuing outside the app, with a control
     for it — and it answers, in advance, any question about the background audio mode.

     Do **not** lock the phone to show this: locking ends the screen recording.
   - Close Control Center, go to the Home Screen, show the widget
5. Stop, then attach the video to the reply

## The audio rights

Both recordings carry open licences and their sources are in
`licenses/azan-recordings.md`. The CC BY credit the Fajr recording requires is in the
app's Settings screen and in the store description, and has to stay there as long as
that recording ships.

## Screenshots

Apple's letter mentions Guideline 2.3.3: screenshots must show the app in use. If any
uploaded screenshot is the iOS Home Screen with the widget on it rather than a screen of
the app itself, replace it.
