# OW-03 — Wishlist MVP With Notes And Shelf Handoff

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement the MVP Wishlist slice as a lightweight but complete user flow with notes, no priority, and explicit interaction rules with Shelf ownership.

This slice should deliver:

- wishlist create, view, edit, and delete behavior
- `title` required, `author` optional, `ISBN` preserved when available
- wishlist notes support
- no wishlist priority in the data contract or UI
- explicit confirmation when the user adds an exact same-edition owned book to wishlist anyway
- automatic wishlist removal with user notification when the same book becomes actively owned
- editable prefill of wishlist notes when a wishlist item moves into an ownership flow

## Acceptance criteria

- [ ] A user can add, edit, and remove wishlist items with notes and without any priority UI or priority persistence requirement.
- [ ] Wishlist items can be created from minimal valid data consistent with the PRD, while preserving ISBN when available.
- [ ] If a wishlist item becomes actively owned through a supported flow, the wishlist entry is removed automatically and the user is notified.
- [ ] Same-edition shelf-plus-wishlist overlap requires explicit confirmation rather than occurring silently.
- [ ] Tests cover wishlist persistence, same-edition confirmation behavior, and automatic wishlist removal on ownership creation.

## Blocked by

- [OW-01-shelf-manual-add-isbn-validation.md](/home/serge/code/personal/book-bee/tasks/OW-01-shelf-manual-add-isbn-validation.md)

