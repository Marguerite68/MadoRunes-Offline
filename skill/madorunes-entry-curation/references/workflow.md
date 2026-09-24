# MadoRunes entry workflow

Apply these instructions only to a MadoRunes Offline repository. Confirm the project contains its expected `app/src/main/assets/item/` and `app/src/main/assets/wikiImg/` directories before working.

## Entry namespaces and scope order

Every entry ID has exactly six digits: a two-digit namespace code followed by a four-digit sequence local to that namespace. Allocate the next sequence from the highest existing ID in the matching namespace and keep IDs unique. The four-digit local sequence runs from `0000` through `9999`, so each namespace can contain 10,000 entries. Reserve namespace codes `03` through `99` for future entry types; when adding a type, register its code in the App parser and reviewer before creating entries.

| Type | Namespace | Full range | Examples |
| --- | --- | --- | --- |
| 魔女 | `00` | `000000`–`009999` | `000000`, `000001` |
| 角色 | `01` | `010000`–`019999` | `010000`, `010001` |
| 词条／术语 | `02` | `020000`–`029999` | `020000`, `020001` |

Prioritize source material in this order:

1. 《魔法少女小圆》TV 版
2. 《魔法少女小圆》剧场版前篇、后篇
3. 《魔法少女小圆》剧场版《叛逆的物语》
4. 《魔法纪录 魔法少女小圆外传》第一季、第二季、第三季
5. 《魔法少女小圆》剧场版《魔女之夜的回天》

Within that order, prefer character entries when choosing work for a batch.

## Initialize only when needed

At the start of work, check both `.review/entry-plan.xlsx` and the reusable image-compression utility. If either is missing, create the missing component before preparing entries. Never recreate, overwrite, or reset a component that already exists.

On initialization:

1. Inspect released JSON, images, and ID allocation without changing them.
2. Create `.review/entry-plan.xlsx` with sheets named `魔女`, `角色`, and `词条`.
3. Each sheet has exactly these four columns: `编号`, `名称`, `是否添加`, `发布版本`.
4. Include released entries and suitable future candidates. Mark released entries as `是`; mark candidates as `否`. Use Chinese names in the `名称` column; use an English name only when no Chinese name can be established after reasonable research.
5. Fill released entries' release version from repository evidence, or `待确认` when it cannot be established. Use `待定` for new candidates until the user supplies a release version.
6. Add one reusable image-compression utility to the project. It must process a file or a directory, preserve PNG alpha, report source/output byte size, format, and dimensions, and target roughly 80% of the source byte size or less while aiming for 300 KB or below. Do not create image-specific scripts.

## Research and draft entries

Use `https://moegirl.icu` and `https://magireco.moe` as the first sources for entry prose and images. High-quality, clean entry images are the top visual priority: search both wikis first, then broaden the search only when neither provides a suitable image.

Prefer clean character art with transparent backgrounds and no obvious outline. Treat a text-only entry as a last resort: use `"imagePath": null` only after reasonable searches across the two priority wikis and other appropriate sources fail to yield a suitable image.

For every used prose or image source, add an `externalLinks` item to the JSON. Do not add type labels that distinguish prose from image sources; list both sources if they differ.

For an entry with an image:

- Put JSON in `.review/pending/item/` and the image in `.review/pending/wikiImg/`.
- The JSON filename and image filename must share the exact basename.
- `imagePath` must equal the actual image filename.
- Prefer PNG; compress it with the reusable utility when necessary.

For every entry, follow existing JSON formatting and content style. Write Chinese prose that is factually grounded and free of invented details, but do not flatten it into a rigid template: retain meaningful setting, context, and distinctive details so the entry remains lively and enjoyable to read.

## Batch cadence

For each batch:

1. Select up to 10 rows with `是否添加` set to `否`, preferring roles.
2. Research and prepare their review candidates in `.review/pending/`.
3. Update prepared rows to `是` and set `发布版本` only when a planned release version is known; otherwise keep `待定`.
4. Add 10 further suitable candidates with `是否添加` set to `否` to keep the queue populated.
5. Report prepared entries, newly queued candidates, JSON validation, image presence/format/dimensions/size, all recorded sources, and unresolved questions.
