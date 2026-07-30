package com.shinoow.abyssalcraft.data.gen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.block.ContentSelfTest;
import com.shinoow.abyssalcraft.content.entity.behavior.EntityLootAudit;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

/** Authoritative conversion of all 69 legacy entity loot tables into both modern data directories. */
public final class EntityLootData implements DataProvider {

    private static final int LEGACY_TABLES = 69;

    private static final Map<String, String> ITEM_IDS = Map.ofEntries(
        Map.entry("abyssalcraft:abyssal_ghoul_flesh", "abyssalcraft:abyssal_ghoul_flesh"),
        Map.entry("abyssalcraft:anti_ghoul_flesh", "abyssalcraft:anti_ghoul_flesh"),
        Map.entry("abyssalcraft:antibeef", "abyssalcraft:anti_beef"),
        Map.entry("abyssalcraft:antibone", "abyssalcraft:anti_bone"),
        Map.entry("abyssalcraft:antichicken", "abyssalcraft:anti_chicken"),
        Map.entry("abyssalcraft:anticorflesh", "abyssalcraft:anti_plagued_flesh"),
        Map.entry("abyssalcraft:antiflesh", "abyssalcraft:rotten_anti_flesh"),
        Map.entry("abyssalcraft:antipork", "abyssalcraft:anti_pork"),
        Map.entry("abyssalcraft:antispidereye", "abyssalcraft:anti_spider_eye"),
        Map.entry("abyssalcraft:coralium", "abyssalcraft:coralium_gem"),
        Map.entry("abyssalcraft:corflesh", "abyssalcraft:coralium_plagued_flesh"),
        Map.entry("abyssalcraft:cudgel", "abyssalcraft:cudgel"),
        Map.entry("abyssalcraft:dghead", "abyssalcraft:dghead"),
        Map.entry("abyssalcraft:dreaded_ghoul_flesh", "abyssalcraft:dreaded_ghoul_flesh"),
        Map.entry("abyssalcraft:dreadfragment", "abyssalcraft:dread_fragment"),
        Map.entry("abyssalcraft:dreadshard", "abyssalcraft:dreaded_shard_of_abyssalnite"),
        Map.entry("abyssalcraft:eldritchscale", "abyssalcraft:eldritch_scale"),
        Map.entry("abyssalcraft:ethaxiumingot", "abyssalcraft:ethaxium_ingot"),
        Map.entry("abyssalcraft:ghoul_flesh", "abyssalcraft:ghoul_flesh"),
        Map.entry("abyssalcraft:ohead", "abyssalcraft:ohead"),
        Map.entry("abyssalcraft:omotholflesh", "abyssalcraft:omothol_ghoul_flesh"),
        Map.entry("abyssalcraft:phead", "abyssalcraft:phead"),
        Map.entry("abyssalcraft:shadow_ghoul_flesh", "abyssalcraft:shadow_ghoul_flesh"),
        Map.entry("abyssalcraft:shadowfragment", "abyssalcraft:shadow_fragment"),
        Map.entry("abyssalcraft:shadowgem", "abyssalcraft:shadow_gem"),
        Map.entry("abyssalcraft:shadowshard", "abyssalcraft:shadow_shard"),
        Map.entry("abyssalcraft:shoggothflesh_abyssal", "abyssalcraft:abyssal_shoggoth_flesh"),
        Map.entry("abyssalcraft:shoggothflesh_dreaded", "abyssalcraft:dreaded_shoggoth_flesh"),
        Map.entry("abyssalcraft:shoggothflesh_omothol", "abyssalcraft:omothol_shoggoth_flesh"),
        Map.entry("abyssalcraft:shoggothflesh_overworld", "abyssalcraft:overworld_shoggoth_flesh"),
        Map.entry("abyssalcraft:shoggothflesh_shadow", "abyssalcraft:shadow_shoggoth_flesh"),
        Map.entry("abyssalcraft:whead", "abyssalcraft:whead"),
        Map.entry("minecraft:dye", "minecraft:ink_sac"),
        Map.entry("minecraft:wool", "minecraft:white_wool")
    );

    private final PackOutput packOutput;

