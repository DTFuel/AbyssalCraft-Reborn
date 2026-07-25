package com.shinoow.abyssalcraft.content.entity.boss;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.platform.EntityPartCompat;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

/** Shared seven-part flight, collision and healing-circle controller for both AbyssalCraft dragons. */
final class DragonFlightController<T extends Mob & EntityPartCompat.Parent> {

    private final T parent;
    private final boolean boss;
    private final double[][] ringBuffer = new double[64][3];
    private final EntityPartCompat.Part<T> head;
    private final EntityPartCompat.Part<T> body;
    private final EntityPartCompat.Part<T>[] tails;
    private final EntityPartCompat.Part<T>[] wings;
    private final EntityPartCompat.Part<?>[] parts;

    private int ringBufferIndex = -1;
    private float yawVelocity;
    private double targetX;
    private double targetY = 100.0D;
    private double targetZ;
    @Nullable
    private UUID healingPartner;

    @SuppressWarnings("unchecked")
    DragonFlightController(T parent, boolean boss) {
        this.parent = parent;
        this.boss = boss;
        float scale = boss ? 1.0F : 1.0F / 3.0F;
        head = new EntityPartCompat.Part<>(parent, "head", boss ? 4.5F : 1.5F, boss ? 4.5F : 1.5F);
        body = new EntityPartCompat.Part<>(parent, "body", boss ? 7.5F : 2.5F, boss ? 4.5F : 1.5F);
        tails = new EntityPartCompat.Part[] {
            new EntityPartCompat.Part<>(parent, "tail_1", 3.0F * scale, 3.0F * scale),
            new EntityPartCompat.Part<>(parent, "tail_2", 3.0F * scale, 3.0F * scale),
            new EntityPartCompat.Part<>(parent, "tail_3", 3.0F * scale, 3.0F * scale)
        };
        wings = new EntityPartCompat.Part[] {
            new EntityPartCompat.Part<>(parent, "wing_1", 6.0F * scale, 3.0F * scale),
            new EntityPartCompat.Part<>(parent, "wing_2", 6.0F * scale, 3.0F * scale)
        };
        parts = new EntityPartCompat.Part<?>[] { head, body, tails[0], tails[1], tails[2], wings[0], wings[1] };
        parent.setNoGravity(true);
    }

    EntityPartCompat.Part<?>[] parts() {
        return parts;
    }

    boolean isHead(EntityPartCompat.Part<?> part) {
        return part == head;
    }

    float adjustedPartDamage(EntityPartCompat.Part<?> part, float amount) {
        if (part != head) amount = boss ? amount * 0.25F + 1.0F : amount * 0.5F + 1.0F;
        if (boss && amount > 30.0F) amount = 10.0F + parent.getRandom().nextInt(10);
        chooseRetreatTarget();
        return amount;
    }

    void tick() {
        recordMovement();
        if (!parent.level().isClientSide && parent.isAlive()) {
            updateFlight();
            updateHealingCircle();
        }
        updateParts();
        if (!parent.level().isClientSide && parent.isAlive() && parent.hurtTime == 0) attackWithParts();
    }

    void save(CompoundTag tag) {
        tag.putDouble("DragonTargetX", targetX);
        tag.putDouble("DragonTargetY", targetY);
        tag.putDouble("DragonTargetZ", targetZ);
        if (healingPartner != null) tag.putUUID("HealingPartner", healingPartner);
    }

    void load(CompoundTag tag) {
        targetX = tag.contains("DragonTargetX") ? tag.getDouble("DragonTargetX") : parent.getX();
        targetY = tag.contains("DragonTargetY") ? tag.getDouble("DragonTargetY") : 100.0D;
        targetZ = tag.contains("DragonTargetZ") ? tag.getDouble("DragonTargetZ") : parent.getZ();
        healingPartner = tag.hasUUID("HealingPartner") ? tag.getUUID("HealingPartner") : null;
    }

    private void recordMovement() {
        if (ringBufferIndex < 0) {
            for (double[] sample : ringBuffer) {
                sample[0] = parent.getYRot();
                sample[1] = parent.getY();
            }
        }
        ringBufferIndex = (ringBufferIndex + 1) & 63;
        ringBuffer[ringBufferIndex][0] = parent.getYRot();
        ringBuffer[ringBufferIndex][1] = parent.getY();
    }

    private double[] movementOffsets(int offset) {
        int current = (ringBufferIndex - offset) & 63;
        int previous = (ringBufferIndex - offset - 1) & 63;
        double yaw = ringBuffer[current][0];
        return new double[] {
            yaw + Mth.wrapDegrees(ringBuffer[previous][0] - yaw),
            ringBuffer[previous][1],
            ringBuffer[previous][2]
        };
    }

