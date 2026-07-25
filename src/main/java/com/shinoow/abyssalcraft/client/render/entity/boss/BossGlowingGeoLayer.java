package com.shinoow.abyssalcraft.client.render.entity.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.content.entity.boss.BossKind;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class BossGlowingGeoLayer extends GeoRenderLayer<BossMob> {

    public BossGlowingGeoLayer(GeoRenderer<BossMob> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, BossMob boss, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource buffers, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        RenderType glowType = AutoGlowingTexture.getRenderType(getTextureResource(boss));
        VertexConsumer glowBuffer = buffers.getBuffer(glowType);
        float alpha = boss.kind() == BossKind.SACTHOTH
            ? boss.getLightLevelDependentMagicValue() : 1.0F;
        //? if >=1.21 {
        /*int color = Math.round(alpha * 255.0F) << 24 | 0xFFFFFF;
        getRenderer().reRender(bakedModel, poseStack, buffers, boss, glowType, glowBuffer,
            partialTick, 15728640, packedOverlay, color);
        *///?} else {
        getRenderer().reRender(bakedModel, poseStack, buffers, boss, glowType, glowBuffer,
            partialTick, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);
        //?}
    }
}
