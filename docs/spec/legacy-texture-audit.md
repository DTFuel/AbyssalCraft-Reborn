# Legacy texture audit (T9.1b / PK-1b)

## Contract

`docs/validation/RR-LEGACY-TEXTURES.json` is the permanent one-entry-per-source ledger for the exact 644 PNG files under `docs/AbyssalCraft-1.12.2/src/main/resources/assets/abyssalcraft/textures`. Each entry records the legacy-relative source path, SHA-256, one of `MIGRATED` / `REPLACED` / `RETIRED` / `BLOCKED`, modern targets, a reason, and an owner.

The ledger is not inferred from current model references. `node scripts/audit_assets.js --check` independently enumerates and decodes all legacy PNG files, verifies every frozen source hash, rejects missing/duplicate/stale rows, checks every target exists, decodes every PNG target, and requires `BLOCKED=0`. Success prints:

```text
RR_LEGACY_TEXTURE_AUDIT_OK source=644 migrated=591 replaced=3 retired=50 blocked=0
```

`MIGRATED` means at least one listed modern PNG has the exact source SHA-256. `REPLACED` means the legacy pixels are intentionally superseded by the listed real modern resource, model catalog, renderer, screen, or registered content behavior. It does not mean that a missing current reference proves completion. `RETIRED` is limited to the frozen list below. `BLOCKED` is never accepted by the gate.

## Direct migrations completed by PK-1b

The legacy ore overlays are preserved byte-for-byte under `textures/block/ore_overlay`. Fourteen modern ore hosts use `abyssalcraft:block/layered_ore`; no AC ore model retains a vanilla ore placeholder. A single overlay may serve multiple modern hosts after registry flattening.

The Coralium Longbow body and three pulling frames are preserved byte-for-byte as `coralium_longbow*.png`. Its modern bow model has three pull overrides and no longer displays the refined Coralium ingot placeholder.

The remaining `MIGRATED` rows include prior direct copies and renamed resources. Their exact target hashes, rather than their names or references, are the evidence.

## Replacement evidence

- Complex block layers, statue/tombstone textures, multiface blocks, and merged machine faces point to `assets/abyssalcraft/complex_block_model_fidelity.json`, whose own gate freezes legacy hashes and modern models/textures.
- Abbreviated armor and item frames point to the registered modern armor/item contract, semantic models, component-driven screens, or consolidated textures.
- Legacy fixed container sheets point to registered modern screen classes. Legacy NEI/JEI sheets point to the production JEI plugin and categories.
- Legacy Necronomicon chrome and fixed page templates point to the modern navigable screen; migrated page images remain separately hash-verifiable assets.
- Legacy entity layers point to the modern renderer/layer contract. Variant-specific pixels that remain active are represented by actual modern texture targets; old-only variants are retired below.

## Frozen retired content

Exactly 50 source PNG files are retired. These are explicit legacy-only artifacts with no modern production registry, metadata, model, screen, or renderer owner:

| Legacy source | Retirement evidence |
|---|---|
| `logo.png` | No Forge/NeoForge metadata logo field or modern logo resource consumes it. |
| `armor/default.png` | Generic legacy armor fallback was replaced by explicit registered armor visual sets. |
| `blocks/altar.png`, `blocks/altar/{basebot,basetop,parts}.png` | The legacy standalone `altar` block has no modern registry ID; the production ritual and energy altar owners use separately audited models. |
| `blocks/calcifiedstone.png` | The legacy `calcified_stone` block and Purged biome owner are absent from the frozen modern registry. |
| `blocks/dsbf.png` | No legacy model/blockstate or modern production model references this orphaned texture. |
| `blocks/ritualaltar/{cloth2,cloth3,cloth4}.png` | These sheets belonged only to the removed material-specific ritual altar IDs; the modern registry explicitly consolidates their stone page visual to `ritual_altar`. |
| `blocks/ritualaltar/parts2.png` | This sheet belonged to the removed legacy sacrificial-altar model; the canonical modern ritual altar uses the separately migrated `parts.png`. |
| `blocks/ritualpedestal/overlay_{1,2,3,4,5,6,7}.png` | These overlays belonged only to removed material-specific ritual pedestal IDs; the modern registry explicitly consolidates their stone page visual to `ritual_pedestal`. |
| `blocks/summoning_statue/{masonry,misc,robe_front,robe_sides,robe_top}.png` | The legacy three-block `summoning_statue` owner has no modern registry IDs; functional deity statues are distinct OBJ-backed content. |
| `gui/necronomicon/{crafting,crystallization,item,materialization,placeofpower,ritual,ritual_creation,ritual_infusion,spell,template,template512,template1024,transmutation}.png` | Unreferenced legacy page-template drafts; production pages were rendered by Java and modern pages render live item, image and recipe content over the five migrated book backgrounds. |
| `gui/necronomicon/{missing,missing_item,missing_recipe}.png` | Explicit missing-content placeholders; the modern manifest reports unavailable content as text and production must never render a placeholder image. |
| `items/deprecated.png` | Deliberate deprecated placeholder; no modern item/model registration exists. |
| `items/devsword.png` | Legacy development-only sword; no modern production item/model registration exists. |
| `items/hilt.png` | Unreferenced 16x16 draft; the production Katana Hilt used `katana.png` with `dreadhilt.json`, both migrated separately. |
| `items/necronahicon.png` | Removed legacy joke/alternate book; no modern production item/model registration exists. |
| `items/scrolls/scroll_alt.png` | Unreferenced blank scroll draft; all production legacy scroll models use their named tier/unique textures. |
| `items/scriptures_omniscience.png` | Removed legacy knowledge shortcut; no modern production item/model registration exists. |
| `model/abyssal_zombie_old.png` | Explicit old entity skin variant; modern renderer uses the current Abyssal Zombie texture. |
| `model/abyssal_zombie_old_eyes.png` | Eyes layer paired only with the retired old entity skin variant. |
| `model/remnant/trader/villager.png` | Skin for the legacy `remnanttrader` test entity whose sole registry call was commented out; it is distinct from the active seven-profession Remnant. |
| `model/staff2.png` | Texture for an abandoned Staff mode whose only model-bakery and mesh-definition registrations were commented out; the production OBJ uses `staff.png`. |

Any future retirement requires adding an explicit source row and auditable content-removal evidence here; unmatched files must not be classified as retired by default.

## Visual validation boundary

The automated gate owns source identity, PNG decoding, target existence, ledger completeness, and model/resource wiring. Final in-game visual judgement remains a user validation item; pixel migration and asset wiring are not delegated to that step.
