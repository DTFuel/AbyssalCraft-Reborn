package com.shinoow.abyssalcraft.content.machine.statetransformer;

import com.shinoow.abyssalcraft.content.item.bag.CrystalBagItem;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletItem;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletStorage;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletItem;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/** Atomic Stone Tablet insert/extract operations for the State Transformer. */
public final class StateTransformerTransaction {

    private StateTransformerTransaction() {}

    public static boolean canProcess(NonNullList<ItemStack> machine, int mode) {
        if (machine.size() != StateTransformerBlockEntity.SLOT_COUNT) return false;
        ItemStack tablet = machine.get(StateTransformerBlockEntity.SLOT_TABLET);
        if (!(tablet.getItem() instanceof StoneTabletItem) || StoneTabletStorage.isCursed(tablet)) return false;
        if (mode == StateTransformerBlockEntity.MODE_INSERT) {
            if (StoneTabletStorage.hasInventory(tablet)) return false;
            boolean hasContent = false;
            for (int slot = StateTransformerBlockEntity.FIRST_CONTENT_SLOT; slot < machine.size(); slot++) {
                ItemStack input = machine.get(slot);
                if (!input.isEmpty()) {
                    if (!isContentAllowed(input)) return false;
                    hasContent = true;
                }
            }
            return hasContent;
        }
        if (mode == StateTransformerBlockEntity.MODE_EXTRACT) {
            if (!StoneTabletStorage.hasInventory(tablet)) return false;
            for (int slot = StateTransformerBlockEntity.FIRST_CONTENT_SLOT; slot < machine.size(); slot++) {
                if (!machine.get(slot).isEmpty()) return false;
            }
            return true;
        }
        return false;
    }

    public static boolean execute(NonNullList<ItemStack> machine, int mode, HolderLookup.Provider registries) {
        if (!canProcess(machine, mode)) return false;
        return mode == StateTransformerBlockEntity.MODE_INSERT
            ? insert(machine, registries) : extract(machine, registries);
    }

    public static boolean isContentAllowed(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof StoneTabletItem
            || stack.getItem() instanceof SpiritTabletItem || stack.getItem() instanceof CrystalBagItem) {
            return false;
        }
        return !(stack.getItem() instanceof BlockItem blockItem
            && blockItem.getBlock() instanceof ShulkerBoxBlock);
    }

    private static boolean insert(NonNullList<ItemStack> machine, HolderLookup.Provider registries) {
        NonNullList<ItemStack> payload = NonNullList.withSize(StoneTabletStorage.INVENTORY_SIZE, ItemStack.EMPTY);
        float potentialEnergy = 0.0F;
        for (int slot = StateTransformerBlockEntity.FIRST_CONTENT_SLOT; slot < machine.size(); slot++) {
            ItemStack input = machine.get(slot);
            if (!input.isEmpty()) {
                if (!isContentAllowed(input)) return false;
                payload.set(slot - StateTransformerBlockEntity.FIRST_CONTENT_SLOT, input.copy());
                potentialEnergy += input.getCount() * Math.max(1, 64 / input.getMaxStackSize());
            }
        }

        ItemStack transformedTablet = machine.get(StateTransformerBlockEntity.SLOT_TABLET).copy();
        StoneTabletStorage.store(transformedTablet, payload, potentialEnergy, registries);
        machine.set(StateTransformerBlockEntity.SLOT_TABLET, transformedTablet);
        for (int slot = StateTransformerBlockEntity.FIRST_CONTENT_SLOT; slot < machine.size(); slot++) {
            machine.set(slot, ItemStack.EMPTY);
        }
        return true;
    }

    private static boolean extract(NonNullList<ItemStack> machine, HolderLookup.Provider registries) {
        ItemStack transformedTablet = machine.get(StateTransformerBlockEntity.SLOT_TABLET).copy();
        final NonNullList<ItemStack> payload;
        try {
            payload = StoneTabletStorage.load(transformedTablet, registries);
        } catch (RuntimeException malformedPayload) {
            return false;
        }
        if (payload.size() != StoneTabletStorage.INVENTORY_SIZE) return false;

        StoneTabletStorage.clear(transformedTablet);
        machine.set(StateTransformerBlockEntity.SLOT_TABLET, transformedTablet);
        for (int index = 0; index < payload.size(); index++) {
            machine.set(index + StateTransformerBlockEntity.FIRST_CONTENT_SLOT, payload.get(index));
        }
        return true;
    }
}