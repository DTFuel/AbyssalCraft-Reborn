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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

/** Single-slot collector that feeds PE into the displayed energy item. */
public class EnergyPedestalBlock extends TieredEnergyBlock {

    private static final VoxelShape SHAPE = box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public EnergyPedestalBlock(BlockBehaviour.Properties properties, EnergyTier tier) {
        super(properties, tier);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyPedestalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide || type != EnergyBlocks.ENERGY_PEDESTAL_BE.get()
            ? null
            : TickingBlockEntity.serverTicker();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()
            && level.getBlockEntity(pos) instanceof EnergyPedestalBlockEntity pedestal) {
            Containers.dropContents(level, pos, pedestal);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean handleEmptyHandUse(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof EnergyPedestalBlockEntity pedestal)) {
            return false;
        }
        ItemStack stack = pedestal.removeItemNoUpdate(0);
        if (stack.isEmpty()) {
            return false;
        }
        pedestal.setChanged();
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
        return true;
    }
}