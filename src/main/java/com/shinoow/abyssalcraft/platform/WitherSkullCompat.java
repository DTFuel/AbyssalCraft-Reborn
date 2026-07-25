package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Compat: construct a vanilla Wither Skull across the 1.20/1.21 direction-argument change. */
public final class WitherSkullCompat {

    private WitherSkullCompat() {}

    public static WitherSkull create(Level level, LivingEntity owner, Vec3 direction) {
        //? if <1.21 {
        return new WitherSkull(level, owner, direction.x, direction.y, direction.z);
        //?} else {
        /*return new WitherSkull(level, owner, direction);
        *///?}
    }
}