# Project State

Last baseline review: **2026-09-23**

## Source provenance

This repository was reconstructed from the earlier `UX Demonstration` workspace used for the Google Photos Ambient API partner-review prototype. The archive intentionally excluded `node_modules` and generated review media such as screenshots/videos.

There was no `.git` directory or GitHub remote in the recovered workspace, so prior commit history is unavailable. This repository is the canonical source history from this baseline forward.

## Verified from source inspection

### Implemented

- Native Android TV application in Kotlin.
- Jetpack Compose UI plus Compose for TV dependencies.
- D-pad/remote-oriented screens for the complete review flow.
- `AmbientPhotosRepository` interface.
- Deterministic `MockAmbientPhotosRepository` used by `MainActivity`.
- `GoogleAmbientPhotosRepository` production skeleton.
- `AmbientSlideshowController`.
- Coil-based image loading.
- Media3/ExoPlayer video playback.
- Photo and video ambient screens.
- Settings/disconnect UX.
- Browser-based 1920x1080 UX simulator and Playwright screenshot tooling.

### Not production-ready

- `MainActivity` currently instantiates `MockAmbientPhotosRepository` directly.
- Production authorization methods remain intentionally incomplete while credentials/access are unavailable.
- Device polling/media listing/disconnect/update methods in the production repository are placeholders.
- The QR component is procedural demonstration artwork, not a real encoded QR implementation.
- Production credential/token storage does not exist.
- Weather is not implemented.
- Advanced playback settings are not implemented.
- Production error/retry/offline behavior is not implemented.
- Automated Android tests are minimal/absent in this reconstructed baseline.

## Repository/bootstrap status

- Repository: `Radioffka/ambient-gallery`.
- Canonical Gradle version: **8.5**.
- `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` were restored from the official Gradle `v8.5.0` source tree during repository bootstrap.
- GitHub Actions validates the wrapper and uses `./gradlew :app:assembleDebug`.
- The reconstructed source tree was checked for obvious credentials/secrets before publication; none were found.
- GitHub Actions run `35893195997` verified JDK 17, Android SDK 34 installation, Gradle wrapper validation, Gradle cache setup, and `./gradlew :app:assembleDebug`; the debug APK build step completed successfully.

## Google API status

The official Ambient API documentation describes the service endpoint `https://photosambient.googleapis.com`, device resources, media listing, and the OAuth scope `https://www.googleapis.com/auth/photosambient.mediaitems`.

Production access/credentials remain pending the Google Photos Partner Program process. Do not implement speculative authentication workarounds.

## Immediate next step

Run the baseline verification task in `CODEX_BOOTSTRAP_PROMPT.md`:

1. establish a reproducible debug build,
2. fix only build-blocking baseline issues,
3. add a small number of high-value tests where useful,
4. verify repository hygiene and CI,
5. update this state file with exact verified results.
