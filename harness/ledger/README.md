# Feedback ledger

`findings.jsonl` is the **durable, append-only record** of every UI finding the harness
produces. It is the substrate the self-improvement ratchet reads: `/ui-distill` clusters
recurring findings into permanent gates/components, and `/ui-metrics` trends iterations-to-pass,
first-pass yield, and per-category recurrence. (Those two steps are not built yet — this file
and its aggregator are the *capture* half of the loop.)

## Who writes it

Two producers emit findings to **build-local sidecars** (gitignored, hermetic), which the
aggregator folds into this committed ledger:

| Producer | Sidecar | Catches |
|---|---|---|
| `Tier1Assertions.kt` (deterministic) | `<module>/build/outputs/roborazzi/_findings.jsonl` | structural blockers a machine can prove: zero-size, touch-target, out-of-bounds, text-overflow |
| `mobile-design-evaluator` agent (vision) | `_eval_findings.jsonl` beside the verdict it grades | everything assertions can't see: taste, hierarchy, spacing, fidelity vs. target |

## Closing the loop

After a render + evaluate pass, fold the sidecars into the ledger:

```bash
python3 harness/bin/aggregate_ledger.py            # collect from the repo
python3 harness/bin/aggregate_ledger.py --dry-run  # preview, write nothing
python3 harness/bin/aggregate_ledger.py --iteration 2   # stamp the loop iteration
```

The aggregator dedups identical findings **within a single batch** (so a test re-run without
`clean` doesn't double-count), then truncates each consumed sidecar so a run is recorded exactly
once. Dedup is intentionally *not* applied across runs — a finding that recurs in a later run is a
fresh row, because cross-run recurrence is the promotion signal the ratchet looks for.

Self-test for the aggregator: `python3 harness/bin/test_aggregate_ledger.py`.

## Row schema (one JSON object per line)

```json
{
  "ts": "2026-06-22T10:00:00Z",
  "screen": "SongsScreen",
  "config": "360_font2_0_dark",
  "loop": "impl",
  "role": "tier1 | evaluator",
  "tier": 1,
  "category": "insets | overflow | truncation | overlap | zero-size | touch-target | out-of-bounds | spacing | hierarchy | color | typography | reuse | fidelity | ...",
  "severity": "blocker | warn | nit",
  "element": "TopAppBar title",
  "finding": "App bar overlaps status bar",
  "fix": "apply Modifier.windowInsetsPadding(WindowInsets.statusBars)",
  "iteration": 1,
  "resolved": false,
  "deposit": null
}
```

`deposit` is set by the (future) distill step when a finding is promoted to a permanent gate,
lint rule, component, or token — marking it as having ratcheted down the durability ladder.
