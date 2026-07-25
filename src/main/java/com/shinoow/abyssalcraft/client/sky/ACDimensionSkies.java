package com.shinoow.abyssalcraft.client.sky;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DimensionEffectsCompat;

/**
 * Dimension-effects relay (PH-2 / Stage H1). <b>Client-only</b>, fork-free.
 *
 * <p>Registers one {@link ACDimensionEffects} per AbyssalCraft dimension under an {@code abyssalcraft:}
 * effects id; each dimension's {@code dimension_type} JSON points its {@code "effects"} field at the
 * matching id so vanilla resolves this instance for fog/sky. Attached to the MOD bus by the main class
 * through {@link DimensionEffectsCompat#attach} (inside {@code runWhenClient}).
 */
public final class ACDimensionSkies {

    private ACDimensionSkies() {}

    /** Register every AC dimension's render effects (faithful 1.12.2 fog; custom skybox deferred). */
    public static void register(DimensionEffectsCompat.Effects sink) {
        // Abyssal Wasteland: overworld-style fog (no 1.12.2 override), no thick fog.
        sink.register(ACRef.id("abyssal_wasteland"), new ACDimensionEffects(null, false));
        // Dreadlands: constant dark-red fog + thick fog.
        sink.register(ACRef.id("dreadlands"), new ACDimensionEffects(ACDimensionEffects.DREADLANDS_FOG, true));
        // Dark Realm: constant very-dark fog, no thick fog (matches 1.12.2 black-sky + dark-fog look).
        sink.register(ACRef.id("dark_realm"), new ACDimensionEffects(ACDimensionEffects.DARK_FOG, false));
        // Omothol: constant very-dark fog + thick fog.
        sink.register(ACRef.id("omothol"), new ACDimensionEffects(ACDimensionEffects.DARK_FOG, true));
    }
}
