package com.shinoow.abyssalcraft.content.menu.base;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.shinoow.abyssalcraft.content.blockentity.base.MachineBlockEntity;

public final class MachineResultSlot extends Slot {

    private final MachineBlockEntity machine;
    private final int machineSlot;
    private int removeCount;

    public MachineResultSlot(Container container, int machineSlot, int x, int y) {
        super(container, machineSlot, x, y);
        this.machine = container instanceof MachineBlockEntity blockEntity ? blockEntity : null;
        this.machineSlot = machineSlot;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        if (machine == null) {
            return super.remove(amount);
        }
        machine.beginPlayerExtraction();
        try {
            ItemStack removed = super.remove(amount);
            removeCount += removed.getCount();
            return removed;
        } finally {
            machine.endPlayerExtraction();
        }
    }

    public void recordQuickTake(int amount) {
        removeCount += Math.max(0, amount);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (machine != null) {
            machine.awardPlayerExperience(player, machineSlot, removeCount);
        }
        removeCount = 0;
        super.onTake(player, stack);
    }
}