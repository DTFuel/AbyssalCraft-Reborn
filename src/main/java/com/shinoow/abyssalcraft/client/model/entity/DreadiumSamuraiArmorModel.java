package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

public final class DreadiumSamuraiArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

    public DreadiumSamuraiArmorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer(float deformation) {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(deformation), 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("crest_front", CubeListBuilder.create().texOffs(0, 32)
            .addBox(-2.0F, -7.0F, -6.0F, 4, 1, 1), PartPose.ZERO);
        head.addOrReplaceChild("crest_right", CubeListBuilder.create().texOffs(10, 32)
            .addBox(1.0F, -10.0F, -6.0F, 1, 3, 1), PartPose.ZERO);
        head.addOrReplaceChild("crest_left", CubeListBuilder.create().texOffs(14, 32)
            .addBox(-2.0F, -10.0F, -6.0F, 1, 3, 1), PartPose.ZERO);
        head.addOrReplaceChild("guard_right", CubeListBuilder.create().texOffs(18, 32)
            .addBox(2.0F, -6.0F, -7.0F, 3, 3, 1),
            PartPose.rotation(0.0F, -0.5576792F, -0.185895F));
        head.addOrReplaceChild("guard_left", CubeListBuilder.create().mirror().texOffs(18, 32)
            .addBox(-5.0F, -6.0F, -7.0F, 3, 3, 1),
            PartPose.rotation(0.0F, 0.5576851F, 0.1858931F));

        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(26, 32)
            .addBox(-3.0F, 1.0F, -4.0F, 6, 9, 1), PartPose.ZERO);
        body.addOrReplaceChild("chest_pin_right", CubeListBuilder.create().texOffs(22, 36)
            .addBox(2.0F, 0.0F, -4.0F, 1, 1, 1), PartPose.ZERO);
        body.addOrReplaceChild("chest_pin_left", CubeListBuilder.create().texOffs(22, 36)
            .addBox(-3.0F, 0.0F, -4.0F, 1, 1, 1), PartPose.ZERO);
        body.addOrReplaceChild("waist_guard", CubeListBuilder.create().texOffs(0, 40)
            .addBox(-4.0F, 12.0F, -3.5F, 8, 5, 1), PartPose.ZERO);

        shoulder(root.getChild("left_arm"), true);
        shoulder(root.getChild("right_arm"), false);
        leg(root.getChild("left_leg"), true);
        leg(root.getChild("right_leg"), false);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void shoulder(PartDefinition arm, boolean left) {
        float sign = left ? 1.0F : -1.0F;
        float[] offsets = {4.0F, 3.5F, 3.0F, 2.5F};
        for (int index = 0; index < offsets.length; index++) {
            float x = left ? offsets[index] : -offsets[index] - 1.0F;
            arm.addOrReplaceChild("shoulder_" + index, CubeListBuilder.create().texOffs(0, 34)
                .addBox(x, -2.0F + index * 1.5F, -2.0F, 1, 2, 4),
                PartPose.rotation(0.0F, 0.0F, -sign * 0.44614F));
        }
    }

    private static void leg(PartDefinition leg, boolean left) {
        leg.addOrReplaceChild("side_guard", CubeListBuilder.create().texOffs(18, 38)
            .addBox(left ? 2.5F : -3.5F, 0.0F, -2.0F, 1, 8, 4), PartPose.ZERO);
        leg.addOrReplaceChild("rear_guard", CubeListBuilder.create().texOffs(0, 46)
            .addBox(-2.0F, 0.0F, 2.5F, 4, 5, 1), PartPose.ZERO);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof ArmorStand stand)) {
            head.setPos(0.0F, 0.0F, 0.0F);
            hat.setPos(0.0F, 0.0F, 0.0F);
            rightLeg.setPos(-1.9F, 12.0F, 0.0F);
            leftLeg.setPos(1.9F, 12.0F, 0.0F);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            return;
        }

        copyPose(head, stand.getHeadPose());
        head.setPos(0.0F, 1.0F, 0.0F);
        copyPose(body, stand.getBodyPose());
        copyPose(leftArm, stand.getLeftArmPose());
        copyPose(rightArm, stand.getRightArmPose());
        copyPose(leftLeg, stand.getLeftLegPose());
        leftLeg.setPos(1.9F, 11.0F, 0.0F);
        copyPose(rightLeg, stand.getRightLegPose());
        rightLeg.setPos(-1.9F, 11.0F, 0.0F);
        hat.copyFrom(head);
    }

    private static void copyPose(ModelPart part, net.minecraft.core.Rotations pose) {
        part.xRot = pose.getX() * ((float) Math.PI / 180.0F);
        part.yRot = pose.getY() * ((float) Math.PI / 180.0F);
        part.zRot = pose.getZ() * ((float) Math.PI / 180.0F);
    }
}