    public EntityLootData(DataGenCompat.Gen gen) {
        packOutput = gen.packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        try {
            ContentSelfTest.run();
            EntityLootAudit.validate();
            Path dataRoot = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK);
            Path projectRoot = locateProjectRoot(dataRoot);
            Path source = projectRoot.resolve(
                "docs/AbyssalCraft-1.12.2/src/main/resources/assets/abyssalcraft/loot_tables/entities");
            require(Files.isDirectory(source), "legacy entity loot source missing: " + source);
            List<Path> files;
            try (var stream = Files.list(source)) {
                files = stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString())).toList();
            }
            require(files.size() == LEGACY_TABLES, "legacy entity loot source count changed: " + files.size());

            Map<String, JsonObject[]> converted = new HashMap<>();
            for (Path file : files) {
                String id = file.getFileName().toString().replaceFirst("\\.json$", "");
                JsonObject legacy = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                converted.put(id, new JsonObject[] { convert(legacy, false), convert(legacy, true) });
            }
            applyBossRewards(converted);

            Set<String> auditIds = new HashSet<>(EntityLootAudit.entries().stream()
                .map(EntityLootAudit.Entry::legacyTable).toList());
            require(converted.keySet().equals(auditIds), "69 entity loot source/audit id mismatch");

            Map<String, String> aliases = EntityLootAudit.modernAliases();
            require(aliases.size() == 28, "modern entity loot alias count changed: " + aliases.size());
            Set<String> outputIds = new HashSet<>(converted.keySet());
            for (var alias : aliases.entrySet()) {
                require(converted.containsKey(alias.getValue()), "loot alias source missing: " + alias);
                require(outputIds.add(alias.getKey()), "loot alias collides with legacy table: " + alias.getKey());
            }
            require(outputIds.size() == 97, "entity loot logical output count changed: " + outputIds.size());

            Path base = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(AbyssalCraft.MODID);
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (var entry : converted.entrySet()) {
                writeBoth(output, futures, base, entry.getKey(), entry.getValue());
            }
            for (var alias : aliases.entrySet()) {
                writeBoth(output, futures, base, alias.getKey(), converted.get(alias.getValue()));
            }
            System.out.printf("RR_ENTITY_LOOT_DATA_OK audit=69 aliases=28 logical=97 physical=194%n");
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    private static void writeBoth(CachedOutput output, List<CompletableFuture<?>> futures, Path base,
                                  String id, JsonObject[] tables) {
        futures.add(DataProvider.saveStable(output, tables[0],
            base.resolve("loot_tables/entities").resolve(id + ".json")));
        futures.add(DataProvider.saveStable(output, tables[1],
            base.resolve("loot_table/entities").resolve(id + ".json")));
    }

    private static JsonObject convert(JsonObject legacy, boolean modern) {
        JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:entity");
        JsonArray pools = new JsonArray();
        if (legacy.has("pools")) {
            for (JsonElement poolElement : legacy.getAsJsonArray("pools")) {
                JsonObject oldPool = poolElement.getAsJsonObject();
                JsonObject pool = new JsonObject();
                pool.addProperty("rolls", oldPool.has("rolls") ? oldPool.get("rolls").getAsInt() : 1);
                if (oldPool.has("conditions")) pool.add("conditions", convertConditions(oldPool.getAsJsonArray("conditions"), modern));
                JsonArray entries = new JsonArray();
                for (JsonElement entryElement : oldPool.getAsJsonArray("entries")) {
                    entries.add(convertEntry(entryElement.getAsJsonObject(), modern));
                }
                pool.add("entries", entries);
                pools.add(pool);
            }
        }
        table.add("pools", pools);
        validateItems(table);
        return table;
    }

    private static void applyBossRewards(Map<String, JsonObject[]> tables) {
        addFixedDrops(tables, "chagaroth", false,
            drop("abyssalcraft:dread_fragment", 20),
            drop("abyssalcraft:dreaded_shard_of_abyssalnite", 5),
            drop("abyssalcraft:dreadium_ingot", 5),
            drop("abyssalcraft:dreadkey", 1));
        addFixedDrops(tables, "sacthoth", false,
            drop("abyssalcraft:shadow_fragment", 20),
            drop("abyssalcraft:shadow_shard", 10),
            drop("abyssalcraft:shadow_gem", 5),
            drop("abyssalcraft:shard_of_oblivion", 5),
            drop("abyssalcraft:soulreaper", 1));
        addFixedDrops(tables, "asorah", false,
            drop("abyssalcraft:chunk_of_coralium", 5),
            drop("abyssalcraft:refined_coralium_ingot", 5),
            drop("abyssalcraft:coralium_plagued_flesh", 5),
            drop("abyssalcraft:eye_of_the_abyss", 1));
        addFixedDrops(tables, "jzahar", true,
            drop("abyssalcraft:essence_of_the_gatekeeper", 1));
    }

    private static void addFixedDrops(Map<String, JsonObject[]> tables, String tableId,
                                      boolean killedByPlayer, FixedDrop... drops) {
        JsonObject[] pair = tables.get(tableId);
        require(pair != null, "boss loot table missing: " + tableId);
        for (int index = 0; index < pair.length; index++) {
            boolean modern = index == 1;
            JsonArray pools = pair[index].getAsJsonArray("pools");
            for (FixedDrop drop : drops) pools.add(fixedDropPool(drop, killedByPlayer, modern));
            validateItems(pair[index]);
        }
    }

    private static JsonObject fixedDropPool(FixedDrop drop, boolean killedByPlayer, boolean modern) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", drop.itemId());
        if (drop.count() != 1) {
            JsonObject setCount = new JsonObject();
            setCount.addProperty("function", "minecraft:set_count");
            setCount.addProperty("count", drop.count());
            if (modern) setCount.addProperty("add", false);
            JsonArray functions = new JsonArray();
            functions.add(setCount);
            entry.add("functions", functions);
        }

        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        pool.add("entries", entries);
        if (killedByPlayer) {
            JsonObject condition = new JsonObject();
            condition.addProperty("condition", "minecraft:killed_by_player");
            JsonArray conditions = new JsonArray();
            conditions.add(condition);
            pool.add("conditions", conditions);
        }
        return pool;
    }

    private static FixedDrop drop(String itemId, int count) {
        return new FixedDrop(itemId, count);
    }

    private record FixedDrop(String itemId, int count) {
        private FixedDrop {
            require(count > 0, "boss loot count must be positive: " + itemId);
        }
    }

    private static JsonObject convertEntry(JsonObject legacy, boolean modern) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        String oldId = legacy.get("name").getAsString();
        entry.addProperty("name", ITEM_IDS.getOrDefault(oldId, oldId));
        if (legacy.has("weight")) entry.addProperty("weight", legacy.get("weight").getAsInt());
        JsonArray functions = new JsonArray();
        if (legacy.has("functions")) {
            for (JsonElement functionElement : legacy.getAsJsonArray("functions")) {
                JsonObject function = functionElement.getAsJsonObject();
                String type = namespaced(function.get("function").getAsString());
                if (type.equals("minecraft:set_data")) continue;
                if (type.equals("minecraft:set_count")) {
                    JsonObject converted = new JsonObject();
                    converted.addProperty("function", "minecraft:set_count");
                    if (modern) converted.addProperty("add", false);
                    converted.add("count", uniform(function.getAsJsonObject("count"), modern));
                    functions.add(converted);
                } else if (type.equals("minecraft:looting_enchant")) {
                    JsonObject converted = new JsonObject();
                    converted.addProperty("function", modern
                        ? "minecraft:enchanted_count_increase" : "minecraft:looting_enchant");
                    if (modern) converted.addProperty("enchantment", "minecraft:looting");
                    converted.add("count", uniform(function.getAsJsonObject("count"), modern));
                    functions.add(converted);
                } else {
                    throw new IllegalStateException("unsupported legacy entity loot function: " + type);
                }
            }
        }
        if (!functions.isEmpty()) entry.add("functions", functions);
        return entry;
    }

    private static JsonArray convertConditions(JsonArray legacy, boolean modern) {
        JsonArray conditions = new JsonArray();
        for (JsonElement element : legacy) {
            JsonObject condition = element.getAsJsonObject();
            String type = namespaced(condition.get("condition").getAsString());
            if (type.equals("minecraft:killed_by_player")) {
                JsonObject converted = new JsonObject();
                converted.addProperty("condition", type);
                conditions.add(converted);
            } else if (type.equals("minecraft:random_chance_with_looting")) {
                JsonObject converted = new JsonObject();
                if (modern) {
                    converted.addProperty("condition", "minecraft:random_chance_with_enchanted_bonus");
                    converted.addProperty("enchantment", "minecraft:looting");
                    converted.addProperty("unenchanted_chance", condition.get("chance").getAsFloat());
                    JsonObject chance = new JsonObject();
                    chance.addProperty("type", "minecraft:linear");
                    chance.addProperty("base", condition.get("chance").getAsFloat()
                        + condition.get("looting_multiplier").getAsFloat());
                    chance.addProperty("per_level_above_first", condition.get("looting_multiplier").getAsFloat());
                    converted.add("enchanted_chance", chance);
                } else {
                    converted.addProperty("condition", type);
                    converted.addProperty("chance", condition.get("chance").getAsFloat());
                    converted.addProperty("looting_multiplier", condition.get("looting_multiplier").getAsFloat());
                }
                conditions.add(converted);
            } else {
                throw new IllegalStateException("unsupported legacy entity loot condition: " + type);
            }
        }
        return conditions;
    }

    private static JsonObject uniform(JsonObject old, boolean modern) {
        JsonObject value = new JsonObject();
        value.addProperty("type", "minecraft:uniform");
        if (modern) {
            value.addProperty("min", old.get("min").getAsFloat());
            value.addProperty("max", old.get("max").getAsFloat());
        } else {
            value.addProperty("min", old.get("min").getAsInt());
            value.addProperty("max", old.get("max").getAsInt());
        }
        return value;
    }

    private static void validateItems(JsonObject table) {
        for (JsonElement pool : table.getAsJsonArray("pools")) {
            for (JsonElement entry : pool.getAsJsonObject().getAsJsonArray("entries")) {
                ResourceLocation id = ResourceLocation.tryParse(entry.getAsJsonObject().get("name").getAsString());
                require(id != null && BuiltInRegistries.ITEM.containsKey(id)
                    && BuiltInRegistries.ITEM.get(id) != Items.AIR, "entity loot references missing item: " + id);
            }
        }
    }

    private static String namespaced(String id) {
        return id.contains(":") ? id : "minecraft:" + id;
    }

    private static Path locateProjectRoot(Path start) {
        for (Path current = start.toAbsolutePath(); current != null; current = current.getParent()) {
            if (Files.isDirectory(current.resolve("docs/AbyssalCraft-1.12.2"))) return current;
        }
        throw new IllegalStateException("Unable to locate project root from " + start);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    @Override
    public String getName() {
        return "AbyssalCraft 69 Entity Loot";
    }
}