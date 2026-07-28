# R7a RR-RENAME / T10.2 rename regression audit

## 1. Scope and result

Status: **PASS (static closure)** on 2026-07-28. This audit covers the 1.20.1 Forge to 1.21.1 NeoForge API and data-directory rename boundary, Stonecutter node selection, modern content IDs, and the explicit legacy rename maps used by current generators and Necronomicon rendering. It does not change runtime behavior or claim that arbitrary legacy saves receive a global data fixer.

Permanent command:

```powershell
node scripts/audit_renames.js
```

Current result:

```text
RR_RENAME_AUDIT_OK json=2619 paired=1186 recipes=1364 mappings=49 stale=0
```

The script reads only `src/main/resources/data`, `src/main/generated/data`, and the four explicit Java map owners listed in section 5. It parses every current JSON file, checks dual directory layouts, checks recipe result keys, verifies modern rename targets have a consumer, and rejects old namespaced item IDs that remain in current JSON values (apart from two documented cross-registry collisions).

## 2. Stonecutter node contract

| Concern | Consumer / owner | Replacement or mapping | 1.20.1 Forge expectation | 1.21.1 NeoForge expectation |
|---|---|---|---|---|
| Logical nodes | `settings.gradle.kts` | `version("1.20.1-forge", "1.20.1")`; `version("1.21.1-neoforge", "1.21.1")` | project `:1.20.1-forge`, Java 17 | project `:1.21.1-neoforge`, Java 21 |
| Active editing node | `stonecutter.gradle.kts` | `stonecutter active "1.20.1-forge"` | raw source defaults remain compilable Forge branches | Neo source is produced by `stonecutterGenerate`, not by compiling raw commented branches |
| Loader constants | `stonecutter.gradle.kts` | project suffix mapped to `forge` / `neoforge` constants | `//? if forge` active | `//? if neoforge` active |
| Version predicates | shared Java sources | `//? if <1.21` / `//? if >=1.21` | old signature active | replacement signature active |
| Build ordering | `build.gradle.kts` | `createMinecraftArtifacts` depends on `stonecutterGenerate` | generated Forge source precedes Loom artifacts | generated Neo source precedes Loom artifacts |
| Resource selection | `build.gradle.kts#processResources` | exclude the inactive binary structure directory | keep `structures/`, exclude `structure/` | keep `structure/`, exclude `structures/` |

The two node directories and their Gradle properties exist. A rename is closed only when the shared business caller stays version-neutral and both generated node sources select exactly one valid branch. The static audit does not replace two-node compilation; the compile regression command remains:

```powershell
.\gradlew.bat :1.20.1-forge:compileJava :1.21.1-neoforge:compileJava --rerun-tasks
```

## 3. API rename matrix

| 1.20.1 API / representation | 1.21.1 replacement | Compat owner | Production consumers | Dual-node expectation |
|---|---|---|---|---|
| `new ResourceLocation(...)` | `fromNamespaceAndPath`, `parse`, `withDefaultNamespace` | `platform/ACRef` | 123 source files / 292 call sites found statically, including registries, worldgen, rendering, recipes and network payload parsing | business code calls `ACRef`; only this owner selects constructors versus factories |
| `BlockEntity.load(CompoundTag)` and registry-free `saveAdditional` | `loadAdditional` and `HolderLookup.Provider` overloads | `platform/BlockEntityCompat` | machine, ritual, portal, tombstone and energy block entities through direct extension or `ACBlockEntity` | subclasses implement only `saveData/loadData`; Forge receives `null`, Neo receives registry lookup |
| `ContainerHelper.saveAllItems/loadAllItems` without lookup | overloads with `HolderLookup.Provider` | `platform/ContainerCompat` | machine inventories, ritual inventories, crystal bags, stone/spirit tablets | identical stored logical inventory; only Neo serializes component-aware stacks with lookup |
| `ItemStack.isSameItemSameTags` | `ItemStack.isSameItemSameComponents` | `platform/ContainerCompat` | machine outputs, ritual/spell ingredients and item transfer filters | one `canStack` contract; no business caller names tags/components directly |
| `Block.use(...)` | `useWithoutItem(...)` plus `useItemOn(...)` / `ItemInteractionResult` | `platform/InteractiveBlockCompat` | machine blocks, ritual blocks, energy drops, sealing lock and spirit altar | held-item command runs first on both nodes; empty/default interaction preserves sided success/pass semantics |
| `Recipe<Container>`, embedded `getId`, plain recipe values | `Recipe<RecipeInput>`, external `RecipeHolder`, holder-returning manager queries | `platform/RecipeCompat`, `DataRecipeCompat`, serializer compat classes | processing recipes, machine lookup and JEI enumeration | Forge assigns file ID and unwraps no holder; Neo receives input/lookup types and unwraps `RecipeHolder.value()` |
| UUID/name `AttributeModifier`; `ADDITION/MULTIPLY_*` | resource ID modifier; `ADD_VALUE/ADD_MULTIPLIED_*` | `platform/AttributeCompat` | armor/tool/entity attribute construction | Forge derives stable UUID from the canonical resource ID; Neo uses that ID directly; operation meaning is unchanged |
| `FoodProperties.Builder.saturationMod` | `saturationModifier` | `platform/FoodCompat` | food item registration | same nutrition/saturation values; one version-correct setter per generated node |
| `setSecondsOnFire` | `igniteForSeconds` | `platform/IgniteCompat` | entity/effect ignition callers | duration remains seconds and only the method name changes |
| code-registered `Enchantment` objects/categories | datapack registry entries and item tags | `platform/EnchantmentCompat`, `system/enchant/ACEnchantments` | five enchantments, effects, Staff of Rending and Necronomicon enchanted books | Forge registers five objects and custom category; Neo loads five `enchantment/*.json` files and the singular Staff tag; shared code uses `ResourceKey` |

