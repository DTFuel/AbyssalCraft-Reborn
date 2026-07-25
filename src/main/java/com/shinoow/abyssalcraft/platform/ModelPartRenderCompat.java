package com.shinoow.abyssalcraft.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;

/** Compat: render a model part with one packed ARGB tint. */
public final class ModelPartRenderCompat {

    private ModelPartRenderCompat() {}

    public static void render(ModelPart part, PoseStack poseStack, VertexConsumer consumer,
                              int packedLight, int packedOverlay, int argb) {
        //? if >=1.21 {
        /*part.render(poseStack, consumer, packedLight, packedOverlay, argb);
        *///?} else {
        float alpha = (argb >>> 24 & 0xFF) / 255.0F;
        float red = (argb >>> 16 & 0xFF) / 255.0F;
        float green = (argb >>> 8 & 0xFF) / 255.0F;
        float blue = (argb & 0xFF) / 255.0F;
        part.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        //?}
    }
}