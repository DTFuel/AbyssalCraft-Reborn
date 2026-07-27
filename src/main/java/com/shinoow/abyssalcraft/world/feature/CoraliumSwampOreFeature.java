package com.shinoow.abyssalcraft.world.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.content.block.ore.OreBlocks;

/** The two Coralium ore passes from the legacy Coralium Infested Swamp decorator. */
public final class CoraliumSwampOreFeature extends Feature<NoneFeatureConfiguration> {

    public CoraliumSwampOreFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!ACConfig.generateCoraliumOre.get()) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int[] settings = ComplexConfig.coraliumOre();
        int changed = replaceExposedOres(level, random, origin, settings[0], settings[2]);
        changed += generateStoneVeins(level, random, origin, settings[0], settings[1], settings[2]);
        return changed > 0;
    }

    private static int replaceExposedOres(WorldGenLevel level, RandomSource random, BlockPos origin,
                                           int attempts, int maximumY) {
        int changed = 0;
        for (int attempt = 0; attempt < attempts; attempt++) {
            BlockPos pos = new BlockPos(origin.getX() + random.nextInt(16), random.nextInt(Math.max(1, maximumY + 1)),
                origin.getZ() + random.nextInt(16));
            if (level.getBlockState(pos).is(Blocks.IRON_ORE) || level.getBlockState(pos).is(Blocks.COAL_ORE)) {
                level.setBlock(pos, OreBlocks.CORALIUM_ORE.get().defaultBlockState(), 2);
                changed++;
            }
        }
        return changed;
    }

    private static int generateStoneVeins(WorldGenLevel level, RandomSource random, BlockPos origin,
                                           int attempts, int veinSize, int maximumY) {
        int changed = 0;
        Direction[] directions = Direction.values();
        for (int vein = 0; vein < attempts; vein++) {
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                origin.getX() + random.nextInt(16), random.nextInt(Math.max(1, maximumY + 1)),
                origin.getZ() + random.nextInt(16));
            for (int block = 0; block < veinSize; block++) {
                if (level.getBlockState(cursor).is(BlockTags.STONE_ORE_REPLACEABLES)) {
                    level.setBlock(cursor, OreBlocks.CORALIUM_ORE.get().defaultBlockState(), 2);
                    changed++;
                }
                cursor.move(directions[random.nextInt(directions.length)]);
            }
        }
        return changed;
    }
}