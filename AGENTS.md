# AGENTS.md

## Mission

Ambient Gallery is evolving from a Google Photos Ambient API UX demonstration into a maintainable Android TV / Google TV application.

Preserve the useful prototype code. Do not rewrite the application from scratch unless a verified technical reason requires it.

The current highest priority is a reproducible, buildable repository with clear architecture and safe boundaries around the still-unavailable production Google Photos integration.

## Read first

Before changing code, read:

1. `README.md`
2. `STATE.md`
3. `ROADMAP.md`
4. `docs/ARCHITECTURE.md`

Update `STATE.md` whenever a task materially changes the verified project state or next step.

## Engineering rules

Act as a senior Android TV engineer.

- Prefer small, reviewable changes over broad rewrites.
- Keep the app remote-first. Every primary flow must work without touch input.
- Preserve separation between UI/playback and external APIs.
- Keep mock and production repositories clearly separated.
- Do not add a backend, database, analytics platform, ad SDK, account system, or cloud storage without an explicit product requirement.
- Do not commit generated build output, media captures, user photos, secrets, credentials, tokens, keystores, or machine-specific configuration.
- Avoid introducing a dependency when platform/standard-library functionality is sufficient.
- Run the relevant build/tests after meaningful changes and report exactly what was verified.
- If the build cannot be run, state why. Never claim a build/test passed without running it.

## Google Photos guardrails

Google Photos Ambient API behavior is a correctness boundary.

Before implementing or changing production API behavior:

- Verify current official Google Photos Ambient API documentation.
- Prefer official Google documentation over blogs, samples copied from unknown sources, or assumptions from other Google Photos APIs.
- Do not invent request/response fields, quotas, URL parameters, polling behavior, or authentication behavior.
- Treat partner-only behavior as unknown until it is documented or available in approved credentials/documentation.
- The current repository models `https://www.googleapis.com/auth/photosambient.mediaitems`, `v1.devices`, and `v1.mediaItems` because those are documented by the Ambient API.
- The generic Google limited-input-device OAuth documentation may list a narrower public set of scopes. Do not "fix" the project by deleting the Ambient scope merely because it is absent from the generic list; investigate the partner-access context first.

Until production access is available, production methods should fail explicitly or remain safely unimplemented rather than silently falling back to invented behavior.

## Privacy and security

The TV is a shared-display surface.

- Do not unnecessarily show a user's email address, full name, tokens, IDs, or other sensitive account details on TV screens.
- Do not log OAuth codes/tokens in production paths.
- Never hardcode OAuth secrets or long-lived credentials.
- Design token persistence only after the approved authentication model is known.
- Disconnect must remove local auth/device state and invoke the documented server-side cleanup/revocation path when production integration exists.

## Playback expectations

- Photos and videos are first-class ambient media types.
- Preserve aspect ratio.
- Avoid unnecessary black flashes, player recreation, or UI chrome during transitions.
- Use Media3/ExoPlayer for production video unless a verified requirement justifies a different player.
- Video end/error behavior must not stall the ambient playlist.
- Permanent branding must not obscure the user's ambient media.

## UI expectations

- Target Android TV / Google TV and a 1920x1080 10-foot experience.
- Maintain predictable D-pad focus and Back behavior.
- Avoid touch-only interactions.
- Settings should remain simple enough for a TV remote.
- Reviewer/debug-only UI must be clearly isolated from future production behavior.

## Git workflow

For agent work:

- Use a focused feature branch unless explicitly told to work directly on `main`.
- Make coherent commits with descriptive messages.
- Do not mix dependency upgrades, large refactors, and product features in one change.
- Prefer a PR with a concise summary, verification results, risks, and follow-up items.
- Do not rewrite published history or force-push unless explicitly instructed.

## Definition of done

A task is complete when:

1. requested behavior is implemented,
2. relevant build/tests have been run where the environment permits,
3. no secrets or generated artifacts were introduced,
4. documentation/state is updated if the project baseline changed,
5. unresolved blockers are stated explicitly.