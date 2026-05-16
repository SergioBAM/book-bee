# OW-01 — Shelf Manual Add With ISBN Validation

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement a complete manual Shelf add slice that lets the user create an owned book record from the Shelf flow with the MVP defaults and validation rules settled in the PRD.

This slice should deliver an end-to-end manual add experience where:

- `title` is required
- `author` is optional free text
- `ISBN` is optional but must be fully validated if provided
- new owned records default to `quantity = 1`
- new owned records default to `readStatus = UNREAD`
- manual books remain first-class even without ISBN
- saving returns the user to Shelf and the new item appears in the owned collection

It should also introduce the first round of lightweight empty-state or explanatory copy needed to make the Shelf meaning clear.

## Acceptance criteria

- [ ] A user can manually add a Shelf book with required title, optional author, optional notes, and an optional ISBN.
- [ ] If an ISBN is provided, save is blocked unless the normalized value is a valid ISBN-10 or ISBN-13.
- [ ] Saving a valid manual book creates an active owned record with `quantity = 1` and `readStatus = UNREAD` unless the user explicitly sets a different read status in the form.
- [ ] Manual books without ISBN persist successfully and appear in Shelf as normal owned records.
- [ ] The slice is covered by tests for form validation and persisted default ownership values.

## Blocked by

None - can start immediately

