# Book Bee - Product Overview

## Vision

Book Bee is a native Android application designed to help users quickly determine whether they already own a physical book while shopping.

The application focuses on speed, clarity, and reliability rather than visual flair or social engagement. It is intended to function as a practical utility tool for avid readers with growing personal libraries.

Book Bee aims to reduce accidental duplicate purchases while also providing lightweight tracking of ownership, reading status, and wishlist items.

---

# Core Product Principles

## Utility First

Book Bee is designed as a tool, not a social platform or entertainment experience.

The application should feel:
- clean
- predictable
- fast
- readable
- low friction

The user should be able to access important information quickly with minimal interaction.

---

## Ownership-Centric

The primary concern is whether the user owns a book, not bibliographic perfection.

Users think in terms of:
- "Do I own Dune?"
rather than:
- "Do I own this exact ISBN edition?"

The system should support:
- exact ISBN matches
- fuzzy title/author matching
- multiple editions and variants

without exposing unnecessary complexity to the user.

---

## Minimal Cognitive Load

The interface should avoid:
- visual clutter
- oversized cover art
- excessive animations
- social features
- gamification
- recommendation feeds

Information density should remain high while preserving readability on mobile devices.

---

## User Ownership of Data

Users should retain ownership and portability of their data.

The application should support:
- JSON export/import
- backup and restore workflows

Cloud synchronization and backups should complement local ownership rather than replace it.

---

# Primary Use Cases

## 1. Ownership Check While Shopping

Primary workflow:

1. User opens the app
2. User taps Scan
3. Barcode is scanned
4. Application checks ownership status
5. User receives one of:
   - Owned
   - Likely Owned
   - Not Owned

The experience should be fast, deliberate, and confidence-building.

---

## 2. Shelf Management

Users can:
- add books manually
- scan books into their shelf
- archive books
- mark books as read
- browse owned books
- search and sort their collection

The shelf represents ownership history rather than physical storage location.

---

## 3. Wishlist Tracking

Users can:
- add books to a wishlist
- review desired books later
- potentially compare pricing in future releases

Wishlist functionality should remain lightweight and practical.

---

# Scan Experience

Book Bee uses a deliberate scan flow.

## Desired Behavior

1. User initiates scan manually
2. Camera opens with live barcode detection
3. Successful scan triggers:
   - haptic feedback
   - scanner freeze
   - immediate navigation to result screen

The scanner should never continuously process multiple books without explicit user intent.

---

# Ownership Matching

## Exact Match

An exact ISBN/barcode match results in:
- Owned

The application may display:
- quantity owned
- purchase/addition date
- matching shelf entry

---

## Fuzzy Match

Books may also match via:
- normalized title
- normalized author
- edition similarity

This results in:
- Likely Owned

The matching owned book should be shown to the user for confirmation.

---

# Shelf Data

Each owned book may contain:

- title
- author
- ISBN
- date added
- read status
- read date (future enhancement)
- ownership status
- optional notes

The system should support multiple copies and editions.

---

# UI Philosophy

The UI should feel:
- enterprise clean
- calm
- readable
- highly functional

Book cover imagery is considered secondary and should not dominate the interface.

Primary navigation is expected to focus on:
- Shelf
- Scan
- Wishlist

with horizontal swipe navigation between sections.

---

# Multi-User Model

Initial releases use isolated user libraries.

Users do not share shelves or collections.

Each user maintains:
- their own owned books
- their own wishlist
- their own read history

---

# Failure Handling

If metadata lookup fails after a successful barcode scan:

The user should be able to:
- retry lookup
- manually enter book details
- continue using the application

The inability to retrieve metadata should not block ownership tracking.

---

# Future Features (Post-MVP)

Potential future enhancements include:

- retailer price comparison
- wishlist notifications
- price tracking
- lending tracking
- read history timeline
- metadata reconciliation
- cloud synchronization
- backup providers
- recommendation systems
- enhanced duplicate detection

These features are explicitly out of scope for the initial MVP.

---

# Explicit Non-Goals

The MVP is not intended to be:

- a social network
- a Goodreads competitor
- a collector cataloguing platform
- a reading analytics system
- a recommendation engine
- a visually decorative bookshelf simulator

Book Bee prioritizes practical ownership tracking above all else.

---

# Platform

Initial platform support:
- Android only

Initial distribution:
- side-loaded APKs for testing

Potential future distribution:
- Google Play Store

---

# Product Identity

Book Bee is intended to feel like a trustworthy household utility application for readers with growing physical libraries.

The name reflects:
- collection
- organization
- persistence
- simplicity

without sacrificing clarity or usability.