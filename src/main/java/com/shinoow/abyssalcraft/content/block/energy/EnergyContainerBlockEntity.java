package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.energy.PEUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent two-slot storage behind all five energy-container variants. */
public class EnergyContainerBlockEntity extends InventoryEnergyBlockEntity implements TickingBlockEntity {

    private static final float ITEM_TRANSFER_QUANTA = 20.0F;

    public EnergyContainerBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.ENERGY_CONTAINER_BE.get(), pos, state, 2, capacity(state));
    }

    @Override
    public void serverTick() {
        float received = PEUtils.transferFromItem(getItem(0), this, ITEM_TRANSFER_QUANTA);
        float sent = PEUtils.transferToItem(this, getItem(1), ITEM_TRANSFER_QUANTA);
        if (received > 0 || sent > 0) {
            setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof IEnergyContainerItem;
    }

    private static int capacity(BlockState state) {
        return state.getBlock() instanceof EnergyContainerBlock container
            ? container.tier().containerCapacity()
            : 0;
    }
}