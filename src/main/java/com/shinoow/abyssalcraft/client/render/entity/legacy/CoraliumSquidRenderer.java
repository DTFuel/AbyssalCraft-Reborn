package com.shinoow.abyssalcraft.client.render.entity.legacy;

import com.shinoow.abyssalcraft.client.render.entity.layers.SimpleEyesLayer;
import com.shinoow.abyssalcraft.content.entity.legacy.CoraliumSquid;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.resources.ResourceLocation;

public final class CoraliumSquidRenderer extends SquidRenderer<CoraliumSquid> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/coraliumsquid.png");
    private static final ResourceLocation EYES = ACRef.id("textures/model/coraliumsquid_eyes.png");

    public CoraliumSquidRenderer(EntityRendererProvider.Context context) {
        super(context, new SquidModel<>(context.bakeLayer(ModelLayers.SQUID)));
        addLayer(new SimpleEyesLayer<>(this, EYES));
    }

    @Override
    public ResourceLocation getTextureLocation(CoraliumSquid entity) {
        return TEXTURE;
    }
}