package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Block item that restores the legacy {@code PotEnergy} value after placement. */
public final class EnergyBlockItem extends BlockItem {

    public static final String ENERGY_KEY = "PotEnergy";

    public EnergyBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player,
                                                   ItemStack stack, BlockState state) {
        boolean updated = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof IEnergyContainer container) {
            container.setEnergy(ItemDataCompat.getFloat(stack, ENERGY_KEY));
            return true;
        }
        return updated;
    }
}