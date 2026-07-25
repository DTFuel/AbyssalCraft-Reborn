package com.shinoow.abyssalcraft.content.machine.transmutator;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.shinoow.abyssalcraft.content.blockentity.base.MachineBlockEntity;
import com.shinoow.abyssalcraft.content.menu.base.MachineResultSlot;

public final class TransmutatorMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOTS = 3;
    private final Container machine;
    private final ContainerData data;

    public TransmutatorMenu(int windowId, Inventory inventory, Container machine, ContainerData data) {
        super(Transmutators.TRANSMUTATOR_MENU.get(), windowId);
        checkContainerSize(machine, MACHINE_SLOTS);
        checkContainerDataCount(data, MachineBlockEntity.DATA_COUNT);
        this.machine = machine;
        this.data = data;
        addSlot(new Slot(machine, MachineBlockEntity.SLOT_INPUT, 56, 17));
        addSlot(new Slot(machine, MachineBlockEntity.SLOT_FUEL, 56, 53) {
            @Override public boolean mayPlace(ItemStack stack) { return TransmutatorBlockEntity.isFuel(stack); }
        });
        addSlot(new MachineResultSlot(machine, MachineBlockEntity.SLOT_OUTPUT, 116, 35));
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    public TransmutatorMenu(int windowId, Inventory inventory, FriendlyByteBuf ignored) {
        this(windowId, inventory, new SimpleContainer(MACHINE_SLOTS),
            new SimpleContainerData(MachineBlockEntity.DATA_COUNT));
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }

    public float progress() { return fraction(MachineBlockEntity.DATA_PROGRESS, MachineBlockEntity.DATA_MAX_PROGRESS); }
    public float burn() { return fraction(MachineBlockEntity.DATA_BURN, MachineBlockEntity.DATA_MAX_BURN); }
    private float fraction(int value, int maxValue) {
        int max = data.get(maxValue);
        return max <= 0 ? 0F : Math.min(1F, (float) data.get(value) / max);
    }

    @Override public boolean stillValid(Player player) { return machine.stillValid(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerEnd = MACHINE_SLOTS + 36;
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, playerEnd, true)) return ItemStack.EMPTY;
            if (slot instanceof MachineResultSlot resultSlot) {
                resultSlot.recordQuickTake(original.getCount() - stack.getCount());
            }
        } else if (TransmutatorBlockEntity.isFuel(stack)) {
            if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
        } else if (machine instanceof TransmutatorBlockEntity blockEntity && blockEntity.isRecipeInput(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else {
            int hotbarStart = MACHINE_SLOTS + 27;
            if (index < hotbarStart) {
                if (!moveItemStackTo(stack, hotbarStart, playerEnd, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, MACHINE_SLOTS, hotbarStart, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
}