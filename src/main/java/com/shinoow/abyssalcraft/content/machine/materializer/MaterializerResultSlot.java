package com.shinoow.abyssalcraft.content.machine.materializer;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class MaterializerResultSlot extends Slot {

    private final MaterializerMenu menu;
    private final int visibleIndex;

    MaterializerResultSlot(MaterializerMenu menu, Container display, int visibleIndex, int x, int y) {
        super(display, visibleIndex, x, y);
        this.menu = menu;
        this.visibleIndex = visibleIndex;
    }

    @Override public boolean mayPlace(ItemStack stack) { return false; }
    @Override public ItemStack getItem() { return menu.displayedResult(visibleIndex); }
    @Override public boolean hasItem() { return !getItem().isEmpty(); }
    @Override public boolean mayPickup(Player player) { return menu.canCraftVisible(visibleIndex, player); }
    @Override public ItemStack remove(int amount) {
        return menu.takeVisible(visibleIndex);
    }
    @Override public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
    }
}