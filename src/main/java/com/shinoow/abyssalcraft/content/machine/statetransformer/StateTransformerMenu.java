package com.shinoow.abyssalcraft.content.machine.statetransformer;

import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletItem;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletStorage;
import com.shinoow.abyssalcraft.content.menu.base.ContainerMenuBase;

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

public final class StateTransformerMenu extends ContainerMenuBase {

    private final Container transformer;
    private final ContainerData data;

    public StateTransformerMenu(MenuType<?> type, int windowId, Inventory playerInventory,
                                Container transformer, ContainerData data) {
        super(type, windowId, StateTransformerBlockEntity.SLOT_COUNT);
        checkContainerSize(transformer, StateTransformerBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, StateTransformerBlockEntity.DATA_COUNT);
        this.transformer = transformer;
        this.data = data;

        addSlot(new LockedProcessingSlot(transformer, StateTransformerBlockEntity.SLOT_TABLET, 8, 71) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !processing() && stack.getItem() instanceof StoneTabletItem
                    && !StoneTabletStorage.isCursed(stack);
            }
        });
        for (int row = 0; row < 7; row++) {
            for (int column = 0; column < 7; column++) {
                addSlot(new LockedProcessingSlot(transformer, column + row * 7 + 1,
                    44 + column * 18, 17 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !processing() && StateTransformerTransaction.isContentAllowed(stack);
                    }
                });
            }
        }
        addPlayerInventory(playerInventory, 156);
        addDataSlots(data);
    }

    public StateTransformerMenu(int windowId, Inventory inventory, FriendlyByteBuf ignored) {
        this(StateTransformers.STATE_TRANSFORMER_MENU.get(), windowId, inventory,
            new SimpleContainer(StateTransformerBlockEntity.SLOT_COUNT),
            new SimpleContainerData(StateTransformerBlockEntity.DATA_COUNT));
    }

    public boolean processing() {
        return data.get(StateTransformerBlockEntity.DATA_PROCESSING) > 0;
    }

    public int processingTime() {
        return data.get(StateTransformerBlockEntity.DATA_PROCESSING);
    }

    public int mode() {
        return data.get(StateTransformerBlockEntity.DATA_MODE);
    }

    public float progress() {
        int duration = data.get(StateTransformerBlockEntity.DATA_DURATION);
        return duration <= 0 ? 0.0F : Math.min(1.0F, (float) processingTime() / duration);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < StateTransformerBlockEntity.MODE_INSERT || id > StateTransformerBlockEntity.MODE_EXTRACT
            || processing()) return false;
        if (transformer instanceof StateTransformerBlockEntity blockEntity) return blockEntity.setMode(id);
        data.set(StateTransformerBlockEntity.DATA_MODE, id);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (processing() || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < StateTransformerBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, StateTransformerBlockEntity.SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof StoneTabletItem && !StoneTabletStorage.isCursed(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (StateTransformerTransaction.isContentAllowed(stack)) {
            if (!moveItemStackTo(stack, StateTransformerBlockEntity.FIRST_CONTENT_SLOT,
                StateTransformerBlockEntity.SLOT_COUNT, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, original);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return transformer.stillValid(player);
    }

    private class LockedProcessingSlot extends Slot {
        private LockedProcessingSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return !processing() && super.mayPickup(player);
        }
    }
}