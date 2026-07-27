package com.shinoow.abyssalcraft.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;

public final class ArmorRenderCompat {

    private static final int DEFAULT_LEATHER_COLOR = 0xA06540;

    private ArmorRenderCompat() {}

    public static VertexConsumer armorBuffer(MultiBufferSource buffers, RenderType renderType, boolean foil) {
        //? if >=1.21 {
        /*return ItemRenderer.getArmorFoilBuffer(buffers, renderType, foil);
        *///?} else {
        return ItemRenderer.getArmorFoilBuffer(buffers, renderType, false, foil);
        //?}
    }

    public static void render(Model model, PoseStack poseStack, VertexConsumer consumer,
                              int packedLight, int packedOverlay, int rgb) {
        render(model, poseStack, consumer, packedLight, packedOverlay, rgb, 1.0F);
    }

    public static void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer consumer,
                                  int packedLight, int packedOverlay, int rgb) {
        //? if >=1.21 {
        /*part.render(poseStack, consumer, packedLight, packedOverlay, 0xFF000000 | rgb);
        *///?} else {
        float red = (rgb >> 16 & 255) / 255.0F;
        float green = (rgb >> 8 & 255) / 255.0F;
        float blue = (rgb & 255) / 255.0F;
        part.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, 1.0F);
        //?}
    }

    public static void render(Model model, PoseStack poseStack, VertexConsumer consumer,
                              int packedLight, int packedOverlay, int rgb, float alpha) {
        //? if >=1.21 {
        /*model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay,
            Math.round(alpha * 255.0F) << 24 | rgb);
        *///?} else {
        float red = (rgb >> 16 & 255) / 255.0F;
        float green = (rgb >> 8 & 255) / 255.0F;
        float blue = (rgb & 255) / 255.0F;
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        //?}
    }

    public static int leatherColor(ItemStack stack) {
        //? if >=1.21 {
        /*return net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack, DEFAULT_LEATHER_COLOR);
        *///?} else {
        return stack.getItem() instanceof net.minecraft.world.item.DyeableLeatherItem dyeable
            ? dyeable.getColor(stack) : DEFAULT_LEATHER_COLOR;
        //?}
    }

    public static void renderGeoCubes(GeoRenderer<?> renderer, PoseStack poseStack, GeoBone bone,
                                      VertexConsumer consumer, int packedLight, int packedOverlay, int rgb) {
        //? if >=1.21 {
        /*renderer.renderCubesOfBone(poseStack, bone, consumer, packedLight, packedOverlay, 0xFF000000 | rgb);
        *///?} else {
        float red = (rgb >> 16 & 255) / 255.0F;
        float green = (rgb >> 8 & 255) / 255.0F;
        float blue = (rgb & 255) / 255.0F;
        renderer.renderCubesOfBone(poseStack, bone, consumer, packedLight, packedOverlay,
            red, green, blue, 1.0F);
        //?}
    }
}