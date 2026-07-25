package com.shinoow.abyssalcraft.client.render.entity.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ArmorRenderCompat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class EliteArmorGeoLayer extends GeoRenderLayer<EliteMob> {

    public enum Mode {
        NONE,
        DREADGUARD
    }

    private static final ResourceLocation DREADGUARD_OVERLAY =
        ACRef.id("textures/model/elite/dreadguard_overlay.png");

    private final Mode mode;

    public EliteArmorGeoLayer(GeoRenderer<EliteMob> renderer, Mode mode) {
        super(renderer);
        this.mode = mode;
    }

    @Override
    public void renderForBone(PoseStack poseStack, EliteMob elite, GeoBone bone, RenderType renderType,
                              MultiBufferSource buffers, VertexConsumer buffer, float partialTick,
                              int packedLight, int packedOverlay) {
        if (mode != Mode.DREADGUARD) return;
        renderBone(poseStack, buffers, bone, DREADGUARD_OVERLAY, null,
            0xFFFFFF, false, 1.01875F, packedLight, packedOverlay);
    }

    private void renderBone(PoseStack poseStack, MultiBufferSource buffers, GeoBone bone,
                            ResourceLocation texture, ResourceLocation overlay, int color, boolean foil,
                            float scale, int packedLight, int packedOverlay) {
        if (bone.getCubes().isEmpty()) return;

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        RenderType baseType = RenderType.armorCutoutNoCull(texture);
        VertexConsumer base = ArmorRenderCompat.armorBuffer(buffers, baseType, foil);
        ArmorRenderCompat.renderGeoCubes(getRenderer(), poseStack, bone, base,
            packedLight, packedOverlay, color);
        if (overlay != null) {
            VertexConsumer overlayBuffer = buffers.getBuffer(RenderType.armorCutoutNoCull(overlay));
            ArmorRenderCompat.renderGeoCubes(getRenderer(), poseStack, bone, overlayBuffer,
                packedLight, packedOverlay, 0xFFFFFF);
        }
        poseStack.popPose();
    }
}