package com.shinoow.abyssalcraft.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.util.Mth;

import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;

import software.bernie.geckolib.cache.object.GeoBone;

final class ShoggothAnimations {

    private ShoggothAnimations() {}

    static void apply(Function<String, Optional<GeoBone>> bones, AbstractShoggoth entity,
                      float partialTick) {
        List<Pose> poses = new ArrayList<>(84);
        Function<String, Pose> pose = name -> {
            Pose value = new Pose(bones.apply(name).orElseThrow(
                () -> new IllegalStateException("Missing Shoggoth bone: " + name)));
            poses.add(value);
            return value;
        };

        Pose headJoint = pose.apply("headJoint");
        Pose mouth01Upper = pose.apply("mouth01Upper");
        Pose mouth01Lower = pose.apply("mouth01Lower");
        Pose mouth02Upper = pose.apply("mouth02Upper");
        Pose mouth02Lower = pose.apply("mouth02Lower");
        Pose mouth03Upper = pose.apply("mouth03Upper");
        Pose mouth03Lower = pose.apply("mouth03Lower");
        Pose lHindTentacle01a = pose.apply("lHindTentacle01a");
        Pose lHindTentacle01b = pose.apply("lHindTentacle01b");
        Pose lHindTentacle01c = pose.apply("lHindTentacle01c");
        Pose lHindTentacle01d = pose.apply("lHindTentacle01d");
        Pose lHindTentacle01e = pose.apply("lHindTentacle01e");
        Pose lHindTentacle02a = pose.apply("lHindTentacle02a");
        Pose lHindTentacle02b = pose.apply("lHindTentacle02b");
        Pose lHindTentacle02c = pose.apply("lHindTentacle02c");
        Pose lHindTentacle02d = pose.apply("lHindTentacle02d");
        Pose lHindTentacle02e = pose.apply("lHindTentacle02e");
        Pose lHindTentacle03a = pose.apply("lHindTentacle03a");
        Pose lHindTentacle03b = pose.apply("lHindTentacle03b");
        Pose lHindTentacle03c = pose.apply("lHindTentacle03c");
        Pose rHindTentacle01a = pose.apply("rHindTentacle01a");
        Pose rHindTentacle01b = pose.apply("rHindTentacle01b");
        Pose rHindTentacle01c = pose.apply("rHindTentacle01c");
        Pose rHindTentacle01d = pose.apply("rHindTentacle01d");
        Pose rHindTentacle01e = pose.apply("rHindTentacle01e");
        Pose rHindTentacle02a = pose.apply("rHindTentacle02a");
        Pose rHindTentacle02b = pose.apply("rHindTentacle02b");
        Pose rHindTentacle02c = pose.apply("rHindTentacle02c");
        Pose rHindTentacle02d = pose.apply("rHindTentacle02d");
        Pose rHindTentacle02e = pose.apply("rHindTentacle02e");
        Pose rHindTentacle03a = pose.apply("rHindTentacle03a");
        Pose rHindTentacle03b = pose.apply("rHindTentacle03b");
        Pose rHindTentacle03c = pose.apply("rHindTentacle03c");
        Pose tail01a = pose.apply("tail01a");
        Pose tail01b = pose.apply("tail01b");
        Pose tail01c = pose.apply("tail01c");
        Pose tail01d = pose.apply("tail01d");
        Pose tail01e = pose.apply("tail01e");
        Pose lBackTentacle01a = pose.apply("lBackTentacle01a");
        Pose lBackTentacle01b = pose.apply("lBackTentacle01b");
        Pose lBackTentacle01c = pose.apply("lBackTentacle01c");
        Pose lBackTentacle02a = pose.apply("lBackTentacle02a");
        Pose lBackTentacle02b = pose.apply("lBackTentacle02b");
        Pose lBackTentacle02c = pose.apply("lBackTentacle02c");
        Pose lBackTentacle02a_1 = pose.apply("lBackTentacle02a_1");
        Pose lBackTentacle02b_1 = pose.apply("lBackTentacle02b_1");
        Pose lBackTentacle02c_1 = pose.apply("lBackTentacle02c_1");
        Pose rBackTentacle01a = pose.apply("rBackTentacle01a");
        Pose rBackTentacle01b = pose.apply("rBackTentacle01b");
        Pose rBackTentacle01c = pose.apply("rBackTentacle01c");
        Pose rBackTentacle02a = pose.apply("rBackTentacle02a");
        Pose rBackTentacle02b = pose.apply("rBackTentacle02b");
        Pose rBackTentacle02c = pose.apply("rBackTentacle02c");
        Pose rBackTentacle02a_1 = pose.apply("rBackTentacle02a_1");
        Pose rBackTentacle02b_1 = pose.apply("rBackTentacle02b_1");
        Pose rBackTentacle02c_1 = pose.apply("rBackTentacle02c_1");
        Pose rArm01a = pose.apply("rArm01a");
        Pose rArm01b = pose.apply("rArm01b");
        Pose rArm01c = pose.apply("rArm01c");
        Pose rArm01d = pose.apply("rArm01d");
        Pose lArm01a = pose.apply("lArm01a");
        Pose lArm01b = pose.apply("lArm01b");
        Pose lArm01c = pose.apply("lArm01c");
        Pose lArm01d = pose.apply("lArm01d");
        Pose rBodyTentacle01a = pose.apply("rBodyTentacle01a");
        Pose rBodyTentacle01b = pose.apply("rBodyTentacle01b");
        Pose rBodyTentacle01c = pose.apply("rBodyTentacle01c");
        Pose lBodyTentacle01a = pose.apply("lBodyTentacle01a");
        Pose lBodyTentacle01b = pose.apply("lBodyTentacle01b");
        Pose lBodyTentacle01c = pose.apply("lBodyTentacle01c");
        Pose lBodyTentacle02a = pose.apply("lBodyTentacle02a");
        Pose lBodyTentacle02b = pose.apply("lBodyTentacle02b");
        Pose lBodyTentacle02c = pose.apply("lBodyTentacle02c");
        Pose lBodyTentacle03a = pose.apply("lBodyTentacle03a");
        Pose lBodyTentacle03b = pose.apply("lBodyTentacle03b");
        Pose lBodyTentacle04a = pose.apply("lBodyTentacle04a");
        Pose lBodyTentacle04b = pose.apply("lBodyTentacle04b");
        Pose rBodyTentacle02a = pose.apply("rBodyTentacle02a");
        Pose rBodyTentacle02b = pose.apply("rBodyTentacle02b");
        Pose rBodyTentacle02c = pose.apply("rBodyTentacle02c");
        Pose rBodyTentacle03a = pose.apply("rBodyTentacle03a");
        Pose rBodyTentacle03b = pose.apply("rBodyTentacle03b");
        Pose rBodyTentacle04a = pose.apply("rBodyTentacle04a");
        Pose rBodyTentacle04b = pose.apply("rBodyTentacle04b");

        float limbSwing = entity.walkAnimation.position(partialTick);
        float limbSwingAmount = Math.min(entity.walkAnimation.speed(partialTick), 1.0F);
        float ageInTicks = entity.tickCount + partialTick;
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = Mth.wrapDegrees(headYaw - bodyYaw);
        float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float swingProgress = entity.getAttackAnim(partialTick);

        headJoint.yRot = netHeadYaw / (180F / (float) Math.PI);
        headJoint.xRot = headPitch / (180F / (float) Math.PI);

        mouth01Upper.xRot = -Mth.cos(ageInTicks * 0.25F) * 0.2F + 0.25F;
        mouth01Upper.yRot = 0.0F;
        mouth01Lower.xRot = 0.2617993877991494F
            + (Mth.cos(ageInTicks * 0.25F) * 0.2F + 0.25F) * 2;
        mouth01Lower.yRot = 0.0F;
        mouth02Upper.xRot = -Mth.cos(ageInTicks * 0.23F) * 0.2F + 0.25F;
        mouth02Upper.yRot = 0.0F;
        mouth02Lower.xRot = 0.2617993877991494F
            + (Mth.cos(ageInTicks * 0.23F) * 0.2F + 0.25F) * 2;
        mouth02Lower.yRot = 0.0F;
        mouth03Upper.xRot = -Mth.cos(ageInTicks * 0.27F) * 0.2F + 0.25F;
        mouth03Upper.yRot = 0.0F;
        mouth03Lower.xRot = 0.2617993877991494F
            + (Mth.cos(ageInTicks * 0.27F) * 0.2F + 0.25F) * 2;
        mouth03Lower.yRot = 0.0F;

        float animation = Mth.sin((limbSwing * 0.4F + 2) * 1.5F) * 0.3F * limbSwingAmount * 0.3F;
        float flap = Mth.sin(entity.tickCount * 0.2F) * 0.3F;
        float flap2 = Mth.cos(entity.tickCount * 0.2F) * 0.4F;

        lHindTentacle01a.yRot = flap * 0.05F + 0.1F + animation * 0.4F;
        lHindTentacle01b.yRot = lHindTentacle01a.yRot * 1.5F - 0.1F;
        lHindTentacle01c.yRot = lHindTentacle01b.yRot * 1.75F - 0.1F;
        lHindTentacle01d.yRot = lHindTentacle01c.yRot * 2F;
        lHindTentacle01e.yRot = lHindTentacle01d.yRot * 2.25F;
        lHindTentacle02a.yRot = flap * 0.05F + 0.3F + animation * 0.4F;
        lHindTentacle02b.yRot = lHindTentacle02a.yRot * 1.5F - 0.35F;
        lHindTentacle02c.yRot = lHindTentacle02b.yRot * 1.65F - 0.2F;
        lHindTentacle02d.yRot = lHindTentacle02c.yRot * 1.8F - 0.1F;
        lHindTentacle02e.yRot = lHindTentacle02d.yRot * 2.05F;
        lHindTentacle03a.yRot = flap * 0.05F + 0.9F + animation * 0.4F;
        lHindTentacle03b.yRot = lHindTentacle03a.yRot * 1.5F - 1.7F;
        lHindTentacle03c.yRot = lHindTentacle03b.yRot * 1.75F + 0.3F;

        rHindTentacle01a.yRot = flap * 0.05F - 0.1F + animation * 0.4F;
        rHindTentacle01b.yRot = rHindTentacle01a.yRot * 1.5F + 0.1F;
        rHindTentacle01c.yRot = rHindTentacle01b.yRot * 1.75F + 0.1F;
        rHindTentacle01d.yRot = rHindTentacle01c.yRot * 2F;
        rHindTentacle01e.yRot = rHindTentacle01d.yRot * 2.25F;
        rHindTentacle02a.yRot = flap * 0.05F - 0.3F + animation * 0.4F;
        rHindTentacle02b.yRot = rHindTentacle02a.yRot * 1.5F + 0.35F;
        rHindTentacle02c.yRot = rHindTentacle02b.yRot * 1.65F + 0.2F;
        rHindTentacle02d.yRot = rHindTentacle02c.yRot * 1.8F + 0.1F;
        rHindTentacle02e.yRot = rHindTentacle02d.yRot * 2.05F;
        rHindTentacle03a.yRot = flap * 0.05F - 0.9F + animation * 0.4F;
        rHindTentacle03b.yRot = rHindTentacle03a.yRot * 1.5F + 1.7F;
        rHindTentacle03c.yRot = rHindTentacle03b.yRot * 1.75F - 0.3F;

        tail01a.yRot = flap * 0.1F + animation * 0.4F;
        tail01b.yRot = tail01a.yRot * 1.5F;
        tail01c.yRot = tail01b.yRot * 1.75F;
        tail01d.yRot = tail01c.yRot * 2F;
        tail01e.yRot = tail01d.yRot * 2.25F;

        chainBack(lBackTentacle01a, lBackTentacle01b, lBackTentacle01c,
            flap * 0.2F + animation * 0.5F, -0.13962634F, -0.41887902F, -0.13962634F, flap2);
        chainBack(lBackTentacle02a, lBackTentacle02b, lBackTentacle02c,
            flap * 0.2F + animation * 0.5F, -0.5235988F, -0.2443461F, 0.15707964F, flap2);
        chainBack(lBackTentacle02a_1, lBackTentacle02b_1, lBackTentacle02c_1,
            flap * 0.2F + animation * 0.5F, -0.7330383F, -0.41887902F, -0.13962634F, flap2);
        chainBack(rBackTentacle01a, rBackTentacle01b, rBackTentacle01c,
            -flap * 0.2F + animation * 0.5F, -0.13962634F, -0.41887902F, -0.13962634F, flap2);
        chainBack(rBackTentacle02a, rBackTentacle02b, rBackTentacle02c,
            -flap * 0.2F + animation * 0.5F, -0.7330383F, -0.41887902F, -0.13962634F, flap2);
        chainBack(rBackTentacle02a_1, rBackTentacle02b_1, rBackTentacle02c_1,
            -flap * 0.2F + animation * 0.5F, -0.5235988F, -0.2443461F, 0.15707964F, flap2);

        rArm01a.xRot = -0.2443461F + Mth.cos(limbSwing * 0.6662F + (float) Math.PI)
            * limbSwingAmount;
        lArm01a.xRot = -0.2443461F + Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        lArm01a.yRot = flap * 0.1F - 0.3F;
        lArm01b.yRot = lArm01a.yRot * 1.5F + 0.36F;
        lArm01c.yRot = lArm01b.yRot * 1.75F + 0.2F;
        lArm01d.yRot = lArm01c.yRot * 2F + 0.1F;
        rArm01a.yRot = -flap * 0.1F + 0.3F;
        rArm01b.yRot = rArm01a.yRot * 1.5F - 0.36F;
        rArm01c.yRot = rArm01b.yRot * 1.75F - 0.2F;
        rArm01d.yRot = rArm01c.yRot * 2F - 0.1F;
        rArm01a.zRot = 0.0F;
        lArm01a.zRot = 0.0F;

        rBodyTentacle01a.xRot = 0.36651915F
            + Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
        lBodyTentacle01a.xRot = 0.36651915F + Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        rBodyTentacle01a.yRot = -flap * 0.1F + 0.78F;
        rBodyTentacle01b.yRot = rBodyTentacle01a.yRot * 1.5F - 1.26F;
        rBodyTentacle01c.yRot = rBodyTentacle01b.yRot * 1.75F + 0.1F;
        lBodyTentacle01a.yRot = flap * 0.1F - 0.78F;
        lBodyTentacle01b.yRot = lBodyTentacle01a.yRot * 1.5F + 1.26F;
        lBodyTentacle01c.yRot = lBodyTentacle01b.yRot * 1.75F - 0.1F;
        rBodyTentacle01a.zRot = 0.0F;
        lBodyTentacle01a.zRot = 0.0F;

        chainY(lBodyTentacle02a, lBodyTentacle02b, lBodyTentacle02c,
            flap * 0.2F + animation, 0.0F);
        lBodyTentacle03a.yRot = -flap * 0.3F - 0.35F + animation;
        lBodyTentacle03b.yRot = lBodyTentacle03a.yRot * 1.5F + 0.8F;
        lBodyTentacle04a.yRot = -flap * 0.4F - 0.08F + animation;
        lBodyTentacle04b.yRot = lBodyTentacle04a.yRot * 1.5F;
        chainY(rBodyTentacle02a, rBodyTentacle02b, rBodyTentacle02c,
            -flap * 0.2F + animation, 0.0F);
        rBodyTentacle03a.yRot = flap * 0.3F + 0.33F + animation;
        rBodyTentacle03b.yRot = rBodyTentacle03a.yRot * 1.5F - 0.8F;
        rBodyTentacle04a.yRot = flap * 0.4F + 0.03F + animation;
        rBodyTentacle04b.yRot = rBodyTentacle04a.yRot * 1.5F;

        float ease = 1.0F - swingProgress;
        ease *= ease;
        ease *= ease;
        ease = 1.0F - ease;
        float attackWave = Mth.sin(ease * (float) Math.PI);
        float pitchCoupling = Mth.sin(swingProgress * (float) Math.PI)
            * -(headJoint.xRot - 0.7F) * 0.75F;
        float attackX = attackWave * 1.2F + pitchCoupling;
        float attackZ = Mth.sin(swingProgress * (float) Math.PI) * 0.4F;
        rArm01a.xRot += attackX;
        rBodyTentacle01a.xRot += attackX;
        rArm01a.zRot = -attackZ;
        rBodyTentacle01a.zRot = -attackZ;
        lArm01a.xRot += attackX;
        lBodyTentacle01a.xRot += attackX;
        lArm01a.zRot = attackZ;
        lBodyTentacle01a.zRot = attackZ;

        poses.forEach(Pose::apply);
    }

    private static void chainBack(Pose a, Pose b, Pose c, float y,
                                  float baseX, float middleX, float endX, float flap2) {
        a.yRot = y;
        b.yRot = a.yRot * 1.5F;
        c.yRot = b.yRot * 1.75F;
        a.xRot = baseX - flap2 * 0.5F;
        b.xRot = middleX - flap2 * 0.75F;
        c.xRot = endX - flap2;
    }

    private static void chainY(Pose a, Pose b, Pose c, float y, float offset) {
        a.yRot = y;
        b.yRot = a.yRot * 1.5F + offset;
        c.yRot = b.yRot * 1.75F;
    }

    private static final class Pose {
        private final GeoBone bone;
        float xRot;
        float yRot;
        float zRot;

        Pose(GeoBone bone) {
            this.bone = bone;
            this.xRot = -bone.getInitialSnapshot().getRotX();
            this.yRot = -bone.getInitialSnapshot().getRotY();
            this.zRot = bone.getInitialSnapshot().getRotZ();
        }

        void apply() {
            bone.setRotX(-xRot);
            bone.setRotY(-yRot);
            bone.setRotZ(zRot);
        }
    }
}