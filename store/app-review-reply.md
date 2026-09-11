# App Review — reply to Guideline 2.1 (Information Needed)

A first submission from a new developer account almost always gets this. It is a request
for information, not a rejection, and nothing in the app has to change.

Paste the block below **twice**:

1. As the reply in App Store Connect → the review message thread
2. Into **App Store Connect → App Review Information → Notes**, so future submissions
   already have it

The screen recording (their point 1) has to be made on a real iPhone — see the last
section.

---

## The reply

```
Thank you for reviewing Namazia. Answers to each point below.

1. SCREEN RECORDING

A screen recording captured on a physical iPhone running the latest iOS is attached. It
starts with the app launching and shows the full typical flow.

Namazia has none of the features listed under this point: there is no account
registration, no login, no account deletion (because there are no accounts), no
user-generated content, and no paid content or in-app purchases. Every feature is
available to every user immediately on first launch.

2. PURPOSE AND TARGET AUDIENCE

Namazia shows Islamic prayer times for cities in Afghanistan and calls the azan (the
call to prayer) at each prayer time.

The audience is Afghans — in Afghanistan and in the diaspora — who need prayer times
they can trust and read. Existing prayer apps serve them poorly in three specific ways,
and Namazia is built around those three:

- Language. The interface is entirely in Dari and Pashto, right to left, with
  Perso-Arabic numerals. Most prayer apps offer Arabic or English only. The two
  languages are switched inside the app, because an Afghan phone is very often set to
  English and the reader should not have to change their whole device.
- Calculation. Asr is calculated for the Hanafi school by default, which is the school
  the great majority in Afghanistan follow. Apps that default to the Shafi'i
  calculation put Asr roughly an hour early for these users.
- Connectivity. The coming week of times is stored on the device, so the app keeps
  working through the intermittent connections that are normal in Afghanistan.

There is no age-restricted, contentious or user-submitted content of any kind. The app
is suitable for all ages.

3. SETUP AND ACCESS

No setup is required and no credentials exist. There is no account, no login, no
subscription and no in-app purchase. Every feature works on first launch.

The interface is in Dari and laid out right to left. The five tabs, right to left:

  اوقات    Times     prayer times for today, with a countdown to the next prayer
  قبله     Qibla     compass pointing to the Kaaba
  تقویم    Calendar  a month of prayer times
  اذکار    Dhikr     supplications, and a tasbih counter
  تنظیمات  Settings  city, calculation, azan switches, language

To test the main features without waiting for a prayer time:

- Hear the azan: Settings tab (gear icon) → "پخش اذان (آزمایش صدا)" ("Play azan (sound
  test)"). It plays the full recording immediately. It continues with the phone locked,
  and a stop control appears on the Lock Screen.
- Switch language: Settings tab → the first section, "ژبه / زبان" → "پښتو" or "دری".
  The whole interface changes immediately.
- Change city: Settings tab → "موقعیت" → "شهر", a list of 15 Afghan cities.
- Widgets: the app provides Home Screen and Lock Screen widgets showing the next prayer
  and a live countdown. Add them from the Home Screen in the usual way.

Permissions the app asks for, and why:

- Notifications, on first launch. Without it the azan cannot sound at prayer time. If
  refused, the app still shows all times and says on screen that the azan is off.
- Location, only when the Qibla tab is opened, and only to rotate the compass dial with
  the phone. The direction to the Kaaba is computed from the city the user selected, so
  the Qibla screen still gives the direction in degrees if location is refused. No
  location data is stored or transmitted.

4. EXTERNAL SERVICES

One, and only one:

- Aladhan Prayer Times API (https://aladhan.com/prayer-times-api). A free public API
  that requires no key and no account. The app sends the latitude and longitude of the
  city the user picked, a calculation method and a fiqh school, and receives that day's
  prayer times. Nothing identifying the user or the device is sent, and no location
  from the device is sent.

The app uses no analytics, no advertising network, no authentication service, no
payment processor, no AI service, and no third-party SDKs of any kind. There is no
server of ours; there is no account system; nothing is collected. This matches the
privacy manifest in the build and the App Privacy answers in App Store Connect.

5. REGIONAL DIFFERENCES

None. The app behaves identically in every region and on every storefront. The features,
the city list and the content are the same everywhere. There is no geo-gating, no
region-specific content, and no server-side configuration that could vary by region.

6. REGULATED INDUSTRY AND THIRD-PARTY MATERIAL

Namazia does not operate in a regulated industry. It provides no financial, medical,
gambling, or government services, and makes no claims that require a licence.

Third-party material included in the app:

- Vazirmatn typeface, by Saber Rastikerdar, licensed under the SIL Open Font License
  1.1, which permits bundling inside an application.
- The supplications (adhkar) are Qur'anic verses and well-known supplications from the
  standard hadith collections. These are religious texts in the public domain; the Dari
  and Pashto renderings of their meaning were written for this app.
- The two azan recordings are used under open licences, and are documented with their
  sources in the project:
  - The regular azan is "Beautiful adhan" by Adam-synagda, from Wikimedia Commons
    (https://commons.wikimedia.org/wiki/File:Beautiful_adhan.ogg), released under CC0
    1.0, a public domain dedication with no conditions.
  - The Fajr azan is "Morning call to prayer on Bodufolhudhoo, Maldives" by jrosin,
    from Freesound (https://freesound.org/people/jrosin/sounds/861625/), licensed under
    CC BY 4.0, which permits commercial use with attribution. The required credit is
    shown inside the app, in the Settings screen, and in the App Store description.

  Both were trimmed for length only; neither was otherwise altered.
```

