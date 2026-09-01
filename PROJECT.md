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

- Native Android.
- Kotlin and Jetpack Compose.
- Custom lightweight 2D rendering/simulation; no game engine unless profiling or requirements justify it.
- `minSdk 26`, `targetSdk 37`, `compileSdk 37`.
- Permanent Android Application ID / Bundle ID and namespace: `com.sl.impulse`.
- Deterministic fixed-step simulation at 60 Hz with seedable randomness.
- Interpolated rendering may run at the display refresh rate without changing simulation outcomes.
- A canonical 9:16 logical playfield keeps gameplay geometry independent from device aspect ratio.

## Gameplay entities

A particle has position, velocity, radius, trigger state and chain depth. A wave has origin, radius, maximum radius, growth rate and chain depth. A particle can trigger only once per attempt.

Level completion is based on `triggeredParticles >= requiredTriggeredCount` after all waves expire.

If multiple wave fronts reach a particle during the same fixed simulation step, the collision is attributed to the earliest impact within that step. Equal-time contacts prefer the deeper chain and otherwise preserve deterministic wave order.

## Campaign and progression

The 0.5.0 campaign contains 20 ordered data-driven levels. Each level defines a stable seed, particle count, and required activation count. Retrying a level recreates the same deterministic simulation so improvement comes from timing and placement rather than rerolling the board.

A successful level unlocks the next level. Each attempt receives a numeric score based on activated particles, chain depth, completion, and activations beyond the minimum target. Successful attempts receive one to three stars: one for completion, two for reaching the level's bonus threshold, and three for activating every particle.

Best per-level score, best star result, highest unlocked level, selected level, and Game Feel settings are stored locally through AndroidX DataStore Preferences. Progress is intentionally device-local; there is no account, cloud sync, or backend dependency.

After the five-second branded launch splash, the app opens a dedicated main menu. Continue starts the highest unlocked level. New game starts level 1 without clearing campaign progress, scores, stars, or settings. Achievements shows the highest unlocked level and total earned stars. About and Exit complete the local menu flow, and Android back navigation returns from gameplay or menu subscreens to the main menu.

## MVP

The first useful MVP contains 20 deterministic levels, standard particles, one impulse per attempt, chain reactions, a main menu with continue/new-game navigation, a local achievements summary, local progression saving, scoring/stars, sound, haptics, basic settings, and Russian and English localization. Ads, purchases, leaderboards, accounts, cloud sync and complex content are explicitly outside the MVP.

## Version 1.0 direction

Target roughly 60 levels, polished visuals/audio, gradually introduced special particle behavior, local statistics, tutorial, settings, accessibility pass, Russian and English localization, and full offline operation.

## Architecture

Gameplay logic belongs under `game/` and must not depend on Compose drawing decisions. Rendering reads game snapshots. Level definitions live in a deterministic catalog and configure the engine without moving campaign state into simulation code. Persistence belongs outside the engine and uses DataStore for progression and settings.

The simulation uses canonical logical coordinates. UI code maps that field to the available canvas with a uniform scale and letterboxing when necessary; taps outside the logical playfield do not affect gameplay.

## Quality requirements

- Fixed 60 Hz simulation with smooth interpolated rendering on 60/90/120/144 Hz displays.
- Identical logical gameplay geometry and seeded outcomes across device aspect ratios.
- Deterministic retry for every campaign level.
- Avoid per-frame allocation pressure where practical.
- Unit-test deterministic gameplay rules, level catalog/scoring/progression rules, field geometry and viewport mapping.
- Run Android lint and release builds in CI.
- Runtime-test launch, main-menu navigation, level selection, achievements/About, settings, and tap-result-retry flows.
- Stress-test larger chain reactions before content expansion.
- No unnecessary permissions, SDKs, dependencies, exported components or cleartext network traffic.

## Repository policy

`main` is protected, PR-based and squash-only. CI must pass before merge. GitHub Actions are pinned to immutable commit SHAs. Dependency, static-analysis and secret-scanning automation is part of the baseline.

## Release policy

Official releases are tag-driven and must match `versionName`. The release signing key is supplied by the repository owner; the repository does not generate or contain it. CI restores the key only into runner temporary storage, signs APK/AAB, verifies both artifacts and the expected certificate fingerprint, generates SHA-256 checksums and provenance attestations, publishes the GitHub Release, then removes temporary signing material.
