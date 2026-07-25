package com.shinoow.abyssalcraft.content.machine.researchtable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.platform.InteractiveBlockCompat;
import com.shinoow.abyssalcraft.platform.MenuCompat;

/**
 * Research Table machine block (owned by PC-8, Stage C2a).
 *
 * <p>Holds a {@link ResearchTableBlockEntity} and opens its (inventory-only) menu on an empty-hand
 * right click. No ticker -- the research table does not process over time. Interaction / menu-opening
 * forks live in {@link InteractiveBlockCompat} / {@link MenuCompat}, so this block carries no
 * {@code //?}.
 */
public class ResearchTableBlock extends InteractiveBlockCompat implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0.8, 0.0, 0.8, 15.2, 14.4, 15.2);

    public ResearchTableBlock(BlockBehaviour.Properties properties) {
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!ACConfig.particleBlock.get()) {
            return;
        }
        double[][] offsets = switch (state.getValue(FACING)) {
            case NORTH -> new double[][] {{0.75, 1.05, 0.80}, {0.75, 1.00, 0.68}, {0.65, 0.95, 0.80}};
            case SOUTH -> new double[][] {{0.25, 1.05, 0.20}, {0.25, 1.00, 0.32}, {0.35, 0.95, 0.20}};
            case EAST -> new double[][] {{0.20, 1.05, 0.75}, {0.30, 1.00, 0.77}, {0.20, 0.95, 0.65}};
            case WEST -> new double[][] {{0.80, 1.05, 0.25}, {0.70, 1.00, 0.20}, {0.80, 0.95, 0.35}};
            default -> new double[0][];
        };
        for (double[] offset : offsets) {
            double x = pos.getX() + offset[0];
            double y = pos.getY() + offset[1];
            double z = pos.getZ() + offset[2];
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchTableBlockEntity(pos, state);
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
