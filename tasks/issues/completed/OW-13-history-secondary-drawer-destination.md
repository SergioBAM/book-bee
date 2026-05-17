# OW-13 — History Secondary Drawer Destination

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-search-tab-and-history-drawer.md](/home/serge/code/personal/book-bee/docs/prd-search-tab-and-history-drawer.md)

## What to build

Make History behave as a secondary app-shell destination opened from the drawer rather than as primary pager content.

This slice should deliver:

- drawer `History` opening a destination outside the horizontal pager
- bottom navigation remaining visible while History is active
- Android back from History returning to the previously active primary bottom-tab page
- selecting any bottom tab exiting History and navigating directly to that tab
- the top bar title showing `History` while History is active
- History keeping its own in-screen search field
- removal of redundant large body title text from History when the top bar already says `History`

The existing bottom-nav History tab may still exist until the Search replacement slice lands, but drawer-opened History should already use the secondary-destination behavior defined here.

Covers user stories: 17-20, 26, 36-37, 40, 42, 44.

## Acceptance criteria

- [ ] Selecting `History` in the drawer closes the drawer and shows History outside the primary pager.
- [ ] Bottom navigation remains visible while the History destination is active.
- [ ] Pressing Android back from drawer-opened History returns to the previously active primary section.
- [ ] Selecting `Shelf`, `Scan`, `Wishlist`, or the current primary tab from the bottom navigation exits History.
- [ ] The top bar title reads `History` while History is active.
- [ ] The History screen keeps its search field and archived-record list behavior.
- [ ] The History body does not repeat a large `History` title beneath the top bar.
- [ ] Tests cover drawer-opened History, back behavior, bottom-tab exit behavior, and History title/search presentation.

## Blocked by

- [OW-12-top-bar-and-history-drawer-entry.md](/home/serge/code/personal/book-bee/tasks/issues/OW-12-top-bar-and-history-drawer-entry.md)
