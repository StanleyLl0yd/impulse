# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

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
- Semgrep CI now pins the container image by immutable digest.
- Gameplay spawning now uses a deterministic checked fallback instead of overlapping at the field center.
- Collision source selection now resolves same-tick contacts by earliest impact time with deterministic chain-depth tie-breaking.
