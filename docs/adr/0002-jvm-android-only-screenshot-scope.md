# ADR-0002: JVM/Android-Only Scope for Screenshot Testing

**Status:** Accepted
**Date:** 2026-06-07
**Deciders:** PianoFlow maintainers

## Context

PianoFlow is a Kotlin Multiplatform app, so screenshot testing could in
principle target Android, iOS, and multiple golden matrices (theme, locale,
size). Attempting all of that on day one would balloon scope before the basic
workflow is proven, and the iOS render path has no equivalent JVM-fast tooling.

## Decision

Scope the initial capability to **Android composables rendered on the JVM**
(Robolectric, no emulator), with a **single pilot test** proving the
record → verify → diff loop end-to-end. Everything else is explicitly deferred.

## Options Considered

### Option A: Android-only, JVM, single pilot (chosen)
| Dimension | Assessment |
|-----------|------------|
| Complexity | Low |
| Time to value | Fast — one test proves the loop |
| Coverage | Narrow by design |

**Pros:** Smallest provable increment; fast feedback; no emulator/CI dependency.
**Cons:** No iOS coverage; no multi-variant matrices yet.

### Option B: Full multiplatform + variant matrix up front
| Dimension | Assessment |
|-----------|------------|
| Complexity | High |
| Time to value | Slow |

**Pros:** Broad coverage immediately. **Cons:** Large unproven surface; high churn risk.

## Trade-off Analysis

A narrow, proven pilot de-risks the toolchain (new AGP + KMP library plugin)
before we invest in breadth. Breadth is cheap to add once the loop works; debugging
a broad matrix on an unproven path is expensive.

## Consequences

- Easier: ship and validate the workflow quickly.
- Harder: iOS visual regressions remain uncovered; contributors must know the limit.
- Revisit when: the pilot passes and we want theme/locale/size matrices or `App`/`SongsScreen` coverage.

## Out of Scope (Explicit)

- iOS / multiplatform screenshot capture.
- `@Preview`-driven test generation.
- Multi-theme / multi-locale / multi-size golden matrices.
- CI pipeline wiring (a documented Gradle command suffices for now).

## Action Items

1. [ ] Land the `PianoKeyboard` pilot test in `sharedUI/src/androidHostTest`.
2. [ ] Defer broader coverage to a follow-up once the pilot is green.
