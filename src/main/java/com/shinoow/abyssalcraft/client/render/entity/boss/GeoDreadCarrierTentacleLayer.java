package com.shinoow.abyssalcraft.client.render.entity.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.client.render.entity.layers.DreadTentacleGeoRenderHelper;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
//? if <1.21 {
import software.bernie.geckolib.core.animatable.GeoAnimatable;
//?} else {
/*import software.bernie.geckolib.animatable.GeoAnimatable;
*///?}

public final class GeoDreadCarrierTentacleLayer<T extends LivingEntity & GeoAnimatable>
        extends GeoRenderLayer<T> {

    private final DreadTentacleGeoRenderHelper geoRenderer = new DreadTentacleGeoRenderHelper();
    private final float anchorX;
    private final float anchorY;
    private final float anchorZ;
    /** Flip the tentacle 180 deg about the (Y-axis) anchor so it drapes the correct way (Dreadguard). */
    private final boolean flipHorizontal;

    public GeoDreadCarrierTentacleLayer(GeoRenderer<T> renderer, EntityModelSet models,
                                        float anchorX, float anchorY, float anchorZ) {
        this(renderer, models, anchorX, anchorY, anchorZ, false);
    }

    public GeoDreadCarrierTentacleLayer(GeoRenderer<T> renderer, EntityModelSet models,
                                        float anchorX, float anchorY, float anchorZ, boolean flipHorizontal) {
        super(renderer);
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.anchorZ = anchorZ;
        this.flipHorizontal = flipHorizontal;
    }

    @Override
    public void render(PoseStack poseStack, T entity, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource buffers, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (entity.isInvisible() || !LegacyEntities.isDread(entity)) return;

        geoRenderer.render(poseStack, buffers, packedLight, partialTick,
            entity.tickCount + partialTick, 0.0F, 0.0F,
            anchorX, anchorY, anchorZ, flipHorizontal);
    }
}