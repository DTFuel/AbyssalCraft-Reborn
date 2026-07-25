package com.shinoow.abyssalcraft.system.energy.structure;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** The vertical three-statue Totem Pole Place of Power. */
public final class TotemPolePlaceOfPower extends AbstractPlaceOfPower {

    public TotemPolePlaceOfPower() {
        super("totempole", 3.0F, BlockPos.ZERO);
    }

    @Override
    protected boolean isValid(Level level, BlockPos basePos, boolean formed) {
        return isActivationBlock(level, basePos, formed)
            && isStatue(level, basePos.above())
            && isStatue(level, basePos.above(2))
            && isStatue(level, basePos.above(3));
    }

    @Override
    protected List<BlockPos> componentPositions(BlockPos basePos) {
        return List.of(basePos.above(), basePos.above(2), basePos.above(3));
    }

    @Override
    public BlockState[][][] getRenderData() {
        return new BlockState[][][] {
            {{BaseBlocks.MONOLITH_STONE.get().defaultBlockState()}},
            {{statue(DeityType.YOGSOTHOTH)}},
            {{statue(DeityType.AZATHOTH)}},
            {{statue(DeityType.NYARLATHOTEP)}}
        };
    }

    private static BlockState statue(DeityType deity) {
        return EnergyBlocks.DEITY_STATUES.get(deity.ordinal()).get().defaultBlockState();
    }
}