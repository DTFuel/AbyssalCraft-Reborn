package com.shinoow.abyssalcraft.client.render.entity.legacy;

import com.shinoow.abyssalcraft.client.render.entity.boss.GeoDreadCarrierTentacleLayer;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class LegacyDreadGeoRenderer extends GeoEntityRenderer<LegacyHostileMob> {

    public LegacyDreadGeoRenderer(EntityRendererProvider.Context context, float modelScale,
                                  float tentacleX, float tentacleY, float tentacleZ,
                                  boolean headItems) {
        super(context, new LegacyGeoModel());
        this.shadowRadius = 0.5F;
        if (modelScale != 1.0F) withScale(modelScale);
        addRenderLayer(new GeoDreadCarrierTentacleLayer<>(this, context.getModelSet(),
            tentacleX, tentacleY, tentacleZ));
        if (headItems) addRenderLayer(new LegacyHeadItemGeoLayer(this, "head"));
    }
}