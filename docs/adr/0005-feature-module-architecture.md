# ADR-0005: Feature-Module Architecture (api/impl + fine-grained core + Koin)

**Status:** Accepted
**Date:** 2026-06-08
**Deciders:** PianoFlow maintainers

## Context

PianoFlow began as three coarse modules (`:sharedLogic`, `:sharedUI`, `:androidApp`).
The roadmap adds more features (a sight-reading "Drill" mode, saved progressions) and
we want, in priority order: (1) the structure to scale to many features, (2) faster
incremental builds and isolated tests, and (3) enforced boundaries so UI cannot reach
into domain internals. The codebase is small today (one feature), so the structure
must earn its keep without over-engineering.

## Decision

Restructure into a feature-oriented graph:

- **Fine-grained core**: `:core:model` (Pitch/Chord), `:core:audio` (TonePlayer/ChordSynth),
  `:core:designsystem` (PianoKeyboard, theme). Shared primitives, each independently
  buildable.
- **Per-feature api/impl split**: `:feature:chord-smoother:api` exposes public contracts
  (the `ProgressionSolver` / `ChordProgressionParser` domain seams, the `Voicing` model,
  and a `ChordSmootherEntry` navigation seam); `:feature:chord-smoother:impl` holds the
  concrete implementation in Clean-Arch packages `domain` / `data` / `presentation` / `di`.
  Other modules depend on **api**; **impl** is private and wired at the app's composition root.
- **DI via Koin** (constructor DSL), started in `:androidApp`'s `Application`.
- **`:shared` umbrella** preserves the iOS `SharedLogic` framework (exports the core
  modules), keeping the framework name stable so Xcode wiring is untouched.
- **Convention plugins** in `build-logic/` (`pianoflow.kmp.library`, `pianoflow.kmp.compose`)
  keep per-module build files to a namespace + dependencies.

## Options Considered

### Option A: Per-feature api/impl + fine-grained core (chosen)
Public surface limited to the two real domain seams plus the nav entry.
**Pros:** strongest boundary enforcement; build parallelism; clear template for the next
feature; headless reuse of the solver later.
**Cons:** most modules to manage; api/impl ceremony for a single feature is partly
forward-investment.

### Option B: Single feature module + coarse core
One `:feature:chord-smoother`, keep `:sharedLogic`/`:sharedUI` as core.
**Pros:** least churn now. **Cons:** drops the impl-hiding boundary we explicitly wanted;
coarser core fattens over time.

### Option C: Compile-time DI (kotlin-inject / Metro)
**Pros:** build-time-verified graphs. **Cons:** most upfront wiring; newer KMP toolchain.
Deferred — revisit if Koin's runtime wiring causes pain.

## Trade-off Analysis

The drivers (scale, build speed, boundaries) justify real structure now, and the
roadmap supplies the second consumer that makes the api/impl seam pay off. Koin is the
pragmatic KMP-first choice; the constructor DSL keeps the module declaration small.

## Consequences

- Easier: each new feature follows the api/impl + Clean-Arch template; modules build and
  test in isolation; the app depends on contracts, not implementations.
- Harder: more modules and a convention-plugin layer to maintain; one extra `:shared`
  umbrella for the iOS framework.
- Deferred: realizing iOS solver reuse (the api seam is iOS-capable, but impl stays
  Android+common until iOS UI is wired); the `data/` layer (arrives with "Save progression");
  generic multi-feature navigation aggregation (arrives with the second feature).

## Action Items

1. [x] Convention plugins `pianoflow.kmp.library` / `pianoflow.kmp.compose`.
2. [x] Extract `:core:model`, `:core:audio`, `:core:designsystem`; `:sharedLogic` → `:shared`.
3. [x] Koin bootstrap in `:androidApp`.
4. [x] Split chord-smoother into api/impl with Clean-Arch packages; introduce `SongsViewModel`.
5. [x] Migrate tests/goldens; delete `:sharedUI`.
