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

**Initial result: partially available.** The previous blanket statement that no API configuration exists was outdated. A subsequent authenticated test is recorded below.

- Google's current [Ambient API reference](https://developers.google.com/photos/ambient/reference/rest), [configuration guide](https://developers.google.com/photos/ambient/guides/configure-your-app), and [partner-program overview](https://developers.google.com/photos/partner-program/overview) still document `https://photosambient.googleapis.com`, the `https://www.googleapis.com/auth/photosambient.mediaitems` scope, and partner-program access requirements.
- In the signed-in Google Cloud Console, project **Google Photos Ambient TV** (`dark-berm-505822-s3`) shows **Google Photos Ambient API / `photosambient.googleapis.com`: Enabled**. It has an existing OAuth 2.0 client named **Ambient Gallery TV**, type **TV and limited input**. No client ID or secret is stored in this repository.
- A real `POST https://oauth2.googleapis.com/device/code` using that existing client ID and the Ambient scope returned **HTTP 200** with `device_code`, `user_code`, `expires_in: 1800`, `interval: 5`, and `verification_url: https://www.google.com/device`. The one-time codes were redacted and not saved. This verifies device-code issuance for the client and requested scope; it does not prove that user consent or an Ambient API call will succeed.
- A real unauthenticated `GET https://photosambient.googleapis.com/v1/devices/access-check` returned **HTTP 401** with `status: UNAUTHENTICATED`, `reason: CREDENTIALS_MISSING`, and method `google.photos.ambient.v1.PhotosThirdPartyAmbientService.GetDevice`. Google's message was: "Request is missing required authentication credential. Expected OAuth 2 access token, login cookie or other valid authentication credential. See https://developers.google.com/identity/sign-in/web/devconsole-project." This confirms that the documented route is reachable and requires credentials; it is not a partner-access denial.
- Cloud Console shows the OAuth app as **External / In production**, with **1 user / 100 user cap**. Its Ambient Photos scope is marked **"This scope is not yet verified"**. The Verification centre says **"Your app's data access is not verified"**, **"Your branding is not being shown to users"**, and **"You need to verify and publish your branding before you can request verification."** These are current approval/public-use blockers; they do not by themselves establish whether a consenting test user can use the API.
- The repository has no OAuth configuration or token-injection path, its GitHub repository has no repository-level Actions secrets or variables, and no matching Google credential environment variables or `gcloud` installation were available in this workspace. `MainActivity` still uses `MockAmbientPhotosRepository`; `GoogleAmbientPhotosRepository` is a skeleton. No consent flow, token exchange, authenticated `devices` call, or `mediaItems.list` call was performed. Partner-program acceptance status was not independently verified.

### Authenticated test with the existing TV client (2026-09-23)

**Result: the OAuth device flow and Ambient device API work for the signed-in test account.** This verifies technical access for this client/account; it does not complete public OAuth verification, media-source selection, or production app integration.

- Google's [device-flow documentation](https://developers.google.com/identity/protocols/oauth2/limited-input-device) requires `client_secret` when polling the token endpoint. Polling without it returned **HTTP 400**: `{"error":"invalid_request","error_description":"Missing required parameter: client_secret"}`. The existing secret was masked and unrecoverable in Cloud Console, so a new secret was added to the existing **Ambient Gallery TV** client solely for this test.
- After consent by the signed-in Google account, `POST https://oauth2.googleapis.com/token` returned **HTTP 200**, `token_type: Bearer`, `expires_in: 3599`, and `scope: https://www.googleapis.com/auth/photosambient.mediaitems`. Access and refresh tokens were never printed or committed.
- An authenticated `GET /v1/devices/{random UUID}` returned **HTTP 400** with `status: INVALID_ARGUMENT`, `reason: REQUEST_INVALID`, and message `Invalid request found. See error details for a description of each violation.` A made-up UUID is not a valid device ID, so this response did not establish device access. That first token was discarded.
- A fresh device authorization again returned `device/code` **HTTP 200**. Token polling returned `428 authorization_pending` multiple times, including after the user reported Google consent success, before eventually returning **HTTP 200** with the same Bearer token type, 3599-second lifetime, and Ambient scope. No `slow_down` or denial response was observed.
- Using that token, `POST /v1/devices?requestId={new UUID v4}` with body `{"displayName":"Ambient Gallery API Test"}` returned **HTTP 200**. The response contained `id`, `displayName`, `mediaSourcesSet`, `settingsUri`, `createTime`, and `pollingConfig`; `mediaSourcesSet` was `false`.
- `GET /v1/devices/{returned id}` returned **HTTP 200** with the same field set and `mediaSourcesSet: false`. `DELETE /v1/devices/{returned id}` returned **HTTP 200** and `{}`, removing the temporary device. IDs and the settings URI were not recorded. No personal media was requested; `mediaItems.list` was not tested because no sources were selected.
- The downloaded test credential JSON was removed from the machine after the test. At the user's request, the new Cloud Console client secret remains enabled alongside the older secret. Its value was briefly offered for one-time copy/download; after leaving that Console page, Cloud Console states that established secret values cannot be viewed or downloaded. No durable local copy was retained. No credentials, codes, tokens, IDs, or media were added to Git. The Android app still runs with the mock repository, and no application features were changed.

