package com.shinoow.abyssalcraft.system.transfer;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import com.shinoow.abyssalcraft.platform.CapabilityAccess;
import com.shinoow.abyssalcraft.platform.ContainerCompat;

/**
 * Adapts a vanilla {@link Container} (a block-entity inventory, a menu backing store, or a plain
 * {@link net.minecraft.world.SimpleContainer}) to the neutral {@link CapabilityAccess.ItemView}, so the
 * {@link ItemTransfer} engine can move items in and out of {@code Container}-based inventories fork-free
 * -- without needing a loader item-handler capability. Slot semantics follow the {@code IItemHandler}
 * contract: {@link #insert}/{@link #extract} honour stack limits and item compatibility and respect
 * {@code simulate} (owned by PC-4).
 */
public final class ContainerItemView implements CapabilityAccess.ItemView {

    private final Container container;

    public ContainerItemView(Container container) {
        this.container = container;
    }

    @Override
    public int size() {
        return container.getContainerSize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return container.getItem(slot);
    }

    @Override
    public ItemStack insert(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !container.canPlaceItem(slot, stack)) {
            return stack;
        }
        ItemStack existing = container.getItem(slot);
        int limit = Math.min(container.getMaxStackSize(), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ContainerCompat.canStack(existing, stack)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }
        int accepted = Math.min(limit, stack.getCount());
        if (!simulate) {
            if (existing.isEmpty()) {
                ItemStack placed = stack.copy();
                placed.setCount(accepted);
                container.setItem(slot, placed);
            } else {
                existing.grow(accepted);
                container.setItem(slot, existing);
            }
            container.setChanged();
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(accepted);
        return remainder;
    }

    @Override
    public ItemStack extract(int slot, int amount, boolean simulate) {
        ItemStack existing = container.getItem(slot);
        if (existing.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        int extracted = Math.min(amount, existing.getCount());
        ItemStack result = existing.copy();
        result.setCount(extracted);
        if (!simulate) {
            container.removeItem(slot, extracted);
            container.setChanged();
        }
        return result;
    }
}
