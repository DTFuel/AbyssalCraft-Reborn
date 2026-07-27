package com.shinoow.abyssalcraft.data.gen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/** Permanent, read-only closure audit for the R6 RR-DATAGEN data-pack surface. */
public final class DatagenClosureValidationData implements DataProvider {

    private static final Set<String> MACHINE_SERIALIZERS = Set.of(
        "abyssalcraft:crystallization", "abyssalcraft:materialization", "abyssalcraft:transmutation");
    private static final Set<String> MACHINE_RECIPE_OWNERS = LegacyMachineRecipeCatalog.entries().stream()
        .filter(entry -> entry.status() == LegacyMachineRecipeCatalog.Status.MIGRATED
            || entry.status() == LegacyMachineRecipeCatalog.Status.REPLACED)
        .map(entry -> entry.recipeId().substring(entry.recipeId().indexOf(':') + 1) + ".json")
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
    private static final Set<String> CUSTOM_SERIALIZERS = Set.of(
        "abyssalcraft:anvil_forging", "abyssalcraft:crystallization", "abyssalcraft:materialization",
        "abyssalcraft:rending", "abyssalcraft:transmutation");
    private static final Set<String> WORLDGEN_TYPES = Set.of(
        "abyssalcraft:chains", "abyssalcraft:configurable_amplified_offset",
        "abyssalcraft:coralium_swamp_ores", "abyssalcraft:dark_realm_cavity_mask",
        "abyssalcraft:dead_tree", "abyssalcraft:mini_pillar", "abyssalcraft:monolith",
        "abyssalcraft:stalagmite", "abyssalcraft:structure");
    private static final Map<String, String> LEGACY_DIRECTORIES = Map.of(
        "recipe", "recipes", "loot_table", "loot_tables", "advancement", "advancements", "structure", "structures");

    private final PackOutput packOutput;

    public DatagenClosureValidationData(DataGenCompat.Gen gen) {
        packOutput = gen.packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.runAsync(this::audit);
    }

    private void audit() {
        Path generatedData = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK);
        Path projectRoot = locateProjectRoot(generatedData);
        List<Path> roots = List.of(projectRoot.resolve("src/main/resources/data"), generatedData);
        List<String> failures = new ArrayList<>();
        Map<String, Path> logicalFiles = new HashMap<>();
        Map<String, Path> physicalFiles = new HashMap<>();
        Set<String> allResources = new HashSet<>();
        List<ParsedResource> parsedResources = new ArrayList<>();
        Map<String, Integer> customSerializerConsumers = new HashMap<>();
        Map<String, Integer> worldgenTypeConsumers = new HashMap<>();
        int jsonCount = 0;
        int exclusiveMachineRecipes = 0;
        Set<String> machineRecipeOwners = new HashSet<>();

