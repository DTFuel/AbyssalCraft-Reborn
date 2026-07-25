package com.shinoow.abyssalcraft.platform;

import java.util.function.Consumer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
*///?}

/**
 * Compat: per-dimension {@link DimensionSpecialEffects} registration (loader axis). <b>Client-only</b>.
 *
 * <p>Both loaders publish the same {@code RegisterDimensionSpecialEffectsEvent} on the MOD bus with an
 * identical {@code register(ResourceLocation, DimensionSpecialEffects)} method; only the package differs
 * (Forge {@code net.minecraftforge.client.event} vs NeoForge {@code net.neoforged.neoforge.client.event}).
 * So only the import forks -- the body is loader-neutral. The business relay
 * ({@link com.shinoow.abyssalcraft.client.sky.ACDimensionSkies}) receives the fork-free {@link Effects}
 * sink and never imports the loader event.
 *
 * <p>The registered id must match the {@code "effects"} field of the matching {@code dimension_type} JSON
 * ({@code abyssalcraft:abyssal_wasteland} / {@code dreadlands} / {@code dark_realm} / {@code omothol}).
 *
 * <p>Never referenced on a dedicated server: the main class attaches it inside
 * {@link SideExecutor#runWhenClient}, so this class (and the client sky package) never load there.
 */
public final class DimensionEffectsCompat {

    private DimensionEffectsCompat() {}

    /** Loader-neutral dimension-effects sink. */
    public interface Effects {
        void register(ResourceLocation key, DimensionSpecialEffects effects);
    }

    /**
     * Attach the client dimension-effects listener to the MOD bus. The callback fires inside the
     * {@code RegisterDimensionSpecialEffectsEvent} with a fork-free sink.
     */
    public static void attach(IEventBus modBus, Consumer<Effects> registrar) {
        modBus.addListener((RegisterDimensionSpecialEffectsEvent event) ->
            registrar.accept(event::register));
    }
}
