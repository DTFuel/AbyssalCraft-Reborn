package com.shinoow.abyssalcraft.content.entity.misc;

import java.util.List;
import java.util.UUID;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.content.entity.behavior.EldritchEntities;
import com.shinoow.abyssalcraft.platform.ACSimpleEntity;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.portal.DimensionDataRegistry;
import com.shinoow.abyssalcraft.world.portal.DimensionTeleport;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Black hole (1.12.2 {@code blackhole}), one of Jzahar's attacks. Drags nearby living entities toward
 * its centre for its 300-tick lifetime, excludes eldritch entities, and teleports close victims to a
 * random available dimension while preserving the J'zahar owner link across save/reload.
 */
public class BlackHole extends ACSimpleEntity {

    private UUID ownerUuid;
    private double speed = 0.05D;

    public BlackHole(EntityType<?> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public BlackHole setOwner(LivingEntity owner) {
        ownerUuid = owner == null ? null : owner.getUUID();
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > 300) {
            discard();
            return;
        }
        LivingEntity owner = owner();
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(48.0D))) {
            if (!target.isAlive() || EldritchEntities.isEldritch(target)) continue;
            if (target.getY() < getY()) target.push(0.0D, 0.025D, 0.0D);
            double dx = getX() + (random.nextDouble() * 8.0D - 4.0D) - target.getX();
            double dy = getY() + (random.nextDouble() * 8.0D - 4.0D) - target.getY();
            double dz = getZ() + (random.nextDouble() * 8.0D - 4.0D) - target.getZ();
            double distanceSquared = Math.max(0.25D, dx * dx + dy * dy + dz * dz);
            target.push(dx / distanceSquared * 7.0D * speed,
                dy / distanceSquared * 7.0D * speed,
                dz / distanceSquared * 7.0D * speed);
            if (owner != null && ACConfig.hardcoreMode.get() && target.distanceToSqr(this) <= 36.0D) {
                target.hurt(damageSources().fellOutOfWorld(), 4.0F);
            }
            if (owner != null && target.distanceToSqr(this) <= 9.0D && level() instanceof ServerLevel) {
                teleport(target);
                if (ACConfig.hardcoreMode.get()) {
                    target.setHealth(target.getHealth() - 20.0F);
                    target.hurt(damageSources().cramming(), 50.0F);
                }
            }
        }
        speed += 0.0005D;
    }

    private LivingEntity owner() {
        if (ownerUuid == null || !(level() instanceof ServerLevel server)) return null;
        Entity entity = server.getEntity(ownerUuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    private void teleport(LivingEntity target) {
        if (target.isOnPortalCooldown()) {
            target.setPortalCooldown();
            return;
        }
        List<ResourceKey<Level>> candidates = DimensionDataRegistry.instance().values().stream()
            .map(data -> data.dimension())
            .filter(dimension -> dimension != target.level().dimension())
            .filter(dimension -> !ComplexConfig.blackHoleDimensionBlacklist().contains(dimension.location()))
            .filter(dimension -> ((ServerLevel) target.level()).getServer().getLevel(dimension) != null)
            .toList();
        if (candidates.isEmpty()) return;
        target.addEffect(MobEffectCompat.vanillaEffect(MobEffects.DAMAGE_RESISTANCE, 80, 255));
        target.setPortalCooldown(ACConfig.portalCooldown.get());
        Entity moved = DimensionTeleport.teleport(target, candidates.get(random.nextInt(candidates.size())));
        moved.setPortalCooldown(ACConfig.portalCooldown.get());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
        tag.putDouble("PullSpeed", speed);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        speed = tag.contains("PullSpeed") ? tag.getDouble("PullSpeed") : 0.05D;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }
}
