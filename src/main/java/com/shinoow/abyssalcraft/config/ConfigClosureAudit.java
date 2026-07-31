package com.shinoow.abyssalcraft.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Definition-to-production-consumer closure for every registered configuration key. */
public final class ConfigClosureAudit {

    public static final int DEFINED = 146;

    public record Consumer(String owner, String symbol) {}

    private static final Map<String, String> BLOCKED = Map.of();
    private static final Map<String, Consumer> CONSUMERS = consumers();

    private ConfigClosureAudit() {}

    public static Map<String, Consumer> consumersByKey() {
        return CONSUMERS;
    }

    public static Map<String, Map<String, String>> consumersByOwner() {
        Map<String, Map<String, String>> owners = new LinkedHashMap<>();
        CONSUMERS.forEach((key, consumer) -> owners
            .computeIfAbsent(consumer.owner(), ignored -> new LinkedHashMap<>())
            .put(key, consumer.symbol()));
        owners.replaceAll((owner, entries) -> Map.copyOf(entries));
        return Map.copyOf(owners);
    }

    public static Set<String> blockedKeys() {
        return BLOCKED.keySet();
    }

    public static Map<String, String> blockedByKey() {
        return BLOCKED;
    }

    public static int blockedCount() {
        return blockedKeys().size();
    }

    private static Map<String, Consumer> consumers() {
        Map<String, Consumer> entries = new LinkedHashMap<>();
        addWorldConsumers(entries);
        addEntityConsumers(entries);
        addClientRitualSpellConsumers(entries);
        addContentSystemConsumers(entries);
        if (entries.size() + BLOCKED.size() != DEFINED) {
            throw new IllegalStateException("config consumer map changed: " + entries.size());
        }
        return Map.copyOf(entries);
    }

