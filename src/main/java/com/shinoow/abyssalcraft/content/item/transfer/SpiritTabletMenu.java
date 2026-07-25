package com.shinoow.abyssalcraft.content.item.transfer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SpiritTabletMenu extends AbstractContainerMenu {

    private final Container filter;
    private final InteractionHand hand;
    private final ItemStack tablet;
    private final DataSlot subtype = DataSlot.standalone();
    private final DataSlot components = DataSlot.standalone();

    public SpiritTabletMenu(int windowId, Inventory playerInventory, Container filter,
                            InteractionHand hand, ItemStack tablet) {
        super(TransferContent.SPIRIT_TABLET_MENU.get(), windowId);
        this.filter = filter;
        this.hand = hand;
        this.tablet = tablet;
        checkContainerSize(filter, SpiritTabletStorage.FILTER_SIZE);
        subtype.set(SpiritTabletStorage.ignoreSubtypes(tablet) ? 1 : 0);
        components.set(SpiritTabletStorage.matchComponents(tablet) ? 1 : 0);
        for (int slot = 0; slot < SpiritTabletStorage.FILTER_SIZE; slot++) {
            addSlot(new Slot(filter, slot, 44 + slot * 18, 17) {
                @Override public boolean mayPlace(ItemStack stack) { return filter.canPlaceItem(getSlotIndex(), stack); }
            });
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
        for (int column = 0; column < 9; column++) {
            int hotbarSlot = column;
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142) {
                @Override public boolean mayPickup(Player player) {
                    return hand != InteractionHand.MAIN_HAND || hotbarSlot != playerInventory.selected;
                }
                @Override public boolean mayPlace(ItemStack stack) {
                    return hand != InteractionHand.MAIN_HAND || hotbarSlot != playerInventory.selected;
                }
            });
        }
        addDataSlot(subtype);
        addDataSlot(components);
        filter.startOpen(playerInventory.player);
    }

    public SpiritTabletMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, new SimpleContainer(SpiritTabletStorage.FILTER_SIZE),
            data.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
            ItemStack.EMPTY);
    }

    public boolean ignoreSubtypes() { return subtype.get() != 0; }
    public boolean matchComponents() { return components.get() != 0; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id > 1 || !(tablet.getItem() instanceof SpiritTabletItem)) {
            return false;
        }
        SpiritTabletStorage.toggleFilter(tablet, id);
        subtype.set(SpiritTabletStorage.ignoreSubtypes(tablet) ? 1 : 0);
        components.set(SpiritTabletStorage.matchComponents(tablet) ? 1 : 0);
        return true;
    }

    @Override public boolean stillValid(Player player) { return filter.stillValid(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).mayPickup(player)) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerStart = SpiritTabletStorage.FILTER_SIZE;
        if (index < playerStart) {
            if (!moveItemStackTo(stack, playerStart, slots.size(), true)) return ItemStack.EMPTY;
        } else if (filter.canPlaceItem(0, stack)) {
            if (!moveItemStackTo(stack, 0, playerStart, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        filter.stopOpen(player);
    }
}