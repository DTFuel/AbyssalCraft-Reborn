package com.shinoow.abyssalcraft.client.render.entity.boss;

import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;
import com.shinoow.abyssalcraft.platform.ACRef;

import software.bernie.geckolib.model.GeoModel;
//? if <1.21 {
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animation.AnimationState;
*///?}

/**
 * GeckoLib model for seven elites (owned by PE-4b): Skeleton Goliath (id {@code gskeleton}), Remnant,
 * Shub Offspring, Gatekeeper Minion (id {@code jzaharminion}), Chagaroth's Fist, Spawn of Chagaroth (id
 * {@code chagarothspawn}) and the Dreadguard. Resolves the mesh + faithful 1.12.2 texture per entity id,
 * so one model serves all seven. Six use converter-generated meshes; the Dreadguard uses a hand-written
 * standard-biped geo ({@code dreadguard.geo.json}, 64x32 UVs) because its 1.12.2 render reused a vanilla
 * {@code ModelZombie}. The Dragon Minion uses a Java model ({@code DragonRenderer}), not GeckoLib. See
 * {@code docs/spec/geckolib-model-porting.md}.
 */
public class EliteGeoModel extends GeoModel<EliteMob> {

    private static final ResourceLocation ANIMATION = ACRef.id("animations/entity/empty.animation.json");

    private static final Map<String, ResourceLocation> TEXTURES = Map.of(
        "gskeleton", ACRef.id("textures/model/elite/skeletongoliath.png"),
        "remnant", ACRef.id("textures/model/remnant/remnant.png"),
        "shuboffspring", ACRef.id("textures/model/shub_offspring.png"),
        "jzaharminion", ACRef.id("textures/model/elite/gatekeeperminion.png"),
        "chagarothfist", ACRef.id("textures/model/chagarothfist.png"),
        "chagarothspawn", ACRef.id("textures/model/spawn_of_chagaroth.png"),
        "dreadguard", ACRef.id("textures/model/elite/dreadguard.png"));

    @Override
    public ResourceLocation getModelResource(EliteMob animatable) {
        return ACRef.id("geo/entity/" + id(animatable) + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EliteMob animatable) {
        return TEXTURES.getOrDefault(id(animatable), ACRef.id("textures/model/elite/skeletongoliath.png"));
    }

    @Override
    public ResourceLocation getAnimationResource(EliteMob animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(EliteMob animatable, long instanceId, AnimationState<EliteMob> animationState) {
        BossAnimations.apply(id(animatable), this::getBone, animatable, animationState.getPartialTick());
    }

    private static String id(EliteMob animatable) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(animatable.getType()).getPath();
    }
}
