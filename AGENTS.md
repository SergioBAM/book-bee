# AGENTS.md

## Project

Book Bee is a native Android application for quickly checking whether the user already owns a physical book while shopping.

The product is a practical utility, not a social network, recommendation engine, or decorative bookshelf app.

Primary goal:

> Help the user answer: "Do I already own this book?"

---

## Current Development Mode

This repository is in early MVP development.

Prioritise:

1. working vertical slices
2. simple, readable code
3. offline-first behaviour
4. testable domain logic
5. data safety
6. maintainability without over-engineering

Do not optimise for:

- complex cloud sync
- perfect bibliographic modelling
- visual polish over utility
- speculative abstractions
- premature modular complexity
- architecture for its own sake

---

## Product Principles

Book Bee should feel:

- fast
- calm
- readable
- predictable
- low-friction
- utility-first

Book cover imagery is secondary.

Ownership status and user actions are primary.

The core user question is ownership, not exact edition cataloguing.

---

## MVP Scope

Build toward the MVP only unless explicitly instructed otherwise.

MVP includes:

- native Android app
- Kotlin
- Jetpack Compose
- Room / SQLite local database
- Hilt dependency injection
- CameraX + ML Kit barcode scanning
- local user profile
- shelf
- wishlist
- manual book add/edit
- barcode scan flow
- ISBN metadata lookup
- exact ISBN ownership matching
- fuzzy title/author ownership matching
- quantity support
- read status
- JSON export
- JSON import
- replace import mode
- merge import mode
- local-first data model
- clean pager/swipe-centric UI

---

## Explicit Non-Goals For MVP

Do not build these unless specifically asked:

- social features
- recommendations
- price tracking
- lending tracking
- full cloud sync
- Azure backend
- multi-device conflict resolution
- advanced reading analytics
- decorative bookshelf UI
- collector-grade edition modelling
- account registration
- telemetry or analytics
- Play Store release automation

Cloud backup and Azure are future concerns. Keep seams where useful, but do not implement them yet.

---

## Architecture Direction

Use a layered architecture:

```text
UI Layer
Jetpack Compose screens, navigation, UI state

Presentation Layer
ViewModels, screen state, user actions

Domain Layer
Use cases, ownership matching, business rules

Data Layer
Repositories, Room DAOs, entity mappings, import/export

Infrastructure Layer
Camera, barcode scanner, metadata providers, file access
```

Dependency direction:

```text
UI -> Presentation -> Domain -> Data -> Infrastructure
```

Domain code must not depend on Android framework APIs.

---

## Suggested Module Structure

Use modest modularisation.

Expected modules:

```text
app
core
domain
data
feature-shelf
feature-scan
feature-wishlist
feature-settings
```

Do not add more modules unless there is a clear need.

Do not collapse everything into `app` once the project has enough code to justify separation.

---

## Technical Stack

Use:

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Navigation or Compose navigation appropriate to the app structure
- Room
- Hilt
- Kotlin coroutines
- StateFlow
- CameraX
- ML Kit Barcode Scanning
- kotlinx.serialization or Moshi for JSON, chosen consistently
- JUnit for domain/unit tests

Avoid:

- RxJava
- XML layouts
- unnecessary third-party UI frameworks
- backend SDKs during MVP
- global mutable state
- direct database access from UI
- business logic inside Composables

---

## Data Ownership Rules

The local database is the source of truth.

The app must remain useful offline.

Core actions should work without network access:

- browse shelf
- search shelf
- check ownership against local records
- add books manually
- edit books
- manage wishlist
- export JSON
- import JSON

Network metadata lookup enriches records. It must not block ownership tracking.

---

## Identity Rules

Every major local entity must use an internal Book Bee ID.

Use internal UUIDs for:

- users
- books
- identifiers
- ownership records
- wishlist items
- metadata cache records

Never use an external identifier as a primary key.

External identifiers such as ISBNs, Google Books IDs, OpenLibrary IDs, or future Azure IDs are data fields only.

---

## Core Entities

Expected conceptual entities:

```text
UserProfile
Book
BookIdentifier
Ownership
WishlistItem
MetadataLookupCache
```

A single default local user is acceptable for MVP.

Manual books are first-class records. They are not failed metadata lookups.

A wishlist item should reference a book record so metadata can be reused.

Multiple owned copies are represented by quantity for MVP.

---

## Barcode Scan Flow

The scan flow is deliberate, not continuous batch scanning.

Expected behaviour:

```text
User taps Scan
Camera opens
ML Kit detects barcode
First valid ISBN barcode is accepted
Haptic feedback fires
Scanner freezes
App navigates to scan result
Ownership check runs
Metadata lookup runs if needed
Result is displayed
```

After a successful scan, the user must explicitly choose to scan again.

Do not continuously process multiple books without user intent.

---

## Ownership Matching

Ownership matching returns one of:

```text
Owned
LikelyOwned
NotOwned
```

### Owned

Return `Owned` when a scanned ISBN exactly matches an ISBN stored locally for an owned book.

### LikelyOwned

Return `LikelyOwned` when no exact ISBN match exists, but normalized title and author are similar enough.

Use practical matching:

- normalized title
- normalized authors
- punctuation stripping
- casing normalization
- token comparison
- edit-distance or similarity scoring

Suggested weighting:

```text
Title: 70%
Author: 30%
```

Show the matching owned book to the user for confirmation.

### NotOwned

Return `NotOwned` when neither exact nor fuzzy matching finds a reasonable match.

The user should be able to:

- add to shelf
- add to wishlist
- edit metadata before adding
- scan again

---

## Metadata Lookup

Use a provider-based metadata lookup design.

Initial provider order:

