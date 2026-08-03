package com.shinoow.abyssalcraft.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;

/** License-safe placeholder retaining the renderer's Ghoul bone interface. */
public class GhoulModel<T extends Mob> extends BrightnessAlphaModel<T> implements HeadedModel, ArmedModel {

    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart shoulders;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public GhoulModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.shoulders = root.getChild("shoulders");
        root.getChild("pelvis");
        root.getChild("lleg").getChild("lleg2");
        root.getChild("rleg").getChild("rleg2");
        this.rightArm = this.shoulders.getChild("rarm1");
        this.leftArm = this.shoulders.getChild("larm1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose origin = PartPose.offset(0.0F, 0.0F, 0.0F);

        root.addOrReplaceChild("head", CubeListBuilder.create(), origin);
        PartDefinition shoulders = root.addOrReplaceChild("shoulders", CubeListBuilder.create(), origin);
        shoulders.addOrReplaceChild("rarm1", CubeListBuilder.create(), origin);
        shoulders.addOrReplaceChild("larm1", CubeListBuilder.create(), origin);
        PartDefinition leftLeg = root.addOrReplaceChild("lleg", CubeListBuilder.create(), origin);
        leftLeg.addOrReplaceChild("lleg2", CubeListBuilder.create(), origin);
        PartDefinition rightLeg = root.addOrReplaceChild("rleg", CubeListBuilder.create(), origin);
        rightLeg.addOrReplaceChild("rleg2", CubeListBuilder.create(), origin);
        root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8), PartPose.offset(0.0F, 16.0F, 0.0F));

        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * DEG_TO_RAD;
        this.head.xRot = headPitch * DEG_TO_RAD;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.shoulders.translateAndRotate(poseStack);
        (arm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm).translateAndRotate(poseStack);
    }
}
