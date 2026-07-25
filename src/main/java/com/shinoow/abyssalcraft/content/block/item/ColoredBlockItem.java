package com.shinoow.abyssalcraft.content.block.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ColoredBlockItem extends BlockItem {

    private final ChatFormatting color;

    public ColoredBlockItem(Block block, Properties properties, ChatFormatting color) {
        super(block, properties);
        this.color = color;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(color);
    }
}