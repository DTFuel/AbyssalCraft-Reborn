package com.shinoow.abyssalcraft.content.block.ritual;

import com.shinoow.abyssalcraft.platform.InteractiveBlockCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ritual pedestal block (owned by content/block/ritual): a stand that holds one offering for a nearby
 * {@link RitualAltarBlock}. Right-clicking with an item places one on the pedestal; right-clicking it
 * again (empty hand) takes it back. Interaction fork lives in {@link InteractiveBlockCompat};
 * {@code EntityBlock} is vanilla-shared, so this block carries no {@code //?}.
 */
public class RitualPedestalBlock extends InteractiveBlockCompat implements EntityBlock {

    public RitualPedestalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualPedestalBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player) {
        return exchangeOffering(ItemStack.EMPTY, level, pos, player);
    }

    @Override
    protected InteractionResult onUseAcceptedItem(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                  Player player, InteractionHand hand) {
        return exchangeOffering(stack, level, pos, player);
    }

    private InteractionResult exchangeOffering(ItemStack held, Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof RitualPedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }
        ItemStack current = pedestal.getOffering();
        return switch (displayedItemAction(!current.isEmpty(), !held.isEmpty())) {
            case TAKE -> {
                if (!level.isClientSide) {
                    ItemStack removed = pedestal.removeOffering();
                    if (!player.addItem(removed)) player.drop(removed, false);
                }
                yield InteractionResult.sidedSuccess(level.isClientSide);
            }
            case STORE -> {
                if (!level.isClientSide) {
                    pedestal.setOffering(held.copyWithCount(1));
                    if (!player.getAbilities().instabuild) held.shrink(1);
                }
                yield InteractionResult.sidedSuccess(level.isClientSide);
            }
            case NONE -> InteractionResult.PASS;
        };
    }

    @Override
    protected boolean acceptsHeldItem() {
        return true;
    }

    @Override
    protected boolean skipsInteractionWhileSneaking() {
        return true;
    }
}
