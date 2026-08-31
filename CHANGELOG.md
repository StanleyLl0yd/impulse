# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

### Added

- Cinematic launch splash using the IMPULSE chain-reaction artwork, fading from pure black to full visibility over three seconds before gameplay starts.

## 0.2.0 - 2026-08-31

### Added

- Layered neon wave rendering, particle trails, activation flashes, pulse bursts, and depth-reactive visual feedback.
- Sound feedback for the initial impulse, chain reactions, success, and failure without adding network or analytics dependencies.
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
