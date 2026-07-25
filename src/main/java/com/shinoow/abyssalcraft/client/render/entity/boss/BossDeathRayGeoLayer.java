package com.shinoow.abyssalcraft.client.render.entity.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.content.entity.boss.BossKind;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.platform.DeathRayRenderCompat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class BossDeathRayGeoLayer extends GeoRenderLayer<BossMob> {

    public BossDeathRayGeoLayer(GeoRenderer<BossMob> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, BossMob boss, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource buffers, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (boss.kind() != BossKind.JZAHAR || boss.getACDeathTime() <= 400) return;

        float progress = (boss.getACDeathTime() + partialTick) / 400.0F;
        poseStack.pushPose();
        poseStack.scale(0.25F, 0.25F, 0.25F);
        DeathRayRenderCompat.render(poseStack, buffers, progress, 30,
            81, 189, 178, 1.0F, 1.0F, false);
        poseStack.popPose();
    }
}