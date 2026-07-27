package com.shinoow.abyssalcraft.content.machine.rendingpedestal;

import com.shinoow.abyssalcraft.content.item.ritual.StaffOfRendingItem;
import com.shinoow.abyssalcraft.content.menu.base.ContainerMenuBase;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.rending.RendingEnergyType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class RendingPedestalMenu extends ContainerMenuBase {

    private final Container pedestal;
    private final ContainerData data;

    public RendingPedestalMenu(MenuType<?> type, int windowId, Inventory playerInventory,
                               Container pedestal, ContainerData data) {
        super(type, windowId, RendingPedestalBlockEntity.SLOT_COUNT);
        checkContainerSize(pedestal, RendingPedestalBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, RendingPedestalBlockEntity.DATA_COUNT);
        this.pedestal = pedestal;
        this.data = data;
        addSlot(new Slot(pedestal, RendingPedestalBlockEntity.SLOT_ENERGY, 26, 52) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof IEnergyContainerItem;
            }
        });
        addSlot(new Slot(pedestal, RendingPedestalBlockEntity.SLOT_STAFF, 26, 21) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof StaffOfRendingItem;
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(outputSlot(pedestal, 2, 73));
        addSlot(outputSlot(pedestal, 3, 94));
        addSlot(outputSlot(pedestal, 4, 114));
        addSlot(outputSlot(pedestal, 5, 135));
        addPlayerInventory(playerInventory, 84);
        addDataSlots(data);
    }

    public RendingPedestalMenu(int windowId, Inventory inventory, FriendlyByteBuf ignored) {
        this(RendingPedestals.RENDING_PEDESTAL_MENU.get(), windowId, inventory,
            new SimpleContainer(RendingPedestalBlockEntity.SLOT_COUNT),
            new SimpleContainerData(RendingPedestalBlockEntity.DATA_COUNT));
    }

    private static Slot outputSlot(Container container, int slot, int x) {
        return new Slot(container, slot, x, 52) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        };
    }

    public int rendingEnergy(RendingEnergyType type) {
        return data.get(type.ordinal());
    }

    public int potentialEnergy() {
        return data.get(RendingPedestalBlockEntity.DATA_PE);
    }

    public int maxPotentialEnergy() {
        return data.get(RendingPedestalBlockEntity.DATA_MAX_PE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < RendingPedestalBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, RendingPedestalBlockEntity.SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof StaffOfRendingItem) {
            if (!moveItemStackTo(stack, RendingPedestalBlockEntity.SLOT_STAFF,
                RendingPedestalBlockEntity.SLOT_STAFF + 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof IEnergyContainerItem) {
            if (!moveItemStackTo(stack, RendingPedestalBlockEntity.SLOT_ENERGY,
                RendingPedestalBlockEntity.SLOT_ENERGY + 1, false)) return ItemStack.EMPTY;
        } else {
            int hotbarStart = RendingPedestalBlockEntity.SLOT_COUNT + 27;
            if (index < hotbarStart) {
                if (!moveItemStackTo(stack, hotbarStart, slots.size(), false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, RendingPedestalBlockEntity.SLOT_COUNT,
                hotbarStart, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, original);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return pedestal.stillValid(player);
    }
}