package com.shinoow.abyssalcraft.config;

import com.shinoow.abyssalcraft.platform.ConfigCompat;

/** One-time migrations for persisted world generation configuration defaults. */
public final class WorldgenConfigMigration {

    static final int CURRENT_VERSION = 1;
    static final int LEGACY_DARKLANDS_REGION_WEIGHT = 2;

    private WorldgenConfigMigration() {}

    public static boolean migrate() {
        int version = ACConfig.worldgenConfigMigrationVersion.get();
        if (version >= CURRENT_VERSION) return false;
        int migratedWeight = migratedDarklandsRegionWeight(version, ACConfig.darklandsRegionWeight.get());
        set("worldgen.darklands_region_weight", migratedWeight);
        set("worldgen.config_migration_version", CURRENT_VERSION);
        return true;
    }

    static int migratedDarklandsRegionWeight(int version, int weight) {
        return version < CURRENT_VERSION && weight == LEGACY_DARKLANDS_REGION_WEIGHT ? 1 : weight;
    }

    private static void set(String path, int value) {
        ConfigCompat.entries().stream()
            .filter(entry -> entry.path().equals(path))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Missing config migration target " + path))
            .setParsed(value);
    }
}