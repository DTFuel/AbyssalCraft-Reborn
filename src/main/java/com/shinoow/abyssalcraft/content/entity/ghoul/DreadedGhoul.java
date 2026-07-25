package com.shinoow.abyssalcraft.content.entity.ghoul;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The Dreaded Ghoul (owned by PD-5, Stage D2a).
 *
 * <p>Faithful port of 1.12.2 {@code EntityDreadedGhoul}: 35 health, 6 attack damage, immune to fire,
 * drops dreaded ghoul flesh and infects non-dread targets on hit.
 */
public class DreadedGhoul extends AbstractGhoul {

    public DreadedGhoul(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living && !EffectHooks.isDreadImmune(living)) {
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 100, 0));
        }
        return hurt;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ghoulAttributes()
            .add(Attributes.MAX_HEALTH, 35.0D)
            .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }
}
