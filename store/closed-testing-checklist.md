# Closed testing → production: what Play actually requires

## The rule that catches everyone

If your developer account is a **personal** account created on or after 13 November 2023,
Google will not let you apply for production access until you have run a closed test with:

- **at least 12 testers**, who
- **opted in and stayed opted in**, for
- **14 continuous days**

The 14 days do not start when you create the track. They start when you have 12 testers
actually opted in. If the count drops below 12, the clock resets. Organisation (company)
accounts are exempt from this requirement.

Plan for this: the 14 days are unavoidable, so start the closed test as early as possible
rather than polishing first.

---

## Steps

### 1. Create the closed testing release

`Test and release → Testing → Closed testing`

There is a default track called **Alpha**. Use it, or create a new track.

Rather than uploading the bundle again, promote what you already have:
`Latest releases and bundles → your internal testing release → Promote release → Closed
testing → Alpha`. Same bundle, no rebuild, no version code bump needed.

### 2. Add at least 12 testers

On the closed track, open the **Testers** tab.

Choose **Email list** and create a list containing at least 12 Google account addresses.
Aim for 14 or 15 so you have slack if someone drops out or never accepts.

Each address must be a real Google account — the same account the person uses on their
Android device. An address that is not a Google account cannot opt in.

### 3. Roll out, then send the opt-in link

Save the release and roll it out to the track. Copy the **opt-in URL** shown on the Testers
tab.

Every tester must, on their own device:
1. Open the opt-in link
2. Tap **Become a tester** / accept
3. Install the app from Play

**Being on the email list is not enough.** Google counts only testers who have accepted.
This is the single most common reason the 14-day clock never starts.

Ask each tester to confirm they installed it, and keep the app installed for the full two
weeks.

### 4. Wait out the 14 days, then apply

`Dashboard → Apply for production access`

The form asks about your closed test — how you recruited testers and what feedback you
acted on. Write real answers; a generic reply gets rejected and the reapplication takes
longer than the test did.

---

## What must also be finished before production

These are independent of the 14-day clock — do them during the wait.

| Item | Where | Status |
|---|---|---|
| App icon (512×512) | Store listing | Done — `store/play-icon-512.png` |
| Feature graphic (1024×500) | Store listing | Done — `store/play-feature-1024x500.png` |
| Store listing text | Store listing | Done — `store/play-store-listing-en.md` |
| **Screenshots, at least 2** | Store listing | **You must take these on a device** |
| **Privacy policy URL** | App content → Privacy policy | Text ready — `store/privacy-policy.md`, needs hosting |
| Data safety form | App content | Answers ready — see the listing doc |
| Content rating questionnaire | App content | Answer no to every content question |
| Exact alarm declaration | App content | Justification ready — see the listing doc |
| Foreground service declaration | App content | Justification ready — see the listing doc |
| Target audience / ads | App content | No ads; audience 13+ |

### Hosting the privacy policy

Play needs a public URL, not a file. The quickest free route is GitHub Pages:

1. In the repository, `Settings → Pages`, source: `main` branch, `/root`
2. Add `privacy-policy.md` at the repo root (or in `/docs`)
3. The URL becomes `https://<username>.github.io/<repo>/privacy-policy`

Paste that URL into `App content → Privacy policy`, and into the store listing's privacy
policy field.

### Screenshots

Minimum two, 16:9 or 9:16, each side between 320 and 3840 px. Phone screenshots are taken
in the emulator with `Ctrl/Cmd + S`, or on a device the normal way.

Best four to show, in this order:
1. **Home** — the countdown ring with the next prayer
2. **Monthly calendar** — shows the app has depth
3. **Qibla compass** — the most visually distinctive screen
4. **Dhikr** — with a category expanded

Take them with the app in light mode on a clean device (no notification clutter in the
status bar).