```text
1. Google Books
2. OpenLibrary
3. Manual entry fallback
```

Use an interface similar to:

```kotlin
interface BookMetadataProvider {
    suspend fun lookupByIsbn(isbn: String): BookMetadataResult
}
```

Provider-specific response models must not leak into UI or domain code.

Cache metadata lookup responses locally where practical.

Metadata failure is normal. The user must still be able to continue manually.

---

## Import And Export

Book Bee exports JSON.

Export should include enough data to reconstruct the local library:

- schema version
- export timestamp
- app name
- users
- books
- identifiers
- ownership records
- wishlist items
- read status
- notes
- metadata cache if useful

Do not omit important user data to save space.

Import modes:

```text
Replace
Merge
```

Replace import must clearly warn before deleting existing local data.

Merge import must be conservative and avoid silent data loss.

Prefer internal IDs when available. Use ISBN/fuzzy matching only when needed to detect likely duplicates.

---

## UI Direction

Primary sections:

```text
Shelf
Scan
Wishlist
```

Use horizontal pager/swipe-centric navigation.

The app may feel similar to swiping between Android home-screen pages.

Keep the active section clear.

Reserve bottom UI space for contextual controls and primary actions.

Avoid oversized cover art and unnecessary animation.

---

## Presentation Pattern

Use ViewModels with immutable UI state.

Expose state using:

```kotlin
StateFlow<UiState>
```

Represent user actions explicitly with clear functions.

Example:

```kotlin
fun onSearchChanged(query: String)
fun onBookSelected(bookId: UUID)
fun onAddBookClicked()
```

Avoid business logic directly inside Composables.

Composables should mostly render state and send events.

---

## Error Handling

Treat failure as expected.

Handle:

- no network
- metadata lookup failure
- incomplete provider response
- barcode scan success but metadata failure
- invalid import file
- duplicate book detection
- uncertain fuzzy match
- Room errors
- permission denial for camera or file access

Every recoverable failure should provide a useful next action.

For metadata failure, allow:

- retry lookup
- manual add
- edit scanned ISBN
- cancel
- continue without metadata

---

## Testing Strategy

Prioritise tests for domain and data safety.

Write unit tests for:

- ISBN normalization
- title normalization
- author normalization
- exact ownership matching
- fuzzy ownership matching
- import merge behaviour
- import replace behaviour
- JSON export shape
- repository mapping logic

Use UI tests selectively for critical flows.

High-value test flows:

- manual add book
- scan result owned
- scan result likely owned
- scan result not owned
- add scanned book to shelf
- add scanned book to wishlist
- export backup
- import backup

Do not over-invest in fragile UI tests before the MVP shape settles.

---

## Implementation Order

Prefer this build order:

1. repository setup and Gradle structure
2. Compose app shell
3. pager navigation: Shelf / Scan / Wishlist
4. Room schema and entities
5. default local user creation
6. repositories
7. manual add book
8. shelf list/search
9. wishlist list/add
10. ownership matching domain logic
11. fake scan result screen using typed ISBN input
12. metadata provider interface
13. Google Books lookup
14. OpenLibrary fallback
15. real CameraX + ML Kit scan screen
16. JSON export
17. JSON import replace
18. JSON import merge
19. settings/data management screen
20. polish and private beta hardening

This order is intentional: prove the app and data model before spending too much time on camera integration.

---

## Technical Spikes

Use short spikes where uncertainty is high.

Recommended spikes:

- CameraX + ML Kit barcode scanning in Compose
- Google Books ISBN lookup response mapping
- OpenLibrary ISBN lookup response mapping
- JSON export/import schema
- fuzzy title/author matching thresholds
- Android file picker export/import workflow

Spikes should produce small, disposable or easily integrated code.

Do not let spikes become permanent architecture unless cleaned up.

---

## Guardrails For Codex CLI

When making code changes:

- keep changes small and reviewable
- prefer one vertical slice over many half-finished layers
- update tests when changing domain behaviour
- do not introduce cloud/backend code for MVP
- do not add libraries without a clear reason
- do not invent product features
- do not remove manual add or offline behaviour
- do not use external IDs as primary keys
- do not place business logic in Composables
- do not silently discard user data
- do not implement continuous batch scanning
- do not make network access required for ownership checks

When unsure, choose the simplest offline-first implementation that preserves future flexibility.

---

## Definition Of Done

A change is done when:

- it builds
- relevant tests pass
- user-facing behaviour matches the product principles
- domain logic is not hidden in UI code
- errors have a recoverable path
- data loss risks are considered
- the code is understandable to a solo developer returning later

---

## Codex CLI Working Style

For each coding session, work from a specific task.

Good task examples:

```text
Create the initial Gradle project structure for Book Bee using Kotlin, Compose, Hilt, Room, and modest modularisation.
```

```text
Implement the domain models and ownership matching use case with unit tests.
```

```text
Create a fake scan result flow that accepts an ISBN string and shows Owned, Likely Owned, or Not Owned using local repository data.
```

Bad task examples:

```text
Build the whole app.
```

```text
Make the architecture.
```

```text
Add all MVP features.
```

Codex should make incremental commits or clearly separable changes.

---

## Current Product Truth

When product details conflict, follow this priority:

1. explicit user instruction in the current task
2. `AGENTS.md`
3. `architecture.md`
4. `product.md`
5. inferred best practice

Do not silently override product direction. If a change would alter product scope, stop and ask.

## Agent skills

### Issue tracker

Issues and PRDs are tracked as local markdown files under `.scratch/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Uses the default five-label triage vocabulary. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context repo: use root `CONTEXT.md` and `docs/adr/` when present, plus existing docs under `docs/`. See `docs/agents/domain.md`.
