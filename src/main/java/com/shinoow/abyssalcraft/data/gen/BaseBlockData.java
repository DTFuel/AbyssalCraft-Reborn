package com.shinoow.abyssalcraft.data.gen;

import com.shinoow.abyssalcraft.platform.BlockModelGen;
import com.shinoow.abyssalcraft.platform.DataGenCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;

/**
 * Building-material block datagen (owned by PB-3).
 *
 * <p>Emits blockstate + block model + item model for every {@link BaseBlocks} entry through the neutral
 * {@link BlockModelGen} facade (fork-free). Textures resolve to {@code abyssalcraft:block/<name>}
 * (shipped under {@code textures/block}). Slabs / stairs / walls / fences reference the source cube
 * texture of the material they are built from (e.g. {@code *_brick_stairs} uses the {@code *_brick}
 * texture), matching the legacy 1.12.2 models. Loot / tags / recipes are separate providers.
 */
public final class BaseBlockData extends BlockModelGen {

    public BaseBlockData(DataGenCompat.Gen gen) {
        super(gen);
    }

    @Override
    protected void generate() {
        // ---- Darkstone ----
        cube(BaseBlocks.DARKSTONE.get(), "darkstone");
        cube(BaseBlocks.DARKSTONE_COBBLESTONE.get(), "darkstone_cobblestone");
        cube(BaseBlocks.DARKSTONE_BRICK.get(), "darkstone_brick");
        cube(BaseBlocks.CHISELED_DARKSTONE_BRICK.get(), "chiseled_darkstone_brick");
        cube(BaseBlocks.CRACKED_DARKSTONE_BRICK.get(), "cracked_darkstone_brick");
        column(BaseBlocks.GLOWING_DARKSTONE_BRICKS.get(), "glowing_darkstone_bricks", "darkstone_brick");
        slab(BaseBlocks.DARKSTONE_SLAB.get(), "darkstone_slab");
        slab(BaseBlocks.DARKSTONE_BRICK_SLAB.get(), "darkstone_brick");
        slab(BaseBlocks.DARKSTONE_COBBLESTONE_SLAB.get(), "darkstone_cobblestone");
        stairs(BaseBlocks.DARKSTONE_BRICK_STAIRS.get(), "darkstone_brick");
        stairs(BaseBlocks.DARKSTONE_COBBLESTONE_STAIRS.get(), "darkstone_cobblestone");
        wall(BaseBlocks.DARKSTONE_COBBLESTONE_WALL.get(), "darkstone_cobblestone");
        fence(BaseBlocks.DARKSTONE_BRICK_FENCE.get(), "darkstone_brick");

        // ---- Abyssal stone ----
        cube(BaseBlocks.ABYSSAL_STONE.get(), "abyssal_stone");
        cube(BaseBlocks.ABYSSAL_COBBLESTONE.get(), "abyssal_cobblestone");
        cube(BaseBlocks.ABYSSAL_STONE_BRICK.get(), "abyssal_stone_brick");
        cube(BaseBlocks.CHISELED_ABYSSAL_STONE_BRICK.get(), "chiseled_abyssal_stone_brick");
        cube(BaseBlocks.CRACKED_ABYSSAL_STONE_BRICK.get(), "cracked_abyssal_stone_brick");
        slab(BaseBlocks.ABYSSAL_STONE_BRICK_SLAB.get(), "abyssal_stone_brick");
        slab(BaseBlocks.ABYSSAL_COBBLESTONE_SLAB.get(), "abyssal_cobblestone");
        stairs(BaseBlocks.ABYSSAL_STONE_BRICK_STAIRS.get(), "abyssal_stone_brick");
        stairs(BaseBlocks.ABYSSAL_COBBLESTONE_STAIRS.get(), "abyssal_cobblestone");
        wall(BaseBlocks.ABYSSAL_COBBLESTONE_WALL.get(), "abyssal_cobblestone");
        fence(BaseBlocks.ABYSSAL_STONE_BRICK_FENCE.get(), "abyssal_stone_brick_fence");

        // ---- Dreadstone ----
        cube(BaseBlocks.DREADSTONE.get(), "dreadstone");
        cube(BaseBlocks.DREADSTONE_COBBLESTONE.get(), "dreadstone_cobblestone");
        cube(BaseBlocks.DREADSTONE_BRICK.get(), "dreadstone_brick");
        cube(BaseBlocks.CHISELED_DREADSTONE_BRICK.get(), "chiseled_dreadstone_brick");
        cube(BaseBlocks.CRACKED_DREADSTONE_BRICK.get(), "cracked_dreadstone_brick");
        slab(BaseBlocks.DREADSTONE_BRICK_SLAB.get(), "dreadstone_brick");
        slab(BaseBlocks.DREADSTONE_COBBLESTONE_SLAB.get(), "dreadstone_cobblestone");
        stairs(BaseBlocks.DREADSTONE_BRICK_STAIRS.get(), "dreadstone_brick");
        stairs(BaseBlocks.DREADSTONE_COBBLESTONE_STAIRS.get(), "dreadstone_cobblestone");
        wall(BaseBlocks.DREADSTONE_COBBLESTONE_WALL.get(), "dreadstone_cobblestone");
        fence(BaseBlocks.DREADSTONE_BRICK_FENCE.get(), "dreadstone_brick");

        // ---- Elysian stone ----
        cube(BaseBlocks.ELYSIAN_STONE.get(), "elysian_stone");
        cube(BaseBlocks.ELYSIAN_COBBLESTONE.get(), "elysian_cobblestone");
        cube(BaseBlocks.ELYSIAN_STONE_BRICK.get(), "elysian_stone_brick");
        cube(BaseBlocks.CHISELED_ELYSIAN_STONE_BRICK.get(), "chiseled_elysian_stone_brick");
        cube(BaseBlocks.CRACKED_ELYSIAN_STONE_BRICK.get(), "cracked_elysian_stone_brick");
        slab(BaseBlocks.ELYSIAN_STONE_BRICK_SLAB.get(), "elysian_stone_brick");
        slab(BaseBlocks.ELYSIAN_COBBLESTONE_SLAB.get(), "elysian_cobblestone");
        stairs(BaseBlocks.ELYSIAN_STONE_BRICK_STAIRS.get(), "elysian_stone_brick");
        stairs(BaseBlocks.ELYSIAN_COBBLESTONE_STAIRS.get(), "elysian_cobblestone");
        wall(BaseBlocks.ELYSIAN_COBBLESTONE_WALL.get(), "elysian_cobblestone");
        fence(BaseBlocks.ELYSIAN_STONE_BRICK_FENCE.get(), "elysian_stone_brick");

        // ---- Coralium stone ----
        cube(BaseBlocks.CORALIUM_STONE.get(), "coralium_stone");
        cube(BaseBlocks.CORALIUM_COBBLESTONE.get(), "coralium_cobblestone");
        cube(BaseBlocks.CORALIUM_STONE_BRICK.get(), "coralium_stone_brick");
        cube(BaseBlocks.CHISELED_CORALIUM_STONE_BRICK.get(), "chiseled_coralium_stone_brick");
        cube(BaseBlocks.CRACKED_CORALIUM_STONE_BRICK.get(), "cracked_coralium_stone_brick");
        slab(BaseBlocks.CORALIUM_STONE_BRICK_SLAB.get(), "coralium_stone_brick");
        slab(BaseBlocks.CORALIUM_COBBLESTONE_SLAB.get(), "coralium_cobblestone");
        stairs(BaseBlocks.CORALIUM_STONE_BRICK_STAIRS.get(), "coralium_stone_brick");
        stairs(BaseBlocks.CORALIUM_COBBLESTONE_STAIRS.get(), "coralium_cobblestone");
        wall(BaseBlocks.CORALIUM_COBBLESTONE_WALL.get(), "coralium_cobblestone");
        fence(BaseBlocks.CORALIUM_STONE_BRICK_FENCE.get(), "coralium_stone_brick");

        // ---- Ethaxium ----
        cube(BaseBlocks.ETHAXIUM.get(), "ethaxium");
        cube(BaseBlocks.ETHAXIUM_BRICKS.get(), "ethaxium_brick");
        cube(BaseBlocks.CHISELED_ETHAXIUM_BRICK.get(), "chiseled_ethaxium_brick");
        cube(BaseBlocks.CRACKED_ETHAXIUM_BRICK.get(), "cracked_ethaxium_brick");
        pillar(BaseBlocks.ETHAXIUM_PILLAR.get(), "ethaxium_pillar", "ethaxium_pillar_top");
        slab(BaseBlocks.ETHAXIUM_BRICK_SLAB.get(), "ethaxium_brick");
        stairs(BaseBlocks.ETHAXIUM_BRICK_STAIRS.get(), "ethaxium_brick");
        fence(BaseBlocks.ETHAXIUM_BRICK_FENCE.get(), "ethaxium_brick");

        // ---- Dark ethaxium ----
        cube(BaseBlocks.DARK_ETHAXIUM_BRICK.get(), "dark_ethaxium_brick");
        cube(BaseBlocks.CHISELED_DARK_ETHAXIUM_BRICK.get(), "chiseled_dark_ethaxium_brick");
        cube(BaseBlocks.CRACKED_DARK_ETHAXIUM_BRICK.get(), "cracked_dark_ethaxium_brick");
        pillar(BaseBlocks.DARK_ETHAXIUM_PILLAR.get(), "dark_ethaxium_pillar", "dark_ethaxium_pillar_top");
        slab(BaseBlocks.DARK_ETHAXIUM_BRICK_SLAB.get(), "dark_ethaxium_brick");
        stairs(BaseBlocks.DARK_ETHAXIUM_BRICK_STAIRS.get(), "dark_ethaxium_brick");
        fence(BaseBlocks.DARK_ETHAXIUM_BRICK_FENCE.get(), "dark_ethaxium_brick");

        // ---- Omothol / Monolith stone ----
        cube(BaseBlocks.OMOTHOL_STONE.get(), "omothol_stone");
        cube(BaseBlocks.MONOLITH_STONE.get(), "monolith_stone");

        // ---- Darklands oak (wood) ----
        leaves(BaseBlocks.DARKLANDS_OAK_LEAVES.get(), "darklands_oak_leaves");
        pillar(BaseBlocks.DARKLANDS_OAK_LOG.get(), "darklands_oak_log", "darklands_oak_log_top");
        cube(BaseBlocks.DARKLANDS_OAK_PLANKS.get(), "darklands_oak_planks");
        slab(BaseBlocks.DARKLANDS_OAK_SLAB.get(), "darklands_oak_planks");
        stairs(BaseBlocks.DARKLANDS_OAK_STAIRS.get(), "darklands_oak_planks");
        fence(BaseBlocks.DARKLANDS_OAK_FENCE.get(), "darklands_oak_planks");

        // ---- Dreadwood ----
        leaves(BaseBlocks.DREADWOOD_LEAVES.get(), "dreadwood_leaves");
        pillar(BaseBlocks.DREADWOOD_LOG.get(), "dreadwood_log", "dreadwood_log_top");
        cube(BaseBlocks.DREADWOOD_PLANKS.get(), "dreadwood_planks");
        slab(BaseBlocks.DREADWOOD_SLAB.get(), "dreadwood_planks");
        stairs(BaseBlocks.DREADWOOD_STAIRS.get(), "dreadwood_planks");
        fence(BaseBlocks.DREADWOOD_FENCE.get(), "dreadwood_planks");

        pillar(BaseBlocks.DEAD_TREE_LOG.get(), "dead_tree", "dead_tree_top");

        // ---- PB-8 forked-construction variants ----
        // buttons/plates reuse the family base texture; gates reuse the planks; saplings use their own
        // (placeholder) cross texture; doors use their bottom/top block + flat item textures.
        button(BaseBlocks.DARKSTONE_BUTTON.get(), "darkstone");
        button(BaseBlocks.ABYSSAL_STONE_BUTTON.get(), "abyssal_stone");
        button(BaseBlocks.CORALIUM_STONE_BUTTON.get(), "coralium_stone");
        button(BaseBlocks.DARKLANDS_OAK_BUTTON.get(), "darklands_oak_planks");
        button(BaseBlocks.DREADWOOD_BUTTON.get(), "dreadwood_planks");

        pressurePlate(BaseBlocks.DARKSTONE_PRESSURE_PLATE.get(), "darkstone");
        pressurePlate(BaseBlocks.ABYSSAL_STONE_PRESSURE_PLATE.get(), "abyssal_stone");
        pressurePlate(BaseBlocks.CORALIUM_STONE_PRESSURE_PLATE.get(), "coralium_stone");
        pressurePlate(BaseBlocks.DARKLANDS_OAK_PRESSURE_PLATE.get(), "darklands_oak_planks");
        pressurePlate(BaseBlocks.DREADWOOD_PRESSURE_PLATE.get(), "dreadwood_planks");

        cross(BaseBlocks.DARKLANDS_OAK_SAPLING.get(), "darklands_oak_sapling");
        cross(BaseBlocks.DREADWOOD_SAPLING.get(), "dreadwood_sapling");

        door(BaseBlocks.DARKLANDS_OAK_DOOR.get(), "darklands_oak_door_bottom", "darklands_oak_door_top");
        door(BaseBlocks.DREADWOOD_DOOR.get(), "dreadwood_door_bottom", "dreadwood_door_top");

        fenceGate(BaseBlocks.DARKLANDS_OAK_FENCE_GATE.get(), "darklands_oak_planks");
        fenceGate(BaseBlocks.DREADWOOD_FENCE_GATE.get(), "dreadwood_planks");

        for (var cluster : CrystalClusterBlocks.CLUSTERS) {
            sharedModel(cluster.get(), "crystal_cluster");
        }
        for (var cluster : CrystalClusterBlocks.MACHINE_COMPAT_CLUSTERS) {
            sharedModel(cluster.get(), "crystal_cluster");
        }
    }
}
