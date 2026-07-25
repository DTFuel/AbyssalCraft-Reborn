package com.shinoow.abyssalcraft.system.energy.structure;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.registry.BaseBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/** The three-layer 3x3 Basic Place of Power with four deity statues. */
public final class BasicPlaceOfPower extends AbstractPlaceOfPower {

    public BasicPlaceOfPower() {
        super("basic", 2.0F, new BlockPos(1, 2, 1));
    }

    @Override
    protected boolean isValid(Level level, BlockPos basePos, boolean formed) {
        if (!isActivationBlock(level, basePos, formed)) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!isStatue(level, basePos.relative(direction))) {
                return false;
            }
        }
        BlockPos middle = basePos.below();
        if (!middleAndCrossAreMonolith(level, middle)) {
            return false;
        }
        for (int x : new int[] {-1, 1}) {
            for (int z : new int[] {-1, 1}) {
                if (!level.getBlockState(middle.offset(x, 0, z)).is(EnergyBlocks.MONOLITH_PILLAR.get())) {
                    return false;
                }
            }
        }
        BlockPos bottom = basePos.below(2);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (!level.getBlockState(bottom.offset(x, 0, z)).is(BaseBlocks.MONOLITH_STONE.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean middleAndCrossAreMonolith(Level level, BlockPos center) {
        if (!level.getBlockState(center).is(BaseBlocks.MONOLITH_STONE.get())) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(center.relative(direction)).is(BaseBlocks.MONOLITH_STONE.get())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected List<BlockPos> componentPositions(BlockPos basePos) {
        return List.of(basePos.north(), basePos.south(), basePos.east(), basePos.west());
    }

    @Override
    public BlockState[][][] getRenderData() {
        BlockState stone = BaseBlocks.MONOLITH_STONE.get().defaultBlockState();
        BlockState pillar = EnergyBlocks.MONOLITH_PILLAR.get().defaultBlockState();
        BlockState statue = EnergyBlocks.DEITY_STATUES.get(0).get().defaultBlockState();
        return new BlockState[][][] {
            {{stone, stone, stone}, {stone, stone, stone}, {stone, stone, stone}},
            {{pillar, stone, pillar}, {stone, stone, stone}, {pillar, stone, pillar}},
            {{null, statue.setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), null},
             {statue, stone, statue.setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH)},
             {null, statue.setValue(HorizontalDirectionalBlock.FACING, Direction.EAST), null}}
        };
    }
}