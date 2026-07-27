package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.energy.PEUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent two-slot storage behind all five energy-container variants. */
public class EnergyContainerBlockEntity extends InventoryEnergyBlockEntity implements TickingBlockEntity, MenuProvider {

    private static final float ITEM_TRANSFER_QUANTA = 20.0F;
    private final ContainerData dataAccess = new ContainerData() {
        @Override public int get(int index) {
            return index == 0 ? (int) getContainedEnergy() : getMaxEnergy();
        }
        @Override public void set(int index, int value) {
            if (index == 0) setEnergy(value);
        }
        @Override public int getCount() { return 2; }
    };

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

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.abyssalcraft.energy_container");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new EnergyContainerMenu(EnergyBlocks.ENERGY_CONTAINER_MENU.get(), windowId,
            inventory, this, dataAccess);
    }

    private static int capacity(BlockState state) {
        return state.getBlock() instanceof EnergyContainerBlock container
            ? container.tier().containerCapacity()
            : 0;
    }
}