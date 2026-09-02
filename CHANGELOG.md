# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

## 0.8.0 - 2026-09-02

### Added

- Twenty new deterministic campaign levels, expanding the campaign from 40 to 60 levels.
- Six ten-level chapters: Impulse, Momentum, Boost, Control, Resonance, and Chaos.
- Chapter headers and progress inside the level picker and Achievements screen.
- A data-driven catalog of 21 local achievements grouped into Journey, Mastery, Chain, and Endurance.
- New achievement goals for chapter completion, full-chapter perfection, deep chains, large reactions, total triggered particles, attempts, and wins.
- Contextual introductions for the denser Resonance and Chaos chapters in English and Russian.
- Verified winning-opening regression coverage for every new level from 41 through 60.

### Changed

- Levels 41–50 build denser mixed-mechanic Resonance fields; levels 51–60 push Booster, Fuse, and Anchor combinations further in Chaos without adding another player input.
- Campaign migration now understands both previous final levels, 20 and 40, so players can update directly from older releases without losing their continuation point.
- Achievements now show an unlocked total and per-chapter completion/perfect counts while continuing to derive entirely from local progress and statistics.
- Android version is now 0.8.0 with `versionCode 10`.

## 0.7.0 - 2026-09-02

### Added

- Three new deterministic gameplay particle types: Booster, Fuse, and Anchor.
- Booster particles emit a larger, faster reaction wave when triggered.
- Fuse particles pause briefly before emitting their reaction wave, adding timing to chain propagation without adding another player input.
- Anchor particles remain stationary, creating reliable relay points inside otherwise moving fields.
- Twenty new deterministic campaign levels, expanding the campaign from 20 to 40 levels.
- Progressive in-game mechanic hints on levels 21, 26, 31, and 36 in English and Russian.
- Distinct visual language for special particles and their waves, including reduced-effects-compatible markers.
- Unit coverage for special particle behavior, the expanded catalog, and campaign-progress migration.

### Changed

- Levels 21–25 introduce Boosters, 26–30 introduce delayed Fuse behavior, 31–35 introduce Anchors, and 36–40 combine all three mechanics.
- Players who completed the former 20-level campaign automatically unlock level 21 after updating; existing scores, stars, statistics, and settings are preserved.
- Android version is now 0.7.0 with `versionCode 9`.

## 0.6.1 - 2026-09-02

### Changed

- Replaced the launcher icon with a new neon chain-reaction emblem that reflects the one-impulse gameplay more directly while remaining legible at launcher sizes.
- Added an Android 13+ monochrome adaptive-icon layer so IMPULSE supports themed launcher icons without changing the full-color identity on standard launchers.
- Android version is now 0.6.1 with `versionCode 8`.

## 0.6.0 - 2026-09-02

### Added

- Contextual onboarding across the first three campaign levels, introducing the one-tap rule, chain propagation, targets, and star goals without adding a separate tutorial mode.
- Persistent local gameplay statistics for attempts, successful attempts, total triggered particles, best chain size, and best chain depth.
- Expanded Achievements with completed levels, perfect levels, best score, success rate, gameplay statistics, and seven local milestone achievements.
- Enhanced result panel with full chain size, target, depth, score, and new-best feedback.
- Unit and Android runtime coverage for the new statistics and result UI.

### Changed

- Tutorial hints automatically disappear as each introductory level is completed, so existing players who already progressed past the first levels are not forced through onboarding again.
- Gameplay panels were separated from the game host to keep UI orchestration focused and maintainable.
- Android version is now 0.6.0 with `versionCode 7`.

## 0.5.0 - 2026-09-01

### Added

- A dedicated main menu after launch with Continue, New game, Achievements, About, and Exit actions.
- Continue starts the highest unlocked level, while New game starts from level 1 without deleting saved progress or achievements.
- A local achievements summary showing the highest unlocked level and total earned stars.
- Android runtime coverage for the new launch, menu, achievements, About, and navigation flows.

### Changed

- The branded launch splash now lasts five seconds before opening the main menu instead of entering gameplay directly after three seconds.
- Android back navigation now returns from gameplay and menu subscreens to the main menu.
- Reduced avoidable runtime allocation and repeated work in gameplay simulation and sound playback without changing gameplay behavior.
- Simplified Android and Gradle configuration by removing unused direct dependencies, redundant buildscript setup, obsolete compatibility defaults, and an obsolete Gradle wrapper bootstrap fallback.
- Qodana now runs in JVM Community mode against `app/src`, preserving scheduled/manual analysis while remaining compatible with AGP 9.3.2.
- Android version is now 0.5.0 with `versionCode 6`.

