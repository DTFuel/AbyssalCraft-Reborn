package com.shinoow.abyssalcraft.content.machine.statetransformer;

import java.util.stream.IntStream;

import com.shinoow.abyssalcraft.content.blockentity.base.InventoryBlockEntity;
import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletItem;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class StateTransformerBlockEntity extends InventoryBlockEntity
        implements MenuProvider, TickingBlockEntity, WorldlyContainer {

    public static final int SLOT_TABLET = 0;
    public static final int FIRST_CONTENT_SLOT = 1;
    public static final int SLOT_COUNT = 50;
    public static final int MODE_INSERT = 0;
    public static final int MODE_EXTRACT = 1;
    public static final int PROCESS_DURATION = 200;
    public static final int DATA_PROCESSING = 0;
    public static final int DATA_MODE = 1;
    public static final int DATA_DURATION = 2;
    public static final int DATA_COUNT = 3;

    private static final int[] TABLET_SLOT = {SLOT_TABLET};
    private static final int[] CONTENT_SLOTS = IntStream.range(FIRST_CONTENT_SLOT, SLOT_COUNT).toArray();
    private static final int[] ALL_SLOTS = IntStream.range(0, SLOT_COUNT).toArray();

    private int processingTime;
    private int mode;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROCESSING -> processingTime;
                case DATA_MODE -> mode;
                case DATA_DURATION -> PROCESS_DURATION;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_PROCESSING) processingTime = value;
            else if (index == DATA_MODE) mode = value;
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public StateTransformerBlockEntity(BlockPos pos, BlockState state) {
        super(StateTransformers.STATE_TRANSFORMER_BE.get(), pos, state, SLOT_COUNT);
    }

    @Override
    public void serverTick() {
        if (level == null) return;
        tick(level.registryAccess());
        boolean hasTablet = !getItem(SLOT_TABLET).isEmpty();
        BlockState state = getBlockState();
        if (state.hasProperty(StateTransformerBlock.TABLET)
            && state.getValue(StateTransformerBlock.TABLET) != hasTablet) {
            level.setBlock(worldPosition, state.setValue(StateTransformerBlock.TABLET, hasTablet), 3);
        }
    }

    void tick(HolderLookup.Provider registries) {
        if (!StateTransformerTransaction.canProcess(items, mode)) {
            if (processingTime != 0) {
                processingTime = 0;
                setChanged();
            }
            return;
        }
        processingTime++;
        if (processingTime >= PROCESS_DURATION) {
            StateTransformerTransaction.execute(items, mode, registries);
            processingTime = 0;
        }
        setChanged();
    }

    public boolean setMode(int requestedMode) {
        if (processingTime != 0 || requestedMode < MODE_INSERT || requestedMode > MODE_EXTRACT) return false;
        if (mode == requestedMode) return true;
        mode = requestedMode;
        setChanged();
        return true;
    }

    public int processingTime() {
        return processingTime;
    }

    public int mode() {
        return mode;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (processingTime != 0) return false;
        if (slot == SLOT_TABLET) {
            return stack.getItem() instanceof StoneTabletItem && !StoneTabletStorage.isCursed(stack);
        }
        return slot >= FIRST_CONTENT_SLOT && slot < SLOT_COUNT
            && StateTransformerTransaction.isContentAllowed(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (mode == MODE_INSERT) return side == Direction.UP ? ALL_SLOTS : TABLET_SLOT;
        if (mode == MODE_EXTRACT) return side == Direction.UP ? TABLET_SLOT
            : side == Direction.DOWN ? ALL_SLOTS : new int[0];
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (processingTime != 0) return false;
        if (mode == MODE_INSERT) {
            if (slot > SLOT_TABLET) return side == Direction.UP && canPlaceItem(slot, stack);
            return side.getAxis().isHorizontal() && canPlaceItem(slot, stack)
                && !StoneTabletStorage.hasInventory(stack);
        }
        return mode == MODE_EXTRACT && slot == SLOT_TABLET && side != Direction.DOWN
            && canPlaceItem(slot, stack) && StoneTabletStorage.hasInventory(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (processingTime != 0 || side != Direction.DOWN) return false;
        if (mode == MODE_INSERT) {
            return slot == SLOT_TABLET && StoneTabletStorage.hasInventory(stack);
        }
        return mode == MODE_EXTRACT && (slot != SLOT_TABLET || !StoneTabletStorage.hasInventory(stack));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.abyssalcraft.state_transformer");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new StateTransformerMenu(StateTransformers.STATE_TRANSFORMER_MENU.get(), windowId,
            playerInventory, this, dataAccess);
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveData(tag, registries);
        tag.putInt("ProcessingTime", processingTime);
        tag.putInt("Mode", mode);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadData(tag, registries);
        processingTime = Math.max(0, Math.min(PROCESS_DURATION - 1, tag.getInt("ProcessingTime")));
        mode = tag.getInt("Mode") == MODE_EXTRACT ? MODE_EXTRACT : MODE_INSERT;
    }
}