package com.shinoow.abyssalcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Default placeholder entity renderer (owned by PE-1, Stage E1).
 *
 * <p>{@code ACEntityRenderers} registers this for every AbyssalCraft {@link Entity} type so the client
 * passes {@code EntityRenderers} validation (a missing renderer is a hard startup crash) -- it draws a
 * small textured cube from the {@link ModModelLayers#PLACEHOLDER} layer, connecting both the renderer
 * and model-layer pipelines. Stage E2 (PE-2..6) overrides it per family with faithful models. Only the
 * version-stable 4-arg {@code ModelPart.render} is used, so this business file carries no {@code //?}.
 */
public class ACPlaceholderRenderer extends EntityRenderer<Entity> {

    private static final ResourceLocation TEXTURE = ACRef.vanilla("textures/entity/creeper/creeper.png");

    private final ModelPart cube;

    public ACPlaceholderRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.cube = context.bakeLayer(ModModelLayers.PLACEHOLDER);
    }

    @Override
    public void render(Entity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);          // entity-model orientation (y/x flip)
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);   // model pixels -> blocks
        poseStack.translate(0.0F, -12.0F, 0.0F);      // lift the cube onto the entity
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.cube.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TEXTURE;
    }
}
