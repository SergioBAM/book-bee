# OW-08 — Metadata Enrichment And Conservative Likely-Owned Upgrade

Status: `ready-for-agent`
Type: `AFK`

## Parent

- [docs/prd-mvp-ownership-workflows.md](/home/serge/code/personal/book-bee/docs/prd-mvp-ownership-workflows.md)

## What to build

Implement the metadata-enriched scan intelligence slice so Book Bee can upgrade immediate local results into conservative same-work warnings without sacrificing offline-first behavior.

This slice should deliver:

- asynchronous metadata lookup after the immediate local result
- conservative fuzzy matching that requires author support
- support for multiple ranked `LikelyOwned` candidates
- live `NotOwned -> LikelyOwned` upgrades when metadata provides enough evidence
- no downgrade of exact `Owned`
- metadata-failure fallback into the minimal manual-save path
- user-data-wins behavior when provider fields conflict with locally trusted values

## Acceptance criteria

- [ ] A local `NotOwned` result can upgrade to `LikelyOwned` after metadata arrives when conservative fuzzy-match rules are satisfied.
- [ ] `LikelyOwned` requires author support and does not trigger from title-only similarity.
- [ ] Metadata failure still allows the user to continue with the defined minimal scan-failure save path.
- [ ] Provider metadata fills blanks without silently replacing locally trusted user-entered values.
- [ ] Tests cover fuzzy-match conservatism, live result upgrades, metadata-failure fallback, and conflict-free enrichment behavior.

## Blocked by

- [OW-06-manual-isbn-scan-result-flow.md](/home/serge/code/personal/book-bee/tasks/OW-06-manual-isbn-scan-result-flow.md)

