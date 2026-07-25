package com.shinoow.abyssalcraft.client.render.entity;

import com.shinoow.abyssalcraft.client.model.entity.DemonSheepModel;
import com.shinoow.abyssalcraft.client.render.entity.layers.DemonSheepWoolLayer;
import com.shinoow.abyssalcraft.content.entity.demon.EvilAnimal;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class EvilSheepRenderer extends MobRenderer<EvilAnimal, DemonSheepModel<EvilAnimal>> {

    private static final ResourceLocation TEXTURE = ACRef.vanilla("textures/entity/sheep/sheep.png");

    public EvilSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new DemonSheepModel<>(context.bakeLayer(ModModelLayers.DEMON_SHEEP)), 0.7F);
        addLayer(new DemonSheepWoolLayer<>(this,
            new DemonSheepModel<>(context.bakeLayer(ModModelLayers.DEMON_SHEEP_FUR)),
            ACRef.vanilla("textures/entity/sheep/sheep_fur.png")));
    }

    @Override
    public ResourceLocation getTextureLocation(EvilAnimal entity) {
        return TEXTURE;
    }
}