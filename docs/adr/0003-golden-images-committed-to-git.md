# ADR-0003: Golden Images Checked Into Git

**Status:** Accepted
**Date:** 2026-06-07
**Deciders:** PianoFlow maintainers

## Context

Screenshot tests compare a rendered bitmap against a stored "golden" reference
PNG. Those goldens have to live somewhere the verify step can read and that
reviewers can inspect. Roborazzi defaults to writing under
`build/outputs/roborazzi/`, which is not version controlled.

## Decision

Store goldens in the repository under
`sharedUI/src/androidHostTest/screenshots/`, committed to git (Roborazzi's
`outputDir` is pointed there). Pull requests then surface image diffs directly
in the review, and a golden refresh is an explicit, reviewable change.

## Options Considered

### Option A: Commit goldens to git (chosen)
| Dimension | Assessment |
|-----------|------------|
| Complexity | Low |
| Cost | Repo size grows with PNGs |
| Review surface | PR diff shows image changes inline |

**Pros:** Zero infra; diffs visible in PR; refreshes are auditable.
**Cons:** Binary churn inflates repo history over time.

### Option B: External golden store (artifact bucket / LFS)
| Dimension | Assessment |
|-----------|------------|
| Complexity | High — extra infra + auth |
| Cost | Storage service + tooling |
| Review surface | Indirect; needs custom diff surfacing |

**Pros:** Keeps the git repo lean. **Cons:** Infra to build and maintain; diffs
no longer appear natively in PRs; overkill at current scale.

## Trade-off Analysis

At pilot scale the number and size of goldens is small, so repo-size cost is
negligible against the big win of native PR diffs and zero infrastructure. An
external store only pays off once goldens are numerous/large — a problem we do
not have yet.

## Consequences

- Easier: reviewers see exactly what changed visually, in the PR.
- Harder: long-term binary churn; large matrices would bloat history.
- Revisit if: golden count/size grows enough that repo bloat hurts — migrate to
  Git LFS or an external store at that point.

## Action Items

1. [ ] Set Roborazzi `outputDir` to `src/androidHostTest/screenshots/`.
2. [ ] Document that golden refreshes must be reviewed as part of the PR.
