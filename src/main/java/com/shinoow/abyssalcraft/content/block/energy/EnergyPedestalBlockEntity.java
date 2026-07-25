package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.IEnergyCollector;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.energy.PEUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent one-slot storage behind all five energy-pedestal variants. */
public class EnergyPedestalBlockEntity extends InventoryEnergyBlockEntity
    implements IEnergyCollector, TickingBlockEntity {

    private static final float ITEM_TRANSFER_QUANTA = 20.0F;

    public EnergyPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.ENERGY_PEDESTAL_BE.get(), pos, state, 1, capacity(state));
    }

    @Override
    public void serverTick() {
        if (PEUtils.transferToItem(this, getStoredItem(), ITEM_TRANSFER_QUANTA) > 0) {
            setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof IEnergyContainerItem;
    }

    private static int capacity(BlockState state) {
        return state.getBlock() instanceof EnergyPedestalBlock pedestal
            ? pedestal.tier().pedestalCapacity()
            : 0;
    }
}