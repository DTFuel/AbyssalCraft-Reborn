package com.shinoow.abyssalcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Modern render of the 1.12.2 {@code BlueFlameParticle}: a small blue flame that rises slowly, decays with
 * friction and shrinks as it ages (RR-CLIENT-FX). <b>Client-only</b>.
 *
 * <p>The 1.12.2 class extended {@code ParticleFlame}; the modern {@code FlameParticle} has a package-private
 * constructor and cannot be subclassed here, so this rebuilds the same rising/shrinking behaviour on
 * {@link TextureSheetParticle} (identical on 1.20.1 and 1.21, hence fork-free) using vanilla
 * {@code RisingParticle}/{@code FlameParticle}'s motion (manual {@code 0.01} velocity seed + {@code 0.96}
 * friction), lifetime ({@code 8 / rand + 4}) and quad-size falloff ({@code 1 - f*f*0.5}).
 */
public class BlueFlameParticle extends TextureSheetParticle {

    protected BlueFlameParticle(ClientLevel level, double x, double y, double z,
                                double vx, double vy, double vz) {
        super(level, x, y, z);
        // Faithful RisingParticle seeding: no velocity normalisation, tiny jitter, slow rise.
        this.xd = this.xd * 0.01D + vx;
        this.yd = this.yd * 0.01D + vy;
        this.zd = this.zd * 0.01D + vz;
        this.x += (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.y += (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.z += (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
        this.gravity = 0.0F;
        this.hasPhysics = false;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float f = ((float) this.age + scaleFactor) / (float) this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.96D;
        this.yd *= 0.96D;
        this.zd *= 0.96D;
        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }
    }

    /** Client factory bound to the sprite set in {@code RegisterParticleProvidersEvent}. */
    public static final class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz) {
            BlueFlameParticle particle = new BlueFlameParticle(level, x, y, z, vx, vy, vz);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
