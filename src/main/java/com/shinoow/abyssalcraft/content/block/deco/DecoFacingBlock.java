package com.shinoow.abyssalcraft.content.block.deco;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Horizontally-facing decorative block (owned by PB-5).
 *
 * <p>Shared base for the decorative statues, murals and tombstones: a block carrying the vanilla
 * horizontal {@link HorizontalDirectionalBlock#FACING} property, placed facing away from the player
 * (matching the 1.12.2 behaviour). It extends the concrete {@link Block} (rather than
 * {@code HorizontalDirectionalBlock}) on purpose: {@code Block} supplies a concrete {@code codec()},
 * which 1.21 requires of every non-abstract block, so this stays fork-free across both loader nodes.
 * Refined per-block collision/visual shapes and {@code rotate}/{@code mirror} ship with the models in
 * the asset stage (PK); this class only guarantees the correct {@code facing} state.
 */
public class DecoFacingBlock extends Block {

    public enum ShapeKind { STATUE, MURAL, TOMBSTONE }

    private static final VoxelShape STATUE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape TOMBSTONE_NS = Block.box(1.6, 0.0, 4.8, 14.4, 16.0, 11.2);
    private static final VoxelShape TOMBSTONE_EW = Block.box(4.8, 0.0, 1.6, 11.2, 16.0, 14.4);
    private static final VoxelShape MURAL_NORTH = Block.box(0.0, 0.0, 12.8, 16.0, 16.0, 16.0);
    private static final VoxelShape MURAL_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.2);
    private static final VoxelShape MURAL_EAST = Block.box(0.0, 0.0, 0.0, 3.2, 16.0, 16.0);
    private static final VoxelShape MURAL_WEST = Block.box(12.8, 0.0, 0.0, 16.0, 16.0, 16.0);

    private final ShapeKind shapeKind;

    public DecoFacingBlock(Properties properties, ShapeKind shapeKind) {
        super(properties);
        this.shapeKind = shapeKind;
        registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        if (shapeKind == ShapeKind.STATUE) return STATUE;
        if (shapeKind == ShapeKind.TOMBSTONE) {
            return facing.getAxis() == Direction.Axis.X ? TOMBSTONE_EW : TOMBSTONE_NS;
        }
        return switch (facing) {
            case NORTH -> MURAL_NORTH;
            case SOUTH -> MURAL_SOUTH;
            case EAST -> MURAL_EAST;
            case WEST -> MURAL_WEST;
            default -> MURAL_NORTH;
        };
    }
}
