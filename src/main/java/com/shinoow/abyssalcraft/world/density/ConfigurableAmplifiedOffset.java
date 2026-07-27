package com.shinoow.abyssalcraft.world.density;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import com.shinoow.abyssalcraft.config.ACConfig;

/** Deterministic terrain offset selected dynamically by the legacy amplified-world config. */
public final class ConfigurableAmplifiedOffset implements DensityFunction.SimpleFunction {

    public static final ConfigurableAmplifiedOffset INSTANCE = new ConfigurableAmplifiedOffset();
    public static final KeyDispatchDataCodec<ConfigurableAmplifiedOffset> CODEC =
        KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

    private ConfigurableAmplifiedOffset() {}

    @Override
    public double compute(FunctionContext context) {
        if (!ACConfig.useAmplifiedWorldType.get()) return 0.0D;
        double broad = Math.sin(context.blockX() / 96.0D) + Math.cos(context.blockZ() / 96.0D);
        double detail = Math.sin((context.blockX() + context.blockZ()) / 32.0D) * 0.35D;
        return (broad + detail) * 0.65D;
    }

    @Override
    public double minValue() {
        return -1.6D;
    }

    @Override
    public double maxValue() {
        return 1.6D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}