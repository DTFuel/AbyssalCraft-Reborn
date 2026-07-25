package com.shinoow.abyssalcraft.data.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

/**
 * Block loot-table datagen (owned by PK-5). A raw {@link DataProvider} that writes each AbyssalCraft
 * block's {@code blocks/<id>} loot table as JSON, so placed blocks stop dropping nothing.
 *
 * <p>Fork-free: the JSON is authored directly (avoiding the {@code BlockLootSubProvider}/{@code LootTableProvider}
 * constructors, which thread {@code HolderLookup.Provider} differently on 1.20.1 vs 1.21). Every run writes
 * both {@code loot_tables} and {@code loot_table}, because both loader nodes share one generated directory.
 * Most blocks drop themselves; ore tables are owned by {@link OreLootData}.
 */
public final class ACBlockLoot implements DataProvider {

    private final DataGenCompat.Gen gen;

    public ACBlockLoot(DataGenCompat.Gen gen) {
        this.gen = gen;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Path root = gen.packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(AbyssalCraft.MODID);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!AbyssalCraft.MODID.equals(id.getNamespace())) {
                continue;
            }
            String path = id.getPath();
            if (block.asItem() == Items.AIR && !"shoggoth_biomass".equals(path)) {
                continue;
            }
            if (OreLootData.ORE_IDS.contains(path)) {
                continue;
            }
            JsonObject table = "shoggoth_ooze".equals(path) || "shoggoth_biomass".equals(path)
                ? emptyTable()
                : "fused_abyssal_sand".equals(path)
                ? blockTable(itemEntry("abyssalcraft:abyssal_sand", 1, 1))
                : "portal_anchor".equals(path)
                    ? blockTable(itemEntry("abyssalcraft:monolith_stone", 1, 1))
                : "unchained_portal_anchor".equals(path)
                    ? blockTable(itemEntry("abyssalcraft:omothol_stone", 1, 1))
                : blockTable(itemEntry("abyssalcraft:" + path, 1, 1));
            futures.add(DataProvider.saveStable(output, table,
                root.resolve("loot_tables/blocks").resolve(path + ".json")));
            futures.add(DataProvider.saveStable(output, table,
                root.resolve("loot_table/blocks").resolve(path + ".json")));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "AbyssalCraft Block Loot";
    }

    private static JsonObject itemEntry(String item, int min, int max) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", item);
        if (min != 1 || max != 1) {
            JsonObject count = new JsonObject();
            count.addProperty("type", "minecraft:uniform");
            count.addProperty("min", min);
            count.addProperty("max", max);
            JsonObject setCount = new JsonObject();
            setCount.addProperty("function", "minecraft:set_count");
            setCount.add("count", count);
            JsonArray functions = new JsonArray();
            functions.add(setCount);
            entry.add("functions", functions);
        }
        return entry;
    }

    private static JsonObject blockTable(JsonObject entry) {
        JsonArray entries = new JsonArray();
        entries.add(entry);

        JsonObject survives = new JsonObject();
        survives.addProperty("condition", "minecraft:survives_explosion");
        JsonArray conditions = new JsonArray();
        conditions.add(survives);

        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        pool.add("entries", entries);
        pool.add("conditions", conditions);

        JsonArray pools = new JsonArray();
        pools.add(pool);

        JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools);
        return table;
    }

    private static JsonObject emptyTable() {
        JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", new JsonArray());
        return table;
    }
}
