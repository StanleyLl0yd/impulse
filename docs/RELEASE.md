# Release process

## One-time signing setup

The repository does not generate, store, or publish local setup scripts or release keystores. Release signing uses a keystore supplied by the repository owner.

Configure the `release` GitHub Environment from a trusted local machine with authenticated GitHub CLI. The local setup command should upload these environment secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `ANDROID_CERT_SHA256`

The certificate SHA-256 value is used by CI to verify that both the APK and AAB were signed with the expected certificate. Keep the original keystore and its credentials backed up securely outside the repository.

## Publishing a GitHub release

The permanent Android Application ID / Bundle ID and namespace are `com.sl.impulse`. Release validation rejects any other package identity.

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`. `versionCode` must be greater than every previous semver release.
2. Update `CHANGELOG.md` and add `.github/release-notes/vX.Y.Z.md`.
3. Merge the version change through a normal PR.
4. The `Tag release version` workflow waits for successful `Android CI`, `Security and Quality`, and `CodeQL` runs on that `main` commit. If the matching semver tag does not already exist, it creates the tag and explicitly dispatches the Android release workflow.

The release workflow validates the semver tag, `versionName`, monotonic `versionCode`, commit ancestry, and successful required `main` verification. It then reruns tests/lint, builds and signs APK/AAB, verifies both signatures and the configured certificate SHA-256, creates SHA-256 checksums and artifact attestations, and publishes the GitHub Release.

An existing version tag is treated as already released and is never moved automatically. Manual `workflow_dispatch` remains available for retrying an existing release tag when appropriate.
