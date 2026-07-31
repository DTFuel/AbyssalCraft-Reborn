package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.EnergyTier;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Five-tier life-force collector restored from the 1.12.2 Sacrificial Altar. */
public final class SacrificialAltarBlock extends TieredEnergyBlock {

    private static final VoxelShape SHAPE = box(2.4D, 0.0D, 2.4D, 13.6D, 14.4D, 13.6D);

    public SacrificialAltarBlock(BlockBehaviour.Properties properties, EnergyTier tier) {
        super(properties, tier);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SacrificialAltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide || type != EnergyBlocks.SACRIFICIAL_ALTAR_BE.get()
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
                && level.getBlockEntity(pos) instanceof SacrificialAltarBlockEntity altar) {
            Containers.dropContents(level, pos, altar);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player) {
        return exchangeItem(ItemStack.EMPTY, level, pos, player);
    }

    @Override
    protected InteractionResult onUseAcceptedItem(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                  Player player, InteractionHand hand) {
        return exchangeItem(stack, level, pos, player);
    }

    private InteractionResult exchangeItem(ItemStack held, Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof SacrificialAltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }
        ItemStack stored = altar.getStoredItem();
        if (!stored.isEmpty()) {
            if (!level.isClientSide) {
                altar.setStoredItem(ItemStack.EMPTY);
                if (!player.addItem(stored)) player.drop(stored, false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.getItem() instanceof IEnergyContainerItem) {
            if (!level.isClientSide) {
                altar.setStoredItem(held.copyWithCount(1));
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.abyssalcraft.energy.status", (int) altar.getContainedEnergy(), altar.getMaxEnergy()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean acceptsHeldItem() {
        return true;
    }

    @Override
    protected boolean skipsInteractionWhileSneaking() {
        return true;
    }
}
