package com.shinoow.abyssalcraft.world.density;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * World-seed-independent Dark Realm cavity mask.
 *
 * <p>The legacy generator constructed a second noise stack from the fixed seed {@code 1251393890}
 * and carved its positive region between Y 30 and 157. This modern density function preserves that
 * contract without retaining server/world references. Four per-thread chunk masks bound memory and
 * make repeated interpolation queries deterministic and inexpensive.
 */
public final class DarkRealmCavityMask implements DensityFunction.SimpleFunction {

    public static final long FIXED_SEED = 1_251_393_890L;
    public static final DarkRealmCavityMask INSTANCE = new DarkRealmCavityMask();
    public static final KeyDispatchDataCodec<DarkRealmCavityMask> CODEC =
        KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

    private static final int MIN_Y = 30;
    private static final int MAX_Y = 157;
    private DarkRealmCavityMask() {}

    @Override
    public double compute(FunctionContext context) {
        int y = context.blockY();
        if (y < MIN_Y || y > MAX_Y) return 0.0D;
        return LegacyDarkRealmNoise.INSTANCE.carves(context.blockX(), y, context.blockZ()) ? 1.0D : 0.0D;
    }

    @Override
    public double minValue() {
        return 0.0D;
    }

    @Override
    public double maxValue() {
        return 1.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    /** Exposed for deterministic validation without constructing a world. */
    public static boolean carves(int blockX, int blockY, int blockZ) {
        return LegacyDarkRealmNoise.INSTANCE.carves(blockX, blockY, blockZ);
    }
}