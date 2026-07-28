package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
//? if forge {
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.WorldlyContainer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
//?}

/**
 * Compat: BlockEntity NBT persistence (vanilla axis).
 *
 * <p>1.21 added a {@code HolderLookup.Provider} parameter to {@code saveAdditional} and renamed
 * {@code load} to {@code loadAdditional}. Subclasses implement the version-neutral
 * {@link #saveData}/{@link #loadData}.
 */
public abstract class BlockEntityCompat extends BlockEntity {

    //? if forge {
    private final Map<Direction, LazyOptional<IItemHandler>> sidedItemHandlers = new EnumMap<>(Direction.class);
    private LazyOptional<IItemHandler> unsidedItemHandler = LazyOptional.empty();
    //?}

    protected BlockEntityCompat(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    //? if >=1.21 {
    /*@Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveData(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadData(tag, registries);
    }
    *///?} else {
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveData(tag, null);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadData(tag, null);
    }
    //?}

    /**
     * Write persistent data. {@code registries} is the component lookup on 1.21 and {@code null} on
     * 1.20.1 (unused there); pass it straight through to {@link ContainerCompat} for item stacks.
     */
    protected abstract void saveData(CompoundTag tag, HolderLookup.Provider registries);

    /** Read persistent data. {@code registries} mirrors {@link #saveData}. */
    protected abstract void loadData(CompoundTag tag, HolderLookup.Provider registries);

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    //? if >=1.21 {
    /*@Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    *///?} else {
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
    //?}

    //? if forge {
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && this instanceof WorldlyContainer inventory) {
            if (side == null) {
                if (!unsidedItemHandler.isPresent()) {
                    unsidedItemHandler = LazyOptional.of(() -> new InvWrapper(inventory));
                }
                return unsidedItemHandler.cast();
            }
            return sidedItemHandlers.computeIfAbsent(side,
                direction -> LazyOptional.of(() -> new SidedInvWrapper(inventory, direction))).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        unsidedItemHandler.invalidate();
        unsidedItemHandler = LazyOptional.empty();
        sidedItemHandlers.values().forEach(LazyOptional::invalidate);
        sidedItemHandlers.clear();
    }
    //?}
}
