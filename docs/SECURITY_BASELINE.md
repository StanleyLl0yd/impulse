# Security baseline

The repository baseline combines the strongest practices already used across the maintainer's Android projects and tightens them for a new application.

- Dedicated IMPULSE signing key.
- GitHub Environment secrets for release material.
- Tag-driven release intent.
- APK and AAB signature/fingerprint verification.
- SHA-256 release checksums and GitHub artifact attestations.
- Protected squash-only `main` with required CI.
- GitHub Actions pinned by commit SHA with `persist-credentials: false`.
- Least-privilege workflow permissions.
- Gradle wrapper validation.
- Android unit, lint, build and emulator tests.
- CodeQL, Semgrep, Gitleaks and Qodana.
- Dependabot for Gradle and GitHub Actions.
- GitHub dependency/security features enabled by bootstrap script.
- No baseline internet permission, dangerous runtime permissions, analytics or advertising SDKs.
- `android:usesCleartextTraffic="false"` and no unnecessary exported components.
