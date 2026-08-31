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

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`. `versionCode` must be greater than every previous semver release.
2. Update `CHANGELOG.md` and optionally add `.github/release-notes/vX.Y.Z.md`.
3. Merge the version change through a normal PR and wait for successful `Android CI`, `Security and Quality`, and `CodeQL` runs on the resulting `main` commit.
4. Tag that exact verified `main` commit and push the tag:

```bash
git switch main
git pull --ff-only
git tag v0.2.0
git push origin v0.2.0
```

The release workflow validates the semver tag, `versionName`, monotonic `versionCode`, commit ancestry, and successful required `main` verification. It then reruns tests/lint, builds and signs APK/AAB, verifies both signatures and the configured certificate SHA-256, creates SHA-256 checksums and artifact attestations, and publishes the GitHub Release.
