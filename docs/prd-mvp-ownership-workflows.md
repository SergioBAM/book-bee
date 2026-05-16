# PRD - MVP Ownership Workflows

Status: `ready-for-agent`
Date: `2026-05-16`
Source: Synthesized from product-design decision session and current repo state.

## Problem Statement

Book Bee needs a coherent MVP product spec for its core ownership workflows before implementation expands beyond the current Room foundation, repository layer, and navigation shell. The app's central promise is simple: help the user answer, quickly and reliably, "Do I already own this book?"

That promise breaks down if ownership state, wishlist intent, archived history, duplicate handling, scan results, and import behavior are ambiguous. At the moment, the repository and domain model already establish major entities such as `Book`, `BookIdentifier`, `Ownership`, `WishlistItem`, and `UserProfile`, but several product rules still need to be fixed so implementation can proceed without drift.

The user needs a clear MVP spec that defines:

- what counts as owned versus historical context
- how exact ISBN and fuzzy same-work matching should behave
- how shelf, wishlist, and archived history interact
- how scan flows should behave under success, ambiguity, and failure
- how import/export should preserve data without introducing silent loss
- which modules should encapsulate the deeper logic so the system stays testable and maintainable

## Solution

Book Bee will ship an ownership-centric MVP built around a small set of durable, explicit concepts:

- `Shelf` represents current ownership
- `Wishlist` represents future intent
- `History` represents archived prior ownership
- the local database remains the source of truth
- exact ISBN identity is the strongest ownership signal
- fuzzy matching is conservative and caution-oriented
- metadata enriches records but never blocks the core ownership answer

The MVP will treat current ownership, wishlist intent, and archived history as separate but related contexts on top of shared `Book` records. Scan and manual ISBN flows will use the same result pipeline. Exact duplicate prevention will be enforced in normal write paths. Archive/restore will be explicit lifecycle transitions rather than implicit side effects. JSON backup and restore will preserve the user's trusted local library model, while derived metadata cache and scan history remain out of scope.

The product will favor calm, predictable behavior over aggressive automation. It will interrupt users only when confidence is genuinely high enough to prevent accidental duplicates, especially in exact ISBN and conservative fuzzy same-work cases.

## User Stories

