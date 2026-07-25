package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.entity.TamableAnimal;

/** Compat: clear an animal's tamed state across the 1.20/1.21 signature change. */
public final class TamableCompat {

    private TamableCompat() {}

    public static void untame(TamableAnimal animal) {
        animal.setOwnerUUID(null);
        //? if >=1.21 {
        /*animal.setTame(false, true);
        *///?} else {
        animal.setTame(false);
        //?}
    }
}