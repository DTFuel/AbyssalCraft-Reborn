package com.shinoow.abyssalcraft.client.render.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.content.entity.misc.PrimedODB;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class PrimedODBRenderer extends EntityRenderer<PrimedODB> {

    private final ModelPart cube;
    private final ResourceLocation texture;

    public PrimedODBRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context);
        this.cube = context.bakeLayer(ModModelLayers.ODB_CUBE);
        this.texture = texture;
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(PrimedODB entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float remaining = entity.getFuse() - partialTicks + 1.0F;
        float scale = 1.0F;
        if (remaining < 10.0F) {
            float pulse = 1.0F - remaining / 10.0F;
            pulse = Math.max(0.0F, Math.min(1.0F, pulse));
            pulse *= pulse;
            pulse *= pulse;
            scale += pulse * 0.3F;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.scale(-scale, -scale, scale);
        VertexConsumer body = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        cube.render(poseStack, body, packedLight, OverlayTexture.NO_OVERLAY);
        if (entity.getFuse() / 5 % 2 == 0) {
            VertexConsumer flash = buffer.getBuffer(RenderType.entityTranslucentEmissive(texture));
            cube.render(poseStack, flash, 15728880, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedODB entity) {
        return texture;
    }
}