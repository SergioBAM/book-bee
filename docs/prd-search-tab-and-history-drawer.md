# PRD - Search Tab And History Drawer

Status: `ready-for-agent`
Date: `2026-05-17`
Source: Synthesized from navigation design review, current repo state, and existing Book Bee product direction.

## Problem Statement

Book Bee's current app shell keeps global search visible at the top of every primary section. This makes the UI feel cluttered and competes with the focused workflows in `Shelf`, `Scan`, `Wishlist`, and `History`.

The product is meant to feel fast, calm, readable, predictable, and utility-first. A persistent global search field weakens that direction because it consumes prime vertical space even when the user is not searching. It also blurs the distinction between active ownership workflows and secondary data management workflows.

The current bottom navigation also includes `History` as a peer section alongside the main daily-use workflows. History is important, but it is secondary: archived ownership records should remain available without making the primary navigation feel heavier than necessary. Future data-management features such as import and export will also need a place to live without becoming bottom-nav destinations.

The user needs a clearer information architecture that:

- removes persistent top search clutter
- keeps active library search easy to reach
- keeps History accessible but secondary
- creates a durable home for future utility/data-management destinations
- preserves the swipe-centric primary navigation model
- avoids gesture conflicts between the pager and drawer

## Solution

Book Bee will move active library search into its own primary bottom-tab section and move History into a hamburger drawer destination.

The primary bottom navigation will become:

- `Shelf`
- `Scan`
- `Wishlist`
- `Search`

The global top search field will be removed. Instead, a persistent compact app top bar will show:

- a hamburger button on the left
- the active destination title

The hamburger opens a sparse navigation drawer. The drawer will show `Book Bee` at the top and `History` as the only selectable item for now. The drawer will open only from the hamburger tap; edge-swipe drawer gestures are disabled so they do not compete with horizontal pager swipes.

`Search` becomes a full page in the primary pager. It searches only active `Shelf` and `Wishlist` records. Archived History records remain searchable only inside the History screen.

`History` becomes a secondary destination outside the horizontal pager. It is opened from the drawer, keeps bottom navigation visible, and supports Android back returning to the previously active bottom-tab page. Selecting any bottom tab exits History and navigates directly to that tab.

This change preserves the MVP's core hierarchy:

- primary daily workflows live in the bottom pager
- secondary ownership history and future data-management utilities live in the drawer
- search has an explicit home instead of occupying every screen

## User Stories

