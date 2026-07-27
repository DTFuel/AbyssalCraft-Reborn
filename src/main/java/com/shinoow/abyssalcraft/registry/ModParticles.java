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
 * <p>Scope note: the 1.12.2 mod had three custom particles -- {@code ACParticleFX} (soft white fade),
 * {@code BlueFlameParticle} (a tinted ritual flame) and {@code PEStreamParticleFX} / {@code ItemRitualParticle}
 * (coloured / item-icon particles carrying per-instance data). {@link #ABYSSAL_FX} and {@link #BLUE_FLAME} are
 * concrete no-payload {@link SimpleParticleType}s here; the coloured PE stream reuses vanilla {@code dust} and
 * the item-ritual particle reuses vanilla {@code item} (both carry data through the vanilla payload), so no
 * further custom particle types are needed (RR-CLIENT-FX).
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
}
