#!/usr/bin/env bash
#
# Archive Namazia and send it to App Store Connect, in one command.
#
#   ./scripts/upload.sh              archive, export, upload
#   ./scripts/upload.sh --dry-run    archive and export only, no upload
#   ./scripts/upload.sh --build 7    use build number 7 instead of the next one
#
# Read scripts/README.md once before the first run — it covers the API key.

set -euo pipefail

cd "$(dirname "$0")/.."          # ios/
ROOT="$PWD"
BUILD_DIR="$ROOT/build"
ARCHIVE="$BUILD_DIR/Namazia.xcarchive"
EXPORT_DIR="$BUILD_DIR/export"

# ---------------------------------------------------------------- arguments

DRY_RUN=false
FORCED_BUILD=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --dry-run) DRY_RUN=true; shift ;;
        --build)   FORCED_BUILD="${2:-}"; shift 2 ;;
        -h|--help) sed -n '3,10p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
        *) echo "unknown option: $1" >&2; exit 2 ;;
    esac
done

# ---------------------------------------------------------------- config

# Secrets come from scripts/.env, which is gitignored. Nothing in this file or
# in project.yml ever holds a key, an issuer id, or a team id.
if [[ -f "$ROOT/scripts/.env" ]]; then
    set -a; source "$ROOT/scripts/.env"; set +a
fi

missing=()
for var in ASC_KEY_ID ASC_ISSUER_ID ASC_KEY_PATH DEVELOPMENT_TEAM; do
    [[ -n "${!var:-}" ]] || missing+=("$var")
done
if [[ ${#missing[@]} -gt 0 ]]; then
    echo "Missing: ${missing[*]}" >&2
    echo "Set them in ios/scripts/.env — see ios/scripts/README.md." >&2
    exit 1
fi

if [[ ! -f "$ASC_KEY_PATH" ]]; then
    echo "ASC_KEY_PATH does not exist: $ASC_KEY_PATH" >&2
    exit 1
fi

command -v xcodegen >/dev/null || { echo "xcodegen not installed: brew install xcodegen" >&2; exit 1; }
command -v xcodebuild >/dev/null || { echo "xcodebuild not found — install Xcode" >&2; exit 1; }

# ---------------------------------------------------------------- build number

# App Store Connect refuses a build number it has already seen, even from a
# build that was never released. Bumping it here rather than by hand is what
# stops the most common failed upload of all.
if [[ -n "$FORCED_BUILD" ]]; then
    BUILD_NUMBER="$FORCED_BUILD"
else
    CURRENT=$(grep -E '^[[:space:]]*CURRENT_PROJECT_VERSION:' project.yml \
              | head -1 | sed -E 's/.*"([0-9]+)".*/\1/')
    BUILD_NUMBER=$((CURRENT + 1))
fi

sed -i '' -E "s/(CURRENT_PROJECT_VERSION: )\"[0-9]+\"/\1\"$BUILD_NUMBER\"/" project.yml

MARKETING=$(grep -E '^[[:space:]]*MARKETING_VERSION:' project.yml \
            | head -1 | sed -E 's/.*"([^"]+)".*/\1/')

echo "▸ Namazia $MARKETING (build $BUILD_NUMBER)"

# ---------------------------------------------------------------- archive

echo "▸ Generating project"
xcodegen generate --quiet

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

echo "▸ Archiving (a few minutes)"
xcodebuild archive \
    -project Namazia.xcodeproj \
    -scheme Namazia \
    -configuration Release \
    -destination 'generic/platform=iOS' \
    -archivePath "$ARCHIVE" \
    -allowProvisioningUpdates \
    -authenticationKeyPath "$ASC_KEY_PATH" \
    -authenticationKeyID "$ASC_KEY_ID" \
    -authenticationKeyIssuerID "$ASC_ISSUER_ID" \
    DEVELOPMENT_TEAM="$DEVELOPMENT_TEAM" \
    | tail -5

# ---------------------------------------------------------------- export

# `destination: upload` makes the export step hand the build straight to App
# Store Connect, so there is no separate altool call — altool is deprecated and
# this is the path Xcode itself uses.
DESTINATION="upload"
$DRY_RUN && DESTINATION="export"

OPTIONS="$BUILD_DIR/ExportOptions.plist"
cat > "$OPTIONS" <<PLIST
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key><string>app-store-connect</string>
    <key>destination</key><string>$DESTINATION</string>
    <key>teamID</key><string>$DEVELOPMENT_TEAM</string>
    <key>signingStyle</key><string>automatic</string>
    <key>uploadSymbols</key><true/>
    <key>manageAppVersionAndBuildNumber</key><false/>
</dict>
</plist>
PLIST

if $DRY_RUN; then
    echo "▸ Exporting (no upload)"
else
    echo "▸ Exporting and uploading"
fi

xcodebuild -exportArchive \
    -archivePath "$ARCHIVE" \
    -exportOptionsPlist "$OPTIONS" \
    -exportPath "$EXPORT_DIR" \
    -allowProvisioningUpdates \
    -authenticationKeyPath "$ASC_KEY_PATH" \
    -authenticationKeyID "$ASC_KEY_ID" \
    -authenticationKeyIssuerID "$ASC_ISSUER_ID" \
    | tail -5

# ---------------------------------------------------------------- done

echo
if $DRY_RUN; then
    echo "✓ Build $BUILD_NUMBER exported to ios/build/export — nothing uploaded."
else
    echo "✓ Build $BUILD_NUMBER uploaded."
    echo "  It takes 10–30 minutes to finish processing before it can be picked"
    echo "  in App Store Connect or TestFlight. You get an email either way."
fi
echo
echo "  Commit the build number so the next run starts from the right place:"
echo "      git commit -am \"iOS build $BUILD_NUMBER\""
