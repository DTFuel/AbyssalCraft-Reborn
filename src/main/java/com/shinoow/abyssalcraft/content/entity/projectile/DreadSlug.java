package com.shinoow.abyssalcraft.content.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.platform.ACThrowableProjectile;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

/**
 * Dread slug (1.12.2 {@code dreadslug}), lobbed by dreadlands mobs. Faithful vanilla flight via
 * {@link ThrowableProjectile}; on entity impact it deals thrown damage and (via the base) discards.
 *
 * <p>Deferred until their subsystems are ported: the {@code dread_plague} potion applied on hit, the
 * dread-immunity check ({@code EntityUtil.isEntityDread}), and hardcore armour-bypass ({@code ACConfig}).
 */
public class DreadSlug extends ACThrowableProjectile {

    public DreadSlug(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void onHit(HitResult result) {
        if (tickCount <= 5) return;
        super.onHit(result);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity living && !EffectHooks.isDreadImmune(living)) {
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 100, 0));
            living.hurt(damageSources().thrown(this, getOwner()), 6.0F);
        }
    }
}
