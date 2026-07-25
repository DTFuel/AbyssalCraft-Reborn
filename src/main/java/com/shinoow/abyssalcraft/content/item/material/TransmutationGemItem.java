package com.shinoow.abyssalcraft.content.item.material;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.shinoow.abyssalcraft.content.item.MachineRemainderItem;

public final class TransmutationGemItem extends Item implements MachineRemainderItem {

    public TransmutationGemItem() {
        super(new Properties().durability(10));
    }

    @Override
    public ItemStack machineRemainder(ItemStack consumed) {
        if (consumed.getDamageValue() >= consumed.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = consumed.copyWithCount(1);
        remainder.setDamageValue(consumed.getDamageValue() + 1);
        return remainder;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.AQUA);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}