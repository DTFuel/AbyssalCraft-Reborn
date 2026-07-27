package com.shinoow.abyssalcraft.client.render.entity.boss;

import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.platform.ACRef;

import software.bernie.geckolib.model.GeoModel;
//? if <1.21 {
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animation.AnimationState;
*///?}

/**
 * GeckoLib model for the bar-bosses Jzahar and Sacthoth (id {@code shadowboss}) -- owned by PE-4b. Resolves
 * the converter-generated mesh ({@code geo/entity/<id>.geo.json}) and the faithful 1.12.2 texture per entity
 * id, so one model instance serves both. Chagaroth keeps its dedicated {@link ChagarothGeoModel} (the first,
 * user-verified mesh); the Abyssal Dragon uses a Java model ({@code DragonRenderer}), not GeckoLib. See
 * {@code docs/spec/geckolib-model-porting.md}.
 */
public class BossGeoModel extends GeoModel<BossMob> {

    private static final ResourceLocation ANIMATION = ACRef.id("animations/entity/empty.animation.json");

    /** Boss id -> faithful texture (paths are not uniform, so mapped explicitly). */
    private static final Map<String, ResourceLocation> TEXTURES = Map.of(
        "jzahar", ACRef.id("textures/model/boss/jzahar.png"),
        "shadowboss", ACRef.id("textures/model/boss/sacthoth.png"));

    @Override
    public ResourceLocation getModelResource(BossMob animatable) {
        return ACRef.id("geo/entity/" + id(animatable) + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BossMob animatable) {
        return TEXTURES.getOrDefault(id(animatable), ACRef.id("textures/model/boss/chagaroth.png"));
    }

    @Override
    public ResourceLocation getAnimationResource(BossMob animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(BossMob animatable, long instanceId, AnimationState<BossMob> animationState) {
        BossAnimations.apply(id(animatable), this::getBone, animatable, animationState.getPartialTick());
    }

    private static String id(BossMob animatable) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(animatable.getType()).getPath();
    }
}