1. As a book shopper, I want to scan a book and get an immediate ownership answer from local data, so that I can decide quickly even without network access.
2. As a book shopper, I want exact ISBN matches to return a strong `Owned` result, so that I can avoid buying a duplicate copy.
3. As a book shopper, I want same-work different-edition matches to return `LikelyOwned`, so that I can pause before buying another edition by mistake.
4. As a book shopper, I want fuzzy matching to be conservative, so that Book Bee does not warn me about books I do not own.
5. As a book shopper, I want `LikelyOwned` warnings to show the candidate owned books, so that I can judge the match myself.
6. As a book shopper, I want exact archived ISBN matches to show `Previously owned` context instead of `Owned`, so that I understand I do not currently own the book.
7. As a book shopper, I want `Restore from history` offered directly from an exact archived scan result, so that I can reactivate a previous record without detouring through History.
8. As a book shopper, I want `Add as new owned copy` to remain available even when a historical match exists, so that I can represent reacquiring a book explicitly.
9. As a book shopper, I want scan results to show wishlist context separately from ownership context, so that `Wishlist` does not get confused with `Owned`.
10. As a book shopper, I want non-ISBN barcode detections to fail clearly and keep me in the scan flow, so that I can keep scanning or enter ISBN manually.
11. As a user without camera access, I want the Scan section to offer manual ISBN entry when permission is denied, so that the ownership-check flow still works.
12. As a user entering an ISBN manually, I want the same result pipeline as camera scan, so that behavior is consistent regardless of input source.
13. As a user scanning a book with good metadata, I want to tap `Add to shelf` and save immediately, so that quick add stays quick.
14. As a user scanning a book with good metadata, I want to tap `Add to wishlist` and save immediately, so that saving intent is as fast as saving ownership.
15. As a user scanning a book while metadata is still loading, I want the app to delay risky final add actions if a `LikelyOwned` warning may still appear, so that I do not bypass duplicate protection by tapping too early.
16. As a user whose metadata lookup fails, I want to continue with a minimal manual save path, so that lookup failures do not block ownership tracking.
17. As a user saving a scan-failure shelf record, I want `title` required, the scanned `ISBN` preserved, and `author` optional, so that I can save a trustworthy record with minimal friction.
18. As a user adding a manual shelf book, I want it to create an active owned record immediately, so that Shelf always means current ownership.
19. As a user adding a manual book without an ISBN, I want that record to be first-class, so that older or unusual books are still supported.
20. As a user who types an ISBN manually, I want the app to validate it fully, so that malformed ISBNs do not poison exact matching later.
21. As a user who later edits an existing book and adds a valid ISBN, I want duplicate conflict logic to run immediately, so that exact identity problems do not get introduced quietly.
22. As a user who owns multiple copies of the same book, I want quantity support, so that I do not need separate owned records for normal same-edition copies.
23. As a user adding another copy, I want a dedicated action to increment quantity immediately, so that common copy-management stays fast.
24. As a user who taps `Add another copy` by mistake, I want an `Undo`, so that immediate actions remain safe.
25. As a user editing quantity down to zero, I want the app to warn that the record will be archived, so that I understand zero is a lifecycle transition rather than a stored state.
26. As a user removing a book from Shelf, I want the default behavior to archive it, so that my prior ownership history is preserved.
27. As a user browsing History, I want a separate archived section, so that current ownership stays clean and historical data remains accessible.
28. As a user browsing History, I want to restore a book explicitly, so that returning ownership is a deliberate action.
29. As a user browsing History, I want to edit archived records without restoring them, so that I can fix metadata on historical records in place.
30. As a user hard-deleting from History, I want a strong confirmation that explains linked wishlist deletion too, so that destructive cleanup is explicit.
31. As a user who restores a record, I want its original `dateAdded`, notes, and read status preserved, so that restore feels like reactivating the same ownership record.
32. As a user who archives a book marked `READING`, I want archiving to remain allowed without special blocking, so that read status does not become a workflow lock.
33. As a user managing reading state, I want `readStatus` modeled per owned record rather than per physical copy, so that the MVP stays simple and useful.
34. As a user adding a new book, I want `UNREAD` to be the default read status unless I explicitly choose otherwise, so that new records behave predictably.
35. As a user adding a book from Wishlist to Shelf, I want the wishlist item removed automatically with a notification, so that my active intent stays accurate.
36. As a user moving from Wishlist to Shelf, I want wishlist notes offered as editable prefill rather than silently copied, so that intent notes can inform but not overwrite ownership notes.
37. As a user who owns a book but still wants another copy or gift copy, I want same-edition shelf-plus-wishlist overlap to be possible with explicit confirmation, so that rare but valid intent is supported.
38. As a wishlist user, I want wishlist notes but not priority controls, so that the list stays practical without unnecessary management overhead.
39. As a user searching my library, I want active global search to include only Shelf and Wishlist, so that search answers the question of what I own or want now.
40. As a user searching active data, I want one consolidated result row per shared book record with badges, so that cross-context matches stay compact.
41. As a user tapping a consolidated result, I want the app to drill into Shelf first and then Wishlist, so that current ownership remains the primary interpretation.
42. As a user whose consolidated result matched via wishlist notes, I want the Shelf detail to still surface linked wishlist context, so that I do not lose why the result appeared.
43. As a user searching History, I want a separate dedicated search surface, so that archived records are discoverable without polluting active search.
44. As a user searching globally, I want Shelf notes and Wishlist notes included in active search matches, so that my own contextual notes remain useful.
45. As a user searching globally, I want exact ISBN results ranked first and Shelf results ranked ahead of Wishlist on ties, so that the most ownership-relevant answer appears first.
46. As a user browsing Shelf, I want the default sort to show most recently added books first, so that the records I am most likely to verify or edit stay near the top.
47. As a user browsing Wishlist, I want the default sort to show most recently added items first, so that recent intent stays visible.
48. As a user browsing History, I want the default sort to show most recently archived records first, so that recent reversals are easy to review.
49. As a user editing a book shared between Shelf and Wishlist, I want the app to explain that book details are shared but notes and status are separate, so that edits do not feel surprising.
50. As a user editing shared book metadata, I want those book-level changes to apply to all contexts using the same book record, so that title, author, and identifiers stay consistent.
51. As a user relying on exact matching, I want ISBN-10 and equivalent ISBN-13 forms treated as the same identity, so that the app does not miss an exact match because of format generation.
52. As a user saving a valid ISBN, I want the app to derive and store the equivalent other ISBN form when possible, so that later search and matching are more robust.
53. As a user entering a valid ISBN-13 without an ISBN-10 equivalent, I want it saved as-is, so that valid identifiers are never rejected just because no paired form exists.
54. As a user relying on provider metadata, I want the scanned or manually entered ISBN to remain authoritative if provider data conflicts, so that the direct observed identifier is trusted first.
55. As a user editing or adding books, I want the app to prevent creation of new duplicate active exact-ISBN records, so that normal write flows preserve trust in exact ownership results.
56. As a user with legacy or imported exact duplicates already present, I want the scan result to show all matching records clearly, so that data anomalies are visible rather than hidden.
57. As a user who owns one edition but scans another edition of the same work, I want the app to warn me with `LikelyOwned` while still letting me add the distinct edition explicitly, so that separate editions stay supported.
58. As a user who removes a wishlist item, I want it deleted permanently without wishlist history, so that the product stays focused on ownership history rather than generic action logs.
59. As a user importing a backup, I want internal IDs preserved, so that links between books, ownership, wishlist, and history remain stable.
60. As a user importing with `Replace`, I want a strong destructive confirmation naming Shelf, Wishlist, and History, so that I understand exactly what will be overwritten.
61. As a user importing with `Merge`, I want exact ISBN duplicates handled conservatively and automatically, so that merge reduces clutter without causing data loss.
62. As a user importing with `Merge`, I want fuzzy duplicates left separate and summarized for later review, so that uncertain merges are not forced on me.
63. As a user importing data that exactly matches a local archived record, I want that record restored and merged by default, so that history reactivation behaves consistently.
64. As a user importing a wishlist item that exactly matches a currently owned shelf record, I want the wishlist item discarded by default, so that active ownership remains the stronger truth.
65. As a user restoring from backup, I want unsupported or newer backup schema versions blocked entirely, so that the app never performs unsafe partial restores.
66. As a user exporting data, I want backups to reflect only committed database state, so that unsaved form edits never leak into backups.
67. As a user restoring a backup, I want metadata cache excluded from backup payloads, so that restore focuses on durable user-trusted data rather than disposable derived data.
68. As a user who cancels a scan result, I want that result to leave no persistent trace, so that the app does not accumulate unwanted scan history.
69. As a user onboarding into the app, I want concise empty-state explanations of Shelf, Wishlist, and History, so that the context model is obvious without a tutorial.
70. As a solo developer returning to the codebase later, I want the deepest logic hidden behind stable interfaces, so that ownership rules remain testable and implementation does not drift across UI screens.

