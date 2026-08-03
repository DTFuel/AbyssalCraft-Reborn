package com.shinoow.abyssalcraft.client.render.entity.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

final class LegacyShadowEyesGeoLayer extends GeoRenderLayer<LegacyHostileMob> {

    private final ResourceLocation texture;

    LegacyShadowEyesGeoLayer(GeoRenderer<LegacyHostileMob> renderer, ResourceLocation texture) {
        super(renderer);
        this.texture = texture;
    }

    @Override
    public void render(PoseStack poseStack, LegacyHostileMob entity, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource buffers, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        if (entity.isInvisible()) return;
        //? if >=1.21 {
        /*RenderType eyesType = RenderType.entityTranslucentEmissive(texture);
        VertexConsumer eyesBuffer = buffers.getBuffer(eyesType);
        getRenderer().reRender(bakedModel, poseStack, buffers, entity, eyesType, eyesBuffer,
            partialTick, 15728640, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        *///?} else {
        RenderType eyesType = RenderType.eyes(texture);
        VertexConsumer eyesBuffer = buffers.getBuffer(eyesType);
        getRenderer().reRender(bakedModel, poseStack, buffers, entity, eyesType, eyesBuffer,
            partialTick, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        //?}
    }
}