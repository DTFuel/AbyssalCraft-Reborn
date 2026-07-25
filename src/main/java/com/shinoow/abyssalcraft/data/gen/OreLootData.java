package com.shinoow.abyssalcraft.data.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

/** Faithful 13-ore loot tables with Silk Touch and Fortune where the legacy ore yielded materials. */
public final class OreLootData implements DataProvider {

    public static final Set<String> ORE_IDS = Set.of(
        "coralium_ore", "abyssalnite_ore", "abyssal_abyssalnite_ore",
        "dreadlands_abyssalnite_ore", "dreaded_abyssalnite_ore", "nitre_ore",
        "abyssal_coralium_ore", "abyssal_iron_ore", "abyssal_gold_ore",
        "abyssal_diamond_ore", "abyssal_nitre_ore", "pearlescent_coralium_ore",
        "liquified_coralium_ore");

    private static final List<Ore> ORES = List.of(
        material("coralium_ore", "abyssalcraft:coralium_gem", 1, 3),
        self("abyssalnite_ore"),
        self("abyssal_abyssalnite_ore"),
        self("dreadlands_abyssalnite_ore"),
        self("dreaded_abyssalnite_ore"),
        material("nitre_ore", "abyssalcraft:nitre", 1, 3),
        material("abyssal_coralium_ore", "abyssalcraft:coralium_gem", 1, 3),
        self("abyssal_iron_ore"),
        self("abyssal_gold_ore"),
        material("abyssal_diamond_ore", "minecraft:diamond", 1, 1),
        material("abyssal_nitre_ore", "abyssalcraft:nitre", 1, 3),
        material("pearlescent_coralium_ore", "abyssalcraft:coralium_pearl", 1, 2),
        self("liquified_coralium_ore")
    );

    private final PackOutput packOutput;

    public OreLootData(DataGenCompat.Gen gen) {
        packOutput = gen.packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        require(ORES.size() == 13, "ore loot definition count changed");
        require(ORES.stream().map(Ore::id).collect(java.util.stream.Collectors.toSet()).equals(ORE_IDS),
            "ore loot definitions do not match the registered catalog");
        Path base = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(AbyssalCraft.MODID);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        int materialDrops = 0;
        for (Ore ore : ORES) {
            if (ore.fortune()) materialDrops++;
            JsonObject forge = table(ore, false);
            JsonObject neo = table(ore, true);
            futures.add(DataProvider.saveStable(output, forge,
                base.resolve("loot_tables/blocks").resolve(ore.id() + ".json")));
            futures.add(DataProvider.saveStable(output, neo,
                base.resolve("loot_table/blocks").resolve(ore.id() + ".json")));
        }
        require(materialDrops == 6, "legacy material-drop ore count changed");
        System.out.printf("RR_DATA_ORE_LOOT_OK ores=%d material=%d self=%d physical=%d%n",
            ORES.size(), materialDrops, ORES.size() - materialDrops, ORES.size() * 2);
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "AbyssalCraft Ore Loot";
    }

    private static JsonObject table(Ore ore, boolean neo) {
        JsonObject silkEntry = itemEntry("abyssalcraft:" + ore.id());
        JsonObject silkCondition = new JsonObject();
        silkCondition.addProperty("condition", "minecraft:match_tool");
        JsonObject predicate = new JsonObject();
        JsonArray enchantments = new JsonArray();
        JsonObject silk = new JsonObject();
        if (neo) silk.addProperty("enchantments", "minecraft:silk_touch");
        else silk.addProperty("enchantment", "minecraft:silk_touch");
        JsonObject levels = new JsonObject();
        levels.addProperty("min", 1);
        silk.add("levels", levels);
        enchantments.add(silk);
        if (neo) {
            JsonObject predicates = new JsonObject();
            predicates.add("minecraft:enchantments", enchantments);
            predicate.add("predicates", predicates);
        } else {
            predicate.add("enchantments", enchantments);
        }
        silkCondition.add("predicate", predicate);
        JsonArray conditions = new JsonArray();
        conditions.add(silkCondition);
        silkEntry.add("conditions", conditions);

        String normalItem = ore.fortune() ? ore.item() : "abyssalcraft:" + ore.id();
        JsonObject normalEntry = itemEntry(normalItem);
        JsonArray functions = new JsonArray();
        if (ore.min() != 1 || ore.max() != 1) {
            JsonObject count = new JsonObject();
            count.addProperty("type", "minecraft:uniform");
            count.addProperty("min", ore.min());
            count.addProperty("max", ore.max());
            JsonObject setCount = new JsonObject();
            setCount.addProperty("function", "minecraft:set_count");
            setCount.add("count", count);
            functions.add(setCount);
        }
        if (ore.fortune()) {
            JsonObject bonus = new JsonObject();
            bonus.addProperty("function", "minecraft:apply_bonus");
            bonus.addProperty("enchantment", "minecraft:fortune");
            bonus.addProperty("formula", "minecraft:ore_drops");
            functions.add(bonus);
        }
        JsonObject explosion = new JsonObject();
        explosion.addProperty("function", "minecraft:explosion_decay");
        functions.add(explosion);
        normalEntry.add("functions", functions);

        JsonArray children = new JsonArray();
        children.add(silkEntry);
        children.add(normalEntry);
        JsonObject alternatives = new JsonObject();
        alternatives.addProperty("type", "minecraft:alternatives");
        alternatives.add("children", children);
        JsonArray entries = new JsonArray();
        entries.add(alternatives);
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1.0F);
        pool.addProperty("bonus_rolls", 0.0F);
        pool.add("entries", entries);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools);
        table.addProperty("random_sequence", "abyssalcraft:blocks/" + ore.id());
        return table;
    }

    private static JsonObject itemEntry(String item) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", item);
        return entry;
    }

    private static Ore material(String id, String item, int min, int max) {
        return new Ore(id, item, min, max, true);
    }

    private static Ore self(String id) {
        return new Ore(id, "abyssalcraft:" + id, 1, 1, false);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private record Ore(String id, String item, int min, int max, boolean fortune) {}
}