## Implementation Decisions

- The MVP will continue to use the existing layered model: UI and feature presentation on top of domain rules, with Room-backed repositories as the local source of truth.
- The active product surfaces remain `Shelf`, `Scan`, and `Wishlist`, with `History` as a separate archived area rather than a fourth peer section.
- A single invisible default local user remains the only user model in MVP. Multi-user support stays out of scope.
- `Book` remains the shared record for bibliographic metadata and identifiers. `Ownership` remains the current-or-archived owned context. `WishlistItem` remains a separate intent context referencing a shared `Book`.
- Archived records do not count as current ownership. They participate only as secondary historical context in scan, add, edit, and import flows.
- Exact ISBN identity is the highest-confidence signal. ISBN-10 and equivalent ISBN-13 must be treated as the same exact identifier.
- ISBN handling requires a deeper identity module in the domain layer that encapsulates:
  - normalization
  - checksum validation
  - conversion between ISBN-10 and ISBN-13 where valid
  - canonical comparison behavior for exact-match workflows
- Fuzzy ownership matching requires a dedicated domain matching module that encapsulates:
  - title normalization
  - author normalization
  - conservative scoring
  - title-first but author-required `LikelyOwned` rules
  - candidate ranking for multi-match caution states
- Subtitle should be weak or mostly ignored in fuzzy matching. Author order for multi-author works should not materially affect fuzzy equivalence.
- `LikelyOwned` remains a caution state, not a merge action. It must never silently create quantity changes or prevent adding a separate edition once the user explicitly confirms that intent.
- The scan pipeline should be orchestrated through a stable application-facing module that can:
  - accept camera or manual ISBN input
  - run immediate local ownership matching
  - load metadata as enrichment
  - upgrade `NotOwned` to `LikelyOwned` if metadata adds enough evidence
  - expose deterministic user actions for add, restore, and wishlist flows
