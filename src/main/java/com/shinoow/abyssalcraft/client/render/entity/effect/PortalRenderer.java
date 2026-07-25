package com.shinoow.abyssalcraft.client.render.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.content.entity.misc.DimensionPortal;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ModelPartRenderCompat;
import com.shinoow.abyssalcraft.registry.ModModelLayers;
import com.shinoow.abyssalcraft.system.portal.DimensionData;

/** Destination-coloured, rotating portal with the optional dimension overlay. */
public final class PortalRenderer extends EntityRenderer<DimensionPortal> {

    private static final ResourceLocation BASE_TEXTURE = ACRef.id("textures/model/portal.png");
    private static final int FULL_BRIGHT = 15728880;

    private final ModelPart quad;

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        quad = context.bakeLayer(ModModelLayers.BILLBOARD);
    }

    @Override
    public void render(DimensionPortal portal, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float age = portal.tickCount + partialTick;
        float baseScale = portal.isSingleUse() ? 1.2F : 2.0F;
        float appearDuration = portal.isSingleUse() ? 45.0F : 30.0F;
        float appear = Math.min(1.0F, Math.max(0.05F, age / appearDuration));
        float wave = (float) Math.sin(age / 10.0F) * 0.05F;
        float xScale = baseScale * appear * (1.0F + wave);
        float yScale = baseScale * 1.5F * appear * (1.0F - wave);
        DimensionData data = portal.getDimensionData();
        int color = data == null ? 0xFFFFFFFF : data.color();

        poseStack.pushPose();
        poseStack.translate(0.0F, portal.getBbHeight() * 0.5F, 0.0F);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotationDegrees(age));
        poseStack.scale(xScale, yScale, xScale);

        VertexConsumer base = buffer.getBuffer(RenderType.entityTranslucentEmissive(BASE_TEXTURE));
        ModelPartRenderCompat.render(quad, poseStack, base, FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY, color);
        if (data != null && data.overlay().isPresent()) {
            VertexConsumer overlay = buffer.getBuffer(
                RenderType.entityTranslucentEmissive(data.overlay().get()));
            ModelPartRenderCompat.render(quad, poseStack, overlay, FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
        poseStack.popPose();
        super.render(portal, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DimensionPortal portal) {
        return BASE_TEXTURE;
    }
}