    private static void addWorldConsumers(Map<String, Consumer> entries) {
        put(entries, "RR-WORLD", "shouldSpread", "LiquidCoraliumBlock#transmuteNeighbors");
        put(entries, "RR-WORLD", "destroyOcean", "LiquidCoraliumBlock#transmute");
        put(entries, "RR-WORLD", "keepLoaded1", "DimensionLoadingCompat#sync(abyssal_wasteland)");
        put(entries, "RR-WORLD", "keepLoaded2", "DimensionLoadingCompat#sync(dreadlands)");
        put(entries, "RR-WORLD", "keepLoaded3", "DimensionLoadingCompat#sync(omothol)");
        put(entries, "RR-WORLD", "keepLoaded4", "DimensionLoadingCompat#sync(dark_realm)");
        put(entries, "RR-WORLD", "startDimension", "DimensionDataRegistry#configuredStartDimension");
        put(entries, "RR-WORLD", "darklandsRegionWeight", "DarklandsWorldgenCompat#registerRegions");
        put(entries, "RR-WORLD", "worldgenConfigMigrationVersion", "WorldgenConfigMigration#migrate");
        put(entries, "RR-WORLD", "generateDarklandsStructures", "WorldgenConfigGate#allowsStructure");
        put(entries, "RR-WORLD", "generateShoggothLairs", "WorldgenConfigGate#allowsStructure");
        put(entries, "RR-WORLD", "generateAbyssalWastelandPillars", "WorldgenConfigGate#allowsPlacedFeature");
        put(entries, "RR-WORLD", "generateAbyssalWastelandRuins", "WorldgenConfigGate#allowsStructure");
        put(entries, "RR-WORLD", "generateAntimatterLake", "WorldgenConfigGate#allowsPlacedFeature");
        put(entries, "RR-WORLD", "generateCoraliumLake", "WorldgenConfigGate#allowsPlacedFeature");
        put(entries, "RR-WORLD", "generateDreadlandsStalagmite", "StalagmiteFeature#place");
        put(entries, "RR-WORLD", "generateStatuesInLairs", "LegacyTemplatePiece#handleDataMarker");
        put(entries, "RR-WORLD", "generateGraveyards", "WorldgenConfigGate#allowsStructure");
        put(entries, "RR-WORLD", "generateOmotholStructures", "WorldgenConfigGate#allowsStructure");
        put(entries, "RR-WORLD", "useAmplifiedWorldType", "ConfigurableAmplifiedOffset#compute");
        put(entries, "RR-WORLD", "generateCoraliumOre", "CoraliumSwampOreFeature#place");
        put(entries, "RR-WORLD", "generateNitreOre", "WorldgenConfigGate#allowsPlacedFeature(ore_nitre)");
        put(entries, "RR-WORLD", "generateAbyssalniteOre", "WorldgenConfigGate#allowsPlacedFeature(ore_abyssalnite)");
        put(entries, "RR-WORLD", "generateAbyssalCoraliumOre", "WorldgenConfigGate#allowsPlacedFeature(ore_abyssal_coralium)");
        put(entries, "RR-WORLD", "generateDreadlandsAbyssalniteOre", "WorldgenConfigGate#allowsPlacedFeature(ore_dreadlands_abyssalnite)");
        put(entries, "RR-WORLD", "generateDreadedAbyssalniteOre", "WorldgenConfigGate#allowsPlacedFeature(ore_dreaded_abyssalnite)");
        put(entries, "RR-WORLD", "generateAbyssalIronOre", "WorldgenConfigGate#allowsPlacedFeature(ore_abyssal_iron)");
        put(entries, "RR-WORLD", "generateAbyssalGoldOre", "WorldgenConfigGate#allowsPlacedFeature(ore_abyssal_gold)");
        put(entries, "RR-WORLD", "generateAbyssalDiamondOre", "WorldgenConfigGate#allowsPlacedFeature(ore_abyssal_diamond)");
        put(entries, "RR-WORLD", "generateAbyssalNitreOre", "WorldgenConfigGate#allowsPlacedFeature(ore_abyssal_nitre)");
        put(entries, "RR-WORLD", "generatePearlescentCoraliumOre", "WorldgenConfigGate#allowsPlacedFeature(ore_pearlescent_coralium)");
        put(entries, "RR-WORLD", "generateLiquifiedCoraliumOre", "WorldgenConfigGate#allowsPlacedFeature(ore_liquified_coralium)");
        put(entries, "RR-WORLD", "shoggothLairSpawnRate", "WorldgenConfigGate#passesLairPlacement(SHOGGOTH_PIT)");
        put(entries, "RR-WORLD", "shoggothLairSpawnRateRivers", "WorldgenConfigGate#passesLairPlacement(SHOGGOTH_PIT_RIVER)");
        put(entries, "RR-WORLD", "shoggothLairGenerationDistance", "WorldgenConfigGate#lairChunkInterval");
        put(entries, "RR-WORLD", "darkShrineSpawnRate", "WorldgenConfigGate#allowsDarkShrine");
        put(entries, "RR-WORLD", "darkRitualGroundsSpawnRate", "WorldgenConfigGate#allowsStructure(DARK_RITUAL_GROUNDS)");
        put(entries, "RR-WORLD", "graveyardGenerationDistance", "WorldgenConfigGate#allowsStructure(GRAVEYARD)");
        put(entries, "RR-WORLD", "graveyardGenerationChance", "WorldgenConfigGate#allowsStructure(GRAVEYARD)");
        put(entries, "RR-WORLD", "breakLogic", "LiquidCoraliumBlock#transmuteNeighbors");
        put(entries, "RR-WORLD", "oreGenerationDimensionBlacklist", "PlacedFeatureMixin#abyssalcraft$applyConfigGate");
        put(entries, "RR-WORLD", "structureGenerationDimensionBlacklist", "StructureStartMixin#abyssalcraft$beginPalette");
        put(entries, "RR-WORLD", "coraliumOreGeneration", "CoraliumSwampOreFeature#place");
        put(entries, "RR-KNOWLEDGE", "dimensionBookTypeMappings", "DimensionDataRegistry#requiredBookType");
    }

