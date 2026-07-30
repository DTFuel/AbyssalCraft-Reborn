package com.shinoow.abyssalcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** Modern sprite-driven equivalent of the 1.12.2 {@code PEStreamParticleFX}. */
public final class PEStreamParticle extends TextureSheetParticle {

    private static final float[][] COLORS = {
        {65.0F / 255.0F, 63.0F / 255.0F, 170.0F / 255.0F},
        {41.0F / 255.0F, 89.0F / 255.0F, 48.0F / 255.0F},
        {39.0F / 255.0F, 80.0F / 255.0F, 135.0F / 255.0F},
    };

    private final SpriteSet sprites;
    private final float baseScale;
    private final float baseRed;
    private final float baseGreen;
    private final float baseBlue;

    private PEStreamParticle(ClientLevel level, double x, double y, double z,
                             SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        float[] color = COLORS[level.random.nextInt(COLORS.length)];
        this.baseRed = color[0];
        this.baseGreen = color[1];
        this.baseBlue = color[2];
        this.lifetime = 20;
        this.quadSize *= 0.75F;
        this.baseScale = this.quadSize;
        this.xd *= 0.10000000149011612D;
        this.yd *= 0.10000000149011612D;
        this.zd *= 0.10000000149011612D;
        this.hasPhysics = true;
        updateAppearance();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return baseScale * Mth.clamp((age + partialTick) / lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) return;
        this.xd *= 0.65D;
        this.yd *= 0.65D;
        this.zd *= 0.65D;
        if (random.nextInt(4) == 0 && age > 0) {
            age--;
        }
        updateAppearance();
    }

    private void updateAppearance() {
        int frame = Math.min(7, age * 8 / lifetime);
        setSprite(sprites.get(frame, 7));
        float life = (lifetime - age) / (float) lifetime;
        float colorScale = 1.5F - life;
        this.rCol = Math.min(1.0F, baseRed * colorScale);
        this.gCol = Math.min(1.0F, baseGreen * colorScale);
        this.bCol = Math.min(1.0F, baseBlue * colorScale);
        this.alpha = life;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new PEStreamParticle(level, x, y, z, sprites);
        }
    }
}