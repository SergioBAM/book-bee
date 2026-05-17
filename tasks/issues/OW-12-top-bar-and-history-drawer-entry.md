# OW-12 — Top Bar And History Drawer Entry

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-search-tab-and-history-drawer.md](/home/serge/code/personal/book-bee/docs/prd-search-tab-and-history-drawer.md)

## What to build

Add the app-shell chrome needed for secondary utility navigation without changing the primary bottom-tab information architecture yet.

This slice should deliver:

- a persistent compact top bar above app content
- a hamburger button on the left side of the top bar
- an active destination title in the top bar
- a sparse navigation drawer opened from the hamburger button
- `Book Bee` shown at the top of the drawer
- `History` shown as the only drawer destination
- no disabled or placeholder import/export drawer entries
- drawer opening only from the hamburger tap, with edge-swipe drawer gestures disabled
- drawer selection state that marks `History` only when History is active

History may still remain available through the existing bottom navigation in this slice. The goal is to establish the drawer entry path before removing History from the primary pager in a later slice.

Covers user stories: 5-16, 38, 40, 44.

## Acceptance criteria

- [ ] Every current app section renders under a persistent compact top bar.
- [ ] The top bar contains a hamburger button and the visible title for the active destination.
- [ ] Tapping the hamburger button opens a drawer containing `Book Bee` and `History`.
- [ ] The drawer does not show import/export placeholders or unrelated utility entries.
- [ ] Edge-swipe gestures do not open the drawer.
- [ ] `History` is visually selected in the drawer only when the app is showing History.
- [ ] Tests or focused UI verification cover the top bar, drawer contents, hamburger open behavior, and absence of import/export placeholders.

## Blocked by

None - can start immediately
