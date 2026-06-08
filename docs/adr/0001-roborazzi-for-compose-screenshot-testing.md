# ADR-0001: Roborazzi for Compose Screenshot Testing

**Status:** Accepted
**Date:** 2026-06-07
**Deciders:** PianoFlow maintainers

## Context

We want automated detection of visual regressions in shared Compose UI. The
`sharedUI` module uses the newer `com.android.kotlin.multiplatform.library`
Gradle plugin (AGP 9.0.1, Kotlin 2.4.0, CMP 1.11.1), which constrains which
screenshot-testing tools can attach cleanly. We need JVM-runnable tests (no
emulator) that live in the same module as the code under test.

## Decision

Use **Roborazzi** (`io.github.takahirom.roborazzi`) with Robolectric and the
Compose UI test rule. Capture bitmaps via `captureRoboImage()` and compare
byte-wise against committed golden PNGs.

## Options Considered

### Option A: Roborazzi
| Dimension | Assessment |
|-----------|------------|
| Complexity | Med — Robolectric + plugin wiring |
| Cost | Free, OSS |
| KMP support | Explicitly supports the KMP Android library plugin + CMP |
| Team familiarity | Low (new), but JUnit 4 based |

**Pros:** Runs on JVM; tests co-located with code; supports our exact plugin.
**Cons:** Newest toolchain combination is unproven; Robolectric version churn.

### Option B: AndroidX Compose Preview Screenshot Testing
| Dimension | Assessment |
|-----------|------------|
| Complexity | High — likely module restructuring |
| KMP support | Tied to `com.android.library`; compat with KMP plugin unverified |

**Pros:** First-party. **Cons:** Plugin mismatch with `sharedUI`.

### Option C: Paparazzi
| Dimension | Assessment |
|-----------|------------|
| KMP support | Historically weak for KMP Android library modules |

**Pros:** Mature, fast. **Cons:** Would likely need a separate Android-only test module.

## Trade-off Analysis

Both alternatives fight our module setup; only Roborazzi advertises support for
the KMP Android library plugin and keeps tests in `sharedUI`. We accept Roborazzi's
newness, mitigated by a pilot test (see ADR-0002) that proves the path before scaling.

## Consequences

- Easier: screenshot tests sit next to the composables they cover.
- Harder: we own pinning compatible Roborazzi/Robolectric/Compose-test versions.
- Revisit if: the pilot fails to attach — fall back to an Android-only library
  module depending on `sharedUI`.

## Action Items

1. [ ] Pin Roborazzi, Robolectric, `ui-test-junit4` versions in `libs.versions.toml`.
2. [ ] Wire the Roborazzi plugin (root `apply false`, applied in `sharedUI`).
3. [ ] Land the pilot test (ADR-0002).
