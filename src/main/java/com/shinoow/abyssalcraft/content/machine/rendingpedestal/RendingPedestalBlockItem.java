package com.shinoow.abyssalcraft.content.machine.rendingpedestal;

import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.system.rending.RendingEnergyType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class RendingPedestalBlockItem extends BlockItem {

    public RendingPedestalBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player,
                                                   ItemStack stack, BlockState state) {
        boolean updated = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide
            && level.getBlockEntity(pos) instanceof RendingPedestalBlockEntity pedestal) {
            pedestal.setEnergy(ItemDataCompat.getFloat(stack, "PotEnergy"));
            for (RendingEnergyType type : RendingEnergyType.values()) {
                pedestal.setRendingEnergy(type, ItemDataCompat.getInt(stack, type.dataKey(), 0));
            }
            return true;
        }
        return updated;
    }
}