# Development

## Android baseline

Use JDK 17 and Android SDK 34. The repository contains a canonical Gradle 8.5 wrapper restored during bootstrap from the official Gradle `v8.5.0` sources.

Verify the toolchain:

```bash
./gradlew --version
```

Build the debug APK:

```bash
./gradlew :app:assembleDebug
```

Run available checks/tests before opening a PR. Keep dependency upgrades separate from baseline-repair changes unless an upgrade is strictly required to restore a build.

## Simulator

The web simulator is optional and exists to demonstrate the TV UX quickly.

```bash
npm ci
npx playwright install chromium
npm run capture
```

`npm run build-video` additionally requires `ffmpeg` in `PATH`. Generated media is ignored by Git.

## Google Photos integration

Do not guess partner-only API behavior. Until approved credentials/access and the relevant current documentation are available, keep `MockAmbientPhotosRepository` as the runtime implementation and preserve `GoogleAmbientPhotosRepository` as an explicit production skeleton.

Never commit OAuth credentials, access/refresh tokens, signing material, personal media, or private partner documentation.

## Git / PR discipline

Use focused branches and small coherent commits. Update `STATE.md` whenever a task materially changes the verified baseline or moves a roadmap phase forward.