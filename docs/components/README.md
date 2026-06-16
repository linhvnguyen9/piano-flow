# Component catalog

Generated artifacts — do not edit by hand.

- `COMPONENTS.md` — the index (start here).
- `screenshots/<group>/<name>.png` — one render per catalogued component.

Source of truth: `@ShowkaseComposable` wrappers in `androidApp/src/debug/kotlin/com/linh/pianoflow/showcase/`,
aggregated by `androidApp/src/debug/.../showcase/PianoFlowRootModule.kt` (`@ShowkaseRoot`). The PNGs are
produced by `ShowkaseCatalogScreenshotTest` (Roborazzi) and the index by `ComponentsIndexTest`, both in
`androidApp/src/test/...`. Regenerate both with:

    ./gradlew :androidApp:testDebugUnitTest -Proborazzi.test.record=true

Humans can also browse interactively: install a **debug** build and open the **PianoFlow Catalog** launcher icon.

See the project `CLAUDE.md` → "Component catalog" for the contribution workflow.
