# Development

## Android baseline

Use JDK 17 and Android SDK 34. The repository contains a canonical Gradle 8.5 wrapper restored during bootstrap from the official Gradle `v8.5.0` sources.

Verify the toolchain:

```bash
./gradlew --version
```

Build the debug APK:

```bash
./gradlew :app:assembleDebug
```

Run available checks/tests before opening a PR. Keep dependency upgrades separate from baseline-repair changes unless an upgrade is strictly required to restore a build.

## Simulator

The web simulator is optional and exists to demonstrate the TV UX quickly.

```bash
npm ci
npx playwright install chromium
npm run capture
```

`npm run build-video` additionally requires `ffmpeg` in `PATH`. Generated media is ignored by Git.

## Google Photos integration

The live repository is the default runtime. Use the existing Google Cloud **TV and limited input** OAuth client, not an Android or web OAuth client. Its JSON must have `installed.client_id` and `installed.client_secret`. Keep the JSON outside Git and build with:

```powershell
.\scripts\build-tv.ps1 -OAuthJsonPath 'C:\path\to\client_secret.json'
```

The resulting APK is `app\build\outputs\apk\debug\app-debug.apk`. With Android TV debugging enabled, install it using `adb install -r app\build\outputs\apk\debug\app-debug.apk`. The TV shows Google's device code; authorize with whichever Google account should provide the media, then scan the second QR code on a phone with Google Photos to select sources. The API may initially return an empty media page; the TV retries automatically. Disconnect in the app before switching accounts.

The build script passes credentials in process environment variables and does not print them. Gradle also accepts an ignored `ambient-oauth.properties` containing `clientId=...` and `clientSecret=...`. A configured APK contains the TV OAuth client values, so do not distribute it publicly. A build without credentials is useful for compilation but shows a configuration message at runtime. For the original deterministic demo, use `.\gradlew.bat :app:assembleDebug -PambientDemoMode=true`.

Actual video responses and `baseUrl=dv` access remain unverified. The player attempts `=dv` only if Ambient returns a video MIME type and skips playback errors. Public OAuth verification and partner approval are separate from this test build; a new Google account may be restricted by Google's consent screen.

Never commit OAuth credentials, access/refresh tokens, signing material, personal media, or private partner documentation.

## Git / PR discipline

Use focused branches and small coherent commits. Update `STATE.md` whenever a task materially changes the verified baseline or moves a roadmap phase forward.