### Removed

- Unused localized chain-result resources, a duplicate round launcher resource, unused Compose test/tooling declarations, and the empty project-specific ProGuard rules file.

## 0.4.0 - 2026-09-01

### Added

- Twenty deterministic data-driven campaign levels with increasing particle counts and completion targets.
- Per-level scoring, one-to-three-star results, best-score tracking, total-star progress, and locked/unlocked level selection.
- Local AndroidX DataStore persistence for campaign progress, selected level, best results, sound, haptics, and reduced-effects settings.
- Next-level navigation after successful attempts and deterministic same-level retry.
- Unit coverage for the level catalog, scoring, star thresholds, and progression rules plus Android runtime coverage for the campaign picker.

### Changed

- The prototype now opens into a persistent 20-level campaign loop instead of a single rerolled challenge.
- Game Feel settings persist across app restarts.
- Result UX now includes score, stars on successful attempts, retry, and next-level actions.
- Android version is now 0.4.0 with `versionCode 5`.

## 0.3.1 - 2026-08-31

### Added

- Soft melodic game audio for the player impulse, chain growth, success, and failure.

### Changed

- Android Application ID / Bundle ID, namespace, source packages, and test packages are permanently `com.sl.impulse`.
- Launch splash now uses the exact supplied 941×1672 IMPULSE artwork while retaining the three-second fade from pure black.
- CI, release tagging, and signed-release validation now enforce the canonical application identity.

### Removed

- Android `ToneGenerator` system beeps from gameplay feedback.

## 0.3.0 - 2026-08-31

### Added

- Cinematic launch splash using the IMPULSE chain-reaction artwork, fading from pure black to full visibility over three seconds before gameplay starts.
- Black Android 12+ native splash handoff so the app opens without flashing the launcher icon before the cinematic artwork appears.
- Verified automatic semver tagging and release dispatch after successful main-branch Android CI, security, and CodeQL checks.

### Changed

- Launch instrumentation tests now advance through the splash before asserting gameplay UI.

## 0.2.0 - 2026-08-31

### Added

- Layered neon wave rendering, particle trails, activation flashes, pulse bursts, and depth-reactive visual feedback.
- Sound feedback for the initial impulse, chain growth, success, and failure without adding network or analytics dependencies.
- Haptic feedback for taps, chain activations, and final results using platform haptic feedback.
- In-session Game Feel controls for sound, haptics, and reduced effects.
- Near-miss result messaging and a stronger result/retry panel.
- A 200-particle chain-reaction stress scenario in unit tests.

### Changed

- Triggered particles now expose deterministic activation age for time-based visual effects without coupling rendering to gameplay decisions.
- Result UX now highlights success, near misses, required particles, and maximum chain depth.
- Visual effects can be reduced while preserving the core gameplay state and collision behavior.

## 0.1.0 - 2026-08-31

### Added

- Native Android Kotlin/Compose project bootstrap.
- Deterministic chain-reaction gameplay prototype.
- Canonical 9:16 logical gameplay field with device-independent geometry.
- Interpolated rendering on top of a fixed 60 Hz simulation.
- Unit tests for deterministic stepping, success/failure, spawn separation, viewport mapping, and field bounds.
- Android runtime tests covering launch and the tap-result-retry flow.
- CI, security scanning, Dependabot, and signed GitHub release automation.
- English and Russian interface resources.
- Game-style README artwork and proprietary All Rights Reserved licensing.

### Changed

- Release signing now documents the owner-supplied key model used by the repository.
- Release validation now requires successful main-branch Android CI, Security and Quality, and CodeQL runs for the tagged commit and a monotonically increasing `versionCode`.
- Gradle wrapper and pinned GitHub Actions dependencies were updated through Dependabot.
- Semgrep CI now pins the container image by immutable digest.
- Gameplay spawning now uses a deterministic checked fallback instead of overlapping at the field center.
- Collision source selection now resolves same-tick contacts by earliest impact time with deterministic chain-depth tie-breaking.
