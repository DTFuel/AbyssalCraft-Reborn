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
        horizontalSharedModel(DecoBlocks.MURAL.get(), "deco_mural");

        cube(DecoBlocks.BLOCK_OF_ABYSSALNITE.get(), "block_of_abyssalnite");
        cube(DecoBlocks.BLOCK_OF_REFINED_CORALIUM.get(), "block_of_refined_coralium");
        cube(DecoBlocks.BLOCK_OF_DREADIUM.get(), "block_of_dreadium");
        cube(DecoBlocks.BLOCK_OF_ETHAXIUM.get(), "block_of_ethaxium");
        cube(DecoBlocks.DREADLANDS_DIRT.get(), "dreadlands_dirt");
        cube(DecoBlocks.DREADLANDS_MUCK.get(), "dreadlands_muck");
        cube(DecoBlocks.ABYSSAL_SAND.get(), "abyssal_sand");
        translucentCube(DecoBlocks.ABYSSAL_SAND_GLASS.get(), "abyssal_sand_glass");
        cross(DecoBlocks.LUMINOUS_THISTLE.get(), "luminous_thistle");
        cross(DecoBlocks.WASTELANDS_THORN.get(), "wastelands_thorn");
    }
}