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
- `minSdk 26`, `targetSdk 37`, `compileSdk 37`.
- Permanent Android Application ID / namespace: `com.sl.impulse`.
- Deterministic fixed-step simulation at 60 Hz with seedable randomness.
- Canonical 9:16 logical playfield and interpolated rendering.

## Gameplay

A particle has position, velocity, radius, type, trigger state and chain depth. A wave has origin, radius, growth characteristics, chain depth and optional delay. A particle can trigger only once per attempt.

Particle types are Standard, Booster, Fuse and Anchor. Level/replay completion remains `triggeredParticles >= requiredTriggeredCount` after all waves expire. The player still receives exactly one impulse per attempt in every mode.

## Campaign

IMPULSE 1.0.0 contains the complete **60-level deterministic campaign** across six chapters: Impulse, Momentum, Boost, Control, Resonance and Chaos. Stable seeds, scores, stars, progression, migration from former 20/40-level campaigns and DataStore keys remain compatible with previous releases.

## Replayability

### Endless

Endless reuses the 40 advanced deterministic fields corresponding to campaign levels 21–60 without changing `GameEngine`. A run seed determines an offset and a coprime traversal step, so all 40 fields appear exactly once before a field repeats. Each subsequent 40-round cycle tightens the required activation target, capped below the total particle count. One failed round ends the run. Best completed round and best run score persist locally.

### Daily Impulse

Daily Impulse derives an epoch day from the device-local `LocalDate` and deterministically maps that date to one of the 40 advanced fields. The same date therefore produces the same challenge without a server, network clock or backend. Retries are unlimited. Daily best score, best stars and completed-day count persist locally.

### Share Result

Replay result cards expose a user-initiated `ACTION_SEND` text share through the native Android Sharesheet. IMPULSE itself does not gain network access, file access or a new runtime permission; the receiving app controls delivery after the user chooses it.

## Achievements and statistics

Achievements are derived from local state rather than stored separately. IMPULSE 1.0.0 contains **25 achievements** in Journey, Mastery, Chain, Endurance and Replay groups.

Replay attempts also feed the existing aggregate attempts, successes, triggered-particle and chain-depth statistics. Replay-specific local statistics include Endless best round/score and number of completed Daily challenges.

## Audiovisual and accessibility

- Procedural ambient music is synthesized locally at runtime and subtly reacts to chain depth.
- Music can be disabled independently from gameplay sound effects.
- Reduced Effects lowers visual intensity without altering simulation or hiding core gameplay information.
- High Contrast is persistent and strengthens field framing, reaction waves, particle outlines and special-particle geometry.
- Booster, Fuse and Anchor remain distinguishable through geometry as well as color.
- English and Russian settings expose the same accessibility controls.

## Navigation

After the branded splash, the main menu exposes Continue, New game, Endless, Daily Impulse, Achievements, About and Exit. Android Back from campaign or replay returns to the menu. Campaign selection remains chapter-aware and isolated from Replay mode state.

The About screen keeps ordinary users on the public IMPULSE website rather than GitHub. Its app website entry opens `https://stanleyll0yd.github.io/apps/impulse/`, and its Privacy policy entry opens `https://stanleyll0yd.github.io/apps/impulse/privacy/`.

## Architecture

- `game/` owns deterministic simulation, campaign definitions and replay challenge generation.
- `progress/` owns DataStore state, statistics and derived achievements.
- `ui/` owns Compose screens, rendering, visual accessibility and Android Sharesheet integration.
- `feedback/` owns game audio and adaptive ambient synthesis.
- Replay challenge generation configures the existing `GameEngine`; it does not fork or duplicate physics.
- Campaign and replay persistence are additive and device-local.

## Quality requirements

- Fixed 60 Hz deterministic simulation across modes.
- Stable Daily challenge for a given local date.
- Endless traversal must cover all 40 replay fields before repeating.
- Endless challenge definitions must remain structurally valid while targets escalate.
- Deterministic winning-opening regression coverage for expanded campaign content.
- Unit-test replay generation, scoring, progression, migrations, special particles and achievements.
- Runtime-test menu/navigation, settings and gameplay flows on API 37.
- Run Android lint, debug/release builds, CodeQL, Semgrep and Gitleaks in CI.
- No unnecessary permissions, SDKs, exported components or cleartext network traffic.

## Version direction

### 1.0.0 · Production baseline

- 60-level Campaign and deterministic replay modes.
- Endless, Daily Impulse and Share Result.
- 25 local achievements and statistics.
- Adaptive procedural ambient music.
- Reduced Effects and persistent High Contrast accessibility modes.
- Full regression/security/release validation.

### 1.0.1 · About polish

- Public app website and privacy destinations replace GitHub-facing About navigation for ordinary users.
- The app website entry uses a two-line label/domain presentation in English and Russian.
- Gameplay, progression, replay modes, achievements, statistics, audio, accessibility and offline architecture remain unchanged.

The original roadmap through 1.0.0 is complete. Future versions may add content or polish while preserving the one-tap core and offline privacy boundary.

## Repository policy

`main` is protected, PR-based and squash-only. CI must pass before merge. GitHub Actions are pinned to immutable commit SHAs. Dependency, static-analysis and secret-scanning automation remains part of the baseline.

## Release policy

Official releases are tag-driven and must match `versionName`. CI validates the tagged `main` commit, restores the owner-provided signing key only in protected runner storage, signs and verifies APK/AAB, generates SHA-256 checksums and provenance attestations, publishes the GitHub Release, then removes temporary signing material.
