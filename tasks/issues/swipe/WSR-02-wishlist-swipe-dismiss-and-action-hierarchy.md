# WSR-02 — Wishlist Swipe Dismiss And Action Hierarchy

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-wishlist-swipe-remove.md](/home/serge/code/personal/book-bee/docs/prd-wishlist-swipe-remove.md)

## What to build

Replace the inline wishlist `Remove` button with a left-only swipe-dismiss interaction so rows stay calmer and the primary handoff into Shelf keeps visual priority.

This slice should deliver:

- wishlist browse rows with exactly two visible actions: `Edit` and `Add to Shelf`
- `Add to Shelf` remaining visually primary and `Edit` remaining visually secondary
- left-only swipe removal rather than a reveal-then-tap action
- a destructive background cue while the row is dragged toward dismissal
- true dismiss behavior once the threshold is crossed, routed through the shared removal-and-undo contract
- support for removing rows that also show `On Shelf`
- gesture precedence that favors row dismissal when a horizontal drag starts on a wishlist row
- preserved section paging outside the row interaction area

## Acceptance criteria

- [ ] Wishlist browse rows no longer show a visible `Remove` button and continue to show visible `Edit` and `Add to Shelf` actions in the intended hierarchy.
- [ ] Swiping a wishlist row left triggers a true dismiss flow with a destructive visual cue and does not stop in a revealed-action state.
- [ ] Swipe removal works for normal wishlist rows and rows that also show `On Shelf`.
- [ ] When a horizontal drag starts on a wishlist row, the row interaction takes precedence over section paging.
- [ ] Section paging remains available from non-row horizontal drags within the Wishlist section.

## Blocked by

- [WSR-01-wishlist-removal-feedback-and-single-item-undo.md](/home/serge/code/personal/book-bee/tasks/issues/swipe/WSR-01-wishlist-removal-feedback-and-single-item-undo.md)
