package com.shinoow.abyssalcraft.data.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/** Emits every executable legacy anvil forging in both supported recipe layouts. */
public final class AnvilForgingRecipeData implements DataProvider {

    private final PackOutput packOutput;

    public AnvilForgingRecipeData(DataGenCompat.Gen gen) {
        packOutput = gen.packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Path base = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(AbyssalCraft.MODID);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        int executable = 0;
        for (LegacyAnvilForgingCatalog.Entry entry : LegacyAnvilForgingCatalog.entries()) {
            if (!entry.executable()) continue;
            requireItem(entry.input1());
            requireItem(entry.input2());
            requireItem(entry.output());
            String path = entry.recipeId().substring(entry.recipeId().indexOf(':') + 1);
            futures.add(DataProvider.saveStable(output, recipe(entry, "item"),
                base.resolve("recipes").resolve(path + ".json")));
            futures.add(DataProvider.saveStable(output, recipe(entry, "id"),
                base.resolve("recipe").resolve(path + ".json")));
            executable++;
        }
        Map<LegacyAnvilForgingCatalog.Status, Integer> counts = LegacyAnvilForgingCatalog.counts();
        futures.add(DataProvider.saveStable(output, auditJson(), base.resolve("anvil_forging_catalog.json")));
        System.out.println("RR_ANVIL_FORGING_AUDIT_OK source=" + LegacyAnvilForgingCatalog.SOURCE_COUNT
            + " migrated=" + counts.get(LegacyAnvilForgingCatalog.Status.MIGRATED)
            + " retired=" + counts.get(LegacyAnvilForgingCatalog.Status.RETIRED)
            + " blocked=0 executable=" + executable + " files=" + executable * 2);
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "AbyssalCraft Anvil Forging Recipes";
    }

    private static JsonObject recipe(LegacyAnvilForgingCatalog.Entry entry, String resultKey) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "abyssalcraft:anvil_forging");
        json.add("input1", ingredient(entry.input1()));
        json.add("input2", ingredient(entry.input2()));
        JsonObject result = new JsonObject();
        result.addProperty(resultKey, entry.output());
        result.addProperty("count", 1);
        json.add("result", result);
        json.addProperty("price", entry.price());
        json.addProperty("forging_type", entry.forgingType());
        return json;
    }

    private static JsonObject ingredient(String item) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        return json;
    }

    private static JsonObject auditJson() {
        JsonObject root = new JsonObject();
        Map<LegacyAnvilForgingCatalog.Status, Integer> counts = LegacyAnvilForgingCatalog.counts();
        root.addProperty("source", LegacyAnvilForgingCatalog.SOURCE_COUNT);
        root.addProperty("migrated", counts.get(LegacyAnvilForgingCatalog.Status.MIGRATED));
        root.addProperty("retired", counts.get(LegacyAnvilForgingCatalog.Status.RETIRED));
        root.addProperty("blocked", 0);
        JsonArray entries = new JsonArray();
        for (LegacyAnvilForgingCatalog.Entry entry : LegacyAnvilForgingCatalog.entries()) {
            JsonObject json = new JsonObject();
            json.addProperty("ordinal", entry.sourceOrdinal());
            json.addProperty("legacy_key", entry.legacyKey());
            json.addProperty("status", entry.status().name());
            json.addProperty("recipe_id", entry.recipeId());
            json.addProperty("input1", entry.input1());
            json.addProperty("input2", entry.input2());
            json.addProperty("output", entry.output());
            json.addProperty("price", entry.price());
            json.addProperty("forging_type", entry.forgingType());
            json.addProperty("reason", entry.reason());
            entries.add(json);
        }
        root.add("entries", entries);
        return root;
    }

    @SuppressWarnings("deprecation")
    private static void requireItem(String id) {
        if (!BuiltInRegistries.ITEM.containsKey(com.shinoow.abyssalcraft.platform.ACRef.parse(id))) {
            throw new IllegalStateException("anvil forging item is not registered: " + id);
        }
    }
}
