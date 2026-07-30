package com.shinoow.abyssalcraft.data.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.core.HolderLookup;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.machine.MachineSelfTest;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

/**
 * Emits every executable entry in the permanent 223-call legacy machine catalog using the complete
 * crystallization, transmutation and materialization schemas.
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

        List<LegacyMachineRecipeCatalog.Entry> entries = LegacyMachineRecipeCatalog.entries();
        int executable = 0;
        for (LegacyMachineRecipeCatalog.Entry entry : entries) {
            if (entry.status() != LegacyMachineRecipeCatalog.Status.MIGRATED
                && entry.status() != LegacyMachineRecipeCatalog.Status.REPLACED) continue;
            executable++;
            JsonObject forgeJson = recipeJson(entry, "item");
            JsonObject neoJson = recipeJson(entry, "id");
            validateRecipe(entry, forgeJson, "item");
            validateRecipe(entry, neoJson, "id");
            saveBoth(futures, output, base, entry.recipeId().substring(entry.recipeId().indexOf(':') + 1),
                forgeJson, neoJson);
        }
        rending(futures, output, base, "abyssal", "Abyssal", 100,
            "abyssalcraft:abyssal_wasteland_essence", "#abyssalcraft:coralium_plague_carriers", 50);
        rending(futures, output, base, "dread", "Dread", 100,
            "abyssalcraft:dreadlands_essence", "#abyssalcraft:dread_plague_carriers", 51);
        rending(futures, output, base, "omothol", "Omothol", 100,
            "abyssalcraft:omothol_essence", "#abyssalcraft:omothol_entities", 52);
        rending(futures, output, base, "shadow", "Shadow", 200,
            "abyssalcraft:shadow_gem", "#abyssalcraft:shadow", -1);
        futures.add(DataProvider.saveStable(output, auditJson(entries), base.resolve("machine_recipe_catalog.json")));
        Map<LegacyMachineRecipeCatalog.Status, Integer> counts = LegacyMachineRecipeCatalog.counts();
        System.out.println("RR_DATAGEN_MACHINE_AUDIT source=" + LegacyMachineRecipeCatalog.SOURCE_COUNT
            + " migrated=" + counts.get(LegacyMachineRecipeCatalog.Status.MIGRATED)
            + " replaced=" + counts.get(LegacyMachineRecipeCatalog.Status.REPLACED)
            + " retired=" + counts.get(LegacyMachineRecipeCatalog.Status.RETIRED)
            + " blocked=" + counts.get(LegacyMachineRecipeCatalog.Status.BLOCKED)
            + " executable=" + executable + " files=" + executable * 2);

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static void validateRecipe(LegacyMachineRecipeCatalog.Entry entry, JsonObject json, String resultKey) {
        if (!json.has("result") || !json.getAsJsonObject("result").has(resultKey)) {
            throw new IllegalStateException("Machine recipe has no result: " + entry.recipeId());
        }
        if (entry.kind() == LegacyMachineRecipeCatalog.Kind.MATERIALIZATION) {
            int inputCount = json.has("inputs") ? json.getAsJsonArray("inputs").size() : 0;
            if (inputCount < 1 || inputCount > 5) {
                throw new IllegalStateException("Materialization recipe input count is outside 1-5: "
                    + entry.recipeId() + " inputs=" + inputCount);
            }
        } else if (!json.has("input") || json.getAsJsonObject("input").entrySet().isEmpty()) {
            throw new IllegalStateException("Machine recipe has no input: " + entry.recipeId());
        }
        if (entry.kind() == LegacyMachineRecipeCatalog.Kind.CRYSTALLIZATION
            && entry.outputs().size() > 2) {
            throw new IllegalStateException("Crystallization recipe has more than two outputs: " + entry.recipeId());
        }
    }

    private static JsonObject recipeJson(LegacyMachineRecipeCatalog.Entry entry, String resultKey) {
        JsonObject json = new JsonObject();
        json.addProperty("type", AbyssalCraft.MODID + ":" + entry.kind().name().toLowerCase(java.util.Locale.ROOT));
        if (entry.kind() == LegacyMachineRecipeCatalog.Kind.MATERIALIZATION) {
            JsonArray inputs = new JsonArray();
            for (LegacyMachineRecipeSource.StackRef input : entry.inputs()) {
                JsonObject counted = new JsonObject();
                counted.add("ingredient", ingredient(input, resultKey));
                counted.addProperty("count", input.count());
                inputs.add(counted);
            }
            json.add("inputs", inputs);
        } else {
            json.add("input", ingredient(entry.inputs().get(0), resultKey));
        }
        json.add("result", stack(entry.outputs().get(0), resultKey));
        if (entry.kind() == LegacyMachineRecipeCatalog.Kind.CRYSTALLIZATION && entry.outputs().size() == 2) {
            json.add("secondary_result", stack(entry.outputs().get(1), resultKey));
        }
        if (entry.kind() != LegacyMachineRecipeCatalog.Kind.MATERIALIZATION) {
            json.addProperty("experience", entry.experience());
            json.addProperty("time", 200);
        }
        return json;
    }

    private static JsonObject ingredient(LegacyMachineRecipeSource.StackRef ref, String resultKey) {
        JsonObject json = new JsonObject();
        String id = ref.id();
        if (ref.tag() && "item".equals(resultKey) && id.startsWith("c:")) id = "forge:" + id.substring(2);
        json.addProperty(ref.tag() ? "tag" : "item", id);
        return json;
    }

    private static JsonObject stack(LegacyMachineRecipeSource.StackRef ref, String resultKey) {
        JsonObject json = new JsonObject();
        json.addProperty(resultKey, ref.id());
        json.addProperty("count", ref.count());
        return json;
    }

    private static JsonObject auditJson(List<LegacyMachineRecipeCatalog.Entry> entries) {
        JsonObject root = new JsonObject();
        Map<LegacyMachineRecipeCatalog.Status, Integer> counts = LegacyMachineRecipeCatalog.counts();
        root.addProperty("source", LegacyMachineRecipeCatalog.SOURCE_COUNT);
        for (LegacyMachineRecipeCatalog.Status status : LegacyMachineRecipeCatalog.Status.values()) {
            root.addProperty(status.name().toLowerCase(java.util.Locale.ROOT), counts.get(status));
        }
        JsonArray catalog = new JsonArray();
        for (LegacyMachineRecipeCatalog.Entry entry : entries) {
            JsonObject json = new JsonObject();
            json.addProperty("ordinal", entry.sourceOrdinal());
            json.addProperty("source_line", entry.sourceLine());
            json.addProperty("kind", entry.kind().name());
            json.addProperty("legacy_call", entry.legacyCall());
            json.addProperty("status", entry.status().name());
            json.addProperty("recipe_id", entry.recipeId());
            json.addProperty("owner", entry.owner());
            json.addProperty("reason", entry.reason());
            json.add("inputs", auditStacks(entry.inputs()));
            json.add("outputs", auditStacks(entry.outputs()));
            JsonArray resolutions = new JsonArray();
            for (MachineOutputResolutionCatalog.Resolution resolution : entry.outputResolutions()) {
                JsonObject resolved = new JsonObject();
                resolved.addProperty("tag", resolution.tag());
                resolved.addProperty("item", resolution.item());
                resolved.addProperty("reason", resolution.reason());
                resolutions.add(resolved);
            }
            json.add("output_resolutions", resolutions);
            catalog.add(json);
        }
        JsonArray resolutions = new JsonArray();
        for (MachineOutputResolutionCatalog.Resolution resolution : MachineOutputResolutionCatalog.resolutions()) {
            JsonObject json = new JsonObject();
            json.addProperty("tag", resolution.tag());
            json.addProperty("item", resolution.item());
            json.addProperty("reason", resolution.reason());
            resolutions.add(json);
        }
        root.add("output_resolutions", resolutions);
        root.add("entries", catalog);
        return root;
    }

    private static JsonArray auditStacks(List<LegacyMachineRecipeSource.StackRef> stacks) {
        JsonArray array = new JsonArray();
        for (LegacyMachineRecipeSource.StackRef stack : stacks) {
            JsonObject json = new JsonObject();
            json.addProperty(stack.tag() ? "tag" : "item", stack.id());
            json.addProperty("count", stack.count());
            array.add(json);
        }
        return array;
    }

    private static void rending(List<CompletableFuture<?>> futures, CachedOutput output, Path base,
                                String id, String name, int maxEnergy, String outputId,
                                String entity, int dimension) {
        saveBoth(futures, output, base, "rending_" + id,
            rendingJson(name, maxEnergy, outputId, entity, dimension, "item"),
            rendingJson(name, maxEnergy, outputId, entity, dimension, "id"));
    }

    private static JsonObject rendingJson(String name, int maxEnergy, String outputId,
                                          String entity, int dimension, String resultKey) {
        JsonObject json = new JsonObject();
        json.addProperty("type", AbyssalCraft.MODID + ":rending");
        json.addProperty("name", name);
        json.addProperty("max_energy", maxEnergy);
        json.add("result", result(outputId, 1, resultKey));
        json.addProperty("entity", entity);
        json.addProperty("dimension", dimension);
        return json;
    }

    private static JsonObject itemIngredient(String id) {
        JsonObject json = new JsonObject();
        json.addProperty("item", id);
        return json;
    }

    private static JsonObject result(String id, int count, String resultKey) {
        JsonObject json = new JsonObject();
        json.addProperty(resultKey, id);
        json.addProperty("count", count);
        return json;
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

    private static void saveBoth(List<CompletableFuture<?>> futures, CachedOutput output, Path base, String name,
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
