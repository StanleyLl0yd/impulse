# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

## 0.9.0 - 2026-09-02

### Added

- Endless mode with deterministic 40-field runs, no repeats inside each 40-round cycle, escalating targets across later cycles, persistent best round, and best run score.
- Daily Impulse with one deterministic advanced challenge selected from the local calendar date and persistent daily best score/stars.
- Native Android Sharesheet support for replay results without adding network access or storage permissions.
- Replay statistics and four new local achievements for Daily and Endless milestones.
- English and Russian replay UI, result sharing text, hints, and achievement content.
- Unit and API 37 runtime coverage for replay challenge generation, menu navigation, and replay progress surfaces.

### Changed

- Replay attempts contribute to the existing local attempt, success, particle, and chain-depth statistics.
- Android version is now 0.9.0 with `versionCode 11`.

## 0.8.0 - 2026-09-02

### Added

- Twenty additional deterministic campaign levels, expanding the campaign from 40 to 60 levels.
- Six ten-level chapters: Impulse, Momentum, Boost, Control, Resonance, and Chaos.
- A data-driven catalog of 21 local achievements across Journey, Mastery, Chain, and Endurance.
- Chapter completion and perfection progress in Achievements.
- Verified deterministic winning openings for every level from 41 through 60.

### Changed

- The Level Picker now groups the full 60-level campaign by chapter.
- Players can upgrade directly from both the former 20-level and 40-level campaigns without losing progression.
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

- Replaced the launcher icon with a new neon chain-reaction emblem and Android 13+ monochrome adaptive-icon layer.
- Android version is now 0.6.1 with `versionCode 8`.

## 0.6.0 - 2026-09-02

### Added

- Contextual onboarding across the first three campaign levels.
- Persistent local gameplay statistics.
- Expanded Achievements and enhanced result panel.

### Changed

- Android version is now 0.6.0 with `versionCode 7`.

## 0.5.0 - 2026-09-01

### Added

- Dedicated main menu with Continue, New game, Achievements, About, and Exit.

### Changed

- Android version is now 0.5.0 with `versionCode 6`.

## 0.4.0 - 2026-09-01

### Added

- Twenty deterministic data-driven campaign levels with progression, scoring, stars, and DataStore persistence.

### Changed

- Android version is now 0.4.0 with `versionCode 5`.

## 0.3.1 - 2026-08-31

### Added

- Soft melodic game audio.

### Changed

- Permanent application identity is `com.sl.impulse`.

## 0.3.0 - 2026-08-31

### Added

- Cinematic launch splash and verified automatic semver release dispatch.

## 0.2.0 - 2026-08-31

### Added

- Layered neon effects, sound, haptics, Game Feel controls, and stress coverage.

## 0.1.0 - 2026-08-31

### Added

- Native Kotlin/Compose bootstrap and deterministic one-tap chain-reaction prototype.
