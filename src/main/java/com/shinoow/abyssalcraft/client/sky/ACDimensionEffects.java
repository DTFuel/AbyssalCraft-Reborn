package com.shinoow.abyssalcraft.client.sky;

import com.shinoow.abyssalcraft.platform.DimensionSkyCompat;
import com.shinoow.abyssalcraft.client.ClientFxConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Per-dimension render effects (fog + tinted skybox) for the AbyssalCraft dimensions (PH-2 / RR-CLIENT-FX).
 * <b>Client-only</b>.
 *
 * <p>Faithful to the 1.12.2 {@code WorldProvider} client overrides: the Dreadlands used a constant dark-red
 * fog {@code (0.2, 0.03, 0.03)}; the Dark Realm and Omothol used a constant very-dark fog derived from
 * {@code 0xA080A0 * 0.15}; the Abyssal Wasteland left the fog default (overworld-style, brightness-scaled).
 * {@code doesXZShowFog} (thick fog) was on for the Dreadlands and Omothol.
 *
 * <p>The distinctive tinted skybox (1.12.2 {@code ACSkyRenderer}) is drawn through {@link DimensionSkyCompat},
 * which absorbs the loader/version split of the {@code renderSky} extension and the immediate-mode cube draw.
 * The Abyssal Wasteland, Dreadlands and Omothol each paint their environment texture; the Dark Realm reuses
 * Omothol's texture with its own tint (faithful to the 1.12.2 {@code WorldProviderDarkRealm} skybox). A
 * {@code null} {@link #skyTexture()} keeps a plain void sky. The fog methods stay fork-free; the loader axis
 * lives in {@link com.shinoow.abyssalcraft.platform.DimensionEffectsCompat} (registration) and
 * {@link DimensionSkyCompat} (render).
 */
public class ACDimensionEffects extends DimensionSkyCompat {

    /** 1.12.2 Dark Realm / Omothol fog: {@code 0xA080A0} scaled by {@code 0.15} (brightness term was zeroed). */
    public static final Vec3 DARK_FOG = fromPacked(10518688, 0.15F);
    /** 1.12.2 Dreadlands fog. */
    public static final Vec3 DREADLANDS_FOG = new Vec3(0.20000000298023224D, 0.029999999329447746D, 0.029999999329447746D);

    /** A live tint source (0-255 per channel), read every frame so a {@code clientvars.json} reload retints. */
    public interface SkyTint {
        int red();

        int green();

        int blue();
    }

    private final Vec3 fog;
    private final boolean foggy;
    private final ResourceLocation skyTexture;
    private final SkyTint tint;
    private final ClientFxConfig.Dimension dimension;

    /**
     * @param fog   constant fog colour, or {@code null} to keep the overworld-style brightness-scaled fog
     *              (the Abyssal Wasteland, which did not override {@code getFogColor} in 1.12.2)
     * @param foggy whether thick "XZ" fog shows (1.12.2 {@code doesXZShowFog})
     */
    public ACDimensionEffects(Vec3 fog, boolean foggy) {
        this(fog, foggy, null, null, null);
    }

    /**
     * @param skyTexture the tinted skybox texture, or {@code null} for a plain void sky
     * @param tint       the live skybox tint (ignored when {@code skyTexture} is {@code null})
     */
    public ACDimensionEffects(Vec3 fog, boolean foggy, ResourceLocation skyTexture, SkyTint tint,
                              ClientFxConfig.Dimension dimension) {
        // No clouds (Float.NaN); no solid ground; void sky base (custom skybox on top); default lightmap/ambient.
        super(Float.NaN, false, SkyType.NONE, false, false);
        this.fog = fog;
        this.foggy = foggy;
        this.skyTexture = skyTexture;
        this.tint = tint;
        this.dimension = dimension;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        // AC dims used a constant fog (1.12.2 zeroed the celestial-angle term); null keeps the vanilla
        // overworld scaling for the Abyssal Wasteland.
        Vec3 resolved = fog != null ? fog : color.scale(sunHeight * 0.94F + 0.06F);
        return isHardcoreDarkness() ? resolved.scale(dimension.legacyMinimumLight()) : resolved;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return foggy;
    }

    @Override
    protected ResourceLocation skyTexture() {
        return skyTexture;
    }

    @Override
    protected int skyRed() {
        return tint.red();
    }

    @Override
    protected int skyGreen() {
        return tint.green();
    }

    @Override
    protected int skyBlue() {
        return tint.blue();
    }

    @Override
    protected float skyBrightness() {
        return isHardcoreDarkness() ? dimension.legacyMinimumLight() : 1.0F;
    }

    private boolean isHardcoreDarkness() {
        return dimension != null && ClientFxConfig.hardcoreDarkness(dimension);
    }

    private static Vec3 fromPacked(int rgb, float mul) {
        return new Vec3((rgb >> 16 & 255) / 255.0F * mul,
                        (rgb >> 8 & 255) / 255.0F * mul,
                        (rgb & 255) / 255.0F * mul);
    }
}
