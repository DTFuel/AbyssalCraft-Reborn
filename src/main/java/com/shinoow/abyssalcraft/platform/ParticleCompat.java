package com.shinoow.abyssalcraft.platform;

import java.util.function.Consumer;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
*///?}

/**
 * Compat: client particle-provider registration (loader axis). <b>Client-only</b>.
 *
 * <p>Both loaders publish the same {@code RegisterParticleProvidersEvent} on the MOD bus with an identical
 * {@code registerSpriteSet(ParticleType, ParticleEngine.SpriteParticleRegistration)} method; only the package
 * differs (Forge {@code net.minecraftforge.client.event} vs NeoForge {@code net.neoforged.neoforge.client.event}).
 * So only the import forks -- the body is loader-neutral. The business relay ({@code ACClientSetup}) receives
 * the fork-free {@link Providers} sink and never imports the loader event.
 *
 * <p>Never referenced on a dedicated server: the main class attaches it inside
 * {@link SideExecutor#runWhenClient}, so this class (and the client particle package) never load there.
 */
public final class ParticleCompat {

    private ParticleCompat() {}

    /** Loader-neutral sprite-particle-provider sink. */
    public interface Providers {
        <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type,
                                                            ParticleEngine.SpriteParticleRegistration<T> registration);
    }

    /**
     * Attach the client particle-provider listener to the MOD bus. The callback fires inside the
     * {@code RegisterParticleProvidersEvent} with a fork-free sink.
     */
    public static void attach(IEventBus modBus, Consumer<Providers> registrar) {
        modBus.addListener((RegisterParticleProvidersEvent event) ->
            registrar.accept(new Providers() {
                @Override
                public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type,
                                                                          ParticleEngine.SpriteParticleRegistration<T> registration) {
                    event.registerSpriteSet(type, registration);
                }
            }));
    }
}
