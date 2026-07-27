package com.shinoow.abyssalcraft.client.render.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.shinoow.abyssalcraft.content.entity.misc.Implosion;
import com.shinoow.abyssalcraft.client.hud.ClientVarsManager;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ArmorRenderCompat;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class ImplosionRenderer extends EntityRenderer<Implosion> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/black_hole.png");
    private final ModelPart quad;

    public ImplosionRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.quad = context.bakeLayer(ModModelLayers.BILLBOARD);
    }

    @Override
    public void render(Implosion entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float age = entity.tickCount + partialTicks;
        float scale = 2.0F + age / 60.0F;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        poseStack.pushPose();
        poseStack.translate(0.0F, entity.getBbHeight() * 0.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 2.0F));
        for (int index = 0; index < 3; index++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(index * 60.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(index * 37.0F + age));
            poseStack.scale(scale, scale, scale);
            ArmorRenderCompat.renderPart(quad, poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY,
                ClientVarsManager.get().implosionColor());
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Implosion entity) {
        return TEXTURE;
    }
}