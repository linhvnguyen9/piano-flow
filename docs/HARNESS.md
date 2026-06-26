# The mobile UI harness (as built)

A map of the self-improving UI harness as it actually exists in this repo — the design rationale
lives in [`mobile-ui-harness-spec.md`](mobile-ui-harness-spec.md); this is the wiring.

The harness has three nested concerns: **prevent** broken/off-theme UI upstream, **gate + grade**
every change in a loop, and **ratchet** recurring findings into permanent gates so the soft guidance
shrinks over time. Its guiding rule: *push each check to the most deterministic substrate it can
reach* — mechanical rules live in tested code (Konsist, `Tier1Assertions`, the Python ledger tools);
only genuine taste is left to the vision evaluator.

## Architecture diagram

The flowchart is its own file (repo convention — diagrams live in `docs/diagrams/`):
**[diagrams/harness.mermaid](diagrams/harness.mermaid)** — raw Mermaid source; open it in a
Mermaid-aware viewer/IDE (GitHub won't render a standalone `.mermaid` inline).

The two **dotted feedback edges** are what make it a *ratchet*, not just a pipeline: a deposit
hardens a finding into an upstream gate (`DEP → rails`) and the now-redundant soft guidance is
deleted; and when metrics show a category still recurring after promotion (`MET → DIST`), that's the
cue to push the deposit one rung deeper down the durability ladder.

## Legend — node → where it lives

| Node | Implementation |
|---|---|
| Konsist gate | [`ArchitectureTest.kt`](../androidApp/src/test/kotlin/com/linh/pianoflow/architecture/ArchitectureTest.kt) — `core ↛ feature`, `api ↛ impl` |
| Render + Tier-1 | `*Inspection.kt` + [`Tier1Assertions.kt`](../feature/chord-smoother/impl/src/androidHostTest/kotlin/com/linh/pianoflow/feature/chordsmoother/impl/testing/Tier1Assertions.kt) |
| Inset assertion | [`AppInsetsTest.kt`](../androidApp/src/test/kotlin/com/linh/pianoflow/AppInsetsTest.kt) |
| Evaluator (fresh lane) | [`mobile-design-evaluator.md`](../.claude/agents/mobile-design-evaluator.md) + rubric [`mobile-design/SKILL.md`](../.claude/skills/mobile-design/SKILL.md) |
| `/ui-iterate` · `/ui-distill` · `/ui-metrics` | [`.claude/commands/`](../.claude/commands/) |
| Aggregate / distill / metrics | [`harness/bin/*.py`](../harness/bin/) (each with a `test_*.py` self-test) |
| Ledger | [`harness/ledger/findings.jsonl`](../harness/ledger/) (+ `screen-aliases.json`; `README.md` documents the schema) |

## Not yet wired

- **Design / target loop (§4, §6):** the loop already checks for `harness/targets/<screen>.png` and
  switches the evaluator to *fidelity-vs-target* mode, but no targets exist and the rubric has no
  Fidelity-mode section yet — so it runs in absolute-rubric mode in practice.
- **CI (§3):** nothing runs these gates on a PR yet.
