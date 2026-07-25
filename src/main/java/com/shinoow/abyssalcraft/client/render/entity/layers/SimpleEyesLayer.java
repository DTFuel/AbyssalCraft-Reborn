package com.shinoow.abyssalcraft.client.render.entity.layers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Glowing-eyes render layer (owned by PE-3, Stage E2). A thin subclass of the vanilla {@link EyesLayer}
 * that draws a fixed emissive texture over the parent model with {@link RenderType#eyes}. Subclassing the
 * vanilla layer means the (loader/version-forked) {@code renderToBuffer} call stays inside vanilla code,
 * so this business layer is fork-free -- only the fixed {@link RenderType} is supplied. Used by the ghoul
 * and shoggoth renderers with their {@code *_eyes} textures (maps 1.12.2 {@code LayerEyes}/{@code
 * LayerShoggothEyes}).
 */
public class SimpleEyesLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {

    private final RenderType renderType;

    public SimpleEyesLayer(RenderLayerParent<T, M> parent, ResourceLocation eyesTexture) {
        super(parent);
        this.renderType = RenderType.eyes(eyesTexture);
    }

    @Override
    public RenderType renderType() {
        return this.renderType;
    }
}
