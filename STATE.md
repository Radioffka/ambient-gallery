# Project State

Last baseline review: **2026-09-23**

## Source provenance

This repository is being reconstructed from the earlier `UX Demonstration` workspace used for the Google Photos Ambient API partner-review prototype. The archive intentionally excluded `node_modules` and generated review media such as screenshots/videos.

There was no `.git` directory or GitHub remote in the recovered workspace, so prior commit history is unavailable. This repository becomes the canonical source history from this baseline forward.

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
- Production authorization methods throw `UnsupportedOperationException` while credentials/access are unavailable.
- Device polling/media listing/disconnect/update methods in the production repository are placeholders.
- The QR component is procedural demonstration artwork, not a real encoded QR implementation.
- Production credential/token storage does not exist.
- Weather is not implemented.
- Advanced playback settings are not implemented.
- Production error/retry/offline behavior is not implemented.
- Automated Android tests are minimal/absent in this recovered baseline.

## Build/bootstrap finding

The recovered archive contains `gradle/wrapper/gradle-wrapper.properties` configured for Gradle 8.5, but **does not contain `gradle-wrapper.jar`**. The recovered POSIX `gradlew` file is also incomplete and should not be trusted.

The repository CI therefore uses an installed Gradle 8.5 initially. The first bootstrap task is to regenerate a canonical Gradle 8.5 wrapper from a trusted Gradle installation and commit:

- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

After that, CI should be switched to `./gradlew` as the primary path.

A full Android build was **not executed during repository reconstruction** because the reconstruction environment did not contain an Android SDK or Gradle installation.

## Google API status

The official Ambient API documentation describes the service endpoint `https://photosambient.googleapis.com`, device resources, media listing, and the OAuth scope `https://www.googleapis.com/auth/photosambient.mediaitems`.

Production access/credentials remain pending the Google Photos Partner Program process. Do not implement speculative authentication workarounds.

## Immediate next step

Run the repository bootstrap task in `CODEX_BOOTSTRAP_PROMPT.md`:

1. restore a canonical Gradle wrapper,
2. establish a reproducible debug build,
3. fix only build-blocking baseline issues,
4. add basic tests where they provide immediate confidence,
5. update this state file with verified results.