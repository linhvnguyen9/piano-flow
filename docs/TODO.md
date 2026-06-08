# PianoFlow TODO

Track upcoming features and bugfixes here. Move items to **Done** when shipped.

## Conventions

- **Priority:** P0 (urgent) · P1 (next up) · P2 (later) · P3 (nice-to-have)
- **Type:** `feature` · `bugfix` · `refactor` · `chore` · `docs`
- Link related code with `path/to/file.kt:line` when relevant.

---

## In Progress

- [ ] **feature** — Implement chord smoother (see `chord-smoother-SPEC.md`)

## Next Up (P1)

- [ ] **feature** — Save chord progression (persist user-built progressions; load/edit/delete)
- [ ] **feature** — Audio playback wiring (`sharedLogic/.../audio/`, `sharedLogic/.../iosMain/.../audio/`)
- [ ] **refactor** — Extract Roborazzi setup from `sharedUI/build.gradle.kts` into a `pianoflow.compose-screenshot-testing` convention plugin in `build-logic/`, reusable when more Compose modules are added

## Backlog (P2)

- [ ] **feature** — Song library UI polish (`sharedUI/.../ui/songs/SongsScreen.kt`)
- [ ] **refactor** — Review `Solver.kt` voicing selection heuristics
- [ ] **test** — Expand `SolverTest.kt` coverage for edge cases

## Nice to Have (P3)

- [ ] _(add ideas here)_

## Bugs

- [ ] _(none reported)_

---

## Done

- [x] Initial project scaffold
