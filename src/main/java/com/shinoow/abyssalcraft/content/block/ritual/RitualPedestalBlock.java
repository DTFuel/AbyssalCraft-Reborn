package com.shinoow.abyssalcraft.content.block.ritual;

import com.shinoow.abyssalcraft.platform.InteractiveBlockCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
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
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RitualPedestalBlockEntity pedestal) {
            ItemStack current = pedestal.getOffering();
            ItemStack held = player.getMainHandItem();
            if (current.isEmpty() && !held.isEmpty()) {
                pedestal.setOffering(held.copyWithCount(1));
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            } else if (!current.isEmpty()) {
                ItemStack removed = current.copy();
                pedestal.consumeOffering();
                if (!player.addItem(removed)) {
                    player.drop(removed, false);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean acceptsHeldItem() {
        return true;
    }
}
