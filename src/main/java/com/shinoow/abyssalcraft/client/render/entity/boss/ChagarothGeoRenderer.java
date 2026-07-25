package com.shinoow.abyssalcraft.client.render.entity.boss;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

import com.shinoow.abyssalcraft.content.entity.boss.BossMob;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for the Chagaroth boss (owned by PE-4, Stage E). Draws the converter-generated geo
 * mesh via {@link ChagarothGeoModel}; {@code ACEntityRenderers} registers it over the E1 placeholder for
 * Chagaroth's {@code EntityType} only (the other bosses keep the placeholder until their PE-4b meshes).
 *
 * <p>{@link GeoEntityRenderer}'s {@code (Context, GeoModel)} constructor is identical across both GeckoLib
 * builds (javap-verified), so this business class is fork-free.
 */
public class ChagarothGeoRenderer extends GeoEntityRenderer<BossMob> {

    public ChagarothGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new ChagarothGeoModel());
    }

    @Override
    protected float getDeathMaxRotation(BossMob entity) {
        return 0.0F;
    }
}
