# PRD - Wishlist Swipe Remove

Status: `ready-for-agent`
Date: `2026-05-17`
Source: Synthesized from wishlist UX design review, current repo state, and existing product direction.

## Problem Statement

Wishlist item cards currently expose too many visible actions. Each card shows `Edit`, `Add to Shelf`, and `Remove`, which makes the row feel crowded and weakens the action hierarchy.

This is especially noticeable in Book Bee because Wishlist is meant to be lightweight, calm, and utility-first. The current card layout gives a destructive action the same visual weight as constructive actions, even though the primary user jobs in Wishlist are:

- maintain lightweight future intent
- edit intent when needed
- move a wishlist item onto Shelf once the book is owned

The result is a noisier list than necessary, weaker emphasis on `Add to Shelf`, and a less predictable card layout. The user wants removal to remain available, but hidden behind a direct swipe gesture so the visible card can focus on the main workflow.

## Solution

Book Bee will remove the visible `Remove` button from Wishlist item cards and move wishlist deletion behind a left-only swipe action.

The new interaction model is:

- a wishlist row keeps visible `Edit` and `Add to Shelf` actions
- `Add to Shelf` remains the dominant action
- `Edit` remains a secondary maintenance action
- swiping a row left triggers a true dismiss rather than revealing a hidden button
- the row shows a destructive visual cue while swiping
- once the dismiss threshold is crossed, the row is removed immediately
- successful removal is acknowledged with a generic snackbar that offers `Undo`
- `Undo` restores the exact same wishlist item in the exact same position
- only the most recently removed wishlist item is undoable
- failure feedback remains inline on the Wishlist screen

To preserve discoverability after removing the button, the Wishlist empty state will teach the hidden gesture inside the existing explanatory copy. This change stays scoped to Wishlist, while establishing a broader product direction that empty section space can teach section-specific behavior.

## User Stories

1. As a wishlist user, I want fewer visible buttons on each wishlist card, so that the list feels calmer and easier to scan.
2. As a wishlist user, I want `Remove` hidden behind a gesture instead of shown inline, so that destructive actions do not compete with primary actions.
3. As a wishlist user, I want `Edit` to remain visible, so that I can quickly adjust title, author, ISBN, or notes.
4. As a wishlist user, I want `Add to Shelf` to remain visible and visually dominant, so that moving intent into ownership stays fast.
5. As a wishlist user, I want removal to happen only on a left swipe, so that the destructive gesture is consistent and learnable.
6. As a wishlist user, I want the swipe to be a true dismiss rather than a reveal-then-tap action, so that the interaction stays low-friction.
7. As a wishlist user, I want the row to show a destructive visual cue while I swipe, so that the result is clear before I release.
8. As a wishlist user, I want the row to disappear immediately once dismissal succeeds, so that the UI confirms the action decisively.
9. As a wishlist user, I want an `Undo` after removal, so that accidental swipe deletes are recoverable.
10. As a wishlist user, I want `Undo` to restore the exact same item in the same place, so that recovery feels trustworthy and lossless.
11. As a wishlist user, I want only the most recent swipe removal to be undoable, so that the behavior stays simple and predictable.
12. As a wishlist user, I want the removal confirmation message to stay generic, so that feedback remains concise and unobtrusive.
13. As a wishlist user, I want failed removals to leave the item visible and explain the problem inline, so that I know the destructive action did not complete.
14. As a wishlist user, I want swipe removal to work even for rows that also show `On Shelf`, so that I can clear stale wishlist intent without affecting ownership.
15. As a wishlist user, I want card-originated horizontal drags to prioritize row dismissal over section paging, so that the swipe gesture works reliably.
16. As a user moving between sections with the pager, I want section paging to remain available outside the wishlist card interaction area, so that section navigation still feels intact.
17. As a screen-reader user, I want a non-visual remove action on wishlist rows, so that swipe-only deletion does not make the feature inaccessible.
18. As a screen-reader user, I want the accessibility remove action to behave the same way as the swipe gesture, so that the feature does not have different safety rules depending on input mode.
19. As a new wishlist user, I want the empty state to teach me how Wishlist works, so that I can understand the section without a tutorial.
20. As a new wishlist user, I want the empty state to mention that saved items can be removed with a left swipe, so that the hidden destructive action remains discoverable.
21. As a product maintainer, I want the empty-state teaching copy folded into the existing Wishlist explanatory card, so that discoverability improves without adding more UI chrome.
22. As a solo developer returning later, I want the swipe-remove behavior clearly specified in one document, so that implementation and later refinements do not drift.

## Implementation Decisions

- The scope of this PRD is the Wishlist browse experience only. It does not widen into a full cross-section empty-state redesign in the same change.
- The feature modifies the `feature-wishlist` UI and presentation flow and requires a small shared app-shell update to support transient snackbar feedback.
- Wishlist browse rows should expose exactly two visible actions after this change:
  - `Edit`
  - `Add to Shelf`
