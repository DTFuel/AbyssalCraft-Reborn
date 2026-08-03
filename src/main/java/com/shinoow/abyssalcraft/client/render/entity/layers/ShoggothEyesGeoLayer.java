package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.client.render.entity.ShoggothGeoRenderer;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class ShoggothEyesGeoLayer extends GeoRenderLayer<AbstractShoggoth> {

    public ShoggothEyesGeoLayer(GeoRenderer<AbstractShoggoth> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, AbstractShoggoth entity, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource buffers, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        if (entity.isInvisible()) return;
        //? if >=1.21 {
        /*RenderType eyesType = RenderType.entityTranslucentEmissive(
            ShoggothGeoRenderer.texture(entity.getShoggothType(), true));
        VertexConsumer eyesBuffer = buffers.getBuffer(eyesType);
        getRenderer().reRender(bakedModel, poseStack, buffers, entity, eyesType, eyesBuffer,
            partialTick, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        *///?} else {
        RenderType eyesType = RenderType.eyes(ShoggothGeoRenderer.texture(entity.getShoggothType(), true));
        VertexConsumer eyesBuffer = buffers.getBuffer(eyesType);
        getRenderer().reRender(bakedModel, poseStack, buffers, entity, eyesType, eyesBuffer,
            partialTick, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        //?}
    }
}