package com.shinoow.abyssalcraft.content.item.misc;

import java.util.function.BiConsumer;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Food whose post-consumption effects are defined by the legacy item variant. */
public final class EffectFoodItem extends Item {

    private final BiConsumer<Level, LivingEntity> effects;

    public EffectFoodItem(Properties properties, BiConsumer<Level, LivingEntity> effects) {
        super(properties);
        this.effects = effects;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        ItemStack result = super.finishUsingItem(stack, level, consumer);
        if (!level.isClientSide) effects.accept(level, consumer);
        return result;
    }
}