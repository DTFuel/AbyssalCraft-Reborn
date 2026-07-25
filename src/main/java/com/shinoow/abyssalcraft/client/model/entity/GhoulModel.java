package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.Mob;

/**
 * Ghoul entity model (owned by PE-3, Stage E2). Modern {@link HierarchicalModel} port of the 1.12.2
 * {@code ModelGhoul} (128x64), shared by all five ghoul entities (per-entity texture in the renderer).
 *
 * <p><b>Full faithful port</b>: every 1.12.2 part is transcribed with its exact texture offset,
 * coordinates, mirror flag and rotation (head + jaw + five teeth, pelvis / spine / neck torso with
 * ribs, sides, back and head-joint, two two-segment fingered arms, two digitigrade legs), generated
 * from the original constructor by {@code scripts/convert_modelbase_to_entitymodel.js}. {@link #setupAnim}
 * ports {@code setRotationAngles} including the standing/moving pose swap (the runtime part-position
 * overrides 1.12.2 applied every frame, which the constructor coordinates alone do not capture).
 */
public class GhoulModel<T extends Mob> extends BrightnessAlphaModel<T> implements HeadedModel, ArmedModel {

    private static final float PI = (float) Math.PI;
    private static final float DEG = PI / 180.0F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart shoulders;
    private final ModelPart pelvis;
    private final ModelPart lleg;
    private final ModelPart rleg;
    private final ModelPart lleg2;
    private final ModelPart rleg2;
        private final ModelPart rarm1;
        private final ModelPart larm1;

