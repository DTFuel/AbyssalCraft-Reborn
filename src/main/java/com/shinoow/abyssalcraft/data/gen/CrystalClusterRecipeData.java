package com.shinoow.abyssalcraft.data.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;
import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

public final class CrystalClusterRecipeData implements DataProvider {

    private final PackOutput packOutput;

    public CrystalClusterRecipeData(DataGenCompat.Gen gen) {
        this.packOutput = gen.packOutput;
    }

    @Override
    public String getName() {
        return "AbyssalCraft Crystal Cluster Recipes";
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        if (MaterialItems.CRYSTAL_ELEMENTS.length != 26 || CrystalClusterBlocks.CLUSTERS.size() != 26) {
            throw new IllegalStateException("crystal cluster recipe catalog must contain 26 elements");
        }
        Path base = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(AbyssalCraft.MODID);
        List<CompletableFuture<?>> futures = new ArrayList<>(104);
        for (String element : MaterialItems.CRYSTAL_ELEMENTS) {
            String crystal = AbyssalCraft.MODID + ":crystal_" + element;
            String cluster = AbyssalCraft.MODID + ":" + element + "_crystal_cluster";
            saveBoth(futures, output, base, element + "_crystal_cluster",
                shaped(crystal, cluster, 1, "item"), shaped(crystal, cluster, 1, "id"));
            saveBoth(futures, output, base, "crystal_" + element + "_from_cluster",
                shaped(cluster, crystal, 9, "item"), shaped(cluster, crystal, 9, "id"));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static void saveBoth(List<CompletableFuture<?>> futures, CachedOutput output, Path base, String name,
                                 JsonObject forgeJson, JsonObject neoJson) {
        futures.add(DataProvider.saveStable(output, forgeJson, base.resolve("recipes").resolve(name + ".json")));
        futures.add(DataProvider.saveStable(output, neoJson, base.resolve("recipe").resolve(name + ".json")));
    }

    private static JsonObject shaped(String ingredientId, String resultId, int count, String resultKey) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray pattern = new JsonArray();
        if (count == 1) {
            pattern.add("###");
            pattern.add("###");
            pattern.add("###");
        } else {
            pattern.add("#");
        }
        json.add("pattern", pattern);
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", ingredientId);
        JsonObject key = new JsonObject();
        key.add("#", ingredient);
        json.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty(resultKey, resultId);
        result.addProperty("count", count);
        json.add("result", result);
        return json;
    }
}