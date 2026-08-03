package com.shinoow.abyssalcraft.client.render.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import com.shinoow.abyssalcraft.content.entity.misc.CompassTentacle;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class CompassTentacleRenderer extends GeoEntityRenderer<CompassTentacle> {

    public CompassTentacleRenderer(EntityRendererProvider.Context context) {
        super(context, new CompassTentacleGeoModel());
        this.shadowRadius = 0.25F;
    }

    @Override
    protected void applyRotations(CompassTentacle entity, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw, float partialTick) {}

    //? if >=1.21 {
    /*@Override
    public void preRender(PoseStack poseStack, CompassTentacle entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                          int renderColor) {
        applyLegacyTransform(poseStack, entity);
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
            packedLight, packedOverlay, renderColor);
    }
    *///?} else {
    @Override
    public void preRender(PoseStack poseStack, CompassTentacle entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        applyLegacyTransform(poseStack, entity);
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
            packedLight, packedOverlay, red, green, blue, alpha);
    }
    //?}

    private static void applyLegacyTransform(PoseStack poseStack, CompassTentacle entity) {
        poseStack.translate(0.1F, 1.3F, -0.1F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        poseStack.scale(2.4F, 2.4F, 2.4F);
    }
}