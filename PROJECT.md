# IMPULSE Project Specification

## Vision

IMPULSE is a minimalist one-touch chain-reaction puzzle game. The player watches moving particles, chooses one position and moment, and taps once. The initial wave activates particles; each activated particle creates another wave; the reaction continues until no active waves remain.

The central rule is: **one tap, one impulse, one chain reaction**.

## Product principles

- Understandable within seconds.
- One-handed portrait play.
- Attempts normally last 5–30 seconds.
- Retry must be effectively instant.
- Visual and audio polish are more important than feature count.
- Offline-first, no mandatory account or backend.
- No artificial energy system or forced wait between attempts.
- Keep the codebase small enough for one developer to understand completely.

## Platform baseline

- Native Android with Kotlin and Jetpack Compose.
- Custom lightweight 2D rendering/simulation; no external game engine unless profiling or requirements justify it.
- `minSdk 26`, `targetSdk 37`, `compileSdk 37`.
- Permanent Android Application ID / namespace: `com.sl.impulse`.
- Deterministic fixed-step simulation at 60 Hz with seedable randomness.
- Interpolated rendering may run at the display refresh rate without changing simulation outcomes.
- A canonical 9:16 logical playfield keeps gameplay geometry independent from device aspect ratio.

## Gameplay entities

A particle has position, velocity, radius, type, trigger state and chain depth. A wave has origin, radius, maximum radius, growth rate, chain depth and optional activation delay. A particle can trigger only once per attempt.

Particle types:

- **Standard** — moving particle with the normal reaction wave.
- **Booster** — moving particle with a larger, faster reaction wave.
- **Fuse** — moving particle whose reaction wave starts after a short delay.
- **Anchor** — stationary particle that acts as a reliable relay point.

Level completion is based on `triggeredParticles >= requiredTriggeredCount` after all waves expire. If multiple wave fronts reach a particle during the same fixed simulation step, collision ownership uses earliest impact time; equal-time contacts prefer the deeper chain and otherwise preserve deterministic wave order.

## Campaign and progression

The 0.8.0 campaign contains **60 ordered deterministic levels** arranged into six ten-level chapters:

1. **Impulse** — levels 1–10, core chain-reaction play.
2. **Momentum** — levels 11–20, denser standard-particle fields.
3. **Boost** — levels 21–30, Booster reach and Fuse timing.
4. **Control** — levels 31–40, Anchors and mixed mechanics.
5. **Resonance** — levels 41–50, denser coordinated mixed fields.
6. **Chaos** — levels 51–60, full-system combinations.

Each level defines a stable seed, particle count, required activation count and optional particle mix. Retrying recreates the same deterministic simulation so improvement comes from timing and placement rather than rerolling the board.

A successful level unlocks the next level. Each attempt receives a numeric score based on activated particles, chain depth, completion, and activations beyond the minimum target. Successful attempts receive one to three stars: one for completion, two for reaching the bonus threshold, and three for activating every particle.

Best per-level score, stars, highest unlocked level, selected level, gameplay statistics and Game Feel settings are stored locally through AndroidX DataStore Preferences. Campaign expansion migration recognizes the former final levels 20 and 40 so direct upgrades from older releases retain the correct continuation point.

## Achievements and statistics

Achievements are derived from local progress and statistics rather than persisted separately. The 0.8.0 catalog contains 21 goals grouped into:

- **Journey** — level and chapter completion.
- **Mastery** — perfect levels, flawless chapters and full perfection.
- **Chain** — chain depth and reaction-size milestones.
- **Endurance** — total triggered particles, attempts and successful attempts.

The Achievements screen also reports campaign totals, chapter completion/perfection, attempts, wins, success rate, total triggered particles, best chain, best depth and best score.

No achievement or statistic requires an account, backend or network connection.

## Navigation and local UX

After the five-second branded launch splash, the app opens the main menu. Continue starts the highest unlocked level. New game starts level 1 without clearing campaign progress, scores, stars, statistics or settings. Achievements opens the local progress/achievement view. About and Exit complete the menu flow, and Android back navigation returns from gameplay or menu subscreens to the main menu.

The Level Picker groups levels by chapter and shows per-level stars while preserving locked/unlocked progression.

## Version direction

### 0.8.0 · Content

- 60 deterministic levels.
- Six chapters.
- Full local achievement catalog.
- Chapter-aware campaign UI.
- Existing progress preserved across prior campaign boundaries.

### 0.9.0

- Endless mode using deterministic procedural generation.
- Daily Impulse using a local date-derived seed.
- Share Result through the Android Sharesheet without adding `INTERNET` permission.

### 1.0.0

- Final balance pass across all 60 levels.
- Audiovisual polish and game-feel tuning.
- Dedicated accessibility pass.
- Regression hardening and production release validation.

## Architecture

Gameplay logic belongs under `game/` and must not depend on Compose drawing decisions. Rendering reads game snapshots. Level and chapter definitions live in deterministic catalogs and configure the engine without moving campaign state into simulation code. Achievement conditions are data-driven and derived from progress/statistics. Persistence belongs outside the engine and uses DataStore.

The simulation uses canonical logical coordinates. UI maps that field to the available canvas with uniform scale and letterboxing when necessary; taps outside the logical playfield do not affect gameplay.

## Quality requirements

- Fixed 60 Hz simulation with smooth interpolated rendering on 60/90/120/144 Hz displays.
- Identical logical gameplay geometry and seeded outcomes across device aspect ratios.
- Deterministic retry for every campaign level.
- Verified winning-opening regression coverage for expanded campaign content.
- Avoid per-frame allocation pressure where practical.
- Unit-test gameplay rules, content catalogs, achievements, scoring/progression, field geometry and viewport mapping.
- Run Android lint and debug/release builds in CI.
- Runtime-test launch, navigation, chapter-aware level selection, achievements/About, settings, and tap-result-retry flows.
- No unnecessary permissions, SDKs, dependencies, exported components or cleartext network traffic.

## Repository policy

`main` is protected, PR-based and squash-only. CI must pass before merge. GitHub Actions are pinned to immutable commit SHAs. Dependency, static-analysis and secret-scanning automation is part of the baseline.

## Release policy

Official releases are tag-driven and must match `versionName`. The release signing key is supplied by the repository owner; the repository does not generate or contain it. CI restores the key only into runner temporary storage, signs APK/AAB, verifies both artifacts and the expected certificate fingerprint, generates SHA-256 checksums and provenance attestations, publishes the GitHub Release, then removes temporary signing material.
