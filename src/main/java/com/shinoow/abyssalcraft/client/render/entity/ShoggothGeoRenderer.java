package com.shinoow.abyssalcraft.client.render.entity;

import com.shinoow.abyssalcraft.client.render.entity.layers.ShoggothEyesGeoLayer;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.platform.ACRef;

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

public final class ShoggothGeoRenderer extends GeoEntityRenderer<AbstractShoggoth> {

    public ShoggothGeoRenderer(EntityRendererProvider.Context context, float modelScale) {
        super(context, new ShoggothGeoModel());
        this.shadowRadius = 0.7F;
        if (modelScale != 1.0F) withScale(modelScale);
        if (ACConfig.shoggothGlowingEyes.get()) addRenderLayer(new ShoggothEyesGeoLayer(this));
    }

    @Override
    public RenderType getRenderType(AbstractShoggoth entity, ResourceLocation texture,
                                    MultiBufferSource buffers, float partialTick) {
        if (entity.getShoggothType() == 4) return RenderType.entityTranslucent(texture);
        return super.getRenderType(entity, texture, buffers, partialTick);
    }

    @Override
    public Color getRenderColor(AbstractShoggoth entity, float partialTick, int packedLight) {
        float alpha = entity.getShoggothType() == 4
            ? entity.getLightLevelDependentMagicValue() : 1.0F;
        return Color.ofRGBA(1.0F, 1.0F, 1.0F, alpha);
    }

    public static ResourceLocation texture(int type, boolean eyes) {
        String name = switch (type) {
            case 1 -> "abyssalshoggoth";
            case 2 -> "dreadedshoggoth";
            case 3 -> "omotholshoggoth";
            case 4 -> "shadowshoggoth";
            default -> "lessershoggoth";
        };
        return ACRef.id("textures/model/shoggoth/" + name + (eyes ? "_eyes" : "") + ".png");
    }
}