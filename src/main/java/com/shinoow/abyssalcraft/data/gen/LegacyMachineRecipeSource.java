package com.shinoow.abyssalcraft.data.gen;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.core.registries.BuiltInRegistries;

import com.shinoow.abyssalcraft.platform.ACRef;

final class LegacyMachineRecipeSource {

    private static final String RESOURCE = "/data/abyssalcraft/catalog/legacy_machine_calls.txt";
    private static final Path SOURCE = Path.of("docs", "AbyssalCraft-1.12.2", "src", "main", "java", "com",
        "shinoow", "abyssalcraft", "common", "AbyssalCrafting.java");
    private static final Pattern CALL = Pattern.compile("AbyssalCraftAPI\\.(addSingleCrystallization|addCrystallization|addTransmutation|addMaterialization)\\((.*)\\);");
    private static final Map<String, String> TAGS = tags();

    record StackRef(String id, int count, boolean tag) {}
    record Definition(int ordinal, int line, LegacyMachineRecipeCatalog.Kind kind, String call,
                      List<StackRef> inputs, List<StackRef> outputs,
                      List<MachineOutputResolutionCatalog.Resolution> outputResolutions, float experience) {}

    private LegacyMachineRecipeSource() {}

    static List<Definition> read() {
        List<String> lines = sourceLines();
        List<Definition> definitions = new ArrayList<>(LegacyMachineRecipeCatalog.SOURCE_COUNT);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            Matcher matcher = CALL.matcher(line);
            if (!matcher.find()) continue;
            LegacyMachineRecipeCatalog.Kind kind = switch (matcher.group(1)) {
                case "addSingleCrystallization", "addCrystallization" -> LegacyMachineRecipeCatalog.Kind.CRYSTALLIZATION;
                case "addTransmutation" -> LegacyMachineRecipeCatalog.Kind.TRANSMUTATION;
                case "addMaterialization" -> LegacyMachineRecipeCatalog.Kind.MATERIALIZATION;
                default -> throw new IllegalStateException(matcher.group(1));
            };
            definitions.add(parse(definitions.size() + 1, index + 1, kind, matcher.group(2), line));
        }
        Set<String> usedOutputTags = definitions.stream()
            .flatMap(definition -> definition.outputResolutions().stream())
            .map(MachineOutputResolutionCatalog.Resolution::tag)
            .collect(Collectors.toUnmodifiableSet());
        long resolvedOutputCount = definitions.stream()
            .mapToLong(definition -> definition.outputResolutions().size())
            .sum();
        MachineOutputResolutionCatalog.validate(usedOutputTags, resolvedOutputCount);
        return List.copyOf(definitions);
    }

    private static List<String> sourceLines() {
        try (InputStream stream = LegacyMachineRecipeSource.class.getResourceAsStream(RESOURCE)) {
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    return reader.lines().toList();
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read packaged legacy machine catalog " + RESOURCE, exception);
        }
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null) {
            Path candidate = directory.resolve(SOURCE);
            if (Files.isRegularFile(candidate)) {
                try {
                    return Files.readAllLines(candidate);
                } catch (IOException exception) {
                    throw new IllegalStateException("Cannot read legacy recipe source " + candidate, exception);
                }
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException("Cannot locate packaged or repository legacy machine source " + SOURCE);
    }

    private static Definition parse(int ordinal, int line, LegacyMachineRecipeCatalog.Kind kind,
                                    String arguments, String call) {
        List<String> raw = split(arguments);
        float experience = kind == LegacyMachineRecipeCatalog.Kind.MATERIALIZATION
            ? 0F : Float.parseFloat(raw.remove(raw.size() - 1).replace("F", ""));
        List<StackRef> stacks = stacks(raw);
        List<StackRef> inputs = kind == LegacyMachineRecipeCatalog.Kind.MATERIALIZATION
            ? List.copyOf(stacks.subList(1, stacks.size())) : List.of(stacks.get(0));
        List<StackRef> rawOutputs = kind == LegacyMachineRecipeCatalog.Kind.MATERIALIZATION
            ? List.of(stacks.get(0)) : List.copyOf(stacks.subList(1, stacks.size()));
        List<MachineOutputResolutionCatalog.Resolution> resolutions = rawOutputs.stream()
            .filter(StackRef::tag)
            .map(output -> MachineOutputResolutionCatalog.resolve(output.id()))
            .toList();
        List<StackRef> outputs = rawOutputs.stream().map(output -> {
            if (!output.tag()) return output;
            MachineOutputResolutionCatalog.Resolution resolution = MachineOutputResolutionCatalog.resolve(output.id());
            return new StackRef(resolution.item(), output.count(), false);
        }).toList();
        return new Definition(ordinal, line, kind, call, inputs, outputs, resolutions, experience);
    }

    private static List<StackRef> stacks(List<String> arguments) {
        List<StackRef> result = new ArrayList<>();
        for (int index = 0; index < arguments.size();) {
            String value = arguments.get(index);
            if (value.startsWith("\"") && index + 1 < arguments.size() && arguments.get(index + 1).matches("[0-9]+")) {
                result.add(tag(unquote(value), Integer.parseInt(arguments.get(index + 1))));
                index += 2;
            } else {
                result.add(stack(value));
                index++;
            }
        }
        return result;
    }

    private static StackRef stack(String expression) {
        if (expression.startsWith("\"")) return tag(unquote(expression), 1);
        if (expression.startsWith("PotionUtils.")) return new StackRef("minecraft:potion", 1, false);
        if (!expression.startsWith("new ItemStack(")) return new StackRef(id(expression, null), 1, false);
        List<String> parts = split(expression.substring("new ItemStack(".length(), expression.length() - 1));
        int count = parts.size() > 1 && parts.get(1).matches("[0-9]+") ? Integer.parseInt(parts.get(1)) : 1;
        return new StackRef(id(parts.get(0), parts.size() > 2 ? parts.get(2) : null), count, false);
    }

    private static String id(String expression, String metadata) {
        String value = expression.trim();
        if (value.startsWith("ACItems.")) {
            return switch (value.substring(8)) {
                case "dread_plagued_gateway_key" -> "abyssalcraft:dreadkey";
                case "omothol_forged_gateway_key" -> "abyssalcraft:gatewaykeyjzh";
                default -> "abyssalcraft:" + value.substring(8);
            };
        }
        if (value.startsWith("ACBlocks.")) return "abyssalcraft:" + value.substring(9);
        if (value.startsWith("Items.")) {
            String name = value.substring(6);
            if (name.equals("DYE")) return "4".equals(metadata) ? "minecraft:lapis_lazuli" : "minecraft:bone_meal";
            if (name.equals("COAL") && "1".equals(metadata)) return "minecraft:charcoal";
            if (name.equals("POTIONITEM")) return "minecraft:potion";
            return "minecraft:" + name.toLowerCase(Locale.ROOT);
        }
        if (value.startsWith("Blocks.")) {
            String name = value.substring(7);
            if (name.equals("STONEBRICK")) return "minecraft:stone_bricks";
            if (name.equals("DEADBUSH")) return "minecraft:dead_bush";
            if (name.equals("PRISMARINE")) return switch (metadata == null ? "0" : metadata) {
                case "1" -> "minecraft:prismarine_bricks";
                case "2" -> "minecraft:dark_prismarine";
                default -> "minecraft:prismarine";
            };
            if (name.equals("STONE") && metadata != null && !metadata.equals("0")) return switch (metadata) {
                case "1" -> "minecraft:granite";
                case "2" -> "minecraft:polished_granite";
                case "3" -> "minecraft:diorite";
                case "4" -> "minecraft:polished_diorite";
                case "5" -> "minecraft:andesite";
                case "6" -> "minecraft:polished_andesite";
                default -> "minecraft:stone";
            };
            return "minecraft:" + name.toLowerCase(Locale.ROOT);
        }
        throw new IllegalStateException("Unsupported legacy machine expression: " + expression);
    }

    private static StackRef tag(String legacy, int count) {
        if (legacy.startsWith("crystalCluster")) {
            return new StackRef("abyssalcraft:" + snake(legacy.substring("crystalCluster".length()))
                + "_crystal_cluster", count, false);
        }
        if (legacy.startsWith("crystalShard")) {
            return new StackRef("abyssalcraft:crystal_shard_" + snake(legacy.substring("crystalShard".length())), count, false);
        }
        if (legacy.startsWith("crystalFragment")) {
            return new StackRef("abyssalcraft:crystal_fragment_" + snake(legacy.substring("crystalFragment".length())), count, false);
        }
        if (legacy.startsWith("crystal")) {
            return new StackRef("abyssalcraft:crystal_" + snake(legacy.substring("crystal".length())), count, false);
        }
        return new StackRef(TAGS.getOrDefault(legacy, "legacy:" + legacy.toLowerCase(Locale.ROOT)), count, true);
    }

    private static String snake(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT)
            .replace("aluminum", "aluminium");
    }

    static String missing(Definition definition) {
        List<String> missing = new ArrayList<>();
        for (StackRef output : definition.outputs()) {
            if (output.tag()) missing.add("machine-recipe-model/output-tag/" + output.id());
            else if (!BuiltInRegistries.ITEM.containsKey(ACRef.parse(output.id()))) {
                missing.add("item-registry/" + output.id());
            }
        }
        for (StackRef input : definition.inputs()) {
            if (input.tag()) {
                if (input.id().startsWith("legacy:")) missing.add("common-item-tag/" + input.id().substring(7));
            } else if (!BuiltInRegistries.ITEM.containsKey(ACRef.parse(input.id()))) {
                missing.add("item-registry/" + input.id());
            }
        }
        return missing.isEmpty() ? null : String.join(",", missing);
    }

    private static List<String> split(String value) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        boolean quoted = false;
        int start = 0;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '"') quoted = !quoted;
            if (quoted) continue;
            if (character == '(') depth++;
            else if (character == ')') depth--;
            else if (character == ',' && depth == 0) {
                result.add(value.substring(start, index).trim());
                start = index + 1;
            }
        }
        result.add(value.substring(start).trim());
        return result;
    }

    private static String unquote(String value) {
        return value.substring(1, value.length() - 1);
    }

    private static Map<String, String> tags() {
        Map<String, String> result = new HashMap<>();
        result.put("oreAbyssalnite", "c:ores/abyssalnite");
        result.put("oreCoralium", "c:ores/coralium");
        result.put("oreIron", "c:ores/iron");
        result.put("oreGold", "c:ores/gold");
        result.put("oreCoal", "c:ores/coal");
        result.put("oreRedstone", "c:ores/redstone");
        result.put("oreDiamond", "c:ores/diamond");
        result.put("oreLapis", "c:ores/lapis");
        result.put("logWood", "minecraft:logs");
        result.put("plankWood", "minecraft:planks");
        result.put("treeSapling", "minecraft:saplings");
        result.put("treeLeaves", "minecraft:leaves");
        result.put("vine", "minecraft:vines");
        result.put("listAllmeatraw", "c:foods/raw_meat");
        for (String material : List.of("Tin", "Copper", "Aluminum", "Aluminium", "Zinc", "Magnesium", "Calcium",
            "Bronze", "Brass", "Iron", "Gold")) {
            String path = material.toLowerCase(Locale.ROOT).replace("aluminum", "aluminium");
            result.put("ingot" + material, "c:ingots/" + path);
            result.put("ore" + material, "c:ores/" + path);
            result.put("nugget" + material, "c:nuggets/" + path);
            result.put("dust" + material, "c:dusts/" + path);
            result.put("block" + material, "c:storage_blocks/" + path);
        }
        result.put("dustCoal", "c:dusts/coal");
        result.put("dustSulfur", "c:dusts/sulfur");
        result.put("dustSaltpeter", "c:dusts/saltpeter");
        result.put("oreSaltpeter", "c:ores/saltpeter");
        return Map.copyOf(result);
    }
}
