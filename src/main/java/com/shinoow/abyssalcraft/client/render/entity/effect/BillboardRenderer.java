package com.shinoow.abyssalcraft.client.render.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BillboardRenderer<T extends Entity> extends EntityRenderer<T> {

    private final ModelPart quad;
    private final ResourceLocation texture;
    private final float baseScale;
    private final float verticalScale;
    private final float rotationSpeed;
    private final boolean wobble;

    public BillboardRenderer(EntityRendererProvider.Context context, ResourceLocation texture, float baseScale,
                             float verticalScale, float rotationSpeed, boolean wobble) {
        super(context);
        this.quad = context.bakeLayer(ModModelLayers.BILLBOARD);
        this.texture = texture;
        this.baseScale = baseScale;
        this.verticalScale = verticalScale;
        this.rotationSpeed = rotationSpeed;
        this.wobble = wobble;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float age = entity.tickCount + partialTicks;
        float xScale = baseScale;
        float yScale = baseScale * verticalScale;
        if (wobble) {
            float wave = (float) Math.sin(age / 10.0F) * 0.05F;
            xScale *= 1.0F + wave;
            yScale *= 1.0F - wave;
        }
        if (entity.tickCount <= 30 && entity.getBbHeight() >= 1.5F) {
            float appear = Math.max(0.05F, age / 30.0F);
            xScale *= appear;
            yScale *= appear;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, entity.getBbHeight() * 0.5F, 0.0F);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotationDegrees(age * rotationSpeed));
        poseStack.scale(xScale, yScale, xScale);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        quad.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}