package com.shinoow.abyssalcraft.content.block.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
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

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.platform.InteractiveBlockCompat;

public final class SpiritAltarBlock extends InteractiveBlockCompat implements EntityBlock {

    private static final VoxelShape SHAPE = box(2.4, 0.0, 2.4, 13.6, 10.4, 13.6);

    public SpiritAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiritAltarBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof SpiritAltarBlockEntity altar && altar.isEnabled()) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.7,
                pos.getZ() + 0.5, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.7,
                pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide || type != com.shinoow.abyssalcraft.content.item.transfer.TransferContent.SPIRIT_ALTAR_BE.get()) {
            return null;
        }
        return TickingBlockEntity.serverTicker();
    }

    @Override
    protected net.minecraft.world.InteractionResult onUse(BlockState state, Level level, BlockPos pos,
                                                            net.minecraft.world.entity.player.Player player) {
        return net.minecraft.world.InteractionResult.PASS;
    }
}