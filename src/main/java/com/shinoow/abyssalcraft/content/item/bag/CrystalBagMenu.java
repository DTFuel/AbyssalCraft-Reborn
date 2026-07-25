package com.shinoow.abyssalcraft.content.item.bag;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.shinoow.abyssalcraft.registry.ModMenus;

public final class CrystalBagMenu extends AbstractContainerMenu {

    private static final int COLUMNS = 9;
    private final Container bag;
    private final int bagSlots;
    private final InteractionHand hand;

    public CrystalBagMenu(int windowId, Inventory playerInventory, Container bag, InteractionHand hand) {
        this(windowId, playerInventory, bag, hand, playerInventory.selected);
    }

    private CrystalBagMenu(int windowId, Inventory playerInventory, Container bag, InteractionHand hand,
                           int selectedHotbarSlot) {
        super(ModMenus.CRYSTAL_BAG.get(), windowId);
        this.bag = bag;
        this.bagSlots = bag.getContainerSize();
        this.hand = hand;
        checkContainerSize(bag, bagSlots);
        int rows = rows();
        for (int slot = 0; slot < bagSlots; slot++) {
            addSlot(new Slot(bag, slot, 8 + slot % COLUMNS * 18, 18 + slot / COLUMNS * 18) {
                @Override public boolean mayPlace(ItemStack stack) { return CrystalBagStorage.isCrystal(stack); }
            });
        }
        int inventoryY = 18 + rows * 18 + 14;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, inventoryY + row * 18));
        for (int column = 0; column < 9; column++) {
            int hotbarSlot = column;
            addSlot(new Slot(playerInventory, column, 8 + column * 18, inventoryY + 58) {
                @Override public boolean mayPickup(Player player) {
                    return hand != InteractionHand.MAIN_HAND || hotbarSlot != selectedHotbarSlot;
                }
                @Override public boolean mayPlace(ItemStack stack) {
                    return hand != InteractionHand.MAIN_HAND || hotbarSlot != selectedHotbarSlot;
                }
            });
        }
        bag.startOpen(playerInventory.player);
    }

    public CrystalBagMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, new SimpleContainer(Math.max(1, data.readVarInt())),
            data.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
            playerInventory.selected);
    }

    public int rows() { return Math.max(1, (bagSlots + COLUMNS - 1) / COLUMNS); }
    public int bagSlots() { return bagSlots; }
    public InteractionHand hand() { return hand; }

    @Override public boolean stillValid(Player player) { return bag.stillValid(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).mayPickup(player)) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerEnd = bagSlots + 36;
        if (index < bagSlots) {
            if (!moveItemStackTo(stack, bagSlots, playerEnd, true)) return ItemStack.EMPTY;
        } else if (CrystalBagStorage.isCrystal(stack)) {
            if (!moveItemStackTo(stack, 0, bagSlots, false)) return ItemStack.EMPTY;
        } else {
            int hotbarStart = bagSlots + 27;
            if (index < hotbarStart) {
                if (!moveItemStackTo(stack, hotbarStart, playerEnd, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, bagSlots, hotbarStart, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        bag.stopOpen(player);
    }
}