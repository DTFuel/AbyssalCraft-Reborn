package com.shinoow.abyssalcraft.data.gen;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;

import com.shinoow.abyssalcraft.platform.ACRef;

/** Stable replacements for legacy OreDictionary values used as machine recipe outputs. */
final class MachineOutputResolutionCatalog {

    record Resolution(String tag, String item, String reason) {}

    private static final Map<String, Resolution> RESOLUTIONS = build();

    private MachineOutputResolutionCatalog() {}

    static Resolution resolve(String tag) {
        Resolution resolution = RESOLUTIONS.get(tag);
        if (resolution == null) {
            throw new IllegalStateException("No canonical machine output for tag " + tag);
        }
        return resolution;
    }

    static List<Resolution> resolutions() {
        return List.copyOf(RESOLUTIONS.values());
    }

    static void validate(Set<String> usedOutputTags, long resolvedOutputCount) {
        if (RESOLUTIONS.size() != 25) {
            throw new IllegalStateException("Machine output resolution count changed: " + RESOLUTIONS.size());
        }
        if (resolvedOutputCount != 25) {
            throw new IllegalStateException("Resolved legacy machine output count changed: " + resolvedOutputCount);
        }
        if (!RESOLUTIONS.keySet().containsAll(usedOutputTags)) {
            throw new IllegalStateException("Machine output resolution coverage is incomplete: used="
                + usedOutputTags + " catalog=" + RESOLUTIONS.keySet());
        }
        for (Resolution resolution : RESOLUTIONS.values()) {
            if (!BuiltInRegistries.ITEM.containsKey(ACRef.parse(resolution.item()))) {
                throw new IllegalStateException("Canonical machine output is not registered: "
                    + resolution.tag() + " -> " + resolution.item());
            }
        }
        if (!RESOLUTIONS.equals(build())) {
            throw new IllegalStateException("Canonical machine output catalog is not deterministic");
        }
    }

    private static Map<String, Resolution> build() {
        Map<String, Resolution> result = new LinkedHashMap<>();
        put(result, "c:foods/raw_meat", "minecraft:beef", "vanilla beef is the stable raw-meat representative");
        put(result, "c:ingots/aluminium", "abyssalcraft:crystal_aluminium", "AC aluminium material preserves the legacy element");
        put(result, "c:nuggets/aluminium", "abyssalcraft:crystal_shard_aluminium", "AC aluminium shard preserves the legacy nugget scale");
        put(result, "c:ingots/calcium", "abyssalcraft:crystal_calcium", "AC calcium material preserves the legacy element");
        put(result, "c:nuggets/calcium", "abyssalcraft:crystal_shard_calcium", "AC calcium shard preserves the legacy nugget scale");
        put(result, "c:ingots/magnesium", "abyssalcraft:crystal_magnesium", "AC magnesium material preserves the legacy element");
        put(result, "c:nuggets/magnesium", "abyssalcraft:crystal_shard_magnesium", "AC magnesium shard preserves the legacy nugget scale");
        put(result, "c:ingots/zinc", "abyssalcraft:crystal_zinc", "AC zinc material preserves the legacy element");
        put(result, "c:nuggets/zinc", "abyssalcraft:crystal_shard_zinc", "AC zinc shard preserves the legacy nugget scale");
        put(result, "c:ingots/copper", "abyssalcraft:crystal_copper", "AC copper compatibility crystal preserves the legacy machine output element");
        put(result, "c:nuggets/copper", "abyssalcraft:crystal_shard_copper", "AC copper compatibility shard preserves the legacy nugget-scale output");
        put(result, "c:nuggets/iron", "minecraft:iron_nugget", "vanilla iron nugget is the exact modern material");
        put(result, "c:ingots/tin", "abyssalcraft:crystal_tin", "AC tin compatibility crystal preserves the legacy machine output element");
        put(result, "c:nuggets/tin", "abyssalcraft:crystal_shard_tin", "AC tin compatibility shard preserves the legacy nugget-scale output");
        put(result, "c:ores/coal", "minecraft:coal_ore", "vanilla coal ore is the exact legacy ore representative");
        put(result, "c:ores/diamond", "minecraft:diamond_ore", "vanilla diamond ore is the exact legacy ore representative");
        put(result, "c:ores/gold", "minecraft:gold_ore", "vanilla gold ore is the exact legacy ore representative");
        put(result, "c:ores/iron", "minecraft:iron_ore", "vanilla iron ore is the exact legacy ore representative");
        put(result, "c:ores/lapis", "minecraft:lapis_ore", "vanilla lapis ore is the exact legacy ore representative");
        put(result, "c:ores/redstone", "minecraft:redstone_ore", "vanilla redstone ore is the exact legacy ore representative");
        put(result, "minecraft:leaves", "minecraft:oak_leaves", "oak leaves are the stable vanilla family representative");
        put(result, "minecraft:logs", "minecraft:oak_log", "oak log is the stable vanilla family representative");
        put(result, "minecraft:planks", "minecraft:oak_planks", "oak planks are the stable vanilla family representative");
        put(result, "minecraft:saplings", "minecraft:oak_sapling", "oak sapling is the stable vanilla family representative");
        put(result, "minecraft:vines", "minecraft:vine", "vanilla vine is the exact item represented by the tag");
        return Collections.unmodifiableMap(result);
    }

    private static void put(Map<String, Resolution> result, String tag, String item, String reason) {
        Resolution previous = result.put(tag, new Resolution(tag, item, reason));
        if (previous != null) throw new IllegalStateException("Duplicate machine output tag " + tag);
    }
}