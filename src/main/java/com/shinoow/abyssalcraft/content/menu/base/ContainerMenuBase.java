package com.shinoow.abyssalcraft.content.menu.base;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Reusable container-menu base (owned by PC-3): the generalized menu framework for Stage 2.
 *
 * <p>Factors out the boilerplate every AbyssalCraft menu repeats -- the 3x9 + hotbar player-inventory
 * slot layout and a generic shift-click ({@link #quickMoveStack}) that moves items between a leading
 * "content" slot region {@code [0, contentSlots)} and the player inventory. Subclasses add their
 * content slots first (indices {@code 0..contentSlots-1}), then call {@link #addPlayerInventory}.
 *
 * <p>Entirely vanilla ({@link AbstractContainerMenu}/{@link Slot}); the only loader fork in the menu
 * subsystem -- {@link MenuType} creation and server-side opening -- lives in
 * {@code platform/MenuCompat}, so this base and its subclasses stay {@code //?}-free. The pilot
 * {@link MachineMenu} (PP-1) keeps its own hand-rolled layout; new Stage 2 menus (item containers such
 * as crystal bag / spirit tablet / spellbook, research table, brewing stand) extend this instead.
 */
public abstract class ContainerMenuBase extends AbstractContainerMenu {

    /** Count of leading content slots (everything before the player inventory) added by the subclass. */
    protected final int contentSlots;

    protected ContainerMenuBase(MenuType<?> type, int windowId, int contentSlots) {
        super(type, windowId);
        this.contentSlots = contentSlots;
    }

    /** Add the standard 3x9 inventory + hotbar with the inventory's top row at {@code invY}. */
    protected void addPlayerInventory(Inventory playerInv, int invY) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 8 + col * 18, invY + 58));
    }

    /** Generic shift-click: content region {@code [0, contentSlots)} &harr; player inventory. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int inventoryEnd = contentSlots + 36;
            if (index < contentSlots) {
                if (!moveItemStackTo(stack, contentSlots, inventoryEnd, true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, contentSlots, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }
}
