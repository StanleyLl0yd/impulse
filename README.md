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

Current source version: **0.3.0** (`versionCode 3`) · Min SDK: **26 (Android 8.0)** · Target SDK: **37**

## ⚡ The idea

1. Watch the particles move.
2. Tap once to place the only player impulse.
3. Let the wave expand and trigger nearby particles.
4. Triggered particles emit their own waves.
5. Reach the target before the reaction dies out — or retry immediately.

The prototype currently starts with **20 moving particles** and requires **12 activations** for success.

> One tap. One impulse. Maximum chain.

## ✨ Current prototype

- One-touch gameplay with a single impulse per attempt
- Moving particles with wall reflection
- Expanding player and reaction waves
- Chain depth tracking and triggered-particle counter
- Instant retry with a new deterministic seed
- Frame-rate-independent fixed-step simulation
- Layered neon waves, trails, activation flashes, pulse bursts, and depth-reactive visual feedback
- Sound and haptic feedback for taps, chain activations, success, and failure
- In-session controls for sound, haptics, and reduced effects
- Near-miss messaging and a stronger result/retry panel
- Cinematic launch from pure black with the IMPULSE artwork fading in over three seconds
- Russian and English interface resources
- Portrait-first Android experience
- No account, backend, analytics, ads, or network permission

The current build is deliberately focused on one question: **is the chain reaction itself satisfying enough to build the full game around it?**

## 🎮 Game feel

IMPULSE is designed around clarity and escalation rather than visual noise:

- **launch** — the app opens from pure black and reveals the IMPULSE artwork over three seconds;
- **idle** — cool cyan particles move quietly across the field;
- **tap** — one cyan impulse expands from the chosen point;
- **chain** — triggered particles switch to violet/magenta and emit new waves;
- **result** — success, failure, and near misses resolve quickly so another attempt is never far away.

The simulation is deterministic and seedable so the same level state can be reproduced across devices and tests, while rendering remains free to run at the display refresh rate.

## 📦 Availability

The latest public prototype, **IMPULSE 0.3.0**, is available through [GitHub Releases](https://github.com/StanleyLl0yd/impulse/releases/tag/v0.3.0) with signed APK and AAB artifacts plus SHA-256 checksums.

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
| Build | Gradle 9.7.1, AGP 9.3.2, Kotlin DSL |
| Android | minSdk 26, targetSdk 37, compileSdk 37 |

Gameplay state and simulation are kept separate from rendering so visual polish can evolve without turning the renderer into the game engine.

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
- No backend or cloud dependency
- No dangerous Android runtime permissions in the current scope

This boundary is intentional and remains the default unless a future feature has a concrete reason to change it.

## 🌍 Languages

- English — default
- Русский

The interface follows the device language through Android resources.

## 🗺 Roadmap

- **Current:** one-tap prototype, deterministic simulation, visual/audio/haptic game feel, result UX, and cinematic launch
- **Next:** data-driven level definitions, progression, scoring, and local persistence
- **Content:** roughly 60 handcrafted/generated levels for the first complete release
- **Later:** tutorial, accessibility pass, local statistics, endless mode, and daily challenge after the core level loop is proven

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