    private static void addEntityConsumers(Map<String, Consumer> entries) {
        put(entries, "RR-ENTITY", "hardcoreMode", "BossMob#applyHardcoreAttributes");
        put(entries, "RR-ENTITY", "damageAmpl", "DragonFlightController#attackHead");
        put(entries, "RR-ENTITY", "mimicFire", "DemonAnimal#spreadFire");
        put(entries, "RR-ENTITY", "demonAnimalFire", "DemonAnimal#isOnFire");
        put(entries, "RR-ENTITY", "evilAnimalSpawnWeight", "SpawnCandidateCompat#configuredCandidateSnapshot");
        put(entries, "RR-ENTITY", "demonAnimalSpawnWeight", "SpawnCandidateCompat#configuredCandidateSnapshot");
        put(entries, "RR-ENTITY", "depthsGhoulBiomeDictSpawn", "SpawnCandidateCompat#configuredCandidateSnapshot");
        put(entries, "RR-ENTITY", "abyssalZombieBiomeDictSpawn", "SpawnCandidateCompat#configuredCandidateSnapshot");
        put(entries, "RR-ENTITY", "darkOffspringSpawnWeight", "SpawnCandidateCompat#configuredCandidateSnapshot");
        put(entries, "RR-ENTITY", "demonAnimalsSpawnOnDeath", "EvilAnimal#die");
        put(entries, "RR-ENTITY", "evilAnimalNewMoonSpawning", "EvilAnimal#checkSpawnRules");
        put(entries, "RR-ENTITY", "antiPlayersPickupLoot", "AntiPlayer#registerGoals");
        put(entries, "RR-ENTITY", "dreadSpawnSpawnLimit", "LegacyHostileMob#aiStep");
        put(entries, "RR-ENTITY", "greaterDreadSpawnSpawnLimit", "LegacyHostileMob#aiStep");
        put(entries, "RR-ENTITY", "jzaharHealingPace", "JzaharBoss#regenerate");
        put(entries, "RR-ENTITY", "jzaharHealingAmount", "JzaharBoss#regenerate");
        put(entries, "RR-ENTITY", "chagarothHealingPace", "ChagarothBoss#regenerate");
        put(entries, "RR-ENTITY", "chagarothHealingAmount", "ChagarothBoss#regenerate");
        put(entries, "RR-ENTITY", "sacthothHealingPace", "SacthothBoss#regenerate");
        put(entries, "RR-ENTITY", "sacthothHealingAmount", "SacthothBoss#regenerate");
        put(entries, "RR-ENTITY", "shoggothOoze", "AbstractShoggoth#spreadOoze");
        put(entries, "RR-ENTITY", "oozeExpire", "ShoggothOozeBlock#randomTick");
        put(entries, "RR-ENTITY", "consumeItems", "AbstractShoggoth#consumeDroppedItems");
        put(entries, "RR-ENTITY", "shieldsBlockAcid", "AcidProjectile#onHitEntity");
        put(entries, "RR-ENTITY", "acidResistanceHardness", "AcidProjectile#onHitBlock");
        put(entries, "RR-ENTITY", "acidSpitFrequency", "AbstractShoggoth#performRangedAttack");
        put(entries, "RR-ENTITY", "monolithBuildingCooldown", "ShoggothBuildMonolithGoal#canUse");
        put(entries, "RR-ENTITY", "biomassPlayerDistance", "ShoggothBiomassBlock#tick");
        put(entries, "RR-ENTITY", "biomassMaxSpawn", "ShoggothBiomassBlock#tick");
        put(entries, "RR-ENTITY", "biomassCooldown", "ShoggothBiomassBlock#tick");
        put(entries, "RR-ENTITY", "biomassShoggothDistance", "ShoggothBiomassBlock#tick");
        put(entries, "RR-ENTITY", "nuclearAntimatterExplosions", "AntiEntity#collideWithOpposite");
        put(entries, "RR-ENTITY", "jzaharBreaksFourthWall", "JzaharBoss#broadcastDialog");
        put(entries, "RR-ENTITY", "odbExplosionSize", "PrimedODB#explode");
        put(entries, "RR-ENTITY", "antimatterExplosionSize", "AntiEntity#collideWithOpposite");
        put(entries, "RR-ENTITY", "no_acid_breaking_blocks", "AcidProjectile#onHitBlock");
        put(entries, "RR-ENTITY", "no_spectral_dragons", "SpawnCandidateCompat#candidateSnapshot");
        put(entries, "RR-ENTITY", "no_projectile_damage_immunity", "AbstractShoggoth#isInvulnerableTo");
        put(entries, "RR-ENTITY", "no_black_holes", "JzaharBoss#serverAiStep");
        put(entries, "RR-ENTITY", "ghouls_burn", "AbstractGhoul#aiStep");
        put(entries, "RR-ENTITY", "mobItemPickupBlacklist", "AbyssalZombie#canHoldItem");
    }

