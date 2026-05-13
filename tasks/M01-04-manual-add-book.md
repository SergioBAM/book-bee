# M1-04 — Manual Add Book

## Goal

Allow the user to manually add a book to their shelf.

## Requirements

Implement:
- Add Book screen or sheet
- fields for title, author, optional ISBN, quantity, read status, notes
- save action
- validation for required title
- persistence through repository layer
- navigation back to Shelf after save

## Constraints

- Manual books are first-class records
- Metadata lookup is not required
- Barcode scanning is not required
- Do not add wishlist behaviour in this task

## Validation

Run:

```bash
./gradlew assembleDebug