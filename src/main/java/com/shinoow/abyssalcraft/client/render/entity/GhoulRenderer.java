package com.shinoow.abyssalcraft.client.render.entity;

import com.shinoow.abyssalcraft.client.model.entity.GhoulModel;
import com.shinoow.abyssalcraft.client.render.entity.layers.SimpleEyesLayer;
import com.shinoow.abyssalcraft.client.render.entity.layers.GhoulArmorLayer;
import com.shinoow.abyssalcraft.client.render.entity.layers.DreadCarrierTentacleLayer;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Ghoul entity renderer (owned by PE-3, Stage E2). One {@link MobRenderer} over {@link GhoulModel},
 * registered for all five ghoul entities with their per-entity base + glowing-eyes textures passed in
 * (via {@code ACEntityRenderers}). Replaces the E1 placeholder cube for the ghoul family. Faithful
 * armor / held-item / custom-head layers (1.12.2 {@code LayerGhoulArmor} / {@code LayerGhoulHeldItem} /
 * {@code LayerCustomHead}) are deferred (armor -> PE-5).
 */
public class GhoulRenderer<T extends Mob> extends MobRenderer<T, GhoulModel<T>> {

    private final ResourceLocation texture;
    private final boolean brightnessAlpha;

    public GhoulRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation eyes,
                         boolean brightnessAlpha, boolean dreadCarrier) {
        super(context, new GhoulModel<>(context.bakeLayer(ModModelLayers.GHOUL)), 0.5F);
        this.texture = texture;
        this.brightnessAlpha = brightnessAlpha;
        if (eyes != null) {
            addLayer(new SimpleEyesLayer<>(this, eyes));
        }
        addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        addLayer(new GhoulArmorLayer<>(this, context.getModelSet()));
        if (dreadCarrier) {
            addLayer(new DreadCarrierTentacleLayer<>(this, context.getModelSet(), 0.0F, -2.5F, 0.0F));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }

    @Override
    protected RenderType getRenderType(T entity, boolean bodyVisible,
                                       boolean translucent, boolean glowing) {
        if (!brightnessAlpha) return super.getRenderType(entity, bodyVisible, translucent, glowing);
        if (bodyVisible || translucent) return RenderType.entityTranslucent(texture);
        return glowing ? RenderType.outline(texture) : null;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        model.setRenderAlpha(brightnessAlpha ? entity.getLightLevelDependentMagicValue() : 1.0F);
        try {
            super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
        } finally {
            model.setRenderAlpha(1.0F);
        }
    }
}
