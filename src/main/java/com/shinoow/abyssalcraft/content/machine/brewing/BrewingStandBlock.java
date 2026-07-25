package com.shinoow.abyssalcraft.content.machine.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.platform.InteractiveBlockCompat;
import com.shinoow.abyssalcraft.platform.MenuCompat;

/**
 * Sequential Brewing Stand machine block (owned by PC-8, Stage C2a).
 *
 * <p>Directional ({@link #FACING} = the direction brewed potions are pushed to the next stand), holds
 * a {@link BrewingStandBlockEntity}, ticks it server-side (via the PC-1
 * {@link TickingBlockEntity#serverTicker()}) and opens its menu on an empty-hand right click. Fork-free
 * -- interaction/menu opening live in {@link InteractiveBlockCompat} / {@link MenuCompat}.
 */
public class BrewingStandBlock extends InteractiveBlockCompat implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BrewingStandBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrewingStandBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != BrewingStands.BREWING_STAND_BE.get()) {
            return null;
        }
        return TickingBlockEntity.serverTicker();
    }

    @Override
    protected InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof MenuProvider provider && player instanceof ServerPlayer serverPlayer) {
                MenuCompat.open(serverPlayer, provider, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
