This is a Kotlin Multiplatform project targeting Android and iOS, organized into feature + core modules with Koin DI.

### Modules

* [/iosApp](./iosApp/iosApp) contains the iOS application entry point. Even when sharing UI with Compose
  Multiplatform you need this for the iOS app; SwiftUI code also goes here.
* [/core/model](./core/model/src) — pure-Kotlin music primitives (`Pitch`, `Chord`, `Quality`).
* [/core/audio](./core/audio/src) — `TonePlayer` (expect/actual) and `ChordSynth`.
* [/core/designsystem](./core/designsystem/src) — shared Compose UI (`PianoKeyboard`, theme).
* [/feature/chord-smoother/api](./feature/chord-smoother/api/src) — public contracts for the "Songs" feature
  (solver/parser seams, `Voicing`, navigation entry).
* [/feature/chord-smoother/impl](./feature/chord-smoother/impl/src) — its implementation, in Clean-Arch
  packages (`domain` / `data` / `presentation` / `di`).
* [/shared](./shared/src) — KMP umbrella that builds the iOS `SharedLogic` framework (exports the core modules).
* [/androidApp](./androidApp/src) — Android entry point; starts Koin.
* [/build-logic](./build-logic) — Gradle convention plugins shared across modules.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Domain + UI (Android host): `./gradlew :feature:chord-smoother:impl:testAndroidHostTest :core:designsystem:testAndroidHostTest`
  - Screenshot goldens verify with `-Proborazzi.test.verify=true`. Inspection-only tests need a `-Proborazzi.test.record=true` pass first on a clean build (see `docs/SCREENSHOT_TESTING.md`).
- iOS framework link: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
