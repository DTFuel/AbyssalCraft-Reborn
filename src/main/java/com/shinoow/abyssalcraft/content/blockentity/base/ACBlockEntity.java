package com.shinoow.abyssalcraft.content.blockentity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.platform.BlockEntityCompat;

/**
 * Common AbyssalCraft block-entity base (owned by PC-1, Stage C1).
 *
 * <p>Extends the platform {@link BlockEntityCompat} (which absorbs the 1.20.1&harr;1.21 save/load
 * fork) and adds the change/refresh helper shared by the framework bases. This is the generic base
 * for AbyssalCraft block entities that are NOT the menu-driven furnace machine (which keeps its own
 * {@link MachineBlockEntity}). Business subclasses stay loader-fork-free.
 */
public abstract class ACBlockEntity extends BlockEntityCompat {

    protected ACBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Flag the block entity dirty and send its update tag to tracking clients. Use for visible state
     * changes (facing, displayed item); pure inventory persistence only needs {@link #setChanged()}.
     */
    protected void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}
