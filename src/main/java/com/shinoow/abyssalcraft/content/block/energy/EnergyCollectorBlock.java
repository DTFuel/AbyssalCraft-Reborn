package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.system.energy.EnergyTier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** A tiered PE collector that accepts energy from statues and depositioners. */
public class EnergyCollectorBlock extends TieredEnergyBlock {

    public EnergyCollectorBlock(BlockBehaviour.Properties properties, EnergyTier tier) {
        super(properties, tier);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCollectorBlockEntity(pos, state);
    }
}