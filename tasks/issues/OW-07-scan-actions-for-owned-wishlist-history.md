# OW-07 — Scan Actions For Owned Wishlist And History Outcomes

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Expand the scan result flow so the user can complete the key ownership actions directly from result states without leaving Scan.

This slice should deliver:

- `Add to shelf` immediate save for supported `NotOwned` outcomes
- `Add to wishlist` immediate save for supported `NotOwned` outcomes
- `Add to wishlist anyway` confirm-then-save behavior for exact owned results
- `Add another copy` immediate quantity increment with undo on exact owned results
- `Restore from history` direct action for exact archived matches
- exact archived default-to-restore behavior with `Add as new owned copy` available as an explicit alternative
- wishlist removal and notification rules when ownership is created or restored from scan

## Acceptance criteria

- [ ] Scan results expose the correct primary and secondary actions for exact owned, not-owned, wishlist-context, and exact archived outcomes.
- [ ] `Add to shelf`, `Add to wishlist`, and `Add another copy` behave as immediate user-initiated actions with the expected notifications and undo where defined.
- [ ] `Add to wishlist anyway` requires confirmation before saving when exact active ownership already exists.
- [ ] Exact archived scan matches default toward restore, while still allowing the user to add a new owned copy explicitly.
- [ ] Tests cover the action matrix across owned, not-owned, wishlist-context, and exact archived result states.

## Blocked by

- [OW-06-manual-isbn-scan-result-flow.md](/home/serge/code/personal/book-bee/tasks/OW-06-manual-isbn-scan-result-flow.md)

