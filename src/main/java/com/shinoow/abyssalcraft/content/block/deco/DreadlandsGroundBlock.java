package com.shinoow.abyssalcraft.content.block.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class DreadlandsGroundBlock extends Block {

    private final boolean grass;

    public DreadlandsGroundBlock(Properties properties, boolean grass) {
        super(properties);
        this.grass = grass;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (grass) {
            BlockPos abovePos = pos.above();
            BlockState above = level.getBlockState(abovePos);
            if (shouldDecay(level.getMaxLocalRawBrightness(abovePos), above.getLightBlock(level, abovePos))) {
                level.setBlockAndUpdate(pos, DecoBlocks.DREADLANDS_DIRT.get().defaultBlockState());
                return;
            }
            for (int attempt = 0; attempt < 4; attempt++) {
                BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                BlockPos targetAbove = target.above();
                if (canSpreadTo(level.getBlockState(target),
                        level.getBlockState(targetAbove).getLightBlock(level, targetAbove))) {
                    level.setBlockAndUpdate(target, defaultBlockState());
                }
            }
        }
    }

    static boolean shouldDecay(int brightness, int lightBlock) {
        return brightness < 4 && lightBlock > 2;
    }

    static boolean canSpreadTo(BlockState target, int lightBlock) {
        return target.is(DecoBlocks.DREADLANDS_DIRT.get()) && lightBlock <= 2;
    }
}