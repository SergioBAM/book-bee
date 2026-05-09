# Book Bee — Roadmap

## Purpose

This roadmap exists to keep Book Bee focused on shipping a genuinely useful product instead of drifting into architecture perfectionism.

The goal is to build:

- a fast utility
- an offline-first ownership tracker
- a trustworthy shelf management app
- a stable scanning workflow

The MVP succeeds when users can reliably answer:

> “Do I already own this book?”

---

# Core Delivery Philosophy

The MVP succeeds if:

- scanning works reliably
- ownership checks are fast
- the shelf feels trustworthy
- backup/restore works
- the app survives real-world usage

The MVP does NOT succeed because:

- the architecture is academically elegant
- every edge case is solved
- sync exists
- the UI is beautiful
- fuzzy matching is perfect

Book Bee is a durable utility application, not a showcase app.

---

# MVP Milestone Plan

## Milestone 0 — Foundation Bootstrap

### Goal

Establish the permanent architectural direction and project foundation.

### Deliverables

- Android project created
- Compose setup
- Hilt setup
- Room setup
- module structure
- navigation skeleton
- CI build working
- baseline theme
- fake in-memory data

### Notes

No real features yet.

The purpose is to lock the development direction early.

### Success Criteria

- app launches
- swipe navigation works
- modules compile cleanly
- architecture direction is stable

### Time Target

1–3 days

---

## Milestone 1 — Shelf First

### Goal

Create a fully usable local shelf before barcode scanning exists.

### Deliverables

- Book entity
- Ownership entity
- Room persistence
- add/edit/delete manual book
- shelf list
- shelf detail screen
- search
- read status
- quantity support

### Notes

No barcode scanning yet.

No metadata providers yet.

This validates:

- data model
- persistence
- navigation
- Compose architecture
- workflows

### Success Criteria

User can genuinely manage a small library manually.

### Time Target

1–2 weeks

---

## Milestone 2 — Scanning MVP

### Goal

Build the core ownership-checking workflow.

### Deliverables

- CameraX integration
- ML Kit barcode scanning
- ISBN extraction
- scan freeze behavior
- ownership lookup
- exact match detection
- “Owned / Not Owned” result UI
- add scanned book to shelf

### Notes

No fuzzy matching yet.

No provider fallback chain yet.

### Success Criteria

User can walk into a bookstore and test ownership checks.

### Time Target

1 week

---

## Milestone 3 — Metadata + Fuzzy Matching

### Goal

Make scanning feel intelligent and reliable.

### Deliverables

- Google Books integration
- OpenLibrary fallback
- metadata cache
- fuzzy matching
- “Likely Owned”
- edit-before-save flow
- metadata failure handling

### Success Criteria

Duplicate detection feels trustworthy most of the time.

### Time Target

1–2 weeks

---

## Milestone 4 — Backup & Restore

### Goal

Make the app safe to trust long-term.

### Deliverables

- JSON export
- JSON import
- merge import
- replace import
- schema versioning
- SAF file picker integration

### Notes

This is more important than cloud sync.

Users forgive missing features.

They do not forgive lost data.

### Success Criteria

Device wipe simulation succeeds.

### Time Target

1 week

---

## Milestone 5 — Beta Hardening

### Goal

Prepare the app for real-world users.

### Deliverables

- crash fixes
- performance fixes
- migration testing
- accessibility pass
- empty states
- loading/error polish
- app icon
- onboarding hints
- optional analytics
- internal testing flow

### Success Criteria

Stable APK usable for months.

### Time Target

Ongoing

---

# Feature Phases

## Phase 1 — Core Utility

### Must Have

- shelf
- scan
- ownership check
- manual add
- search
- wishlist
- backup/restore

This is the real MVP.

---

## Phase 2 — Reliability & Intelligence

### Features

- improved fuzzy matching
- metadata reconciliation
- improved search
- duplicate merge tools
- scan history
- better import/export UX

---

## Phase 3 — Quality-of-Life

### Features

- cloud backup
- tablet optimization
- widgets
- bulk scanning
- batch edit
- barcode history
- statistics

---

## Phase 4 — Optional Expansion

### Possible Features

- price comparison
- wishlist alerts
- lending tracking
- cross-device sync

### Important

Avoid drifting into:

- social platform features
- Goodreads-style recommendations
- decorative collection systems

---

# Repo Setup Order

## Recommended Initial Structure

```text
app
core
data
domain
feature-shelf
feature-scan
feature-wishlist
feature-settings
```

## Important

Avoid excessive modularisation early.

Do not create:

- core-ui
- core-designsystem
- core-navigation
- core-network
- core-database

until genuinely needed.

---

# Setup Order

## Step 1

Create:

- app
- core

Only.

## Step 2

Add:

- domain
- data

After Room exists.

## Step 3

Add:

- feature-shelf

Build the first real feature completely.

## Step 4

Add:

- feature-scan

Only after shelf is stable.

## Step 5

Add remaining feature modules as needed.

---

# Implementation Order

1. Local data model
2. Room + repositories
3. Shelf workflows
4. Search
5. Scanner
6. Metadata providers
7. Fuzzy matching
8. Import/export
9. Polish

---

# Technical Spikes

## Spike 1 — CameraX + ML Kit

Validate scan speed and freeze behavior.

## Spike 2 — Room Migration Strategy

Understand migration pain early.

## Spike 3 — Fuzzy Matching

Experiment with matching quality before production implementation.

## Spike 4 — JSON Export Size

Validate that full exports remain practical.

---

# “Don’t Build This Yet” Guardrails

## Do NOT Build

- cloud sync
- multi-device conflict resolution
- user accounts
- advanced metadata reconciliation
- recommendation systems
- decorative bookshelf UI
- offline sync queues
- generic repository abstractions
- MVI mega-frameworks
- event bus architectures
- excessive modularisation

---

# Testing Strategy

## Unit Tests

Focus on:

- fuzzy matching
- normalization
- import/export
- use cases

## Instrumentation Tests

Focus on:

- Room migrations
- scan workflows
- navigation-critical flows

## Manual Testing

Test with:

- real books
- damaged barcodes
- weird ISBNs
- offline usage
- large shelf sizes

---

# Release Strategy

## Phase 1 — Local APK

Install manually for rapid iteration.

## Phase 2 — Internal App Sharing

Use Play Console internal app sharing.

## Phase 3 — Closed Beta

Validate:

- crash telemetry
- migrations
- upgrade stability

## Phase 4 — Public Release

Only after:

- backup/restore proven
- migrations stable
- crash rate acceptable

---

# Beta Distribution Flow

## Alpha Group

Developer + spouse.

## Small Beta

5–10 real readers.

## Broader Closed Beta

25–100 users.

---

# Metrics That Matter

Track:

- scan success rate
- duplicate detection accuracy
- restore success rate
- crash-free sessions
- shelf size scaling

---

# Debt Intentionally Deferred

## Acceptable MVP Debt

- basic fuzzy matching
- imperfect metadata normalization
- minimal animations
- simple search
- non-perfect Compose optimization
- simple module boundaries
- minimal theming

## Debt NOT Acceptable

- data loss risk
- migration instability
- fragile persistence
- unreliable scanning
- unclear ownership matching

---

# Final Product Goal

The first target is NOT:

> “finish the app”

The first target is:

> “build a stable shelf workflow trusted in daily use”

Once the app becomes personally trustworthy:

- scanning becomes meaningful
- backup becomes meaningful
- polish becomes meaningful

That is the point where Book Bee becomes a real product instead of just a project.