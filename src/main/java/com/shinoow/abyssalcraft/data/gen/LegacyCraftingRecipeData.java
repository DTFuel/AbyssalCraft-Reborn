package com.shinoow.abyssalcraft.data.gen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

/** Migrates the 401 legacy crafting JSON files into the two modern recipe layouts. */
public final class LegacyCraftingRecipeData implements DataProvider {

    private static final int EXPECTED_RECIPES = 401;
    private static final Pattern BLOCK_REGISTRATION = Pattern.compile(
        "registerBlock\\(ACBlocks\\.([A-Za-z0-9_]+).*\\\"([a-zA-Z0-9_]+)\\\"\\s*\\);");
    private static final Pattern ITEM_REGISTRATION = Pattern.compile(
        "registerItem\\(ACItems\\.([A-Za-z0-9_]+)\\s*,\\s*\\\"([a-zA-Z0-9_]+)\\\"\\s*\\);");

    private static final Map<String, String> FIELD_OVERRIDES = Map.ofEntries(
        Map.entry("crystallizer_idle", "crystallizer"),
        Map.entry("transmutator_idle", "transmutator"),
        Map.entry("darklands_oak_wood", "darklands_oak_log"),
        Map.entry("darklands_oak_wood_2", "darklands_oak_log"),
        Map.entry("dreadlands_door", "dreadwood_door")
    );

    private static final Map<String, String> LEGACY_ID_OVERRIDES = Map.of(
        "ethaxiumbrick", "ethaxium_bricks",
        "ethbrick", "ethaxium_brick"
    );

    private static final Map<String, String> LEGACY_TAGS = Map.ofEntries(
        Map.entry("CHESTWOOD", "abyssalcraft:legacy/chest_wood"),
        Map.entry("DUSTSALTPETER", "abyssalcraft:legacy/dust_saltpeter"),
        Map.entry("DUSTSULFUR", "abyssalcraft:legacy/dust_sulfur"),
        Map.entry("INGOTIRON", "abyssalcraft:legacy/ingot_iron"),
        Map.entry("PLANKWOOD", "minecraft:planks"),
        Map.entry("STICKWOOD", "abyssalcraft:legacy/stick_wood"),
        Map.entry("DYECYAN", "abyssalcraft:legacy/dye_cyan"),
        Map.entry("DYEYELLOW", "abyssalcraft:legacy/dye_yellow"),
        Map.entry("DYEGRAY", "abyssalcraft:legacy/dye_gray"),
        Map.entry("DYEPURPLE", "abyssalcraft:legacy/dye_purple"),
        Map.entry("DYEBLUE", "abyssalcraft:legacy/dye_blue"),
        Map.entry("DYEORANGE", "abyssalcraft:legacy/dye_orange"),
        Map.entry("DYEBLACK", "abyssalcraft:legacy/dye_black")
    );

    private final PackOutput packOutput;

    public LegacyCraftingRecipeData(DataGenCompat.Gen gen) {
        packOutput = gen.packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Path dataRoot = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK);
        Path projectRoot = locateProjectRoot(dataRoot);
        Path recipeRoot = projectRoot.resolve(
            "docs/AbyssalCraft-1.12.2/src/main/resources/assets/abyssalcraft/recipes");
        Path blockHandler = projectRoot.resolve(
            "docs/AbyssalCraft-1.12.2/src/main/java/com/shinoow/abyssalcraft/init/BlockHandler.java");
        Path itemHandler = projectRoot.resolve(
            "docs/AbyssalCraft-1.12.2/src/main/java/com/shinoow/abyssalcraft/init/ItemHandler.java");

