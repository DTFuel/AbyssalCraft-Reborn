package com.shinoow.abyssalcraft.content.entity.projectile;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Coralium arrow (1.12.2 {@code coraliumarrow}), fired by depths ghouls. Extends the concrete vanilla
 * {@link Arrow} so flight, block/entity collision, pickup and the base 2.0 damage come for free and
 * stay fork-free (the abstract {@code AbstractArrow} pickup hook forks across 1.20.1 &harr; 1.21).
 *
 * <p>Successful hits infect non-coralium targets with the coralium plague.
 */
public class CoraliumArrow extends Arrow {

    public CoraliumArrow(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        int oldInvulnerability = result.getEntity().invulnerableTime;
        super.onHitEntity(result);
        if (!level().isClientSide && result.getEntity() instanceof LivingEntity living
                && living.invulnerableTime != oldInvulnerability && !EffectHooks.isCoraliumImmune(living)) {
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 100, 0));
        }
    }
}
