# AGENTS.md

## Project
Book Bee is a native Android app for checking whether a user already owns a book by scanning its ISBN/barcode.

## Current direction
- Native Android only.
- Kotlin.
- Jetpack Compose.
- CameraX + ML Kit for barcode scanning.
- Azure Functions .NET backend.
- Azure SQL later.
- No offline mode required for MVP.
- Ownership check should be fuzzy by title, not exact ISBN-only.

## Development rules
- Keep changes small and focused.
- Prefer simple implementations over clever abstractions.
- Do not introduce cross-platform frameworks.
- Do not add authentication yet unless explicitly requested.
- Use clear package/module names.
- Update docs when architectural decisions change.

## Verification
Before finishing a task, run the relevant build/test command and report results.