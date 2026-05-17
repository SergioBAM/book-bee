# OW-02 — Exact ISBN Identity And Duplicate Prevention

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement the exact-identity slice for owned books so Book Bee can treat ISBN-10 and ISBN-13 equivalents as the same exact ownership signal and stop creating new exact duplicate active records in normal write paths.

This slice should deliver:

- ISBN checksum validation and normalization as a domain rule
- ISBN-10/ISBN-13 equivalence handling for exact match behavior
- automatic storage of the derived paired ISBN when it exists
- duplicate prevention when a user creates or edits an active owned record with an ISBN that exactly matches an existing active record
- `Add another copy` as the normal same-edition resolution path
- quantity decrement to archive behavior, including confirmation and undo-friendly immediate copy increments

This is a deep rules slice: exact identity should become reliable enough that later scan, search, and import flows can build on it.

## Acceptance criteria

- [ ] Equivalent ISBN-10 and ISBN-13 values are treated as the same exact identifier in owned-book workflows.
- [ ] When a valid ISBN can be converted to an equivalent paired form, both identifiers are stored on the same book record.
- [ ] Normal add or edit flows do not create a second active owned record with the same exact ISBN identity; instead they route into conflict handling or copy increment behavior.
- [ ] Quantity changes support immediate `Add another copy`, reliable undo, and archive confirmation when decrementing from one copy to zero.
- [ ] Domain and repository tests cover ISBN validation, equivalence, derived identifier storage, duplicate prevention, and quantity-to-archive behavior.

