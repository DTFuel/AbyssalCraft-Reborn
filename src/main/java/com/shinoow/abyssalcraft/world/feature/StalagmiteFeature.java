package com.shinoow.abyssalcraft.world.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.registry.BaseBlocks;

/**
 * Tapering stone spire (owned by PG-4 / Stage G1) -- the modern port of the 1.12.2
 * {@code WorldGenAbyssalStalagmite}/{@code WorldGenDreadlandsStalagmite}. One block-state-configured
 * feature serves every stalagmite variant (Abyssal Wasteland {@code abyssal_stone}, Dreadlands
 * {@code dreadstone}, Abyssal Plateau {@code coralium_stone}) -- the block comes from the
 * {@code configured_feature} JSON, so no per-variant code.
 *
 * <p>The {@code place}/{@code setBlock}/{@code FeaturePlaceContext} surface and
 * {@code BlockStateConfiguration} are identical on both loaders and MC versions -- no {@code //?} fork.
 */
public class StalagmiteFeature extends Feature<BlockStateConfiguration> {

    public StalagmiteFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        if (context.config().state.is(BaseBlocks.DREADSTONE.get())
            && !ACConfig.generateDreadlandsStalagmite.get()) {
            return false;
        }
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        BlockState state = context.config().state;
        int height = 5 + random.nextInt(8);
        for (int y = 0; y < height; y++) {
            int radius = (int) Math.round((1.0 - (double) y / height) * 2.0);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius + radius) {
                        level.setBlock(origin.offset(dx, y, dz), state, 2);
                    }
                }
            }
        }
        return true;
    }
}
