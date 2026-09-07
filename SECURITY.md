# Security Policy

## Supported versions

IMPULSE follows a latest-release support model.

| Version | Supported |
| --- | --- |
| Latest published release | Yes |
| Older releases | No |

Security fixes are normally shipped in a new release rather than backported to older versions.

## Reporting a vulnerability

Use GitHub private vulnerability reporting for this repository. Do not disclose a suspected vulnerability, zero-day, credential, signing material, token, private key, or other sensitive detail in a public issue, discussion, pull request, commit message, or log.

Include the affected version or commit, reproduction steps, expected impact, and relevant logs or screenshots with secrets removed. If the issue may expose credentials or signing material, stop testing after establishing the minimum evidence needed to report it safely.

## Triage process

- Initial acknowledgement target: within 3 business days.
- Initial severity and scope assessment target: within 7 business days.
- Valid reports are reproduced when practical, assigned a severity, and tracked privately until a fix or mitigation is available.
- Critical and high-impact issues are prioritized over feature work.
- Disclosure timing is coordinated after affected users have a reasonable opportunity to update.

These are response targets, not a guarantee of a specific remediation date.

## Security scope

In scope:

- Android application code and packaged resources;
- dependency and build-toolchain risks introduced by this repository;
- GitHub Actions, CI/CD, release signing, checksums, provenance, and release integrity;
- Android manifest, exported components, permissions, local data handling, and unintended network exposure;
- repository-secret exposure or supply-chain weaknesses caused by repository configuration.

Generally out of scope unless the repository directly causes or amplifies the issue:

- vulnerabilities in GitHub, Android, device firmware, app stores, or other third-party infrastructure;
- social engineering and phishing;
- denial-of-service requiring unrealistic local resource exhaustion;
- findings that require a rooted/compromised device and do not cross an additional IMPULSE trust boundary.

## Security model

IMPULSE minimizes attack surface:

- no mandatory network access;
- no accounts or backend in the baseline application;
- no advertising or analytics SDKs by default;
- no dangerous Android runtime permissions in the baseline app;
- owner-controlled release signing key supplied outside the repository;
- release signing material provided only through GitHub Environment secrets;
- release APK/AAB signature and certificate verification;
- immutable release intent, checksums, OIDC-backed artifact attestations, and provenance;
- SHA-pinned GitHub Actions and digest-pinned workflow containers;
- CodeQL, Semgrep, Gitleaks, Dependency Review, Qodana, and Dependabot;
- protected squash-only linear `main` with required security gates.

Never commit a keystore, `key.properties`, private key, token, `.env` file, service-account credential, or other credential material.
