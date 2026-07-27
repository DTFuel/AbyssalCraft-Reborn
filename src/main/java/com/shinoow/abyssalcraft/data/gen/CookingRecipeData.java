package com.shinoow.abyssalcraft.data.gen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

/** Restores the 53 expanded legacy furnace recipes from {@code AbyssalCrafting}. */
public final class CookingRecipeData implements DataProvider {

    private static final int EXPECTED_SOURCE_COUNT = 53;
    private static final int DEFAULT_COOKING_TIME = 200;

    private final PackOutput packOutput;

    public CookingRecipeData(DataGenCompat.Gen gen) {
        packOutput = gen.packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<Entry> entries = entries();
        require(entries.size() == EXPECTED_SOURCE_COUNT,
            "legacy smelting source count changed: " + entries.size());

        Path dataRoot = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK);
        Path projectRoot = locateProjectRoot(dataRoot);
        Path base = dataRoot.resolve(AbyssalCraft.MODID);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<Audit> audits = new ArrayList<>();

        for (Entry entry : entries) {
            if (entry.status() != Status.MIGRATED) {
                audits.add(new Audit(entry.legacyName(), entry.status(), entry.modernId(), entry.reason()));
                continue;
            }
            require(ingredientExists(entry.ingredient()),
                "missing cooking ingredient " + entry.ingredient() + " for " + entry.legacyName());
            require(itemExists(entry.result()),
                "missing cooking result " + entry.result() + " for " + entry.legacyName());
            JsonObject common = common(entry);
            JsonObject forge = common.deepCopy();
            JsonObject forgeResult = new JsonObject();
            forgeResult.addProperty("item", entry.result());
            if (entry.count() != 1) forgeResult.addProperty("count", entry.count());
            forge.add("result", forgeResult);

            JsonObject neo = common.deepCopy();
            JsonObject neoResult = new JsonObject();
            neoResult.addProperty("id", entry.result());
            if (entry.count() != 1) neoResult.addProperty("count", entry.count());
            neo.add("result", neoResult);

            futures.add(DataProvider.saveStable(output, forge,
                base.resolve("recipes").resolve(entry.modernId() + ".json")));
            futures.add(DataProvider.saveStable(output, neo,
                base.resolve("recipe").resolve(entry.modernId() + ".json")));
            audits.add(new Audit(entry.legacyName(), Status.MIGRATED,
                "abyssalcraft:" + entry.modernId(), ""));
        }

