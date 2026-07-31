package com.shinoow.abyssalcraft.content.item.weapon;

import java.util.List;

import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Sacthoth's blade, growing stronger as it harvests up to one thousand souls. */
public final class SoulReaperItem extends SwordItem {

    private static final String SOULS = "souls";
    private static final int MAX_SOULS = 1000;

    public SoulReaperItem(Tier tier, Item.Properties properties) {
        //? if >=1.21 {
        /*super(tier, properties.attributes(SwordItem.createAttributes(tier, 3, -2.4F)));
        *///?} else {
        super(tier, 3, -2.4F, properties);
        //?}
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && selected && entity instanceof LivingEntity holder) {
            applySoulEffects(holder, souls(stack));
        }
    }

    //? if >=1.21 {
    /*@Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        harvestSoul(stack, target);
    }
    *///?} else {
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        harvestSoul(stack, target);
        return result;
    }
    //?}

    //? if >=1.21 {
    /*@Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendSoulTooltip(stack, tooltip);
    }
    *///?} else {
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        appendSoulTooltip(stack, tooltip);
    }
    //?}

    public static int souls(ItemStack stack) {
        return Math.min(MAX_SOULS, Math.max(0, ItemDataCompat.getInt(stack, SOULS, 0)));
    }

    private static void harvestSoul(ItemStack stack, LivingEntity target) {
        int souls = souls(stack);
        if (!target.isAlive() && souls < MAX_SOULS) {
            ItemDataCompat.putInt(stack, SOULS, souls + 1);
        }
    }

    private static void appendSoulTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.abyssalcraft.soulreaper", souls(stack), MAX_SOULS));
    }

    private static void applySoulEffects(LivingEntity holder, int souls) {
        if (souls < 60) return;

        int strength = souls >= 500 ? 2 : souls >= 125 ? 1 : 0;
        holder.addEffect(MobEffectCompat.vanillaEffect(MobEffects.DAMAGE_BOOST, 20, strength));
        if (souls >= 125) {
            int speed = souls >= 1000 ? 2 : souls >= 500 ? 1 : 0;
            holder.addEffect(MobEffectCompat.vanillaEffect(MobEffects.MOVEMENT_SPEED, 20, speed));
        }
        if (souls >= 250) {
            holder.addEffect(MobEffectCompat.vanillaEffect(MobEffects.DAMAGE_RESISTANCE, 20, 0));
        }
        if (souls >= 500) {
            holder.addEffect(MobEffectCompat.vanillaEffect(MobEffects.FIRE_RESISTANCE, 20, 0));
        }
        if (souls >= 1000) {
            holder.addEffect(MobEffectCompat.vanillaEffect(MobEffects.SATURATION, 20, 0));
            if (!holder.hasEffect(MobEffects.HEALTH_BOOST)) {
                holder.addEffect(MobEffectCompat.vanillaEffect(MobEffects.HEALTH_BOOST, 1200, 2));
            }
        }
    }
}