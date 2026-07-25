package com.shinoow.abyssalcraft.content.block.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The narrow, nearly indestructible powerstone used by the gateway-key progression. */
public final class DreadlandsPowerstoneBlock extends Block {

    private static final VoxelShape SHAPE = box(3.2D, 0.0D, 3.2D, 12.8D, 16.0D, 12.8D);

    public DreadlandsPowerstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                pos.getX() + random.nextDouble(), pos.getY() + 1.1D, pos.getZ() + random.nextDouble(),
                0.0D, 0.0D, 0.0D);
        }
    }
}