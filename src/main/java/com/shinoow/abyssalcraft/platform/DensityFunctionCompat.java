package com.shinoow.abyssalcraft.platform;

import java.util.Map;

//? if >=1.21 {
/*import com.mojang.serialization.MapCodec;
*///?} else {
import com.mojang.serialization.Codec;
//?}

import net.minecraft.core.registries.Registries;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import com.shinoow.abyssalcraft.AbyssalCraft;

/** Compat for the Codec-to-MapCodec density-function registry change in 1.21. */
public final class DensityFunctionCompat {

    private DensityFunctionCompat() {}

    /** Create a density-function registrar and register the supplied dispatch codecs. */
    public static ModRegistrar<?> register(Map<String, KeyDispatchDataCodec<? extends DensityFunction>> codecs) {
        //? if >=1.21 {
        /*ModRegistrar<MapCodec<? extends DensityFunction>> registrar =
            ModRegistrar.of(Registries.DENSITY_FUNCTION_TYPE, AbyssalCraft.MODID);
        codecs.forEach((name, codec) -> registrar.register(name, codec::codec));
        return registrar;
        *///?} else {
        ModRegistrar<Codec<? extends DensityFunction>> registrar =
            ModRegistrar.of(Registries.DENSITY_FUNCTION_TYPE, AbyssalCraft.MODID);
        codecs.forEach((name, codec) -> registrar.register(name, codec::codec));
        return registrar;
        //?}
    }
}