The audit deliberately documents these owners without changing `platform/`; broad compatibility work belongs to RR-COMPAT.

## 4. Resource directory and schema matrix

| Resource kind | 1.20.1 path | 1.21.1 path | Consumers / producer | Expected assembly |
|---|---|---|---|---|
| Recipe | `recipes/` | `recipe/` | vanilla RecipeManager; all recipe data providers | shared source/generated roots contain both logical copies |
| Loot table | `loot_tables/` | `loot_table/` | entity/block loot loading; loot providers | both copies, including modern entity aliases |
| Advancement | `advancements/` | `advancement/` | advancement loader and knowledge hooks | both copies for each AC advancement |
| Binary structure template | `structures/` | `structure/` | `DataDirs.STRUCTURE`, `StructureFixtureValidator`, template manager | both exist in source; `processResources` ships only the active node copy |
| Predicate | `predicates/` | `predicate/` | vanilla datapack loader | pair required whenever AC adds one |
| Item modifier | `item_modifiers/` | `item_modifier/` | vanilla datapack loader | pair required whenever AC adds one |
| Block tag | `tags/blocks/` | `tags/block/` | block/tag consumers and `ACTagData` | `Layout.BOTH` emits both; explicitly Neo-only `incorrect_for_*` tags emit singular only |
| Item tag | `tags/items/` | `tags/item/` | recipes, item tags and enchantments | normal tags emit both; Staff enchantable tag is Neo-only because Forge uses `EnchantmentCategory` |
| Entity type tag | `tags/entity_types/` | `tags/entity_type/` | entity tag consumers | pair required when present |
| Fluid tag | `tags/fluids/` | `tags/fluid/` | fluid tag consumers | pair required when present |
| Worldgen JSON | `dimension/`, `dimension_type/`, `worldgen/...` | same | vanilla dynamic registries | one shared path; no synthetic singular/plural copy |

Five intentional single-node files are frozen in `scripts/audit_renames.js` with owners: the Staff of Rending enchantable item tag and four Neo `incorrect_for_{abyssalnite,dreadium,ethaxium,refined_coralium}_tool` block tags. Any new single-node path fails until it receives a specific audited owner.

Recipe result shape is also versioned: files under `recipes/` must use `result.item`; files under `recipe/` must use `result.id`. The permanent audit rejects a missing object, a wrong key, or a file containing both keys. Ingredients are not rewritten by this rule: legacy crafting conversion intentionally retains the ingredient schema accepted by each generated recipe.

## 5. Registry IDs and legacy rename maps

| Map owner | Entries checked | Consumer | Replacement rule | Closure expectation |
|---|---:|---|---|---|
| `LegacyCraftingRecipeData.FIELD_OVERRIDES` | 7 | 401-file legacy crafting converter | legacy Java field names to canonical modern item/block paths | target is registered/consumed; generated recipe contains only modern target IDs |
| `LegacyCraftingRecipeData.LEGACY_ID_OVERRIDES` | 2 | legacy crafting ingredient/result conversion | `ethaxiumbrick -> ethaxium_bricks`; `ethbrick -> ethaxium_brick` | block material and item remain distinct canonical IDs |
| `EntityLootData.ITEM_IDS` | 34 | 69 legacy entity loot tables plus 28 modern aliases | old loot item IDs to current item IDs | emitted loot uses a current item target; identity entries document retained IDs |
| `NecronomiconItemVisuals.RENAMES` | 6 | legacy item-page visual resolver | old block/fluid field names to renderable modern item IDs | target has a current Java/resource consumer and produces an item stack at runtime |

Total explicit entries checked: **49**. The audit treats IDs as registry-scoped. Two strings are valid collisions and are not stale item references:

- `abyssalcraft:antichicken`: old item ID in `EntityLootData.ITEM_IDS`, but still the canonical entity type in the Coralium Infested Swamp spawn list.
- `abyssalcraft:coralium`: old item ID in `EntityLootData.ITEM_IDS`, but still the canonical damage type referenced by vanilla damage-type tags.

This distinction prevents a string-only scanner from deleting valid entity or damage-type references. No current JSON value retains another renamed namespaced item ID from the audited maps.

Legacy recipe IDs such as `aaxe` and legacy loot-table IDs may remain as resource IDs for compatibility; they are not registry-item aliases. `EntityLootAudit.modernAliases()` intentionally adds modern entity loot-table names while retaining legacy table names. A global missing-mapping handler or save data fixer is outside this task and was not introduced.

## 6. Permanent regression contract

`scripts/audit_renames.js` fails on:

1. invalid JSON in either current data root;
2. a present singular/plural resource without its counterpart, unless the exact path has a documented node-only owner;
3. a recipe whose result object uses the wrong `item`/`id` key for its directory;
4. disappearance or unparsability of one of the four explicit rename maps;
5. a non-identity modern rename target with no independent Java/resource consumer;
6. a renamed namespaced item ID still present in current JSON values, excluding the two registry-scoped collisions above.

The existing `DatagenClosureValidationData` remains the stronger runtime-registry-aware gate for serializers, ingredients, loot targets, advancement parents, tags and worldgen types. The new script is complementary: it is cheap, headless, independent of Gradle/game registries, and suitable for rename regression checks before a two-node compile or datagen run.

No repository behavior error was found that justified changing production mappings or resource references. No commit was created, and `DEVELOPMENT.md`, porting01-03, and broad platform compatibility code were not modified.