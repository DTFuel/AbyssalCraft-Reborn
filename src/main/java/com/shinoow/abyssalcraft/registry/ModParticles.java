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
 * {@code PEStreamParticleFX} (a coloured fade, effectively vanilla {@code dust}) and {@code ItemRitualParticle}
 * (an item-icon particle, effectively vanilla {@code item}). Only {@code ACParticleFX} is ported here as a
 * concrete {@link #ABYSSAL_FX} type (spawn-testable via {@code /particle abyssalcraft:abyssal_fx}); the other
 * two carry per-instance data (colour / {@code ItemStack}) and are best rebuilt alongside the ritual / Purified
 * Essence content that emits them (currently unported), so they are deferred with that content.
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
}
