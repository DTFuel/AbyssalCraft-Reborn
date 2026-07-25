package com.shinoow.abyssalcraft.data.gen;

import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.platform.BlockModelGen;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

public final class DecoBlockData extends BlockModelGen {

    public DecoBlockData(DataGenCompat.Gen gen) {
        super(gen);
    }

    @Override
    public String getName() {
        return "AbyssalCraft Decorative Block States";
    }

    @Override
    protected void generate() {
        horizontalSharedModel(DecoBlocks.DECORATIVE_CTHULHU_STATUE.get(), "deco_statue");
        horizontalSharedModel(DecoBlocks.DECORATIVE_HASTUR_STATUE.get(), "deco_statue");
        horizontalSharedModel(DecoBlocks.DECORATIVE_JZAHAR_STATUE.get(), "deco_statue");
        horizontalSharedModel(DecoBlocks.DECORATIVE_AZATHOTH_STATUE.get(), "deco_statue");
        horizontalSharedModel(DecoBlocks.DECORATIVE_NYARLATHOTEP_STATUE.get(), "deco_statue");
        horizontalSharedModel(DecoBlocks.DECORATIVE_YOG_SOTHOTH_STATUE.get(), "deco_statue");
        horizontalSharedModel(DecoBlocks.DECORATIVE_SHUB_NIGGURATH_STATUE.get(), "deco_statue");
        horizontalSharedModel(DecoBlocks.MURAL.get(), "deco_mural");

        horizontalParent(DecoBlocks.TOMBSTONE_STONE.get(), "deco_tombstone", "darkstone");
        horizontalParent(DecoBlocks.TOMBSTONE_ABYSSAL_STONE.get(), "deco_tombstone", "abyssal_stone");
        horizontalParent(DecoBlocks.TOMBSTONE_CORALIUM_STONE.get(), "deco_tombstone", "coralium_stone");
        horizontalParent(DecoBlocks.TOMBSTONE_DARKSTONE.get(), "deco_tombstone", "darkstone");
        horizontalParent(DecoBlocks.TOMBSTONE_DREADSTONE.get(), "deco_tombstone", "dreadstone");
        horizontalParent(DecoBlocks.TOMBSTONE_ELYSIAN_STONE.get(), "deco_tombstone", "elysian_stone");
        horizontalParent(DecoBlocks.TOMBSTONE_ETHAXIUM.get(), "deco_tombstone", "ethaxium");
        horizontalParent(DecoBlocks.TOMBSTONE_MONOLITH_STONE.get(), "deco_tombstone", "monolith_stone");
        horizontalParent(DecoBlocks.TOMBSTONE_OMOTHOL_STONE.get(), "deco_tombstone", "omothol_stone");

        cube(DecoBlocks.BLOCK_OF_ABYSSALNITE.get(), "block_of_abyssalnite");
        cube(DecoBlocks.BLOCK_OF_REFINED_CORALIUM.get(), "block_of_refined_coralium");
        cube(DecoBlocks.BLOCK_OF_DREADIUM.get(), "block_of_dreadium");
        cube(DecoBlocks.BLOCK_OF_ETHAXIUM.get(), "block_of_ethaxium");
        cube(DecoBlocks.DREADLANDS_DIRT.get(), "dreadlands_dirt");
        grass(DecoBlocks.DREADLANDS_GRASS.get(), "dreadlands_grass_side", "dreadlands_dirt");
        cube(DecoBlocks.DREADLANDS_MUCK.get(), "dreadlands_muck");
        cube(DecoBlocks.ABYSSAL_SAND.get(), "abyssal_sand");
        layeredGround(DecoBlocks.FUSED_ABYSSAL_SAND.get(), "fused_abyssal_sand_side",
            "abyssal_sand", "fused_abyssal_sand_top");
        translucentCube(DecoBlocks.ABYSSAL_SAND_GLASS.get(), "abyssal_sand_glass");
        cross(DecoBlocks.LUMINOUS_THISTLE.get(), "luminous_thistle");
        cross(DecoBlocks.WASTELANDS_THORN.get(), "wastelands_thorn");
    }
}