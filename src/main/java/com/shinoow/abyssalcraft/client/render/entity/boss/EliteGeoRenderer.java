package com.shinoow.abyssalcraft.client.render.entity.boss;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for the seven {@code Model*}-backed elites (owned by PE-4b), driven by
 * {@link EliteGeoModel} (per-id mesh + texture). Registered by {@code BossRenderers}. Fork-free (the
 * {@code (Context, GeoModel)} constructor is identical across both GeckoLib builds).
 */
public class EliteGeoRenderer extends GeoEntityRenderer<EliteMob> {

    public EliteGeoRenderer(EntityRendererProvider.Context context, boolean glowingEyes,
                            EliteArmorGeoLayer.Mode armorMode, boolean skeletonGoliath,
                            boolean dreadCarrier, float carrierY) {
        super(context, new EliteGeoModel());
        if (glowingEyes) {
            addRenderLayer(new software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer<>(this));
        }
        if (armorMode == EliteArmorGeoLayer.Mode.DREADGUARD) {
            addRenderLayer(new EliteArmorGeoLayer(this, armorMode));
        }
        if (skeletonGoliath) {
            addRenderLayer(new BossHeldItemGeoLayer<>(this,
                BossHeldItemGeoLayer.Mode.SKELETON_GOLIATH));
        }
        if (skeletonGoliath || armorMode == EliteArmorGeoLayer.Mode.DREADGUARD) {
            withScale(1.5F);
        }
        if (dreadCarrier) {
            boolean dreadguard = armorMode == EliteArmorGeoLayer.Mode.DREADGUARD;
            float carrierZ = dreadguard ? 0.0F : 2.0F;
            addRenderLayer(new GeoDreadCarrierTentacleLayer<>(this, context.getModelSet(),
                0.0F, carrierY, carrierZ, dreadguard));
        }
    }
}