- Scan acceptance is limited to valid ISBNs only. Non-ISBN barcode detection stays in the Scan UI and offers manual ISBN entry rather than a result-screen transition.
- Metadata lookup is enrichment, not authority. User-entered or scanned ISBN stays authoritative. User-edited book fields beat provider data. Provider data fills blanks by default and should not silently overwrite trusted local values.
- Scan results and metadata previews remain transient until the user explicitly saves to Shelf or Wishlist.
- Scan cancellation produces no history. Scan history is out of scope for MVP.
- Shelf write behavior should be driven by a dedicated ownership lifecycle module or use-case layer that encapsulates:
  - create owned record
  - add another copy
  - archive ownership
  - restore ownership
  - restore-vs-new-copy decision logic
  - quantity-to-zero archive transition
  - exact duplicate prevention in add and edit flows
- New owned records default to `quantity = 1` and `readStatus = UNREAD`.
- Quantity zero is not a valid persisted state. Quantity decrement from one to zero triggers archive confirmation and archive behavior.
- `Add another copy` is a first-class immediate action. It increments quantity and exposes a reliable short-window undo.
- `dateAdded` represents the timestamp when the ownership record first entered the shelf. It should not be updated when quantity changes or when a record is restored.
- Restore reactivates the same ownership record and preserves contextual fields such as notes and read status.
- Hard delete remains a History-only destructive action. If the deleted record is the last context referencing the book aggregate, the book and identifiers should also be removed.
- Book-level metadata edits are shared across Shelf and Wishlist when both contexts reference the same book record. Context-specific data remains separate:
  - Shelf notes
  - Wishlist notes
  - quantity
  - read status
- Wishlist notes remain in scope. Wishlist priority is removed from MVP and should be removed from the data contract as part of the product simplification.
- Wishlist-to-Shelf transitions remove matching wishlist items automatically and notify the user. Wishlist notes can be offered as editable prefill but must not be silently copied into Shelf notes.
- Same-edition Shelf-plus-Wishlist overlap is allowed only by explicit confirmation. It is not the default or silent path.
- Global search across active contexts should return one consolidated row per shared book record, using badges for `On Shelf` and `Wishlist`.
- Search ranking should prioritize exact ISBN, then stronger shared metadata matches, while preferring Shelf over Wishlist when relevance is otherwise equal.
- Search should include active Shelf notes and active Wishlist notes, but exclude archived notes because History is intentionally separated.
- History requires its own search and default sort of most recently archived first.
- Import/export should be handled through a data-safety-oriented backup module that encapsulates:
  - schema version validation
  - replace confirmation behavior
  - merge decision policy
  - exact ISBN merge rules
  - fuzzy duplicate summary reporting
  - ID preservation
