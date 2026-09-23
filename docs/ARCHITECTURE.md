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
- `GoogleAmbientPhotosRepository`: production skeleton, intentionally incomplete while partner credentials/access are unavailable.

Production code must not silently fall back to mock behavior after a real API error. Mock mode must be an explicit development/demo choice.

### Playback

`app/src/main/java/com/ambienttv/photosambient/slideshow/AmbientSlideshowController.kt`

Owns high-level ambient playback state such as current media, play/pause, photo timing, and transient overlay behavior. Video rendering uses Media3/ExoPlayer in the UI layer.

## Desired dependency direction

```text
UI -> repository interface
UI -> slideshow controller
production repository -> HTTP/auth/persistence adapters (future)
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

The prototype is mostly in-memory. Production integration will require durable device/auth state. Add persistence only after the approved Google authentication requirements are known, especially for token handling.

Android Keystore-backed storage or encrypted app storage may be appropriate, but the exact design must follow the actual credentials/token model rather than assumptions.

## Simulator

`simulator/` is a review/development aid and is not the production application. It should remain useful for fast visual review but must not become the source of truth for runtime behavior.