    public GhoulModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.shoulders = root.getChild("shoulders");
        this.pelvis = root.getChild("pelvis");
        this.lleg = root.getChild("lleg");
        this.rleg = root.getChild("rleg");
        this.lleg2 = this.lleg.getChild("lleg2");
        this.rleg2 = this.rleg.getChild("rleg2");
        this.rarm1 = this.shoulders.getChild("rarm1");
        this.larm1 = this.shoulders.getChild("larm1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -9.5F, -4.5F, 9, 9, 9),
                PartPose.offset(0.0F, 6.5F, -20.2F));
        PartDefinition jaw = head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(36, 0).addBox(-4.5F, -0.5F, -4.5F, 9, 1, 9),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.2365560978651047F, 0.0F, 0.0F));
        jaw.addOrReplaceChild("tooth4",
                CubeListBuilder.create().texOffs(48, 11).addBox(-0.5F, -1.0F, -0.5F, 1, 2, 1),
                PartPose.offset(2.0F, -1.5F, -4.0F));
        jaw.addOrReplaceChild("tooth1",
                CubeListBuilder.create().texOffs(48, 11).addBox(-0.5F, -1.0F, -0.5F, 1, 2, 1),
                PartPose.offset(-4.0F, -1.5F, -4.0F));
        jaw.addOrReplaceChild("tooth5",
                CubeListBuilder.create().texOffs(48, 11).addBox(-0.5F, -1.0F, -0.5F, 1, 2, 1),
                PartPose.offset(4.0F, -1.5F, -4.0F));
        jaw.addOrReplaceChild("tooth2",
                CubeListBuilder.create().texOffs(48, 11).addBox(-0.5F, -1.0F, -0.5F, 1, 2, 1),
                PartPose.offset(-2.0F, -1.5F, -4.0F));
        jaw.addOrReplaceChild("tooth3",
                CubeListBuilder.create().texOffs(48, 11).addBox(-0.5F, -1.0F, -0.5F, 1, 2, 1),
                PartPose.offset(0.0F, -1.5F, -4.0F));

        PartDefinition lleg = root.addOrReplaceChild("lleg",
                CubeListBuilder.create().mirror().texOffs(16, 40).addBox(-2.1F, 0.0F, -2.1F, 4, 12, 4),
                PartPose.offsetAndRotation(3.5F, 6.4F, 4.0F, -0.8726646259971648F, -0.7853981633974483F, 0.0F));
        lleg.addOrReplaceChild("lleg2",
                CubeListBuilder.create().mirror().texOffs(32, 40).addBox(-2.1F, 0.1F, -3.2F, 4, 12, 4),
                PartPose.offsetAndRotation(0.0F, 9.4F, -0.2F, 0.8726646259971648F, 0.0F, 0.0F));
        PartDefinition rleg = root.addOrReplaceChild("rleg",
                CubeListBuilder.create().texOffs(16, 40).addBox(-2.1F, 0.0F, -2.2F, 4, 12, 4),
                PartPose.offsetAndRotation(-3.3F, 6.4F, 4.1F, -0.8726646259971648F, 0.7853981633974483F, 0.0F));
        rleg.addOrReplaceChild("rleg2",
                CubeListBuilder.create().texOffs(32, 40).addBox(-2.1F, 0.1F, -3.3F, 4, 12, 4),
                PartPose.offsetAndRotation(0.0F, 9.4F, -0.2F, 0.8726646259971648F, 0.0F, 0.0F));

        PartDefinition shoulders = root.addOrReplaceChild("shoulders",
                CubeListBuilder.create().texOffs(54, 12).addBox(-8.0F, -2.0F, -2.0F, 16, 4, 4),
                PartPose.offsetAndRotation(0.0F, 5.5F, -14.53F, 1.6997761585172775F, 0.0F, 0.0F));
        PartDefinition rarm1 = shoulders.addOrReplaceChild("rarm1",
                CubeListBuilder.create().texOffs(56, 22).addBox(-4.0F, -2.0F, -2.0F, 4, 12, 4),
                PartPose.offsetAndRotation(-8.0F, 0.8F, -0.8F, -1.48352986419518F, 0.0F, 0.0F));
        PartDefinition rarm2 = rarm1.addOrReplaceChild("rarm2",
                CubeListBuilder.create().texOffs(72, 26).addBox(-2.0F, 1.0F, 3.0F, 4, 8, 4),
                PartPose.offsetAndRotation(-2.0F, 4.0F, -2.0F, -0.8290313946973066F, 0.0F, 0.0F));
        rarm2.addOrReplaceChild("rfinger2",
                CubeListBuilder.create().texOffs(50, 34).addBox(-0.5F, -0.5F, -1.0F, 1, 5, 1),
                PartPose.offset(1.0F, 8.0F, 3.0F));
        rarm2.addOrReplaceChild("rfinger4",
                CubeListBuilder.create().texOffs(50, 34).addBox(-1.0F, -0.5F, -1.0F, 1, 5, 1),
                PartPose.offset(3.0F, 8.0F, 5.0F));
        rarm2.addOrReplaceChild("rfinger1",
                CubeListBuilder.create().texOffs(50, 34).addBox(-0.5F, -0.5F, -1.0F, 1, 5, 1),
                PartPose.offset(-1.0F, 8.0F, 3.0F));
        rarm2.addOrReplaceChild("rfinger3",
                CubeListBuilder.create().texOffs(50, 34).addBox(0.0F, -0.5F, -1.0F, 1, 5, 1),
                PartPose.offset(-3.0F, 8.0F, 5.0F));
        PartDefinition larm1 = shoulders.addOrReplaceChild("larm1",
                CubeListBuilder.create().mirror().texOffs(56, 22).addBox(0.0F, -2.0F, -2.0F, 4, 12, 4),
                PartPose.offsetAndRotation(8.0F, 0.8F, -0.8F, -1.48352986419518F, 0.0F, 0.0F));
        PartDefinition larm2 = larm1.addOrReplaceChild("larm2",
                CubeListBuilder.create().mirror().texOffs(72, 26).addBox(-2.0F, 1.0F, 3.0F, 4, 8, 4),
                PartPose.offsetAndRotation(2.0F, 4.0F, -2.0F, -0.8290313946973066F, 0.0F, 0.0F));
        larm2.addOrReplaceChild("lfinger2",
                CubeListBuilder.create().mirror().texOffs(50, 34).addBox(-0.5F, -0.5F, -1.0F, 1, 5, 1),
                PartPose.offset(1.0F, 8.0F, 3.0F));
        larm2.addOrReplaceChild("lfinger4",
                CubeListBuilder.create().mirror().texOffs(50, 34).addBox(0.0F, -0.5F, -1.0F, 1, 5, 1),
                PartPose.offset(2.0F, 8.0F, 5.0F));
        larm2.addOrReplaceChild("lfinger3",
                CubeListBuilder.create().mirror().texOffs(50, 34).addBox(-1.0F, -0.5F, -1.0F, 1, 5, 1),
                PartPose.offset(-2.0F, 8.0F, 5.0F));
        larm2.addOrReplaceChild("lfinger1",
                CubeListBuilder.create().mirror().texOffs(50, 34).addBox(-0.5F, -0.5F, -1.0F, 1, 5, 1),
                PartPose.offset(-1.0F, 8.0F, 3.0F));

        PartDefinition pelvis = root.addOrReplaceChild("pelvis",
                CubeListBuilder.create().texOffs(72, 0).addBox(-5.0F, -1.0F, -3.0F, 10, 6, 5),
                PartPose.offsetAndRotation(0.0F, 4.69F, 1.0F, 1.5707963267948966F, 0.0F, 0.0F));
        PartDefinition spine = pelvis.addOrReplaceChild("spine",
                CubeListBuilder.create().texOffs(0, 44).addBox(-2.0F, -8.0F, -3.0F, 4, 14, 4),
                PartPose.offset(0.0F, -7.0F, 0.5F));
        spine.addOrReplaceChild("back",
                CubeListBuilder.create().texOffs(0, 18).addBox(-5.0F, -5.5F, -1.0F, 10, 13, 0),
                PartPose.offset(0.0F, -1.5F, 2.51F));
        spine.addOrReplaceChild("lrib1",
                CubeListBuilder.create().mirror().texOffs(39, 35).addBox(-2.0F, -1.0F, -1.0F, 3, 2, 2),
                PartPose.offset(4.0F, -3.58F, -0.5F));
        spine.addOrReplaceChild("rrib2",
                CubeListBuilder.create().texOffs(39, 35).addBox(-1.0F, -1.0F, -1.0F, 3, 2, 2),
                PartPose.offset(-4.0F, -0.38F, -0.5F));
        spine.addOrReplaceChild("rside",
                CubeListBuilder.create().texOffs(40, 13).addBox(0.99F, -5.5F, -3.0F, 0, 13, 5),
                PartPose.offset(-6.0F, -1.5F, -0.49F));
        spine.addOrReplaceChild("lrib2",
                CubeListBuilder.create().mirror().texOffs(39, 35).addBox(-2.0F, -1.0F, -1.0F, 3, 2, 2),
                PartPose.offset(4.0F, -0.38F, -0.5F));
        spine.addOrReplaceChild("rrib3",
                CubeListBuilder.create().texOffs(39, 35).addBox(-1.0F, -1.0F, -1.0F, 3, 2, 2),
                PartPose.offset(-4.0F, 2.82F, -0.5F));
        PartDefinition neck = spine.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 44).addBox(-2.0F, -4.0F, -3.0F, 4, 8, 4),
                PartPose.offsetAndRotation(0.0F, -11.0F, -0.95F, 0.2617993877991494F, 0.0F, 0.0F));
        neck.addOrReplaceChild("headJoint",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -1.0471975511965976F, 0.0F, 0.0F));
        spine.addOrReplaceChild("lrib3",
                CubeListBuilder.create().mirror().texOffs(39, 35).addBox(-2.0F, -1.0F, -1.0F, 3, 2, 2),
                PartPose.offset(4.0F, 2.82F, -0.5F));
        spine.addOrReplaceChild("rrib1",
                CubeListBuilder.create().texOffs(39, 35).addBox(-1.0F, -1.0F, -1.0F, 3, 2, 2),
                PartPose.offset(-4.0F, -3.58F, -0.5F));
        spine.addOrReplaceChild("lside",
                CubeListBuilder.create().texOffs(30, 13).addBox(-0.99F, -5.5F, -3.0F, 0, 13, 5),
                PartPose.offset(6.0F, -1.5F, -0.49F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
        public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Faithful port of 1.12.2 ModelGhoul#setRotationAngles: head track + a walk/idle pose swap
        // (standing still -> a hunched crouch). 1.12.2 keyed this off posX==prevPosX...; limbSwingAmount
        // is the modern "is moving" proxy. The tiny idle offsetY nudges (0.25-0.35px) have no modern
        // ModelPart equivalent and are omitted.
        head.yRot = netHeadYaw * DEG;
        head.xRot = headPitch * DEG;

        float f = limbSwingAmount > 0.01F ? 1.0F : 0.0F;

        head.y = f == 0.0F ? -10.0F : 6.5F;
        head.z = f == 0.0F ? -9.2F : -14.2F;

        shoulders.y = f == 0.0F ? -7.5F : 5.5F;
        shoulders.z = f == 0.0F ? -4.7F : -8.53F;
        shoulders.xRot = f == 0.0F ? 0.9143779951198293F : 1.6997761585172775F;

        rleg.xRot = -0.8726646259971648F + Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        rleg.yRot = 0.7853981633974483F;
        lleg.xRot = -0.8726646259971648F + Mth.cos(limbSwing * 0.6662F + PI) * 1.4F * limbSwingAmount;
        lleg.yRot = -0.7853981633974483F;
        if (f == 0.0F) {
            rleg.xRot = -1.4726646259971648F;
            lleg.xRot = -1.4726646259971648F;
        }

        pelvis.xRot = f == 0.0F ? 0.7853981633974483F : 1.5707963267948966F;

        lleg2.xRot = 0.8726646259971648F + (f == 0.0F ? 1.3F : 0.0F);
        rleg2.xRot = 0.8726646259971648F + (f == 0.0F ? 1.3F : 0.0F);

        pelvis.z = f == 1.0F ? 5.0F : 7.0F;
        lleg.z = f == 1.0F ? 8.5F : 9.5F;
        rleg.z = f == 1.0F ? 8.5F : 9.5F;
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
                shoulders.translateAndRotate(poseStack);
                (arm == HumanoidArm.RIGHT ? rarm1 : larm1).translateAndRotate(poseStack);
        }
}
