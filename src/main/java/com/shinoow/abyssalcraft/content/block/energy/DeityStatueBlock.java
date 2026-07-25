package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Deity statue block (pilot PE source, owned by content/block/energy). Holds a ticking
 * {@link DeityStatueBlockEntity} that charges nearby players' energy items (the faithful 1.12.2 statue
 * worship). The {@link DeityType} is stored for the deferred deity-filtered amplifiers / disruptions
 * (PS-5b); the pilot is always-charging (no activation ceremony). {@code EntityBlock} is vanilla-shared,
 * so this block carries no {@code //?} fork.
 */
public class DeityStatueBlock extends Block implements EntityBlock {

    private final DeityType deity;

    public DeityStatueBlock(BlockBehaviour.Properties properties, DeityType deity) {
        super(properties);
        this.deity = deity;
        registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    /** The deity this statue channels (used by the deferred deity-filtered amplifiers / disruptions). */
    public DeityType deity() {
        return deity;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeityStatueBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
            context.getHorizontalDirection().getOpposite());
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != EnergyBlocks.DEITY_STATUE_BE.get()) {
            return null;
        }
        return TickingBlockEntity.serverTicker();
    }
}
