# OW-14 — Search Tab Replaces Global Search

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-search-tab-and-history-drawer.md](/home/serge/code/personal/book-bee/docs/prd-search-tab-and-history-drawer.md)

## What to build

Replace the always-visible global search surface and primary History tab with a dedicated active-library Search section.

This slice should deliver:

- bottom navigation ordered as `Shelf`, `Scan`, `Wishlist`, `Search`
- the horizontal pager limited to those four primary sections
- removal of the persistent global search field from the top of every page
- a full Search page containing its own search field
- a quiet Search empty state before the user types
- active Search results rendered in the Search page body
- active Search scoped to Shelf and Wishlist only
- History records excluded from active Search
- Search result selection navigating to the matched primary section, Shelf or Wishlist
- existing History still reachable from the drawer after its bottom tab is removed

Item scroll/focus/highlight after selecting a Search result is intentionally left for a future enhancement. Page navigation is sufficient for this slice.

Covers user stories: 1-4, 21-35, 39, 41-44.

## Acceptance criteria

- [ ] The bottom navigation shows exactly `Shelf`, `Scan`, `Wishlist`, and `Search` in that order.
- [ ] `History` no longer appears in the bottom navigation or primary pager.
- [ ] The global search field no longer appears above every primary section.
- [ ] The Search tab opens a full page with a search field and an empty state before typing.
- [ ] Search results render in the Search page body after a matching query.
- [ ] Search results include active Shelf and Wishlist records only.
- [ ] Archived History records do not appear in active Search results.
- [ ] Selecting a Shelf result navigates to Shelf; selecting a Wishlist result navigates to Wishlist.
- [ ] History remains reachable from the hamburger drawer after the bottom History tab is removed.
- [ ] Tests cover the bottom-nav section list, removal of global search, Search empty/results states, result navigation, and active-only search scope.

## Blocked by

- [OW-13-history-secondary-drawer-destination.md](/home/serge/code/personal/book-bee/tasks/issues/OW-13-history-secondary-drawer-destination.md)
