package com.shinoow.abyssalcraft.content.entity.projectile;

import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Dreaded charge (1.12.2 {@code dreadedcharge}), a non-fiery fireball flung by dreadguards. Extends
 * {@link AbstractHurtingProjectile} (which implements {@code defineSynchedData}, so this stays
 * fork-free) for faithful accelerating flight. Impact deals dread damage and creates a lingering
 * dread-plague cloud plus the legacy non-destructive blast.
 */
public class DreadedCharge extends AbstractHurtingProjectile {

    public DreadedCharge(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected void onHit(HitResult result) {
        if (tickCount <= 5) return;
        if (!level().isClientSide) createImpact();
        super.onHit(result);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide) {
            result.getEntity().hurt(ACDamageTypes.projectile(this, ACDamageTypes.DREAD), 4.0F);
            result.getEntity().invulnerableTime = 0;
        }
    }

    private void createImpact() {
        AreaEffectCloud cloud = new AreaEffectCloud(level(), getX(), getY(), getZ());
        if (getOwner() instanceof LivingEntity owner) cloud.setOwner(owner);
        cloud.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 400, 0));
        cloud.setRadius(2.0F);
        cloud.setDuration(200 + random.nextInt(200));
        cloud.setRadiusPerTick((3.0F - cloud.getRadius()) / cloud.getDuration());
        level().addFreshEntity(cloud);
        level().playSound(null, blockPosition(), SoundEvents.DRAGON_FIREBALL_EXPLODE,
            SoundSource.HOSTILE, 1.0F, random.nextFloat() * 0.1F + 0.9F);
        level().explode(this, getX(), getY() + 1.0D, getZ(), 3.0F, Level.ExplosionInteraction.NONE);
    }
}
