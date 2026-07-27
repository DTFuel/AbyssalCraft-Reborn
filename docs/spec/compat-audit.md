# RR-COMPAT audit (R7a / T10.1b)

## Boundary

`com.shinoow.abyssalcraft.platform` owns every Forge/NeoForge type and every loader-specific entry point. Code outside `platform/**` may call platform contracts but must not import `net.minecraftforge.*` or `net.neoforged.*`.

Vanilla 1.20.1/1.21.1 method overrides still present in business classes are not loader API. They remain source-level Stonecutter forks where Java inheritance makes delegation impossible; loader forks and movable construction/event/registration APIs belong in `platform/**`.

Permanent gate: `node scripts/audit_compat.js`, also wired as Gradle task `rrCompatAudit` and a dependency of `check`. It rejects every business loader reference (imports and fully qualified names), loader-conditioned business forks, malformed or unknown conditions, platform fork symbols outside the consumer-reachable closure (including configured mixins), and any mismatch between that closure and the inventory below.

## Signature families

| Code | 1.20.1 Forge signature | 1.21.1 NeoForge signature |
|---|---|---|
| L-BUS | `net.minecraftforge.eventbus.api.IEventBus`; Forge lifecycle/game events | `net.neoforged.bus.api.IEventBus`; NeoForge lifecycle/game events |
| L-ENTRY | `@Mod` no-arg constructor; `FMLJavaModLoadingContext.get().getModEventBus()` | `@Mod` constructor receives `IEventBus` |
| L-REG | Forge `DeferredRegister`, capability, attachment, network, datagen and renderer registration APIs | NeoForge equivalents under `net.neoforged.*` |
| L-HOOK | `MinecraftForge.EVENT_BUS` and Forge event types | `NeoForge.EVENT_BUS` and NeoForge event types |
| V-ENTITY | no-arg `defineSynchedData()` / old entity constructors and part APIs | `defineSynchedData(SynchedEntityData.Builder)` / builder and new part APIs |
| V-DATA | `CompoundTag` save/load and old data-component/item naming APIs | `HolderLookup.Provider`, data components and new item naming APIs |
| V-RECIPE | `RecipeType`/`RecipeSerializer` and old recipe lookup/result signatures | registry-aware recipe input, holder and display signatures |
| V-RENDER | old `PoseStack`, model/render-layer and dimension-effect signatures | 1.21 render state, layer/model and dimension-effect signatures |
| V-WORLD | plural datapack directories, old structure/spawn/teleport/worldgen signatures | singular datapack directories and 1.21 registry-aware signatures |
| V-ITEM | old food, armor, tooltip, durability, enchantment and tool signatures | data-component/holder based item and enchantment signatures |
| V-MENU | old menu/container/brewing signatures | 1.21 menu, item handler and brewing signatures |

A symbol may use more than one family. Loader-only rows have identical business-facing signatures on both versions; the listed family describes the hidden implementation signature.

## Fork inventory

