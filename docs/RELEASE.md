# Release process

## One-time setup

After the bootstrap PR is merged, GitHub Actions automatically opens a small follow-up PR containing the generated Gradle Wrapper binary. Merge that PR, then run the repository hardening script and signing setup script from a trusted local machine with GitHub CLI authenticated as the repository owner.

macOS/Linux:

```bash
./tools/bootstrap-github.sh
./tools/setup-release-signing.sh
```

Windows PowerShell:

```powershell
.\tools\bootstrap-github.ps1
.\tools\setup-release-signing.ps1
```

The signing script creates a dedicated IMPULSE keystore locally, prints its location and certificate fingerprint, creates the `release` GitHub Environment, and writes the signing material to environment secrets through `gh secret set`.

Back up the generated keystore and password outside the repository. Losing the release key can prevent publishing compatible updates on stores that rely on that signing identity.

## Publishing a GitHub release

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Update `CHANGELOG.md` and optionally add `.github/release-notes/vX.Y.Z.md`.
3. Merge the version change through a normal PR.
4. Tag the exact `main` commit and push the tag:

```bash
git switch main
git pull --ff-only
git tag v0.1.0
git push origin v0.1.0
```

The release workflow validates the tag and version, reruns tests/lint, builds and signs APK/AAB, verifies both signatures and the certificate SHA-256, creates checksums and attestations, then publishes a GitHub Release.

## RuStore

The first RuStore version should be published manually. After the application has an active RuStore version and its API application identity is known, add scoped RuStore publication credentials and enable the store-publishing workflow. Do not reuse signing or API keys from another application.
