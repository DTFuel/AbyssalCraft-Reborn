package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.client.model.entity.DreadTentacleModel;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class DreadCarrierTentacleLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/dread_tentacle.png");

    private final DreadTentacleModel<T> model;
    private final float anchorX;
    private final float anchorY;
    private final float anchorZ;

    public DreadCarrierTentacleLayer(RenderLayerParent<T, M> parent, EntityModelSet models,
                                     float anchorX, float anchorY, float anchorZ) {
        super(parent);
        this.model = new DreadTentacleModel<>(models.bakeLayer(ModModelLayers.DREAD_TENTACLE));
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.anchorZ = anchorZ;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || !LegacyEntities.isDread(entity)) return;

        model.setupCarrierAnim(entity, limbSwing, limbSwingAmount, ageInTicks,
            anchorX, anchorY, anchorZ);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.root().render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }
}