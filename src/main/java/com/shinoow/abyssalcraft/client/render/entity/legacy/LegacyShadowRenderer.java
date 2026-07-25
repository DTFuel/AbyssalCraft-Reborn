package com.shinoow.abyssalcraft.client.render.entity.legacy;

import com.shinoow.abyssalcraft.client.render.entity.layers.SimpleEyesLayer;
import com.shinoow.abyssalcraft.client.model.entity.BrightnessAlphaModel;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;

public final class LegacyShadowRenderer<M extends BrightnessAlphaModel<LegacyHostileMob>>
        extends MobRenderer<LegacyHostileMob, M> {

    private final ResourceLocation texture;

    public LegacyShadowRenderer(EntityRendererProvider.Context context, M model, ResourceLocation texture,
                                ResourceLocation eyes) {
        super(context, model, 0.0F);
        this.texture = texture;
        addLayer(new SimpleEyesLayer<>(this, eyes));
        if (model instanceof HeadedModel) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            CustomHeadLayer raw = new CustomHeadLayer(this, context.getModelSet(), context.getItemInHandRenderer());
            addLayer(raw);
        }
    }

    @Override
    protected RenderType getRenderType(LegacyHostileMob entity, boolean bodyVisible,
                                       boolean translucent, boolean glowing) {
        if (bodyVisible || translucent) return RenderType.entityTranslucent(texture);
        return glowing ? RenderType.outline(texture) : null;
    }

    @Override
    public void render(LegacyHostileMob entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        model.setRenderAlpha(entity.getLightLevelDependentMagicValue());
        try {
            super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
        } finally {
            model.setRenderAlpha(1.0F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyHostileMob entity) {
        return texture;
    }
}