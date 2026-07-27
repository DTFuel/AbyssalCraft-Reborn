package com.shinoow.abyssalcraft.client;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.platform.LoaderCompat;

/** Live, headless-testable mapping for the RR-CLIENT-FX configuration contract. */
public final class ClientFxConfig {

    public enum Dimension {
        ABYSSAL_WASTELAND(0.25F), DREADLANDS(0.35F), OMOTHOL(0.25F), DARK_REALM(0.10F);

        private final float legacyMinimumLight;

        Dimension(float legacyMinimumLight) {
            this.legacyMinimumLight = legacyMinimumLight;
        }

        public float legacyMinimumLight() {
            return legacyMinimumLight;
        }
    }

    private ClientFxConfig() {}

    public static boolean darkRealmSmokeParticles() {
        return ACConfig.darkRealmSmokeParticles.get() && ACConfig.particleEntity.get();
    }

    public static float depthsHelmetOverlayOpacity() {
        return clampOpacity(ACConfig.depthsHelmetOverlayOpacity.get());
    }

    public static boolean hardcoreDarkness(Dimension dimension) {
        return hardcoreDarkness(dimension, ClientFxConfig::hardcoreDarknessLoaded,
            () -> ACConfig.hcdarkness_aw.get(), () -> ACConfig.hcdarkness_dl.get(),
            () -> ACConfig.hcdarkness_omt.get(), () -> ACConfig.hcdarkness_dr.get());
    }

    public static boolean hardcoreDarkness(Dimension dimension, BooleanSupplier modLoaded,
                                           BooleanSupplier wasteland, BooleanSupplier dreadlands,
                                           BooleanSupplier omothol, BooleanSupplier darkRealm) {
        if (!modLoaded.getAsBoolean()) return false;
        return switch (dimension) {
            case ABYSSAL_WASTELAND -> wasteland.getAsBoolean();
            case DREADLANDS -> dreadlands.getAsBoolean();
            case OMOTHOL -> omothol.getAsBoolean();
            case DARK_REALM -> darkRealm.getAsBoolean();
        };
    }

    public static float clampOpacity(double value) {
        return (float) Math.max(0.5D, Math.min(1.0D, value));
    }

    public static boolean defaults(BooleanSupplier smoke, DoubleSupplier opacity,
                                   BooleanSupplier wasteland, BooleanSupplier dreadlands,
                                   BooleanSupplier omothol, BooleanSupplier darkRealm) {
        return smoke.getAsBoolean() && clampOpacity(opacity.getAsDouble()) == 1.0F
            && wasteland.getAsBoolean() && dreadlands.getAsBoolean()
            && omothol.getAsBoolean() && darkRealm.getAsBoolean();
    }

    private static boolean hardcoreDarknessLoaded() {
        return LoaderCompat.isModLoaded("hardcoredarkness");
    }
}