        try {
            for (Path root : roots) {
                if (!Files.isDirectory(root)) continue;
                try (Stream<Path> paths = Files.walk(root)) {
                    for (Path file : paths.filter(DatagenClosureValidationData::isDataFile).sorted().toList()) {
                        Resource resource = resource(root, file);
                        if (resource == null) continue;
                        allResources.add(resource.key());
                        Path physicalPrevious = physicalFiles.putIfAbsent(resource.key(), file);
                        if (physicalPrevious != null) failures.add("stale generated " + resource.key());
                        Path previous = logicalFiles.putIfAbsent(resource.logicalKey(), file);
                        if (previous != null && !sameVersionPair(resource, previous, roots)) {
                            failures.add("duplicate logical ID " + resource.logicalKey());
                        }
                        if (!file.toString().endsWith(".json")) continue;
                        jsonCount++;
                        JsonObject json;
                        try {
                            json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                        } catch (RuntimeException exception) {
                            failures.add("invalid JSON " + projectRoot.relativize(file));
                            continue;
                        }
                        parsedResources.add(new ParsedResource(resource, json));
                    }
                }
            }
            for (ParsedResource parsed : parsedResources) {
                Resource resource = parsed.resource();
                JsonObject json = parsed.json();
                if (resource.directory().equals("recipe") || resource.directory().equals("recipes")) {
                    String type = string(json, "type");
                    if (MACHINE_SERIALIZERS.contains(type)) {
                        exclusiveMachineRecipes++;
                        machineRecipeOwners.add(resource.path());
                        if (!MACHINE_RECIPE_OWNERS.contains(resource.path())) {
                            failures.add("machine recipe has no catalog owner " + resource.id());
                        }
                    }
                    if (CUSTOM_SERIALIZERS.contains(type)) customSerializerConsumers.merge(type, 1, Integer::sum);
                    validateRecipe(resource, json, allResources, failures);
                } else if (resource.directory().equals("loot_table") || resource.directory().equals("loot_tables")) {
                    validateLoot(resource, json, allResources, failures);
                } else if (resource.directory().equals("advancement") || resource.directory().equals("advancements")) {
                    validateAdvancement(resource, json, allResources, failures);
                } else if (resource.directory().equals("tags")) {
                    validateTag(resource, json, allResources, failures);
                } else if (resource.directory().equals("worldgen") || resource.directory().equals("dimension")) {
                    validateWorldgen(resource, json, allResources, worldgenTypeConsumers, failures);
                }
            }
            for (String serializer : CUSTOM_SERIALIZERS) {
                if (customSerializerConsumers.getOrDefault(serializer, 0) == 0) {
                    failures.add("registered recipe serializer has no data " + serializer);
                }
            }
            for (String type : WORLDGEN_TYPES) {
                if (worldgenTypeConsumers.getOrDefault(type, 0) == 0) {
                    failures.add("registered worldgen type has no data " + type);
                }
            }
            validateBlockLootCatalog(allResources, failures);
            validatePairs(allResources, failures);
            if (!machineRecipeOwners.equals(MACHINE_RECIPE_OWNERS)) {
                Set<String> missingOwners = new HashSet<>(MACHINE_RECIPE_OWNERS);
                missingOwners.removeAll(machineRecipeOwners);
                failures.add("machine recipe owner closure logical=" + machineRecipeOwners.size()
                    + " expected=" + MACHINE_RECIPE_OWNERS.size() + " missing=" + missingOwners.stream().limit(10).toList());
            }
            if (exclusiveMachineRecipes != MACHINE_RECIPE_OWNERS.size() * 2) {
                failures.add("machine recipe physical closure=" + exclusiveMachineRecipes
                    + " expected=" + MACHINE_RECIPE_OWNERS.size() * 2);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("RR-DATAGEN audit could not scan data roots", exception);
        }

        if (!failures.isEmpty()) {
            throw new IllegalStateException("RR-DATAGEN missing=" + failures.size() + ": "
                + String.join("; ", failures.stream().limit(30).toList()));
        }
        System.out.printf("RR_DATAGEN_AUDIT_OK missing=0 json=%d logical=%d exclusiveMachineRecipes=%d%n",
            jsonCount, logicalFiles.size(), exclusiveMachineRecipes);
    }

    private static void validateRecipe(Resource resource, JsonObject json, Set<String> resources, List<String> failures) {
        String type = string(json, "type");
        if (type == null || !registered(BuiltInRegistries.RECIPE_SERIALIZER.keySet(), type)) {
            failures.add("recipe serializer " + resource.id() + " -> " + type);
        }
        JsonElement result = json.get("result");
        String output = result != null && result.isJsonPrimitive() ? result.getAsString()
            : result != null && result.isJsonObject() ? firstString(result.getAsJsonObject(), "item", "id") : null;
        if (output == null || !registered(BuiltInRegistries.ITEM.keySet(), output)) {
            failures.add("recipe output " + resource.id() + " -> " + output);
        }
        JsonObject secondaryResult = object(json, "secondary_result");
        String secondaryOutput = secondaryResult == null ? null : firstString(secondaryResult, "item", "id");
        if (secondaryOutput != null && !registered(BuiltInRegistries.ITEM.keySet(), secondaryOutput)) {
            failures.add("recipe secondary output " + resource.id() + " -> " + secondaryOutput);
        }
        List<JsonElement> ingredients = new ArrayList<>();
        collectIngredients(json.get("ingredient"), ingredients);
        collectIngredients(json.get("ingredients"), ingredients);
        JsonObject keys = object(json, "key");
        if (keys != null) keys.entrySet().forEach(entry -> collectIngredients(entry.getValue(), ingredients));
        collectIngredients(json.get("input"), ingredients);
        JsonArray countedInputs = json.getAsJsonArray("inputs");
        if (countedInputs != null) countedInputs.forEach(entry -> {
            if (entry.isJsonObject()) collectIngredients(entry.getAsJsonObject().get("ingredient"), ingredients);
        });
        collectIngredients(json.get("input1"), ingredients);
        collectIngredients(json.get("input2"), ingredients);
        if ("abyssalcraft:rending".equals(type)) {
            if (string(json, "entity") == null || !json.has("max_energy")) {
                failures.add("rending recipe input schema " + resource.id());
            }
        } else if (ingredients.isEmpty()) failures.add("recipe has no input " + resource.id());
        ingredients.forEach(element -> validateIngredient(resource.id(), element, resources, failures));
    }

