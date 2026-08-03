package com.shinoow.abyssalcraft.client.render.entity.effect;

import com.shinoow.abyssalcraft.content.entity.misc.CompassTentacle;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.resources.ResourceLocation;

//? if <1.21 {
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animation.AnimationState;
*///?}
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public final class CompassTentacleGeoModel extends GeoModel<CompassTentacle> {

    private static final ResourceLocation MODEL = ACRef.id("geo/effect/dread_tentacle.geo.json");
    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/compass_tentacle.png");
    private static final ResourceLocation ANIMATION = ACRef.id("animations/entity/empty.animation.json");

    @Override
    public ResourceLocation getModelResource(CompassTentacle animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CompassTentacle animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CompassTentacle animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(CompassTentacle animatable, long instanceId,
                                    AnimationState<CompassTentacle> animationState) {
        DreadTentacleAnimations.apply(this::getBone,
            animatable.tickCount + animationState.getPartialTick(),
            0.0F, 0.0F, true, 0.0F, 10.0F, 0.0F);
    }
}