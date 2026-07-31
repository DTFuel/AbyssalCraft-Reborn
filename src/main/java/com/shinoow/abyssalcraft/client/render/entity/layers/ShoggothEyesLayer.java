package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.client.model.entity.ShoggothModel;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class ShoggothEyesLayer extends RenderLayer<AbstractShoggoth, ShoggothModel> {

    private static final int FULL_BRIGHT = 15728880;
    private static final ResourceLocation LESSER = texture("lessershoggoth_eyes");
    private static final ResourceLocation ABYSSAL = texture("abyssalshoggoth_eyes");
    private static final ResourceLocation DREADED = texture("dreadedshoggoth_eyes");
    private static final ResourceLocation OMOTHOL = texture("omotholshoggoth_eyes");
    private static final ResourceLocation SHADOW = texture("shadowshoggoth_eyes");

    public ShoggothEyesLayer(RenderLayerParent<AbstractShoggoth, ShoggothModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       AbstractShoggoth shoggoth, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ResourceLocation texture = switch (shoggoth.getShoggothType()) {
            case 1 -> ABYSSAL;
            case 2 -> DREADED;
            case 3 -> OMOTHOL;
            case 4 -> SHADOW;
            default -> LESSER;
        };
        //? if >=1.21 {
        /*VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(texture));
        getParentModel().renderToBuffer(poseStack, consumer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        *///?} else {
        VertexConsumer consumer = buffers.getBuffer(RenderType.eyes(texture));
        getParentModel().renderToBuffer(poseStack, consumer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
            1.0F, 1.0F, 1.0F, 1.0F);
        //?}
    }

    private static ResourceLocation texture(String name) {
        return ACRef.id("textures/model/shoggoth/" + name + ".png");
    }
}