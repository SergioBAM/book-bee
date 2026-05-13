# M1-02 — Domain Models

## Goal

Create domain models that represent Book Bee shelf data without depending on Android or Room.

## Context

Read:
- AGENTS.md
- docs/product.md
- docs/architecture.md
- docs/roadmap.md

## Requirements

Implement domain models for:
- UserProfile
- Book
- BookIdentifier
- Ownership
- WishlistItem

Implement value/enums for:
- IdentifierType
- OwnershipStatus
- ReadStatus

Add basic normalization helpers if appropriate:
- title normalization
- author normalization
- ISBN normalization placeholder

## Constraints

- Domain code must not depend on Android framework APIs
- Domain code must not depend on Room annotations
- Do not implement repositories yet
- Do not build UI

## Validation

Run:

```bash
./gradlew test
./gradlew assembleDebug