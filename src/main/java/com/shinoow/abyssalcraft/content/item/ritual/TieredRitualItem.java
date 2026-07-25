package com.shinoow.abyssalcraft.content.item.ritual;

import java.util.List;

import com.shinoow.abyssalcraft.platform.TooltipCompat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** A plain ritual progression item carrying the legacy tier tooltip. */
public class TieredRitualItem extends TooltipCompat {

    private final int tier;

    public TieredRitualItem(int tier) {
        super(new Item.Properties().stacksTo(1));
        this.tier = tier;
    }

    public int tier() {
        return tier;
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tiereditem.tier", tier));
    }
}