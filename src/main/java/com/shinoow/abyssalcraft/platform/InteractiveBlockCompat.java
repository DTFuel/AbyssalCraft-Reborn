package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
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

/**
 * Compat base for blocks that react to an empty-hand right click (vanilla axis).
 *
 * <p>1.21 renamed {@code use(...,InteractionHand,BlockHitResult)} (public) to
 * {@code useWithoutItem(...,BlockHitResult)} (protected, no hand) and split item interactions into
 * {@code useItemOn}. Subclasses implement the version-neutral {@link #onUse}; this class maps it onto
 * whichever override the active version needs so machine blocks stay fork-free.
 */
// 1.20.1 marks BlockBehaviour.use as deprecated-for-override (a "don't call directly" signal); the
// override here is intentional, so the deprecation warning is suppressed.
@SuppressWarnings("deprecation")
public abstract class InteractiveBlockCompat extends Block {

    protected InteractiveBlockCompat(BlockBehaviour.Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    /*@Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return onUse(state, level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult itemResult = onUseItem(stack, state, level, pos, player, hand);
        if (itemResult.consumesAction()) return ItemInteractionResult.sidedSuccess(level.isClientSide);
        if (!acceptsHeldItem()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return onUse(state, level, pos, player).consumesAction()
            ? ItemInteractionResult.sidedSuccess(level.isClientSide)
            : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    *///?} else {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult itemResult = onUseItem(player.getItemInHand(hand), state, level, pos, player, hand);
        if (itemResult.consumesAction()) return itemResult;
        if (!player.getItemInHand(hand).isEmpty() && !acceptsHeldItem()) {
            return InteractionResult.PASS;
        }
        return onUse(state, level, pos, player);
    }
    //?}

    /** Version-neutral empty-hand interaction. */
    protected abstract InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player);

    /** Optional held-item command handled before the generic block interaction. */
    protected InteractionResult onUseItem(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /** Whether this block's {@link #onUse} also handles a held stack. */
    protected boolean acceptsHeldItem() {
        return false;
    }
}
