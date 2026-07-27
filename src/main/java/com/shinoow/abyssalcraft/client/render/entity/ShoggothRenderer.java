package com.shinoow.abyssalcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import com.shinoow.abyssalcraft.client.model.entity.ShoggothModel;
import com.shinoow.abyssalcraft.client.render.entity.layers.SimpleEyesLayer;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.content.entity.shoggoth.GreaterShoggoth;
import com.shinoow.abyssalcraft.content.entity.shoggoth.LesserShoggoth;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Shoggoth entity renderer (owned by PE-3, Stage E2). One {@link MobRenderer} over {@link ShoggothModel},
 * registered for the three shoggoth entities with their per-entity base + glowing-eyes textures passed in
 * (via {@code ACEntityRenderers}). Replaces the E1 placeholder cube for the shoggoth family. The 1.12.2
 * semi-transparent shadow-dimension variant is deferred (all render opaque here).
 *
 * <p>{@link #scale} reproduces the 1.12.2 per-size model scaling (lesser 0.75x, shoggoth 1.0x, greater
 * 1.5x) so each variant's mesh matches its differently-sized hitbox.
 */
public class ShoggothRenderer extends MobRenderer<AbstractShoggoth, ShoggothModel> {

    private final ResourceLocation texture;

    public ShoggothRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation eyes) {
        super(context, new ShoggothModel(context.bakeLayer(ModModelLayers.SHOGGOTH)), 0.7F);
        this.texture = texture;
        if (eyes != null && ACConfig.shoggothGlowingEyes.get()) {
            addLayer(new SimpleEyesLayer<>(this, eyes));
        }
    }

    @Override
    protected void scale(AbstractShoggoth entity, PoseStack poseStack, float partialTick) {
        float scale = entity instanceof LesserShoggoth ? 0.75F : entity instanceof GreaterShoggoth ? 1.5F : 1.0F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractShoggoth entity) {
        return this.texture;
    }
}
