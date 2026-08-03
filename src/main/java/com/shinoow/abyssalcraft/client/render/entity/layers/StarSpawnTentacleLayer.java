package com.shinoow.abyssalcraft.client.render.entity.layers;

import java.util.Set;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
//? if <1.21 {
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationState;
*///?}

public final class StarSpawnTentacleLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/tentacles.png");
    private static final Set<UUID> STAR_SPAWN = Set.of(
        UUID.fromString("a5d8abca-0979-4bb0-825a-f1ccda0b350b"),
        UUID.fromString("08f3211c-d425-47fd-afd8-f0e7f94152c4"));

    private final State state = new State();
    private final GeoObjectRenderer<State> renderer = new GeoObjectRenderer<>(new Model());

    public StarSpawnTentacleLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                                  net.minecraft.client.model.geom.EntityModelSet models) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible() || !isStarSpawn(player)) return;

                    state.ageInTicks = player.tickCount + partialTick;
        poseStack.pushPose();
        if (player.isCrouching()) poseStack.translate(0.0F, 0.24F, 0.0F);
        getParentModel().head.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.22F, 0.0F);
                    poseStack.translate(-0.5F, -0.51F, -0.5F);
                    RenderType renderType = RenderType.entityCutoutNoCull(TEXTURE);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
                    //? if >=1.21 {
                    /*renderer.render(poseStack, state, buffer, renderType, consumer, packedLight, partialTick);
                    *///?} else {
                    renderer.render(poseStack, state, buffer, renderType, consumer, packedLight);
                    //?}
        poseStack.popPose();
    }

    private static boolean isStarSpawn(AbstractClientPlayer player) {
        return STAR_SPAWN.contains(player.getUUID());
    }

    private static final class Model extends GeoModel<State> {

        private static final ResourceLocation MODEL = ACRef.id("geo/player/star_spawn_tentacles.geo.json");
        private static final ResourceLocation ANIMATION = ACRef.id("animations/entity/empty.animation.json");

        @Override
        public ResourceLocation getModelResource(State animatable) {
            return MODEL;
        }

        @Override
        public ResourceLocation getTextureResource(State animatable) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(State animatable) {
            return ANIMATION;
        }

        @Override
        public void setCustomAnimations(State animatable, long instanceId, AnimationState<State> animationState) {
            animate("limb1", animatable.ageInTicks, 0.0299F);
            animate("limb2", animatable.ageInTicks, 0.0301F);
            animate("limb3", animatable.ageInTicks, 0.0301F);
            animate("limb4", animatable.ageInTicks, 0.0299F);
        }

        private void animate(String root, float ageInTicks, float speed) {
            float xRot = Mth.sin(ageInTicks * speed) * 4.5F * (float) Math.PI / 180.0F;
            setX(root, xRot);
            for (int segment = 2; segment <= 4; segment++) setX(root + "_" + segment, xRot);
        }

        private void setX(String name, float xRot) {
            GeoBone bone = getBone(name).orElseThrow();
            bone.setRotX(-xRot);
            bone.setRotY(0.0F);
            bone.setRotZ(0.0F);
        }
    }

    private static final class State implements SingletonGeoAnimatable {

        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
        private float ageInTicks;

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }

        @Override
        public double getTick(Object context) {
            return ageInTicks;
        }
    }
}