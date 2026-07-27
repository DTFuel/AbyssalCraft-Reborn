package com.shinoow.abyssalcraft.system.spell;

import static com.shinoow.abyssalcraft.system.spell.SpellManifest.TargetType.BLOCK;
import static com.shinoow.abyssalcraft.system.spell.SpellManifest.TargetType.ENTITY;
import static com.shinoow.abyssalcraft.system.spell.SpellManifest.TargetType.ENTITY_OR_SELF;
import static com.shinoow.abyssalcraft.system.spell.SpellManifest.TargetType.SELF;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Ordered metadata for the fourteen spells registered by AbyssalCraft 1.12.2. */
public final class SpellManifestCatalog {

    private static final SpellIngredient EMPTY = SpellIngredient.empty();
    private static final List<SpellManifest> ENTRIES = build();
    private static final Map<String, SpellManifest> BY_ID = index();

    private SpellManifestCatalog() {}

    public static List<SpellManifest> entries() {
        return ENTRIES;
    }

    public static SpellManifest get(String idOrAlias) {
        return BY_ID.get(idOrAlias);
    }

    private static List<SpellManifest> build() {
        List<SpellManifest> spells = new ArrayList<>(14);
        spells.add(spell(1, "entropy", Set.of(), 0, 0, ScrollType.GREATER, BLOCK, false, 0x171f68,
            mc("coal"), ac("darkstone_cobblestone")));
        spells.add(spell(2, "lifedrain", Set.of("life_drain"), 0, 100, ScrollType.BASIC, ENTITY, true, 0xa00404,
            mc("apple")));
        spells.add(spell(3, "mining", Set.of(), 0, 500, ScrollType.BASIC, BLOCK, true, 0xc45b05,
            mc("wooden_pickaxe"), mc("stone_pickaxe"), mc("iron_pickaxe"), mc("golden_pickaxe"), mc("diamond_pickaxe")));
        spells.add(spell(4, "graspofcthulhu", Set.of("grasp_of_cthulhu"), 0, 20, ScrollType.LESSER, ENTITY, false, 0x2ba2ad,
            mc("cod")));
        spells.add(spell(5, "invisibility", Set.of(), 0, 500, ScrollType.BASIC, ENTITY_OR_SELF, true, 0xaeb1b7,
            mc("glass_bottle")));
        spells.add(spell(6, "detachment", Set.of(), 0, 100, ScrollType.MODERATE, ENTITY, true, 0x463faa,
            mc("iron_ingot")));
        spells.add(spell(7, "stealvigor", Set.of("steal_vigor"), 0, 500, ScrollType.BASIC, ENTITY, true, 0xdb3d3d,
            mc("beef")));
        spells.add(spell(8, "sirenssong", Set.of("sirens_song"), 0, 1000, ScrollType.LESSER, ENTITY, true, 0x1c8edb,
            mc("wheat")));
        spells.add(spell(9, "undeathtodust", Set.of("undeath_to_dust"), 0, 1000, ScrollType.MODERATE, ENTITY, true, 0x1a1b1c,
            mc("bone")));
        spells.add(spell(10, "oozeremoval", Set.of("ooze_removal"), 0, 100, ScrollType.BASIC, SELF, true, 0x000000,
            mc("sponge")));
        spells.add(spell(11, "teleporthostiles", Set.of("teleport_hostiles", "teleport_hosotiles"), 0, 10000,
            ScrollType.GREATER, ENTITY, true, 0x31227c, mc("sugar")));
        spells.add(spell(12, "floating", Set.of(), 3, 15, ScrollType.BASIC, SELF, false, 0xffffff,
            mc("feather"), mc("feather")));
        spells.add(spell(13, "teleportHome", Set.of("teleport_home"), 0, 1000, ScrollType.MODERATE, SELF, true, 0x0565ff,
            SpellIngredient.tag("minecraft:beds")));
        spells.add(spell(14, "compass", Set.of(), 0, 1000, ScrollType.BASIC, SELF, true, 0xa9b598,
            mc("compass")));
        return List.copyOf(spells);
    }

    private static SpellManifest spell(int order, String id, Set<String> aliases, int bookType,
                                       float energy, ScrollType scroll, SpellManifest.TargetType target,
                                       boolean charging, int color, SpellIngredient... reagents) {
        List<SpellIngredient> layout = new ArrayList<>(List.of(reagents));
        while (layout.size() < SpellManifest.REAGENT_SLOTS) layout.add(EMPTY);
        return new SpellManifest(order, id, aliases, bookType, energy, scroll, target,
            charging, false, color, layout, null, null, null);
    }

    private static SpellIngredient mc(String id) {
        return SpellIngredient.item("minecraft:" + id);
    }

    private static SpellIngredient ac(String id) {
        return SpellIngredient.item("abyssalcraft:" + id);
    }

    private static Map<String, SpellManifest> index() {
        Map<String, SpellManifest> result = new LinkedHashMap<>();
        for (SpellManifest spell : ENTRIES) {
            result.put(spell.id(), spell);
            for (String alias : spell.aliases()) result.put(alias, spell);
        }
        return Map.copyOf(result);
    }
}