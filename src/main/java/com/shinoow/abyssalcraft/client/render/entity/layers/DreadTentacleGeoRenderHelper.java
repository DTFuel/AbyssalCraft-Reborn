package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.shinoow.abyssalcraft.client.render.entity.effect.DreadTentacleAnimations;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
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

public final class DreadTentacleGeoRenderHelper {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/dread_tentacle.png");

    private final State state = new State();
    private final GeoObjectRenderer<State> renderer = new GeoObjectRenderer<>(new Model());

    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, float partialTick,
                       float ageInTicks, float limbSwing, float limbSwingAmount,
                       float anchorX, float anchorY, float anchorZ, boolean flipHorizontal) {
        state.update(ageInTicks, limbSwing, limbSwingAmount, anchorX, anchorY, anchorZ);
        RenderType renderType = RenderType.entityCutoutNoCull(TEXTURE);
        VertexConsumer buffer = buffers.getBuffer(renderType);
        poseStack.pushPose();
        if (flipHorizontal) poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(-0.5F, -0.51F, -0.5F);
        //? if >=1.21 {
        /*renderer.render(poseStack, state, buffers, renderType, buffer, packedLight, partialTick);
        *///?} else {
        renderer.render(poseStack, state, buffers, renderType, buffer, packedLight);
        //?}
        poseStack.popPose();
    }

    private static final class Model extends GeoModel<State> {

        private static final ResourceLocation MODEL = ACRef.id("geo/effect/dread_tentacle.geo.json");
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
            DreadTentacleAnimations.apply(this::getBone, animatable.ageInTicks,
                animatable.limbSwing, animatable.limbSwingAmount, false,
                animatable.anchorX, animatable.anchorY, animatable.anchorZ);
        }
    }

    private static final class State implements SingletonGeoAnimatable {

        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
        private float ageInTicks;
        private float limbSwing;
        private float limbSwingAmount;
        private float anchorX;
        private float anchorY;
        private float anchorZ;

        private void update(float ageInTicks, float limbSwing, float limbSwingAmount,
                            float anchorX, float anchorY, float anchorZ) {
            this.ageInTicks = ageInTicks;
            this.limbSwing = limbSwing;
            this.limbSwingAmount = limbSwingAmount;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.anchorZ = anchorZ;
        }

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