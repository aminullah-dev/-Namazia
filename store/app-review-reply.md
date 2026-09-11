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
- The azan recordings: {{AZAN_AUDIO}}
```

---

## Before sending: the azan audio

Point 6 is the only one that cannot be answered from the repository. Replace
`{{AZAN_AUDIO}}` with whichever of these is true — and it must actually be true, because
Apple can ask for the documentation:

**If you recorded it, or a muezzin recorded it for you:**

> recorded for this app, with the permission of the muezzin. No third-party recording is
> used.

**If it came from a site that licenses it for reuse** (public domain, Creative Commons,
a royalty-free library):

> obtained from {source and URL}, which licenses the recording for use in applications
> under {licence}. A copy of the licence is available on request.

**If you do not know where it came from:** do not guess. Replace the recordings with
ones whose origin you can name before replying. An azan recording is someone's
performance and is copyrightable, and a wrong answer here is far worse than a delayed
submission.

Whatever the answer, write it down in the repository afterwards so the next submission
does not have to reconstruct it.

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
