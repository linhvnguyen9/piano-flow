# Screenshot Testing

Compose modules use [Roborazzi](https://github.com/takahirom/roborazzi) for Compose
screenshot testing. Tests run on the JVM via Robolectric — no emulator or device
required. Modules with screenshot tests today: `:core:designsystem` and
`:feature:impl:chord-smoother`.

## Where things live (per module)

- Tests: `<module>/src/androidHostTest/kotlin/...`
- Goldens: `<module>/src/androidHostTest/screenshots/` (committed to git)
- Diff reports: `<module>/build/outputs/roborazzi/*_compare.png` (generated on a
  failing verify run)
- Inspection renders: `<module>/build/outputs/roborazzi/_inspect_*.png` (gitignored,
  no committed golden — render-and-look only)

## How the setup is wired

The Roborazzi wiring (the plugin, the `androidHostTest` test dependencies, the
Compose UI-tooling runtime dependency, the golden output directory, and the
`-Proborazzi.test.*` mode plumbing) lives in the `pianoflow.compose-screenshot-testing`
convention plugin under `build-logic/convention/`, not in each module's
`build.gradle.kts`. A module just opts in:

```kotlin
plugins {
    id("pianoflow.kmp.compose")
    id("pianoflow.compose-screenshot-testing")
}
```

To enable screenshot testing in another Compose module, apply that one plugin id
and put goldens under the module's own `src/androidHostTest/screenshots/`.

## Commands

The Roborazzi gradle plugin does not currently auto-wire its lifecycle tasks
(`recordRoborazzi`, `verifyRoborazzi`, `compareRoborazzi`) onto the KMP
`testAndroidHostTest` variant, so we pass the mode as a project property and
invoke the test task directly. The mode is registered as a task input, so
changing it invalidates the test cache automatically — no `--rerun-tasks`
needed.

- Record / refresh goldens after an intentional UI change:

  ```bash
  ./gradlew :feature:impl:chord-smoother:testAndroidHostTest -Proborazzi.test.record=true
  ```

- Verify (fail on visual diff — this is the CI command):

  ```bash
  ./gradlew :feature:impl:chord-smoother:testAndroidHostTest -Proborazzi.test.verify=true
  ```

- Generate a side-by-side compare PNG without failing the build:

  ```bash
  ./gradlew :feature:impl:chord-smoother:testAndroidHostTest -Proborazzi.test.compare=true
  ```

  Output: `feature/impl/chord-smoother/build/outputs/roborazzi/<test>_compare.png`.

Swap in `:core:designsystem` (or any other screenshot module) as needed.

## Inspection tests vs the verify gate

Inspection tests (`*Inspection`) have **no committed golden** — they only render to
`build/outputs/roborazzi/_inspect_*.png` for visual review. On a clean `build/`,
`-Proborazzi.test.verify=true` will **fail** them ("image not found") because there
is nothing to compare against. Run `record` once to seat the inspection images, then
`verify`. Committed regression goldens verify normally either way.

## When to re-record

Re-record goldens only when a visual change is intentional. Re-recorded PNGs
must be reviewed as part of the PR — the diff in the screenshot file is the
review surface.

## Adding a new test

1. Add a JUnit 4 test class under `<module>/src/androidHostTest/kotlin/...`.
2. Annotate with `@RunWith(RobolectricTestRunner::class)`,
   `@GraphicsMode(GraphicsMode.Mode.NATIVE)`, and
   `@Config(qualifiers = "...", sdk = [35])` for deterministic rendering.
   (`sdk = [35]` is required while Robolectric does not yet support SDK 36;
   the project's `compileSdk` is 36.)
3. Compose the subject with `composeRule.setContent { ... }`. Wrap it in
   `MaterialTheme { Surface { ... } }` if it relies on `MaterialTheme`. Prefer
   rendering a **stateless** composable with sample data over a Koin-wired screen.
4. Capture with
   `composeRule.onRoot().captureRoboImage(filePath = "src/androidHostTest/screenshots/<name>.png")`.
   The `filePath` is interpreted relative to the module directory.
5. Run the record command above to create the golden, then commit the test
   file and the PNG together.
