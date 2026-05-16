# OW-10 — Backup Export And Replace Import

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement the first backup-and-restore slice covering committed-data export and destructive replace import.

This slice should deliver:

- JSON export of durable library data only
- preservation of internal IDs in export and restore
- exclusion of metadata cache and unsaved transient form state
- schema-version enforcement that blocks unsupported or newer backup versions
- replace import with strong explicit confirmation naming Shelf, Wishlist, and History
- post-import landing on Shelf with a concise result summary

## Acceptance criteria

- [ ] Export includes the durable library model needed to reconstruct Shelf, Wishlist, History, identifiers, and shared books while preserving internal IDs.
- [ ] Export excludes metadata cache and does not include unsaved or transient UI state.
- [ ] Replace import blocks unsupported or newer schema versions instead of attempting a partial restore.
- [ ] Replace import requires a strong destructive confirmation that explicitly names Shelf, Wishlist, and History.
- [ ] Tests cover export shape, ID preservation, schema blocking, replace behavior, and Shelf landing summary expectations.

## Blocked by

- [OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md](/home/serge/code/personal/book-bee/tasks/OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md)
- [OW-04-history-lifecycle-archive-restore-hard-delete.md](/home/serge/code/personal/book-bee/tasks/OW-04-history-lifecycle-archive-restore-hard-delete.md)
- [OW-09-shared-book-edit-semantics.md](/home/serge/code/personal/book-bee/tasks/OW-09-shared-book-edit-semantics.md)

