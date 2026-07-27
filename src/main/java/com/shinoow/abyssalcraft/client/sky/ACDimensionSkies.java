package com.shinoow.abyssalcraft.client.sky;

import java.util.function.ToIntFunction;

import com.shinoow.abyssalcraft.client.hud.ClientVars;
import com.shinoow.abyssalcraft.client.hud.ClientVarsManager;
import com.shinoow.abyssalcraft.client.ClientFxConfig;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DimensionEffectsCompat;

/**
 * Dimension-effects relay (PH-2 / RR-CLIENT-FX). <b>Client-only</b>, fork-free.
 *
 * <p>Registers one {@link ACDimensionEffects} per AbyssalCraft dimension under an {@code abyssalcraft:}
 * effects id; each dimension's {@code dimension_type} JSON points its {@code "effects"} field at the
 * matching id so vanilla resolves this instance for fog/sky. Attached to the MOD bus by the main class
 * through {@link DimensionEffectsCompat#attach} (inside {@code runWhenClient}). Skybox tints are read live
 * from {@link ClientVarsManager} so a {@code clientvars.json} resource reload retints without a restart.
 */
public final class ACDimensionSkies {

    private ACDimensionSkies() {}

    /** Register every AC dimension's render effects (faithful 1.12.2 fog + tinted skybox). */
    public static void register(DimensionEffectsCompat.Effects sink) {
        // Abyssal Wasteland: overworld-style fog (no 1.12.2 override), no thick fog; tinted skybox.
        sink.register(ACRef.id("abyssal_wasteland"), new ACDimensionEffects(null, false,
            ACRef.id("textures/environment/abyssal_wasteland_sky.png"),
            tint(ClientVars::abyssalWastelandR, ClientVars::abyssalWastelandG, ClientVars::abyssalWastelandB),
            ClientFxConfig.Dimension.ABYSSAL_WASTELAND));
        // Dreadlands: constant dark-red fog + thick fog; tinted skybox.
        sink.register(ACRef.id("dreadlands"), new ACDimensionEffects(ACDimensionEffects.DREADLANDS_FOG, true,
            ACRef.id("textures/environment/dreadlands_sky.png"),
            tint(ClientVars::dreadlandsR, ClientVars::dreadlandsG, ClientVars::dreadlandsB),
            ClientFxConfig.Dimension.DREADLANDS));
        // Dark Realm: constant very-dark fog, no thick fog; reuses Omothol's skybox with its own tint.
        sink.register(ACRef.id("dark_realm"), new ACDimensionEffects(ACDimensionEffects.DARK_FOG, false,
            ACRef.id("textures/environment/omothol_sky.png"),
            tint(ClientVars::darkRealmR, ClientVars::darkRealmG, ClientVars::darkRealmB),
            ClientFxConfig.Dimension.DARK_REALM));
        // Omothol: constant very-dark fog + thick fog; tinted skybox.
        sink.register(ACRef.id("omothol"), new ACDimensionEffects(ACDimensionEffects.DARK_FOG, true,
            ACRef.id("textures/environment/omothol_sky.png"),
            tint(ClientVars::omotholR, ClientVars::omotholG, ClientVars::omotholB),
            ClientFxConfig.Dimension.OMOTHOL));
    }

    /** Build a live tint that reads the current {@link ClientVars} each frame (hot-reload friendly). */
    private static ACDimensionEffects.SkyTint tint(ToIntFunction<ClientVars> red,
                                                   ToIntFunction<ClientVars> green,
                                                   ToIntFunction<ClientVars> blue) {
        return new ACDimensionEffects.SkyTint() {
            @Override
            public int red() {
                return red.applyAsInt(ClientVarsManager.get());
            }

            @Override
            public int green() {
                return green.applyAsInt(ClientVarsManager.get());
            }

            @Override
            public int blue() {
                return blue.applyAsInt(ClientVarsManager.get());
            }
        };
    }
}
