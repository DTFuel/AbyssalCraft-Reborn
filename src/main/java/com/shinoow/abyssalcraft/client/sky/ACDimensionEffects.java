package com.shinoow.abyssalcraft.client.sky;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * Per-dimension render effects (fog) for the AbyssalCraft dimensions (PH-2 / Stage H1). <b>Client-only</b>.
 *
 * <p>Faithful to the 1.12.2 {@code WorldProvider} client overrides: the Dreadlands used a constant dark-red
 * fog {@code (0.2, 0.03, 0.03)}; the Dark Realm and Omothol used a constant very-dark fog derived from
 * {@code 0xA080A0 * 0.15}; the Abyssal Wasteland left the fog default (overworld-style, brightness-scaled).
 * {@code doesXZShowFog} (thick fog) was on for the Dreadlands and Omothol.
 *
 * <p>This is fork-free: {@link DimensionSpecialEffects}, {@link Vec3} and the two overridden methods have
 * identical signatures on 1.20.1 and 1.21, so the whole class is loader-neutral. The loader axis lives only
 * in {@link com.shinoow.abyssalcraft.platform.DimensionEffectsCompat} (the {@code RegisterDimensionSpecialEffectsEvent}
 * package differs). The distinctive tinted skybox textures (AW/Dreadlands/Omothol) are migrated under
 * {@code textures/environment/} but their immediate-mode cube render is deferred -- that draw call diverges
 * heavily between the 1.20.1 and 1.21 render pipelines -- so {@link DimensionSpecialEffects.SkyType#NONE} is
 * used, giving a void sky over the correct fog (which matches the Dark Realm's 1.12.2 black sky exactly).
 */
public class ACDimensionEffects extends DimensionSpecialEffects {

    /** 1.12.2 Dark Realm / Omothol fog: {@code 0xA080A0} scaled by {@code 0.15} (brightness term was zeroed). */
    public static final Vec3 DARK_FOG = fromPacked(10518688, 0.15F);
    /** 1.12.2 Dreadlands fog. */
    public static final Vec3 DREADLANDS_FOG = new Vec3(0.20000000298023224D, 0.029999999329447746D, 0.029999999329447746D);

    private final Vec3 fog;
    private final boolean foggy;

    /**
     * @param fog   constant fog colour, or {@code null} to keep the overworld-style brightness-scaled fog
     *              (the Abyssal Wasteland, which did not override {@code getFogColor} in 1.12.2)
     * @param foggy whether thick "XZ" fog shows (1.12.2 {@code doesXZShowFog})
     */
    public ACDimensionEffects(Vec3 fog, boolean foggy) {
        // No clouds (Float.NaN); no solid ground; void sky (custom skybox deferred); default lightmap/ambient.
        super(Float.NaN, false, SkyType.NONE, false, false);
        this.fog = fog;
        this.foggy = foggy;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        // AC dims used a constant fog (1.12.2 zeroed the celestial-angle term); null keeps the vanilla
        // overworld scaling for the Abyssal Wasteland.
        return fog != null ? fog : color.scale(sunHeight * 0.94F + 0.06F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return foggy;
    }

    private static Vec3 fromPacked(int rgb, float mul) {
        return new Vec3((rgb >> 16 & 255) / 255.0F * mul,
                        (rgb >> 8 & 255) / 255.0F * mul,
                        (rgb & 255) / 255.0F * mul);
    }
}
