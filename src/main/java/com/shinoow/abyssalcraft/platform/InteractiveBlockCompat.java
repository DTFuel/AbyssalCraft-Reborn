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
        if (skipsInteractionWhileSneaking() && player.isShiftKeyDown()) return InteractionResult.CONSUME;
        return onUse(state, level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (skipsInteractionWhileSneaking() && player.isShiftKeyDown()) {
            return ItemInteractionResult.CONSUME;
        }
        InteractionResult itemResult = onUseItem(stack, state, level, pos, player, hand);
        if (itemResult.consumesAction()) return ItemInteractionResult.sidedSuccess(level.isClientSide);
        if (!invokesBlockForHeldItem(false, acceptsHeldItem())) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return onUseAcceptedItem(stack, state, level, pos, player, hand).consumesAction()
            ? ItemInteractionResult.sidedSuccess(level.isClientSide)
            : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    *///?} else {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (skipsInteractionWhileSneaking() && player.isShiftKeyDown()) return InteractionResult.CONSUME;
        InteractionResult itemResult = onUseItem(player.getItemInHand(hand), state, level, pos, player, hand);
        if (itemResult.consumesAction()) return itemResult;
        if (!player.getItemInHand(hand).isEmpty()
            && !invokesBlockForHeldItem(false, acceptsHeldItem())) {
            return InteractionResult.PASS;
        }
        return player.getItemInHand(hand).isEmpty()
            ? onUse(state, level, pos, player)
            : onUseAcceptedItem(player.getItemInHand(hand), state, level, pos, player, hand);
    }
    //?}

    /** Version-neutral empty-hand interaction. */
    protected abstract InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player);

    /** Optional held-item command handled before the generic block interaction. */
    protected InteractionResult onUseItem(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /** Generic block interaction for a held stack accepted by this block. */
    protected InteractionResult onUseAcceptedItem(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                  Player player, InteractionHand hand) {
        return onUse(state, level, pos, player);
    }

    /** Whether this block's {@link #onUse} also handles a held stack. */
    protected boolean acceptsHeldItem() {
        return false;
    }

    /** Whether sneaking bypasses this block's interaction without running subclass logic. */
    protected boolean skipsInteractionWhileSneaking() {
        return false;
    }

    public final boolean bypassesInteractionWhileSneaking() {
        return skipsInteractionWhileSneaking();
    }

    public static boolean invokesBlockForHeldItem(boolean itemCommandConsumed, boolean acceptsHeldItem) {
        return !itemCommandConsumed && acceptsHeldItem;
    }

    public static boolean bypassesBlockInteraction(boolean sneaking, boolean skipsWhileSneaking) {
        return sneaking && skipsWhileSneaking;
    }

    public static DisplayedItemAction displayedItemAction(boolean hasStoredItem, boolean hasHeldItem) {
        if (hasStoredItem) return DisplayedItemAction.TAKE;
        return hasHeldItem ? DisplayedItemAction.STORE : DisplayedItemAction.NONE;
    }

    public enum DisplayedItemAction {
        TAKE,
        STORE,
        NONE
    }
}
