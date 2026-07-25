package com.shinoow.abyssalcraft.world.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/**
 * Tall monolith slab (owned by PG-4 / Stage G1) -- the modern port of the 1.12.2
 * {@code WorldGenShoggothMonolith} pillar (built by shoggoths / disruptions / the shoggoth pit). A
 * 3-wide, 1-deep rectangular column of {@code monolith_stone} (block supplied by the
 * {@code configured_feature} JSON). Fork-free.
 */
public class MonolithFeature extends Feature<BlockStateConfiguration> {

    public MonolithFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        return placeMonolith(context.level(), context.origin(), context.random(), context.config().state);
    }

    public static boolean placeMonolith(WorldGenLevel level, BlockPos origin, RandomSource random, BlockState state) {
        int height = 7 + random.nextInt(6);
        for (int y = 0; y < height; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                level.setBlock(origin.offset(dx, y, 0), state, 2);
            }
        }
        return true;
    }
}
