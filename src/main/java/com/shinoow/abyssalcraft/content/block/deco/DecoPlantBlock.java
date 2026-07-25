package com.shinoow.abyssalcraft.content.block.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class DecoPlantBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);
    private final boolean thorn;

    public DecoPlantBlock(Properties properties, boolean thorn) {
        super(properties);
        this.thorn = thorn;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState ground = level.getBlockState(pos.below());
        return ground.is(DecoBlocks.ABYSSAL_SAND.get())
            || ground.is(DecoBlocks.FUSED_ABYSSAL_SAND.get())
            || ground.is(Blocks.GRASS_BLOCK)
            || ground.is(Blocks.DIRT)
            || ground.is(Blocks.COARSE_DIRT)
            || ground.is(Blocks.PODZOL)
            || ground.is(Blocks.FARMLAND);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (thorn && entity instanceof Player player
                && shouldDamage(true, player.getItemBySlot(EquipmentSlot.FEET).isEmpty(),
                    player.getItemBySlot(EquipmentSlot.LEGS).isEmpty())) {
            player.hurt(level.damageSources().cactus(), 1.0F);
        }
    }

    static boolean shouldDamage(boolean thorn, boolean feetEmpty, boolean legsEmpty) {
        return thorn && feetEmpty && legsEmpty;
    }
}