        try {
            List<Path> files;
            try (var paths = Files.list(recipeRoot)) {
                files = paths.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !path.getFileName().toString().equals("_constants.json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
            }
            require(files.size() == EXPECTED_RECIPES,
                "legacy crafting source count changed: " + files.size());
            require(readConstants(recipeRoot.resolve("_constants.json")).equals(LEGACY_TAGS.keySet()),
                "legacy crafting constants changed");

            Map<String, String> legacyFields = readLegacyRegistrations(blockHandler, itemHandler);
            legacyFields.put("cpearl", "coralium_pearl");
            requireMapping(legacyFields, "abyssalcraft:ethaxiumbrick", "abyssalcraft:ethaxium_bricks",
                "legacy Ethaxium brick block mapped to its material item");
            requireMapping(legacyFields, "abyssalcraft:ethbrick", "abyssalcraft:ethaxium_brick",
                "legacy Ethaxium brick item mapping changed");
            List<Audit> audits = new ArrayList<>();
            List<CompletableFuture<?>> futures = new ArrayList<>();
            Set<String> generatedIds = new HashSet<>();
            Path base = dataRoot.resolve(AbyssalCraft.MODID);

            for (Path file : files) {
                String legacyName = stripJson(file.getFileName().toString());
                try {
                    if (legacyName.equals("dltplank_alt")) {
                        audits.add(new Audit(legacyName, Status.REPLACED, "abyssalcraft:dltplank",
                            "legacy second Darklands log is the same modern darklands_oak_log"));
                        continue;
                    }
                    JsonObject legacy = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                    Converted converted = convert(legacy, legacyFields);
                    String replacement = replacementFor(projectRoot, converted.forge());
                    if (replacement != null) {
                        audits.add(new Audit(legacyName, Status.REPLACED, replacement,
                            "equivalent modern recipe already exists"));
                        continue;
                    }
                    require(generatedIds.add(legacyName), "duplicate generated recipe id: " + legacyName);
                    futures.add(DataProvider.saveStable(output, converted.forge(),
                        base.resolve("recipes").resolve(legacyName + ".json")));
                    futures.add(DataProvider.saveStable(output, converted.neo(),
                        base.resolve("recipe").resolve(legacyName + ".json")));
                    audits.add(new Audit(legacyName, Status.MIGRATED, "abyssalcraft:" + legacyName, ""));
                } catch (Blocked blocked) {
                    audits.add(new Audit(legacyName, Status.BLOCKED, "", blocked.getMessage()));
                }
            }

            writeAudit(projectRoot.resolve("docs/spec/rr-data-crafting-audit.csv"), audits);
            EnumMap<Status, Integer> counts = new EnumMap<>(Status.class);
            for (Status status : Status.values()) counts.put(status, 0);
            for (Audit audit : audits) counts.merge(audit.status(), 1, Integer::sum);
            require(audits.size() == EXPECTED_RECIPES, "crafting audit lost source files");
            require(counts.values().stream().mapToInt(Integer::intValue).sum() == EXPECTED_RECIPES,
                "crafting audit statuses do not close");
            System.out.printf("RR_DATA_CRAFTING_AUDIT_OK source=%d migrated=%d replaced=%d blocked=%d retired=%d%n",
                EXPECTED_RECIPES, counts.get(Status.MIGRATED), counts.get(Status.REPLACED),
                counts.get(Status.BLOCKED), counts.get(Status.RETIRED));
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to migrate legacy crafting recipes", exception);
        }
    }

    @Override
    public String getName() {
        return "AbyssalCraft Legacy Crafting Recipes";
    }

    private static Converted convert(JsonObject source, Map<String, String> legacyFields) throws Blocked {
        if (containsNbt(source)) throw new Blocked("legacy NBT/component recipe requires its content subsystem");
        String oldType = requiredString(source, "type");
        String type = switch (oldType) {
            case "minecraft:crafting_shaped", "forge:ore_shaped" -> "minecraft:crafting_shaped";
            case "minecraft:crafting_shapeless", "forge:ore_shapeless" -> "minecraft:crafting_shapeless";
            default -> throw new Blocked("unsupported legacy recipe type " + oldType);
        };

        JsonObject common = new JsonObject();
        common.addProperty("type", type);
        common.addProperty("category", "misc");
        if (source.has("group")) common.add("group", source.get("group").deepCopy());
        if (type.endsWith("shaped")) {
            common.add("pattern", source.getAsJsonArray("pattern").deepCopy());
            JsonObject key = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : source.getAsJsonObject("key").entrySet()) {
                key.add(entry.getKey(), convertIngredient(entry.getValue(), legacyFields));
            }
            common.add("key", key);
        } else {
            JsonArray ingredients = new JsonArray();
            for (JsonElement ingredient : source.getAsJsonArray("ingredients")) {
                ingredients.add(convertIngredient(ingredient, legacyFields));
            }
            common.add("ingredients", ingredients);
        }

        Result result = convertResult(source.getAsJsonObject("result"), legacyFields);
        JsonObject forge = common.deepCopy();
        JsonObject forgeResult = new JsonObject();
        forgeResult.addProperty("item", result.item());
        if (result.count() != 1) forgeResult.addProperty("count", result.count());
        forge.add("result", forgeResult);

