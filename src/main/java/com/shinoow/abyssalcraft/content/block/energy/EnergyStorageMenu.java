package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.menu.base.ContainerMenuBase;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Shared legacy two-slot PE menu layout at x=44/116, y=38. */
public abstract class EnergyStorageMenu extends ContainerMenuBase {

    protected final Container storage;
    protected final ContainerData data;

    protected EnergyStorageMenu(MenuType<?> type, int windowId, Inventory inventory,
                                Container storage, ContainerData data) {
        super(type, windowId, 2);
        checkContainerSize(storage, 2);
        this.storage = storage;
        this.data = data;
        addSlot(createInputSlot(storage));
        addSlot(createOutputSlot(storage));
        addPlayerInventory(inventory, 84);
        addDataSlots(data);
    }

    protected Slot createInputSlot(Container container) {
        return new Slot(container, 0, 44, 38) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof IEnergyContainerItem;
            }
        };
    }

    protected Slot createOutputSlot(Container container) {
        return new Slot(container, 1, 116, 38) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof IEnergyContainerItem;
            }
        };
    }

    public int potentialEnergy() {
        return data.get(0);
    }

    public int maxPotentialEnergy() {
        return data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof IEnergyContainerItem) {
            if (!moveItemStackTo(stack, 0, 2, false)) return ItemStack.EMPTY;
        } else {
            int hotbarStart = 29;
            if (index < hotbarStart) {
                if (!moveItemStackTo(stack, hotbarStart, slots.size(), false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 2, hotbarStart, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, original);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return storage.stillValid(player);
    }
}