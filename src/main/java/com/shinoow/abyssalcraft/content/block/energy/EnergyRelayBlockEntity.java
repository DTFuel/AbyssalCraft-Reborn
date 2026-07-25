package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.IEnergyTransporter;
import com.shinoow.abyssalcraft.system.energy.PEUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Persistent, server-ticking storage behind all five relay variants. */
public class EnergyRelayBlockEntity extends EnergyBlockEntity implements IEnergyTransporter, TickingBlockEntity {

    public EnergyRelayBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.ENERGY_RELAY_BE.get(), pos, state, capacity(state));
    }

    @Override
    public void serverTick() {
        if (level == null || level.hasNeighborSignal(worldPosition)) {
            return;
        }
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        long gameTime = level.getGameTime();
        if (gameTime % 20 == 0) {
            PEUtils.collectAdjacent(level, worldPosition, facing.getOpposite(), this);
        }
        if (gameTime % 40 == 0 && canTransferPE()) {
            PEUtils.transferInDirection(level, worldPosition, facing, this);
        }
    }

    @Override
    public int getTransferRange() {
        return relayBlock().tier().relayRange();
    }

    @Override
    public float getDrainQuanta() {
        return relayBlock().tier().relayDrainQuanta();
    }

    @Override
    public float getTransferQuanta() {
        return relayBlock().tier().transferQuanta();
    }

    private EnergyRelayBlock relayBlock() {
        return (EnergyRelayBlock) getBlockState().getBlock();
    }

    private static int capacity(BlockState state) {
        return state.getBlock() instanceof EnergyRelayBlock relay
            ? relay.tier().relayCapacity()
            : 0;
    }
}