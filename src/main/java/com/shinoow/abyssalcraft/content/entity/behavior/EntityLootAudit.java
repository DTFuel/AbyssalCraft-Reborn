package com.shinoow.abyssalcraft.content.entity.behavior;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;

import com.shinoow.abyssalcraft.platform.ACRef;

/** Closed mapping from the 69 legacy entity loot tables to the modern entity catalog. */
public final class EntityLootAudit {

    public enum Resolution {
        DIRECT,
        CONDITIONAL,
        REPLACED,
        RETIRED
    }

    public record Entry(String legacyTable, Resolution resolution, String modernEntity, String discriminator) {}

    public record Summary(int direct, int conditional, int replaced, int retired) {}

    private static final List<Entry> ENTRIES = List.of(
        direct("abyssal_anti_zombie", "antiabyssalzombie"),
        conditional("abyssal_shoggoth", "shoggoth", "type=abyssal"),
        direct("abyssal_zombie", "abyssalzombie"),
        direct("anti_bat", "antibat"),
        direct("anti_chicken", "antichicken"),
        direct("anti_cow", "anticow"),
        direct("anti_creeper", "anticreeper"),
        direct("anti_ghoul", "antighoul"),
        direct("anti_pig", "antipig"),
        direct("anti_player", "antiplayer"),
        direct("anti_skeleton", "antiskeleton"),
        direct("anti_spider", "antispider"),
        direct("anti_zombie", "antizombie"),
        replaced("asorah", "dragonboss", "legacy dragon-boss loot id"),
        direct("chagaroth", "chagaroth"),
        replaced("coralium_infested_squid", "coraliumsquid", "clean registry id"),
        direct("demon_chicken", "demon_chicken"),
        direct("demon_cow", "demon_cow"),
        direct("demon_pig", "demon_pig"),
        direct("demon_sheep", "demon_sheep"),
        direct("depths_ghoul", "depths_ghoul"),
        conditional("depths_ghoul_orange", "depths_ghoul", "variant=orange"),
        conditional("depths_ghoul_pete", "depths_ghoul", "variant=pete"),
        conditional("depths_ghoul_wilson", "depths_ghoul", "variant=wilson"),
        conditional("dreaded_shoggoth", "shoggoth", "type=dreaded"),
        direct("dreadguard", "dreadguard"),
        direct("dreadling", "dreadling"),
        replaced("dread_ghoul", "dreaded_ghoul", "clean registry id"),
        replaced("dread_spawn", "dreadspawn", "clean registry id"),
        direct("evil_chicken", "evil_chicken"),
        direct("evil_cow", "evil_cow"),
        direct("evil_pig", "evil_pig"),
        direct("evil_sheep", "evil_sheep"),
        replaced("fist_of_chagaroth", "chagarothfist", "clean registry id"),
        direct("ghoul", "ghoul"),
        conditional("greater_abyssal_shoggoth", "greater_shoggoth", "type=abyssal"),
        conditional("greater_dreaded_shoggoth", "greater_shoggoth", "type=dreaded"),
        replaced("greater_dread_spawn", "greaterdreadspawn", "clean registry id"),
        conditional("greater_omothol_shoggoth", "greater_shoggoth", "type=omothol"),
        conditional("greater_shadow_shoggoth", "greater_shoggoth", "type=shadow"),
        direct("greater_shoggoth", "greater_shoggoth"),
        direct("jzahar", "jzahar"),
        conditional("lesser_abyssal_shoggoth", "lesser_shoggoth", "type=abyssal"),
        replaced("lesser_dreadbeast", "lesserdreadbeast", "clean registry id"),
        conditional("lesser_dreaded_shoggoth", "lesser_shoggoth", "type=dreaded"),
        conditional("lesser_omothol_shoggoth", "lesser_shoggoth", "type=omothol"),
        conditional("lesser_shadow_shoggoth", "lesser_shoggoth", "type=shadow"),
        direct("lesser_shoggoth", "lesser_shoggoth"),
        replaced("minion_of_the_gatekeeper", "jzaharminion", "clean registry id"),
        direct("omothol_ghoul", "omothol_ghoul"),
        conditional("omothol_shoggoth", "shoggoth", "type=omothol"),
        direct("remnant", "remnant"),
        conditional("remnant_banker", "remnant", "profession=banker"),
        conditional("remnant_blacksmith", "remnant", "profession=blacksmith"),
        conditional("remnant_butcher", "remnant", "profession=butcher"),
        conditional("remnant_librarian", "remnant", "profession=librarian"),
        conditional("remnant_master_blacksmith", "remnant", "profession=master_blacksmith"),
        conditional("remnant_priest", "remnant", "profession=priest"),
        replaced("sacthoth", "shadowboss", "clean registry id"),
        replaced("shadow_beast", "shadowbeast", "clean registry id"),
        replaced("shadow_creature", "shadowcreature", "clean registry id"),
        direct("shadow_ghoul", "shadow_ghoul"),
        replaced("shadow_monster", "shadowmonster", "clean registry id"),
        conditional("shadow_shoggoth", "shoggoth", "type=shadow"),
        direct("shoggoth", "shoggoth"),
        replaced("shub_offspring", "shuboffspring", "clean registry id"),
        replaced("skeleton_goliath", "gskeleton", "clean registry id"),
        replaced("spawn_of_chagaroth", "chagarothspawn", "clean registry id"),
        replaced("spectral_dragon", "dragonminion", "legacy dragon-minion loot id")
    );

