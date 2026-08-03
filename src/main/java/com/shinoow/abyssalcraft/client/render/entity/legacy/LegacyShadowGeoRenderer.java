package com.shinoow.abyssalcraft.client.render.entity.legacy;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
//? if <1.21 {
import software.bernie.geckolib.core.object.Color;
//?} else {
/*import software.bernie.geckolib.util.Color;
*///?}

public final class LegacyShadowGeoRenderer extends GeoEntityRenderer<LegacyHostileMob> {

    public LegacyShadowGeoRenderer(EntityRendererProvider.Context context, ResourceLocation eyes,
                                   String headBone) {
        super(context, new LegacyGeoModel());
        this.shadowRadius = 0.0F;
        addRenderLayer(new LegacyShadowEyesGeoLayer(this, eyes));
        addRenderLayer(new LegacyHeadItemGeoLayer(this, headBone));
    }

    @Override
    public RenderType getRenderType(LegacyHostileMob entity, ResourceLocation texture,
                                    MultiBufferSource buffers, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public Color getRenderColor(LegacyHostileMob entity, float partialTick, int packedLight) {
        return Color.ofRGBA(1.0F, 1.0F, 1.0F, entity.getLightLevelDependentMagicValue());
    }
}