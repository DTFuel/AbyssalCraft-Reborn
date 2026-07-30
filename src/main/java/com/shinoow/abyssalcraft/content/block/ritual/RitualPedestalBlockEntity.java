package com.shinoow.abyssalcraft.content.block.ritual;

import com.shinoow.abyssalcraft.content.blockentity.base.ACBlockEntity;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.MachineItemCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ritual pedestal block entity (owned by content/block/ritual): holds a single offering for a nearby
 * {@link RitualAltarBlock}. Implements {@link RitualPedestal} so the altar's ring scan (CR-62) can gather
 * and consume the offering. The stack persists through {@link ACBlockEntity} (the 1.20 &harr; 1.21
 * save/load fork) + {@link ContainerCompat} (the ItemStack NBT fork), kept out of this business code.
 */
public class RitualPedestalBlockEntity extends ACBlockEntity implements RitualPedestal {

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public RitualPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(RitualBlocks.RITUAL_PEDESTAL_BE.get(), pos, state);
    }

    @Override
    public ItemStack getOffering() {
        return items.get(0);
    }

    @Override
    public void consumeOffering(int count) {
        ItemStack offering = items.get(0);
        if (offering.isEmpty() || count <= 0) return;
        ItemStack remainder = MachineItemCompat.craftingRemainder(offering.copyWithCount(1));
        offering.shrink(Math.min(count, offering.getCount()));
        if (offering.isEmpty()) items.set(0, remainder);
        markUpdated();
    }

    /** Place {@code offering} on the pedestal (a single item). */
    public void setOffering(ItemStack offering) {
        items.set(0, offering);
        markUpdated();
    }

    public ItemStack removeOffering() {
        ItemStack offering = items.get(0);
        if (offering.isEmpty()) return ItemStack.EMPTY;
        items.set(0, ItemStack.EMPTY);
        markUpdated();
        return offering;
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerCompat.saveItems(tag, items, registries);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerCompat.loadItems(tag, items, registries);
    }
}
