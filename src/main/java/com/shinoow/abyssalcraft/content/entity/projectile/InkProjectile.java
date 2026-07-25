package com.shinoow.abyssalcraft.content.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.platform.ACThrowableProjectile;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

/**
 * Ink projectile (1.12.2 {@code inkprojectile}), squirted by the coralium squid. Faithful vanilla
 * flight via {@link ThrowableProjectile}; on entity impact it deals thrown damage and (via the base)
 * discards.
 *
 * <p>Deferred until their subsystems are ported: the {@code coralium_plague} potion, the random
 * blindness / slowness applications (the {@code MobEffectInstance} holder constructor forks across
 * 1.20.1 &harr; 1.21), the squid self-immunity, and hardcore armour-bypass ({@code ACConfig}).
 */
public class InkProjectile extends ACThrowableProjectile {

    public InkProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity living) {
            boolean immune = EffectHooks.isCoraliumImmune(living);
            if (!immune && random.nextBoolean()) {
                living.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 100, 0));
            }
            if (!immune && random.nextInt(4) == 0) living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
            if (!immune && random.nextInt(5) == 0) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100));
            if (!immune) {
                living.hurt(damageSources().thrown(this, getOwner()), 2.0F);
            }
        }
    }
}
