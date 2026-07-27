package com.shinoow.abyssalcraft.content.block.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Existing tombstone block upgraded with its legacy ghoul-spawning host. */
public final class TombstoneBlock extends DecoFacingBlock implements EntityBlock {

    public TombstoneBlock(Properties properties) {
        super(properties, ShapeKind.TOMBSTONE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TombstoneBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide || type != DecoBlocks.TOMBSTONE_BE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<TombstoneBlockEntity>)
            TombstoneBlockEntity::serverTick;
    }
}