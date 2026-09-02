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

Current source version: **0.9.0** (`versionCode 11`) · Min SDK: **26 (Android 8.0)** · Target SDK: **37**

## ⚡ The idea

1. Watch the field.
2. Tap once to place your only impulse.
3. Let the wave trigger nearby particles.
4. Use Standard particles, Boosters, delayed Fuses, and fixed Anchors to extend the reaction.
5. Reach the target before the chain dies out.
6. Improve deterministic challenges across Campaign, Endless, and Daily Impulse.

> One tap. One impulse. Maximum chain.

## ✨ Current game

- One-touch gameplay with a single impulse per attempt
- **60 deterministic campaign levels** in six ten-level chapters
- Chapters: **Impulse, Momentum, Boost, Control, Resonance, Chaos**
- Standard, Booster, Fuse, and Anchor particles with distinct behavior and visual language
- Contextual onboarding and chapter introductions
- Level unlocking, chapter-aware picker, score, 1–3 stars, and local best-result tracking
- **Endless mode** with seeded 40-field cycles, no field repeats inside a cycle, escalating targets, and persistent best round/run score
- **Daily Impulse** derived entirely from the device-local calendar date, with one stable challenge per day and unlimited retries
- **Share Result** through the native Android Sharesheet with no network permission
- **25 local achievements** across Journey, Mastery, Chain, Endurance, and Replay
- Local statistics for campaign and replay attempts, wins, triggered particles, best chain and depth
- Local persistence through AndroidX DataStore
- Deterministic fixed-step 60 Hz simulation and instant retries
- Layered neon waves, trails, activation flashes, sound, haptics, and reduced-effects setting
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

Levels use stable seeds, so retries reproduce the same logical challenge. Expanded campaign content is protected by deterministic winning-opening regression tests.

## ♾ Replayability

**Endless** creates a run from the 40 advanced campaign fields. A run seed chooses a permutation that visits all 40 fields before any repeat. After each complete cycle the target tightens, and one failed round ends the run. Best round and best run score are stored locally.

**Daily Impulse** maps the local calendar date to one advanced deterministic field. The challenge stays identical for that date, can be retried without limit, and stores the day's best score and stars plus the number of completed Daily challenges.

Replay result cards can be sent to any compatible app through Android's native Sharesheet. Sharing is user-initiated and does not require IMPULSE itself to have network access.

## 🎮 Game feel

IMPULSE is designed around clarity and escalation rather than visual noise. Cool cyan idle particles, a single cyan player impulse, pink Boosters, gold Fuses, blue Anchors and neon reaction feedback make propagation readable without changing the deterministic simulation. Results surface chain size, target, depth, score and relevant mode progress immediately.

Rendering can run at the display refresh rate while gameplay remains deterministic through a fixed 60 Hz simulation.

## 📦 Availability

Official signed APK and AAB builds are published through [GitHub Releases](https://github.com/StanleyLl0yd/impulse/releases) with SHA-256 checksums and GitHub artifact attestations.

The repository is publicly visible for project transparency and review. **Public visibility does not grant permission to copy, build, modify, execute, redistribute, or otherwise use the source code or assets.** See [LICENSE](LICENSE).

Android 8.0 or newer is required.

## 🛠️ Development

Requirements: JDK 17, Android SDK 37, and Gradle 9.7.1 through the repository Gradle Wrapper.

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

Gameplay state and simulation remain separate from rendering. Campaign and replay progress, statistics, achievements, and preferences remain local to the device.

## ✅ Quality and security checks

Pull requests and pushes to `main` are checked with unit tests, Android Lint, debug/release assembly, API 37 runtime instrumentation tests, CodeQL, Semgrep and Gitleaks. Scheduled/manual Qodana and weekly minimum-API runtime coverage complement the required checks.

The protected `main` branch requires configured verification checks before squash merge. Third-party GitHub Actions are pinned to immutable commit SHAs and workflow permissions follow least privilege.

Security issues should be reported according to [SECURITY.md](SECURITY.md).

## 🔐 Release integrity

Official releases are tag-driven with `vMAJOR.MINOR.PATCH` tags matching `versionName`. The release workflow validates source version and required checks, uses the owner-provided signing identity, verifies APK/AAB signatures and certificate identity, generates SHA-256 checksums and artifact attestations, then publishes the GitHub Release. Signing secrets and keystore material are never stored in the repository.

See [docs/RELEASE.md](docs/RELEASE.md).

## 🔒 Privacy

- Offline by design; no Android `INTERNET` permission
- No account, analytics, tracking, advertising, or backend
- Campaign/replay progress, statistics, achievements, and settings stay on the device
- Daily Impulse uses only the device-local calendar date
- Sharing is explicitly initiated through the Android Sharesheet
- No dangerous Android runtime permissions in the current scope

## 🌍 Languages

- English — default
- Русский

## 🗺 Roadmap

- **0.9.0 · Replayability:** Endless, Daily Impulse, Share Result
- **1.0.0:** final campaign/replay balance, audiovisual polish, dedicated accessibility pass, regression hardening, production release

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
