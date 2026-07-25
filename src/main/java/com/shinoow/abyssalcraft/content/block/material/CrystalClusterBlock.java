package com.shinoow.abyssalcraft.content.block.material;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CrystalClusterBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(3.2, 0.0, 3.2, 12.8, 11.2, 12.8);
    private final String element;

    public CrystalClusterBlock(BlockBehaviour.Properties properties, String element) {
        super(properties);
        this.element = element;
    }

    @Override
    public MutableComponent getName() {
        String crystalKey = "item.abyssalcraft.crystal_" + Character.toUpperCase(element.charAt(0)) + element.substring(1);
        return Component.translatable("block.abyssalcraft.crystal_cluster", Component.translatable(crystalKey));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}