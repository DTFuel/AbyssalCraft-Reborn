package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.IEnergyAmplifier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Range-amplifying pillar used by deity statues and the Basic Place of Power. */
public final class MonolithPillarBlock extends Block implements IEnergyAmplifier {

    private static final VoxelShape SHAPE = box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public MonolithPillarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public AmplifierType getAmplifierType() {
        return AmplifierType.RANGE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}