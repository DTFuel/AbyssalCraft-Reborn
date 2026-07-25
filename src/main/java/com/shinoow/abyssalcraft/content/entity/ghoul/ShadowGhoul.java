package com.shinoow.abyssalcraft.content.entity.ghoul;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The Shadow Ghoul (owned by PD-5, Stage D2a).
 *
 * <p>Faithful port of 1.12.2 {@code EntityShadowGhoul}: 35 health, 6 attack damage, immune to poison,
 * drops shadow ghoul flesh (loot table {@code entities/shadow_ghoul}). The SHADOW creature typing and
 * the ambient shadow particles are deferred with the mob-type/particle subsystems.
 */
public class ShadowGhoul extends AbstractGhoul {

    public ShadowGhoul(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ghoulAttributes()
            .add(Attributes.MAX_HEALTH, 35.0D)
            .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }
}
