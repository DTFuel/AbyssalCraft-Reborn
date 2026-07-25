package com.shinoow.abyssalcraft.platform;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Compat: {@code Item.appendHoverText} (vanilla axis).
 *
 * <p>1.21 changed the 2nd parameter from {@code Level} to {@code Item.TooltipContext}. Items that
 * need custom tooltips extend this and implement the version-neutral {@link #appendTooltip}.
 */
public abstract class TooltipCompat extends Item {

    protected TooltipCompat(Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    /*@Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendTooltip(stack, tooltip, flag);
    }
    *///?} else {
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        appendTooltip(stack, tooltip, flag);
    }
    //?}

    /** Add custom tooltip lines (version-neutral). */
    protected abstract void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag);
}
