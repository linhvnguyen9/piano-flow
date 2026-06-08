# Compose Screenshot Testing — Design

**Date:** 2026-06-07
**Status:** Approved (design); pending implementation plan
**Module:** `sharedUI`

> Key decisions from this design are recorded as ADRs in [`../../adr/`](../../adr/README.md):
> [0001](../../adr/0001-roborazzi-for-compose-screenshot-testing.md) (Roborazzi),
> [0002](../../adr/0002-jvm-android-only-screenshot-scope.md) (JVM/Android-only scope),
> [0003](../../adr/0003-golden-images-committed-to-git.md) (goldens in git),
> [0004](../../adr/0004-robolectric-sdk-pin.md) (Robolectric SDK pin).

## Goal

Add Compose screenshot testing to PianoFlow so visual regressions in shared UI composables are caught automatically. Initial scope is Android-only, running on the JVM (no emulator), with one pilot test proving the workflow end-to-end.

## Non-Goals

- iOS / multi-platform screenshot capture.
- CI pipeline wiring (a documented gradle command is sufficient for now).
- `@Preview`-annotation-driven testing.
- Multi-variant golden matrices (theme, locale, size sweeps).
- Coverage of every existing composable on day one.

## Tool Choice

**Roborazzi** (`io.github.takahirom.roborazzi`).

Why not the alternatives:

- **AndroidX Compose Preview Screenshot Testing** (`com.android.compose.screenshot`) — tied to the standard `com.android.library` plugin. `sharedUI` uses the newer `com.android.kotlin.multiplatform.library` plugin; compatibility is unverified and likely to require module restructuring.
- **Paparazzi** — strong tool but historically limited support for KMP Android library modules; would likely require a separate Android-only test module.

Roborazzi explicitly supports the KMP Android library plugin and Compose Multiplatform, runs on the JVM via Robolectric, and keeps tests in the same module as the code under test.

## Architecture

A single JVM test process handles each screenshot test:

1. **Robolectric** provides the Android runtime environment.
2. **Compose UI Test rule** (`createComposeRule()`) composes the target composable.
3. **Roborazzi** captures the resulting bitmap via `captureRoboImage()`.
4. The captured image is compared byte-wise against a golden PNG stored in the repo.

Tests are written as ordinary JUnit 4 tests — there is no reliance on `@Preview` annotations.

## Module Layout

All code lives in the existing `sharedUI` module:

- **Test source set:** `sharedUI/src/androidHostTest/kotlin/...`
  - This is the KMP Android library plugin's host-test source set, already enabled in `sharedUI/build.gradle.kts` via `withHostTest { isIncludeAndroidResources = true }`.
- **Golden images:** `sharedUI/src/androidHostTest/screenshots/`
  - Committed to git so pull requests surface image diffs directly.

No new Gradle modules are introduced.

## Dependencies

Added to `gradle/libs.versions.toml`:

- `io.github.takahirom.roborazzi:roborazzi`
- `io.github.takahirom.roborazzi:roborazzi-compose`
- `org.robolectric:robolectric`
- `androidx.compose.ui:ui-test-junit4`

Already present and reused:

- `junit:junit`
- `kotlin-test`

Versions for Roborazzi, Robolectric, and Compose UI test will be pinned in `libs.versions.toml` during implementation, choosing versions known to be compatible with Kotlin 2.4.0 / CMP 1.11.1 / AGP 9.0.1.

## Plugin Wiring

- Root `build.gradle.kts`: declare the Roborazzi plugin with `apply false`.
- `sharedUI/build.gradle.kts`: apply `id("io.github.takahirom.roborazzi")`.
- Plugin version in `libs.versions.toml` alongside other plugins.

Test dependencies are wired into the `androidHostTest` source set of the Kotlin Multiplatform DSL.

## Pilot Test

Single test class proving the workflow end-to-end:

- **File:** `sharedUI/src/androidHostTest/kotlin/com/linh/pianoflow/ui/PianoKeyboardScreenshotTest.kt`
- **Subject:** `PianoKeyboard` composable in its default state.
- **Configuration:**
  - `@RunWith(RobolectricTestRunner::class)`
  - `@Config(qualifiers = "w360dp-h640dp-xxhdpi")` for deterministic rendering.
- **Method:** Compose `PianoKeyboard` inside a fixed-size box (e.g. 360×120 dp), call `captureRoboImage("piano_keyboard_default.png")`.
- **Golden:** `sharedUI/src/androidHostTest/screenshots/piano_keyboard_default.png`, committed to git.

## Developer Workflow

- `./gradlew :sharedUI:recordRoborazzi*` — write or refresh golden images after intentional UI changes.
- `./gradlew :sharedUI:verifyRoborazzi*` — fail the build when the rendered image differs from the golden. This is the command intended for future CI integration.
- `./gradlew :sharedUI:compareRoborazzi*` — generate an HTML diff report for visual inspection.

The exact task suffix depends on the variant name produced by the KMP Android library plugin and will be confirmed during implementation, then documented in the module's README or a top-level docs entry.

## Testing the Tester

For the pilot to be considered done:

1. `recordRoborazzi*` produces `piano_keyboard_default.png` on a clean checkout.
2. `verifyRoborazzi*` passes immediately after recording.
3. Intentionally changing `PianoKeyboard` (e.g. a color tweak) causes `verifyRoborazzi*` to fail with a diff report.
4. Reverting the change and re-running `verifyRoborazzi*` passes again.

## Risks and Mitigations

- **AGP 9.0.1 + `com.android.kotlin.multiplatform.library` is very new.** Roborazzi nominally supports it, but task names or source-set wiring may differ from public documentation. *Mitigation:* during implementation, inspect the actual task graph (`./gradlew :sharedUI:tasks --all | grep -i robo`) to discover real task names; if Roborazzi fails to attach, fall back to a small Android-only library module that depends on `sharedUI` and hosts the tests.
- **Compose Multiplatform vs AndroidX Compose APIs on the test path.** `roborazzi-compose` targets AndroidX Compose APIs; CMP composables compile to those same APIs on Android, so it is expected to work. *Mitigation:* the pilot test is exactly the verification step for this assumption — if it fails, address before scaling to more tests.
- **Golden image churn.** Font rendering and antialiasing can shift between Robolectric versions. *Mitigation:* pin Robolectric version explicitly; document that golden refreshes must be reviewed as part of the PR.

## Open Questions Deferred to Implementation

- Exact pinned versions of Roborazzi, Robolectric, and `ui-test-junit4`.
- Final golden directory configuration (Roborazzi's default is `build/outputs/roborazzi/`; we want it under `src/androidHostTest/screenshots/` for git checkin — this is set via Roborazzi's `roborazzi { outputDir = ... }` extension).
- Whether to add a brief README in `sharedUI/` describing how to record/verify, or fold it into top-level docs.

## Out of Scope (Explicit)

- iOS screenshot testing.
- Multi-theme / multi-locale golden matrices.
- Visual regression on `App` and `SongsScreen` (deferred until pilot proves the workflow).
- CI integration.
- `@Preview`-driven test generation.
