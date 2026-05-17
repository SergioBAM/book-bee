# OW-03 Review 01 — Wishlist Metadata And Duplicate Integrity Fixes

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md](/home/serge/code/personal/book-bee/tasks/issues/OW-03-wishlist-mvp-with-notes-and-shelf-handoff.md)

## Review source

- Review findings against the OW-03 implementation patch

## What to build

Fix the data-integrity regressions introduced in the OW-03 wishlist implementation so shared `Book` metadata is not degraded by lightweight wishlist or shelf forms, and exact ISBN wishlist saves remain duplicate-safe.

This follow-up should deliver:

- preservation of richer existing Shelf metadata when a user confirms `Add to wishlist anyway` for an exact owned ISBN
- reuse of an existing exact-ISBN wishlist item instead of creating a second active wishlist entry for the same edition
- preservation of richer existing Wishlist metadata when a manual Shelf add promotes a wishlist book by exact ISBN
- tests that lock these metadata-preservation and duplicate-prevention rules in place

## Review issues to address

### 1. Preserve shelf metadata on owned-overlap confirmation

Current risk:

- Saving a wishlist item for an already-owned exact ISBN can overwrite the shared `Book` title/authors using sparse wishlist form input.
- Because `author` is optional in the wishlist form, confirming overlap with a blank author can erase author data from the owned shelf record.

Action points:

- Change the exact-owned-overlap save path so it does not degrade existing shared `Book` metadata.
- Treat the lightweight wishlist form as insufficient authority to clear richer shared metadata unless that edit is explicitly intended.
- If the shared `Book` must be reused, preserve existing title/authors when the wishlist form omits them.

### 2. Reuse the existing wishlist item for exact ISBN matches

Current risk:

- Saving the same ISBN to Wishlist twice can create duplicate active wishlist rows for the same edition.
- Auto-removal on later ownership creation becomes unreliable because exact-ISBN cleanup may remove only one of the duplicates.

Action points:

- Add exact-ISBN lookup against Wishlist before creating a new wishlist record.
- Reuse or update the existing wishlist item when an exact ISBN match already exists for the same user.
- Keep the duplicate-prevention rule aligned with the product’s exact-edition identity behavior.

### 3. Preserve wishlist metadata when promoting to Shelf by ISBN

Current risk:

- Manual Shelf add currently rebuilds the reused shared `Book` from the shelf form when an exact wishlist ISBN match is found.
- Because the shelf form only requires `title`, leaving `author` blank during promotion can erase author data that already exists on the wishlist-backed shared book.

Action points:

- Reuse the existing matched wishlist book metadata during ownership creation unless the user explicitly provides replacement values.
- Prevent sparse shelf form input from silently clearing existing shared title/author metadata during wishlist-to-shelf promotion.

## Acceptance criteria

- [ ] Confirming `Add to wishlist anyway` for an exact owned ISBN does not erase or downgrade existing shared Shelf metadata when wishlist form fields are sparse.
- [ ] Saving a wishlist item for an ISBN already present in Wishlist reuses the existing wishlist record instead of creating a duplicate active row.
- [ ] Creating Shelf ownership from an exact wishlist ISBN match preserves existing shared wishlist book metadata unless the user explicitly replaces it.
- [ ] Tests cover owned-overlap metadata preservation, exact-ISBN wishlist duplicate prevention, and wishlist-to-shelf metadata preservation.

## Suggested verification

- Run the relevant domain and app tests for wishlist save and wishlist-to-shelf ownership creation flows.
- Add focused unit coverage for sparse-form cases where `author` is blank but shared metadata already exists.

## Additional review issues from swipe-remove follow-up

### 4. Route undo restore through wishlist save invariants

Severity: `P1`

Location:

- [WishlistViewModel.kt](/home/serge/code/personal/book-bee/app/src/main/java/com/sergebailes/bookbee/ui/wishlist/WishlistViewModel.kt)

Current risk:

- `Undo` restores a deleted wishlist row by calling `wishlistRepository.saveWishlistBook(...)` directly from the ViewModel.
- This bypasses the exact-ISBN reuse and overlap rules in `SaveWishlistItemUseCase`.
- If the user deletes a wishlist row, saves the same ISBN again, and then taps `Undo`, the old row can be restored as a second active wishlist entry or a second shared `Book` for the same edition.

Action points:

- Restore wishlist removals through the same save invariants used by normal wishlist saves, or add an equivalent domain-level restore path that enforces those invariants.
- Add coverage for deleting a wishlist item, re-adding the same ISBN, and then attempting undo.
- Ensure undo cannot reintroduce duplicate ISBN/book state.

### 5. Clear pending undo when the browse layout leaves composition

Severity: `P2`

Location:

- [WishlistScreen.kt](/home/serge/code/personal/book-bee/app/src/main/java/com/sergebailes/bookbee/ui/wishlist/WishlistScreen.kt)

Current risk:

- The snackbar-driven undo state is cleared only when the browse-layout `showSnackbar(...)` call returns.
- If the user leaves the browse layout while the snackbar is visible, such as opening the add form or move-to-shelf flow, the coroutine is cancelled before either snackbar result branch clears `removalFeedback`.
- Returning to the browse layout can reshow the stale snackbar and keep `Undo` available past the intended short-lived recovery window.

Action points:

- Clear pending removal feedback when the browse layout leaves composition or when the UI enters a non-browse mode.
- Keep the transient undo window bounded to the snackbar lifecycle.
- Add coverage for leaving browse mode while removal feedback is active.
