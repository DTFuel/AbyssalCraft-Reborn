package com.shinoow.abyssalcraft.data.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.core.HolderLookup;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.machine.MachineSelfTest;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

/**
 * Machine recipe datagen (owned by PC-9, Stage C2b): emits example custom-machine recipes for the three
 * MP synthesis machines -- crystallizer / transmutator / materializer -- in the {@code ProcessingRecipe}
 * shape ({@code input -> result + time}). Faithful to the 1.12.2 {@code AbyssalCrafting} tables, curated
 * to items that exist in the current port (multi-output crystallization and multi-input materialization
 * are dropped until the richer recipe shapes land in the C2a regression).
 *
 * <p>Fork-free: {@link DataProvider#run}/{@link DataProvider#saveStable} share a signature across both
 * loaders. Because the recipe folder singularised in 1.21 ({@code recipes/} -&gt; {@code recipe/}) and the
 * result item key changed ({@code "item"} -&gt; {@code "id"}), and both loaders' {@code runData} share one
 * output dir (so a loader-specific folder would be clobbered by the other loader's HashCache), this
 * provider emits <b>both</b> forms every run: {@code recipes/*.json} ({@code "item"}, read by 1.20.1) and
 * {@code recipe/*.json} ({@code "id"}, read by 1.21). Each loader reads only its own folder. Registered on
 * the server pack in {@code data/ACDataGenerators}.
 */
public final class MachineRecipeData implements DataProvider {

    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> lookup;

    public MachineRecipeData(DataGenCompat.Gen gen) {
        this.packOutput = gen.packOutput;
        this.lookup = gen.lookup;
    }