    private void updateFlight() {
        LivingEntity combatTarget = parent.getTarget();
        if (combatTarget != null && combatTarget.isAlive()) {
            targetX = combatTarget.getX();
            targetZ = combatTarget.getZ();
            double horizontal = Math.sqrt(parent.distanceToSqr(targetX, parent.getY(), targetZ));
            targetY = combatTarget.getBoundingBox().minY + Math.min(10.0D, Math.max(0.0D,
                0.4D + horizontal / 80.0D - 1.0D));
        }

        double dx = targetX - parent.getX();
        double dy = targetY - parent.getY();
        double dz = targetZ - parent.getZ();
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared < 100.0D || distanceSquared > 22500.0D || parent.horizontalCollision
                || parent.verticalCollision) {
            chooseNewTarget();
            dx = targetX - parent.getX();
            dy = targetY - parent.getY();
            dz = targetZ - parent.getZ();
        }

        double horizontal = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        Vec3 velocity = parent.getDeltaMovement();
        double vertical = Mth.clamp(dy / horizontal, -0.6D, 0.6D);
        velocity = velocity.add(0.0D, vertical * 0.1D, 0.0D);

        double desiredYaw = 180.0D - Math.atan2(dx, dz) * Mth.RAD_TO_DEG;
        double yawDelta = Mth.clamp(Mth.wrapDegrees(desiredYaw - parent.getYRot()), -50.0D, 50.0D);
        yawVelocity = yawVelocity * 0.8F + (float) (yawDelta * 0.035D);
        parent.setYRot(parent.getYRot() + yawVelocity * 0.1F);
        parent.yBodyRot = parent.getYRot();

