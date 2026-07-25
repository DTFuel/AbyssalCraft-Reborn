package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** PE manipulator that distributes stored energy to nearby collectors. */
public class EnergyDepositionerBlock extends EnergyDropBlock implements EntityBlock {

    public EnergyDepositionerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected net.minecraft.world.InteractionResult onUse(BlockState state, Level level, BlockPos pos,
                                                            net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof EnergyDepositionerBlockEntity depositioner) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.abyssalcraft.energy.status", (int) depositioner.getContainedEnergy(),
                depositioner.getMaxEnergy()), true);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyDepositionerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide || type != EnergyBlocks.ENERGY_DEPOSITIONER_BE.get()
            ? null
            : TickingBlockEntity.serverTicker();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()
            && level.getBlockEntity(pos) instanceof EnergyDepositionerBlockEntity depositioner) {
            Containers.dropContents(level, pos, depositioner);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}