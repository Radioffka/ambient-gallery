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

## Google Photos Ambient API access check (2026-09-23)

**Result: partially available; end-to-end use is not yet verified.** The previous blanket statement that no API configuration exists is outdated, but this check did not obtain an Ambient-scoped access token or successfully call an authenticated Ambient resource.

- Google's current [Ambient API reference](https://developers.google.com/photos/ambient/reference/rest), [configuration guide](https://developers.google.com/photos/ambient/guides/configure-your-app), and [partner-program overview](https://developers.google.com/photos/partner-program/overview) still document `https://photosambient.googleapis.com`, the `https://www.googleapis.com/auth/photosambient.mediaitems` scope, and partner-program access requirements.
- In the signed-in Google Cloud Console, project **Google Photos Ambient TV** (`dark-berm-505822-s3`) shows **Google Photos Ambient API / `photosambient.googleapis.com`: Enabled**. It has an existing OAuth 2.0 client named **Ambient Gallery TV**, type **TV and limited input**. No client ID or secret is stored in this repository.
- A real `POST https://oauth2.googleapis.com/device/code` using that existing client ID and the Ambient scope returned **HTTP 200** with `device_code`, `user_code`, `expires_in: 1800`, `interval: 5`, and `verification_url: https://www.google.com/device`. The one-time codes were redacted and not saved. This verifies device-code issuance for the client and requested scope; it does not prove that user consent or an Ambient API call will succeed.
- A real unauthenticated `GET https://photosambient.googleapis.com/v1/devices/access-check` returned **HTTP 401** with `status: UNAUTHENTICATED`, `reason: CREDENTIALS_MISSING`, and method `google.photos.ambient.v1.PhotosThirdPartyAmbientService.GetDevice`. Google's message was: "Request is missing required authentication credential. Expected OAuth 2 access token, login cookie or other valid authentication credential. See https://developers.google.com/identity/sign-in/web/devconsole-project." This confirms that the documented route is reachable and requires credentials; it is not a partner-access denial.
- Cloud Console shows the OAuth app as **External / In production**, with **1 user / 100 user cap**. Its Ambient Photos scope is marked **"This scope is not yet verified"**. The Verification centre says **"Your app's data access is not verified"**, **"Your branding is not being shown to users"**, and **"You need to verify and publish your branding before you can request verification."** These are current approval/public-use blockers; they do not by themselves establish whether a consenting test user can use the API.
- The repository has no OAuth configuration or token-injection path, its GitHub repository has no repository-level Actions secrets or variables, and no matching Google credential environment variables or `gcloud` installation were available in this workspace. `MainActivity` still uses `MockAmbientPhotosRepository`; `GoogleAmbientPhotosRepository` is a skeleton. No consent flow, token exchange, authenticated `devices` call, or `mediaItems.list` call was performed. Partner-program acceptance status was not independently verified.

**Next access test:** with an authorized Google account and an approved plan for handling the Photos scope, complete the device consent/token exchange and call an authenticated Ambient resource. Record Google's exact result before treating Phase 2 as unblocked. Keep tokens, codes, client secrets, and personal media out of Git.

## Immediate next step

Complete the access test above when user authorization and verification status permit it. Until an authenticated Ambient API call succeeds, retain the mock runtime and avoid speculative production behavior. The repository/CI baseline build was already verified as recorded above; any additional baseline tests remain separate roadmap work.
