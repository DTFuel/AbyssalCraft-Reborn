package com.shinoow.abyssalcraft.content.block.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.platform.LiquidAntimatterCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.world.ACDimensions;

/** Liquid Coralium's legacy adjacent-liquid conversion and stone transmutation. */
public final class LiquidCoraliumBlock extends LiquidBlock {

    public LiquidCoraliumBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (!level.isClientSide && level.getBiome(pos).is(BiomeTags.IS_OCEAN)
            && !ACConfig.destroyOcean.get()) {
            level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
            return;
        }
        if (!level.isClientSide) transmuteNeighbors(level, pos);
        super.onPlace(state, level, pos, oldState, moved);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof Level actualLevel && !actualLevel.isClientSide
            && (direction != Direction.UP || ACConfig.breakLogic.get())) {
            transmute(actualLevel, neighborPos);
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    private static void transmuteNeighbors(Level level, BlockPos pos) {
        if (!ACConfig.shouldSpread.get() && level.dimension() != ACDimensions.ABYSSAL_WASTELAND) return;
        for (Direction direction : Direction.Plane.HORIZONTAL) transmute(level, pos.relative(direction));
        transmute(level, pos.below());
        if (ACConfig.breakLogic.get()) transmute(level, pos.above());
    }

    private static void transmute(Level level, BlockPos pos) {
        if (!ACConfig.shouldSpread.get() && level.dimension() != ACDimensions.ABYSSAL_WASTELAND) return;
        BlockState state = level.getBlockState(pos);
        if (level.getBiome(pos).is(BiomeTags.IS_OCEAN) && !ACConfig.destroyOcean.get()) {
            return;
        }
        if (!state.getFluidState().isEmpty()
            && state.getFluidState().getType() != LiquidAntimatterCompat.SOURCE.get()
            && state.getFluidState().getType() != LiquidAntimatterCompat.FLOWING.get()) {
            level.setBlockAndUpdate(pos, com.shinoow.abyssalcraft.platform.LiquidCoraliumCompat.BLOCK.get()
                .defaultBlockState());
        } else if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)
            || state.is(Blocks.END_STONE)) {
            level.setBlockAndUpdate(pos, BaseBlocks.ABYSSAL_STONE.get().defaultBlockState());
        } else if (state.is(BlockTags.STONE_BRICKS)) {
            level.setBlockAndUpdate(pos, BaseBlocks.ABYSSAL_STONE_BRICK.get().defaultBlockState());
        } else if (state.is(BlockTags.STONE_ORE_REPLACEABLES)) {
            level.setBlockAndUpdate(pos, BaseBlocks.ABYSSAL_COBBLESTONE.get().defaultBlockState());
        }
    }
}