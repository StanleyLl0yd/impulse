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

Current source version: **0.8.0** (`versionCode 10`) · Min SDK: **26 (Android 8.0)** · Target SDK: **37**

## ⚡ The idea

1. Watch the field.
2. Tap once to place your only impulse.
3. Let the wave trigger nearby particles.
4. Use standard particles, Boosters, delayed Fuses, and fixed Anchors to extend the reaction.
5. Reach the target before the chain dies out.
6. Improve the same deterministic board for a better score and up to three stars.

> One tap. One impulse. Maximum chain.

## ✨ Current game

- One-touch gameplay with a single impulse per attempt
- **60 deterministic campaign levels** grouped into six ten-level chapters
- Chapters: **Impulse, Momentum, Boost, Control, Resonance, Chaos**
- Standard, Booster, Fuse, and Anchor particles with distinct behavior and visual language
- Contextual onboarding for the core rule and newly introduced gameplay patterns
- Level unlocking, next-level flow, chapter-aware picker, score, 1–3 stars, and best-result tracking
- **21 local achievements** grouped into Journey, Mastery, Chain, and Endurance
- Achievement and statistics screen with chapter completion/perfection progress
- Local statistics for attempts, wins, success rate, triggered particles, best chain, depth, and score
- Local persistence through AndroidX DataStore
- Deterministic same-level retry with fixed-step 60 Hz simulation
- Layered neon waves, trails, activation flashes, sound, and haptic feedback
- Persistent controls for sound, haptics, and reduced effects
- English and Russian interface resources
- Portrait-first Android experience
- No account, backend, analytics, ads, cloud dependency, or `INTERNET` permission

## 🧩 Campaign

| Chapter | Levels | Focus |
| --- | ---: | --- |
| I · Impulse | 1–10 | Core one-tap chain reaction |
| II · Momentum | 11–20 | Denser standard-particle fields |
| III · Boost | 21–30 | Booster reach and Fuse timing |
| IV · Control | 31–40 | Anchors and mixed mechanics |
| V · Resonance | 41–50 | Dense coordinated combinations |
| VI · Chaos | 51–60 | Full-system mastery |

Levels use stable seeds, so retries reproduce the same logical challenge. The campaign is protected by deterministic regression tests, including verified winning openings for levels 21–60.

## 🎮 Game feel

IMPULSE is designed around clarity and escalation rather than visual noise:

- **launch** — a five-second branded reveal opens into the main menu;
- **idle** — cool cyan particles move quietly across the field;
- **tap** — one cyan impulse expands from the chosen point;
- **Boosters** — pink particles create larger reaction waves;
- **Fuses** — gold particles delay their wave before expansion;
- **Anchors** — blue fixed particles act as reliable relay points;
- **chain** — activation colors and wave feedback make propagation readable;
- **result** — chain size, target, depth, score, stars, new-best feedback, retry, and next level resolve quickly.

Rendering can run at the display refresh rate while gameplay remains deterministic through a fixed 60 Hz simulation.

## 📦 Availability

Official signed APK and AAB builds are published through [GitHub Releases](https://github.com/StanleyLl0yd/impulse/releases) with SHA-256 checksums and GitHub artifact attestations.

The repository is publicly visible for project transparency and review. **Public visibility does not grant permission to copy, build, modify, execute, redistribute, or otherwise use the source code or assets.** See [LICENSE](LICENSE).

Android 8.0 or newer is required.

## 🛠️ Development

The following commands are for the copyright owner and explicitly authorized development only.

Requirements:

- JDK 17
- Android SDK 37
- Gradle 9.7.1 through the repository Gradle Wrapper

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

Gameplay state and simulation remain separate from rendering. Campaign progress, statistics, achievements, and preferences remain local to the device.

## ✅ Quality and security checks

Pull requests and pushes to `main` are automatically checked with unit tests, Android Lint, debug/release assembly, API 37 runtime instrumentation tests, CodeQL, Semgrep, Gitleaks, and scheduled/manual Qodana analysis. Weekly runtime coverage also targets the minimum API 26.

The protected `main` branch requires the configured verification checks before squash merge. Third-party GitHub Actions are pinned to immutable commit SHAs and workflow permissions follow least privilege.

Security issues should be reported according to [SECURITY.md](SECURITY.md).

## 🔐 Release integrity

Official releases are tag-driven with `vMAJOR.MINOR.PATCH` tags matching `versionName`. The release workflow validates the source version and required checks, builds with the owner-provided signing identity, verifies APK/AAB signatures and certificate identity, generates SHA-256 checksums and artifact attestations, then publishes the GitHub Release. Signing secrets and keystore material are never stored in the repository.

See [docs/RELEASE.md](docs/RELEASE.md).

## 🔒 Privacy

- Offline by design; no Android `INTERNET` permission
- No account, analytics, tracking, or advertising
- Progress, statistics, achievements, and settings stay on the device
- No backend or cloud dependency
- No dangerous Android runtime permissions in the current scope

## 🌍 Languages

- English — default
- Русский

## 🗺 Roadmap

- **0.8.0 · Content:** 60-level campaign, six chapters, full local achievements
- **0.9.0:** Endless, Daily Impulse, Share Result
- **1.0.0:** final campaign balance, audiovisual polish, dedicated accessibility pass, regression hardening, production release

The product direction is tracked in [PROJECT.md](PROJECT.md).

## 📊 Changelog

- [CHANGELOG.md](CHANGELOG.md)
- [GitHub Releases](https://github.com/StanleyLl0yd/impulse/releases)

## 🤝 Feedback and contributions

Bug reports and suggestions are welcome. Source-code, asset, documentation, or other contributions are accepted only when explicitly agreed in advance by the copyright owner. Project-specific rules are documented in [AGENTS.md](AGENTS.md).

## 📄 License

**IMPULSE is proprietary software. Copyright © 2026 Stanley Lloyd. All rights reserved.**

All original project code, assets, documentation, game design, and other original materials are owned by Stanley Lloyd. No permission is granted to copy, modify, build, execute, redistribute, publish, sublicense, sell, create derivative works from, or otherwise use any part of the project except where required by applicable law, GitHub's platform terms, or prior written permission from the copyright owner.

Third-party dependencies remain under their respective licenses. See [LICENSE](LICENSE).

## 👨‍💻 Author

**Stanley Lloyd** · [@StanleyLl0yd](https://github.com/StanleyLl0yd)

---

<div align="center">

**ONE TAP · ONE IMPULSE · MAXIMUM CHAIN**

</div>
