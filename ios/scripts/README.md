# One-command upload

```bash
cd ios
./scripts/upload.sh
```

Regenerates the project, bumps the build number, archives, and hands the build to
App Store Connect. `--dry-run` stops before the upload; `--build N` pins a build
number instead of taking the next one.

Setup is once. After that this replaces the whole Product → Archive → Distribute
dance in Xcode.

---

## Once: the App Store Connect API key

The key is what lets the script upload **without your Apple ID password and without a
two-factor prompt**.

1. [appstoreconnect.apple.com](https://appstoreconnect.apple.com) → **Users and Access**
   → **Integrations** → **App Store Connect API** → **Team Keys**
2. **+** → Name: `Namazia upload` → Access: **Admin** → Generate

   Admin, not App Manager, because the script signs automatically: with no Apple
   Distribution certificate in the keychain, Xcode creates one or signs with a
   cloud-managed one, and both are refused to an App Manager key. Admin also means the
   key can do nearly anything to the account — treat the `.p8` like a password, and
   revoke it on the same page if it is ever exposed.
3. Download the `AuthKey_XXXXXXXXXX.p8`. **Apple lets you download it once.** Put it
   somewhere outside the repo:

   ```bash
   mkdir -p ~/.appstoreconnect/private_keys
   mv ~/Downloads/AuthKey_*.p8 ~/.appstoreconnect/private_keys/
   ```

4. From that same page copy the **Key ID** (next to the key) and the **Issuer ID**
   (above the table).

## Once: `ios/scripts/.env`

```bash
cp scripts/env.example scripts/.env
```

Then fill it in:

```bash
ASC_KEY_ID=XXXXXXXXXX
ASC_ISSUER_ID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
ASC_KEY_PATH=/Users/you/.appstoreconnect/private_keys/AuthKey_XXXXXXXXXX.p8
DEVELOPMENT_TEAM=27RXPRW77S
```

`.env` is gitignored, and so is every `.p8`. Nothing in the repo — not this file, not
`project.yml` — holds a key or a team id, which is why `project.yml` still leaves
`DEVELOPMENT_TEAM` empty.

---

## What the script does that is easy to forget

- **Bumps the build number.** App Store Connect refuses a build number it has already
  seen, even from a build that was never released — the single most common failed
  upload. The new number is written into `project.yml`, so commit it afterwards or the
  next run starts from the old one.
- **Uploads through `xcodebuild -exportArchive`** with `destination: upload`, which is
  the path Xcode itself uses. `altool` is deprecated and is not involved.
- **Leaves the marketing version alone.** `MARKETING_VERSION` is the number people see
  and only changes for a real release; edit it by hand in `project.yml`.

## After it finishes

Processing takes 10–30 minutes before the build can be selected in App Store Connect or
TestFlight. Apple emails either way — including when a build is rejected at that stage,
usually for a missing icon size or a bad Info.plist value.

---

## Not tested on a Mac

This script was written on Linux and has never been run. The first run may need a small
fix — the likely candidates are the `sed -i ''` build-number bump (BSD sed syntax) and
whether your Xcode version accepts `method: app-store-connect` (Xcode 15+) rather than
the older `app-store`. Run it with `--dry-run` first; that exercises everything except
the upload itself.
