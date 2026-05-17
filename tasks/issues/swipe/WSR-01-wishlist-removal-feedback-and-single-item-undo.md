# WSR-01 — Wishlist Removal Feedback And Single-Item Undo

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-wishlist-swipe-remove.md](/home/serge/code/personal/book-bee/docs/prd-wishlist-swipe-remove.md)

## What to build

Implement the recoverable wishlist removal contract so a successful remove request becomes immediate, generic, and undoable without widening the scope beyond Wishlist.

This slice should deliver:

- a shared remove flow for wishlist rows that can be called from any input mode
- immediate removal from the visible list after a successful remove request
- transient snackbar feedback with a generic `Undo` action
- exact restoration of the same wishlist item identity when `Undo` is triggered
- restoration to the same recency-sorted position by preserving the original wishlist item timestamps
- single-item undo tracking that always prefers the most recently removed wishlist item
- inline failure feedback when deletion fails, while leaving the row visible
- safe removal of wishlist rows that also show `On Shelf`, without touching Shelf ownership state

## Acceptance criteria

- [ ] A successful wishlist remove request removes the row immediately and surfaces a generic snackbar with `Undo`.
- [ ] Triggering `Undo` restores the exact same wishlist item in the same recency-sorted position it occupied before removal.
- [ ] Only the most recently removed wishlist item is undoable, and a newer removal replaces any older pending undo state.
- [ ] Failed removals keep the item visible and show inline failure feedback instead of success snackbar feedback.
- [ ] Removing a wishlist row that also shows `On Shelf` clears only wishlist intent and does not change the shared book record's Shelf ownership state.

## Notes

- No current blocker. [OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md](/home/serge/code/personal/book-bee/tasks/issues/OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md) has already been implemented.
