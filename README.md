# Ambient Gallery

Ambient Gallery is an Android TV / Google TV application for displaying a user's selected Google Photos media as a full-screen ambient experience. The long-term product goal is to combine high-quality photo playback, smooth video playback, privacy-safe Google Photos integration, weather information, and more playback/configuration controls than the standard Google TV Ambient experience.

This repository starts from the native Android TV UX prototype originally prepared for Google Photos Partner Program review. The Android TV MVP now uses the real Google Photos Ambient API by default. Test-account OAuth, device creation, source selection, and photo metadata were verified with direct API probes; public OAuth verification and Ambient video behavior remain unresolved.

## Current status

| Area | Status |
| --- | --- |
| Native Android TV / Google TV application | Implemented baseline |
| Compose for TV UI and D-pad navigation | Implemented baseline |
| Mixed photo + video slideshow | Implemented baseline |
| Media3 / ExoPlayer video playback | Implemented baseline |
| Google Photos TV setup flow | Real device OAuth and source selection |
| `AmbientPhotosRepository` abstraction | Implemented |
| Production `GoogleAmbientPhotosRepository` | MVP implementation |
| Real Google OAuth / Ambient API calls | Implemented; runtime TV test still needed |
| Real QR generation | Implemented for OAuth and `settingsUri` |
| Weather overlay/integration | Planned |
| Advanced slideshow settings | Planned |
| Encrypted token/device persistence | Implemented; extended recovery remains planned |
| Play Store / release hardening | Not started |

See [`STATE.md`](STATE.md) for the exact working baseline and [`ROADMAP.md`](ROADMAP.md) for the planned development sequence.

## Product direction

Ambient Gallery should eventually provide:

- Google Photos media selected by the user through the supported Ambient API flow.
- Photos and short videos in the same ambient playlist.
- Smooth, reliable video playback with correct aspect ratio and minimal transition disruption.
- A clean 10-foot TV interface controlled entirely by a remote.
- Privacy-safe setup and account state on a shared/public display.
- Weather information that does not dominate the media.
- More playback controls and configuration than the stock Ambient experience.
- No third-party media storage backend unless a future requirement makes one unavoidable.

## Architecture

The app intentionally separates UI/playback from Google Photos access:

```text
TV UI / Compose screens
        |
        v
AmbientNavHost
        |
        +------------------------+
        |                        |
        v                        v
AmbientPhotosRepository   AmbientSlideshowController
        |                        |
        +-- Mock implementation  +-- photo timing
        |                        +-- play/pause
        +-- Google API MVP       +-- transient overlay
                                 +-- Media3/ExoPlayer
```

The separation is important: Google Photos API behavior must never be guessed. Until approved production access is available, development should continue against the mock implementation while preserving the production interface.

More detail: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Existing user flow

The current UX prototype covers the partner-review path:

```text
Welcome
  -> Permission explanation
  -> Device name
  -> TV OAuth / QR setup demonstration
  -> Wait for media source configuration
  -> Connected state
  -> Photo/video ambient slideshow
  -> Settings
  -> Disconnect
```

A reviewer/debug quick-jump bar is also available in the prototype so each state can be inspected independently.

## Google Photos Ambient API boundary

The production service documented by Google uses:

```text
https://photosambient.googleapis.com
```

The codebase uses the documented `v1.devices` and `v1.mediaItems` resources and the OAuth scope:

```text
https://www.googleapis.com/auth/photosambient.mediaitems
```

Important: The OAuth app is not yet verified for public use. A different Google account may see an unverified-app warning or an access restriction even though the API worked with the original test account. See [STATE.md](STATE.md) for observed results and remaining blockers.

Official references:

- https://developers.google.com/photos/ambient/reference/rest
- https://developers.google.com/photos/ambient/guides/media-items
- https://developers.google.com/identity/protocols/oauth2/limited-input-device

## Repository layout

```text
.
├── .github/workflows/        # CI
├── app/                      # Native Android TV application
│   └── src/main/
│       ├── java/com/ambienttv/photosambient/
│       │   ├── data/         # models + repository implementations
│       │   ├── slideshow/    # ambient playback controller
│       │   └── ui/           # TV screens, components, theme, navigation
│       └── res/
├── docs/                     # architecture and development notes
├── simulator/                # browser-based 1920x1080 UX simulator
├── AGENTS.md                 # standing instructions for coding agents
├── STATE.md                  # current verified project state
├── ROADMAP.md                # ordered development plan
└── CODEX_BOOTSTRAP_PROMPT.md # first-task prompt for Codex
```

## Development prerequisites

Recommended local environment:

- Android Studio with Android SDK 34 installed.
- JDK 17 for Android/Gradle builds.
- Canonical Gradle 8.5 wrapper included in the repository.
- Node.js 20+ only if using the web UX simulator.
- `ffmpeg` in `PATH` only when building the optional review walkthrough video.

### Android build

The repository contains the canonical Gradle 8.5 wrapper. The wrapper scripts and `gradle-wrapper.jar` were restored during repository bootstrap from the official Gradle `v8.5.0` source tree.

```bash
./gradlew :app:assembleDebug
```

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

### UX simulator

```bash
npm ci
npx playwright install chromium
npm run capture
```

To build the optional screenshot walkthrough video, install `ffmpeg` and run:

```bash
npm run build-video
```

Generated screenshots/videos are intentionally ignored by Git.

## Secrets and credentials

Never commit:

- OAuth client secrets or tokens.
- refresh/access tokens.
- keystores or signing passwords.
- `local.properties`.
- private Partner Program documentation that is not intended for source control.
- personal Google Photos media.

To build a TV APK with an existing **TV and limited input** OAuth client JSON, keep that JSON outside Git and run:

```powershell
.\scripts\build-tv.ps1 -OAuthJsonPath 'C:\path\to\client_secret.json'
```

The script passes credentials to Gradle through process environment variables. Alternatively, create ignored `ambient-oauth.properties` with `clientId=...` and `clientSecret=...`, then run `.\gradlew.bat :app:assembleDebug`. The APK is at `app/build/outputs/apk/debug/app-debug.apk`; install it with `adb install -r <apk-path>`. The Google account is selected during device authorization on `google.com/device`, so the same configured APK can be tested with another consenting account. Disconnect on TV before switching accounts.

**A TV client secret is embedded in a configured APK and cannot be kept confidential from someone who has the APK.** Keep the client JSON and configured APK private; never commit either. A build without credentials succeeds but shows a configuration message instead of starting OAuth. For a visual demo without Google API access, run Gradle with `-PambientDemoMode=true`.

## Working with Codex

Codex should read these files before making changes:

1. [`AGENTS.md`](AGENTS.md)
2. [`STATE.md`](STATE.md)
3. [`ROADMAP.md`](ROADMAP.md)
4. [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)

The first task is defined in [`CODEX_BOOTSTRAP_PROMPT.md`](CODEX_BOOTSTRAP_PROMPT.md).

## Origin of this baseline

The initial source was reconstructed on 2026-09-23 from the previous `UX Demonstration` workspace. Node modules and generated photo/video review evidence were intentionally excluded. The original UX work predates this Git repository, so this repository should be treated as the canonical history from this baseline forward.
