package com.shinoow.abyssalcraft.content.item.ritual;

import com.shinoow.abyssalcraft.platform.TooltipCompat;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Minimal faithful PE storage shared by the cage and tiered Staff of Rending items. */
public class RitualEnergyItem extends TooltipCompat implements IEnergyContainerItem {

    private final int maxEnergy;

    public RitualEnergyItem(int maxEnergy) {
        super(new Item.Properties().stacksTo(1));
        this.maxEnergy = maxEnergy;
    }

    @Override
    public int getMaxEnergy(ItemStack stack) {
        return maxEnergy;
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.energy", (int) getContainedEnergy(stack), maxEnergy));
    }
}