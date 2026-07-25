package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.EnergyTier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Two-slot PE buffer: input items charge the block and output items are charged by it. */
public class EnergyContainerBlock extends TieredEnergyBlock {

    public EnergyContainerBlock(BlockBehaviour.Properties properties, EnergyTier tier) {
        super(properties, tier);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyContainerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide || type != EnergyBlocks.ENERGY_CONTAINER_BE.get()
            ? null
            : TickingBlockEntity.serverTicker();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()
            && level.getBlockEntity(pos) instanceof EnergyContainerBlockEntity container) {
            Containers.dropContents(level, pos, container);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean handleEmptyHandUse(Level level, BlockPos pos, Player player) {
        if (!player.isShiftKeyDown()
            || !(level.getBlockEntity(pos) instanceof EnergyContainerBlockEntity container)) {
            return false;
        }
        int slot = container.getItem(1).isEmpty() ? 0 : 1;
        ItemStack stack = container.removeItemNoUpdate(slot);
        if (stack.isEmpty()) {
            return false;
        }
        container.setChanged();
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
        return true;
    }
}