    private static final Set<String> BASELINE_UNIQUE_TABLES = Set.of(
        "antiabyssalzombie", "antichicken", "anticow", "anticreeper", "antighoul", "antipig",
        "antiskeleton", "antispider", "antizombie", "demon_chicken", "demon_cow", "demon_pig",
        "demon_sheep", "evil_chicken", "evil_cow", "evil_pig", "evil_sheep", "ghoul",
        "depths_ghoul", "dreaded_ghoul", "omothol_ghoul", "shadow_ghoul", "lesser_shoggoth",
        "shoggoth", "greater_shoggoth", "abyssalzombie", "coraliumsquid", "dreadling",
        "dreadspawn", "greaterdreadspawn", "lesserdreadbeast", "shadowcreature", "shadowmonster",
        "shadowbeast"
    );

    private static final Set<String> SPAWN_MODIFIERS = Set.of(
        "abyssal_wasteland", "coralium_squid", "dark_realm", "dreadlands_base", "dreadlands_forest",
        "dreadlands_mountains", "ghoul", "omothol", "shoggoth"
    );

    private static final Map<String, String> MODERN_ALIASES = modernAliasesInternal();

    private static final Set<String> LOGICAL_TABLES = logicalTablesInternal();

    private static final Set<String> EMPTY_LOGICAL_TABLES = Set.of(
        "anti_bat", "antibat", "anti_player", "antiplayer",
        "fist_of_chagaroth", "chagarothfist", "shub_offspring", "shuboffspring"
    );

    private EntityLootAudit() {}

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static Set<String> baselineUniqueTables() {
        return BASELINE_UNIQUE_TABLES;
    }

    public static Set<String> spawnModifiers() {
        return SPAWN_MODIFIERS;
    }

    public static Map<String, String> modernAliases() {
        return MODERN_ALIASES;
    }

    public static Set<String> logicalTables() {
        return LOGICAL_TABLES;
    }

    public static Set<String> emptyLogicalTables() {
        return EMPTY_LOGICAL_TABLES;
    }

    public static Summary validate() {
        require(ENTRIES.size() == 69, "legacy entity loot audit must contain 69 entries");
        require(new HashSet<>(ENTRIES.stream().map(Entry::legacyTable).toList()).size() == ENTRIES.size(),
            "legacy entity loot audit contains duplicate table ids");
        require(BASELINE_UNIQUE_TABLES.size() == 34, "modern entity loot baseline must contain 34 tables");
        require(SPAWN_MODIFIERS.size() == 9, "spawn modifier mirror baseline must contain 9 ids");
        require(MODERN_ALIASES.size() == 28, "modern entity loot alias count must be 28");
        require(LOGICAL_TABLES.size() == 97, "modern entity loot logical table count must be 97");
        require(EMPTY_LOGICAL_TABLES.size() == 8 && LOGICAL_TABLES.containsAll(EMPTY_LOGICAL_TABLES),
            "modern empty entity loot table contract changed");

        Map<Resolution, Integer> counts = new EnumMap<>(Resolution.class);
        for (Resolution resolution : Resolution.values()) counts.put(resolution, 0);
        for (Entry entry : ENTRIES) {
            counts.compute(entry.resolution(), (ignored, count) -> count + 1);
            if (entry.resolution() != Resolution.RETIRED) {
                require(entry.modernEntity() != null && !entry.modernEntity().isBlank(),
                    "non-retired loot entry lacks a modern entity: " + entry.legacyTable());
                require(BuiltInRegistries.ENTITY_TYPE.containsKey(ACRef.id(entry.modernEntity())),
                    "loot entry targets an unregistered entity: " + entry.legacyTable() + " -> " + entry.modernEntity());
            }
        }
        for (String table : BASELINE_UNIQUE_TABLES) {
            require(BuiltInRegistries.ENTITY_TYPE.containsKey(ACRef.id(table)),
                "baseline loot table targets an unregistered entity: " + table);
        }
        return new Summary(counts.get(Resolution.DIRECT), counts.get(Resolution.CONDITIONAL),
            counts.get(Resolution.REPLACED), counts.get(Resolution.RETIRED));
    }

    private static Entry direct(String legacyTable, String modernEntity) {
        return new Entry(legacyTable, Resolution.DIRECT, modernEntity, "");
    }

    private static Entry conditional(String legacyTable, String modernEntity, String discriminator) {
        return new Entry(legacyTable, Resolution.CONDITIONAL, modernEntity, discriminator);
    }

    private static Entry replaced(String legacyTable, String modernEntity, String reason) {
        return new Entry(legacyTable, Resolution.REPLACED, modernEntity, reason);
    }

    private static Map<String, String> modernAliasesInternal() {
        Map<String, String> aliases = new HashMap<>();
        for (Entry entry : ENTRIES) {
            if (entry.resolution() == Resolution.CONDITIONAL
                    || entry.resolution() == Resolution.RETIRED
                    || entry.legacyTable().equals(entry.modernEntity())) continue;
            aliases.putIfAbsent(entry.modernEntity(), entry.legacyTable());
        }
        return Map.copyOf(aliases);
    }

    private static Set<String> logicalTablesInternal() {
        Set<String> tables = new HashSet<>(ENTRIES.stream().map(Entry::legacyTable).toList());
        tables.addAll(MODERN_ALIASES.keySet());
        return Set.copyOf(tables);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}