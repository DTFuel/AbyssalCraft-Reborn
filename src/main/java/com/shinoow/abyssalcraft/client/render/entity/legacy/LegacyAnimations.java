package com.shinoow.abyssalcraft.client.render.entity.legacy;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.util.Mth;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;

import software.bernie.geckolib.cache.object.GeoBone;

final class LegacyAnimations {

    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;

    private LegacyAnimations() {}

    static void apply(String id, Function<String, Optional<GeoBone>> bones,
                      LegacyHostileMob entity, float partialTick) {
        float limbSwing = entity.walkAnimation.position(partialTick);
        float limbSwingAmount = Math.min(entity.walkAnimation.speed(partialTick), 1.0F);
        float ageInTicks = entity.tickCount + partialTick;
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = Mth.wrapDegrees(headYaw - bodyYaw);
        float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        switch (id) {
            case "dreadling" -> dreadling(bones, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
            case "dreadspawn", "greaterdreadspawn" -> dreadSpawn(bones, entity, limbSwing,
                limbSwingAmount, ageInTicks, netHeadYaw);
            case "lesserdreadbeast" -> dreadbeast(bones, limbSwing, limbSwingAmount);
            case "shadowcreature" -> shadowCreature(bones, limbSwing, limbSwingAmount,
                netHeadYaw, headPitch);
            case "shadowmonster" -> shadowMonster(bones, limbSwing, limbSwingAmount,
                netHeadYaw, headPitch);
            case "shadowbeast" -> shadowBeast(bones, entity, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, partialTick);
            default -> { }
        }
    }

    private static void dreadling(Function<String, Optional<GeoBone>> bones, float limbSwing,
                                  float limbSwingAmount, float netHeadYaw, float headPitch) {
        headTrack(bones, "head", netHeadYaw, headPitch);
        float swing = limbSwing * 0.6662F;
        setArm(bones, "rightarm", Mth.cos(swing + (float) Math.PI) * limbSwingAmount);
        setArm(bones, "leftarm", Mth.cos(swing) * limbSwingAmount);
    }

    private static void dreadSpawn(Function<String, Optional<GeoBone>> bones, LegacyHostileMob entity,
                                   float limbSwing, float limbSwingAmount, float ageInTicks,
                                   float netHeadYaw) {
        float swing = limbSwing * 0.6662F;
        setBoneY(bones, "arm", -Mth.cos(swing + (float) Math.PI) * limbSwingAmount);
        float armWave = Mth.sin(ageInTicks * (0.02F * (entity.getId() % 10))) * 4.5F * DEG_TO_RAD;
        setBoneY(bones, "arm1", -armWave);
        setBoneY(bones, "arm2", -armWave);
        setBoneY(bones, "arm3", -armWave);
        setBoneZ(bones, "thing", Mth.cos(swing) * 1.4F * limbSwingAmount);
        setBoneY(bones, "head", -netHeadYaw * DEG_TO_RAD);

        setTentacleZ(bones, "t1", ageInTicks, 0.03F);
        setTentacleZ(bones, "t2", ageInTicks, 0.04F);
        setTentacleZ(bones, "t3", ageInTicks, -0.04F);
    }

    private static void setTentacleZ(Function<String, Optional<GeoBone>> bones, String prefix,
                                     float ageInTicks, float frequency) {
        float rotation = Mth.cos(ageInTicks * frequency) * 4.25F * DEG_TO_RAD;
        setBoneZ(bones, prefix, rotation);
        for (int index = 1; index <= 3; index++) setBoneZ(bones, prefix + index, rotation);
    }

    private static void dreadbeast(Function<String, Optional<GeoBone>> bones, float limbSwing,
                                   float limbSwingAmount) {
        float swing = limbSwing * 0.6662F;
        setGroupY(bones, "frontleg", Mth.cos(swing + (float) Math.PI) * limbSwingAmount);
        setGroupY(bones, "leftleg", Mth.cos(swing) * limbSwingAmount);
        setGroupY(bones, "backleg", Mth.cos(swing) * 1.4F * limbSwingAmount);
        setGroupY(bones, "rightleg", Mth.cos(swing + (float) Math.PI) * 1.4F * limbSwingAmount);
    }

    private static void shadowCreature(Function<String, Optional<GeoBone>> bones, float limbSwing,
                                       float limbSwingAmount, float netHeadYaw, float headPitch) {
        headTrack(bones, "Head1", netHeadYaw, headPitch);
        float swing = limbSwing * 0.6662F;
        setArm(bones, "RightArm2", Mth.cos(swing + (float) Math.PI) * limbSwingAmount);
        setArm(bones, "LeftArm2", Mth.cos(swing) * limbSwingAmount);
    }

    private static void shadowMonster(Function<String, Optional<GeoBone>> bones, float limbSwing,
                                      float limbSwingAmount, float netHeadYaw, float headPitch) {
        headTrack(bones, "Head", netHeadYaw, headPitch);
        float swing = limbSwing * 0.6662F;
        setArm(bones, "Rarm1", Mth.cos(swing + (float) Math.PI) * limbSwingAmount);
        setArm(bones, "Larm1", Mth.cos(swing) * limbSwingAmount);
        setArm(bones, "Rarm2", Mth.cos(swing + (float) Math.PI) * limbSwingAmount * 0.1F);
        setArm(bones, "Larm2", Mth.cos(swing) * limbSwingAmount * 0.1F);
    }

    private static void shadowBeast(Function<String, Optional<GeoBone>> bones, LegacyHostileMob entity,
                                    float limbSwing, float limbSwingAmount, float ageInTicks,
                                    float netHeadYaw, float headPitch, float partialTick) {
        headTrack(bones, "head", netHeadYaw, headPitch);
        setLeg(bones, "rightleg", Mth.cos(limbSwing * 0.3331F) * 0.07F * limbSwingAmount);
        setLeg(bones, "leftleg", Mth.cos(limbSwing * 0.3331F + (float) Math.PI)
            * 0.07F * limbSwingAmount);

        float attack = entity.getAttackAnim(partialTick);
        float first = Mth.sin(attack * (float) Math.PI);
        float second = Mth.sin((1.0F - (1.0F - attack) * (1.0F - attack)) * (float) Math.PI);
        float rightX = -((float) Math.PI / 7.2F) - first * 1.2F + second * 0.4F
            + Mth.sin(ageInTicks * 0.067F) * 0.05F;
        float leftX = -((float) Math.PI / 7.2F) - first * 1.2F + second * 0.4F
            - Mth.sin(ageInTicks * 0.067F) * 0.05F;
        float idleZ = Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        setBeastArm(bones, "rarm1", rightX, -(0.1F - first * 0.6F), idleZ);
        setBeastArm(bones, "larm1", leftX, 0.1F - first * 0.6F, -idleZ);
    }

    private static void headTrack(Function<String, Optional<GeoBone>> bones, String name,
                                  float netHeadYaw, float headPitch) {
        bones.apply(name).ifPresent(head -> {
            head.setRotY(-netHeadYaw * DEG_TO_RAD);
            head.setRotX(-headPitch * DEG_TO_RAD);
        });
    }

    private static void setArm(Function<String, Optional<GeoBone>> bones, String name, float xRot) {
        bones.apply(name).ifPresent(arm -> {
            arm.setRotX(xRot);
            arm.setRotZ(0.0F);
        });
    }

    private static void setGroupY(Function<String, Optional<GeoBone>> bones, String prefix, float yRot) {
        for (int index = 1; index <= 3; index++) {
            bones.apply(prefix + index).ifPresent(bone -> bone.setRotY(yRot));
        }
    }

    private static void setBoneY(Function<String, Optional<GeoBone>> bones, String name, float yRot) {
        bones.apply(name).ifPresent(bone -> bone.setRotY(yRot));
    }

    private static void setBoneZ(Function<String, Optional<GeoBone>> bones, String name, float zRot) {
        bones.apply(name).ifPresent(bone -> bone.setRotZ(zRot));
    }

    private static void setLeg(Function<String, Optional<GeoBone>> bones, String name, float xRot) {
        bones.apply(name).ifPresent(leg -> {
            leg.setRotX(xRot);
            leg.setRotY(0.0F);
        });
    }

    private static void setBeastArm(Function<String, Optional<GeoBone>> bones, String name,
                                    float xRot, float yRot, float zRot) {
        bones.apply(name).ifPresent(arm -> {
            arm.setRotX(-xRot);
            arm.setRotY(-yRot);
            arm.setRotZ(zRot);
        });
    }
}