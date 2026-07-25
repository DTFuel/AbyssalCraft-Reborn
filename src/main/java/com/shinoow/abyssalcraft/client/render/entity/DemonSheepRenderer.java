package com.shinoow.abyssalcraft.client.render.entity;

import com.shinoow.abyssalcraft.client.model.entity.DemonSheepModel;
import com.shinoow.abyssalcraft.client.render.entity.layers.DemonSheepWoolLayer;
import com.shinoow.abyssalcraft.client.render.entity.layers.DreadCarrierTentacleLayer;
import com.shinoow.abyssalcraft.content.entity.demon.DemonAnimal;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class DemonSheepRenderer extends MobRenderer<DemonAnimal, DemonSheepModel<DemonAnimal>> {

    private final ResourceLocation texture;

    public DemonSheepRenderer(EntityRendererProvider.Context context, ResourceLocation texture,
                              ResourceLocation woolTexture) {
        super(context, new DemonSheepModel<>(context.bakeLayer(ModModelLayers.DEMON_SHEEP)), 0.7F);
        this.texture = texture;
        addLayer(new DemonSheepWoolLayer<>(this,
            new DemonSheepModel<>(context.bakeLayer(ModModelLayers.DEMON_SHEEP_FUR)), woolTexture));
        addLayer(new DreadCarrierTentacleLayer<>(this, context.getModelSet(), 0.0F, 5.0F, 0.0F));
    }

    @Override
    public ResourceLocation getTextureLocation(DemonAnimal entity) {
        return texture;
    }
}