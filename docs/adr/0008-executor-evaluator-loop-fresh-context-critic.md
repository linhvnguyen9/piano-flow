# ADR-0008: Executor–evaluator UI loop with a fresh-context evaluator subagent

**Status:** Accepted
**Date:** 2026-06-27
**Deciders:** PianoFlow maintainers

## Context

A coding agent that both writes a `@Composable` and grades how it looks is reviewing its own work: it
anchors on the intent it just had ("I meant for it to look like this") instead of judging the pixels
("this is what actually rendered"). The harness ([`../mobile-ui-harness-spec.md`](../mobile-ui-harness-spec.md))
needs UI graded against an explicit rubric, grounded in real renders, in a way that resists that
correlated-blind-spot failure. We already render the stress matrix to PNGs (ADR-0001/0002) and have a
deterministic structural gate (ADR-0007); what remains is the taste judgment and the loop that drives
edit → render → grade → fix to convergence.

## Decision

Run UI implementation as a **two-lane loop** with the lanes kept in separate contexts:

- **Executor lane** = the main coding context. Edits the `@Composable`, runs the cheap gates
  (Konsist boundaries ADR-0011, then Tier-1 ADR-0007), and renders the matrix.
- **Evaluator lane** = the `mobile-design-evaluator` subagent (`.claude/agents/mobile-design-evaluator.md`),
  dispatched in a **fresh context** with only the rendered PNGs + the committed `mobile-design` rubric.
  It reads every image, returns a `pass`/`fail` verdict with element-level fixes, and writes
  `_verdict.md` + an `_eval_findings.jsonl` ledger sidecar (ADR-0009). It **never edits source** — it
  judges artifacts, not intent.

The `/ui-iterate <Screen>` command (`.claude/commands/ui-iterate.md`) orchestrates one screen end to
end: resolve target → edit → boundary gate → render+Tier-1 → dispatch evaluator → aggregate ledger →
decide. **Hard cap of 4 iterations**, then escalate to the human. Stop condition: Tier-1 clean AND
(rubric pass OR fidelity diff under tolerance).

Heavy work (Gradle render, evaluator dispatch) lives **in the command, not in edit hooks**: the gates
are Gradle tests, far too slow to run per keystroke. `/ui-iterate` is run after a batch of edits.

## Options Considered

### Option A: Separate evaluator subagent in a fresh context, orchestrated by `/ui-iterate` (chosen)
**Pros:** breaks the correlated-critic failure (the grader never saw the executor's reasoning); judges
rendered artifacts against an explicit rubric; bounded, resumable loop; the rubric is a committed file
so grading is reproducible. **Cons:** an extra subagent dispatch per iteration (latency/token cost);
the two lanes must agree on file conventions (PNG dir, sidecar names).

### Option B: Implementer self-reviews its own renders
**Pros:** no extra dispatch. **Cons:** self-review — the exact anchoring failure this loop exists to
prevent. Rejected.

### Option C: Evaluator shares the executor's context (same conversation)
**Pros:** no re-establishing context. **Cons:** correlated blind spots; the verdict is contaminated by
the executor's stated intent. Rejected.

### Option D: A PostToolUse hook that grades after every edit
**Pros:** automatic. **Cons:** rendering + a vision pass per edit is prohibitively slow and noisy;
hooks can't hold the multi-step loop. Rejected — orchestration belongs in the command.

## Trade-off Analysis

The value is *decorrelation*: a fresh-context grader reading only artifacts catches what an
intent-anchored self-review misses. We pay one subagent dispatch per iteration and a small
coordination contract (shared PNG dir + sidecar filenames) for it. The 4-iteration cap with human
escalation bounds non-convergence; keeping the deterministic gates *before* the vision pass (ADR-0007)
keeps most iterations from needing the expensive lane at all.

## Consequences

- Easier: UI work is graded, not just rendered; verdicts are element-level and reproducible; the loop
  is one command with a clear stop/escalate rule.
- Harder: a coordination contract spans two lanes (the canonical-screen-id reconciliation in ADR-0009
  exists because of it); evaluator output quality depends on the committed rubric staying current.
- Revisit if: convergence routinely needs >4 iterations (tighten the rubric or the matrix), or a
  cheaper local vision check could pre-filter before the subagent dispatch.

## Action Items

1. [x] `mobile-design-evaluator` subagent — fresh context, reads PNGs + rubric, writes verdict + sidecar, never edits.
2. [x] `/ui-iterate` command — gate → render → evaluate → aggregate, 4-iteration cap + escalation.
3. [x] Keep the rubric a committed project file (`.claude/skills/mobile-design/SKILL.md`) so grading is reproducible.
4. [x] No per-edit grading hook; heavy work stays in the command.
5. [x] Smoke-tested end to end on `SongsScreen` (gate→render→evaluate→aggregate).
