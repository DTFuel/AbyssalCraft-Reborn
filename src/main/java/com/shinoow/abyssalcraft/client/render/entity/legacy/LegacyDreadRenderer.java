package com.shinoow.abyssalcraft.client.render.entity.legacy;

import com.mojang.blaze3d.vertex.PoseStack;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;
import com.shinoow.abyssalcraft.client.render.entity.layers.DreadCarrierTentacleLayer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;

public final class LegacyDreadRenderer<M extends EntityModel<LegacyHostileMob>>
        extends MobRenderer<LegacyHostileMob, M> {

    private final ResourceLocation texture;
    private final float modelScale;

    public LegacyDreadRenderer(EntityRendererProvider.Context context, M model, ResourceLocation texture,
                               float shadowRadius, float modelScale,
                               float tentacleX, float tentacleY, float tentacleZ) {
        super(context, model, shadowRadius);
        this.texture = texture;
        this.modelScale = modelScale;
        if (model instanceof HeadedModel) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            CustomHeadLayer raw = new CustomHeadLayer(this, context.getModelSet(), context.getItemInHandRenderer());
            addLayer(raw);
        }
        addLayer(new DreadCarrierTentacleLayer<>(this, context.getModelSet(), tentacleX, tentacleY, tentacleZ));
    }

    @Override
    protected void scale(LegacyHostileMob entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(modelScale, modelScale, modelScale);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyHostileMob entity) {
        return texture;
    }
}