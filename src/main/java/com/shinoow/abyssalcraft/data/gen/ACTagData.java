package com.shinoow.abyssalcraft.data.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;

import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.content.block.deco.DecoPlantBlock;
import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;
import com.shinoow.abyssalcraft.content.block.ore.OreBlocks;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.platform.DataGenCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;

/** Loader-neutral M1 block/item tags, emitted to both 1.20 and 1.21 directory layouts. */
public final class ACTagData implements DataProvider {

    private static final String[] WOOD_FAMILIES = {"darklands_oak", "dreadwood"};
    private final PackOutput packOutput;

    public ACTagData(DataGenCompat.Gen gen) {
        packOutput = gen.packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<TagPath, List<String>> tags = new LinkedHashMap<>();
        for (String family : WOOD_FAMILIES) {
            add(tags, block("minecraft", "logs"), id(family + "_log"));
            add(tags, item("minecraft", "logs"), id(family + "_log"));
            add(tags, block("minecraft", "logs_that_burn"), id(family + "_log"));
            add(tags, item("minecraft", "logs_that_burn"), id(family + "_log"));
            add(tags, block("minecraft", "planks"), id(family + "_planks"));
            add(tags, item("minecraft", "planks"), id(family + "_planks"));
            add(tags, block("minecraft", "leaves"), id(family + "_leaves"));
            add(tags, item("minecraft", "leaves"), id(family + "_leaves"));
            add(tags, block("minecraft", "saplings"), id(family + "_sapling"));
            add(tags, item("minecraft", "saplings"), id(family + "_sapling"));
            add(tags, block("minecraft", "wooden_slabs"), id(family + "_slab"));
            add(tags, item("minecraft", "wooden_slabs"), id(family + "_slab"));
            add(tags, block("minecraft", "wooden_stairs"), id(family + "_stairs"));
            add(tags, item("minecraft", "wooden_stairs"), id(family + "_stairs"));
            add(tags, block("minecraft", "wooden_fences"), id(family + "_fence"));
            add(tags, item("minecraft", "wooden_fences"), id(family + "_fence"));
            add(tags, block("minecraft", "fence_gates"), id(family + "_fence_gate"));
            add(tags, item("minecraft", "fence_gates"), id(family + "_fence_gate"));
            add(tags, block("minecraft", "wooden_doors"), id(family + "_door"));
            add(tags, item("minecraft", "wooden_doors"), id(family + "_door"));
            add(tags, block("minecraft", "wooden_buttons"), id(family + "_button"));
            add(tags, item("minecraft", "wooden_buttons"), id(family + "_button"));
            add(tags, block("minecraft", "wooden_pressure_plates"), id(family + "_pressure_plate"));
            add(tags, item("minecraft", "wooden_pressure_plates"), id(family + "_pressure_plate"));
        }
        add(tags, block("minecraft", "logs"), id("dead_tree_log"));
        add(tags, item("minecraft", "logs"), id("dead_tree_log"));
        add(tags, block("minecraft", "logs_that_burn"), id("dead_tree_log"));
        add(tags, item("minecraft", "logs_that_burn"), id("dead_tree_log"));

        Set<String> m1Blocks = new LinkedHashSet<>();
        Set<String> mineable = new LinkedHashSet<>();
        for (Supplier<? extends Block> supplier : BaseBlocks.BLOCKS.entries()) {
            Block value = supplier.get();
            String blockId = blockId(value);
            m1Blocks.add(blockId);
            classifyBase(tags, value, blockId, mineable);
        }
        for (Supplier<? extends Block> supplier : DecoBlocks.BLOCKS.entries()) {
            Block value = supplier.get();
            String blockId = blockId(value);
            m1Blocks.add(blockId);
            if (value instanceof DecoPlantBlock) {
                continue;
            }
                if (blockId.equals(id("dreadlands_dirt")) || blockId.equals(id("dreadlands_grass"))
                    || blockId.equals(id("dreadlands_muck")) || blockId.equals(id("abyssal_sand"))) {
                add(tags, block("minecraft", "mineable/shovel"), blockId);
            } else {
                add(tags, block("minecraft", "mineable/pickaxe"), blockId);
            }
            mineable.add(blockId);
        }
        for (Supplier<Block> supplier : OreBlocks.ALL) {
            String blockId = blockId(supplier.get());
            m1Blocks.add(blockId);
            mineable.add(blockId);
            add(tags, block("minecraft", "mineable/pickaxe"), blockId);
        }
        for (Supplier<? extends Block> supplier : CrystalClusterBlocks.BLOCKS.entries()) {
            String blockId = blockId(supplier.get());
            m1Blocks.add(blockId);
            mineable.add(blockId);
            add(tags, block("minecraft", "mineable/pickaxe"), blockId);
            add(tags, block("abyssalcraft", "crystal_clusters"), blockId);
            add(tags, item("abyssalcraft", "crystal_clusters"), blockId);
        }
        for (Supplier<? extends Block> supplier : EnergyBlocks.BLOCKS.entries()) {
            add(tags, block("minecraft", "mineable/pickaxe"), blockId(supplier.get()));
        }
        addAll(tags, block("abyssalcraft", "m1_blocks"), m1Blocks.toArray(String[]::new));
        addAll(tags, item("abyssalcraft", "m1_blocks"), m1Blocks.toArray(String[]::new));

        String[] ironOres = {
            id("coralium_ore"), id("abyssalnite_ore"), id("abyssal_abyssalnite_ore"),
            id("nitre_ore"), id("abyssal_iron_ore"), id("abyssal_gold_ore"),
            id("abyssal_diamond_ore"), id("abyssal_nitre_ore")
        };
        String[] diamondOres = {
            "#abyssalcraft:crystal_clusters", id("dreadlands_abyssalnite_ore"),
            id("dreaded_abyssalnite_ore"), id("abyssal_coralium_ore"),
            id("pearlescent_coralium_ore"), id("liquified_coralium_ore")
        };
        addAll(tags, block("minecraft", "needs_iron_tool"), ironOres);
        addAll(tags, block("minecraft", "needs_diamond_tool"), diamondOres);

        String[] netheriteRequired = {
            id("dreadlands_abyssalnite_ore"), id("dreaded_abyssalnite_ore"),
            id("liquified_coralium_ore")
        };
        addAll(tags, forgeBlock("forge", "needs_netherite_tool"), netheriteRequired);
        addAll(tags, neoBlock("neoforge", "needs_netherite_tool"), netheriteRequired);
        add(tags, block("abyssalcraft", "requires_refined_coralium_tool"),
            id("pearlescent_coralium_ore"));
        add(tags, block("abyssalcraft", "requires_dreadium_tool"), id("omothol_stone"));

        String[] ethaxiumOnly = {
            id("ethaxium"), id("ethaxium_bricks"), id("chiseled_ethaxium_brick"),
            id("cracked_ethaxium_brick"), id("ethaxium_pillar"), id("ethaxium_brick_slab"),
            id("ethaxium_brick_stairs"), id("ethaxium_brick_fence"), id("dark_ethaxium_brick"),
            id("chiseled_dark_ethaxium_brick"), id("cracked_dark_ethaxium_brick"),
            id("dark_ethaxium_pillar"), id("dark_ethaxium_brick_slab"),
            id("dark_ethaxium_brick_stairs"), id("dark_ethaxium_brick_fence"), id("materializer")
        };
        addAll(tags, block("abyssalcraft", "requires_ethaxium_tool"), ethaxiumOnly);

        addAll(tags, neoBlock("minecraft", "incorrect_for_wooden_tool"),
            "#minecraft:needs_iron_tool", "#minecraft:needs_diamond_tool",
            "#neoforge:needs_netherite_tool", "#abyssalcraft:requires_refined_coralium_tool",
            "#abyssalcraft:requires_dreadium_tool", "#abyssalcraft:requires_ethaxium_tool");
        addAll(tags, neoBlock("minecraft", "incorrect_for_stone_tool"),
            "#minecraft:needs_iron_tool", "#minecraft:needs_diamond_tool",
            "#neoforge:needs_netherite_tool", "#abyssalcraft:requires_refined_coralium_tool",
            "#abyssalcraft:requires_dreadium_tool", "#abyssalcraft:requires_ethaxium_tool");
        addAll(tags, neoBlock("minecraft", "incorrect_for_iron_tool"),
            "#minecraft:needs_diamond_tool", "#neoforge:needs_netherite_tool",
            "#abyssalcraft:requires_refined_coralium_tool", "#abyssalcraft:requires_dreadium_tool",
            "#abyssalcraft:requires_ethaxium_tool");
        addAll(tags, neoBlock("minecraft", "incorrect_for_diamond_tool"),
            "#neoforge:needs_netherite_tool", "#abyssalcraft:requires_refined_coralium_tool",
            "#abyssalcraft:requires_dreadium_tool", "#abyssalcraft:requires_ethaxium_tool");
        addAll(tags, neoBlock("minecraft", "incorrect_for_netherite_tool"),
            "#abyssalcraft:requires_refined_coralium_tool", "#abyssalcraft:requires_dreadium_tool",
            "#abyssalcraft:requires_ethaxium_tool");

        addAll(tags, neoBlock("abyssalcraft", "incorrect_for_dreadium_tool"),
            "#abyssalcraft:requires_ethaxium_tool");
        addAll(tags, neoBlock("abyssalcraft", "incorrect_for_refined_coralium_tool"),
            "#abyssalcraft:requires_dreadium_tool", "#abyssalcraft:requires_ethaxium_tool");
        addAll(tags, neoBlock("abyssalcraft", "incorrect_for_abyssalnite_tool"),
            "#abyssalcraft:requires_refined_coralium_tool", "#abyssalcraft:requires_dreadium_tool",
            "#abyssalcraft:requires_ethaxium_tool");
        tags.putIfAbsent(neoBlock("abyssalcraft", "incorrect_for_ethaxium_tool"), new ArrayList<>());

        blockAndItemTag(tags, "legacy/ore_abyssalnite", id("abyssalnite_ore"), id("abyssal_abyssalnite_ore"),
            id("dreadlands_abyssalnite_ore"));
        blockAndItemTag(tags, "legacy/ore_coralium", id("coralium_ore"), id("abyssal_coralium_ore"));
        itemTag(tags, "legacy/chest_wood", "minecraft:chest", "minecraft:trapped_chest");
        itemTag(tags, "legacy/dust_saltpeter", id("nitre"));
        itemTag(tags, "legacy/dust_sulfur", id("sulfur"));
        itemTag(tags, "legacy/ingot_iron", "minecraft:iron_ingot");
        itemTag(tags, "legacy/stick_wood", "minecraft:stick");
        itemTag(tags, "legacy/dye_cyan", "minecraft:cyan_dye");
        itemTag(tags, "legacy/dye_yellow", "minecraft:yellow_dye");
        itemTag(tags, "legacy/dye_gray", "minecraft:gray_dye");
        itemTag(tags, "legacy/dye_purple", "minecraft:purple_dye");
        itemTag(tags, "legacy/dye_blue", "minecraft:blue_dye");
        itemTag(tags, "legacy/dye_orange", "minecraft:orange_dye");
        itemTag(tags, "legacy/dye_black", "minecraft:black_dye");

        itemTag(tags, "ingots/abyssalnite", id("abyssalnite_ingot"));
        itemTag(tags, "ingots/refined_coralium", id("refined_coralium_ingot"));
        itemTag(tags, "ingots/dreadium", id("dreadium_ingot"));
        itemTag(tags, "ingots/ethaxium", id("ethaxium_ingot"));
        itemTag(tags, "nuggets/abyssalnite", id("abyssalnite_nugget"));
        itemTag(tags, "nuggets/refined_coralium", id("refined_coralium_nugget"));
        itemTag(tags, "nuggets/dreadium", id("dreadium_nugget"));
        itemTag(tags, "nuggets/ethaxium", id("ethaxium_nugget"));
        itemTag(tags, "gems/coralium", id("coralium_gem"));
        itemTag(tags, "storage_blocks/abyssalnite", id("block_of_abyssalnite"));
        itemTag(tags, "storage_blocks/refined_coralium", id("block_of_refined_coralium"));
        itemTag(tags, "storage_blocks/dreadium", id("block_of_dreadium"));
        itemTag(tags, "storage_blocks/ethaxium", id("block_of_ethaxium"));
        List<String> basicMaterials = MaterialItems.BASICS.stream()
            .map(Supplier::get).map(BuiltInRegistries.ITEM::getKey).map(Object::toString).toList();
        addAll(tags, item("abyssalcraft", "materials"), basicMaterials.toArray(String[]::new));
        for (String element : MaterialItems.CRYSTAL_ELEMENTS) {
            String crystal = id("crystal_" + element);
            String shard = id("crystal_shard_" + element);
            String fragment = id("crystal_fragment_" + element);
            add(tags, item("abyssalcraft", "crystals"), crystal);
            add(tags, item("abyssalcraft", "crystal_shards"), shard);
            add(tags, item("abyssalcraft", "crystal_fragments"), fragment);
            itemTag(tags, "crystals/" + element, crystal);
            itemTag(tags, "crystal_shards/" + element, shard);
            itemTag(tags, "crystal_fragments/" + element, fragment);
        }
        for (String element : MaterialItems.MACHINE_COMPAT_ELEMENTS) {
            String crystal = id("crystal_" + element);
            String shard = id("crystal_shard_" + element);
            add(tags, item("abyssalcraft", "crystals"), crystal);
            add(tags, item("abyssalcraft", "crystal_shards"), shard);
            itemTag(tags, "crystals/" + element, crystal);
            itemTag(tags, "crystal_shards/" + element, shard);
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<TagPath, List<String>> entry : tags.entrySet()) {
            validateValues(entry.getKey(), entry.getValue());
            JsonObject json = tagJson(entry.getValue());
            for (Path target : entry.getKey().targets(packOutput)) {
                futures.add(DataProvider.saveStable(output, json, target));
            }
        }
        require(tags.get(item("abyssalcraft", "legacy/ore_abyssalnite")).size() == 3,
            "abyssalnite ore tag changed");
        require(tags.get(item("abyssalcraft", "legacy/ore_coralium")).size() == 2,
            "coralium ore tag changed");
        long expectedMineable = m1Blocks.stream().filter(blockId -> {
            Block value = BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.tryParse(blockId));
            return !(value instanceof DecoPlantBlock) && !(value instanceof SaplingBlock);
        }).count();
        require(mineable.size() == expectedMineable,
            "M1 mineable coverage mismatch: " + mineable.size() + "/" + expectedMineable);
        int physical = tags.keySet().stream().mapToInt(tag -> tag.targets(packOutput).size()).sum();
        System.out.printf("RR_DATA_TAGS_OK logical=%d physical=%d%n", tags.size(), physical);
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "AbyssalCraft M1 Tags";
    }

    private static TagPath block(String namespace, String path) {
        return new TagPath(namespace, path, Kind.BLOCK);
    }

    private static TagPath item(String namespace, String path) {
        return new TagPath(namespace, path, Kind.ITEM);
    }

    private static TagPath forgeBlock(String namespace, String path) {
        return new TagPath(namespace, path, Kind.BLOCK, Layout.FORGE);
    }

    private static TagPath neoBlock(String namespace, String path) {
        return new TagPath(namespace, path, Kind.BLOCK, Layout.NEO);
    }

    private static void blockAndItemTag(Map<TagPath, List<String>> tags, String path, String... values) {
        addAll(tags, block("abyssalcraft", path), values);
        addAll(tags, item("abyssalcraft", path), values);
    }

    private static void itemTag(Map<TagPath, List<String>> tags, String path, String... values) {
        addAll(tags, item("abyssalcraft", path), values);
    }

    private static void classifyBase(Map<TagPath, List<String>> tags, Block value, String blockId,
                                     Set<String> mineable) {
        String path = blockId.substring("abyssalcraft:".length());
        boolean wood = path.startsWith("darklands_oak_") || path.startsWith("dreadwood_")
            || path.equals("dead_tree_log");
        if (value instanceof SaplingBlock) {
            return;
        }
        if (value instanceof LeavesBlock) {
            add(tags, block("minecraft", "mineable/hoe"), blockId);
        } else if (wood) {
            add(tags, block("minecraft", "mineable/axe"), blockId);
        } else {
            add(tags, block("minecraft", "mineable/pickaxe"), blockId);
        }
        mineable.add(blockId);

        if (value instanceof SlabBlock) blockAndItem(tags, "minecraft", "slabs", blockId);
        if (value instanceof StairBlock) blockAndItem(tags, "minecraft", "stairs", blockId);
        if (value instanceof WallBlock) blockAndItem(tags, "minecraft", "walls", blockId);
        if (value instanceof FenceBlock) {
            blockAndItem(tags, "minecraft", "fences", blockId);
            if (wood) blockAndItem(tags, "minecraft", "wooden_fences", blockId);
        }
        if (value instanceof ButtonBlock) {
            blockAndItem(tags, "minecraft", "buttons", blockId);
            blockAndItem(tags, "minecraft", wood ? "wooden_buttons" : "stone_buttons", blockId);
        }
        if (value instanceof PressurePlateBlock) {
            blockAndItem(tags, "minecraft", "pressure_plates", blockId);
            blockAndItem(tags, "minecraft", wood ? "wooden_pressure_plates" : "stone_pressure_plates", blockId);
        }
        if (value instanceof DoorBlock) {
            blockAndItem(tags, "minecraft", "doors", blockId);
            if (wood) blockAndItem(tags, "minecraft", "wooden_doors", blockId);
        }
        if (value instanceof FenceGateBlock) blockAndItem(tags, "minecraft", "fence_gates", blockId);
        if (value instanceof RotatedPillarBlock && path.endsWith("_log")) {
            blockAndItem(tags, "minecraft", "logs", blockId);
            blockAndItem(tags, "minecraft", "logs_that_burn", blockId);
        }
    }

    private static void blockAndItem(Map<TagPath, List<String>> tags, String namespace,
                                     String path, String... values) {
        addAll(tags, block(namespace, path), values);
        addAll(tags, item(namespace, path), values);
    }

    private static String blockId(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static void validateValues(TagPath tag, List<String> values) {
        for (String value : values) {
            if (value.startsWith("#")) continue;
            net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(value);
            require(id != null, "invalid value in tag " + tag + ": " + value);
            boolean present = tag.kind() == Kind.BLOCK
                ? BuiltInRegistries.BLOCK.getOptional(id).isPresent()
                : BuiltInRegistries.ITEM.getOptional(id).isPresent();
            require(present, "unregistered value in tag " + tag + ": " + value);
        }
    }

    private static void addAll(Map<TagPath, List<String>> tags, TagPath key, String... values) {
        tags.computeIfAbsent(key, ignored -> new ArrayList<>()).addAll(Arrays.asList(values));
    }

    private static void add(Map<TagPath, List<String>> tags, TagPath key, String value) {
        tags.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
    }

    private static JsonObject tagJson(List<String> values) {
        JsonArray array = new JsonArray();
        values.stream().distinct().sorted().forEach(array::add);
        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        json.add("values", array);
        return json;
    }

    private static String id(String path) {
        return "abyssalcraft:" + path;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private enum Kind { BLOCK, ITEM }
    private enum Layout { BOTH, FORGE, NEO }

    private record TagPath(String namespace, String path, Kind kind, Layout layout) {
        private TagPath(String namespace, String path, Kind kind) {
            this(namespace, path, kind, Layout.BOTH);
        }

        private List<Path> targets(PackOutput output) {
            Path data = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(namespace).resolve("tags");
            String forgeDir = kind == Kind.BLOCK ? "blocks" : "items";
            String neoDir = kind == Kind.BLOCK ? "block" : "item";
            Path forge = data.resolve(forgeDir).resolve(path + ".json");
            Path neo = data.resolve(neoDir).resolve(path + ".json");
            return switch (layout) {
                case BOTH -> List.of(forge, neo);
                case FORGE -> List.of(forge);
                case NEO -> List.of(neo);
            };
        }
    }
}