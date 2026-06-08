# ADR-0004: Pin Robolectric to SDK 35 While compileSdk Is 36

**Status:** Accepted
**Date:** 2026-06-07
**Deciders:** PianoFlow maintainers

## Context

The project builds against `android-compileSdk = "36"` (see
`gradle/libs.versions.toml`). Robolectric provides the Android runtime for our
JVM screenshot tests, but Robolectric ships SDK support on a lag — released
versions do not yet include a SDK 36 runtime sandbox. Running a test on an SDK
Robolectric doesn't bundle fails at runtime.

## Decision

Pin the Robolectric runtime to **SDK 35** for screenshot tests (via
`@Config(sdk = [35])` or an equivalent `robolectric.properties`) while the module
continues to compile against `compileSdk = 36`. Lift the pin once Robolectric
ships SDK 36 support (4.16+).

## Options Considered

### Option A: Pin Robolectric to SDK 35, keep compileSdk 36 (chosen)
| Dimension | Assessment |
|-----------|------------|
| Complexity | Low — one `@Config`/properties line |
| Risk | Low — SDK 35 vs 36 render delta is negligible for our composables |

**Pros:** Unblocks screenshot tests today; no app-wide downgrade.
**Cons:** A second SDK number to track; must remember to lift the pin later.

### Option B: Lower compileSdk to 35 project-wide
**Pros:** One SDK everywhere. **Cons:** Gives up SDK 36 APIs across the whole
app to satisfy a test-only constraint — wrong trade.

### Option C: Wait for Robolectric SDK 36 support
**Pros:** No pin. **Cons:** Blocks the whole feature on an upstream release date
we don't control.

## Trade-off Analysis

The pin is a localized, test-only workaround; lowering `compileSdk` would let a
test dependency dictate production API availability. Waiting blocks delivery on
an external timeline. The pin is the smallest, most reversible choice.

## Consequences

- Easier: screenshot tests run now, on a stable Robolectric runtime.
- Harder: goldens render under SDK 35; tracked separately from `compileSdk`.
- Revisit when: Robolectric 4.16+ ships SDK 36 support — drop the pin and
  re-record goldens if any render delta appears.

## Action Items

1. [ ] Add the SDK 35 pin when wiring the pilot test.
2. [ ] File a follow-up to remove the pin once Robolectric supports SDK 36.
