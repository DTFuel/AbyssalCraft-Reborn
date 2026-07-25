package com.shinoow.abyssalcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Modern render of the 1.12.2 {@code ACParticleFX}: a soft spark that grows in quickly, drifts with
 * friction and no gravity, then fades out (PH-3 / Stage H1). <b>Client-only</b>.
 *
 * <p>The 1.12.2 class drew directly through a {@code BufferBuilder} against the vanilla particle atlas;
 * that immediate-mode path no longer exists, so this is rebuilt on {@link TextureSheetParticle} (the
 * modern sprite-driven base), which is identical on 1.20.1 and 1.21 -- hence fork-free. The 0.1x initial
 * velocity, {@code 0.75} base scale, the {@code age/lifetime*32} grow-in ramp and the ground-friction
 * behaviour all mirror the original.
 */
public class ACFadeParticle extends TextureSheetParticle {

    private final SpriteSet sprites;
    private final float baseScale;

    protected ACFadeParticle(ClientLevel level, double x, double y, double z,
                             double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = vx * 0.10000000149011612D;
        this.yd = vy * 0.10000000149011612D;
        this.zd = vz * 0.10000000149011612D;
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.quadSize *= 0.75F;
        this.baseScale = this.quadSize;
        this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
        this.hasPhysics = true;
        setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
            float t = (float) age / (float) lifetime;
            // 1.12.2 ramped scale to full over the first 1/32 of life, then held it.
            this.quadSize = baseScale * Mth.clamp(t * 32.0F, 0.0F, 1.0F);
            this.alpha = 1.0F - t;
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
            return new ACFadeParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
