package com.shinoow.abyssalcraft.world.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;

/** Legacy Luminous Thistle and Wasteland's Thorn decorator passes. */
public final class AbyssalWastelandPlantsFeature extends Feature<NoneFeatureConfiguration> {

    public AbyssalWastelandPlantsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int placed = 0;
        for (int attempt = 0; attempt < 5; attempt++) {
            if (random.nextInt(4) == 0) {
                placed += placePatch(level, random, origin, DecoBlocks.LUMINOUS_THISTLE.get().defaultBlockState());
            }
            if (random.nextInt(8) == 0) {
                placed += placePatch(level, random, origin, DecoBlocks.WASTELANDS_THORN.get().defaultBlockState());
            }
        }
        return placed > 0;
    }

    private static int placePatch(WorldGenLevel level, RandomSource random, BlockPos origin, BlockState plant) {
        int x = origin.getX() + random.nextInt(16) + 8;
        int z = origin.getZ() + random.nextInt(16) + 8;
        int height = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) * 2;
        if (height <= 0) return 0;

        BlockPos center = new BlockPos(x, random.nextInt(height), z);
        int placed = 0;
        for (int attempt = 0; attempt < 64; attempt++) {
            BlockPos pos = center.offset(
                random.nextInt(8) - random.nextInt(8),
                random.nextInt(4) - random.nextInt(4),
                random.nextInt(8) - random.nextInt(8));
            if (level.isEmptyBlock(pos) && plant.canSurvive(level, pos)) {
                level.setBlock(pos, plant, 2);
                placed++;
            }
        }
        return placed;
    }
}