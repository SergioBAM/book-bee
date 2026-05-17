# OW-05 — Active Global Search And Separate History Search

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement the MVP search slice with active global search across Shelf and Wishlist plus a separate dedicated History search.

This slice should deliver:

- active-only global search across current ownership and wishlist data
- one consolidated result row per shared book record
- badges for `On Shelf` and `Wishlist`
- Shelf-first drill-down priority with linked wishlist context surfaced in detail
- ranking that prioritizes exact ISBN and prefers Shelf over Wishlist on ties
- inclusion of active context notes in search matching
- complete exclusion of archived records and notes from active search
- a dedicated search flow inside History

## Acceptance criteria

- [ ] Global search returns only active Shelf and Wishlist results, never archived records.
- [ ] Results are consolidated per shared book record with the correct active-context badges and Shelf-first navigation behavior.
- [ ] Search ranking prefers exact ISBN matches first and Shelf over Wishlist when relevance is otherwise equal.
- [ ] History exposes its own search path and default ordering independent of active global search.
- [ ] Tests cover active-only inclusion, consolidation, badge calculation, ranking, note-based matches, and archived exclusion.

## Blocked by

- [OW-04-history-lifecycle-archive-restore-hard-delete.md](/home/serge/code/personal/book-bee/tasks/OW-04-history-lifecycle-archive-restore-hard-delete.md)