The OAuth app's Ambient scope remains unverified in Cloud Console, and its verification centre still lists branding and data-access review blockers. Successful calls here establish test-account API functionality, not public launch readiness or formal partner-program status.

### Media-source and media-list test (2026-09-23)

**Result so far: source selection succeeded, but real media metadata has not yet been returned.** This was a direct API probe using a temporary device, not an Android app change.

- A new device authorization for the signed-in test account returned `device/code` **HTTP 200**. After the user approved consent, token polling returned **HTTP 200** with a Bearer token, `expires_in: 3599`, and the Ambient scope. The access and refresh tokens were kept out of Git and logs.
- `POST /v1/devices?requestId={new UUID v4}` with `{"displayName":"Ambient Gallery Media Test"}` returned **HTTP 200** with a temporary device ID, `settingsUri`, `mediaSourcesSet: false`, and `pollingConfig.pollInterval: "5s"`.
- Before source selection, authenticated `GET /v1/mediaItems?deviceId={returned id}&pageSize=10` returned **HTTP 400**: `{"error":{"code":400,"message":"This device cannot stream media content because no media sources have been selected by the user. Please prompt the user to update their settings from the Google Photos app.","status":"FAILED_PRECONDITION","details":[{"@type":"type.googleapis.com/google.rpc.ErrorInfo","reason":"PENDING_USER_ACTION","domain":"photos.googleapis.com"}]}}`.
- The user opened the temporary device's `settingsUri` and reported selecting an album containing a photo and a video in Google Photos. Subsequent `GET /v1/devices/{id}` returned **HTTP 200** with `mediaSourcesSet: true` and one media source. This verifies that the selection reached the Ambient device API; the source ID and album name were not recorded.
- The first authenticated `GET /v1/mediaItems?deviceId={returned id}` after selection returned **HTTP 200** with the empty JSON object `{}`: no `mediaItems` or `nextPageToken`. The test script interpreted this as zero items. The temporary device was then removed with `DELETE /v1/devices/{id}` **HTTP 200**, response `{}`. This single immediate list call does not establish whether media will appear after a delay or whether a direct `mediaSourceId` query is needed.
- The local download of the one-time OAuth test secret was deleted after that run. Cloud Console now masks both enabled secrets and says established values cannot be downloaded; there is no retained local credential. A repeat authorization currently needs a locally saved credential or replacement of the new test secret. The older secret remains untouched. No media IDs, URLs, or personal metadata have been recorded.

## Immediate next step

Technical test-account access and media-source selection are verified, but `mediaItems.list` has not yet returned metadata. Repeat the temporary-device test when a usable OAuth client secret is available, allowing for propagation after source selection and trying the selected `mediaSourceId`; confirm photo and video metadata separately. Then address the OAuth branding/scope verification blockers and document the approved production authentication model before implementing Phase 2. The repository/CI baseline build was already verified as recorded above; any additional baseline tests remain separate roadmap work.
