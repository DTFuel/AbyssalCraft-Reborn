package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.entity.Entity;

/** Compat: ignite an entity for a number of seconds across the 1.20/1.21 rename. */
public final class IgniteCompat {

    private IgniteCompat() {}

    public static void ignite(Entity entity, int seconds) {
        //? if >=1.21 {
        /*entity.igniteForSeconds(seconds);
        *///?} else {
        entity.setSecondsOnFire(seconds);
        //?}
    }
}