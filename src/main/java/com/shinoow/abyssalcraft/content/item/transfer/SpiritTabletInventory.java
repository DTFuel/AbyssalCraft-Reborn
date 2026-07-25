package com.shinoow.abyssalcraft.content.item.transfer;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public final class SpiritTabletInventory implements Container {

    private final ServerPlayer owner;
    private final InteractionHand hand;
    private final ItemStack tablet;
    private final NonNullList<ItemStack> filter;

    public SpiritTabletInventory(ServerPlayer owner, InteractionHand hand, ItemStack tablet) {
        this.owner = owner;
        this.hand = hand;
        this.tablet = tablet;
        this.filter = SpiritTabletStorage.loadFilter(tablet, owner.level().registryAccess());
    }

    @Override public int getContainerSize() { return SpiritTabletStorage.FILTER_SIZE; }
    @Override public boolean isEmpty() { return filter.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return filter.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(filter, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(filter, slot);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !canPlaceItem(slot, stack)) return;
        filter.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof SpiritTabletItem)
            && !(stack.getItem() instanceof com.shinoow.abyssalcraft.content.item.bag.CrystalBagItem)
            && (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof ShulkerBoxBlock));
    }

    @Override
    public void setChanged() {
        SpiritTabletStorage.saveFilter(tablet, filter, owner.level().registryAccess());
        owner.getInventory().setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return player == owner && owner.getItemInHand(hand) == tablet
            && tablet.getItem() instanceof SpiritTabletItem;
    }

    @Override
    public void clearContent() {
        filter.clear();
        setChanged();
    }
}