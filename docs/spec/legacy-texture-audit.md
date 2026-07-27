# Legacy texture audit (T9.1b / PK-1b)

## Contract

`docs/validation/RR-LEGACY-TEXTURES.json` is the permanent one-entry-per-source ledger for the exact 644 PNG files under `docs/AbyssalCraft-1.12.2/src/main/resources/assets/abyssalcraft/textures`. Each entry records the legacy-relative source path, SHA-256, one of `MIGRATED` / `REPLACED` / `RETIRED` / `BLOCKED`, modern targets, a reason, and an owner.

The ledger is not inferred from current model references. `node scripts/audit_assets.js --check` independently enumerates and decodes all legacy PNG files, verifies every frozen source hash, rejects missing/duplicate/stale rows, checks every target exists, decodes every PNG target, and requires `BLOCKED=0`. Success prints:

```text
RR_LEGACY_TEXTURE_AUDIT_OK source=644 migrated=474 replaced=161 retired=9 blocked=0
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

Exactly nine source PNG files are retired. These are explicit legacy-only artifacts with no modern production registry, metadata, model, screen, or renderer owner:

| Legacy source | Retirement evidence |
|---|---|
| `logo.png` | No Forge/NeoForge metadata logo field or modern logo resource consumes it. |
| `armor/default.png` | Generic legacy armor fallback was replaced by explicit registered armor visual sets. |
| `gui/container/spellcraft_test.png` | Test-only spellcraft sheet; production spell UI and JEI category do not register the test screen. |
| `items/deprecated.png` | Deliberate deprecated placeholder; no modern item/model registration exists. |
| `items/devsword.png` | Legacy development-only sword; no modern production item/model registration exists. |
| `items/necronahicon.png` | Removed legacy joke/alternate book; no modern production item/model registration exists. |
| `items/scriptures_omniscience.png` | Removed legacy knowledge shortcut; no modern production item/model registration exists. |
| `model/abyssal_zombie_old.png` | Explicit old entity skin variant; modern renderer uses the current Abyssal Zombie texture. |
| `model/abyssal_zombie_old_eyes.png` | Eyes layer paired only with the retired old entity skin variant. |

Any future retirement requires adding an explicit source row and auditable content-removal evidence here; unmatched files must not be classified as retired by default.

## Visual validation boundary

The automated gate owns source identity, PNG decoding, target existence, ledger completeness, and model/resource wiring. Final in-game visual judgement remains a user validation item; pixel migration and asset wiring are not delegated to that step.
