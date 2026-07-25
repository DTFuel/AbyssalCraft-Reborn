package com.shinoow.abyssalcraft.content.item.bag;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CrystalBagInventory implements Container {

    private final ServerPlayer owner;
    private final InteractionHand hand;
    private final ItemStack bag;
    private final NonNullList<ItemStack> contents;

    public CrystalBagInventory(ServerPlayer owner, InteractionHand hand, ItemStack bag) {
        if (!CrystalBagStorage.isBag(bag)) {
            throw new IllegalArgumentException("Crystal Bag inventory requires a Crystal Bag stack");
        }
        this.owner = owner;
        this.hand = hand;
        this.bag = bag;
        this.contents = CrystalBagStorage.load(bag, owner.level().registryAccess());
    }

    @Override public int getContainerSize() { return contents.size(); }
    @Override public boolean isEmpty() { return contents.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return contents.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(contents, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(contents, slot);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !canPlaceItem(slot, stack)) return;
        contents.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return CrystalBagStorage.isCrystal(stack); }

    @Override
    public void setChanged() {
        CrystalBagStorage.saveInventory(bag, contents, owner.level().registryAccess());
        owner.getInventory().setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return player == owner && owner.getItemInHand(hand) == bag && CrystalBagStorage.isBag(bag);
    }

    @Override
    public void clearContent() {
        contents.clear();
        setChanged();
    }
}