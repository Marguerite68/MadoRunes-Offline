---
name: madorunes-entry-curation
description: "Collect, research, and prepare MadoRunes Offline encyclopedia entries for local review. Use when adding or curating this project's wiki data; do not use for general Android or web work."
---

# Madorunes Entry Curation

Prepare reviewable MadoRunes Offline encyclopedia entries without changing released data.

## Required boundaries

- Never modify `app/src/main/assets/item/` or `app/src/main/assets/wikiImg/` while curating entries.
- Write new review candidates only to `.review/pending/`; leave `.review/approved/` and `.review/rejected/` untouched.
- Do not commit, push, or import approved entries into formal assets unless the user explicitly asks.
- Preserve the existing JSON schema. In particular, do not add separate image-source or image-license fields.

## Workflow

Before initializing a plan or preparing a batch, read [the MadoRunes workflow reference](references/workflow.md). It defines the ID namespaces, source priority, review-plan spreadsheet, image requirements, and batch cadence.

Use the repository's existing entries as the schema and content-style reference. Keep factual claims traceable through `externalLinks`; list every source actually used, regardless of whether it supplied prose or an image.

At the end of each batch, report what was prepared, validation results, image details, source links, newly queued candidates, and anything that needs user review.
