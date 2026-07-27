# Advancement and Plugin API

## Scope

RR-ADV-API provides three production contracts:

- nine AbyssalCraft progression advancements and their Necronomicon progression entries;
- `/acunlockallknowledge` permission, synchronization, and persistence semantics;
- a versioned third-party entity integration API, plus a Forge compatibility bridge for five legacy IMC keys.

## Plugin discovery

Implement `com.shinoow.abyssalcraft.integration.api.IACPlugin` and declare the provider in:

`META-INF/services/com.shinoow.abyssalcraft.integration.api.IACPlugin`

The file contains one fully-qualified provider class name per line. Providers need a public no-argument constructor. They are discovered during AbyssalCraft construction and applied in deterministic plugin-ID order when the first server reaches `ServerAboutToStartEvent`.

`IACPlugin.id()` must be globally unique. `apiVersion()` must equal `IACPlugin.API_VERSION`. One broken provider is logged and rolled back without changing registrations from earlier valid providers. Registration is closed after the immutable server snapshot is published.

Explicit registration through `ACPluginRegistry.register(IACPlugin)` is available before the first server starts. ServiceLoader is preferred for independently packaged integrations.

## Typed context

`IACPlugin.register(ACPluginContext)` may call:

- `registerShoggothFood(EntityType<? extends LivingEntity>)`
- `registerDreadPlagueImmunity(EntityType<? extends LivingEntity>)`
- `registerDreadPlagueCarrier(EntityType<? extends LivingEntity>)`
- `registerCoraliumPlagueImmunity(EntityType<? extends LivingEntity>)`
- `registerCoraliumPlagueCarrier(EntityType<? extends LivingEntity>)`

A carrier is also immune to its plague. Unknown or unregistered entity types are rejected. The context is writable only during the provider callback.

## Forge legacy IMC

Forge 1.20.1 consumes these keys during `InterModProcessEvent`:

| Key | Accepted payload |
|---|---|
| `shoggothFood` | `EntityType`, `ResourceLocation`, or namespaced `String` |
| `addDreadPlagueImmunity` | same |
| `addDreadPlagueCarrier` | same |
| `addCoraliumPlagueImmunity` | same |
| `addCoraliumPlagueCarrier` | same |

Example send call:

```java
InterModComms.sendTo("abyssalcraft", "shoggothFood", () -> EntityType.ZOMBIE);
```

NeoForge 21.1 no longer exposes loader IMC. NeoForge integrations use ServiceLoader or explicit typed registration.

## Retired IMC keys

The following runtime-mutation keys are intentionally rejected with a migration warning:

- `addCrystal`
- `addCrystallization`
- `addSingleCrystallization`
- `addOredictCrystallization`
- `addSingleOredictCrystallization`
- `addTransmutation`
- `addOredictTransmutation`
- `addMaterialization`
- `addGhoulArmor`
- `addGhoulHelmet`
- `addGhoulChestplate`
- `addGhoulLeggings`
- `addGhoulBoots`

Machine recipes belong in datapacks. Ghoul armor textures belong in resource packs. This avoids mutable runtime recipe and rendering registries that cannot reload consistently on both supported loaders.

## Advancement knowledge

The progression chain is:

`root -> mine_abyssalnite -> mine_coralium -> shadow_gems -> mine_abyssal_coralium -> mine_abyssal_ores -> mine_dreadlands_ores -> dreadium -> ethaxium`

Completion is observed through each loader's `AdvancementEarnEvent`. The full advancement ID is stored once in the player's `advancementTriggers` necrodata list and synchronized with knowledge message type 7 plus a full snapshot. Existing players are backfilled from vanilla advancement progress when they log in.

These entries form a separate Necronomicon Progression category. They do not impersonate the 42 legacy biome, dimension, entity, plague, or book-tier researches.

## Compatibility

- Forge 1.20.1 reads `data/abyssalcraft/advancements/` and object-form inventory predicates.
- NeoForge 1.21.1 reads `data/abyssalcraft/advancement/`, `display.icon.id`, and string-form inventory predicates.
- The permanent datagen gate prints `RR_ADV_API_SELF_TEST_OK` after validating both schemas, all item references, parent links, OR requirements, plugin rollback, and command toggle semantics.