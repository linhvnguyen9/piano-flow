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

- [ ] **feature** — Save chord progression (persist user-built progressions; load/edit/delete). Lands in `feature/chord-smoother/impl/.../impl/data/`.
- [ ] **feature** — Audio playback wiring (`core/audio/.../audio/`, `core/audio/src/iosMain/.../audio/`)

## Backlog (P2)

- [ ] **feature** — Song library UI polish (`feature/chord-smoother/impl/.../impl/presentation/SongsScreen.kt`)
- [ ] **refactor** — Review `VoiceLeadingSolver.kt` voicing selection heuristics
- [ ] **test** — Expand `SolverTest.kt` coverage for edge cases (`feature/chord-smoother/impl/src/commonTest/...`)

## Nice to Have (P3)

- [ ] _(add ideas here)_

## Bugs

- [ ] _(none reported)_

---

## Done

- [x] Initial project scaffold
- [x] **refactor** — Multi-module + Clean Architecture restructure (`:core:*`, `:feature:api/impl:chord-smoother`, `:shared` umbrella, Koin DI, convention plugins). See ADR-0005.
