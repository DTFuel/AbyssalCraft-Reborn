package com.shinoow.abyssalcraft.content.block.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Low portal pedestal. The Silver Key is its only activation interaction. */
public final class PortalAnchorBlock extends Block implements EntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE = box(2.4D, 0.0D, 2.4D, 13.6D, 8.0D, 13.6D);

    private final boolean unchained;

    public PortalAnchorBlock(BlockBehaviour.Properties properties, boolean unchained) {
        super(properties);
        this.unchained = unchained;
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    public boolean isUnchained() {
        return unchained;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortalAnchorBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide || type != PortalBlocks.PORTAL_ANCHOR_BE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<PortalAnchorBlockEntity>)
            PortalAnchorBlockEntity::serverTick;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()
            && level.getBlockEntity(pos) instanceof PortalAnchorBlockEntity anchor) {
            anchor.removePortal(false);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}