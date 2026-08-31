# Security Policy

## Supported versions

Only the latest released version is actively supported during early development.

## Reporting a vulnerability

Use GitHub private vulnerability reporting for this repository when available. Do not disclose credentials, signing material, unpublished vulnerabilities, or other sensitive details in a public issue.

Include the affected version or commit, reproduction steps, expected impact, and any relevant logs with secrets removed.

## Security model

IMPULSE is designed to minimize attack surface:

- no mandatory network access;
- no accounts or backend in the initial releases;
- no advertising or analytics SDKs by default;
- no dangerous Android runtime permissions in the baseline app;
- owner-controlled release signing key supplied outside the repository;
- release signing material provided only through protected GitHub Environment secrets;
- immutable action pinning, dependency updates, static analysis, and secret scanning in CI.

Never commit a keystore, `key.properties`, private key, token, `.env` file, or other credential material.