    private static void addClientRitualSpellConsumers(Map<String, Consumer> entries) {
        put(entries, "RR-CLIENT-FX", "particleBlock", "ResearchTableBlock#animateTick");
        put(entries, "RR-CLIENT-FX", "particleEntity", "ShadowEntityEffects#tick");
        put(entries, "RR-CLIENT-FX", "darkRealmSmokeParticles", "ClientFxEffects#entityTick");
        put(entries, "RR-CLIENT-FX", "depthsHelmetOverlayOpacity", "ClientFxEffects#renderHelmetOverlay");
        put(entries, "RR-CLIENT-FX", "shoggothGlowingEyes", "ShoggothRenderer#addRenderLayers");
        put(entries, "RR-CLIENT-FX", "hcdarkness_aw", "ACDimensionEffects#isFoggyAt");
        put(entries, "RR-CLIENT-FX", "hcdarkness_dl", "ACDimensionEffects#isFoggyAt");
        put(entries, "RR-CLIENT-FX", "hcdarkness_omt", "ACDimensionEffects#isFoggyAt");
        put(entries, "RR-CLIENT-FX", "hcdarkness_dr", "ACDimensionEffects#isFoggyAt");

        put(entries, "RR-RITUAL", "corruptionRitualRange", "RitualBehaviors#biomeTaskRange");
        put(entries, "RR-RITUAL", "cleansingRitualRange", "RitualBehaviors#biomeTaskRange");
        put(entries, "RR-RITUAL", "purgingRitualRange", "RitualBehaviors#biomeTaskRange");
        put(entries, "RR-RITUAL", "enchantmentMaxLevel", "MassEnchantBehavior#apply");
        put(entries, "RR-RITUAL", "enchantBooks", "MassEnchantBehavior#apply");
        put(entries, "RR-RITUAL", "curingRitualRange", "RitualBehaviors#biomeTaskRange");
        put(entries, "RR-RITUAL", "infestingRitualRange", "RitualBehaviors#biomeTaskRange");
        put(entries, "RR-RITUAL", "enchantMergedBooks", "MassEnchantBehavior#apply");
        put(entries, "RR-RITUAL", "no_disruptions",
            "RitualAltarBlockEntity#completeRitual; DeityStatueBlockEntity#triggerDisruption");

        put(entries, "RR-SPELL", "entropy_spell", "SpellAvailability#isEnabled(entropy)");
        put(entries, "RR-SPELL", "life_drain_spell", "SpellAvailability#isEnabled(lifedrain)");
        put(entries, "RR-SPELL", "mining_spell", "SpellAvailability#isEnabled(mining)");
        put(entries, "RR-SPELL", "grasp_of_cthulhu_spell", "SpellAvailability#isEnabled(graspofcthulhu)");
        put(entries, "RR-SPELL", "invisibility_spell", "SpellAvailability#isEnabled(invisibility)");
        put(entries, "RR-SPELL", "detachment_spell", "SpellAvailability#isEnabled(detachment)");
        put(entries, "RR-SPELL", "steal_vigor_spell", "SpellAvailability#isEnabled(stealvigor)");
        put(entries, "RR-SPELL", "sirens_song_spell", "SpellAvailability#isEnabled(sirenssong)");
        put(entries, "RR-SPELL", "undeath_to_dust_spell", "SpellAvailability#isEnabled(undeathtodust)");
        put(entries, "RR-SPELL", "ooze_removal_spell", "SpellAvailability#isEnabled(oozeremoval)");
        put(entries, "RR-SPELL", "teleport_hostile_spell", "SpellAvailability#isEnabled(teleporthostiles)");
        put(entries, "RR-SPELL", "floating_spell", "SpellAvailability#isEnabled(floating)");
        put(entries, "RR-SPELL", "teleport_home_spell", "SpellAvailability#isEnabled(teleportHome)");
        put(entries, "RR-SPELL", "compass_spell", "SpellAvailability#isEnabled(compass)");
    }