    private static void validateIngredient(String owner, JsonElement element, Set<String> resources, List<String> failures) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(value -> validateIngredient(owner, value, resources, failures));
            return;
        }
        if (element.isJsonPrimitive()) {
            String id = element.getAsString();
            if (!registered(BuiltInRegistries.ITEM.keySet(), id)) failures.add("recipe input " + owner + " -> " + id);
            return;
        }
        if (!element.isJsonObject()) return;
        JsonObject object = element.getAsJsonObject();
        String item = firstString(object, "item", "id");
        if (item != null && !registered(BuiltInRegistries.ITEM.keySet(), item)) failures.add("recipe input " + owner + " -> " + item);
        String tag = string(object, "tag");
        if (tag != null && tag.startsWith(AbyssalCraft.MODID + ":")) {
            String path = ACRef.parse(tag).getPath() + ".json";
            if (!hasTag(resources, "item", path)) failures.add("recipe input tag " + owner + " -> " + tag);
        }
    }

    private static void collectIngredients(JsonElement element, List<JsonElement> target) {
        if (element == null) return;
        if (element.isJsonArray()) element.getAsJsonArray().forEach(target::add);
        else target.add(element);
    }

    private static void validateLoot(Resource resource, JsonObject json, Set<String> resources, List<String> failures) {
        visitObjects(json, object -> {
            String type = string(object, "type");
            String name = string(object, "name");
            if ("minecraft:item".equals(type) && (name == null || !registered(BuiltInRegistries.ITEM.keySet(), name))) {
                failures.add("loot item " + resource.id() + " -> " + name);
            } else if ("minecraft:loot_table".equals(type) && name != null && name.startsWith(AbyssalCraft.MODID + ":")) {
                String target = ACRef.parse(name).getPath();
                String directory = resource.directory().equals("loot_table") ? "loot_table" : "loot_tables";
                if (!resources.contains("abyssalcraft/" + directory + "/" + target + ".json")) {
                    failures.add("loot table " + resource.id() + " -> " + name);
                }
            }
        });
    }

    private static void validateAdvancement(Resource resource, JsonObject json, Set<String> resources, List<String> failures) {
        String parent = string(json, "parent");
        if (parent != null && parent.startsWith(AbyssalCraft.MODID + ":")) {
            String directory = resource.directory().equals("advancement") ? "advancement" : "advancements";
            String target = "abyssalcraft/" + directory + "/" + ACRef.parse(parent).getPath() + ".json";
            if (!resources.contains(target)) failures.add("advancement parent " + resource.id() + " -> " + parent);
        }
        JsonObject display = object(json, "display");
        JsonObject icon = display == null ? null : object(display, "icon");
        String iconId = icon == null ? null : firstString(icon, "item", "id");
        if (iconId == null || !registered(BuiltInRegistries.ITEM.keySet(), iconId)) {
            failures.add("advancement icon " + resource.id() + " -> " + iconId);
        }
        JsonObject criteria = object(json, "criteria");
        if (criteria == null || criteria.size() == 0) failures.add("advancement predicate missing " + resource.id());
        if (criteria != null) criteria.entrySet().forEach(entry -> {
            JsonObject criterion = entry.getValue().isJsonObject() ? entry.getValue().getAsJsonObject() : null;
            if (criterion == null || string(criterion, "trigger") == null) {
                failures.add("advancement trigger " + resource.id() + " -> " + entry.getKey());
            } else {
                validateAdvancementItems(resource, criterion, failures);
            }
        });
    }

    private static void validateTag(Resource resource, JsonObject json, Set<String> resources, List<String> failures) {
        JsonArray values = json.getAsJsonArray("values");
        if (values == null) {
            failures.add("tag values missing " + resource.id());
            return;
        }
        String registry = resource.path().startsWith("item/") || resource.path().startsWith("items/") ? "item"
            : resource.path().startsWith("block/") || resource.path().startsWith("blocks/") ? "block" : null;
        for (JsonElement value : values) {
            String id = value.isJsonPrimitive() ? value.getAsString() : string(value.getAsJsonObject(), "id");
            if (id == null || !id.startsWith(AbyssalCraft.MODID + ":") && !id.startsWith("#" + AbyssalCraft.MODID + ":")) continue;
            if (id.startsWith("#")) {
                String target = id.substring(1 + AbyssalCraft.MODID.length() + 1);
                if (registry != null && !hasTag(resources, registry, target + ".json")) {
                    failures.add("tag reference " + resource.id() + " -> " + id);
                }
                continue;
            }
            boolean exists = registry == null || registry.equals("item") && registered(BuiltInRegistries.ITEM.keySet(), id)
                || registry.equals("block") && registered(BuiltInRegistries.BLOCK.keySet(), id);
            if (!exists) failures.add("tag entry " + resource.id() + " -> " + id);
        }
    }

    private static boolean hasTag(Set<String> resources, String registry, String path) {
        String plural = registry.equals("item") ? "items" : "blocks";
        return resources.contains("abyssalcraft/tags/" + registry + "/" + path)
            || resources.contains("abyssalcraft/tags/" + plural + "/" + path);
    }

    private static void validateAdvancementItems(Resource resource, JsonObject criterion, List<String> failures) {
        visitObjects(criterion, object -> {
            String item = firstString(object, "item", "id");
            if (item != null && item.startsWith(AbyssalCraft.MODID + ":")
                    && !registered(BuiltInRegistries.ITEM.keySet(), item)) {
                failures.add("advancement predicate item " + resource.id() + " -> " + item);
            }
            JsonArray items = object.getAsJsonArray("items");
            if (items != null) items.forEach(value -> {
                if (value.isJsonPrimitive()) {
                    String id = value.getAsString();
                    if (id.startsWith(AbyssalCraft.MODID + ":") && !registered(BuiltInRegistries.ITEM.keySet(), id)) {
                        failures.add("advancement predicate item " + resource.id() + " -> " + id);
                    }
                }
            });
        });
    }

    private static void validateWorldgen(Resource resource, JsonObject json, Set<String> resources,
                                         Map<String, Integer> typeConsumers, List<String> failures) {
        visitObjects(json, object -> {
            String type = string(object, "type");
            if (type != null && WORLDGEN_TYPES.contains(type)) typeConsumers.merge(type, 1, Integer::sum);
            if (type != null && type.startsWith(AbyssalCraft.MODID + ":")
                    && isWorldgenTypePosition(resource, object) && !WORLDGEN_TYPES.contains(type)
                    && !registered(BuiltInRegistries.ENTITY_TYPE.keySet(), type)) {
                failures.add("worldgen type " + resource.id() + " -> " + type);
            }
            validateWorldgenReference(resource, object, "feature", "worldgen/configured_feature", resources, failures);
            validateWorldgenReference(resource, object, "structure", "worldgen/structure", resources, failures);
            validateWorldgenReference(resource, object, "biome", "worldgen/biome", resources, failures);
            validateWorldgenReference(resource, object, "settings", "worldgen/noise_settings", resources, failures);
        });
    }

    private static boolean isWorldgenTypePosition(Resource resource, JsonObject object) {
        return resource.path().startsWith("configured_feature/") || resource.path().startsWith("structure/")
            || resource.path().startsWith("density_function/") || resource.path().startsWith("noise_settings/")
            || resource.path().startsWith("biome/") && object.has("weight");
    }

    private static void validateWorldgenReference(Resource owner, JsonObject object, String key, String directory,
                                                  Set<String> resources, List<String> failures) {
        String value = string(object, key);
        if (value == null || !value.startsWith(AbyssalCraft.MODID + ":")) return;
        String target = "abyssalcraft/" + directory + "/" + ACRef.parse(value).getPath() + ".json";
        if (!resources.contains(target)) failures.add("worldgen " + key + " " + owner.id() + " -> " + value);
    }

    private static void validateBlockLootCatalog(Set<String> resources, List<String> failures) {
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!AbyssalCraft.MODID.equals(id.getNamespace())) continue;
            if (block.asItem() == Items.AIR && !id.getPath().equals("shoggoth_biomass")) continue;
            String path = "blocks/" + id.getPath() + ".json";
            if (!resources.contains("abyssalcraft/loot_table/" + path)
                    || !resources.contains("abyssalcraft/loot_tables/" + path)) {
                failures.add("registered block loot " + id);
            }
        }
    }

    private static void validatePairs(Set<String> resources, List<String> failures) {
        for (String key : resources) {
            String[] parts = key.split("/", 3);
            if (parts.length < 3) continue;
            String counterpart = LEGACY_DIRECTORIES.get(parts[1]);
            if (counterpart == null) {
                counterpart = LEGACY_DIRECTORIES.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(parts[1]))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
            }
            if (counterpart == null) continue;
            String expected = parts[0] + "/" + counterpart + "/" + parts[2];
            if (!resources.contains(expected)) failures.add("version pair " + key + " -> " + expected);
        }
    }

    private static Resource resource(Path root, Path file) {
        Path relative = root.relativize(file);
        if (relative.getNameCount() < 3) return null;
        String namespace = relative.getName(0).toString();
        String directory = relative.getName(1).toString();
        String path = relative.subpath(2, relative.getNameCount()).toString().replace('\\', '/');
        return new Resource(namespace, directory, path);
    }

    private static boolean isDataFile(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".json") || name.endsWith(".nbt");
    }

    private static boolean sameVersionPair(Resource resource, Path previous, List<Path> roots) {
        Resource other = roots.stream().filter(previous::startsWith).findFirst().map(base -> resource(base, previous)).orElse(null);
        if (other == null || !resource.namespace().equals(other.namespace()) || !resource.path().equals(other.path())) return false;
        String counterpart = LEGACY_DIRECTORIES.get(resource.directory());
        return counterpart != null && counterpart.equals(other.directory())
            || LEGACY_DIRECTORIES.getOrDefault(other.directory(), "").equals(resource.directory());
    }

    private static Path locateProjectRoot(Path start) {
        for (Path current = start.toAbsolutePath(); current != null; current = current.getParent()) {
            if (Files.isRegularFile(current.resolve("settings.gradle.kts"))) return current;
        }
        throw new IllegalStateException("RR-DATAGEN could not locate project root from " + start);
    }

    private static boolean registered(Set<ResourceLocation> registry, String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        return id != null && registry.contains(id);
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }

    private static String firstString(JsonObject object, String... keys) {
        for (String key : keys) {
            String value = string(object, key);
            if (value != null) return value;
        }
        return null;
    }

    private static JsonObject object(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    private static void visitObjects(JsonElement element, ObjectVisitor visitor) {
        if (element.isJsonObject()) {
            visitor.accept(element.getAsJsonObject());
            element.getAsJsonObject().entrySet().forEach(entry -> visitObjects(entry.getValue(), visitor));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(value -> visitObjects(value, visitor));
        }
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-DATAGEN Closure Audit";
    }

    private record Resource(String namespace, String directory, String path) {
        String id() { return namespace + ":" + path; }
        String key() { return namespace + "/" + directory + "/" + path; }
        String logicalKey() { return namespace + "/" + LEGACY_DIRECTORIES.getOrDefault(directory, directory) + "/" + path; }
    }

    private record ParsedResource(Resource resource, JsonObject json) {}

    @FunctionalInterface
    private interface ObjectVisitor {
        void accept(JsonObject object);
    }
}