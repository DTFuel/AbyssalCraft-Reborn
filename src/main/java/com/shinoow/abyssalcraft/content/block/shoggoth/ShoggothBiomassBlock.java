package com.shinoow.abyssalcraft.content.block.shoggoth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;

/** Persistent Shoggoth biomass that produces five Shoggoths before hardening into monolith stone. */
public final class ShoggothBiomassBlock extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.6D, 16.0D);

    public ShoggothBiomassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                        BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof AbstractShoggoth)) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.4D, 1.0D, 0.4D));
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShoggothBiomassBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide || type != ShoggothBlocks.SHOGGOTH_BIOMASS_BE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<ShoggothBiomassBlockEntity>)
            (tickerLevel, pos, tickerState, biomass) ->
                ShoggothBiomassBlockEntity.serverTick((ServerLevel) tickerLevel, pos, tickerState, biomass);
    }
}