package com.shinoow.abyssalcraft.client.render.entity.legacy;

import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;
import com.shinoow.abyssalcraft.platform.ACRef;

import software.bernie.geckolib.model.GeoModel;
//? if <1.21 {
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animation.AnimationState;
*///?}

public final class LegacyGeoModel extends GeoModel<LegacyHostileMob> {

    private static final ResourceLocation ANIMATION = ACRef.id("animations/entity/empty.animation.json");
    private static final Map<String, String> MODELS = Map.of(
        "dreadling", "dreadling",
        "dreadspawn", "dread_spawn",
        "greaterdreadspawn", "dread_spawn",
        "lesserdreadbeast", "lesser_dreadbeast",
        "shadowcreature", "shadow_creature",
        "shadowmonster", "shadow_monster",
        "shadowbeast", "shadow_beast");
    private static final Map<String, ResourceLocation> TEXTURES = Map.of(
        "dreadling", ACRef.id("textures/model/dreadling.png"),
        "dreadspawn", ACRef.id("textures/model/dread_spawn.png"),
        "greaterdreadspawn", ACRef.id("textures/model/greater_dread_spawn.png"),
        "lesserdreadbeast", ACRef.id("textures/model/elite/lesser_dreadbeast.png"),
        "shadowcreature", ACRef.id("textures/model/shadowcreature.png"),
        "shadowmonster", ACRef.id("textures/model/shadowmonster.png"),
        "shadowbeast", ACRef.id("textures/model/elite/shadowbeast.png"));

    @Override
    public ResourceLocation getModelResource(LegacyHostileMob animatable) {
        String id = id(animatable);
        return ACRef.id("geo/entity/" + MODELS.getOrDefault(id, "dreadling") + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LegacyHostileMob animatable) {
        return TEXTURES.getOrDefault(id(animatable), TEXTURES.get("dreadling"));
    }

    @Override
    public ResourceLocation getAnimationResource(LegacyHostileMob animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(LegacyHostileMob animatable, long instanceId,
                                    AnimationState<LegacyHostileMob> animationState) {
        LegacyAnimations.apply(id(animatable), this::getBone, animatable, animationState.getPartialTick());
    }

    private static String id(LegacyHostileMob animatable) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(animatable.getType()).getPath();
    }
}