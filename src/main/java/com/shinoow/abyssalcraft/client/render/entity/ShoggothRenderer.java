package com.shinoow.abyssalcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import com.shinoow.abyssalcraft.client.model.entity.ShoggothModel;
import com.shinoow.abyssalcraft.client.render.entity.layers.ShoggothEyesLayer;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.content.entity.shoggoth.GreaterShoggoth;
import com.shinoow.abyssalcraft.content.entity.shoggoth.LesserShoggoth;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Shoggoth entity renderer (owned by PE-3, Stage E2). One {@link MobRenderer} over {@link ShoggothModel},
 * registered for the three shoggoth entities with their per-entity base + glowing-eyes textures passed in
 * (via {@code ACEntityRenderers}). Replaces the E1 placeholder cube for the shoggoth family. The 1.12.2
 * <p>{@link #scale} reproduces the 1.12.2 per-size model scaling (lesser 0.75x, shoggoth 1.0x, greater
 * 1.5x) so each variant's mesh matches its differently-sized hitbox.
 */
public class ShoggothRenderer extends MobRenderer<AbstractShoggoth, ShoggothModel> {

    private static final ResourceLocation LESSER_TEXTURE = texture("lessershoggoth");
    private static final ResourceLocation ABYSSAL_TEXTURE = texture("abyssalshoggoth");
    private static final ResourceLocation DREADED_TEXTURE = texture("dreadedshoggoth");
    private static final ResourceLocation OMOTHOL_TEXTURE = texture("omotholshoggoth");
    private static final ResourceLocation SHADOW_TEXTURE = texture("shadowshoggoth");

    public ShoggothRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation eyes) {
        super(context, new ShoggothModel(context.bakeLayer(ModModelLayers.SHOGGOTH)), 0.7F);
        if (eyes != null && ACConfig.shoggothGlowingEyes.get()) {
            addLayer(new ShoggothEyesLayer(this));
        }
    }

    @Override
    protected RenderType getRenderType(AbstractShoggoth entity, boolean bodyVisible,
                                       boolean translucent, boolean glowing) {
        if (entity.getShoggothType() != 4) {
            return super.getRenderType(entity, bodyVisible, translucent, glowing);
        }
        if (bodyVisible || translucent) return RenderType.entityTranslucent(SHADOW_TEXTURE);
        return glowing ? RenderType.outline(SHADOW_TEXTURE) : null;
    }

    @Override
    public void render(AbstractShoggoth entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        boolean shadow = entity.getShoggothType() == 4;
        model.setRenderAlpha(shadow ? entity.getLightLevelDependentMagicValue() : 1.0F);
        try {
            super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
        } finally {
            model.setRenderAlpha(1.0F);
        }
    }

    @Override
    protected void scale(AbstractShoggoth entity, PoseStack poseStack, float partialTick) {
        float scale = entity instanceof LesserShoggoth ? 0.75F : entity instanceof GreaterShoggoth ? 1.5F : 1.0F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractShoggoth entity) {
        return switch (entity.getShoggothType()) {
            case 1 -> ABYSSAL_TEXTURE;
            case 2 -> DREADED_TEXTURE;
            case 3 -> OMOTHOL_TEXTURE;
            case 4 -> SHADOW_TEXTURE;
            default -> LESSER_TEXTURE;
        };
    }

    private static ResourceLocation texture(String name) {
        return ACRef.id("textures/model/shoggoth/" + name + ".png");
    }
}
