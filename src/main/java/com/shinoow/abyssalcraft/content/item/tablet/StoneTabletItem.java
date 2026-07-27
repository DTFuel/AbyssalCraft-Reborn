package com.shinoow.abyssalcraft.content.item.tablet;

import java.util.List;

import com.shinoow.abyssalcraft.platform.TooltipCompat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Portable inventory payload consumed by the State Transformer. */
public final class StoneTabletItem extends TooltipCompat {

    public StoneTabletItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return StoneTabletStorage.hasInventory(stack) || super.isFoil(stack);
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        if (StoneTabletStorage.isCursed(stack)) {
            tooltip.add(Component.translatable("tooltip.abyssalcraft.stone_tablet.cursed")
                .withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (StoneTabletStorage.hasInventory(stack)) {
            tooltip.add(Component.translatable("tooltip.abyssalcraft.stone_tablet.energy",
                (int) StoneTabletStorage.potentialEnergy(stack)).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.abyssalcraft.stone_tablet.contents",
                StoneTabletStorage.storedStacks(stack)).withStyle(ChatFormatting.GRAY));
        }
    }
}