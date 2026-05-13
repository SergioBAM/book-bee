# M1-03 — Repository Layer

## Goal

Add repository interfaces and Room-backed implementations for shelf storage.

## Context

Read:
- AGENTS.md
- docs/product.md
- docs/architecture.md
- docs/roadmap.md

## Requirements

Implement:
- repository interfaces for shelf operations
- Room-backed repository implementations
- mappings between Room entities and domain models
- default local user creation or retrieval

Support:
- create book
- update book
- archive ownership
- observe owned books
- get book detail by ID

## Constraints

- Do not build manual add UI yet
- Do not implement scanner logic
- Do not implement metadata lookup
- Do not implement import/export
- Keep business rules out of Composables

## Validation

Run:

```bash
./gradlew test
./gradlew assembleDebug