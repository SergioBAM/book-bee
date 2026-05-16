# OW-04 — History Lifecycle For Archive Restore And Hard Delete

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement the full ownership-history slice so users can archive active ownership, browse a separate History area, edit archived records, restore them explicitly, and hard-delete them only from History.

This slice should deliver:

- archive as the default remove-from-shelf behavior
- a separate History or Archived area distinct from active Shelf
- explicit restore behavior that preserves original `dateAdded`, notes, and read status
- conflict handling when restore would collide with an active exact ISBN record
- archived-record editing without forced restore
- hard delete from History with strong confirmation and linked wishlist warning
- cleanup of orphaned book aggregates when the final context is deleted

## Acceptance criteria

- [ ] Removing a Shelf item archives it instead of hard-deleting it, and the record moves into History.
- [ ] History shows archived records separately from active Shelf and supports editing without restoring.
- [ ] Restore reactivates the same ownership record, preserving contextual data and handling exact ISBN conflicts explicitly.
- [ ] Hard delete is available only in History, warns about linked wishlist removal, and removes the underlying book aggregate when no contexts remain.
- [ ] Tests cover archive transitions, restore behavior, restore conflict handling, and hard-delete cleanup semantics.

## Blocked by

- [OW-02-exact-isbn-identity-and-duplicate-prevention.md](/home/serge/code/personal/book-bee/tasks/OW-02-exact-isbn-identity-and-duplicate-prevention.md)
- [OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md](/home/serge/code/personal/book-bee/tasks/OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md)

