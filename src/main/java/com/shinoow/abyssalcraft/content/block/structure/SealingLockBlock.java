package com.shinoow.abyssalcraft.content.block.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.shinoow.abyssalcraft.content.item.ritual.RitualItems;
import com.shinoow.abyssalcraft.platform.InteractiveBlockCompat;

/** Structure lock unlocked by the Sealing Key, preserving the legacy one-way interaction. */
public final class SealingLockBlock extends InteractiveBlockCompat implements EntityBlock {

    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");
    private static final VoxelShape SHAPE = box(3.2D, 0.0D, 3.2D, 12.8D, 15.2D, 12.8D);

    public SealingLockBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(LOCKED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LOCKED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(LOCKED) ? SHAPE : net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SealingLockBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player) {
        if (!state.getValue(LOCKED)) return InteractionResult.PASS;
        ItemStack held = player.getMainHandItem();
        if (!held.is(RitualItems.SEALING_KEY.get())) return InteractionResult.PASS;
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SealingLockBlockEntity lock) {
            lock.unlock();
            level.setBlock(pos, state.setValue(LOCKED, false), 3);
            if (!player.getAbilities().instabuild) held.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}