1. As a Book Bee user, I want the global search field removed from the top of every page, so that the app feels less cluttered.
2. As a Book Bee user, I want search available as a dedicated bottom-tab section, so that I can still find active books quickly.
3. As a Book Bee user, I want the bottom navigation to show `Shelf`, `Scan`, `Wishlist`, and `Search`, so that the main app workflows are clear.
4. As a Book Bee user, I want `History` removed from the bottom navigation, so that archived ownership does not compete with daily shopping workflows.
5. As a Book Bee user, I want a hamburger button in the top-left corner, so that I can access secondary destinations.
6. As a Book Bee user, I want the top bar to show the current destination title, so that I always know where I am.
7. As a Book Bee user, I want the top bar to stay visible while page content scrolls, so that navigation remains reachable.
8. As a Book Bee user, I want the drawer to open only when I tap the hamburger button, so that horizontal app gestures remain predictable.
9. As a Book Bee user, I do not want the drawer to open from an edge swipe, so that it does not interfere with the section pager.
10. As a Book Bee user, I want the drawer to show `Book Bee` at the top, so that the menu has clear app context.
11. As a Book Bee user, I want the drawer to be sparse and utility-focused, so that it does not feel like a second main navigation system.
12. As a Book Bee user, I want `History` available in the drawer, so that archived ownership records remain easy to reach.
13. As a Book Bee user, I want future utility features such as import and export to have a logical drawer home later, so that the bottom nav stays focused.
14. As a Book Bee user, I do not want disabled import/export placeholders shown before those features exist, so that the app does not feel unfinished.
15. As a Book Bee user, I want the drawer to mark `History` selected only while I am viewing History, so that selection state remains accurate.
16. As a Book Bee user, I want no drawer item selected while I am on `Shelf`, `Scan`, `Wishlist`, or `Search`, so that the drawer does not imply a false utility destination.
17. As a Book Bee user, I want History to open as its own destination, so that archived records are clearly separate from primary pager pages.
18. As a Book Bee user, I want bottom navigation to remain visible while viewing History, so that I can quickly return to primary workflows.
19. As a Book Bee user, I want Android back from History to return to my previous bottom-tab page, so that navigation behaves naturally.
20. As a Book Bee user, I want selecting any bottom tab to exit History immediately, so that bottom navigation always takes me to primary sections.
21. As a Book Bee user, I want the primary pager to remain limited to `Shelf`, `Scan`, `Wishlist`, and `Search`, so that horizontal swiping stays focused on daily workflows.
22. As a Book Bee user, I want the primary tab order to remain `Shelf`, `Scan`, `Wishlist`, `Search`, so that adding Search causes minimal disruption.
23. As a Book Bee user, I want Scan to remain a primary bottom-tab destination, so that ISBN ownership checking stays central to the product.
24. As a Book Bee user, I want Search to search Shelf and Wishlist records, so that it answers what I own or want now.
25. As a Book Bee user, I do not want active Search to include History records, so that archived books do not confuse current ownership decisions.
26. As a Book Bee user, I want History to keep its own search field, so that archived ownership remains discoverable in the right context.
27. As a Book Bee user, I want the Search page to show a search field at the top of its body, so that searching remains obvious once I enter the section.
28. As a Book Bee user, I want the Search page to show a quiet empty state before typing, so that the blank state is understandable without extra decoration.
29. As a Book Bee user, I want the Search empty state to say it searches Shelf and Wishlist, so that the scope is clear.
30. As a Book Bee user, I want Search results to appear in the Search page body, so that results have enough space and do not crowd other workflows.
31. As a Book Bee user, I want active Search results to continue showing whether a match belongs to Shelf or Wishlist, so that result context is clear.
32. As a Book Bee user, I want selecting a Shelf search result to navigate to Shelf, so that I can continue with the owned record.
33. As a Book Bee user, I want selecting a Wishlist search result to navigate to Wishlist, so that I can continue with the wishlist item.
34. As a Book Bee user, I want later support for search-result scroll or highlight focus, so that large lists can take me directly to the selected item.
35. As a Book Bee user, I accept page navigation alone for the immediate MVP, so that this refactor remains focused and reviewable.
36. As a Book Bee user, I want the History screen to avoid a duplicate large `History` title in the body, so that the fixed top bar title does not repeat itself.
37. As a Book Bee user, I want dense utility screens to start with their controls or content, so that vertical space is used efficiently.
38. As a Book Bee user, I want the app shell to feel calm and predictable after this change, so that navigation does not distract from ownership decisions.
39. As a developer, I want Search implemented as a full screen rather than a reused global surface, so that page-level state and layout can evolve cleanly.
40. As a developer, I want the drawer and bottom-nav behavior represented by explicit app-shell state, so that History, back behavior, and tab selection remain deterministic.
41. As a developer, I want existing active search domain behavior reused, so that this navigation change does not alter search matching rules.
42. As a developer, I want existing History search behavior reused in the History destination, so that archived-record search remains scoped and stable.
43. As a developer, I want this change to avoid database, repository, and domain schema work, so that it stays a UI/navigation refactor.
44. As a developer, I want focused tests around app-shell navigation and Search page behavior, so that the information architecture is protected from regression.

## Implementation Decisions