        Vec3 desired = new Vec3(dx, dy, dz).normalize();
        Vec3 facing = new Vec3(Mth.sin(parent.getYRot() * Mth.DEG_TO_RAD), velocity.y,
            -Mth.cos(parent.getYRot() * Mth.DEG_TO_RAD)).normalize();
        float alignment = Math.max(0.0F, (float) (facing.dot(desired) + 0.5D) / 1.5F);
        double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) + 1.0D;
        float acceleration = 0.06F * (alignment * (float) (2.0D / (horizontalSpeed + 1.0D))
            + 1.0F - (float) (2.0D / (horizontalSpeed + 1.0D)));
        velocity = velocity.add(Mth.sin(parent.getYRot() * Mth.DEG_TO_RAD) * acceleration, 0.0D,
            -Mth.cos(parent.getYRot() * Mth.DEG_TO_RAD) * acceleration);
        Vec3 normalizedVelocity = velocity.lengthSqr() > 0.0D ? velocity.normalize() : Vec3.ZERO;
        float drag = 0.8F + 0.15F * (float) ((normalizedVelocity.dot(facing) + 1.0D) / (boss ? 2.0D : 2.5D));
        parent.setDeltaMovement(velocity.x * drag, velocity.y * 0.91D, velocity.z * drag);
    }

    private void chooseNewTarget() {
        if (parent.getRandom().nextBoolean()) {
            Player player = parent.level().getNearestPlayer(parent, 150.0D);
            if (player != null && !player.isCreative() && !player.isSpectator()) {
                targetX = player.getX();
                targetY = player.getY();
                targetZ = player.getZ();
                return;
            }
        }
        targetX = parent.getX() + parent.getRandom().nextFloat() * 120.0F - 60.0F;
        targetY = Math.max(parent.level().getMinBuildHeight() + 10,
            parent.getY() + parent.getRandom().nextFloat() * 50.0F - 25.0F);
        targetZ = parent.getZ() + parent.getRandom().nextFloat() * 120.0F - 60.0F;
    }

    private void chooseRetreatTarget() {
        float yaw = parent.getYRot() * Mth.DEG_TO_RAD;
        targetX = parent.getX() + Mth.sin(yaw) * 5.0F + parent.getRandom().nextFloat() * 2.0F - 1.0F;
        targetY = parent.getY() + parent.getRandom().nextFloat() * 3.0F + 1.0D;
        targetZ = parent.getZ() - Mth.cos(yaw) * 5.0F + parent.getRandom().nextFloat() * 2.0F - 1.0F;
    }

    private void updateParts() {
        double[] oldBody = movementOffsets(5);
        double[] olderBody = movementOffsets(10);
        float pitch = (float) (oldBody[1] - olderBody[1]) * 10.0F * Mth.DEG_TO_RAD;
        float pitchCos = Mth.cos(pitch);
        float pitchSin = -Mth.sin(pitch);
        float yaw = parent.getYRot() * Mth.DEG_TO_RAD;
        float yawSin = Mth.sin(yaw);
        float yawCos = Mth.cos(yaw);
        float bodyOffset = boss ? 1.5F : 0.1F;
        float wingOffset = boss ? 6.5F : 2.2F;
        double wingY = parent.getY() + (boss ? 2.0D : 1.0D);

        movePart(body, parent.getX() + yawSin * bodyOffset, parent.getY(),
            parent.getZ() - yawCos * bodyOffset);
        movePart(wings[0], parent.getX() + yawCos * wingOffset, wingY,
            parent.getZ() + yawSin * wingOffset);
        movePart(wings[1], parent.getX() - yawCos * wingOffset, wingY,
            parent.getZ() - yawSin * wingOffset);

        double[] headNow = movementOffsets(0);
        float headReach = boss ? 9.2F : 2.5F;
        float adjustedYaw = parent.getYRot() * Mth.DEG_TO_RAD - yawVelocity * 0.01F;
        movePart(head, parent.getX() + Mth.sin(adjustedYaw) * headReach * pitchCos,
            parent.getY() + (headNow[1] - oldBody[1]) + pitchSin * headReach,
            parent.getZ() - Mth.cos(adjustedYaw) * headReach * pitchCos);

        float tailBase = boss ? 1.5F : 0.1F;
        float tailStep = boss ? 3.0F : 1.5F;
        double tailY = boss ? 1.5D : 1.0D;
        for (int index = 0; index < tails.length; index++) {
            double[] tailHistory = movementOffsets(12 + index * 2);
            float tailYaw = yaw + Mth.wrapDegrees((float) (tailHistory[0] - oldBody[0])) * Mth.DEG_TO_RAD;
            float length = (index + 1) * tailStep;
            movePart(tails[index],
                parent.getX() - (yawSin * tailBase + Mth.sin(tailYaw) * length) * pitchCos,
                parent.getY() + (tailHistory[1] - oldBody[1]) - (length + tailBase) * pitchSin + tailY,
                parent.getZ() + (yawCos * tailBase + Mth.cos(tailYaw) * length) * pitchCos);
        }
    }

    private void movePart(EntityPartCompat.Part<?> part, double x, double y, double z) {
        part.setPos(x, y, z);
        part.setYRot(parent.getYRot());
        part.setXRot(parent.getXRot());
    }

    private void attackWithParts() {
        double wingInflateXz = boss ? 5.0D : 1.0D;
        double wingInflateY = boss ? 4.0D : 0.5D;
        double wingDown = boss ? -4.0D : -0.5D;
        if (boss || ACConfig.hardcoreMode.get()) {
            pushEntities(wings[0].getBoundingBox().inflate(wingInflateXz, wingInflateY, wingInflateXz)
                .move(0.0D, wingDown, 0.0D));
            pushEntities(wings[1].getBoundingBox().inflate(wingInflateXz, wingInflateY, wingInflateXz)
                .move(0.0D, wingDown, 0.0D));
        }
        attackHead(head.getBoundingBox().inflate(boss ? 1.5D : 0.25D));
    }

    private void pushEntities(AABB area) {
        double centerX = (body.getBoundingBox().minX + body.getBoundingBox().maxX) * 0.5D;
        double centerZ = (body.getBoundingBox().minZ + body.getBoundingBox().maxZ) * 0.5D;
        for (LivingEntity target : parent.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != parent && !isDragon(entity))) {
            double dx = target.getX() - centerX;
            double dz = target.getZ() - centerZ;
            double distance = Math.max(0.1D, dx * dx + dz * dz);
            target.push(dx / distance * 4.0D, 0.2D, dz / distance * 4.0D);
        }
    }

    private void attackHead(AABB area) {
        for (LivingEntity target : parent.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != parent && !isDragon(entity))) {
            if (boss) {
                target.hurt(parent.damageSources().mobAttack(parent), 10.0F);
                if (ACConfig.hardcoreMode.get() && target instanceof Player) {
                    target.hurt(ACDamageTypes.source(parent, ACDamageTypes.CORALIUM),
                        Math.max(1.0F, ACConfig.damageAmpl.get().floatValue()));
                }
            } else if (!EffectHooks.isCoraliumImmune(target)) {
                target.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 200, 0));
                if (ACConfig.hardcoreMode.get() && target instanceof Player) {
                    target.hurt(ACDamageTypes.source(parent, ACDamageTypes.CORALIUM), 1.0F);
                }
            }
        }
    }

    private boolean isDragon(LivingEntity entity) {
        return entity instanceof DragonBoss || entity instanceof DragonMinion;
    }

    private void updateHealingCircle() {
        if (parent.tickCount % 10 != 0 || !(parent.level() instanceof net.minecraft.server.level.ServerLevel server)) {
            return;
        }
        Entity linked = healingPartner == null ? null : server.getEntity(healingPartner);
        if (boss) {
            DragonMinion minion = linked instanceof DragonMinion dragon && dragon.isAlive()
                ? dragon : nearest(DragonMinion.class);
            if (minion == null) {
                healingPartner = null;
                return;
            }
            healingPartner = minion.getUUID();
            if (parent.getHealth() < parent.getMaxHealth()) parent.heal(1.0F);
        } else {
            DragonBoss dragonBoss = linked instanceof DragonBoss dragon && dragon.isAlive()
                ? dragon : nearest(DragonBoss.class);
            if (dragonBoss == null) {
                if (healingPartner != null) parent.setHealth(0.0F);
                healingPartner = null;
                return;
            }
            healingPartner = dragonBoss.getUUID();
            if (dragonBoss.getHealth() < dragonBoss.getMaxHealth()) {
                parent.setHealth(parent.getHealth() - 1.0F);
            }
        }
    }

    @Nullable
    private <E extends LivingEntity> E nearest(Class<E> type) {
        E nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (E candidate : parent.level().getEntitiesOfClass(type, parent.getBoundingBox().inflate(32.0D),
                LivingEntity::isAlive)) {
            double distance = candidate.distanceToSqr(parent);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }
}