---

## For the Notes field — a shorter version

**App Review Information → Notes caps at 4000 characters**, and the reply above is 5843,
so it cannot go there as it stands. Paste the full reply in the message thread, and this
condensed version — 3120 characters — into Notes.

It drops the parts that only answer *this* letter (the reasoning about the audience, the
screen recording) and keeps what a reviewer needs on every future submission: how to
reach each feature without an account, what the Dari tabs are, and where the content
comes from.

```
Namazia is a prayer-times app for Afghanistan. It shows Islamic prayer times for 15 Afghan cities, calls the azan at each prayer time, and gives the Qibla direction, a Hijri calendar and supplications. The interface is in Dari and Pashto, right to left.

NO ACCOUNT, NO PURCHASES
There is no account, login, subscription or in-app purchase, and no user-generated content. Every feature works on first launch, so no demo credentials exist.

THE FIVE TABS (right to left, as they appear)
  اوقات    Times     today's prayer times, with a countdown
  قبله     Qibla     compass pointing to the Kaaba
  تقویم    Calendar  a month of prayer times
  اذکار    Dhikr     supplications, and a tasbih counter
  تنظیمات  Settings  city, calculation, azan switches, language

HOW TO TEST THE MAIN FEATURES
- Hear the azan without waiting for a prayer time: Settings tab (gear icon) -> "پخش اذان (آزمایش صدا)" = "Play azan (sound test)". It plays immediately, continues with the phone locked, and shows a stop control on the Lock Screen.
- Switch language: Settings tab -> first section -> "پښتو" or "دری". The whole interface changes at once.
- Change city: Settings tab -> "موقعیت" -> "شهر", a list of 15 Afghan cities.
- Widgets: Home Screen and Lock Screen widgets show the next prayer and a live countdown.

PERMISSIONS
- Notifications, asked on first launch: without it the azan cannot sound at prayer time. If refused, all times still show and the app says on screen that the azan is off.
- Location, asked only when the Qibla tab is opened, and only to rotate the compass dial with the phone. The direction to the Kaaba is computed from the city the user selected, so the screen still gives the direction in degrees if location is refused. No location data is stored or transmitted.

EXTERNAL SERVICES
One only: the Aladhan Prayer Times API (https://aladhan.com/prayer-times-api), a free public API needing no key or account. The app sends the coordinates of the selected city, a calculation method and a fiqh school, and receives that day's times. Nothing identifying the user or device is sent. No analytics, advertising, authentication, payment, AI services or third-party SDKs of any kind. Nothing is collected, matching the privacy manifest and the App Privacy answers.

REGIONAL DIFFERENCES
None. Identical features and content in every region and storefront. No geo-gating and no server-side configuration.

THIRD-PARTY MATERIAL
- Regular azan: "Beautiful adhan" by Adam-synagda, Wikimedia Commons, CC0 1.0 (public domain dedication).
- Fajr azan: "Morning call to prayer on Bodufolhudhoo, Maldives" by jrosin, Freesound, CC BY 4.0. The required credit is shown in the app's Settings screen and in the App Store description.
- Vazirmatn typeface by Saber Rastikerdar, SIL Open Font License 1.1, which permits bundling in an app.
- The supplications are Qur'anic verses and well-known supplications from the standard hadith collections — public domain religious texts. Their Dari and Pashto renderings were written for this app.

Namazia does not operate in a regulated industry and makes no claims requiring a licence.
```

---

## The azan audio

Already answered in the reply above. Both recordings carry open licences and their
sources are recorded in `licenses/azan-recordings.md`; the CC BY credit the Fajr
recording requires is in the app's Settings screen and in the store description, and
has to stay there as long as that recording ships.

---

## The screen recording

Made on a real iPhone, not a simulator — Apple asks for this explicitly.

1. Settings → Control Center → add **Screen Recording**
2. Open Control Center, press the record button, wait for the countdown, then go to the
   Home Screen
3. **Start by launching the app from the Home Screen** — Apple asks for this specifically
4. Then, calmly, about two minutes total:
   - The Times tab: the countdown, then scroll through the list of prayers
   - The Qibla tab: allow location, turn the phone so the needle moves
   - The Calendar tab: scroll, step to the next month
   - The Dhikr tab: open a supplication, then the tasbih, tap it a few times
   - The Settings tab: switch the language to Pashto and back, change the city, then
     press "پخش اذان" so the azan is heard
   - Lock the phone while the azan plays, so the Lock Screen control is visible
   - Unlock, go to the Home Screen, show the widget
5. Stop the recording, then attach the video in the App Store Connect reply

One more thing worth checking while you are there: Apple's note mentions Guideline 2.3.3
— screenshots must show the app in use. If any of your uploaded screenshots is the iOS
Home Screen with the widget on it rather than a screen of the app itself, replace it.
