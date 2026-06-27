# ADR-0007: Tier-1 deterministic structural assertions as a host-test gate ("broken before tasteless")

**Status:** Accepted
**Date:** 2026-06-27
**Deciders:** PianoFlow maintainers

## Context

The self-improving UI harness ([`../mobile-ui-harness-spec.md`](../mobile-ui-harness-spec.md)) grades rendered
screens two ways. Some defects a machine can prove *exactly*: a 0-size content node, a 20dp tap
target, content past the screen edge, truncated must-not-truncate text, an app bar under the status
bar. Others are matters of taste only a vision pass can judge. Letting an LLM evaluator (ADR-0008)
re-derive the mechanical ones is slow, nondeterministic, and wastes a vision pass on screens that are
simply *broken*. Robolectric already renders every inspection screen on the JVM (ADR-0001, ADR-0002),
and the Compose semantics tree is available in-test before the screenshot is taken.

## Decision

Implement **Tier-1 assertions** — deterministic checks over the Compose semantics tree — as a
Roborazzi host test that is **co-located with, and gates, the screen render**. `Tier1Assertions.assertAll(rule, label)`
(`feature/.../impl/testing/Tier1Assertions.kt`) walks the unmerged tree and fails on: zero-size nodes
carrying text/contentDescription, touch targets under 48dp (32dp for M3 `Button`-role nodes whose tap
area is padded by `minimumInteractiveComponentSize`), horizontal out-of-bounds, and `noTruncate`-tagged
`Text` that reported visual overflow. Inset safety gets a dedicated `AppInsetsTest` because it needs
non-zero `WindowInsets` injected at the app scaffold, which the per-screen inspections don't set up.

Two principles are part of this decision:

1. **Broken before tasteless.** A Tier-1 failure stops the loop; the vision evaluator is not spent
   until structure is clean. `captureRoboImage` runs *before* the assert, so the PNG always lands for
   post-mortem even on failure, and the violation is written to a `_findings.jsonl` sidecar (ADR-0009)
   before the assertion throws.
2. **Selective, not universal.** Tier-1 runs on **screen-level inspection renders** (the stress
   matrix) and the insets test — **not** on isolated-component regression goldens or the Showkase
   catalog (ADR-0006). There it is false-positive noise (e.g. `PianoKeyboard`'s per-key `clickable`
   cells are intentionally sub-48dp) and their job is pixel-diff, not structural grading.

## Options Considered

### Option A: Deterministic Tier-1 assertions co-located with screen renders (chosen)
Machine-checkable invariants run in-test, before the screenshot, gating the vision pass.
**Pros:** exact, fast, zero-flake, precise messages an agent can act on without vision; reuses the
existing Robolectric render; the cheapest, most upstream catch (prevention over detection).
**Cons:** another assertion surface to maintain; only covers what semantics expose (not real taste).

### Option B: Let the vision evaluator judge everything (no deterministic tier)
**Pros:** one grading mechanism. **Cons:** nondeterministic on mechanical facts; burns an LLM pass to
"discover" a 0-size node; cannot be a hard CI gate. Rejected — mechanical truths deserve a mechanical check.

### Option C: Structural checks as a separate static linter
**Pros:** decoupled from tests. **Cons:** layout sizing/overflow/insets are *render-time* facts a static
analyzer can't see — they only exist once the tree is measured. Rejected for these checks (Konsist
covers the *static* boundary rules instead — ADR-0011).

### Sub-decision: run Tier-1 on every Robolectric render test
Rejected. On component goldens and the catalog it floods false positives (small piano keys, components
rendered in arbitrary non-screen `Surface`s) and couples pixel-diff tests to the structural gate.

## Trade-off Analysis

Tier-1 deliberately covers only the mechanizable subset of the `mobile-design` rubric; the rest is the
evaluator's job (ADR-0008). The split — deterministic gate first, vision second — is the harness's
"broken before tasteless" principle made executable: it keeps the expensive, nondeterministic pass for
genuine taste and makes structural blockers a hard, reproducible failure. Co-locating with the render
(rather than a separate pass) means one Gradle task both produces the PNG and gates it.

## Consequences

- Easier: structural blockers fail fast with an element-level message; the evaluator never wastes a
  pass on a broken screen; the same task yields the PNG for review.
- Harder: the assertion is invoked by a manual call inside each inspection class's capture helper —
  nothing yet *enforces* that a new `*Inspection` test wires it in (a silent-coverage risk; candidate
  for a shared `captureAndGate` helper or a Konsist meta-rule later).
- Revisit if: false positives appear on a legitimate screen (tune thresholds/roles), or insets/overflow
  recur enough to warrant promotion of more categories into Tier-1 (ADR-0010 is the mechanism).

## Action Items

1. [x] `Tier1Assertions.assertAll` — zero-size, touch-target, out-of-bounds, `noTruncate` overflow.
2. [x] Wire it into `SongsScreenInspection` / `ChordPickerInspection` capture helpers (after capture).
3. [x] Dedicated `AppInsetsTest` for status/nav-bar inset safety at the app scaffold.
4. [x] Emit a `_findings.jsonl` sidecar before throwing (feeds the ledger, ADR-0009).
5. [ ] Optional: extract a shared `captureAndGate(...)` helper + Konsist guard so no inspection render can skip the gate.
