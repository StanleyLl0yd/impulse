# AGENTS.md

## Project rules

- Inspect the existing implementation before changing it.
- Keep the project native Android with Kotlin and Jetpack Compose unless explicitly approved otherwise.
- Keep gameplay logic independent from rendering and Android UI where practical.
- Preserve deterministic, seedable simulation behavior.
- Prefer the smallest correct implementation and avoid speculative abstractions.
- Do not introduce a dependency without a concrete need.
- Do not add analytics, ads, accounts, backend services, or network access unless explicitly requested.
- Keep runtime permissions at the minimum required by implemented features.
- Add or update tests for gameplay logic and regressions.
- Run relevant Gradle tests, lint, and builds before considering a task complete.
- Never commit signing keys, passwords, API keys, tokens, local properties, or generated secrets.
- Comments must be minimal, necessary, current, and English-only.
- Do not keep commented-out code or obsolete TODOs.
- Source identifiers must be English. User-facing text must live in Android resources.
- Maintain Russian and English user-facing strings.
- Treat frame pacing, deterministic behavior, instant retry, and offline operation as product requirements.
- Keep `main` buildable and use focused commits/PRs.
- After every release, review and update all repository text files so they accurately reflect the released state.
- Preserve the established formatting and visual presentation of text files during release updates; add, change, or remove formatting only when there is a compelling need.
