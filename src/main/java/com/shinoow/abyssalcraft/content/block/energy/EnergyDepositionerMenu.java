package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletItem;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletStorage;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class EnergyDepositionerMenu extends EnergyStorageMenu {

    public EnergyDepositionerMenu(int windowId, Inventory inventory, Container storage, ContainerData data) {
        super(EnergyBlocks.ENERGY_DEPOSITIONER_MENU.get(), windowId, inventory, storage, data);
        checkContainerDataCount(data, EnergyDepositionerBlockEntity.DATA_COUNT);
    }

    public EnergyDepositionerMenu(int windowId, Inventory inventory, FriendlyByteBuf ignored) {
        this(windowId, inventory, new SimpleContainer(2),
            new SimpleContainerData(EnergyDepositionerBlockEntity.DATA_COUNT));
    }

    @Override
    protected Slot createInputSlot(Container container) {
        return new Slot(container, 0, 44, 38) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof StoneTabletItem
                    && StoneTabletStorage.hasInventory(stack) && !StoneTabletStorage.isCursed(stack);
            }
            @Override public int getMaxStackSize() { return 1; }
        };
    }

    @Override
    protected Slot createOutputSlot(Container container) {
        return new Slot(container, 1, 116, 38) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        };
    }

    @Override
    public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
        if (index >= 2 && index < slots.size()) {
            ItemStack stack = slots.get(index).getItem();
            if (stack.getItem() instanceof StoneTabletItem
                && StoneTabletStorage.hasInventory(stack) && !StoneTabletStorage.isCursed(stack)) {
                ItemStack original = stack.copy();
                if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
                if (stack.isEmpty()) slots.get(index).set(ItemStack.EMPTY);
                else slots.get(index).setChanged();
                slots.get(index).onTake(player, original);
                return original;
            }
        }
        return super.quickMoveStack(player, index);
    }

    public int processingTime() { return data.get(EnergyDepositionerBlockEntity.DATA_PROCESSING); }
    public float progress() { return (float) processingTime() / EnergyDepositionerBlockEntity.PROCESS_DURATION; }
    public int tolerance() { return data.get(EnergyDepositionerBlockEntity.DATA_TOLERANCE); }
    public int collectorCount() { return data.get(EnergyDepositionerBlockEntity.DATA_COLLECTORS); }
    public DeityType activeDeity() {
        int value = data.get(EnergyDepositionerBlockEntity.DATA_DEITY);
        return value < 0 || value >= DeityType.values().length ? null : DeityType.values()[value];
    }
    public AmplifierType activeAmplifier() {
        int value = data.get(EnergyDepositionerBlockEntity.DATA_AMPLIFIER);
        return value < 0 || value >= AmplifierType.values().length ? null : AmplifierType.values()[value];
    }
}