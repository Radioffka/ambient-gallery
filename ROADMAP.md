# Ambient Gallery Roadmap

This roadmap is ordered to avoid mixing infrastructure, partner-gated API work, and product features.

Status (2026-09-23): a first live Phase 2 TV MVP is implemented for a privately configured debug APK. Real TV playback, a second-account consent test, Ambient video responses, and public OAuth verification are still open; see `STATE.md`.

## Phase 0: Repository bootstrap

Goal: make the recovered prototype a reliable development baseline.

- Restore a canonical Gradle 8.5 wrapper.
- Run and fix `:app:assembleDebug`.
- Add/repair essential CI.
- Remove machine-specific paths and stale prototype documentation.
- Add a small set of baseline tests for non-UI logic where practical.
- Record the verified state in `STATE.md`.

Exit criteria: clean clone -> documented prerequisites -> reproducible debug build.

## Phase 1: Production architecture hardening

Goal: prepare for live Google integration without inventing unavailable API behavior.

- Introduce dependency injection or another simple repository-selection mechanism so mock vs production is not hardcoded in `MainActivity`.
- Define explicit configuration for demo/development/production behavior.
- Add structured network/error abstractions without committing credentials.
- Add persistence interfaces for device/auth state, but only implement token storage after the approved auth requirements are known.
- Replace the procedural QR drawing with a real QR encoder for URLs supplied by the supported flow.

Exit criteria: production integration has clean seams and demo mode remains independently usable.

## Phase 2: Google Photos Ambient API integration

Gate: approved Google Photos Ambient API/Partner Program access and current official integration documentation.

- Implement approved OAuth flow.
- Create/recover Ambient devices safely using documented request IDs and device APIs.
- Show `settingsUri` correctly.
- Poll `mediaSourcesSet` according to returned configuration.
- Implement paginated `mediaItems.list` with documented quotas/behavior.
- Handle photo and video URLs according to current official base-URL rules.
- Implement device rename/update where supported.
- Implement disconnect/device deletion and documented token revocation/cleanup.
- Add robust error, expiry, retry, and re-authentication paths.

Exit criteria: real user account -> selected sources -> stable ambient playback -> clean disconnect.

## Phase 3: Ambient playback quality

Goal: make the app feel better than a basic slideshow.

- Preload upcoming media where appropriate.
- Minimize black frames/player recreation between media items.
- Improve image/video transitions.
- Respect aspect ratio and configurable fit/fill behavior.
- Define video duration/skip behavior for long clips.
- Add playback recovery for unavailable/expired/bad media URLs.
- Add configurable photo duration.
- Add shuffle/order behavior that remains compatible with the Ambient API contract.

## Phase 4: Product features

- Weather display with configurable location/provider behavior and unobtrusive UI.
- Clock/date options.
- Advanced overlay visibility/timing.
- Screen-safe options appropriate for long-running TV display.
- User-selectable playback settings.
- Better first-run/help experience.

## Phase 5: Production hardening

- Lifecycle/background behavior on Android TV.
- Network-loss recovery.
- Long-duration soak testing.
- Memory/player leak testing.
- Accessibility and D-pad focus audit.
- Release signing/configuration.
- Privacy policy and store metadata.
- Partner/compliance review updates as required.
- Play Store / distribution preparation only when explicitly authorized.
