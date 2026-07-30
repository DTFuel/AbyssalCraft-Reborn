package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.EnergyTier;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

/** Single-slot collector that feeds PE into the displayed energy item. */
public class EnergyPedestalBlock extends TieredEnergyBlock {

    public static final BooleanProperty TILTED = BooleanProperty.create("tilted");
    private static final VoxelShape SHAPE = box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public EnergyPedestalBlock(BlockBehaviour.Properties properties, EnergyTier tier) {
        super(properties, tier);
        registerDefaultState(stateDefinition.any().setValue(TILTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(TILTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int rotation = Mth.floor(context.getRotation() * 16.0F / 360.0F + 0.5D) & 15;
        return defaultBlockState().setValue(TILTED, rotation % 4 != 0);
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
    protected InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player) {
        return exchangeDisplayedItem(ItemStack.EMPTY, level, pos, player);
    }

    @Override
    protected InteractionResult onUseAcceptedItem(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                  Player player, InteractionHand hand) {
        return exchangeDisplayedItem(stack, level, pos, player);
    }

    private InteractionResult exchangeDisplayedItem(ItemStack held, Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof EnergyPedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }
        ItemStack stored = pedestal.getStoredItem();
        return switch (displayedItemAction(!stored.isEmpty(), !held.isEmpty())) {
            case TAKE -> {
                if (!level.isClientSide) {
                    pedestal.setStoredItem(ItemStack.EMPTY);
                    if (!player.addItem(stored)) player.drop(stored, false);
                }
                yield InteractionResult.sidedSuccess(level.isClientSide);
            }
            case STORE -> {
                if (!level.isClientSide) {
                    pedestal.setStoredItem(held.copyWithCount(1));
                    if (!player.getAbilities().instabuild) held.shrink(1);
                }
                yield InteractionResult.sidedSuccess(level.isClientSide);
            }
            case NONE -> InteractionResult.PASS;
        };
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