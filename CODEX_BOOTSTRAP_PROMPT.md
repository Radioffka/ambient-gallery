# Codex baseline verification task

You are taking over the `ambient-gallery` repository, an Android TV / Google TV application reconstructed from a previous Google Photos Ambient API UX prototype.

Read `AGENTS.md`, `README.md`, `STATE.md`, `ROADMAP.md`, and `docs/ARCHITECTURE.md` before changing anything.

## Objective

Turn the imported source into a **verified, reproducible development baseline** and prepare it for continued product work. Do not start major product features and do not implement speculative live Google Photos authentication/API behavior.

## Required work

1. Inspect the complete repository and confirm the reconstructed project structure.
2. Verify the checked-in canonical **Gradle 8.5 wrapper** with `./gradlew --version`. Do not replace it unless verification fails for a concrete reason.
3. Run `./gradlew :app:assembleDebug` with JDK 17 and Android SDK 34.
4. Fix any **build-blocking issues** in the recovered baseline. Keep fixes narrow. Do not use this task for broad dependency upgrades or UI rewrites.
5. Run static checks/tests that already exist. If essentially no tests exist, add a small number of high-value JVM unit tests for deterministic, non-Android logic only where this can be done without a large refactor.
6. Inspect `MainActivity` and confirm that demo mode is currently explicit/hardcoded through `MockAmbientPhotosRepository`. Do not switch to the production repository yet.
7. Inspect `GoogleAmbientPhotosRepository`. Preserve its explicit incomplete state. Do not invent credentials, endpoints, fields, token handling, or partner behavior. If comments disagree with current official Google Ambient API documentation, correct comments/documentation only after checking official sources.
8. Confirm that no secrets, credentials, generated screenshots/videos, `node_modules`, local SDK paths, or machine-specific user paths are tracked.
9. Verify the GitHub Actions Android workflow uses `./gradlew :app:assembleDebug` and wrapper validation. Fix the workflow only if verification identifies a concrete problem.
10. Review the browser UX simulator only for obvious broken relative paths or bootstrap regressions. Do not redesign it.
11. Update `STATE.md` with exactly what you verified, including build/test commands and any remaining blockers.

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

- exact commands run,
- build/test results,
- build-blocking fixes made,
- files intentionally left as production stubs,
- remaining blockers,
- recommended next product-development step.

The task is complete only when the repository has a reproducible debug build or you have documented a specific external blocker that prevents one.
