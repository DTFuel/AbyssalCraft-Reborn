package com.shinoow.abyssalcraft.content.blockentity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity that remembers the direction it faces (owned by PC-1) -- the modern equivalent of the
 * 1.12.2 {@code TEDirectional}.
 *
 * <p>Concrete block entities that need a stored facing beyond a blockstate property (idols, spawners,
 * portal anchor, sealing locks, ...) subclass this. The facing is persisted as the vanilla 3D data
 * value and re-synced to the client through {@link ACBlockEntity#markUpdated()} when changed.
 */
public class DirectionalBlockEntity extends ACBlockEntity {

    private Direction facing = Direction.NORTH;

    public DirectionalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getFacing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
        markUpdated();
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByte("Facing", (byte) facing.get3DDataValue());
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        facing = Direction.from3DDataValue(tag.getByte("Facing"));
    }
}
