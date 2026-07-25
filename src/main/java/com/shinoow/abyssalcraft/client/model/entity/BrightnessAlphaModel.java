package com.shinoow.abyssalcraft.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.Entity;

public abstract class BrightnessAlphaModel<T extends Entity> extends HierarchicalModel<T> {

    private float renderAlpha = 1.0F;

    public void setRenderAlpha(float renderAlpha) {
        this.renderAlpha = renderAlpha;
    }

    //? if >=1.21 {
    /*@Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, int color) {
        int alpha = Math.round((color >>> 24) * renderAlpha);
        root().render(poseStack, consumer, packedLight, packedOverlay,
            alpha << 24 | color & 0x00FFFFFF);
    }
    *///?} else {
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        root().render(poseStack, consumer, packedLight, packedOverlay,
            red, green, blue, alpha * renderAlpha);
    }
    //?}
}