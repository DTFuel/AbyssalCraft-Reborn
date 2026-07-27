package com.shinoow.abyssalcraft.system.enchant;

import com.shinoow.abyssalcraft.content.entity.boss.BossKind;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ArmorDurabilityCompat;
import com.shinoow.abyssalcraft.platform.EnchantmentDataCompat;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Production consumers for the five legacy AbyssalCraft enchantment effects. */
public final class EnchantmentEffects {

    private EnchantmentEffects() {}

    public static boolean preventsKnockback(LivingEntity entity) {
        return preventsKnockback(level(entity.getItemBySlot(EquipmentSlot.CHEST), "iron_wall"));
    }

    public static boolean preventsKnockback(int level) {
        return level > 0;
    }

    public static float modifyDamage(LivingEntity victim, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && isShadow(victim)) {
            int lightPierce = level(attacker.getMainHandItem(), "light_pierce");
            amount += lightPierceBonus(lightPierce);
        }
        if (amount > 0.0F && source.is(ACDamageTypes.SHADOW)) {
            ItemStack shield = victim.getUseItem();
            if (absorbsShadowDamage(level(shield, "blinding_light"), victim.isBlocking(),
                    victim.isDamageSourceBlocked(source))) {
                ArmorDurabilityCompat.damageHeld(shield, shieldDamage(amount), victim, victim.getUsedItemHand());
                return 0.0F;
            }
        }
        return amount;
    }

    public static float lightPierceBonus(int level) {
        return Math.max(0, Math.min(5, level)) * 2.5F;
    }

    public static int shieldDamage(float amount) {
        return Math.max(1, (int) amount * 2);
    }

    public static boolean absorbsShadowDamage(int level, boolean blocking, boolean blockedSource) {
        return level > 0 && blocking && blockedSource;
    }

    public static int sappingDrainAmount(int tier, int level) {
        return tier + 1 + Math.max(0, Math.min(3, level));
    }

    public static double multiRendRadius(int level) {
        return level > 0 ? 3.0D : 0.0D;
    }

    public static boolean isShadow(LivingEntity entity) {
        if (entity instanceof AbstractShoggoth shoggoth) return shoggoth.getShoggothType() == 4;
        if (entity.getType() == GhoulEntities.SHADOW_GHOUL.get()
                || entity.getType() == LegacyEntities.SHADOW_CREATURE.get()
                || entity.getType() == LegacyEntities.SHADOW_MONSTER.get()
                || entity.getType() == LegacyEntities.SHADOW_BEAST.get()) return true;
        return entity instanceof BossMob boss && boss.kind() == BossKind.SACTHOTH;
    }

    private static int level(ItemStack stack, String id) {
        return EnchantmentDataCompat.read(stack).getOrDefault(ACRef.id(id), 0);
    }
}