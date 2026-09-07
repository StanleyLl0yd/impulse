# Security baseline

IMPULSE is the reference security baseline for the maintainer's Android repositories. Controls are selected for practical risk reduction without adding approval ceremony that provides little value to a single-maintainer project.

## Repository protection

The target repository ruleset baseline is:

- default branch protection for `~DEFAULT_BRANCH`;
- deletion and non-fast-forward updates forbidden;
- squash-only pull requests and linear history;
- required signed commits;
- required conversation resolution;
- strict required status checks;
- release tags matching `refs/tags/v*` are immutable after creation;
- CodeQL code-scanning enforcement at `medium_or_higher` security severity with error-level merge blocking.

Required merge gates:

- `Verify`;
- `Analyze Java and Kotlin`;
- `Semgrep`;
- `Gitleaks`;
- `Dependency Review` (direct ruleset target; until repository-admin ruleset mutation is available, it is also enforced transitively by the required `Verify` gate).

Qodana remains scheduled/manual rather than required because an external-service or tooling failure must not routinely deadlock development.

## CI/CD supply chain

- Every non-local GitHub Action is pinned to a full 40-character commit SHA.
- Human-readable version comments are retained next to pins.
- Workflow container images are pinned by immutable SHA-256 digest.
- `pull_request_target`, persisted checkout credentials, and inherited reusable-workflow secrets are forbidden by repository policy checks.
- Dependabot updates both Gradle dependencies and SHA-pinned GitHub Actions.
- Workflow permissions default to `permissions: {}` or read-only access.
- Write permissions are isolated to the smallest release-orchestration/publish jobs.
- OIDC `id-token: write` and `attestations: write` are isolated to the artifact-attestation job.

## Analysis and dependency security

- CodeQL advanced setup for Java/Kotlin with `security-extended` queries.
- Semgrep security-audit and secrets rules.
- Gitleaks full-history secret scanning.
- Official GitHub Dependency Review on pull requests, failing for newly introduced high/critical known vulnerabilities while retaining license analysis.
- Qodana JVM analysis on scheduled/manual runs.
- Dependabot weekly updates for Gradle and GitHub Actions.

## Release integrity

- Owner-provided release signing key; no keystore is stored in the repository.
- Signing material is supplied only through GitHub Environment secrets.
- Release tag/version/application identity and monotonic `versionCode` are validated.
- Release source must be a verified `main` commit.
- APK and AAB signatures and expected certificate fingerprint are verified.
- Launcher raster integrity is validated in source and inside the generated AAB.
- SHA-256 checksums are generated and verified before publication.
- APK and AAB receive OIDC-backed GitHub artifact attestations.
- Existing releases are not overwritten.

## Application attack surface

- No baseline internet permission, dangerous runtime permissions, analytics, or advertising SDKs.
- `android:usesCleartextTraffic="false"`.
- No unnecessary exported components.
- Local secret/signing files and build artifacts are ignored and additionally scanned by Gitleaks.
