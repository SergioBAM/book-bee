# OW-06 — Manual ISBN Scan Result Flow

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement the first complete Scan slice using manual ISBN entry as the tracer bullet for the full scan-result pipeline.

This slice should deliver:

- Scan-section manual ISBN entry
- permission-denied fallback state that keeps the user inside Scan
- immediate local ownership evaluation from the normalized ISBN
- `Owned` result for exact active ownership
- `NotOwned` result with secondary `Previously owned` context for exact archived matches
- wishlist context surfaced separately from ownership state
- no persistence when the user cancels the result

This slice should not depend on camera integration yet. The goal is to prove the result pipeline and state model end to end.

## Acceptance criteria

- [ ] A user can enter a valid ISBN in the Scan section and receive an immediate result from local data.
- [ ] Scan results distinguish active exact ownership, exact archived history, and wishlist-only context according to the PRD.
- [ ] A camera-permission-denied or unavailable state still offers manual ISBN entry in place.
- [ ] Canceling the result leaves no scan history or saved record.
- [ ] Tests cover manual ISBN parity with scan-state rules and no-persistence-on-cancel behavior.

## Blocked by
- [OW-04-history-lifecycle-archive-restore-hard-delete.md](/home/serge/code/personal/book-bee/tasks/OW-04-history-lifecycle-archive-restore-hard-delete.md)

