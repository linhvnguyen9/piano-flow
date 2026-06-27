# ADR-0011: Konsist for module-boundary rules (over Detekt)

**Status:** Accepted
**Date:** 2026-06-27
**Deciders:** PianoFlow maintainers

## Context

ADR-0005 defined the module boundaries (fine-grained `core/*`, `feature/*` api/impl, Koin) but nothing
*enforced* them — a bad import compiled fine. The harness's "prevention over detection" principle wants
boundary violations caught the moment they're written, the cheapest upstream catch. The first rule we
need: feature UI must go through `core:designsystem` wrappers, never raw `androidx.compose.material3.*`
(so the design system is the single styling chokepoint). The build is bleeding-edge — KMP source sets
and the `com.android.kotlin.multiplatform.library` variant on AGP 9 — where Gradle-plugin-based linters
attach unreliably (see ADR-0001/0006 for the recurring AGP-9 plugin-compat tax).

## Decision

Enforce module boundaries with **Konsist 0.17.3** as plain JVM unit tests, in
`androidApp/src/test/.../architecture/ArchitectureTest.kt` (alongside the other repo-wide tests), run by
`:androidApp:testDebugUnitTest`. Rules today:

- **No raw Material 3 in feature code** — feature production files must not import
  `androidx.compose.material3.*` (use `core:designsystem` wrappers).
- **`core/*` must not depend on `feature/*`**.
- **A feature's `api` must not import its `impl`**.

Three pre-existing feature screens are **grandfathered** via an in-test `MATERIAL3_BURNDOWN` allowlist
(shrink-only, with a guard test that fails if a listed file no longer needs the exemption) — so the rule
gates new code immediately while the debt is burned down deliberately.

Konsist is chosen over Detekt for *these* rules because it **parses source from disk with no Gradle
plugin**, sidestepping the KMP source-set wiring and AGP-9 plugin-compat traps; it's the same "rule as a
host test" paradigm already used by `Tier1Assertions` (ADR-0007); and its layer/import DSL expresses
architecture rules Detekt's `ForbiddenImport` can't. The strongest end-state — not shipping Material 3
to feature modules' classpath at all (so the import won't even resolve) — is noted as a future
structural rung; the Konsist rule is the intermediate gate.

## Options Considered

| Dimension | Konsist (chosen) | Detekt `ForbiddenImport` |
|-----------|------------------|--------------------------|
| Attaches on AGP-9 KMP variant | Test dependency only — no plugin to attach | Gradle plugin; KMP/AGP-9 attach is the known risk here |
| KMP source sets | Parses `commonMain`/`androidMain` from disk | Needs manual `source.setFrom(...)` wiring |
| Expressiveness | Import + architecture-layer rules | Import-glob only |
| Paradigm fit | Same "rule as host test" as `Tier1Assertions` | A second, separate toolchain |
| Grandfathering | In-test allowlist (no built-in baseline) | First-class baseline file |
| Broad style/lint rules | Architecture only | Hundreds of style/complexity rules |

### Option A: Konsist host tests (chosen)
**Pros:** no plugin → dodges the AGP-9/KMP attach risk; KMP-aware; expresses layer + import rules; one
paradigm with Tier-1. **Cons:** no baseline file (grandfathering is a hand-kept allowlist); architecture
only — not a general style linter.

### Option B: Detekt `ForbiddenImport`
**Pros:** first-class baseline; brings a broad style/complexity ruleset; `compose-lints` ecosystem.
**Cons:** Gradle-plugin attach is exactly the fragile path on this toolchain; needs explicit KMP
source-set wiring; import-glob only. Still viable later for *style* rules, complementary to Konsist.

### Option C: Classpath split (don't give feature modules Material 3 at all)
**Pros:** strongest — the import can't resolve (structurally impossible, top of the durability ladder).
**Cons:** the shared `pianoflow.kmp.compose` convention plugin currently puts `compose-material3` on
every Compose module; splitting it is a larger build change. Deferred — the chosen end-state once the
`MATERIAL3_BURNDOWN` burndown reaches zero.

### Option D: Nothing (rely on review/CLAUDE.md)
Rejected — ADR-0005's boundaries stay unenforced; this is the gap this ADR closes.

## Trade-off Analysis

For *boundary* rules on this specific toolchain, Konsist's no-plugin model removes the single biggest
failure mode (plugin attach on the KMP/AGP-9 variant) and keeps one "rules as tests" paradigm. We give
up Detekt's baseline (replaced by a small, visible, shrink-only allowlist) and its broad style ruleset —
acceptable, since this ADR is about architecture, and Detekt can still be added later for style without
conflict. The Konsist rule is explicitly the *intermediate* rung; Option C remains the structural target.

## Consequences

- Easier: boundary violations fail a fast JVM test the moment they're written; new feature code is gated
  immediately; rules read as plain Kotlin and live with the other repo-wide tests.
- Harder: `MATERIAL3_BURNDOWN` must be burned down (and only ever shrink); grandfathering is hand-kept,
  not a baseline file; if broad style linting is later wanted, that's a separate Detekt decision.
- Revisit if: the burndown reaches zero → pursue Option C (classpath split) and drop the grandfather +
  rule; or style/complexity linting becomes desirable → add Detekt alongside (not instead of) Konsist.

## Action Items

1. [x] Add Konsist 0.17.3 (`testImplementation`) + `ArchitectureTest.kt` in `androidApp/src/test`.
2. [x] Rules: no raw Material 3 in feature; `core` ↛ `feature`; feature `api` ↛ `impl`.
3. [x] `MATERIAL3_BURNDOWN` shrink-only allowlist + stale-entry guard test.
4. [x] Verify the gate bites (negative test) and document in `CLAUDE.md`.
5. [ ] Future: classpath split (Option C) once burndown hits zero; supersede the grandfather then.
