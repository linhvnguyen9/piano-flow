# PianoFlow

Kotlin Multiplatform (Android + iOS) Compose Multiplatform app, organized into feature + core modules. iOS app lives in `iosApp/`.

**Module graph:**

- `:core:model` — pure-Kotlin music primitives (`Pitch`, `Chord`, `Quality`). KMP, iOS-capable.
- `:core:audio` — `TonePlayer` (expect/actual) + `ChordSynth`. KMP, iOS-capable.
- `:core:designsystem` — shared Compose UI (`PianoKeyboard`, theme). Android + common.
- `:feature:chord-smoother:api` — public contracts for the "Songs" feature: `ProgressionSolver`, `ChordProgressionParser`, `Voicing`, `ChordSmootherEntry`. KMP, iOS-capable; no Compose UI.
- `:feature:chord-smoother:impl` — the implementation, in Clean-Arch packages `domain/` · `data/` (empty until persistence) · `presentation/` (incl. `SongsViewModel`) · `di/` (Koin module). Android + common.
- `:shared` — KMP umbrella that builds the iOS `SharedLogic` framework (exports `:core:model` + `:core:audio`). Holds template `Greeting`/`Platform` (throwaway).
- `:androidApp` — Android entry point; `PianoFlowApp` starts Koin and registers `chordSmootherModule`; `App()` renders the Koin-injected `ChordSmootherEntry`.

**DI is Koin** (constructor DSL: `singleOf(::Impl) bind Contract::class`, `viewModelOf(::Vm)`). Module build files stay tiny via convention plugins in `build-logic/`: `pianoflow.kmp.library` (KMP + Android + iOS targets), `pianoflow.kmp.compose` (the above minus iOS, plus Compose), and `pianoflow.compose-screenshot-testing` (Roborazzi).

## Visual inspection of Compose screens (preferred over asking the user)

When you need to **see** what a Compose screen actually renders — to verify a layout change, check alignment, sanity-check theming after a tweak — write or run a Roborazzi inspection test and then `Read` the resulting PNG. You are multimodal; the PNG is visible to you.

Inspection tests live alongside regression tests in a module's `src/androidHostTest/kotlin/...` but are distinct:

- **Inspection tests** write to `build/outputs/roborazzi/_inspect_*.png` (gitignored). No committed golden, no comparison. Render-and-look only. Example: `com.linh.pianoflow.feature.chordsmoother.impl.presentation.SongsScreenInspection`. They render the **stateless** `SongsScreenContent` with a sample state (no Koin needed in Robolectric).
- **Screenshot regression tests** write goldens to `<module>/src/androidHostTest/screenshots/` (committed). Diffs fail the build. Examples: `com.linh.pianoflow.core.designsystem.PianoKeyboardScreenshotTest`, `com.linh.pianoflow.feature.chordsmoother.impl.presentation.ChordPickerScreenshotTest`.

Run a screen's inspection matrix and view it:

```bash
./gradlew :feature:chord-smoother:impl:testAndroidHostTest \
  --tests "com.linh.pianoflow.feature.chordsmoother.impl.presentation.SongsScreenInspection" \
  -Proborazzi.test.record=true
```

Then `Read feature/chord-smoother/impl/build/outputs/roborazzi/_inspect_songs_default_411.png` (and the sibling `_inspect_songs_*.png` — one per matrix config).

**Don't delete reusable inspection tests** after running — keep them so future inspections cost one command. New screens get a new `*Inspection` test on first visit.

`SongsScreenInspection` renders the **stress matrix** from the `.claude/skills/mobile-design/SKILL.md` rubric so an evaluator pass can grade the worst case, not just the happy path: width {360, 411} × `fontScale` {1.0, 1.5, 2.0} × {light, dark} × content {default, long, empty, error}. Copy that pattern for new screens' `*Inspection` tests. Two levers:

- **Width** — `@Config(qualifiers = "w360dp-h2000dp-xxhdpi", sdk = [35])` per method (the lever Robolectric honors for display metrics; tall canvas so the `LazyColumn` lays out in one frame). One capture per `@Test` — `setContent` is single-shot per compose rule.
- **Font scale** — override `LocalDensity` (`CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale))`), since Robolectric has no font-scale qualifier. `sp` text scales, `dp` containers don't — mimics accessibility text size and the overflow it causes.

## Executor-evaluator UI loop (after any @Composable change)

