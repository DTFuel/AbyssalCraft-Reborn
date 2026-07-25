package com.shinoow.abyssalcraft.world.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import com.shinoow.abyssalcraft.registry.BaseBlocks;

/** Vertical Coralium chain with Abyssal-stone anchors at both ends. */
public final class ChainsFeature extends Feature<NoneFeatureConfiguration> {

    public ChainsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int top = level.getMaxBuildHeight() - 1;
        int links = 5 + random.nextInt(35);
        int bottomY = top - links * 6;
        while (bottomY < top - 1 && !level.isEmptyBlock(new BlockPos(origin.getX(), bottomY + 1, origin.getZ()))) {
            bottomY++;
            links--;
        }
        if (links <= 0) return false;

        BlockPos bottom = new BlockPos(origin.getX(), bottomY, origin.getZ());
        boolean bottomAnchor = !level.isEmptyBlock(bottom) || !level.isEmptyBlock(bottom.below());
        placeAnchor(level, bottom, bottomAnchor);
        placeAnchor(level, new BlockPos(origin.getX(), top, origin.getZ()), true);

        for (int link = 0; link < links; link++) {
            int y = bottomY + link * 6;
            set(level, origin.getX(), y, origin.getZ());
            set(level, origin.getX(), y + 1, origin.getZ() + 1);
            set(level, origin.getX(), y + 1, origin.getZ() - 1);
            set(level, origin.getX(), y + 2, origin.getZ() + 1);
            set(level, origin.getX(), y + 2, origin.getZ() - 1);
            set(level, origin.getX(), y + 3, origin.getZ());
            set(level, origin.getX() + 1, y + 4, origin.getZ());
            set(level, origin.getX() - 1, y + 4, origin.getZ());
            set(level, origin.getX() + 1, y + 5, origin.getZ());
            set(level, origin.getX() - 1, y + 5, origin.getZ());
        }
        set(level, origin.getX(), bottomY + links * 6, origin.getZ());
        return true;
    }

    private static void placeAnchor(WorldGenLevel level, BlockPos center, boolean enabled) {
        if (!enabled) return;
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (Math.abs(x) <= 1 || Math.abs(z) <= 1) {
                    level.setBlock(center.offset(x, 0, z), BaseBlocks.ABYSSAL_STONE.get().defaultBlockState(), 2);
                }
            }
        }
    }

    private static void set(WorldGenLevel level, int x, int y, int z) {
        if (y >= level.getMinBuildHeight() && y < level.getMaxBuildHeight()) {
            level.setBlock(new BlockPos(x, y, z), BaseBlocks.CORALIUM_COBBLESTONE.get().defaultBlockState(), 2);
        }
    }
}