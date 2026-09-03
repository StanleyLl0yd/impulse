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
- Music has a separate persistent setting from game sound.
- High Contrast and Reduced Effects are independent persistent accessibility controls.
- High Contrast strengthens field, wave and particle readability without changing gameplay mechanics.
- English and Russian UI remain first-class supported localizations.

## About and public product pages

The in-app About screen sends ordinary users only to public product pages on `stanleyll0yd.github.io`:

- App website: `https://stanleyll0yd.github.io/apps/impulse/`
- Privacy policy: `https://stanleyll0yd.github.io/apps/impulse/privacy/`

The About screen does not expose a GitHub destination.

## Release baseline

IMPULSE 1.0.1 uses `versionCode 13`. The patch release changes only About/product-page presentation and links; gameplay, progression, replay modes, achievements, statistics, audio, accessibility behavior, persistence and offline architecture remain unchanged from 1.0.0.
