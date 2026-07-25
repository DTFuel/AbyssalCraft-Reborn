package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Drain ten percent from the first charged PE item in each nearby player's main inventory. */
public final class PotentialEnergyDisruption extends Disruption {

    public PotentialEnergyDisruption() {
        super("potentialEnergy", null);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (level.isClientSide) {
            return;
        }
        for (Player player : players) {
            int mainInventorySize = Math.min(36, player.getInventory().getContainerSize());
            for (int slot = 0; slot < mainInventorySize; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.getItem() instanceof IEnergyContainerItem energyItem) {
                    float contained = energyItem.getContainedEnergy(stack);
                    if (contained > 0) {
                        energyItem.consumeEnergy(stack, contained / 10.0F);
                        player.hurt(level.damageSources().magic(), 2.0F);
                        break;
                    }
                }
            }
        }
    }
}