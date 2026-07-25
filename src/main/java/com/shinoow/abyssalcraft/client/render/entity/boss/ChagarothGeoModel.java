package com.shinoow.abyssalcraft.client.render.entity.boss;

import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.platform.ACRef;

import software.bernie.geckolib.model.GeoModel;
//? if forge {
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animation.AnimationState;
*///?}

/**
 * GeckoLib model for the Chagaroth boss (owned by PE-4, Stage E). Resolves the converter-generated geo
 * mesh ({@code geo/entity/chagaroth.geo.json}, produced by {@code scripts/convert_modelbase_to_geo.js}
 * from the 1.12.2 {@code ModelChagaroth}), the faithful 1.12.2 texture, and a static (empty) animation.
 *
 * <p>The {@link GeoModel} resource API is identical across the two GeckoLib builds (4.8.4 forge / 4.9.2
 * neoforge -- javap-verified), so this business class is fork-free; the version divergence is confined
 * to {@link BossMob}'s two GeckoLib imports. See {@code docs/spec/geckolib-model-porting.md}.
 */
public class ChagarothGeoModel extends GeoModel<BossMob> {

    private static final ResourceLocation MODEL = ACRef.id("geo/entity/chagaroth.geo.json");
    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/boss/chagaroth.png");
    private static final ResourceLocation ANIMATION = ACRef.id("animations/entity/empty.animation.json");

    @Override
    public ResourceLocation getModelResource(BossMob animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BossMob animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BossMob animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(BossMob animatable, long instanceId, AnimationState<BossMob> animationState) {
        BossAnimations.apply("chagaroth", this::getBone, animatable, animationState.getPartialTick());
    }
}
