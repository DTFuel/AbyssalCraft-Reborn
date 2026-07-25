package com.shinoow.abyssalcraft.content.blockentity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.platform.ContainerCompat;

/**
 * Reusable inventory block entity (owned by PC-1) -- a {@link Container} backed by a fixed-size stack
 * list, persisted through the compat layer.
 *
 * <p>Size 1 is the modern equivalent of the 1.12.2 {@code ISingletonInventory} (altars / pedestals
 * that hold and display a single stack); larger sizes back crates and other non-machine inventories.
 * Furnace-style machines keep their own {@link MachineBlockEntity} (menu + progress/fuel data). Item
 * NBT and the save/load fork live in {@link ContainerCompat} / {@code BlockEntityCompat}, so this
 * class carries no loader {@code //?}.
 */
public class InventoryBlockEntity extends ACBlockEntity implements Container {

    protected final NonNullList<ItemStack> items;

    public InventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    /** Single-slot read convenience (the {@code ISingletonInventory} shape). */
    public ItemStack getStoredItem() {
        return items.get(0);
    }

    /** Single-slot write convenience: stores the stack in slot 0 and pushes a render update. */
    public void setStoredItem(ItemStack stack) {
        items.set(0, stack);
        markUpdated();
    }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // --- persistence (registries is null on 1.20.1, the component lookup on 1.21) ---

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerCompat.saveItems(tag, items, registries);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerCompat.loadItems(tag, items, registries);
    }
}