        writeAudit(projectRoot.resolve("docs/spec/rr-data-smelting-audit.csv"), audits);
        EnumMap<Status, Integer> counts = new EnumMap<>(Status.class);
        for (Status status : Status.values()) counts.put(status, 0);
        for (Audit audit : audits) counts.merge(audit.status(), 1, Integer::sum);
        require(audits.size() == EXPECTED_SOURCE_COUNT, "smelting audit lost source entries");
        require(counts.values().stream().mapToInt(Integer::intValue).sum() == EXPECTED_SOURCE_COUNT,
            "smelting audit statuses do not close");
        System.out.printf("RR_DATA_SMELTING_AUDIT_OK source=%d migrated=%d replaced=%d blocked=%d retired=%d%n",
            EXPECTED_SOURCE_COUNT, counts.get(Status.MIGRATED), counts.get(Status.REPLACED),
            counts.get(Status.BLOCKED), counts.get(Status.RETIRED));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "AbyssalCraft Legacy Smelting Recipes";
    }

    private static JsonObject common(Entry entry) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:smelting");
        recipe.addProperty("category", "misc");
        JsonObject ingredient = new JsonObject();
        if (entry.ingredient().startsWith("#")) {
            ingredient.addProperty("tag", entry.ingredient().substring(1));
        } else {
            ingredient.addProperty("item", entry.ingredient());
        }
        recipe.add("ingredient", ingredient);
        recipe.addProperty("experience", entry.experience());
        recipe.addProperty("cookingtime", DEFAULT_COOKING_TIME);
        return recipe;
    }

    private static List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        add(entries, "darkstone_cobblestone", "abyssalcraft:darkstone_cobblestone",
            "abyssalcraft:darkstone", 1, 0.1F);
        add(entries, "ore_abyssalnite", "#abyssalcraft:legacy/ore_abyssalnite",
            "abyssalcraft:abyssalnite_ingot", 1, 3.0F);
        add(entries, "ore_coralium", "#abyssalcraft:legacy/ore_coralium",
            "abyssalcraft:coralium_gem", 1, 3.0F);
        add(entries, "darklands_oak_wood", "abyssalcraft:darklands_oak_log",
            "minecraft:charcoal", 1, 1.0F);
        entries.add(new Entry("darklands_oak_wood_2", Status.REPLACED,
            "smelting_darklands_oak_log", "", "", 0, 0,
            "legacy second log block is the same modern darklands_oak_log"));
        blocked(entries, "coralium_infused_stone", "coralium_infused_stone is not registered");
        add(entries, "pearlescent_coralium_ore", "abyssalcraft:pearlescent_coralium_ore",
            "abyssalcraft:coralium_pearl", 1, 3.0F);
        add(entries, "liquified_coralium_ore", "abyssalcraft:liquified_coralium_ore",
            "abyssalcraft:refined_coralium_ingot", 1, 3.0F);
        add(entries, "dreaded_abyssalnite_ore", "abyssalcraft:dreaded_abyssalnite_ore",
            "abyssalcraft:abyssalnite_ingot", 1, 3.0F);
        add(entries, "coralium_stone", "abyssalcraft:coralium_stone",
            "abyssalcraft:coralium_brick", 1, 0.1F);
        add(entries, "nitre_ore", "abyssalcraft:nitre_ore", "abyssalcraft:nitre", 1, 1.0F);
        add(entries, "abyssal_iron_ore", "abyssalcraft:abyssal_iron_ore",
            "minecraft:iron_ingot", 1, 0.7F);
        add(entries, "abyssal_gold_ore", "abyssalcraft:abyssal_gold_ore",
            "minecraft:gold_ingot", 1, 1.0F);
        add(entries, "abyssal_diamond_ore", "abyssalcraft:abyssal_diamond_ore",
            "minecraft:diamond", 1, 1.0F);
        add(entries, "abyssal_nitre_ore", "abyssalcraft:abyssal_nitre_ore",
            "abyssalcraft:nitre", 1, 1.0F);
        add(entries, "ethaxium", "abyssalcraft:ethaxium", "abyssalcraft:ethaxium_brick", 1, 0.2F);
        add(entries, "ethaxium_brick", "abyssalcraft:ethaxium_bricks",
            "abyssalcraft:cracked_ethaxium_brick", 1, 0.1F);
        add(entries, "dark_ethaxium_brick", "abyssalcraft:dark_ethaxium_brick",
            "abyssalcraft:cracked_dark_ethaxium_brick", 1, 0.1F);
        add(entries, "darkstone_brick", "abyssalcraft:darkstone_brick",
            "abyssalcraft:cracked_darkstone_brick", 1, 0.1F);
        add(entries, "abyssal_stone_brick", "abyssalcraft:abyssal_stone_brick",
            "abyssalcraft:cracked_abyssal_stone_brick", 1, 0.1F);
        add(entries, "dreadstone_brick", "abyssalcraft:dreadstone_brick",
            "abyssalcraft:cracked_dreadstone_brick", 1, 0.1F);
        add(entries, "elysian_stone_brick", "abyssalcraft:elysian_stone_brick",
            "abyssalcraft:cracked_elysian_stone_brick", 1, 0.1F);
        add(entries, "coralium_stone_brick", "abyssalcraft:coralium_stone_brick",
            "abyssalcraft:cracked_coralium_stone_brick", 1, 0.1F);
        add(entries, "abyssal_sand", "abyssalcraft:abyssal_sand",
            "abyssalcraft:abyssal_sand_glass", 1, 0.1F);
        add(entries, "abyssal_cobblestone", "abyssalcraft:abyssal_cobblestone",
            "abyssalcraft:abyssal_stone", 1, 0.1F);
        add(entries, "dreadstone_cobblestone", "abyssalcraft:dreadstone_cobblestone",
            "abyssalcraft:dreadstone", 1, 0.1F);
        add(entries, "elysian_cobblestone", "abyssalcraft:elysian_cobblestone",
            "abyssalcraft:elysian_stone", 1, 0.1F);
        add(entries, "coralium_cobblestone", "abyssalcraft:coralium_cobblestone",
            "abyssalcraft:coralium_stone", 1, 0.1F);
        add(entries, "dreadwood_log", "abyssalcraft:dreadwood_log",
            "abyssalcraft:charcoal", 1, 1.0F);
        add(entries, "generic_meat", "abyssalcraft:generic_meat",
            "abyssalcraft:cooked_generic_meat", 1, 0.35F);
        add(entries, "dead_tree_log", "abyssalcraft:dead_tree_log",
            "minecraft:charcoal", 1, 1.0F);
        add(entries, "chunk_of_coralium", "abyssalcraft:chunk_of_coralium",
            "abyssalcraft:refined_coralium_ingot", 2, 3.0F);
        add(entries, "coin", "abyssalcraft:coin", "minecraft:iron_ingot", 4, 0.5F);

        armor(entries, "leather");
        armor(entries, "abyssalnite");
        armor(entries, "refined_coralium");
        armor(entries, "dreadium");
        armor(entries, "ethaxium");
        return entries;
    }

    private static void armor(List<Entry> entries, String material) {
        String[] pieces = {"helmet", "chestplate", "leggings", "boots"};
        for (String piece : pieces) {
            String result = material.equals("leather") ? "minecraft:leather"
                : "abyssalcraft:" + (material.equals("refined_coralium") ? "refined_coralium_ingot"
                    : material + "_ingot");
            entries.add(new Entry(material + "_" + piece + "_recycling", Status.MIGRATED,
                "smelting_" + material + "_" + piece + "_recycling",
                (material.equals("leather") ? "minecraft:" : "abyssalcraft:") + material + "_" + piece,
                result, 1, 0.1F, ""));
        }
    }

    private static void add(List<Entry> entries, String legacyName, String ingredient,
                            String result, int count, float experience) {
        entries.add(new Entry(legacyName, Status.MIGRATED, "smelting_" + legacyName,
            ingredient, result, count, experience, ""));
    }

    private static void blocked(List<Entry> entries, String legacyName, String reason) {
        entries.add(new Entry(legacyName, Status.BLOCKED, "", "", "", 0, 0, reason));
    }

    private static boolean ingredientExists(String ingredient) {
        return ingredient.startsWith("#") || itemExists(ingredient);
    }

    private static boolean itemExists(String id) {
        return BuiltInRegistries.ITEM.getOptional(ACRef.parse(id)).isPresent();
    }

    private static Path locateProjectRoot(Path start) {
        for (Path current = start.toAbsolutePath(); current != null; current = current.getParent()) {
            if (Files.isDirectory(current.resolve("docs/AbyssalCraft-1.12.2"))) return current;
        }
        throw new IllegalStateException("Unable to locate project root from " + start);
    }

    private static void writeAudit(Path path, List<Audit> audits) {
        StringBuilder csv = new StringBuilder("legacy_entry,status,modern_recipe,reason\n");
        for (Audit audit : audits) {
            csv.append(csv(audit.legacyName())).append(',').append(audit.status()).append(',')
                .append(csv(audit.modernRecipe())).append(',').append(csv(audit.reason())).append('\n');
        }
        try {
            Files.writeString(path, csv.toString(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write smelting audit", exception);
        }
    }

    private static String csv(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private enum Status { MIGRATED, REPLACED, BLOCKED, RETIRED }

    private record Entry(String legacyName, Status status, String modernId, String ingredient,
                         String result, int count, float experience, String reason) {}
    private record Audit(String legacyName, Status status, String modernRecipe, String reason) {}
}