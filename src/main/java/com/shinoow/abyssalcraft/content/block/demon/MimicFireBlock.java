package com.shinoow.abyssalcraft.content.block.demon;

import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.FireMessage;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Faster-spreading legacy demon fire, kept distinct so players can extinguish it remotely. */
public final class MimicFireBlock extends FireBlock {

    public MimicFireBlock(Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    /*@Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        return extinguish(level, pos, player, InteractionHand.MAIN_HAND);
    }
    *///?} else {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        return extinguish(level, pos, player, hand);
    }
    //?}

    private static InteractionResult extinguish(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            ACNetwork.sendToServer(new FireMessage(pos));
            player.swing(hand);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos) || level.isRainingAt(pos)) {
            level.removeBlock(pos, false);
            return;
        }
        if (random.nextFloat() < 0.2F) {
            super.tick(state, level, pos, random);
        }
        if (level.getBlockState(pos).is(this)) {
            level.scheduleTick(pos, this, 1 + random.nextInt(10));
        }
    }
}