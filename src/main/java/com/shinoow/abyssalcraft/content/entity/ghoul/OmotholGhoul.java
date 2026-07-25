package com.shinoow.abyssalcraft.content.entity.ghoul;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The Omothol Ghoul (owned by PD-5, Stage D2a).
 *
 * <p>Faithful port of 1.12.2 {@code EntityOmotholGhoul}: 60 health, 15 attack damage, follow range 64,
 * knockback resistance 0.2, immune to fire, immune to poison, and inflicts slowness + blindness +
 * night vision on hit. Drops omothol ghoul flesh (loot table {@code entities/omothol_ghoul}). The
 * larger 1.3x2.7 hitbox is set on the registered {@link EntityType}. Underwater breathing is deferred
 * ({@code canBreatheUnderwater} is {@code final} in 1.21).
 */
public class OmotholGhoul extends AbstractGhoul {

    public OmotholGhoul(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        if (flag && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100));
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20));
            living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20));
        }
        return flag;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ghoulAttributes()
            .add(Attributes.FOLLOW_RANGE, 64.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
            .add(Attributes.MAX_HEALTH, 60.0D)
            .add(Attributes.ATTACK_DAMAGE, 15.0D);
    }
}
