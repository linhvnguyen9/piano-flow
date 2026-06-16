# ADR-0006: Showkase + Roborazzi Component Catalog (debug browser + committed screenshots)

**Status:** Accepted
**Date:** 2026-06-16
**Deciders:** PianoFlow maintainers

## Context

As the component library grows, both humans and AI coding agents need to discover
which reusable Composables already exist so they reuse rather than re-implement. Two
distinct consumers want two different artifacts:

- **AI agents / GitHub reviewers** need static, committed assets they can read without
  building or running anything — screenshots plus a text index.
- **Developers / designers** benefit from a live, on-device gallery to inspect components
  in real states (theming, sizing, interaction) a static image can't show.

We already have Roborazzi-based Compose screenshot testing (ADR-0001) that runs on the
JVM (ADR-0002) and commits golden PNGs to git (ADR-0003). The toolchain is bleeding-edge
(Kotlin 2.4.0, KSP 2.3.9 / KSP2, AGP 9.0.1, CMP 1.11.1), which constrains what can attach.

## Decision

Add **Airbnb Showkase 1.0.5** as a **debug-only** component catalog, wired centrally in
`:androidApp`, paired with the existing Roborazzi infrastructure:

- **Discovery:** `@ShowkaseComposable` showcase wrappers + a single `@ShowkaseRoot`
  aggregator produce `Showkase.getMetadata().componentList`.
- **Committed screenshots:** a parameterized Robolectric/Roborazzi test iterates that list
  and writes one PNG per component to `docs/components/screenshots/<group>/<name>.png`.
- **Index:** a generator emits `docs/components/COMPONENTS.md` (component → preview, FQN,
  usage) — the entry point an agent reads.
- **Human browser:** a debug-only `ShowkaseLauncherActivity` (a second "PianoFlow Catalog"
  launcher icon) opens the interactive Showkase browser.

Two implementation constraints are part of this decision:

1. **Centralized, not distributed.** Wrappers + `@ShowkaseRoot` live in
   `androidApp/src/debug`, not in each module's `androidMain`. The processor-generated
   metadata references the full `showkase` runtime models, so a per-module layout would
   drag the Showkase browser library into every `:core`/`:feature` module in **all** build
   variants. All catalogued components are module-public, so the app's debug source can host
   the wrappers — keeping Showkase entirely debug-only and out of the core modules.
2. **Roborazzi Gradle plugin not applied to the app module.** On AGP 9 it fails to attach to
   `com.android.application` (expects the removed legacy `TestedExtension`). The Roborazzi
   *runtime* plus a manual `-Proborazzi.test.{record,verify,compare}` → system-property
   bridge drive recording instead; the test writes to explicit file paths.

Scope: module-public reusable components only (atoms/molecules) — not whole screens/sheets,
not `private` screen-internals. Initial catalog: `PianoKeyboard`, `ChordProgressionField`.

## Options Considered

### Option A: Showkase + Roborazzi (chosen)
Annotation-driven registry feeds Roborazzi (committed PNGs + index) **and** a live browser.
**Pros:** serves both consumers; one source of truth drives screenshots and the browser;
annotation model scales to many components/states. **Cons:** extra dependency + KSP processor
on a bleeding-edge toolchain; debug-only wiring complexity; the browser is the least-used
output (agents and GitHub can't open it).

### Option B: Roborazzi-only with an explicit registry (no Showkase)
A plain `catalogComponents` list drives the same `ShowkaseCatalogScreenshotTest` +
`ComponentsIndexTest`. **Pros:** identical committed PNGs + index with no Showkase dependency,
no KSP processor, no AGP-9 workaround. **Cons:** no interactive browser; component list is
hand-maintained. This is the documented fallback if the browser proves unused or the
processor breaks on a future upgrade.

### Option C: Showkase browser only (no committed screenshots)
**Pros:** quickest to a runtime gallery. **Cons:** produces nothing an AI agent or GitHub
reviewer can read — fails the primary goal. Rejected.

## Trade-off Analysis

The committed PNGs + `COMPONENTS.md` carry the value for the read-only consumers; that part
is independent of Showkase (Option B delivers it alone). Showkase's marginal contribution is
the interactive browser plus annotation-driven registration. We accept that extra machinery
for the live browser and to let the catalog scale by annotation rather than a growing list.
Option B remains a clean, low-cost escape hatch, which bounds the downside of this bet.

## Consequences

- Easier: agents/humans discover components from one index; adding a component is a single
  annotated wrapper + one `details` row; screenshots and browser stay in sync from one
  source; reuses the existing Roborazzi/Robolectric conventions.
- Harder: we own a Showkase + KSP-processor dependency on a fast-moving toolchain, plus the
  Roborazzi-Gradle-plugin workaround for the app module.
- Android-only: consistent with ADR-0002 — the catalog covers the Android target; iOS is not
  included (components remain KMP; only the showcase wrappers are Android).
- Revisit if: the browser is confirmed unused **and** the catalog stays small → drop Showkase
  for the Option B explicit registry; or the Showkase processor breaks on a Kotlin/KSP/AGP
  bump → same fallback.

## Action Items

1. [x] Add Showkase 1.0.5 to `libs.versions.toml`; add the `pianoflow.showkase` convention plugin (debug-only).
2. [x] Add `@ShowkaseComposable` wrappers + `@ShowkaseRoot` in `androidApp/src/debug`; prove `Showkase.getMetadata()` resolves under KSP2/Kotlin 2.4.0 (gate test).
3. [x] Parameterized Roborazzi catalog test → committed PNGs under `docs/components/screenshots/`.
4. [x] `ComponentsIndexTest` → generated `docs/components/COMPONENTS.md`.
5. [x] Debug-only `ShowkaseLauncherActivity` for the interactive browser.
6. [x] Document the add-a-component workflow in `CLAUDE.md` + `docs/components/README.md`.
