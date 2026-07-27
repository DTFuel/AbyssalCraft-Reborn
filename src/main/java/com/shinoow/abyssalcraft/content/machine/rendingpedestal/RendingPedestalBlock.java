package com.shinoow.abyssalcraft.content.machine.rendingpedestal;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.energy.EnergyDropBlock;
import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.system.rending.RendingEnergyType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RendingPedestalBlock extends EnergyDropBlock implements EntityBlock {

    public static final BooleanProperty TILTED = BooleanProperty.create("tilted");
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public RendingPedestalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(TILTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TILTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int rotation = Mth.floor(context.getRotation() * 16.0F / 360.0F + 0.5D) & 15;
        return defaultBlockState().setValue(TILTED, rotation % 4 != 0);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RendingPedestalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide || type != RendingPedestals.RENDING_PEDESTAL_BE.get()
            ? null : TickingBlockEntity.serverTicker();
    }

    @Override
    protected InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof MenuProvider provider) {
            MenuCompat.open(serverPlayer, provider, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean acceptsHeldItem() {
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        Object blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        return List.of(blockEntity instanceof RendingPedestalBlockEntity pedestal
            ? stackWithState(state, pedestal) : new ItemStack(state.getBlock()));
    }

    public static ItemStack stackWithState(BlockState state, RendingPedestalBlockEntity pedestal) {
        ItemStack stack = new ItemStack(state.getBlock());
        if (pedestal.getContainedEnergy() > 0) {
            ItemDataCompat.putFloat(stack, "PotEnergy", pedestal.getContainedEnergy());
        }
        for (RendingEnergyType type : RendingEnergyType.values()) {
            int stored = pedestal.getRendingEnergy(type);
            if (stored > 0) ItemDataCompat.putInt(stack, type.dataKey(), stored);
        }
        return stack;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
            && level.getBlockEntity(pos) instanceof RendingPedestalBlockEntity pedestal) {
            Containers.dropContents(level, pos, pedestal);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}