# Screenshot Testing (sharedUI)

This module uses [Roborazzi](https://github.com/takahirom/roborazzi) for Compose
screenshot testing. Tests run on the JVM via Robolectric — no emulator or device
required.

## Where things live

- Tests: `src/androidHostTest/kotlin/...`
- Goldens: `src/androidHostTest/screenshots/` (committed to git)
- Diff reports: `build/outputs/roborazzi/*_compare.png` (generated on a failing
  verify run)

## Commands

The Roborazzi gradle plugin does not currently auto-wire its lifecycle tasks
(`recordRoborazzi`, `verifyRoborazzi`, `compareRoborazzi`) onto the KMP
`testAndroidHostTest` variant, so we pass the mode as a project property and
invoke the test task directly. `--rerun-tasks` forces the test to re-execute
even when Gradle considers it up-to-date.

- Record / refresh goldens after an intentional UI change:

  ```bash
  ./gradlew :sharedUI:testAndroidHostTest -Proborazzi.test.record=true --rerun-tasks
  ```

- Verify (fail on visual diff — this is the CI command):

  ```bash
  ./gradlew :sharedUI:testAndroidHostTest -Proborazzi.test.verify=true --rerun-tasks
  ```

- Generate a side-by-side compare PNG without failing the build:

  ```bash
  ./gradlew :sharedUI:testAndroidHostTest -Proborazzi.test.compare=true --rerun-tasks
  ```

  Output: `sharedUI/build/outputs/roborazzi/<test>_compare.png`.

## When to re-record

Re-record goldens only when a visual change is intentional. Re-recorded PNGs
must be reviewed as part of the PR — the diff in the screenshot file is the
review surface.

## Adding a new test

1. Add a JUnit 4 test class under `src/androidHostTest/kotlin/...`.
2. Annotate with `@RunWith(RobolectricTestRunner::class)`,
   `@GraphicsMode(GraphicsMode.Mode.NATIVE)`, and
   `@Config(qualifiers = "...", sdk = [35])` for deterministic rendering.
   (`sdk = [35]` is required while Robolectric does not yet support SDK 36;
   the project's `compileSdk` is 36.)
3. Compose the subject with `composeRule.setContent { ... }`. Wrap it in
   `MaterialTheme { Surface { ... } }` if it relies on `MaterialTheme`.
4. Capture with
   `composeRule.onRoot().captureRoboImage(filePath = "src/androidHostTest/screenshots/<name>.png")`.
   The `filePath` is interpreted relative to the module directory.
5. Run the record command above to create the golden, then commit the test
   file and the PNG together.
