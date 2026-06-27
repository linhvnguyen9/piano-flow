# ADR-0010: Self-improvement ratchet — durability ladder + promote-and-delete

**Status:** Accepted
**Date:** 2026-06-27
**Deciders:** PianoFlow maintainers

## Context

Soft guidance (a rubric line, a `CLAUDE.md` rule) is cheap to add and easy to ignore — and it
accumulates. A harness that only *captures* findings (ADR-0009) without acting on them grows an
ever-longer list of advice an agent re-violates. The harness spec
([`../mobile-ui-harness-spec.md`](../mobile-ui-harness-spec.md) §7) calls the act-half "the heart of
the request": recurring findings must be promoted to the most permanent substrate that can catch them,
and the now-redundant soft version deleted — so the body of soft guidance *shrinks* as the harness
matures. We also need to know whether any of this is actually working.

## Decision

Add the **ratchet** that consumes the committed ledger and hardens recurring findings, plus metrics
that show whether it compounds.

- **`harness/bin/distill_ledger.py`** (run via `/ui-distill`) clusters **open** findings (not
  `resolved`, no `deposit`) by `category`, flags every category at/over a **threshold** (default 3,
  i.e. "a pattern, not a one-off") and maps it to the most permanent substrate on the **durability
  ladder** — a new Tier-1 assertion (ADR-0007), a Konsist boundary rule (ADR-0011), a `core:designsystem`
  component, or a token — writing proposals to `harness/ledger/promotions.md`.
- **Human approval is required** to implement a promotion (`/ui-distill` carries the
  approve → implement → **delete the now-redundant soft guidance** steps).
- **`--deposit <category> --label <...>`** is the *one sanctioned in-place mutation* of the otherwise
  append-only ledger (capture appends; distill closes out): it stamps `resolved=true` + `deposit=<label>`
  on a category's open findings so they stop surfacing as candidates and start feeding the
  recurrence-after-deposit signal.
- **`harness/bin/metrics_ledger.py`** (via `/ui-metrics`) renders three numbers to `metrics.md`:
  iterations-to-pass per screen (↓), first-pass yield (↑), and **recurrence after deposit** (→ 0 — if a
  promoted category keeps recurring, the promotion didn't stick and should ratchet down a tier).

`promotions.md` and `metrics.md` are regenerated each run → gitignored; the durable record is the
`deposit` label on ledger rows plus the committed gate the promotion produced.

## Options Considered

### Option A: Threshold-clustered distillation onto a durability ladder, with promote-and-delete + metrics (chosen)
**Pros:** turns ephemeral findings into permanent, structural prevention; soft guidance shrinks instead
of bloating; recurrence-after-deposit objectively flags promotions that didn't stick; threshold avoids
hardening one-off noise. **Cons:** needs human judgment per promotion; another set of scripts to own;
metrics are only as honest as the ledger's recurrence data.

### Option B: Static, accumulating guidance (rubric/CLAUDE.md lines, never deleted)
**Pros:** zero machinery. **Cons:** unbounded guidance the agent re-violates; no path from "noticed" to
"impossible"; no signal on what's working. Rejected — this is the failure mode the ratchet exists to fix.

### Option C: Auto-promote without human approval
**Pros:** fully hands-off. **Cons:** a wrong promotion bakes a bad gate into the build; structural
changes (lint rules, components, API) warrant a human. Rejected — approval gate kept for Tier-2/3 deposits.

## Trade-off Analysis

The ladder encodes "prevention over detection": push each recurring learning as far down as it can go
(evaluator judgment → deterministic gate → structurally impossible), then delete the weaker upstream
copy. The threshold (default 3) trades a little latency-to-promotion for not ossifying noise into gates.
Keeping `deposit` as the *only* in-place ledger write preserves the append-only capture contract
(ADR-0009) while still letting distill close findings out and metrics measure stick-rate.

## Consequences

- Easier: recurring defects become permanent gates; the rubric/`CLAUDE.md` stay a small staging area;
  there's an objective read on whether the harness is compounding.
- Harder: someone must run `/ui-distill` periodically and judge proposals; metrics need enough ledger
  history to be meaningful; a deposit that keeps recurring signals real work (ratchet it down a tier).
- Revisit if: a category resists every promotion tier (rethink the check), or threshold/cadence needs
  tuning as finding volume grows.

## Action Items

1. [x] `distill_ledger.py` — cluster open findings by `category`, threshold, map to durability ladder → `promotions.md`.
2. [x] `--deposit` mode — stamp `resolved` + `deposit` (the sole in-place ledger mutation).
3. [x] `metrics_ledger.py` — iterations-to-pass, first-pass yield, recurrence-after-deposit → `metrics.md`.
4. [x] `/ui-distill` (approve → implement → delete soft guidance) and `/ui-metrics` commands.
5. [x] `promotions.md` / `metrics.md` gitignored (regenerated); unit tests `test_distill_ledger.py`, `test_metrics_ledger.py`.
