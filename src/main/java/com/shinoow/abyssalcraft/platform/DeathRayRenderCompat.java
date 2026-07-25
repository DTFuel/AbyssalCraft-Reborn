package com.shinoow.abyssalcraft.platform;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public final class DeathRayRenderCompat {

    private static final float HALF_SQRT_3 = 0.8660254F;

    private DeathRayRenderCompat() {}

    public static void render(PoseStack poseStack, MultiBufferSource buffers, float progress,
                              int maxRays, int red, int green, int blue,
                              float lengthFadeScale, float radiusFadeScale, boolean clampFade) {
        float fade = progress > 0.8F ? (progress - 0.8F) / 0.2F : 0.0F;
        if (clampFade) fade = Mth.clamp(fade, 0.0F, 1.0F);
        int centerAlpha = (int) (255.0F * (1.0F - fade)) & 255;
        int count = (int) Math.ceil((progress + progress * progress) * 0.5F * maxRays);
        Random random = new Random(432L);

        //? if >=1.21 {
        /*renderPass(poseStack, buffers.getBuffer(RenderType.dragonRays()), random, progress,
            fade, count, red, green, blue, centerAlpha, lengthFadeScale, radiusFadeScale);
        random = new Random(432L);
        renderPass(poseStack, buffers.getBuffer(RenderType.dragonRaysDepth()), random, progress,
            fade, count, red, green, blue, centerAlpha, lengthFadeScale, radiusFadeScale);
        *///?} else {
        renderPass(poseStack, buffers.getBuffer(RenderType.lightning()), random, progress,
            fade, count, red, green, blue, centerAlpha, lengthFadeScale, radiusFadeScale);
        //?}
    }

    private static void renderPass(PoseStack poseStack, VertexConsumer consumer, Random random,
                                   float progress, float fade, int count, int red, int green, int blue,
                                   int centerAlpha, float lengthFadeScale, float radiusFadeScale) {
        poseStack.pushPose();
        for (int index = 0; index < count; index++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + progress * 90.0F));

            float length = random.nextFloat() * 20.0F + 5.0F + fade * lengthFadeScale;
            float radius = random.nextFloat() * 2.0F + 1.0F + fade * radiusFadeScale;
            float leftX = -HALF_SQRT_3 * radius;
            float rightX = HALF_SQRT_3 * radius;
            float rearZ = -0.5F * radius;

            //? if >=1.21 {
            /*vertex(consumer, poseStack, 0.0F, 0.0F, 0.0F, 255, 255, 255, centerAlpha);
            vertex(consumer, poseStack, leftX, length, rearZ, red, green, blue, 0);
            vertex(consumer, poseStack, rightX, length, rearZ, red, green, blue, 0);
            vertex(consumer, poseStack, 0.0F, 0.0F, 0.0F, 255, 255, 255, centerAlpha);
            vertex(consumer, poseStack, rightX, length, rearZ, red, green, blue, 0);
            vertex(consumer, poseStack, 0.0F, length, radius, red, green, blue, 0);
            vertex(consumer, poseStack, 0.0F, 0.0F, 0.0F, 255, 255, 255, centerAlpha);
            vertex(consumer, poseStack, 0.0F, length, radius, red, green, blue, 0);
            vertex(consumer, poseStack, leftX, length, rearZ, red, green, blue, 0);
            *///?} else {
            vertex(consumer, poseStack, 0.0F, 0.0F, 0.0F, 255, 255, 255, centerAlpha);
            vertex(consumer, poseStack, leftX, length, rearZ, red, green, blue, 0);
            vertex(consumer, poseStack, rightX, length, rearZ, red, green, blue, 0);
            vertex(consumer, poseStack, 0.0F, length, radius, red, green, blue, 0);
            //?}
        }
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, PoseStack poseStack, float x, float y, float z,
                               int red, int green, int blue, int alpha) {
        //? if >=1.21 {
        /*consumer.addVertex(poseStack.last(), x, y, z).setColor(red, green, blue, alpha);
        *///?} else {
        consumer.vertex(poseStack.last().pose(), x, y, z).color(red, green, blue, alpha).endVertex();
        //?}
    }
}