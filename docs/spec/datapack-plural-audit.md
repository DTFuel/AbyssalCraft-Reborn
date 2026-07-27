# Datapack plural path audit (PL-3 / T10.3b)

## Scope and invariant

This specification freezes the datapack leaf-directory contract seen in the current
`src/main/resources/data` and `src/main/generated/data` trees. Forge 1.20.1 consumes the plural
layout; NeoForge 1.21.1 consumes the singular layout. Shared sources intentionally carry both
physical forms because both Stonecutter nodes use the same resource roots.

Every physical file in an audited family maps to one logical ID:

`<family>:<namespace>:<path-with-extension>`

The two forms of a logical ID must both exist unless the exact ID is listed in the exception table.
An unlisted one-sided ID, a missing declared exception, a now-paired stale exception, a duplicate
physical path across `resources` and `generated`, invalid JSON, or an invalid recipe result schema
fails closed. Cross-version JSON objects are not required to be byte-identical: advancement,
loot-table, and recipe payload schemas legitimately differ between Minecraft versions.

## Frozen path matrix

| Family | Forge 1.20.1 path | NeoForge 1.21.1 path | 1.20 recipe result | 1.21 recipe result |
| --- | --- | --- | --- | --- |
| Recipe | `data/<ns>/recipes/<id>.json` | `data/<ns>/recipe/<id>.json` | `result.item` | `result.id` |
| Loot table | `data/<ns>/loot_tables/<id>.json` | `data/<ns>/loot_table/<id>.json` | n/a | n/a |
| Advancement | `data/<ns>/advancements/<id>.json` | `data/<ns>/advancement/<id>.json` | n/a | n/a |
| Structure | `data/<ns>/structures/<id>` | `data/<ns>/structure/<id>` | n/a | n/a |
| Block tag | `data/<ns>/tags/blocks/<id>.json` | `data/<ns>/tags/block/<id>.json` | n/a | n/a |
| Item tag | `data/<ns>/tags/items/<id>.json` | `data/<ns>/tags/item/<id>.json` | n/a | n/a |
| Entity-type tag | `data/<ns>/tags/entity_types/<id>.json` | `data/<ns>/tags/entity_type/<id>.json` | n/a | n/a |
| Fluid tag | `data/<ns>/tags/fluids/<id>.json` | `data/<ns>/tags/fluid/<id>.json` | n/a | n/a |

Recipe `result` may also be a string where the serializer supports it. Object results are strict:
the Forge form must contain `item` and not `id`; the Neo form must contain `id` and not `item`.

## Producers and consumers

| Family | Current source/provider owner | Runtime consumer/selector |
| --- | --- | --- |
| Recipe | Static resources; `LegacyCraftingRecipeData`, `CookingRecipeData`, `CrystalClusterRecipeData`, `MachineRecipeData` | Vanilla recipe loading; `DataDirs.RECIPE` selects the active leaf name in versioned Java |
| Loot table | Static resources; `ACBlockLoot`, `OreLootData`, `EntityLootData` | Vanilla loot loading and block/entity/structure loot references; `DataDirs.LOOT_TABLE` selects the active name |
| Advancement | Static resources | Vanilla advancement loading and parent references; `DataDirs.ADVANCEMENT` selects the active name |
| Structure | Static JSON/NBT resources | Vanilla worldgen/structure template loading; `DataDirs.STRUCTURE` and `processResources` select the active NBT directory |
| Tags | Static resources; `ACTagData` (`Layout.BOTH`, `FORGE`, or `NEO`) | Vanilla tag loading; `DataDirs.TAG_*`, `ToolCompat`, recipe/tag references, and `EnchantmentCompat` |

`ACDataGenerators` registers every generated-data provider. `ACTagData` and the recipe/loot
providers deliberately emit both physical layouts. `build.gradle.kts` removes the inactive binary
structure-template directory per loader; JSON and tags remain shared, so the source audit rather
than build-directory presence is the authoritative pairing check.

## Exact one-sided exceptions

The allowlist lives in `scripts/audit_datapack_plural.js` and contains exact logical IDs, sides,
and owners. Directory-wide or wildcard exceptions are forbidden.

Neo/1.21-only tags:

| Logical ID | Owner/reason |
| --- | --- |
| `tag_item:abyssalcraft:enchantable/staff_of_rending.json` | `EnchantmentCompat` / `EnchantmentMatrixSelfTest`; Forge uses `EnchantmentCategory` |
| `tag_block:abyssalcraft:incorrect_for_abyssalnite_tool.json` | `ACTagData Layout.NEO` / `ToolCompat` |
| `tag_block:abyssalcraft:incorrect_for_dreadium_tool.json` | `ACTagData Layout.NEO` / `ToolCompat` |
| `tag_block:abyssalcraft:incorrect_for_ethaxium_tool.json` | `ACTagData Layout.NEO` / `ToolCompat` |
| `tag_block:abyssalcraft:incorrect_for_refined_coralium_tool.json` | `ACTagData Layout.NEO` / `ToolCompat` |
| `tag_block:minecraft:incorrect_for_wooden_tool.json` | Vanilla 1.21 tier contract |
| `tag_block:minecraft:incorrect_for_stone_tool.json` | Vanilla 1.21 tier contract |
| `tag_block:minecraft:incorrect_for_iron_tool.json` | Vanilla 1.21 tier contract |
| `tag_block:minecraft:incorrect_for_diamond_tool.json` | Vanilla 1.21 tier contract |
| `tag_block:minecraft:incorrect_for_netherite_tool.json` | Vanilla 1.21 tier contract |
| `tag_block:neoforge:needs_netherite_tool.json` | NeoForge tier tag |
| `tag_entity_type:minecraft:arthropod.json` | Minecraft 1.21 entity classification |
| `tag_entity_type:minecraft:can_breathe_under_water.json` | Minecraft 1.21 entity classification |
| `tag_entity_type:minecraft:undead.json` | Minecraft 1.21 entity classification |

Forge/1.20-only tag:

| Logical ID | Owner/reason |
| --- | --- |
| `tag_block:forge:needs_netherite_tool.json` | `ACTagData Layout.FORGE` / `TierSortingRegistry` |

## Permanent audit

Run from the repository root:

```powershell
node .\scripts\audit_datapack_plural.js
```

Success prints exactly one line beginning with `RR_DATAPACK_PLURAL_AUDIT_OK`. Counts are discovered
from the two source roots on every run; the script contains no hard-coded expected resource counts.
The current baseline produced by the command is:

```text
RR_DATAPACK_PLURAL_AUDIT_OK logical=1056 paired=1041 exceptions=15 stale=0 recipeSchema=984 recipe=492/492/0 loot_table=335/335/0 advancement=9/9/0 structure=37/37/0 tag_block=47/36/11 tag_item=133/132/1 tag_entity_type=3/0/3 tag_fluid=0/0/0
```

Each family triple is `logical/paired/exceptions`. The acceptance invariant is always
`logical = paired + exceptions`, every declared exception is present on its declared side,
`stale=0`, all audited JSON parses, and every recipe result passes the side-specific schema check.
The numeric snapshot documents this tree state; the command's dynamically computed output is the
expected count source after any intentional datapack change.