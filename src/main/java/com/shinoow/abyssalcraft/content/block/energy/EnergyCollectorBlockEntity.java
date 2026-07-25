package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.system.energy.IEnergyCollector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent storage behind all five collector variants. */
public class EnergyCollectorBlockEntity extends EnergyBlockEntity implements IEnergyCollector {

    public EnergyCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.ENERGY_COLLECTOR_BE.get(), pos, state, capacity(state));
    }

    private static int capacity(BlockState state) {
        return state.getBlock() instanceof EnergyCollectorBlock collector
            ? collector.tier().collectorCapacity()
            : 0;
    }
}