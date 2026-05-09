# Task 03 — Navigation Shell Refactor

## Goal

Refactor the current application shell navigation into a pager-based navigation model aligned with the Book Bee product philosophy.

This task establishes the permanent navigation direction for the application before deeper feature implementation begins.

The navigation system should feel:

- calm
- utility-first
- low-friction
- fast
- predictable

Avoid generic Material-style bottom navigation patterns.

---

# Product Context

Book Bee is not a content feed app or social platform.

The app has three core modes:

- Shelf
- Scan
- Wishlist

These are peer destinations, not hierarchical sections.

Navigation should reinforce this mental model.

The app should feel closer to:

- Android home screen paging
- Kindle-like utility navigation

and less like:

- Spotify
- Instagram
- generic Material dashboard apps

---

# Navigation Direction

Use:

- Horizontal pager navigation
- Top pager tabs
- Hard page edges
- No bottom navigation bar

The navigation structure should be:

```text
Shelf | Scan | Wishlist
```

Where:

- Scan is centered intentionally
- users can swipe horizontally between pages
- users can tap tabs to jump directly
- the active page is visually indicated
- pages do NOT infinitely wrap

---

# UI Requirements

## Top Navigation Tabs

Implement top pager tabs using text labels only:

```text
Shelf     Scan     Wishlist
```

Requirements:

- text only
- no icons
- centered horizontally
- active tab visually highlighted
- tabs are tappable
- pager swipes update selected tab
- tab taps animate pager movement

---

## Active Indicator

Use a bee-themed orange pill underline indicator.

Requirements:

- short underline/pill
- rounded edges
- thicker than a normal underline
- smooth movement between tabs
- visually subtle
- should not dominate the UI

The indicator should feel:

- calm
- intentional
- lightweight

Avoid:

- oversized tab indicators
- bouncing animations
- full-width segmented controls

---

## Pager Behavior

Use hard edges.

Requirements:

- Shelf is the left-most page
- Wishlist is the right-most page
- no infinite carousel behavior
- no wraparound scrolling

This preserves spatial memory and predictability.

---

# Bottom Area

Remove the current bottom navigation/page indicator redundancy.

Do NOT use:

- Material BottomNavigation
- NavigationBar
- redundant page indicators

The bottom area should instead become reserved future space for contextual actions.

For this task, placeholder space is acceptable.

Future examples:

Shelf:
- search
- sort
- manual add

Scan:
- launch camera
- torch
- manual ISBN

Wishlist:
- search
- filter

Do not implement these actions yet unless already present.

---

# Scan Page Behavior

Important:

Do NOT automatically open the camera when the Scan page becomes active.

Expected behavior:

```text
User swipes/taps into Scan page
↓
Scan screen becomes visible
↓
User explicitly taps scan action
↓
Camera opens
```

Avoid automatic camera initialization triggered solely by pager navigation.

---

# Gesture Behavior

The pager must coexist cleanly with vertically scrolling content.

Shelf and Wishlist pages will eventually contain vertically scrolling lists.

Avoid:

- accidental horizontal page switches during vertical scrolling
- sluggish gesture handling

Prioritize responsive and predictable gesture behavior.

---

# Edge-To-Edge Support

Fix the current issue where navigation content overlaps Android system navigation buttons.

Requirements:

- proper edge-to-edge support
- safe navigation bar insets
- correct bottom padding
- content remains fully usable on gesture navigation and 3-button navigation devices

Use modern Compose inset handling.

---

# Technical Direction

Preferred Compose primitives:

- HorizontalPager
- PagerState
- Material 3 top app bar patterns where useful

Avoid introducing unnecessary navigation complexity.

This is NOT a multi-stack navigation app.

The pager itself is the primary top-level navigation mechanism.

---

# Architecture Notes

This task should remain UI-shell focused.

Do NOT introduce:

- new backend code
- cloud sync
- additional feature modules
- unnecessary abstractions
- complex navigation frameworks

Keep implementation simple and readable.

---

# Success Criteria

This task is successful when:

- horizontal swipe navigation feels natural
- tabs clearly communicate app structure
- navigation feels calm and lightweight
- Android system bars no longer overlap content
- Scan feels intentionally centered and primary
- the UI no longer feels like generic Material bottom navigation
- the app navigation direction feels stable enough to build remaining features on top of

---

# Out Of Scope

Do NOT implement:

- camera scanning flow
- contextual bottom actions
- animations beyond simple tab indicator movement
- settings navigation
- nested navigation stacks
- deep linking
- infinite carousel paging
- decorative transitions

---

# Design Intent Summary

The desired feeling is:

> A focused utility application with lightweight workspace-style navigation.

Not:

> A content-heavy social/mobile dashboard app.