| Symbol | Family | Consumer | Validation |
|---|---|---|---|
| `ACRef` | V-DATA | global resource-id callers | audit + both compile nodes |
| `ACSimpleEntity` | V-ENTITY | simple effect entities | audit + both compile nodes |
| `ACThrowableProjectile` | L-REG | projectile base classes | audit + both compile nodes |
| `AnvilForgingRecipeSerializer` | L-REG, V-RECIPE | anvil recipe registry | audit + recipe compile |
| `ArmorClientCompat` | L-BUS, L-REG | `ModBootstrapCompat` | audit + client compile |
| `ArmorCompat` | V-ITEM | armor items/visuals | audit + both compile nodes |
| `ArmorDurabilityCompat` | V-ITEM | ritual/ooze durability consumers | audit + both compile nodes |
| `ArmorRenderCompat` | V-RENDER | armor render layers | audit + client compile |
| `BlockEntityCompat` | L-REG, V-DATA | block entities | audit + both compile nodes |
| `BlockFactory` | V-WORLD | block registrars | audit + both compile nodes |
| `BlockModelGen` | L-REG | datagen model providers | audit + datagen compile |
| `BrewingHooksCompat` | L-HOOK, V-MENU | brewing subsystem | audit + both compile nodes |
| `CapabilityAccess` | L-REG | spirit/tablet capability consumers | audit + both compile nodes |
| `ClientColorCompat` | L-BUS, L-REG | `ModBootstrapCompat` | audit + client compile |
| `ClientHooksCompat` | L-BUS, L-HOOK | HUD/clientvars bootstrap | audit + client compile |
| `ClientItemPropertiesCompat` | L-BUS | `ModBootstrapCompat` -> `ClientItemProperties` | audit + client compile |
| `ClientScreenCompat` | L-BUS, L-REG | screen registration | audit + client compile |
| `CommandCompat` | L-HOOK | command bootstrap | audit + both compile nodes |
| `ConfigCompat` | L-REG | config model/runtime | audit + both compile nodes |
| `ConfigScreenCompat` | L-REG | `ModBootstrapCompat` | audit + client compile |
| `ContainerCompat` | V-MENU | inventories and menus | audit + both compile nodes |
| `ContentLootCompat` | L-HOOK, V-WORLD | loot injection hooks | audit + both compile nodes |
| `CrystallizationRecipeSerializer` | L-REG, V-RECIPE | crystallization recipes | audit + recipe compile |
| `DarklandsWorldgenCompat` | L-BUS, L-REG | worldgen bootstrap | audit + both compile nodes |
| `DataDirs` | V-WORLD | datapack path consumers | audit + datagen/static scan |
| `DataGenCompat` | L-BUS, L-REG | all data providers | audit + datagen compile |
| `DataRecipeCompat` | L-REG, V-RECIPE | recipe data providers | audit + recipe/datagen compile |
| `DeathRayRenderCompat` | V-RENDER | boss death-ray layers | audit + client compile |
| `DensityFunctionCompat` | V-WORLD | density-function registration | audit + both compile nodes |
| `DimensionEffectsCompat` | L-BUS, L-REG | client dimension effects | audit + client compile |
| `DimensionLoadingCompat` | L-HOOK | dimension bootstrap | audit + server compile |
| `DimensionSkyCompat` | L-REG, V-RENDER | dimension sky renderers | audit + client compile |
| `EnchantmentCompat` | L-REG | enchantment bootstrap | audit + both compile nodes |
| `EnchantmentDataCompat` | V-ITEM, V-DATA | enchantment datagen/runtime | audit + both compile nodes |
| `EntityAttributeCompat` | L-BUS, L-REG | entity registrars | audit + both compile nodes |
| `EntityCatalogValidationCompat` | L-HOOK | startup validation | audit + server compile |
| `EntityPartCompat` | V-ENTITY | multipart entities | audit + both compile nodes |
| `EntityRendererCompat` | L-BUS, L-REG | renderer bootstrap | audit + client compile |
| `EventBuses` | L-HOOK | platform event hooks | audit + both compile nodes |
| `FoodCompat` | V-ITEM | food/item definitions | audit + both compile nodes |
| `GameHooksCompat` | L-HOOK | knowledge/gameplay hooks | audit + both compile nodes |
| `IgniteCompat` | V-WORLD | ignition consumers | audit + both compile nodes |
| `IMCCompat` | L-BUS, L-REG | integration bootstrap | audit + both compile nodes |
| `InteractiveBlockCompat` | V-WORLD | interactive blocks | audit + both compile nodes |
| `ItemDataCompat` | V-DATA, V-ITEM | item persistent data | audit + both compile nodes |
| `ItemModelGen` | L-REG | item model datagen | audit + datagen compile |
| `ItemNameCompat` | V-ITEM | dynamic item names | audit + both compile nodes |
| `ItemTransferAttachmentCompat` | L-REG | spirit transfer storage | audit + both compile nodes |
| `KnowledgeSetupCompat` | L-BUS | `ModBootstrapCompat` | audit + both compile nodes |
| `LiquidAntimatterCompat` | L-BUS, L-REG | antimatter fluid/block | audit + both compile nodes |
| `LiquidCoraliumCompat` | L-BUS, L-REG | coralium fluid/block | audit + both compile nodes |
| `LoaderCompat` | L-REG | client optional-mod detection | audit + both compile nodes |
| `MachineCapabilityCompat` | L-BUS, L-REG | machine automation | audit + both compile nodes |
| `MachineItemCompat` | L-REG, V-ITEM | machine block items | audit + both compile nodes |
| `MaterializationRecipeSerializer` | L-REG, V-RECIPE | materialization recipes | audit + recipe compile |
| `MenuCompat` | L-REG, V-MENU | menu hosts | audit + both compile nodes |
| `MenuHostCapabilityCompat` | L-REG | item-hosted menus | audit + both compile nodes |
| `MerchantOfferCompat` | V-DATA | Remnant offers | audit + both compile nodes |
| `MobEffectCompat` | L-HOOK, V-ITEM | effect application/removal | audit + both compile nodes |
| `MobSpawnCompat` | V-ENTITY | entity spawn helpers | audit + both compile nodes |
| `ModBootstrapCompat` | L-ENTRY, L-BUS | loader mod discovery | audit + launch/compile nodes |
| `ModelPartRenderCompat` | V-RENDER | entity models | audit + client compile |
| `ModRegistrar` | L-BUS, L-REG | all content registrars | audit + both compile nodes |
| `NetworkChannel` | L-BUS, L-REG | `ACNetwork` | audit + both compile nodes |
| `ParticleCompat` | L-BUS, L-REG | particle bootstrap | audit + client compile |
| `PlayerDataCompat` | L-REG, V-DATA | necrodata/player hooks | audit + both compile nodes |
| `PotionBrewingCompat` | L-BUS, V-MENU | brewing bootstrap/machine | audit + both compile nodes |
| `RecipeCompat` | L-REG, V-RECIPE | machine recipes | audit + recipe compile |
| `RecipeDisplayCompat` | L-REG, V-RECIPE | recipe display integration | audit + recipe/client compile |
| `RecipeManagerCompatMixin` | V-RECIPE | mixin-configured armor recycling gate | audit + both compile nodes |
| `RecipeSerializerCompat` | L-REG, V-RECIPE | serializer registry | audit + recipe compile |
| `RendingRecipeSerializer` | L-REG, V-RECIPE | rending recipes | audit + recipe compile |
| `RitualTaskCompat` | L-HOOK | ritual scheduler bootstrap | audit + server compile |
| `SavedDataCompat` | V-DATA | saved world data | audit + both compile nodes |
| `ServerDataCompat` | V-DATA | server persistent-state consumers | audit + both compile nodes |
| `ShearableCompat` | L-REG | `RemnantMob`, `EvilAnimal` | audit + entity compile |
| `SideExecutor` | L-REG | client-only bootstrap | audit + dedicated-server compile |
| `SpawnCandidateCompat` | L-HOOK | natural spawn candidate hooks | audit + server compile |
| `SpawnEggCompat` | L-REG | entity spawn eggs | audit + both compile nodes |
| `SpawnPlacementCompat` | L-BUS, L-REG | entity registrars | audit + both compile nodes |
| `StructureCompat` | V-WORLD | structure generation | audit + both compile nodes |
| `StructureLootCompat` | V-WORLD | mineshaft/structure mixins | audit + both compile nodes |
| `StructureNbtCompat` | V-WORLD, V-DATA | structure template loading | audit + both compile nodes |
| `TamableCompat` | V-ENTITY | tameable entities | audit + both compile nodes |
| `TeleportCompat` | V-ENTITY, V-WORLD | portal/teleport consumers | audit + both compile nodes |
| `ToolCompat` | L-HOOK, V-ITEM | tools and tool actions | audit + both compile nodes |
| `TooltipCompat` | V-ITEM | ritual/staff/tablet tooltips | audit + client compile |
| `TransmutationRecipeSerializer` | L-REG, V-RECIPE | transmutation recipes | audit + recipe compile |
| `WitherSkullCompat` | V-ENTITY | projectile consumers | audit + both compile nodes |
| `WorldgenServerValidationCompat` | L-HOOK | `ModBootstrapCompat` | audit + server compile |

`AttributeCompat` was removed as an unreachable, unconsumed version fork. `ModBootstrapCompat`, `ClientItemPropertiesCompat`, and `ShearableCompat` replace the former loader imports in the main entry point, client item-property registration, `RemnantMob`, and `EvilAnimal` without changing gameplay behavior.

## Validation procedure

1. Run `node scripts/audit_compat.js`. Expected summary: `RR_COMPAT_AUDIT_OK`, zero business loader imports, and all platform fork symbols reachable.
2. Run `gradlew.bat stonecutterGenerate` to parse and preprocess both Stonecutter nodes.
3. Run the node compile tasks for `1.20.1-forge` and `1.21.1-neoforge`; loader/vanilla signature mistakes must fail their owning node.
4. Run `git -c core.whitespace=cr-at-eol diff --check` over the touched Java, Gradle, script and spec files.
5. If Gradle fails before task execution with `java.util.zip.ZipException: zip file is empty`, report that infrastructure failure verbatim; the standalone Node audit remains the authoritative static boundary check for that run.
