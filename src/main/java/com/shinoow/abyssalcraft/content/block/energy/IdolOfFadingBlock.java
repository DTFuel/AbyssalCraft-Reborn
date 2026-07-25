package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** PE-powered idol that calls shadow creatures from darkness. */
public final class IdolOfFadingBlock extends EnergyDropBlock implements EntityBlock {

    private static final VoxelShape SHAPE = box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public IdolOfFadingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected net.minecraft.world.InteractionResult onUse(BlockState state, Level level, BlockPos pos,
                                                            net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof IdolOfFadingBlockEntity idol) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.abyssalcraft.energy.status", (int) idol.getContainedEnergy(), idol.getMaxEnergy()), true);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IdolOfFadingBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide || type != EnergyBlocks.IDOL_OF_FADING_BE.get()
            ? null
            : TickingBlockEntity.serverTicker();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}