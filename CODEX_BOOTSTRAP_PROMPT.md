# Codex bootstrap task

You are taking over the `ambient-gallery` repository, an Android TV / Google TV application reconstructed from a previous Google Photos Ambient API UX prototype.

Read `AGENTS.md`, `README.md`, `STATE.md`, `ROADMAP.md`, and `docs/ARCHITECTURE.md` before changing anything.

## Objective

Turn the recovered source into a **verified, reproducible development baseline**. Do not start major product features and do not implement speculative live Google Photos authentication/API behavior.

## Required work

1. Inspect the complete repository and confirm the recovered project structure.
2. Restore a canonical **Gradle 8.5 wrapper** from a trusted Gradle installation:
   - regenerate `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, and wrapper properties,
   - do not handcraft or download wrapper binaries from an untrusted source,
   - verify `./gradlew --version`.
3. Run `./gradlew :app:assembleDebug` with JDK 17 and Android SDK 34.
4. Fix any **build-blocking issues** in the recovered baseline. Keep these fixes narrow. Do not use this task for broad dependency upgrades or UI rewrites.
5. Run static checks/tests that already exist. If essentially no tests exist, add a small number of high-value JVM unit tests for deterministic, non-Android logic only where this can be done without a large refactor.
6. Inspect `MainActivity` and confirm that demo mode is currently explicit/hardcoded through `MockAmbientPhotosRepository`. Do not switch to the production repository yet.
7. Inspect `GoogleAmbientPhotosRepository`. Preserve its explicit incomplete state. Do not invent credentials, endpoints, fields, token handling, or partner behavior. If comments disagree with current official Google Ambient API documentation, correct comments/documentation only after checking official sources.
8. Confirm that no secrets, credentials, generated screenshots/videos, `node_modules`, local SDK paths, or machine-specific user paths are tracked.
9. Check the GitHub Actions workflow. Once the wrapper is restored and verified, update CI to use `./gradlew :app:assembleDebug` as the canonical build path and add official Gradle wrapper validation if appropriate.
10. Update `STATE.md` with exactly what you verified, including build/test commands and any remaining blockers.

## Constraints

- Preserve the existing UX prototype and architecture unless a concrete build/correctness issue requires a change.
- No backend service.
- No database.
- No analytics or ads.
- No Play Store/release work.
- No weather implementation yet.
- No production OAuth/Ambient API implementation until approved access and the relevant current documentation are available.
- Do not expose user email/full name in shared TV UI.
- Do not commit secrets or tokens.

## Git workflow

Work on a branch named `codex/bootstrap-baseline`.

Use small coherent commits. Open a PR to `main` when finished. In the PR description include:

- what was repaired,
- exact commands run,
- build/test results,
- files intentionally left as production stubs,
- remaining blockers or recommended next step.

The task is complete only when the repository has a reproducible debug build or you have documented a specific external blocker that prevents one.