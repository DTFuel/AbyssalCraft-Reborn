package com.shinoow.abyssalcraft.content.block.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class DreadlandsGroundBlock extends Block {

    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;
    private final boolean grass;

    public DreadlandsGroundBlock(Properties properties, boolean grass) {
        super(properties);
        this.grass = grass;
        if (grass) registerDefaultState(stateDefinition.any().setValue(SNOWY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SNOWY);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(SNOWY,
            isSnowy(context.getLevel().getBlockState(context.getClickedPos().above())));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.UP ? state.setValue(SNOWY, isSnowy(neighbor))
            : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
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
                    level.setBlockAndUpdate(target, defaultBlockState().setValue(
                        SNOWY, isSnowy(level.getBlockState(targetAbove))));
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

    private static boolean isSnowy(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK);
    }
}