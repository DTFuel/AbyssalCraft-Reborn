package com.shinoow.abyssalcraft.content.menu.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import com.shinoow.abyssalcraft.content.blockentity.base.MachineBlockEntity;

/**
 * Base furnace-like machine menu (owned by PP-1; frozen for P2 reuse).
 *
 * <p>Input/fuel/output slots + the player inventory, plus a {@link ContainerData} carrying
 * progress/burn so a screen can draw the arrows/flame. Entirely vanilla ({@code AbstractContainerMenu},
 * {@code Slot}, {@code ContainerData}); only the {@link MenuType} creation forks and lives in
 * {@code MenuCompat}. P2 machine screens bind to these three machine slots and this data.
 */
public class MachineMenu extends AbstractContainerMenu {

    private final Container machine;
    private final ContainerData data;

    /** Server-side: bound to the real machine block entity inventory + data. */
    public MachineMenu(MenuType<?> type, int windowId, Inventory playerInv, Container machine, ContainerData data) {
        super(type, windowId);
        checkContainerSize(machine, MachineBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, MachineBlockEntity.DATA_COUNT);
        this.machine = machine;
        this.data = data;

        addSlot(new Slot(machine, MachineBlockEntity.SLOT_INPUT, 56, 17));
        addSlot(new Slot(machine, MachineBlockEntity.SLOT_FUEL, 56, 53));
        addSlot(new Slot(machine, MachineBlockEntity.SLOT_OUTPUT, 116, 35));

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));

        addDataSlots(data);
    }

    /** Client-side factory from the opening buffer -- dummy backing store until wired to a real BE in P2. */
    public MachineMenu(MenuType<?> type, int windowId, Inventory playerInv, FriendlyByteBuf ignoredExtraData) {
        this(type, windowId, playerInv,
            new SimpleContainer(MachineBlockEntity.SLOT_COUNT),
            new SimpleContainerData(MachineBlockEntity.DATA_COUNT));
    }

    /** Progress fraction in {@code [0,1]} for the GUI arrow. */
    public float progress() {
        int max = data.get(MachineBlockEntity.DATA_MAX_PROGRESS);
        return max == 0 ? 0F : (float) data.get(MachineBlockEntity.DATA_PROGRESS) / max;
    }

    @Override
    public boolean stillValid(Player player) {
        return machine.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int machineSlots = MachineBlockEntity.SLOT_COUNT;
            int inventoryEnd = machineSlots + 36;
            if (index < machineSlots) {
                if (!moveItemStackTo(stack, machineSlots, inventoryEnd, true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, MachineBlockEntity.SLOT_INPUT, MachineBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }
}
