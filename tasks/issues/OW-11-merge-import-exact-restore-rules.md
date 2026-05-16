# OW-11 — Merge Import With Exact Restore-And-Merge Rules

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement the merge-import slice so Book Bee can reconcile imported data conservatively without violating the ownership rules established elsewhere in the MVP.

This slice should deliver:

- merge mode based on exact identity first and conservative fallback behavior second
- automatic exact ISBN merge when clearly safe
- restore-and-merge when imported active ownership exactly matches a local archived ownership
- discard of imported wishlist items that exactly match already active local ownership
- fuzzy duplicate preservation as separate records with an import summary rather than automatic merge
- result reporting that surfaces what was merged, restored, discarded, or left separate

## Acceptance criteria

- [ ] Merge import combines exact ISBN duplicates conservatively instead of creating unnecessary active duplicates.
- [ ] Imported active ownership that exactly matches a local archived record reactivates that ownership through restore-and-merge behavior.
- [ ] Imported wishlist items that exactly match active local ownership are discarded by default during merge.
- [ ] Fuzzy duplicate candidates remain separate and are surfaced in the import summary instead of being merged automatically.
- [ ] Tests cover exact duplicate merge policy, archived restore-and-merge behavior, wishlist discard rules, and fuzzy duplicate summary behavior.

## Blocked by

- [OW-10-backup-export-and-replace-import.md](/home/serge/code/personal/book-bee/tasks/OW-10-backup-export-and-replace-import.md)

