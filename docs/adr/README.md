# Architecture Decision Records

Short, focused records of significant technical decisions — one decision per
file. Each ADR is immutable once Accepted; to change a decision, add a new ADR
that supersedes the old one (and set the old one's status to `Superseded by ADR-XXXX`).

**Status values:** Proposed · Accepted · Deprecated · Superseded

| ADR | Title | Status |
|-----|-------|--------|
| [0001](0001-roborazzi-for-compose-screenshot-testing.md) | Roborazzi for Compose screenshot testing | Accepted |
| [0002](0002-jvm-android-only-screenshot-scope.md) | JVM/Android-only scope for screenshot testing | Accepted |
| [0003](0003-golden-images-committed-to-git.md) | Golden images checked into git | Accepted |
| [0004](0004-robolectric-sdk-pin.md) | Pin Robolectric to SDK 35 while compileSdk is 36 | Accepted |
| [0005](0005-feature-module-architecture.md) | Feature-module architecture (api/impl + fine-grained core + Koin) | Accepted |

The fuller rationale and implementation detail behind ADRs 0001–0004 lives in
[`../superpowers/specs/2026-06-07-compose-screenshot-testing-design.md`](../superpowers/specs/2026-06-07-compose-screenshot-testing-design.md).