- `Merge` import must be conservative. Exact ISBN duplicates can merge automatically when clearly safe. Fuzzy duplicates remain separate and are summarized for review.
- Imported active ownership that exactly matches a local archived ownership by ISBN should default to restore-and-merge.
- Imported wishlist entries that exactly match active local ownership should be dropped by default during merge rather than preserved as active intent.
- Metadata cache is derived data and remains out of export/import scope for MVP.
- The major modules expected to be built or expanded are:
  - ISBN identity and validation module
  - ownership matching engine
  - scan result orchestration module
  - ownership lifecycle and duplicate-prevention module
  - global search aggregation and ranking module
  - import/export merge policy module
  - feature presentation flows for Shelf, Scan, Wishlist, History, and Settings

## Testing Decisions

- Good tests should verify externally visible behavior and stable domain contracts rather than implementation details. Tests should assert user-meaningful outcomes such as result state, conflict handling, merge decisions, archive transitions, and search ordering.
- Domain-level modules should receive the highest testing priority because they carry the most product risk and the least UI churn.
- The ISBN identity module should be tested for:
  - normalization
  - checksum validation
  - invalid input rejection
  - ISBN-10 to ISBN-13 conversion
  - equivalence matching
- The ownership matching engine should be tested for:
  - exact owned matches
  - exact archived matches
  - conservative fuzzy matches
  - title-only non-matches
  - multi-author order-insensitive matches
  - multi-candidate ranking behavior
- The ownership lifecycle module should be tested for:
  - manual create defaults
  - quantity increment
  - quantity-to-archive transition
  - archive behavior
  - restore behavior
  - restore conflict handling
  - exact duplicate prevention on add and edit
- The scan orchestration module should be tested for:
  - immediate local result emission
  - later metadata enrichment upgrades
  - metadata failure fallback
  - manual ISBN parity with camera scan
  - permission-denied fallback behavior at the presentation boundary
- The global search aggregation module should be tested for:
  - active-only inclusion
  - consolidated row formation
  - badge computation
  - ranking rules
  - note-based matches
  - archived exclusion from active search
- The import/export module should be tested for:
  - schema version rejection
  - ID preservation
  - replace behavior
  - exact duplicate merge behavior
  - fuzzy duplicate separation
  - archived restore-and-merge behavior
  - wishlist removal when exact owned duplicates exist
- Repository and mapper tests should continue verifying persistence shape and invariants at the data boundary, following the same style as the current repository mapping and default-user tests already present in the repo.
- Useful prior art in the codebase includes existing normalization tests, domain invariant tests, repository mapping tests, and Room-backed repository behavior tests. New tests should follow that pattern: narrow fixtures, deterministic expectations, and no dependence on UI internals.

## Out of Scope

- Multi-user support or visible profile management
- Cloud sync, backend services, or Azure implementation
- Scan history or behavioral analytics
- Wishlist priority or complex wishlist ranking
- Price tracking, retailer comparison, or notifications
- Continuous batch scanning
- Non-ISBN scan-based book creation
- Per-copy reading state or acquisition event history
- Advanced metadata reconciliation UX
- Full-text search infrastructure beyond what Room and normalized fields can support in MVP
- Collector-grade edition modeling
- Social features, recommendations, or decorative bookshelf presentation
- Global undo outside the narrow immediate-action cases explicitly defined here

## Further Notes

- This PRD assumes the current repo direction remains intact: native Android, Kotlin, Compose, Room, Hilt, offline-first behavior, and layered architecture.
- The current codebase already supports the foundational entities and repository posture needed for this work, but at least one product-model simplification should feed back into implementation planning immediately: wishlist priority is out of scope and should not be surfaced in the MVP user experience.
- The heaviest logic should stay in deep domain or data-policy modules rather than being split across ViewModels or Composables. The quality bar for this feature set is not visual polish; it is reliable ownership truth, data safety, and predictable recovery paths.
