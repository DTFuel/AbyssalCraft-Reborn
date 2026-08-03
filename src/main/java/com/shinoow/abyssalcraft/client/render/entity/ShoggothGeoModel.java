package com.shinoow.abyssalcraft.client.render.entity;

import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.platform.ACRef;

import software.bernie.geckolib.model.GeoModel;
//? if <1.21 {
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animation.AnimationState;
*///?}

public final class ShoggothGeoModel extends GeoModel<AbstractShoggoth> {

    private static final ResourceLocation MODEL = ACRef.id("geo/entity/shoggoth.geo.json");
    private static final ResourceLocation ANIMATION = ACRef.id("animations/entity/empty.animation.json");

    @Override
    public ResourceLocation getModelResource(AbstractShoggoth animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractShoggoth animatable) {
        return ShoggothGeoRenderer.texture(animatable.getShoggothType(), false);
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractShoggoth animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(AbstractShoggoth animatable, long instanceId,
                                    AnimationState<AbstractShoggoth> animationState) {
        ShoggothAnimations.apply(this::getBone, animatable, animationState.getPartialTick());
    }
}