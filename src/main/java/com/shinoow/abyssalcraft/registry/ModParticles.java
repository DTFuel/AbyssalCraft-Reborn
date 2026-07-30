package com.shinoow.abyssalcraft.registry;

import java.util.function.Supplier;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Particle-type registry (owned by PH-3 / Stage H1). Fork-free: {@link Registries#PARTICLE_TYPE} and
 * {@link SimpleParticleType} are identical on 1.20.1 and 1.21, so this is a plain {@link ModRegistrar}
 * attached via {@code ModRegistries.ALL}. The client provider (which needs a {@code SpriteSet}) is wired
 * separately through {@link com.shinoow.abyssalcraft.platform.ParticleCompat} on the client only.
 *
 * <p>The three no-payload types retain distinct legacy physics: white AC sparks, blue ritual flames and the
 * coloured PE stream. Item ritual particles continue to use the vanilla item payload.
 */
public final class ModParticles {

    private ModParticles() {}

    /** {@code minecraft:particle_type} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<ParticleType<?>> PARTICLES =
        ModRegistrar.of(Registries.PARTICLE_TYPE, AbyssalCraft.MODID);

    /**
     * Soft fading spark (faithful to 1.12.2 {@code ACParticleFX}). {@code new SimpleParticleType(false){}}
     * uses an anonymous subclass because the vanilla constructor is {@code protected}.
     */
    public static final Supplier<SimpleParticleType> ABYSSAL_FX =
        PARTICLES.register("abyssal_fx", () -> new SimpleParticleType(false) {});

    /**
     * Blue ritual flame (faithful to 1.12.2 {@code BlueFlameParticle}), spawned from ritual pedestals while an
     * altar ceremony runs. A no-payload {@link SimpleParticleType} emitted client-side by
     * {@link com.shinoow.abyssalcraft.client.ritual.ClientRitualEffects}.
     */
    public static final Supplier<SimpleParticleType> BLUE_FLAME =
        PARTICLES.register("blue_flame", () -> new SimpleParticleType(false) {});

    /** Coloured Potential Energy stream, faithful to 1.12.2 {@code PEStreamParticleFX}. */
    public static final Supplier<SimpleParticleType> PE_STREAM =
        PARTICLES.register("pe_stream", () -> new SimpleParticleType(false) {});
}