    @Override
    public String getName() {
        return "AbyssalCraft Machine Recipes";
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        MachineSelfTest.run(lookup.join());
        Path base = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(AbyssalCraft.MODID);
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // ---- Crystallization (crystallizer: item -> crystal, single-output subset) ----
        crystallization(futures, output, base, "coal_ore", "minecraft:coal_ore", "abyssalcraft:crystal_carbon", 2, 0.2F);
        crystallization(futures, output, base, "redstone_ore", "minecraft:redstone_ore", "abyssalcraft:crystal_redstone", 2, 0.2F);
        crystallization(futures, output, base, "gold_nugget", "minecraft:gold_nugget", "abyssalcraft:crystal_shard_gold", 1, 0.1F);
        crystallization(futures, output, base, "abyssalnite_ingot", "abyssalcraft:abyssalnite_ingot", "abyssalcraft:crystal_shard_abyssalnite", 1, 0.1F);
        crystallization(futures, output, base, "refined_coralium_ingot", "abyssalcraft:refined_coralium_ingot", "abyssalcraft:crystal_shard_coralium", 1, 0.1F);
        crystallization(futures, output, base, "dreadium_ingot", "abyssalcraft:dreadium_ingot", "abyssalcraft:crystal_shard_dreadium", 1, 0.2F);
        crystallization(futures, output, base, "coralium_gem", "abyssalcraft:coralium_gem", "abyssalcraft:crystal_shard_coralium", 1, 0.1F);

        // ---- Transmutation (transmutator: input -> output) ----
        transmutation(futures, output, base, "stone_to_darkstone", "minecraft:stone", "abyssalcraft:darkstone", 1);
        transmutation(futures, output, base, "darkstone_to_stone", "abyssalcraft:darkstone", "minecraft:stone", 1);
        transmutation(futures, output, base, "cobblestone_to_darkstone_cobblestone", "minecraft:cobblestone", "abyssalcraft:darkstone_cobblestone", 1);
        transmutation(futures, output, base, "darkstone_cobblestone_to_cobblestone", "abyssalcraft:darkstone_cobblestone", "minecraft:cobblestone", 1);
        transmutation(futures, output, base, "dreaded_shard_to_dreadium_ingot", "abyssalcraft:dreaded_shard_of_abyssalnite", "abyssalcraft:dreadium_ingot", 1);
        transmutation(futures, output, base, "dense_carbon_cluster_to_diamond", "abyssalcraft:dense_carbon_cluster", "minecraft:diamond", 1);
        transmutation(futures, output, base, "anti_beef_to_cooked_beef", "abyssalcraft:anti_beef", "minecraft:cooked_beef", 1);
        transmutation(futures, output, base, "anti_pork_to_cooked_porkchop", "abyssalcraft:anti_pork", "minecraft:cooked_porkchop", 1);
        transmutation(futures, output, base, "anti_chicken_to_cooked_chicken", "abyssalcraft:anti_chicken", "minecraft:cooked_chicken", 1);
        transmutation(futures, output, base, "crystal_abyssalnite_to_nugget", "abyssalcraft:crystal_abyssalnite", "abyssalcraft:abyssalnite_nugget", 1);
        transmutation(futures, output, base, "crystal_gold_to_gold_nugget", "abyssalcraft:crystal_gold", "minecraft:gold_nugget", 1);

        // ---- Materialization (materializer: crystal -> item, single-input subset) ----
        materialization(futures, output, base, "calcium_to_bone", "abyssalcraft:crystal_calcium", "minecraft:bone", 1);
        materialization(futures, output, base, "carbon_to_coal", "abyssalcraft:crystal_carbon", "minecraft:coal", 1);
        materialization(futures, output, base, "iron_to_ingot", "abyssalcraft:crystal_iron", "minecraft:iron_ingot", 1);
        materialization(futures, output, base, "gold_to_ingot", "abyssalcraft:crystal_gold", "minecraft:gold_ingot", 1);
        materialization(futures, output, base, "blaze_to_powder", "abyssalcraft:crystal_blaze", "minecraft:blaze_powder", 1);
        materialization(futures, output, base, "abyssalnite_to_ingot", "abyssalcraft:crystal_abyssalnite", "abyssalcraft:abyssalnite_ingot", 1);
        materialization(futures, output, base, "coralium_to_ingot", "abyssalcraft:crystal_coralium", "abyssalcraft:refined_coralium_ingot", 1);
        materialization(futures, output, base, "dreadium_to_ingot", "abyssalcraft:crystal_dreadium", "abyssalcraft:dreadium_ingot", 1);
        materialization(futures, output, base, "redstone_to_redstone", "abyssalcraft:crystal_redstone", "minecraft:redstone", 1);

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private void crystallization(List<CompletableFuture<?>> futures, CachedOutput output, Path base,
                                 String name, String input, String result, int count, float experience) {
        crystallizationJson(futures, output, base, "crystallization_" + name, input, result, count, experience);
    }

    private void transmutation(List<CompletableFuture<?>> futures, CachedOutput output, Path base,
                               String name, String input, String result, int count) {
        transmutationJson(futures, output, base, "transmutation_" + name, input, result, count, 0.1F);
    }

    private void materialization(List<CompletableFuture<?>> futures, CachedOutput output, Path base,
                                 String name, String input, String result, int count) {
        materializationJson(futures, output, base, "materialization_" + name, input, result, count);
    }

    private void crystallizationJson(List<CompletableFuture<?>> futures, CachedOutput output, Path base,
                                     String name, String input, String result, int count, float experience) {
        saveBoth(futures, output, base, name,
            recipeJson("crystallization", input, result, count, 200, experience, "item"),
            recipeJson("crystallization", input, result, count, 200, experience, "id"));
    }

    private void transmutationJson(List<CompletableFuture<?>> futures, CachedOutput output, Path base,
                                   String name, String input, String result, int count, float experience) {
        saveBoth(futures, output, base, name,
            recipeJson("transmutation", input, result, count, 200, experience, "item"),
            recipeJson("transmutation", input, result, count, 200, experience, "id"));
    }

    private void materializationJson(List<CompletableFuture<?>> futures, CachedOutput output, Path base,
                                     String name, String input, String result, int count) {
        saveBoth(futures, output, base, name,
            materializationRecipeJson(input, result, count, "item"),
            materializationRecipeJson(input, result, count, "id"));
    }

    private void saveBoth(List<CompletableFuture<?>> futures, CachedOutput output, Path base, String name,
                          JsonObject forgeJson, JsonObject neoJson) {
        futures.add(DataProvider.saveStable(output, forgeJson, base.resolve("recipes").resolve(name + ".json")));
        futures.add(DataProvider.saveStable(output, neoJson, base.resolve("recipe").resolve(name + ".json")));
    }

    private static JsonObject recipeJson(String type, String input, String result, int count, int time,
                                         float experience, String resultKey) {
        JsonObject json = new JsonObject();
        json.addProperty("type", AbyssalCraft.MODID + ":" + type);
        JsonObject in = new JsonObject();
        in.addProperty("item", input);
        json.add("input", in);
        JsonObject res = new JsonObject();
        res.addProperty(resultKey, result);
        res.addProperty("count", count);
        json.add("result", res);
        json.addProperty("experience", experience);
        json.addProperty("time", time);
        return json;
    }

    private static JsonObject materializationRecipeJson(String input, String result, int count, String resultKey) {
        JsonObject json = new JsonObject();
        json.addProperty("type", AbyssalCraft.MODID + ":materialization");
        com.google.gson.JsonArray inputs = new com.google.gson.JsonArray();
        JsonObject counted = new JsonObject();
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", input);
        counted.add("ingredient", ingredient);
        counted.addProperty("count", 1);
        inputs.add(counted);
        json.add("inputs", inputs);
        JsonObject res = new JsonObject();
        res.addProperty(resultKey, result);
        res.addProperty("count", count);
        json.add("result", res);
        return json;
    }
}
