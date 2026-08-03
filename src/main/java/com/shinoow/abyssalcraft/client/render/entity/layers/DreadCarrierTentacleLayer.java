package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public final class DreadCarrierTentacleLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private final DreadTentacleGeoRenderHelper geoRenderer = new DreadTentacleGeoRenderHelper();
    private final float anchorX;
    private final float anchorY;
    private final float anchorZ;

    public DreadCarrierTentacleLayer(RenderLayerParent<T, M> parent, EntityModelSet models,
                                     float anchorX, float anchorY, float anchorZ) {
        super(parent);
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.anchorZ = anchorZ;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || !LegacyEntities.isDread(entity)) return;

        geoRenderer.render(poseStack, buffers, packedLight, partialTick, ageInTicks,
            limbSwing, limbSwingAmount, anchorX, anchorY, anchorZ, false);
    }
}