    private static void addContentSystemConsumers(Map<String, Consumer> entries) {
        put(entries, "RR-CONTENT", "smeltingRecipes", "RecipeManagerMixin#abyssalcraft$filterArmorRecycling");
        put(entries, "RR-CONTENT", "purgeMobSpawns", "SpawnCandidateCompat#configuredCandidateSnapshot");
        put(entries, "RR-CONTENT", "armorPotionEffects", "ArmorEffects#inventoryTick");
        put(entries, "RR-CONTENT", "portalSpawnsNearPlayer", "DimensionPortal#tickPortalSpawn");
        put(entries, "RR-CONTENT", "showBossDialogs", "BossMob#broadcastDialog");
        put(entries, "RR-CONTENT", "lootTableContent", "ContentLootCompat#apply");
        put(entries, "RR-CONTENT", "nightVisionEverywhere", "ArmorEffects#inventoryTick");
        put(entries, "RR-CONTENT", "tombstoneMaxSpawn", "TombstoneBlockEntity#serverTick");
        put(entries, "RR-CONTENT", "tombstoneCooldown", "TombstoneBlockEntity#serverTick");
        put(entries, "RR-CONTENT", "tombstoneGhoulDistance", "TombstoneBlockEntity#serverTick");

        put(entries, "RR-SYSTEM", "shouldInfect", "DepthsGhoul#coraliumPlagueAttack");
        put(entries, "RR-SYSTEM", "antiItemDisintegration", "LiquidAntimatterBlock#entityInside");
        put(entries, "RR-SYSTEM", "syncDataOnBookOpening", "KnowledgeHooks#onBookOpened");
        put(entries, "RR-SYSTEM", "knowledgeSyncDelay", "KnowledgeHooks#onChangedDimension");
        put(entries, "RR-SYSTEM", "no_potion_clouds", "EffectHooks#createCarrierCloud/spreadDeath");
        put(entries, "RR-SYSTEM", "vanilla_handling", "PortalAnchorBlockEntity#toggle");
        put(entries, "RR-SYSTEM", "portalCooldown", "DimensionPortal#tick");
        put(entries, "RR-SYSTEM", "no_dreadlands_spread", "DreadPlagueSpread#tick");
        put(entries, "RR-SYSTEM", "no_odb_explosions", "OblivionDeathbombCoreBlock#use");
        put(entries, "RR-SYSTEM", "spirit_items", "SpiritItem#tick");
        put(entries, "RR-SYSTEM", "startDimensionColors", "DimensionPortal#getDimensionData");
        put(entries, "RR-SYSTEM", "interdimensionalCageBlacklist", "InterdimensionalCageMessage#handle");
        put(entries, "RR-SYSTEM", "itemTransportBlacklist", "SpiritItem#deliver");
        put(entries, "RR-SYSTEM", "dreadPlagueImmunityList", "EffectHooks#isDreadImmune");
        put(entries, "RR-SYSTEM", "dreadPlagueCarrierList", "EffectHooks#isDreadCarrier");
        put(entries, "RR-SYSTEM", "coraliumPlagueImmunityList", "EffectHooks#isCoraliumImmune");
        put(entries, "RR-SYSTEM", "coraliumPlagueCarrierList", "EffectHooks#isCoraliumCarrier");
        put(entries, "RR-SYSTEM", "demonAnimalTransformations", "EffectHooks#transformDemonAnimal");
        put(entries, "RR-SYSTEM", "blackHoleDimensionBlacklist", "BlackHole#tick");
    }

    private static void put(Map<String, Consumer> entries, String owner, String key, String symbol) {
        if (owner.isBlank() || symbol.isBlank()) {
            throw new IllegalArgumentException("blank config consumer for " + key);
        }
        if (entries.put(key, new Consumer(owner, symbol)) != null) {
            throw new IllegalStateException("duplicate config consumer key " + key);
        }
    }
}