Pair the inspection matrix with a separate **evaluator** so UI work is graded, not just rendered. Keep the two lanes apart — the context that edits a `@Composable` must not also grade it (that's self-review). The loop:

1. **Executor** edits the `@Composable`.
2. **Render** the matrix: `./gradlew :<module>:testAndroidHostTest --tests "...<Screen>Inspection" -Proborazzi.test.record=true` → `build/outputs/roborazzi/_inspect_*.png`.
3. **Evaluate** in a separate lane: dispatch the `mobile-design-evaluator` agent (defined in `.claude/agents/mobile-design-evaluator.md`) at the PNG directory. It loads the `mobile-design` skill, reads every PNG, and writes a `pass`/`fail` verdict with element-level fixes to `_verdict.md` beside them. It never edits source.
4. **Read `_verdict.md`**, apply the Tier-1 fixes, loop from step 2 until the verdict passes. Ship on `pass` (Tier-2 issues become follow-ups).

Dispatch it by name (`subagent_type: "mobile-design-evaluator"`), or have a generic agent `Read` and follow `.claude/agents/mobile-design-evaluator.md`. Inspection PNG filenames encode their config (`360`/`411`, `font1_5`/`font2_0`, `_dark`, state words) so the evaluator can attribute each defect to a specific config — keep new captures self-describing.

## Component catalog (check before building new UI)

Before creating a new reusable Composable, check the catalog for one to reuse:

- Index: `docs/components/COMPONENTS.md` — a table of every catalogued component with a screenshot, fully-qualified name, and usage snippet. `Read` it (the PNGs are visible to you).
- Humans: on a **debug** build, open the **PianoFlow Catalog** launcher icon for the interactive Showkase browser.

The catalog is powered by Airbnb Showkase, wired **debug-only and centralized in `:androidApp`** (the `pianoflow.showkase` convention plugin adds `showkase`/`showkase-processor` to debug; the Showkase Gradle plugin is intentionally not applied — it can't attach to a `com.android.application` module on AGP 9). Showcase wrappers and the `@ShowkaseRoot` live in `androidApp/src/debug/.../showcase/`; the Roborazzi catalog test + `ComponentsIndexTest` live in `androidApp/src/test/...`.

To add a component (must be **module-public** so the app's debug source can call it):

1. Add a zero-arg `@ShowkaseComposable(name = …, group = …)` wrapper in `androidApp/src/debug/kotlin/com/linh/pianoflow/showcase/`, wrapped in `PianoFlowTheme { Surface { … } }`.
2. Add a `details` row in `ComponentsIndexTest` (fully-qualified name + usage snippet).
3. Regenerate the PNGs + index:

```bash
./gradlew :androidApp:testDebugUnitTest -Proborazzi.test.record=true
```

Only module-public, reusable components belong in the catalog — not whole screens/sheets, not `private` screen-internals.

## Screenshot testing quirks

Full workflow doc: `docs/SCREENSHOT_TESTING.md`. Things easy to trip on:

- The KMP test variant is `testAndroidHostTest` (not `testDebugUnitTest`). Source set is `androidHostTest`, accessed via `getByName("androidHostTest")` — the type-safe accessor doesn't exist.
- Robolectric is pinned to 4.15.1, which supports up to **SDK 35**. The project's `compileSdk` is 36, so every Robolectric test must declare `@Config(sdk = [35])` or it dies in `DefaultSdkPicker`.
- `captureRoboImage(filePath = "...")` resolves relative to the **module directory**, not Roborazzi's `outputDir`. Goldens use `"src/androidHostTest/screenshots/<name>.png"`; inspections use `"build/outputs/roborazzi/_inspect_*.png"`.
- Roborazzi mode is passed as a Gradle project property (`-Proborazzi.test.record=true` / `verify=true` / `compare=true`) and is registered as a task input — switching modes invalidates the cache automatically; no `--rerun-tasks` needed.
- **`verify` fails inspection tests on a clean `build/`.** Inspection tests have no committed golden, so `-Proborazzi.test.verify=true` errors ("image not found") unless the `_inspect_*.png` was already recorded. Run `record` once to seat them, then `verify`. Regression goldens (committed) verify normally.

## Build basics

- Android SDKs: `compileSdk = 36`, `minSdk = 24`. AGP 9.0.1, Gradle 9.1, Kotlin 2.4.0, CMP 1.11.1.
- Type-safe project accessors are enabled: e.g. `projects.core.model`, `projects.feature.chordSmoother.impl`.
- Library modules use `com.android.kotlin.multiplatform.library` (not the standard `com.android.library`), applied via the convention plugins. Plugin compatibility for that variant is sometimes lagging — check before assuming a plugin attaches cleanly.

## What lives where

- `core/` — shared primitives: `model` (pitch/chord), `audio` (TonePlayer), `designsystem` (PianoKeyboard, theme).
- `feature/chord-smoother/api/`, `feature/chord-smoother/impl/` — the "Songs" chord-smoother feature (contracts vs implementation).
- `shared/` — KMP umbrella for the iOS framework.
- `androidApp/` — Android entry point + Koin startup.
- `iosApp/` — Xcode project (currently the KMP template; real Compose UI is not wired to iOS yet).
- `build-logic/` — convention plugins.
- `docs/superpowers/specs/`, `docs/superpowers/plans/` — design docs and implementation plans (gitignored).
- `docs/adr/` — architecture decision records.
