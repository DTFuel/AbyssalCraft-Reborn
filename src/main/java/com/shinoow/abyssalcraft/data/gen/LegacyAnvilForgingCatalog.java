package com.shinoow.abyssalcraft.data.gen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Frozen catalog of the 74 anvil forgings registered by 1.12.2 AbyssalCrafting. */
public final class LegacyAnvilForgingCatalog {

    public static final int SOURCE_COUNT = 74;

    public enum Status { MIGRATED, RETIRED }

    public record Entry(int sourceOrdinal, String legacyKey, Status status, String recipeId,
                        String input1, String input2, String output, int price,
                        String forgingType, String reason) {
        public boolean executable() {
            return status == Status.MIGRATED;
        }
    }

    private static final List<Entry> ENTRIES = build();

    private LegacyAnvilForgingCatalog() {}

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static Map<Status, Integer> counts() {
        EnumMap<Status, Integer> counts = new EnumMap<>(Status.class);
        for (Status status : Status.values()) counts.put(status, 0);
        ENTRIES.forEach(entry -> counts.merge(entry.status(), 1, Integer::sum));
        return Map.copyOf(counts);
    }

    public static Set<String> executableRecipeIds() {
        return ENTRIES.stream().filter(Entry::executable).map(Entry::recipeId)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static List<Entry> build() {
        List<Entry> entries = new ArrayList<>();
        for (String family : List.of("charm", "cthulhucharm", "hasturcharm", "jzaharcharm",
                "azathothcharm", "nyarlathotepcharm", "yogsothothcharm", "shubniggurathcharm")) {
            charm(entries, family, "range", "minecraft:arrow");
            charm(entries, family, "power", "minecraft:gunpowder");
            charm(entries, family, "duration", "minecraft:redstone");
        }

        energyFamily(entries, "energy_pedestal", List.of(
            "energypedestal", "overworld_energy_pedestal", "abyssal_wasteland_energy_pedestal",
            "dreadlands_energy_pedestal", "omothol_energy_pedestal"), true);
        energyFamily(entries, "energy_collector", List.of(
            "energycollector", "overworld_energy_collector", "abyssal_wasteland_energy_collector",
            "dreadlands_energy_collector", "omothol_energy_collector"), true);
        energyFamily(entries, "energy_container", List.of(
            "energycontainer", "overworld_energy_container", "abyssal_wasteland_energy_container",
            "dreadlands_energy_container", "omothol_energy_container"), true);
        energyFamily(entries, "energy_relay", List.of(
            "energyrelay", "overworld_energy_relay", "abyssal_wasteland_energy_relay",
            "dreadlands_energy_relay", "omothol_energy_relay"), true);
        energyFamily(entries, "sacrificial_altar", List.of(
            "sacrificialaltar", "overworld_sacrificial_altar", "abyssal_wasteland_sacrificial_altar",
            "dreadlands_sacrificial_altar", "omothol_sacrificial_altar"), true);

        if (entries.size() != SOURCE_COUNT) {
            throw new IllegalStateException("legacy anvil source count changed: " + entries.size());
        }
        Set<String> keys = new HashSet<>();
        Set<String> recipeIds = new HashSet<>();
        for (Entry entry : entries) {
            if (!keys.add(entry.legacyKey())) {
                throw new IllegalStateException("duplicate legacy anvil key " + entry.legacyKey());
            }
            if (entry.executable() && !recipeIds.add(entry.recipeId())) {
                throw new IllegalStateException("duplicate anvil recipe id " + entry.recipeId());
            }
        }
        return List.copyOf(entries);
    }

    private static void charm(List<Entry> entries, String family, String amplifier, String material) {
        String output = family + "_" + amplifier;
        entries.add(new Entry(entries.size() + 1, family + "+" + amplifier, Status.MIGRATED,
            "abyssalcraft:anvil/" + output, "abyssalcraft:" + family, material,
            "abyssalcraft:" + output, 1, "ritual_charm", ""));
    }

    private static void energyFamily(List<Entry> entries, String family, List<String> tiers,
                                     boolean migrated) {
        String[] rings = {"ring_overworld", "ring_abyssal_wasteland", "ring_dreadlands", "ring_omothol"};
        int[][] upgrades = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 2}, {1, 3}, {1, 4},
            {2, 3}, {2, 4}, {3, 4}};
        for (int[] upgrade : upgrades) {
            String input = tiers.get(upgrade[0]);
            String output = tiers.get(upgrade[1]);
            String ring = rings[upgrade[1] - 1];
            Status status = migrated ? Status.MIGRATED : Status.RETIRED;
            String recipeId = migrated ? "abyssalcraft:anvil/" + output + "_from_" + input : "";
            String reason = migrated ? ""
                : "legacy tiered sacrificial altars were removed from the modern content registry";
            entries.add(new Entry(entries.size() + 1, family + ":" + input + "+" + ring,
                status, recipeId, "abyssalcraft:" + input, "abyssalcraft:" + ring,
                "abyssalcraft:" + output, 1, "default", reason));
        }
    }
}
