# WSR-03 — Wishlist Empty-State Teaching And Accessibility Remove

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-wishlist-swipe-remove.md](/home/serge/code/personal/book-bee/docs/prd-wishlist-swipe-remove.md)

## What to build

Finish the swipe-remove change by making the hidden destructive action discoverable and accessible without adding new UI chrome or divergent behavior paths.

This slice should deliver:

- Wishlist empty-state explanatory copy that teaches both the section purpose and the left-swipe remove gesture
- concise discoverability copy folded into the existing explanatory card rather than a new hint surface
- a non-visual row remove action for assistive technology users
- accessibility removal behavior that matches swipe removal exactly:
  - immediate remove
  - generic snackbar
  - single-item undo
- selective tests around hidden-gesture teaching and accessibility action availability

## Acceptance criteria

- [ ] The Wishlist empty-state explanatory card teaches that saved items can be removed with a left swipe while still explaining the section's purpose concisely.
- [ ] Wishlist rows expose a non-visual remove action for assistive technology users.
- [ ] The accessibility remove action follows the same behavior contract as swipe removal, including generic snackbar feedback and single-item undo semantics.
- [ ] Tests cover the empty-state teaching copy and the presence of the accessibility removal path.
- [ ] No extra hint surface or separate removal rules are introduced outside the existing Wishlist browse experience.

## Blocked by

- [WSR-02-wishlist-swipe-dismiss-and-action-hierarchy.md](/home/serge/code/personal/book-bee/tasks/issues/swipe/WSR-02-wishlist-swipe-dismiss-and-action-hierarchy.md)
