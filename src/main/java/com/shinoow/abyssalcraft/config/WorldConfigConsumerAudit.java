package com.shinoow.abyssalcraft.config;

import java.util.LinkedHashMap;
import java.util.Map;

/** Permanent RR-WORLD definition-to-production-owner audit for the single integrator. */
public final class WorldConfigConsumerAudit {

    public enum Status { ACTIVE, PARTIAL, BLOCKED }

    public record Entry(Status status, String owner) {}

    private WorldConfigConsumerAudit() {}

    public static Map<String, Entry> entries() {
        Map<String, Entry> entries = new LinkedHashMap<>();
        active(entries, "shouldSpread", "LiquidCoraliumBlock#transmuteNeighbors");
        active(entries, "destroyOcean", "LiquidCoraliumBlock#transmute");
        active(entries, "keepLoaded1", "DimensionLoadingCompat ticket for abyssalcraft:abyssal_wasteland");
        active(entries, "keepLoaded2", "DimensionLoadingCompat ticket for abyssalcraft:dreadlands");
        active(entries, "keepLoaded3", "DimensionLoadingCompat ticket for abyssalcraft:omothol");
        active(entries, "keepLoaded4", "DimensionLoadingCompat ticket for abyssalcraft:dark_realm");
        active(entries, "startDimension", "DimensionDataRegistry#areDimensionsConnected legacy id map");
        active(entries, "darklandsRegionWeight", "DarklandsWorldgenCompat#registerRegions");
        active(entries, "worldgenConfigMigrationVersion", "WorldgenConfigMigration one-time legacy default migration");
        active(entries, "generateDarklandsStructures", "structures abyssalcraft:dark_shrine/dark_ritual_grounds via ACStructure");
        active(entries, "generateShoggothLairs", "structures abyssalcraft:shoggoth_pit/shoggoth_pit_river via ACStructure");
        active(entries, "generateAbyssalWastelandPillars", "placed feature abyssalcraft:abyssal_wasteland_pillars via PlacedFeatureMixin");
        active(entries, "generateAbyssalWastelandRuins", "structure abyssalcraft:abyruin via ACStructure");
        active(entries, "generateAntimatterLake", "placed feature abyssalcraft:lake_liquid_antimatter via PlacedFeatureMixin");
        active(entries, "generateCoraliumLake", "placed feature abyssalcraft:lake_liquid_coralium via PlacedFeatureMixin");
        active(entries, "generateDreadlandsStalagmite", "placed feature abyssalcraft:dreadlands_stalagmite via StalagmiteFeature");
        active(entries, "generateStatuesInLairs", "LegacyTemplatePiece#handleDataMarker functional/decorative statue selection");
        active(entries, "generateGraveyards", "structure abyssalcraft:graveyard via ACStructure");
        active(entries, "generateOmotholStructures", "structures abyssalcraft:omothol_city/temple/tower/storage and ethaxium_house");
        active(entries, "useAmplifiedWorldType", "ConfigurableAmplifiedOffset in all four dimension noise settings");
        active(entries, "generateCoraliumOre", "placed feature abyssalcraft:coralium_swamp_ores via CoraliumSwampOreFeature");
        activeOre(entries, "generateNitreOre", "ore_nitre");
        activeOre(entries, "generateAbyssalniteOre", "ore_abyssalnite");
        activeOre(entries, "generateAbyssalCoraliumOre", "ore_abyssal_coralium");
        activeOre(entries, "generateDreadlandsAbyssalniteOre", "ore_dreadlands_abyssalnite");
        activeOre(entries, "generateDreadedAbyssalniteOre", "ore_dreaded_abyssalnite");
        activeOre(entries, "generateAbyssalIronOre", "ore_abyssal_iron");
        activeOre(entries, "generateAbyssalGoldOre", "ore_abyssal_gold");
        activeOre(entries, "generateAbyssalDiamondOre", "ore_abyssal_diamond");
        activeOre(entries, "generateAbyssalNitreOre", "ore_abyssal_nitre");
        activeOre(entries, "generatePearlescentCoraliumOre", "ore_pearlescent_coralium");
        activeOre(entries, "generateLiquifiedCoraliumOre", "ore_liquified_coralium");
        active(entries, "shoggothLairSpawnRate", "WorldgenConfigGate SHOGGOTH_PIT combined density grid");
        active(entries, "shoggothLairSpawnRateRivers", "WorldgenConfigGate SHOGGOTH_PIT_RIVER combined density grid");
        active(entries, "shoggothLairGenerationDistance", "WorldgenConfigGate shared Shoggoth Lair spacing grid");
        active(entries, "darkShrineSpawnRate", "ACStructure dark_shrine legacy probability gate");
        active(entries, "darkRitualGroundsSpawnRate", "WorldgenConfigGate DARK_RITUAL_GROUNDS candidate gate");
        active(entries, "graveyardGenerationDistance", "ACStructure graveyard deterministic chunk-distance gate");
        active(entries, "graveyardGenerationChance", "ACStructure graveyard candidate gate");
        active(entries, "breakLogic", "LiquidCoraliumBlock upward transmutation gate");
        active(entries, "no_spectral_dragons", "SpawnCandidateCompat removes abyssalcraft:dragonminion");
        return Map.copyOf(entries);
    }

    public static void run() {
        Map<String, Entry> entries = entries();
        require(entries.size() == 41, "RR-WORLD key count changed: " + entries.size());
        require(entries.values().stream().allMatch(entry -> !entry.owner().isBlank()), "RR-WORLD owner is blank");
        long active = count(entries, Status.ACTIVE);
        long partial = count(entries, Status.PARTIAL);
        long blocked = count(entries, Status.BLOCKED);
        require(active + partial + blocked == entries.size(), "RR-WORLD status closure is incomplete");
        require(active == 41 && partial == 0 && blocked == 0,
            "RR-WORLD config closure regressed: active=" + active + " partial=" + partial + " blocked=" + blocked);
        System.out.printf("RR_WORLD_CONFIG_AUDIT_OK total=%d active=%d partial=%d blocked=%d%n",
            entries.size(), active, partial, blocked);
    }

    private static long count(Map<String, Entry> entries, Status status) {
        return entries.values().stream().filter(entry -> entry.status() == status).count();
    }

    private static void active(Map<String, Entry> entries, String key, String owner) {
        put(entries, key, Status.ACTIVE, owner);
    }

    private static void partial(Map<String, Entry> entries, String key, String owner) {
        put(entries, key, Status.PARTIAL, owner);
    }

    private static void blocked(Map<String, Entry> entries, String key, String owner) {
        put(entries, key, Status.BLOCKED, owner);
    }

    private static void activeOre(Map<String, Entry> entries, String key, String id) {
        active(entries, key, "configured/placed feature abyssalcraft:" + id + " via PlacedFeatureMixin");
    }

    private static void put(Map<String, Entry> entries, String key, Status status, String owner) {
        require(entries.put(key, new Entry(status, owner)) == null, "duplicate RR-WORLD key " + key);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}