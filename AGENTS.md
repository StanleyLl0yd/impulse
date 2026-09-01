# AGENTS.md

## Project rules

- Inspect the existing implementation before changing it.
- Keep the project native Android with Kotlin and Jetpack Compose unless explicitly approved otherwise.
- Keep gameplay logic independent from rendering and Android UI where practical.
- Preserve deterministic, seedable simulation behavior.
- Keep gameplay coordinates independent of device aspect ratio through the canonical logical playfield.
- Keep the fixed 60 Hz simulation independent from display refresh rate and use interpolation for higher-refresh rendering.
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

## Repository-wide audit and refactoring protocol

When asked to audit, optimize, clean up, simplify, or deeply refactor the project, treat the task as a repository-wide correctness-preserving reduction of unnecessary complexity, not as a stylistic rewrite.

### Primary objective

- Minimize the necessary complexity, size, duplication, and maintenance burden of the codebase while preserving 100% of current functionality, behavior, UI/UX, public interfaces, data formats, edge-case behavior, and documented capabilities.
- Prefer deletion over retention when code is proven unnecessary; prefer simplification over abstraction when both are equally correct.
- Do not optimize for the minimum line count at the expense of clarity, safety, or maintainability.
- Do not change code merely to make it different. Every change must have an objective benefit such as less code, less duplication, fewer states or branches, lower coupling, fewer dependencies, clearer ownership, or lower runtime cost.

### Required audit scope

Inspect the entire repository before making conclusions, including:

- production source code;
- tests and test utilities;
- Android resources and manifests;
- Gradle and build logic;
- CI/CD workflows and release automation;
- dependencies and development tooling;
- configuration files;
- documentation;
- generated or checked-in assets where relevant;
- directory and module structure.

Identify the actual architecture and every current user-visible and internal product capability before refactoring.

### Removal candidates

Actively look for and remove, when proven safe:

- dead or unreachable code;
- unused functions, classes, methods, variables, constants, types, interfaces, imports, files, and resources;
- legacy code that no longer participates in current behavior;
- obsolete workarounds and compatibility branches;
- duplicated or near-duplicated code;
- unnecessary abstraction layers, wrappers, helpers, adapters, and conversions;
- redundant intermediate objects and states;
- checks that repeat guarantees already enforced by types, architecture, validation, or earlier control flow;
- repeated validation of the same condition;
- redundant fallbacks and defensive branches;
- unused dependencies and tooling dependencies;
- obsolete configuration, feature flags, TODO/FIXME items, and commented-out code.

Do not classify something as unused only because a textual search finds no direct caller. Check callbacks, events, framework conventions, Android lifecycle and resources, reflection or dynamic loading, Gradle wiring, CI, configuration, platform integration, and other indirect usage paths.

### Simplification candidates

Look for opportunities to:

- shorten code without reducing readability;
- simplify control flow and reduce nesting;
- combine equivalent logic;
- replace custom code with suitable Kotlin, Android, Compose, Gradle, or standard-library facilities;
- remove unnecessary temporary values, copies, passes, loops, allocations, and transformations;
- reduce duplicated state and branching;
- centralize genuinely shared logic only when doing so reduces total code and complexity;
- remove premature generalization and architecture that exists only for hypothetical future needs;
- merge or delete components, classes, modules, or layers whose separation no longer provides value.

Pay special attention to repeated null checks, repeated validation of already validated data, excessive `try/catch`, wrapper-to-wrapper chains, DTO/model conversion chains, duplicated state, stale compatibility code, and leftovers from previous refactors.

### Architecture review

Verify that the architecture matches the real complexity of the current product. Look for:

- historical layers or components that are no longer necessary;
- over-abstraction;
- premature extensibility;
- speculative interfaces or generalized infrastructure;
- modules or classes that can be safely removed or merged;
- coupling caused by duplicated business rules or unclear ownership of state.

Do not perform a large rewrite merely because another architecture appears cleaner.

### Performance review

Optimize only where the benefit is practical, measurable, or obvious. Check for:

- repeated calculations;
- unnecessary recomposition, rendering, rebuilding, or state propagation;
- repeated reads or transformations of the same data;
- avoidable allocations and copies in hot paths;
- redundant loops or multiple passes;
- inappropriate data structures;
- work that can safely be performed once instead of repeatedly.

Do not introduce micro-optimizations that make the code harder to understand without a meaningful benefit.

### Dependency review

For every dependency:

- confirm that it is actually used;
- remove unused or duplicate-purpose dependencies;
- prefer platform or standard-library functionality when a dependency can be replaced by a small amount of simpler code;
- do not replace a well-maintained library with custom code unless the custom implementation clearly reduces project complexity and risk.

### Behavior-preservation rules

Unless explicitly authorized, do not:

- remove user-facing features;
- change existing UX or visual behavior;
- change business logic;
- change public APIs or contracts;
- change persisted or exchanged data formats;
- change documented edge-case behavior;
- intentionally reduce functionality to reduce code size;
- add unrelated features;
- add architectural complexity for the sake of a pattern or best practice.

When uncertain whether code is safe to remove, keep the working behavior until its redundancy is proven.

### Required working method

1. Inspect the whole repository and map current architecture and functionality.
2. Build an internal candidate list for deletion, merging, simplification, and practical optimization.
3. Prove each deletion safe against direct and indirect usage.
4. Make changes in small, coherent logical groups rather than as a broad rewrite.
5. After every significant group, run the relevant available tests, lint, build, static analysis, and project-specific checks.
6. If test coverage is insufficient for a risky change, first add the smallest regression test needed to lock current behavior.
7. At the end, run the full available clean build, complete test suite, lint, static analysis/type checks where applicable, and all project-specific verification.
8. Perform a second complete pass over the refactored repository and again look for remaining dead code, duplication, unnecessary abstractions, redundant checks, unused dependencies, and legacy remnants created or exposed by the first pass.

### Completion standard

A repository-wide refactoring task is not complete merely because code was formatted or recommendations were listed. Apply all safe, justified improvements directly to the project and leave the codebase objectively smaller, simpler, or easier to maintain without functional regressions.

The final report must include, when applicable and reliably measurable:

1. what was removed;
2. what was merged;
3. what was simplified;
4. which dependencies were removed;
5. which potential legacy components were found;
6. what was intentionally left unchanged and why;
7. which tests, builds, lint, static analysis, and project-specific checks were run;
8. any areas that could not be safely optimized without additional information or coverage;
9. before/after statistics such as file count, source lines, dependency count, test count, and production artifact size.
