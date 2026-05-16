# OW-09 — Shared Book Edit Semantics Across Shelf Wishlist And History

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement the shared-edit slice so Book Bee handles shared `Book` metadata consistently across Shelf, Wishlist, and archived records while preserving context-specific fields.

This slice should deliver:

- shared book-level edits for title, author, subtitle, and identifiers
- preserved separation for Shelf notes, Wishlist notes, quantity, and read status
- explanatory UI copy that makes the shared-versus-separate model clear
- exact ISBN conflict checks on archived edits as well as active edits
- support for post-hoc ISBN addition or change on existing records with the same duplicate-prevention rules used elsewhere

## Acceptance criteria

- [ ] Editing shared book metadata from one context updates the underlying book record for all contexts that reference it.
- [ ] Context-specific fields remain independent and are not overwritten by shared book edits.
- [ ] Edit screens explain that book metadata is shared while notes and status remain context-specific.
- [ ] Archived-record edits run exact ISBN conflict checks before saving.
- [ ] Tests cover shared metadata propagation, separate context preservation, and archived edit conflict behavior.

## Blocked by

- [OW-02-exact-isbn-identity-and-duplicate-prevention.md](/home/serge/code/personal/book-bee/tasks/OW-02-exact-isbn-identity-and-duplicate-prevention.md)
- [OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md](/home/serge/code/personal/book-bee/tasks/OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md)
- [OW-04-history-lifecycle-archive-restore-hard-delete.md](/home/serge/code/personal/book-bee/tasks/OW-04-history-lifecycle-archive-restore-hard-delete.md)

