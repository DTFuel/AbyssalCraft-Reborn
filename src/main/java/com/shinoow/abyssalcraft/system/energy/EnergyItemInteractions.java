package com.shinoow.abyssalcraft.system.energy;

import com.shinoow.abyssalcraft.content.block.energy.EnergyContainerBlockEntity;
import com.shinoow.abyssalcraft.content.block.energy.EnergyPedestalBlockEntity;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Shared placement interaction for the existing PE transporter items. */
public final class EnergyItemInteractions {

    private EnergyItemInteractions() {}

    public static InteractionResult placeInEnergyBlock(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        int slot;
        if (blockEntity instanceof EnergyPedestalBlockEntity pedestal) {
            if (!pedestal.getStoredItem().isEmpty()) {
                return InteractionResult.PASS;
            }
            slot = 0;
        } else if (blockEntity instanceof EnergyContainerBlockEntity container) {
            slot = context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? 1 : 0;
            if (!container.getItem(slot).isEmpty()) {
                return InteractionResult.PASS;
            }
        } else {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide) {
            ItemStack held = context.getItemInHand();
            ItemStack placed = held.copyWithCount(1);
            if (blockEntity instanceof EnergyPedestalBlockEntity pedestal) {
                pedestal.setStoredItem(placed);
            } else if (blockEntity instanceof EnergyContainerBlockEntity container) {
                container.setItem(slot, placed);
            }
            Player player = context.getPlayer();
            if (player == null || !player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}