- The primary app sections become `Shelf`, `Scan`, `Wishlist`, and `Search`.
- `History` is removed from the primary bottom navigation.
- The persistent global search surface is removed from the app shell.
- Active library search moves into a dedicated `Search` primary section.
- The `Search` section should be a full screen/page, not an inline global surface mounted above the pager.
- Existing active library search behavior should be reused. The search scope remains active `Shelf` and `Wishlist` records only.
- Search results should continue to identify their target context as Shelf or Wishlist.
- Selecting a Search result navigates to the result's owning primary section.
- Item scroll/focus/highlight after Search result selection is a future enhancement. Page navigation is sufficient for this PRD.
- The Search page should show its search field and a quiet empty state before the user types.
- The Search empty state should communicate scope without adding recent searches, suggestions, or search history.
- Archived History records remain excluded from active Search.
- History keeps its own in-screen search field and archived-record result list.
- A compact persistent app top bar is added above the content area.
- The top bar contains a hamburger button and the active destination title.
- The active destination title is one of `Shelf`, `Scan`, `Wishlist`, `Search`, or `History`.
- Body-level large titles should be removed where they duplicate the top bar title and create vertical clutter.
- The top bar remains fixed while destination content scrolls.
- A modal navigation drawer is added to the app shell.
- The drawer opens only from the hamburger button.
- Drawer edge gestures are disabled everywhere.
- Disabling drawer gestures applies both to primary pager pages and the History destination.
- The drawer layout is sparse and utility-focused.
- The drawer shows `Book Bee` at the top.
- The drawer shows `History` as the only selectable item for now.
- `History` is marked selected in the drawer only while the History destination is active.
- No drawer item is selected while a primary bottom-tab destination is active.
- Import/export drawer entries are omitted until those features are implemented.
- `History` opens as a separate app-shell destination outside the horizontal pager.
- Bottom navigation remains visible while History is active.
- Android back from History returns to the previously active bottom-tab page.
- Selecting any bottom tab exits History and navigates directly to that tab.
- The primary horizontal pager contains only the four primary sections.
- The primary tab order remains `Shelf`, `Scan`, `Wishlist`, `Search`.
- The app should preserve pager swiping for primary sections.
- Drawer behavior must not compete with horizontal pager gestures.
- This PRD does not require changes to Room entities, repositories, backup schema, domain matching, or search ranking.
- The major modules or seams likely to be modified are:
  - app-shell navigation state
  - bottom navigation section metadata
  - persistent top bar
  - modal drawer integration
  - dedicated Search presentation screen
  - History presentation layout cleanup
  - app-shell back handling
- A useful deep module opportunity is a small app-shell navigation state model that encapsulates:
  - active primary section
  - active secondary drawer destination
  - previous primary section for History back behavior
  - selected bottom-tab behavior
  - drawer selection state
  This keeps drawer, pager, and bottom-nav coordination testable without spreading navigation rules through composables.

## Testing Decisions

- Good tests should verify externally visible navigation and search behavior rather than implementation details such as exact drawer internals, animation frames, or Compose layout node structure.
- The app-shell navigation state model should be tested if extracted.
- App-shell behavior should be tested for:
  - bottom nav sections are `Shelf`, `Scan`, `Wishlist`, and `Search`
  - global top search is not rendered on every page
  - top bar title follows the active primary section
  - hamburger opens the drawer
  - drawer exposes `Book Bee` and `History`
  - drawer does not expose import/export placeholders
  - selecting History opens the History destination
  - History keeps bottom navigation visible
  - selecting a bottom tab exits History
  - back from History returns to the previous primary section
  - drawer selection marks History only while History is active
- Search presentation should be tested for:
  - Search page has its own search field
  - Search page shows an empty state before typing
  - Search results render in the page body
  - active search results navigate to Shelf or Wishlist when selected
  - History results are not included in active Search
- History presentation should be tested for:
  - History remains reachable from the drawer
  - History keeps its own search field
  - History does not show a duplicate large body title when the top bar title is present
- Gesture behavior should be verified manually or with targeted UI coverage where practical:
  - horizontal pager swiping still works across primary sections
  - edge-swipe does not open the drawer
  - hamburger tap remains the reliable drawer-opening path
- Prior art in the codebase includes existing ViewModel state tests, domain search tests, core section tests, and selective Compose UI tests. New tests should follow those patterns with deterministic user-visible assertions.

## Out of Scope

- Implementing import/export drawer destinations
- Showing disabled or future import/export drawer placeholders
- Adding search suggestions, recent searches, or saved searches
- Including archived History records in active Search
- Adding a drawer-level global search concept
- Adding item scroll, focus, or highlight behavior after selecting a Search result
- Redesigning Shelf, Scan, Wishlist, or History card content beyond title/search placement needed for this refactor
- Changing search ranking, matching, normalization, or duplicate detection rules
- Changing Room schemas, repositories, or domain entities
- Adding account/profile content to the drawer
- Adding decorative drawer headers or marketing-style branding
- Removing pager swiping between primary sections
- Changing the scan flow or barcode behavior

## Further Notes

- This PRD intentionally keeps the change as a focused UI/navigation refactor.
- The current codebase already has active search and History search behavior; the work is primarily to relocate those surfaces and clarify navigation hierarchy.
- `Search` should be treated as a primary active-library workflow because it helps answer what the user owns or wants now.
- `History` should be treated as secondary ownership context because archived records must remain accessible without polluting current ownership decisions.
- The drawer creates a future home for data-management utilities, but only implemented destinations should be shown.
- The fixed top bar should support Book Bee's calm utility-first direction by giving orientation without consuming the vertical space previously used by global search.