        JsonObject neo = common.deepCopy();
        JsonObject neoResult = new JsonObject();
        neoResult.addProperty("id", result.item());
        if (result.count() != 1) neoResult.addProperty("count", result.count());
        neo.add("result", neoResult);
        return new Converted(forge, neo);
    }

    private static JsonObject convertIngredient(JsonElement element, Map<String, String> legacyFields)
            throws Blocked {
        if (element.isJsonArray()) {
            JsonArray alternatives = element.getAsJsonArray();
            if (alternatives.size() == 1) {
                return convertIngredient(alternatives.get(0), legacyFields);
            }
            throw new Blocked("multi-option legacy ingredient requires a modern tag");
        }
        if (!element.isJsonObject()) throw new Blocked("non-object legacy ingredient");
        JsonObject source = element.getAsJsonObject();
        if (!source.has("item")) throw new Blocked("legacy ingredient has no item");
        String item = source.get("item").getAsString();
        JsonObject target = new JsonObject();
        if (item.startsWith("#")) {
            String tag = LEGACY_TAGS.get(item.substring(1));
            if (tag == null) throw new Blocked("unknown legacy constant " + item);
            target.addProperty("tag", tag);
            return target;
        }
        int data = source.has("data") ? source.get("data").getAsInt() : 0;
        String mapped = mapItem(item, data, legacyFields, false);
        target.addProperty("item", mapped);
        return target;
    }

    private static Result convertResult(JsonObject source, Map<String, String> legacyFields) throws Blocked {
        if (!source.has("item")) throw new Blocked("legacy result has no plain item");
        int data = source.has("data") ? source.get("data").getAsInt() : 0;
        String mapped = mapItem(source.get("item").getAsString(), data, legacyFields, true);
        int count = source.has("count") ? source.get("count").getAsInt() : 1;
        if (count < 1 || count > 64) throw new Blocked("invalid legacy result count " + count);
        return new Result(mapped, count);
    }

    private static String mapItem(String legacyId, int data, Map<String, String> legacyFields,
                                  boolean result) throws Blocked {
        if (result && data != 0) throw new Blocked("non-zero legacy result metadata " + data);
        if (!result && data != 0 && data != 32767) {
            if (legacyId.equals("minecraft:coal") && data == 1) return "minecraft:charcoal";
            if (!(data == 4 && (legacyId.equals("abyssalcraft:dltplank")
                    || legacyId.equals("abyssalcraft:dreadplanks")))) {
                throw new Blocked("unmapped legacy ingredient metadata " + legacyId + "@" + data);
            }
        }
        if (!legacyId.startsWith("abyssalcraft:")) {
            if (itemExists(legacyId)) return legacyId;
            throw new Blocked("missing vanilla item " + legacyId);
        }

        String oldPath = legacyId.substring("abyssalcraft:".length());
        String pathOverride = LEGACY_ID_OVERRIDES.get(oldPath);
        if (pathOverride != null) {
            String mapped = "abyssalcraft:" + pathOverride;
            if (itemExists(mapped)) return mapped;
            throw new Blocked("modern override not registered for " + legacyId + " (" + mapped + ")");
        }
        String direct = "abyssalcraft:" + oldPath;
        if (itemExists(direct)) return direct;
        String field = legacyFields.get(oldPath);
        if (field == null) throw new Blocked("unknown legacy registry id " + legacyId);
        String modernPath = FIELD_OVERRIDES.getOrDefault(field, field);
        String mapped = "abyssalcraft:" + modernPath;
        if (itemExists(mapped)) return mapped;
        throw new Blocked("modern content not registered for " + legacyId + " (" + modernPath + ")");
    }

    private static boolean itemExists(String id) {
        return BuiltInRegistries.ITEM.getOptional(ACRef.parse(id)).isPresent();
    }

    private static String replacementFor(Path projectRoot, JsonObject recipe) {
        String semantic = semanticReplacement(recipe);
        if (semantic != null) return semantic;
        return null;
    }

    private static String semanticReplacement(JsonObject recipe) {
        JsonObject result = recipe.getAsJsonObject("result");
        String outputId = result.get("item").getAsString();
        if (!outputId.startsWith("abyssalcraft:")) return null;
        String output = outputId.substring("abyssalcraft:".length());
        int count = result.has("count") ? result.get("count").getAsInt() : 1;
        List<String> ingredients = ingredientItems(recipe);
        if (ingredients.size() == 9 && new HashSet<>(ingredients).size() == 1) {
            String input = ingredients.get(0);
            if (output.startsWith("block_of_") && input.endsWith("_ingot")) {
                return "abyssalcraft:" + output;
            }
            if (output.endsWith("_crystal_cluster") && input.startsWith("crystal_")) {
                return "abyssalcraft:" + output;
            }
        }
        if (ingredients.size() == 1 && count == 9) {
            String input = ingredients.get(0);
            if (input.startsWith("block_of_") && output.endsWith("_ingot")) {
                return "abyssalcraft:" + output + "_from_block";
            }
            if (input.endsWith("_crystal_cluster") && output.startsWith("crystal_")) {
                return "abyssalcraft:" + output + "_from_cluster";
            }
        }
        return null;
    }

    private static List<String> ingredientItems(JsonObject recipe) {
        List<String> items = new ArrayList<>();
        if (recipe.get("type").getAsString().endsWith("shaped")) {
            JsonObject key = recipe.getAsJsonObject("key");
            for (JsonElement row : recipe.getAsJsonArray("pattern")) {
                for (char symbol : row.getAsString().toCharArray()) {
                    if (symbol == ' ') continue;
                    JsonObject ingredient = key.getAsJsonObject(String.valueOf(symbol));
                    if (!ingredient.has("item")) return List.of();
                    String item = ingredient.get("item").getAsString();
                    if (!item.startsWith("abyssalcraft:")) return List.of();
                    items.add(item.substring("abyssalcraft:".length()));
                }
            }
        } else {
            for (JsonElement element : recipe.getAsJsonArray("ingredients")) {
                JsonObject ingredient = element.getAsJsonObject();
                if (!ingredient.has("item")) return List.of();
                String item = ingredient.get("item").getAsString();
                if (!item.startsWith("abyssalcraft:")) return List.of();
                items.add(item.substring("abyssalcraft:".length()));
            }
        }
        return items;
    }

    private static Map<String, String> readLegacyRegistrations(Path blockHandler, Path itemHandler)
            throws IOException {
        Map<String, String> result = new HashMap<>();
        readRegistrations(blockHandler, BLOCK_REGISTRATION, result);
        readRegistrations(itemHandler, ITEM_REGISTRATION, result);
        return result;
    }

    private static void readRegistrations(Path source, Pattern pattern, Map<String, String> result)
            throws IOException {
        for (String line : Files.readAllLines(source)) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) result.put(matcher.group(2), matcher.group(1));
        }
    }

    private static Set<String> readConstants(Path source) throws IOException {
        Set<String> names = new HashSet<>();
        for (JsonElement element : JsonParser.parseString(Files.readString(source)).getAsJsonArray()) {
            names.add(element.getAsJsonObject().get("name").getAsString());
        }
        return names;
    }

    private static boolean containsNbt(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("nbt") || object.has("type")
                    && object.get("type").getAsString().equals("minecraft:item_nbt")) return true;
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (containsNbt(entry.getValue())) return true;
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) if (containsNbt(child)) return true;
        }
        return false;
    }

    private static Path locateProjectRoot(Path start) {
        for (Path current = start.toAbsolutePath(); current != null; current = current.getParent()) {
            if (Files.isDirectory(current.resolve("docs/AbyssalCraft-1.12.2"))) return current;
        }
        throw new IllegalStateException("Unable to locate project root from " + start);
    }

    private static void writeAudit(Path path, List<Audit> audits) throws IOException {
        StringBuilder csv = new StringBuilder("legacy_file,status,modern_recipe,reason\n");
        for (Audit audit : audits) {
            csv.append(csv(audit.legacyName())).append(',').append(audit.status()).append(',')
                .append(csv(audit.modernRecipe())).append(',').append(csv(audit.reason())).append('\n');
        }
        Files.writeString(path, csv.toString(), StandardCharsets.UTF_8);
    }

    private static String csv(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static String requiredString(JsonObject object, String key) throws Blocked {
        if (!object.has(key)) throw new Blocked("missing legacy " + key);
        return object.get(key).getAsString();
    }

    private static void requireMapping(Map<String, String> legacyFields, String legacyId,
                                       String expected, String message) {
        try {
            require(mapItem(legacyId, 0, legacyFields, false).equals(expected), message);
        } catch (Blocked blocked) {
            throw new IllegalStateException(message, blocked);
        }
    }

    private static String stripJson(String name) {
        return name.substring(0, name.length() - ".json".length());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private enum Status { MIGRATED, REPLACED, BLOCKED, RETIRED }

    private record Audit(String legacyName, Status status, String modernRecipe, String reason) {}
    private record Converted(JsonObject forge, JsonObject neo) {}
    private record Result(String item, int count) {}

    private static final class Blocked extends Exception {
        private Blocked(String message) {
            super(message);
        }
    }
}