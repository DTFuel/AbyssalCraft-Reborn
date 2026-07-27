package com.shinoow.abyssalcraft.system.advancement;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.resources.ResourceLocation;

/** The nine AbyssalCraft progression advancements and their Necronomicon presentation metadata. */
public final class AdvancementKnowledge {

    private static final String TITLE_PREFIX = "advancements.abyssalcraft.";

    public static final List<Entry> ENTRIES = List.of(
        entry("root", null, "necronomicon", "necro", "necronomicon"),
        entry("mine_abyssalnite", "root", "abyssalnite_ore", "mineaby", "abyssalnite_ore"),
        entry("mine_coralium", "mine_abyssalnite", "coralium_gem", "minecorgem",
            "coralium_gem", "coralium_ore", "coralium_gem_cluster_2", "coralium_gem_cluster_3",
            "coralium_gem_cluster_4", "coralium_gem_cluster_5", "coralium_gem_cluster_6",
            "coralium_gem_cluster_7", "coralium_gem_cluster_8", "coralium_gem_cluster_9"),
        entry("shadow_gems", "mine_coralium", "shadow_gem", "shadowgems", "shadow_gem"),
        entry("mine_abyssal_coralium", "shadow_gems", "abyssal_coralium_ore", "minecor",
            "abyssal_coralium_ore", "pearlescent_coralium_ore", "liquified_coralium_ore"),
        entry("mine_abyssal_ores", "mine_abyssal_coralium", "abyssal_diamond_ore", "mineabyores",
            "abyssal_diamond_ore", "abyssal_gold_ore", "abyssal_iron_ore", "abyssal_nitre_ore"),
        entry("mine_dreadlands_ores", "mine_abyssal_ores", "dreaded_abyssalnite_ore", "minedread",
            "dreadlands_abyssalnite_ore", "dreaded_abyssalnite_ore"),
        entry("dreadium", "mine_dreadlands_ores", "dreadium_ingot", "dreadium", "dreadium_ingot"),
        entry("ethaxium", "dreadium", "ethaxium_ingot", "ethaxium", "ethaxium_ingot"));

    private static final Map<ResourceLocation, Entry> BY_ID = ENTRIES.stream()
        .collect(Collectors.toUnmodifiableMap(Entry::id, Function.identity()));

    private AdvancementKnowledge() {}

    public static boolean contains(ResourceLocation id) {
        return BY_ID.containsKey(id);
    }

    public static Entry get(ResourceLocation id) {
        return BY_ID.get(id);
    }

    private static Entry entry(String path, String parentPath, String iconPath, String translationPath,
            String... criterionItemPaths) {
        return new Entry(ACRef.id(path), parentPath == null ? null : ACRef.id(parentPath), ACRef.id(iconPath),
            TITLE_PREFIX + translationPath + ".title",
            TITLE_PREFIX + translationPath + ".description",
            java.util.Arrays.stream(criterionItemPaths).map(ACRef::id).toList());
    }

    public record Entry(ResourceLocation id, ResourceLocation parent, ResourceLocation icon,
                        String titleKey, String descriptionKey, List<ResourceLocation> criterionItems) {}
}