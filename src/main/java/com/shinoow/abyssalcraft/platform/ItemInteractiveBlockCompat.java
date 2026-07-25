package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if >=1.21 {
/*import net.minecraft.world.ItemInteractionResult;
*///?}
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Compat base for blocks whose held-item right-click behavior differs between 1.20 and 1.21. */
@SuppressWarnings("deprecation")
public abstract class ItemInteractiveBlockCompat extends Block {

    protected ItemInteractiveBlockCompat(BlockBehaviour.Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    /*@Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        return onUseItem(stack, state, level, pos, player, hand)
            ? ItemInteractionResult.sidedSuccess(level.isClientSide)
            : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    *///?} else {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        return onUseItem(player.getItemInHand(hand), state, level, pos, player, hand)
            ? InteractionResult.sidedSuccess(level.isClientSide)
            : InteractionResult.PASS;
    }
    //?}

    protected abstract boolean onUseItem(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                         Player player, InteractionHand hand);
}