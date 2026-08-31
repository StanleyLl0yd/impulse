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
- Deterministic fixed-step simulation at 60 Hz with seedable randomness.
- Interpolated rendering may run at the display refresh rate without changing simulation outcomes.
- A canonical 9:16 logical playfield keeps gameplay geometry independent from device aspect ratio.

## Gameplay entities

A particle has position, velocity, radius, trigger state and chain depth. A wave has origin, radius, maximum radius, growth rate and chain depth. A particle can trigger only once per attempt.

Level completion is based on `triggeredParticles >= requiredTriggeredCount` after all waves expire.

If multiple wave fronts reach a particle during the same fixed simulation step, the collision is attributed to the earliest impact within that step. Equal-time contacts prefer the deeper chain and otherwise preserve deterministic wave order.

## MVP

The first useful MVP should contain 20 levels, standard particles, one impulse per attempt, chain reactions, progress saving, sound, haptics, basic settings, Russian and English localization. Ads, purchases, leaderboards, accounts, cloud sync and complex content are explicitly outside the MVP.

## Version 1.0 direction

Target roughly 60 levels, polished visuals/audio, gradually introduced special particle behavior, local statistics, tutorial, settings, accessibility pass, Russian and English localization, and full offline operation.

## Architecture

Gameplay logic belongs under `game/` and must not depend on Compose drawing decisions. Rendering reads game snapshots. Levels should become data-driven and deterministic. Persistence should remain local; add DataStore when progression/settings are implemented.

The simulation uses canonical logical coordinates. UI code maps that field to the available canvas with a uniform scale and letterboxing when necessary; taps outside the logical playfield do not affect gameplay.

## Quality requirements

- Fixed 60 Hz simulation with smooth interpolated rendering on 60/90/120/144 Hz displays.
- Identical logical gameplay geometry and seeded outcomes across device aspect ratios.
- Avoid per-frame allocation pressure where practical.
- Unit-test deterministic gameplay rules, field geometry and viewport mapping.
- Run Android lint and release builds in CI.
- Stress-test larger chain reactions before content expansion.
- No unnecessary permissions, SDKs, dependencies, exported components or cleartext network traffic.

## Repository policy

`main` is protected, PR-based and squash-only. CI must pass before merge. GitHub Actions are pinned to immutable commit SHAs. Dependency, static-analysis and secret-scanning automation is part of the baseline.

## Release policy

Official releases are tag-driven and must match `versionName`. The release signing key is supplied by the repository owner; the repository does not generate or contain it. CI restores the key only into runner temporary storage, signs APK/AAB, verifies both artifacts and the expected certificate fingerprint, generates SHA-256 checksums and provenance attestations, publishes the GitHub Release, then removes temporary signing material.