- `Add to Shelf` should remain visually primary. `Edit` should remain visually secondary.
- Wishlist removal moves from an inline button to a left-only swipe gesture.
- The swipe interaction is a true dismiss. It must not stop in a revealed-action state.
- The swipe interaction should show a destructive background cue while the row is moving left.
- Crossing the dismiss threshold should remove the row immediately from the visible list.
- Successful removal should surface through a transient snackbar with `Undo`, rather than the existing inline success message pattern.
- Removal failure should not use the snackbar. Failure remains inline so the user can read and react while the row stays present.
- `Undo` semantics are exact restoration semantics. The restored wishlist item must keep the same identity and return to the same sort position it previously occupied.
- Only the most recently removed wishlist item is tracked for undo. Multi-item undo stacks are out of scope.
- The snackbar copy for successful removal should remain generic rather than item-specific.
- Swipe removal applies to all wishlist rows, including rows that also show `On Shelf`. Removing wishlist intent must not affect the shared book record or any shelf ownership state.
- Because Book Bee uses pager/swipe-centric section navigation, wishlist card gestures must take precedence when a horizontal drag starts on a wishlist row. Section paging remains available outside that interaction area.
- Accessibility must preserve a non-visual removal path. The accessibility action should execute the same behavior contract as swipe removal:
  - immediate remove
  - generic snackbar
  - single-item undo
- The Wishlist empty state should teach the hidden gesture inside the existing explanatory card rather than through a new hint surface.
- The empty-state copy should stay concise and cover both the section purpose and the hidden remove gesture.
- This change suggests a broader product principle that empty section space can teach section-specific behaviors, but that broader pattern remains follow-up work rather than part of this implementation.
- The main modules or seams likely to be modified are:
  - Wishlist browse presentation state and event handling
  - Wishlist row interaction composable behavior
  - transient app-level feedback handling for snackbar-based undo
  - wishlist deletion and restoration orchestration for single-item undo
- A useful deep module opportunity is a small wishlist removal coordinator that encapsulates:
  - immediate UI removal
  - tracking the last removed item
  - restoring the item on undo
  - clearing undo state after the snackbar window closes
  This keeps row gesture code and ViewModel code simpler and easier to test.

## Testing Decisions

- Good tests should verify external behavior and stable user-visible outcomes, not Compose implementation details or animation internals.
- The most important tests for this feature are behavior tests around removal, undo, and the interaction contract between Wishlist state and transient feedback.
- The Wishlist presentation layer should be tested for:
  - removing an item from the visible list after a successful dismiss event
  - exposing snackbar undo state after successful removal
  - restoring the exact same item when `Undo` is triggered
  - restoring the item to its original position in the recency-sorted list
  - limiting undo to the most recently removed item
  - keeping failure feedback inline when deletion fails
  - allowing removal of wishlist rows that also show `On Shelf`
- The wishlist removal coordinator or equivalent orchestration seam should be tested for:
  - single-item undo tracking
  - replacement of older undo state when a newer removal occurs
  - cleanup of undo state after the transient recovery window ends
- UI tests should be selective and focus on critical user-visible outcomes, such as:
  - the `Remove` button no longer appearing on wishlist cards
  - `Edit` and `Add to Shelf` remaining visible
  - empty-state copy teaching swipe removal
  - accessibility action availability for row removal
- Tests should not assert exact swipe animation frames or internal dismiss-state mechanics unless those mechanics become part of a stable abstraction.
- Prior art in the codebase includes existing ViewModel state tests, normalization tests, domain invariant tests, and repository behavior tests. New tests should follow the same pattern of deterministic inputs and user-meaningful assertions.

## Out of Scope

- Redesigning empty-state teaching patterns across `Shelf`, `Scan`, or future sections in this change
- Adding multi-item undo stacks or batch recovery
- Adding a confirmation dialog before wishlist removal
- Converting swipe removal into a reveal-then-tap pattern
- Changing Wishlist sort order
- Changing Shelf, Scan, or History information architecture
- Adding wishlist history or soft-delete/archive behavior for removed wishlist items
- Adding item-specific snackbar copy for removal success
- Broad gesture-system refactors outside the Wishlist row and app feedback scope

## Further Notes

- The current codebase already has immediate wishlist deletion and inline message handling, but it does not yet have a snackbar-based undo path or an established swipe-dismiss pattern.
- This feature should preserve Book Bee's MVP principles: calm, predictable, low-friction, and offline-first.
- The destructive action being hidden is acceptable only because the design pairs it with both a recoverable `Undo` path and explicit empty-state teaching.
- This PRD intentionally keeps the change reviewable. It solves the current Wishlist clutter problem without silently expanding into a larger navigation or section-layout redesign.
