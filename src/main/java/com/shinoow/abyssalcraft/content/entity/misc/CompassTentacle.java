package com.shinoow.abyssalcraft.content.entity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.platform.ACSimpleEntity;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.util.GeckoLibUtil;
//? if <1.21 {
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
//?} else {
/*import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
*///?}

/**
 * Compass tentacle (1.12.2 {@code compasstentacle}), a self-contained easter-egg entity: on its first
 * tick it turns to face a fixed world point, then after 120 ticks spawns an explosion puff and vanishes.
 * Ported faithfully with no external subsystem dependencies.
 */
public class CompassTentacle extends ACSimpleEntity implements GeoEntity {

    private static final BlockPos THERE = new BlockPos(4, 54, 85);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private boolean hasLooked;

    public CompassTentacle(EntityType<?> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasLooked) {
            lookAtThere();
            hasLooked = true;
        }
        if (tickCount >= 120) {
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            discard();
        }
    }

    private void lookAtThere() {
        double dx = THERE.getX() - getX();
        double dz = THERE.getZ() - getZ();
        setYRot((float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
