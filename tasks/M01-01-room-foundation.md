# M1-01 — Room Foundation

## Context

Read:
- AGENTS.md
- docs/product.md
- docs/architecture.md
- docs/roadmap.md

## Goal

Add the initial Room persistence foundation for Book Bee.

## Requirements

Implement:
- Room dependencies
- database package structure
- Room database class
- initial entities:
  - UserProfileEntity
  - BookEntity
  - BookIdentifierEntity
  - OwnershipEntity
  - WishlistItemEntity
  - MetadataLookupCacheEntity
- enum/storage support for:
  - IdentifierType
  - OwnershipStatus
  - ReadStatus
  - MetadataProvider
  - LookupType
- basic DAOs for each entity
- database creation wiring
- simple migration posture for schema version 1

## Constraints

- Do not build UI
- Do not implement repositories yet
- Do not add Hilt unless already present and required by current project structure
- Do not implement barcode scanning
- Do not implement metadata lookup
- Do not implement import/export
- Do not use external IDs as primary keys
- Use internal UUIDs or a clearly documented Room-compatible representation
- Keep changes small and reviewable

## Validation

Run:

```bash
./gradlew assembleDebug