<div align="center">

# ◉ IMPULSE

### ONE TAP · ONE IMPULSE · MAXIMUM CHAIN

<img src="docs/assets/readme/impulse-chain.webp" alt="IMPULSE chain reaction" width="100%">

[![Android CI](https://img.shields.io/github/actions/workflow/status/StanleyLl0yd/impulse/ci.yml?branch=main&label=CI&labelColor=050814&color=00E5FF)](https://github.com/StanleyLl0yd/impulse/actions/workflows/ci.yml)
[![Latest release](https://img.shields.io/github/v/release/StanleyLl0yd/impulse?label=release&labelColor=050814&color=9E4DFF)](https://github.com/StanleyLl0yd/impulse/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/StanleyLl0yd/impulse/total?label=downloads&labelColor=050814&color=00E5FF)](https://github.com/StanleyLl0yd/impulse/releases)
[![Android 8+](https://img.shields.io/badge/Android-8.0%2B-00E5FF?labelColor=050814&logo=android&logoColor=8FF8FF)](https://github.com/StanleyLl0yd/impulse)
[![Offline](https://img.shields.io/badge/network-offline-9E4DFF?labelColor=050814)](https://github.com/StanleyLl0yd/impulse)
[![License](https://img.shields.io/badge/license-Proprietary%20%2F%20All%20Rights%20Reserved-9E4DFF?labelColor=050814)](LICENSE)

[![English](https://img.shields.io/badge/lang-EN-00E5FF?labelColor=050814)](README.md)
[![Русский](https://img.shields.io/badge/lang-RU-9E4DFF?labelColor=050814)](README_RU.md)

A minimalist one-touch chain-reaction puzzle for Android.

</div>

Particles drift across a near-black field. You get one tap. That tap creates an expanding impulse; every particle it reaches can become another wave, and every new wave can continue the reaction. The entire attempt is decided by that single moment.

Current source version: **0.4.0** (`versionCode 5`) · Min SDK: **26 (Android 8.0)** · Target SDK: **37**

## ⚡ The idea

1. Watch the particles move.
2. Tap once to place the only player impulse.
3. Let the wave expand and trigger nearby particles.
4. Triggered particles emit their own waves.
5. Reach the target before the reaction dies out — or retry immediately.
6. Complete levels, improve the score, and collect up to three stars per level.

The current campaign contains **20 deterministic levels** with increasing particle counts and completion targets.

> One tap. One impulse. Maximum chain.

## ✨ Current prototype

- One-touch gameplay with a single impulse per attempt
- 20 data-driven deterministic levels with increasing targets
- Level unlocking, next-level flow, and a compact campaign picker
- Per-level score, 1–3 star evaluation, best-score tracking, and total-star progress
- Local persistence for progress, selected level, best results, and Game Feel settings through AndroidX DataStore
- Moving particles with wall reflection
- Expanding player and reaction waves
- Chain depth tracking and triggered-particle counter
- Instant deterministic retry of the same level
- Frame-rate-independent fixed-step simulation
- Layered neon waves, trails, activation flashes, pulse bursts, and depth-reactive visual feedback
- Sound and haptic feedback for taps, chain activations, success, and failure
- Persistent controls for sound, haptics, and reduced effects
- Near-miss messaging and result/retry/next-level UX
- Cinematic launch from pure black with the IMPULSE artwork fading in over three seconds
- Russian and English interface resources
- Portrait-first Android experience
- No account, backend, analytics, ads, or network permission

The current build now provides a complete local campaign loop: **choose a level, make one impulse, earn a result, persist progress, and continue or improve the same deterministic challenge.**

## 🎮 Game feel

IMPULSE is designed around clarity and escalation rather than visual noise:

- **launch** — the app opens from pure black and reveals the IMPULSE artwork over three seconds;
- **campaign** — unlocked levels expose their best star result and the campaign tracks total stars;
- **idle** — cool cyan particles move quietly across the field;
- **tap** — one cyan impulse expands from the chosen point;
- **chain** — triggered particles switch to violet/magenta and emit new waves;
- **result** — score, stars, success, failure, and near misses resolve quickly so retry or the next level is one step away.

The simulation is deterministic and seedable so each level can be reproduced across devices, retries, and tests, while rendering remains free to run at the display refresh rate.

## 📦 Availability

The latest public build, **IMPULSE 0.4.0**, is available through [GitHub Releases](https://github.com/StanleyLl0yd/impulse/releases/tag/v0.4.0) with signed APK and AAB artifacts plus SHA-256 checksums.

The repository is publicly visible for project transparency and review. **Public visibility does not grant permission to copy, build, modify, execute, redistribute, or otherwise use the source code or assets.** See [LICENSE](LICENSE).

Android 8.0 or newer is required for the public build.

## 🛠️ Development

The following commands are for the copyright owner and explicitly authorized development only. They do not grant a license to anyone else.

Requirements:

- JDK 17
- Android SDK 37
- Gradle 9.7.1 through the repository Gradle Wrapper

Build the debug application from an authorized working copy:

```bash
./gradlew assembleDebug
```

Run the main local verification:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

## 🧱 Technology

| Category | Technology |
| --- | --- |
| Language | Kotlin 2.4.10 |
| UI | Jetpack Compose + Material 3 |
| Rendering | Compose Canvas |
| Simulation | Custom deterministic fixed-step 2D engine |
| Persistence | AndroidX DataStore Preferences 1.2.1 |
| Build | Gradle 9.7.1, AGP 9.3.2, Kotlin DSL |
| Android | minSdk 26, targetSdk 37, compileSdk 37 |
| Application ID / namespace | `com.sl.impulse` |

Gameplay state and simulation are kept separate from rendering, while campaign progress and preferences are stored locally outside the engine.

## ✅ Quality and security checks

Pull requests and pushes to `main` are automatically checked with:

- unit tests;
- Android Lint;
- debug and release APK/AAB assembly;
- Android instrumentation-test compilation;
- runtime instrumentation tests on API 37;
- weekly runtime coverage on the minimum API 26;
- CodeQL for Java/Kotlin;
- Semgrep security and secret rules;
- Gitleaks across full Git history;
- scheduled/manual Qodana analysis.

Third-party GitHub Actions are pinned to immutable commit SHAs, workflow permissions follow least privilege, and the protected `main` branch requires `Verify`, `Analyze Java and Kotlin`, `Semgrep`, and `Gitleaks` before squash merge.

Security issues should be reported according to [SECURITY.md](SECURITY.md).

## 🔐 Release integrity

Official releases are tag-driven with `vMAJOR.MINOR.PATCH` tags that must match `versionName`.

The Android Release workflow:

1. validates the tag, source version, `versionCode`, and successful required checks for the tagged `main` commit;
2. runs tests and Android Lint;
3. restores the owner-provided signing key only inside the protected `release` environment;
4. builds signed APK and AAB artifacts;
5. verifies APK Signature Schemes v2/v3, signer count, and the expected certificate SHA-256 for both APK and AAB;
6. generates SHA-256 checksums;
7. creates GitHub artifact attestations for APK and AAB;
8. publishes a GitHub Release only after verification succeeds.

The keystore and signing passwords are never stored in the repository. See [docs/RELEASE.md](docs/RELEASE.md).

## 🔒 Privacy

- **Offline by default** — the app does not request Android `INTERNET` permission
- **No account, analytics, tracking, or advertising**
- Progress and settings are stored locally on the device through DataStore
- No backend or cloud dependency
- No dangerous Android runtime permissions in the current scope

This boundary is intentional and remains the default unless a future feature has a concrete reason to change it.

## 🌍 Languages

- English — default
- Русский

The interface follows the device language through Android resources.

## 🗺 Roadmap

- **Current:** 20-level deterministic campaign, unlocking, scoring, stars, local persistence, polished visual/audio/haptic game feel, and cinematic launch
- **Next:** level balancing, tutorial/onboarding, accessibility pass, and richer local statistics
- **Content:** expand toward roughly 60 handcrafted/generated levels for the first complete release
- **Later:** endless mode and daily challenge after the core campaign loop is proven

The product direction is tracked in [PROJECT.md](PROJECT.md).

## 📊 Changelog

- [CHANGELOG.md](CHANGELOG.md)
- [GitHub Releases](https://github.com/StanleyLl0yd/impulse/releases)

## 🤝 Feedback and contributions

Bug reports and suggestions are welcome.

Source-code, asset, documentation, or other project contributions are accepted only when explicitly agreed in advance by the copyright owner. Submission of material does not grant any license to IMPULSE and does not alter ownership of the project. Project-specific rules are documented in [AGENTS.md](AGENTS.md).

## 📄 License

**IMPULSE is proprietary software. Copyright © 2026 Stanley Lloyd. All rights reserved.**

All original project code, assets, documentation, game design, and other original materials are owned by Stanley Lloyd. No permission is granted to copy, modify, build, execute, redistribute, publish, sublicense, sell, create derivative works from, or otherwise use any part of the project except where required by applicable law, GitHub's platform terms, or prior written permission from the copyright owner.

Third-party dependencies remain under their respective licenses. See [LICENSE](LICENSE) for the authoritative terms.

## 👨‍💻 Author

**Stanley Lloyd** · [@StanleyLl0yd](https://github.com/StanleyLl0yd)

---

<div align="center">

**ONE TAP · ONE IMPULSE · MAXIMUM CHAIN**

</div>