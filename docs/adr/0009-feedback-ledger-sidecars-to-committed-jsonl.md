# ADR-0009: Feedback ledger — hermetic sidecars aggregated into a committed append-only JSONL

**Status:** Accepted
**Date:** 2026-06-27
**Deciders:** PianoFlow maintainers

## Context

The harness only *self-improves* if findings persist. Two producers emit them: the deterministic
Tier-1 assertions (ADR-0007) and the vision evaluator (ADR-0008). They run in different processes
(a Gradle host test; a subagent), potentially in parallel across modules, and on every loop iteration.
We need a durable, append-only record the ratchet (ADR-0010) and metrics can read — without making test
runs non-hermetic (a test that writes into the committed tree dirties the working copy and races under
parallel execution) and without unbounded git churn from per-run scratch writes.

## Decision

Capture in two stages — **hermetic build-local sidecars**, folded into a **committed append-only
ledger** by a deliberate aggregation step:

- **Producers write sidecars under `build/`** (gitignored, wiped on `clean`): Tier-1 →
  `<module>/build/outputs/roborazzi/_findings.jsonl`; the evaluator → `_eval_findings.jsonl` beside the
  PNGs it grades. Tests/agents stay hermetic; nothing touches the committed tree.
- **`harness/bin/aggregate_ledger.py`** collects the sidecars, appends them to the committed
  `harness/ledger/findings.jsonl` (one JSON object per finding, schema in `harness/ledger/README.md`),
  then **truncates each consumed sidecar** so a run is recorded exactly once.

Three semantic decisions:

1. **Dedup is within-batch only.** Identical findings in one aggregation collapse; the same finding
   recurring in a *later* run is a fresh row — because **cross-run recurrence is the promotion signal**
   the ratchet (ADR-0010) and metrics read.
2. **Canonical screen id.** The two lanes name a screen differently (Tier-1 emits the PNG filename slug
   `songs`; the evaluator the composable name `SongsScreen`). The aggregator canonicalizes every
   `screen` through `harness/ledger/screen-aliases.json` (keys matched case/punctuation-insensitively;
   unmapped names fall back to a lowercased, alnum-stripped form) so findings cluster per screen.
3. **`category` names the defect, not the fix** (`touch-target`, never `reuse`) — the field the ratchet
   clusters on must be stable across however the evaluator phrases a remedy.

The ledger is **committed** (it lives outside `**/build/`); aggregation is a deliberate step
(`/ui-iterate` step 5, or run by hand), never an automatic side effect of a test, which keeps churn
intentional. A Python stdlib script (not a Gradle task) owns this because the consumers — `/ui-distill`,
`/ui-metrics` — are JSONL processors, and it is unit-tested (`test_aggregate_ledger.py`).

## Options Considered

### Option A: Hermetic sidecars + deliberate aggregation into a committed JSONL (chosen)
**Pros:** tests stay hermetic and parallel-safe; churn is intentional, not per-run; durable, diff-able,
append-only history; trivial for downstream JSONL tools to consume. **Cons:** a two-step capture (sidecar
then aggregate); a canonical-id reconciliation is needed because two lanes disagree on names.

### Option B: Tests write directly to the committed `findings.jsonl`
**Pros:** one step. **Cons:** non-hermetic (every test run dirties git), racy under parallel modules,
and unbounded churn. Rejected.

### Option C: No durable ledger — keep findings in `_verdict.md` only
**Pros:** nothing to maintain. **Cons:** findings evaporate on `clean`; no cross-run recurrence, so no
ratchet and no metrics. Rejected — defeats the self-improvement goal.

### Option D: A real database (SQLite/served)
**Pros:** queryable. **Cons:** not diff-able/reviewable in git, heavyweight for a single-developer repo,
another dependency. Rejected — JSONL is enough and reviews well.

## Trade-off Analysis

The split keeps the two properties that conflict under a single mechanism: hermetic, parallel-safe
*capture* (sidecars in `build/`) and a durable, low-churn, reviewable *record* (committed JSONL written
only on an explicit aggregate). Within-batch-only dedup is the deliberate choice that preserves
recurrence as signal rather than silently collapsing it. The canonical-screen-id table is the cost of
having two independent producers; centralizing it in the aggregator (not trusting the LLM to emit a
stable id) keeps screen identity deterministic.

## Consequences

- Easier: every finding from both lanes lands in one durable, reviewable file; downstream tools read
  plain JSONL; test runs never dirty the tree.
- Harder: `screen-aliases.json` must gain an entry when a new screen or evaluator phrasing appears
  (unmapped names still won't *split* a screen, but may not *merge* synonyms); aggregation is a step
  authors must remember (it's wired into `/ui-iterate`).
- Revisit if: finding volume outgrows a flat JSONL (move to a DB), or producers converge on a shared
  screen id at emit time (drop the alias table).

## Action Items

1. [x] `aggregate_ledger.py` — collect sidecars → committed `findings.jsonl`, truncate consumed sidecars.
2. [x] Within-batch-only dedup (`DEDUP_KEY`); preserve cross-run recurrence.
3. [x] `screen-aliases.json` + `canonical_screen()` to reconcile the two lanes' screen names.
4. [x] Evaluator emits `_eval_findings.jsonl`; Tier-1 emits `_findings.jsonl`; both gitignored under `build/`.
5. [x] Schema + flow documented in `harness/ledger/README.md`; unit test `test_aggregate_ledger.py`.
