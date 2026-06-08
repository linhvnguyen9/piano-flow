# PianoFlow

Kotlin Multiplatform (Android + iOS) Compose Multiplatform app. Three Gradle modules: `:sharedLogic`, `:sharedUI`, `:androidApp`. iOS app lives in `iosApp/`.

## Visual inspection of Compose screens (preferred over asking the user)

When you need to **see** what a Compose screen actually renders — to verify a layout change, check alignment, sanity-check theming after a tweak — write or run a Roborazzi inspection test and then `Read` the resulting PNG. You are multimodal; the PNG is visible to you.

Inspection tests live alongside regression tests in `sharedUI/src/androidHostTest/kotlin/...` but are distinct:

- **Inspection tests** write to `build/outputs/roborazzi/_inspect_*.png` (gitignored). No committed golden, no comparison. Render-and-look only. Example: `com.linh.pianoflow.ui.songs.SongsScreenInspection`.
- **Screenshot regression tests** write goldens to `sharedUI/src/androidHostTest/screenshots/` (committed). Diffs fail the build. Example: `com.linh.pianoflow.ui.PianoKeyboardScreenshotTest`.

Run one inspection test and view it:

```bash
./gradlew :sharedUI:testAndroidHostTest \
  --tests "com.linh.pianoflow.ui.songs.SongsScreenInspection.inspect_songsScreen_default" \
  -Proborazzi.test.record=true
```

Then `Read sharedUI/build/outputs/roborazzi/_inspect_songs_screen.png`.

**Don't delete reusable inspection tests** after running — keep them so future inspections cost one command. New screens get a new `*Inspection` test on first visit.

For a scrollable screen, give Robolectric a tall canvas so the `LazyColumn` lays out everything in one frame: `@Config(qualifiers = "w360dp-h2000dp-xxhdpi", sdk = [35])`. See `SongsScreenInspection.inspect_songsScreen_fullProgression` for the pattern.

## Screenshot testing quirks

Full workflow doc: `sharedUI/SCREENSHOT_TESTING.md`. Things easy to trip on:

- The KMP test variant is `testAndroidHostTest` (not `testDebugUnitTest`). Source set is `androidHostTest`, accessed via `getByName("androidHostTest")` — the type-safe accessor doesn't exist.
- Robolectric is pinned to 4.15.1, which supports up to **SDK 35**. The project's `compileSdk` is 36, so every Robolectric test must declare `@Config(sdk = [35])` or it dies in `DefaultSdkPicker`.
- `captureRoboImage(filePath = "...")` resolves relative to the **module directory**, not Roborazzi's `outputDir`. Goldens use `"src/androidHostTest/screenshots/<name>.png"`; inspections use `"build/outputs/roborazzi/_inspect_*.png"`.
- Roborazzi mode is passed as a Gradle project property (`-Proborazzi.test.record=true` / `verify=true` / `compare=true`) and is registered as a task input — switching modes invalidates the cache automatically; no `--rerun-tasks` needed.

## Build basics

- Android SDKs: `compileSdk = 36`, `minSdk = 24`. AGP 9.0.1, Gradle 9.1, Kotlin 2.4.0, CMP 1.11.1.
- Type-safe project accessors are enabled: `projects.sharedLogic`, `projects.sharedUI`.
- `sharedUI` uses `com.android.kotlin.multiplatform.library` (not the standard `com.android.library`). Plugin compatibility for that variant is sometimes lagging — check before assuming a plugin attaches cleanly.

## What lives where

- `sharedLogic/` — pure Kotlin domain (chord solving, voicing, pitch utilities). `commonMain` + `commonTest`, no Android dependencies.
- `sharedUI/` — Compose UI shared across Android + iOS.
- `androidApp/` — Android entry point.
- `iosApp/` — Xcode project.
- `docs/superpowers/specs/`, `docs/superpowers/plans/` — design docs and implementation plans from prior brainstorming sessions.
