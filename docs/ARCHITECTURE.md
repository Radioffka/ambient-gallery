# Architecture

## Principles

Ambient Gallery uses a deliberately small architecture. The TV UI should not know whether media comes from mock data or the production Google Photos Ambient API, and playback should not contain authentication/network concerns.

## Main areas

### UI

`app/src/main/java/com/ambienttv/photosambient/ui/`

Compose-based screens and reusable TV components. Navigation is coordinated by `AmbientNavHost`.

### Data / Google Photos boundary

`app/src/main/java/com/ambienttv/photosambient/data/`

`AmbientPhotosRepository` is the contract consumed by the UI. The repository currently has two implementations:

- `MockAmbientPhotosRepository`: deterministic prototype/review behavior.
- `GoogleAmbientPhotosRepository`: TV OAuth, Ambient device creation/polling, media listing, token refresh and device deletion. It is selected by default when local build credentials are provided.

Production code does not fall back to mock behavior after a real API error. Mock mode requires the explicit `-PambientDemoMode=true` build option.

### Playback

`app/src/main/java/com/ambienttv/photosambient/slideshow/AmbientSlideshowController.kt`

Owns high-level ambient playback state such as current media, play/pause, photo timing, and transient overlay behavior. Video rendering uses Media3/ExoPlayer in the UI layer.

## Desired dependency direction

```text
UI -> repository interface
UI -> slideshow controller
production repository -> HTTP + Android Keystore-backed session store
mock repository -> deterministic in-memory demo data
```

Avoid:

```text
UI -> Google HTTP calls directly
slideshow controller -> OAuth/token logic
production repository -> mock fallback
```

## Configuration direction

A future bootstrap step should remove the hardcoded repository instantiation from `MainActivity` and select implementations through a simple build/configuration mechanism. Keep it lightweight. A full dependency-injection framework is not required unless complexity later justifies one.

## Persistence direction

The live repository encrypts the refresh/access tokens and device ID with an Android Keystore AES-GCM key. It refreshes access tokens and resumes a pending source-selection session after app restart. Android backup is disabled so ciphertext is not restored without its device-bound key. The OAuth client secret is supplied only at build time from a local file or environment variables and is embedded in the configured APK because Google's TV token endpoint requires it; treat the APK as private test material.

## Simulator

`simulator/` is a review/development aid and is not the production application. It should remain useful for fast visual review but must not become the source of truth for runtime behavior.
