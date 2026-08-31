# IMPULSE

Minimalist chain-reaction puzzle game for Android. One tap. One impulse. Maximum chain.

[Русский README](README_RU.md)

## Status

Early playable prototype (`0.1.0`). The repository is intentionally small and offline-first.

## Core loop

1. Watch the particles move.
2. Tap once to create an expanding impulse.
3. Trigger particles and let the chain reaction propagate.
4. Reach the target or retry immediately.

## Technical baseline

- Kotlin
- Jetpack Compose
- Custom deterministic 2D simulation
- `minSdk 26`, `targetSdk 37`, `compileSdk 37`
- Gradle 9.5.0, AGP 9.3.2, Kotlin 2.4.10
- No account, backend, analytics, ads, or network permission

## Build

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

## Release model

Official releases are tag-driven (`vMAJOR.MINOR.PATCH`). Release signing uses a dedicated IMPULSE key stored only as GitHub Environment secrets. The workflow verifies APK/AAB signatures, checks certificate SHA-256, generates checksums, creates attestations, and publishes a GitHub Release.

See [docs/RELEASE.md](docs/RELEASE.md).

## Security

See [SECURITY.md](SECURITY.md). Repository security automation includes CodeQL, Semgrep, Gitleaks, Qodana, Dependabot, pinned GitHub Actions and least-privilege workflow permissions.

## License

PolyForm Noncommercial License 1.0.0. See [LICENSE](LICENSE).
