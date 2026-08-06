package com.shinoow.abyssalcraft.client.render.effect;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shinoow.abyssalcraft.platform.LineRenderCompat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

/** Batches short-lived, faceted world-space beams rendered by the line core shader. */
public final class LineEffectRenderer {

    private static final int MAX_EFFECTS = 128;
    private static final int BEAM_SIDES = 8;
    private static final double MAX_LENGTH_SQR = 128.0D * 128.0D;
    private static final float BEAM_RADIUS = 0.055F;
    private static final Vec3 LIGHT_DIRECTION = new Vec3(0.35D, 0.8D, -0.45D).normalize();
    private static final List<LineEffect> EFFECTS = new ArrayList<>();

    private LineEffectRenderer() {}

    public static void add(Vec3 start, Vec3 end, int startColor, int endColor, int durationTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !finite(start) || !finite(end)
            || start.distanceToSqr(end) < 1.0E-6D || start.distanceToSqr(end) > MAX_LENGTH_SQR) return;
        if (EFFECTS.size() >= MAX_EFFECTS) EFFECTS.remove(0);
        EFFECTS.add(new LineEffect(level, start, end, startColor, endColor,
            Math.max(1, Math.min(40, durationTicks)), level.getGameTime()));
    }

    public static void render(PoseStack poseStack, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || EFFECTS.isEmpty() || !LineRenderCompat.ready()) return;

        float renderTime = level.getGameTime() + partialTick;
        EFFECTS.removeIf(effect -> effect.level() != level
            || renderTime - effect.startedAt() >= effect.durationTicks());
        if (EFFECTS.isEmpty()) return;

        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(LineRenderCompat.lineType());

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        for (LineEffect effect : EFFECTS) {
            float age = renderTime - effect.startedAt();
            renderLine(poseStack, consumer, effect, age / effect.durationTicks());
        }
        LineRenderCompat.endBatch(buffers);
        poseStack.popPose();
    }

    private static void renderLine(PoseStack poseStack, VertexConsumer consumer,
                                   LineEffect effect, float progress) {
        Vec3 direction = effect.end().subtract(effect.start()).normalize();
        Vec3 reference = Math.abs(direction.y) < 0.9D
            ? new Vec3(0.0D, 1.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 axisA = direction.cross(reference).normalize();
        Vec3 axisB = direction.cross(axisA).normalize();
        double radius = BEAM_RADIUS * (1.0F - progress * 0.25F);
        int startAlpha = Math.round((effect.startColor() >>> 24) * fade(progress));
        int endAlpha = Math.round((effect.endColor() >>> 24) * fade(progress));

        for (int side = 0; side < BEAM_SIDES; side++) {
            double angleA = Math.PI * 2.0D * side / BEAM_SIDES;
            double angleB = Math.PI * 2.0D * (side + 1) / BEAM_SIDES;
            Vec3 radialA = radial(axisA, axisB, angleA);
            Vec3 radialB = radial(axisA, axisB, angleB);
            Vec3 faceNormal = radialA.add(radialB).normalize();
            float shade = 0.68F + 0.32F * (float) Math.max(0.0D, faceNormal.dot(LIGHT_DIRECTION));
            int startRed = shade(effect.startColor() >> 16 & 0xFF, shade);
            int startGreen = shade(effect.startColor() >> 8 & 0xFF, shade);
            int startBlue = shade(effect.startColor() & 0xFF, shade);
            int endRed = shade(effect.endColor() >> 16 & 0xFF, shade);
            int endGreen = shade(effect.endColor() >> 8 & 0xFF, shade);
            int endBlue = shade(effect.endColor() & 0xFF, shade);
            Vec3 startA = effect.start().add(radialA.scale(radius));
            Vec3 startB = effect.start().add(radialB.scale(radius));
            Vec3 endB = effect.end().add(radialB.scale(radius));
            Vec3 endA = effect.end().add(radialA.scale(radius));

            LineRenderCompat.vertex(consumer, poseStack.last().pose(), startA,
                startRed, startGreen, startBlue, startAlpha, 0.0F, 0.0F);
            LineRenderCompat.vertex(consumer, poseStack.last().pose(), startB,
                startRed, startGreen, startBlue, startAlpha, 0.0F, 1.0F);
            LineRenderCompat.vertex(consumer, poseStack.last().pose(), endB,
                endRed, endGreen, endBlue, endAlpha, 1.0F, 1.0F);
            LineRenderCompat.vertex(consumer, poseStack.last().pose(), endA,
                endRed, endGreen, endBlue, endAlpha, 1.0F, 0.0F);
        }
        renderCap(poseStack, consumer, effect.start(), axisA, axisB, radius,
            effect.startColor(), startAlpha, true);
        renderCap(poseStack, consumer, effect.end(), axisA, axisB, radius,
            effect.endColor(), endAlpha, false);
    }

    private static void renderCap(PoseStack poseStack, VertexConsumer consumer, Vec3 center,
                                  Vec3 axisA, Vec3 axisB, double radius,
                                  int color, int alpha, boolean reverse) {
        for (int side = 0; side < BEAM_SIDES; side++) {
            double angleA = Math.PI * 2.0D * side / BEAM_SIDES;
            double angleB = Math.PI * 2.0D * (side + 1) / BEAM_SIDES;
            Vec3 edgeA = center.add(radial(axisA, axisB, angleA).scale(radius));
            Vec3 edgeB = center.add(radial(axisA, axisB, angleB).scale(radius));
            if (reverse) {
                Vec3 swap = edgeA;
                edgeA = edgeB;
                edgeB = swap;
            }
            int red = color >> 16 & 0xFF;
            int green = color >> 8 & 0xFF;
            int blue = color & 0xFF;
            LineRenderCompat.vertex(consumer, poseStack.last().pose(), center,
                red, green, blue, alpha, 0.5F, 0.5F);
            LineRenderCompat.vertex(consumer, poseStack.last().pose(), edgeA,
                red, green, blue, alpha, 0.0F, 0.0F);
            LineRenderCompat.vertex(consumer, poseStack.last().pose(), edgeB,
                red, green, blue, alpha, 1.0F, 0.0F);
            LineRenderCompat.vertex(consumer, poseStack.last().pose(), center,
                red, green, blue, alpha, 0.5F, 0.5F);
        }
    }

    private static Vec3 radial(Vec3 axisA, Vec3 axisB, double angle) {
        return axisA.scale(Math.cos(angle)).add(axisB.scale(Math.sin(angle)));
    }

    private static int shade(int channel, float shade) {
        return Math.min(255, Math.round(channel * shade));
    }

    private static float fade(float progress) {
        float remaining = 1.0F - progress;
        return remaining * remaining;
    }

    private static boolean finite(Vec3 point) {
        return Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
    }

    private record LineEffect(ClientLevel level, Vec3 start, Vec3 end,
                              int startColor, int endColor, int durationTicks, long startedAt) {}
}