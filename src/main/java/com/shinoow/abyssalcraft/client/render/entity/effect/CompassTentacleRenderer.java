package com.shinoow.abyssalcraft.client.render.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.shinoow.abyssalcraft.client.model.entity.DreadTentacleModel;
import com.shinoow.abyssalcraft.content.entity.misc.CompassTentacle;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class CompassTentacleRenderer extends EntityRenderer<CompassTentacle> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/compass_tentacle.png");
    private final DreadTentacleModel<CompassTentacle> model;

    public CompassTentacleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new DreadTentacleModel<>(context.bakeLayer(ModModelLayers.DREAD_TENTACLE));
    }

    @Override
    public void render(CompassTentacle entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        poseStack.pushPose();
        poseStack.translate(0.1F, 1.3F, -0.1F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        poseStack.scale(2.4F, 2.4F, 2.4F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.root().render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CompassTentacle entity) {
        return TEXTURE;
    }
}