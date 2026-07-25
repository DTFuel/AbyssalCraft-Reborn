package com.shinoow.abyssalcraft.system.energy.structure;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.registry.BaseBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Four-way darkstone arch surrounding a single deity statue. */
public final class ArchwayPlaceOfPower extends AbstractPlaceOfPower {

    public ArchwayPlaceOfPower() {
        super("archway", 1.0F, new BlockPos(2, 0, 0));
    }

    @Override
    protected boolean isValid(Level level, BlockPos basePos, boolean formed) {
        if (!isActivationBlock(level, basePos, formed) || !isStatue(level, basePos.above())) {
            return false;
        }
        return matchesArch(level, basePos, Direction.WEST, Direction.EAST)
            || matchesArch(level, basePos, Direction.NORTH, Direction.SOUTH);
    }

    private static boolean matchesArch(Level level, BlockPos basePos, Direction negative, Direction positive) {
        for (int height = 0; height < 3; height++) {
            if (!(level.getBlockState(basePos.relative(negative, 2).above(height)).getBlock() instanceof WallBlock)
                || !(level.getBlockState(basePos.relative(positive, 2).above(height)).getBlock() instanceof WallBlock)) {
                return false;
            }
        }
        return matchesStair(level.getBlockState(basePos.relative(negative, 2).above(3)),
                positive, Half.BOTTOM)
            && matchesStair(level.getBlockState(basePos.relative(negative).above(3)), negative, Half.TOP)
            && matchesSlab(level.getBlockState(basePos.above(3)), SlabType.TOP)
            && matchesStair(level.getBlockState(basePos.relative(positive).above(3)), positive, Half.TOP)
            && matchesStair(level.getBlockState(basePos.relative(positive, 2).above(3)),
                negative, Half.BOTTOM)
            && matchesSlab(level.getBlockState(basePos.relative(negative, 2).above(4)), SlabType.BOTTOM)
            && matchesSlab(level.getBlockState(basePos.relative(negative).above(4)), SlabType.BOTTOM)
            && matchesSlab(level.getBlockState(basePos.above(4)), SlabType.BOTTOM)
            && matchesSlab(level.getBlockState(basePos.relative(positive).above(4)), SlabType.BOTTOM)
            && matchesSlab(level.getBlockState(basePos.relative(positive, 2).above(4)), SlabType.BOTTOM);
    }

    private static boolean matchesStair(BlockState state, Direction facing, Half half) {
        return state.getBlock() instanceof StairBlock
            && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing
            && state.getValue(BlockStateProperties.HALF) == half;
    }

    private static boolean matchesSlab(BlockState state, SlabType type) {
        return state.getBlock() instanceof SlabBlock
            && state.getValue(BlockStateProperties.SLAB_TYPE) == type;
    }

    @Override
    protected List<BlockPos> componentPositions(BlockPos basePos) {
        return List.of(basePos.above());
    }

    @Override
    public BlockState[][][] getRenderData() {
        BlockState wall = BaseBlocks.DARKSTONE_COBBLESTONE_WALL.get().defaultBlockState();
        BlockState stone = BaseBlocks.MONOLITH_STONE.get().defaultBlockState();
        BlockState statue = EnergyBlocks.DEITY_STATUES.get(0).get().defaultBlockState();
        BlockState stair = BaseBlocks.DARKSTONE_COBBLESTONE_STAIRS.get().defaultBlockState();
        BlockState slab = BaseBlocks.DARKSTONE_COBBLESTONE_SLAB.get().defaultBlockState();
        return new BlockState[][][] {
            {{wall}, {null}, {stone}, {null}, {wall}},
            {{wall}, {null}, {statue}, {null}, {wall}},
            {{wall}, {null}, {null}, {null}, {wall}},
            {{stair.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)},
             {stair.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
                 .setValue(BlockStateProperties.HALF, Half.TOP)},
             {slab.setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP)},
             {stair.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                 .setValue(BlockStateProperties.HALF, Half.TOP)},
             {stair.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)}},
            {{slab}, {slab}, {slab}, {slab}, {slab}}
        };
    }
}