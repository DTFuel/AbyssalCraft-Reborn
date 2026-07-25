package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;

/**
 * Full faithful port of 1.12.2 ModelShadowBeast (128x64). Geometry, part
 * hierarchy and the setRotationAngles animation are transcribed verbatim by
 * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).
 */
public class LegacyShadowBeastModel extends BrightnessAlphaModel<LegacyHostileMob> implements HeadedModel {

        private static final float DEG = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart spine;
    private final ModelPart leftshoulder;
    private final ModelPart lsspike;
    private final ModelPart larm1;
    private final ModelPart rightshoulder;
    private final ModelPart rsspike;
    private final ModelPart rarm1;
    private final ModelPart pelvis;
    private final ModelPart leftleg;
    private final ModelPart rightleg;
    private final ModelPart btooth1;
    private final ModelPart btooth2;
    private final ModelPart btooth3;
    private final ModelPart btooth4;
    private final ModelPart btooth5;
    private final ModelPart jaw;
    private final ModelPart larm2;
    private final ModelPart laspike1;
    private final ModelPart laspike2;
    private final ModelPart lfinger1;
    private final ModelPart lfinger3;
    private final ModelPart lfinger2;
    private final ModelPart lfinger4;
    private final ModelPart rarm2;
    private final ModelPart raspike1;
    private final ModelPart raspike2;
    private final ModelPart rfinger1;
    private final ModelPart rfinger2;
    private final ModelPart rfinger3;
    private final ModelPart rfinger4;
    private final ModelPart stooth1;
    private final ModelPart stooth2;
    private final ModelPart stooth3;
    private final ModelPart stooth4;

    public LegacyShadowBeastModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.spine = root.getChild("spine");
        this.leftshoulder = root.getChild("leftshoulder");
        this.lsspike = root.getChild("lsspike");
        this.larm1 = root.getChild("larm1");
        this.rightshoulder = root.getChild("rightshoulder");
        this.rsspike = root.getChild("rsspike");
        this.rarm1 = root.getChild("rarm1");
        this.pelvis = root.getChild("pelvis");
        this.leftleg = root.getChild("leftleg");
        this.rightleg = root.getChild("rightleg");
        this.btooth1 = this.head.getChild("btooth1");
        this.btooth2 = this.head.getChild("btooth2");
        this.btooth3 = this.head.getChild("btooth3");
        this.btooth4 = this.head.getChild("btooth4");
        this.btooth5 = this.head.getChild("btooth5");
        this.jaw = this.head.getChild("jaw");
        this.larm2 = this.larm1.getChild("larm2");
        this.laspike1 = this.larm1.getChild("laspike1");
        this.laspike2 = this.larm1.getChild("laspike2");
        this.lfinger1 = this.larm1.getChild("lfinger1");
        this.lfinger3 = this.larm1.getChild("lfinger3");
        this.lfinger2 = this.larm1.getChild("lfinger2");
        this.lfinger4 = this.larm1.getChild("lfinger4");
        this.rarm2 = this.rarm1.getChild("rarm2");
        this.raspike1 = this.rarm1.getChild("raspike1");
        this.raspike2 = this.rarm1.getChild("raspike2");
        this.rfinger1 = this.rarm1.getChild("rfinger1");
        this.rfinger2 = this.rarm1.getChild("rfinger2");
        this.rfinger3 = this.rarm1.getChild("rfinger3");
        this.rfinger4 = this.rarm1.getChild("rfinger4");
        this.stooth1 = this.jaw.getChild("stooth1");
        this.stooth2 = this.jaw.getChild("stooth2");
        this.stooth3 = this.jaw.getChild("stooth3");
        this.stooth4 = this.jaw.getChild("stooth4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-4.5F, -9.0F, -4.5F, 9, 9, 9),
                PartPose.offset(0.0F, -13.0F, -1.0F));
        PartDefinition btooth1 = head.addOrReplaceChild("btooth1",
                CubeListBuilder.create().mirror().texOffs(37, 4).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offset(-4.5F, 0.0F, -4.5F));
        PartDefinition btooth2 = head.addOrReplaceChild("btooth2",
                CubeListBuilder.create().mirror().texOffs(37, 4).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offset(-2.5F, 0.0F, -4.5F));
        PartDefinition btooth3 = head.addOrReplaceChild("btooth3",
                CubeListBuilder.create().mirror().texOffs(37, 4).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offset(-0.5F, 0.0F, -4.5F));
        PartDefinition btooth4 = head.addOrReplaceChild("btooth4",
                CubeListBuilder.create().mirror().texOffs(37, 4).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offset(1.5F, 0.0F, -4.5F));
        PartDefinition btooth5 = head.addOrReplaceChild("btooth5",
                CubeListBuilder.create().mirror().texOffs(37, 4).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offset(3.5F, 0.0F, -4.5F));
        PartDefinition jaw = head.addOrReplaceChild("jaw",
                CubeListBuilder.create().mirror().texOffs(36, 0).addBox(-4.5F, 1.9F, -5.0F, 9, 1, 9),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4461433F, 0.0F, 0.0F));
        PartDefinition stooth1 = jaw.addOrReplaceChild("stooth1",
                CubeListBuilder.create().mirror().texOffs(37, 4).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-3.5F, 0.9F, -5.0F));
        PartDefinition stooth2 = jaw.addOrReplaceChild("stooth2",
                CubeListBuilder.create().mirror().texOffs(37, 4).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-1.5F, 0.9F, -5.0F));
        PartDefinition stooth3 = jaw.addOrReplaceChild("stooth3",
                CubeListBuilder.create().mirror().texOffs(37, 4).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.5F, 0.9F, -5.0F));
        PartDefinition stooth4 = jaw.addOrReplaceChild("stooth4",
                CubeListBuilder.create().mirror().texOffs(37, 4).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(2.5F, 0.9F, -5.0F));
        PartDefinition spine = root.addOrReplaceChild("spine",
                CubeListBuilder.create().mirror().texOffs(72, 0).addBox(-2.5F, 0.0F, -2.5F, 5, 20, 5),
                PartPose.offsetAndRotation(0.0F, -12.0F, -1.0F, 0.2974289F, 0.0F, 0.0F));
        PartDefinition leftshoulder = root.addOrReplaceChild("leftshoulder",
                CubeListBuilder.create().mirror().texOffs(41, 11).addBox(-8.0F, 0.0F, 0.0F, 8, 5, 7),
                PartPose.offsetAndRotation(9.5F, -8.0F, -3.0F, 0.0F, 0.0F, 0.111544F));
        PartDefinition lsspike = root.addOrReplaceChild("lsspike",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, -4.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(7.0F, -9.0F, 0.0F, 0.0F, 0.0F, 0.111544F));
        PartDefinition larm1 = root.addOrReplaceChild("larm1",
                CubeListBuilder.create().mirror().texOffs(0, 19).addBox(0.0F, -1.0F, -1.5F, 3, 11, 3),
                PartPose.offset(5.0F, -6.5F, 0.5F));
        PartDefinition larm2 = larm1.addOrReplaceChild("larm2",
                CubeListBuilder.create().mirror().texOffs(0, 19).addBox(0.0F, 6.0F, 5.0F, 3, 7, 3),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807508F, 0.0F, 0.0F));
        PartDefinition laspike1 = larm1.addOrReplaceChild("laspike1",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(6.0F, -1.0F, -0.5F, 1, 4, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.8179311F));
        PartDefinition laspike2 = larm1.addOrReplaceChild("laspike2",
                CubeListBuilder.create().mirror().texOffs(34, 0).addBox(-1.0F, 10.0F, 5.5F, 4, 1, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7744724F, -0.2617994F, -0.2617994F));
        PartDefinition lfinger1 = larm1.addOrReplaceChild("lfinger1",
                CubeListBuilder.create().mirror().texOffs(30, 0).addBox(3.0F, 11.0F, 6.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807556F, 0.0F, 0.0F));
        PartDefinition lfinger3 = larm1.addOrReplaceChild("lfinger3",
                CubeListBuilder.create().mirror().texOffs(30, 0).addBox(1.0F, 11.0F, 4.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807556F, 0.0F, 0.0F));
        PartDefinition lfinger2 = larm1.addOrReplaceChild("lfinger2",
                CubeListBuilder.create().mirror().texOffs(30, 0).addBox(-1.0F, 11.0F, 6.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807556F, 0.0F, 0.0F));
        PartDefinition lfinger4 = larm1.addOrReplaceChild("lfinger4",
                CubeListBuilder.create().mirror().texOffs(30, 0).addBox(1.0F, 11.0F, 8.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807556F, 0.0F, 0.0F));
        PartDefinition rightshoulder = root.addOrReplaceChild("rightshoulder",
                CubeListBuilder.create().mirror().texOffs(41, 11).addBox(0.0F, 0.0F, 0.0F, 8, 5, 7),
                PartPose.offsetAndRotation(-9.5F, -8.0F, -3.0F, 0.0F, 0.0F, -0.1115358F));
        PartDefinition rsspike = root.addOrReplaceChild("rsspike",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, -4.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(-8.0F, -9.0F, 0.0F, 0.0F, 0.0F, -0.111544F));
        PartDefinition rarm1 = root.addOrReplaceChild("rarm1",
                CubeListBuilder.create().mirror().texOffs(0, 19).addBox(-3.0F, -1.0F, -1.5F, 3, 11, 3),
                PartPose.offset(-5.0F, -6.5F, 0.5F));
        PartDefinition rarm2 = rarm1.addOrReplaceChild("rarm2",
                CubeListBuilder.create().mirror().texOffs(0, 19).addBox(-3.0F, 6.0F, 5.0F, 3, 7, 3),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807508F, 0.0F, 0.0F));
        PartDefinition raspike1 = rarm1.addOrReplaceChild("raspike1",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-7.0F, -1.0F, -0.5F, 1, 4, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.8179294F));
        PartDefinition raspike2 = rarm1.addOrReplaceChild("raspike2",
                CubeListBuilder.create().mirror().texOffs(34, 0).addBox(-3.0F, 10.0F, 5.5F, 4, 1, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807556F, 0.2617994F, 0.2617994F));
        PartDefinition rfinger1 = rarm1.addOrReplaceChild("rfinger1",
                CubeListBuilder.create().mirror().texOffs(30, 0).addBox(0.0F, 11.0F, 6.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807556F, 0.0F, 0.0F));
        PartDefinition rfinger2 = rarm1.addOrReplaceChild("rfinger2",
                CubeListBuilder.create().mirror().texOffs(30, 0).addBox(-4.0F, 11.0F, 6.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807556F, 0.0F, 0.0F));
        PartDefinition rfinger3 = rarm1.addOrReplaceChild("rfinger3",
                CubeListBuilder.create().mirror().texOffs(30, 0).addBox(-2.0F, 11.0F, 4.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807556F, 0.0F, 0.0F));
        PartDefinition rfinger4 = rarm1.addOrReplaceChild("rfinger4",
                CubeListBuilder.create().mirror().texOffs(30, 0).addBox(-2.0F, 11.0F, 8.0F, 1, 5, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7807556F, 0.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis",
                CubeListBuilder.create().mirror().texOffs(37, 24).addBox(0.0F, 0.0F, -1.0F, 12, 5, 5),
                PartPose.offsetAndRotation(-6.0F, 7.0F, 3.0F, 0.2974216F, 0.0F, 0.0F));
        PartDefinition leftleg = root.addOrReplaceChild("leftleg",
                CubeListBuilder.create().mirror().texOffs(16, 18).addBox(-2.5F, 0.0F, -2.5F, 5, 8, 5),
                PartPose.offsetAndRotation(3.5F, 11.0F, 6.0F, 0.2974216F, 0.0F, 0.0F));
        PartDefinition rightleg = root.addOrReplaceChild("rightleg",
                CubeListBuilder.create().mirror().texOffs(16, 18).addBox(-2.5F, 0.0F, -2.5F, 5, 8, 5),
                PartPose.offsetAndRotation(-3.5F, 11.0F, 6.0F, 0.2974216F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(LegacyHostileMob entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
                                head.yRot = netHeadYaw * DEG;
                                head.xRot = headPitch * DEG;
                                rightleg.xRot = Mth.cos(limbSwing * 0.3331F) * 0.07F * limbSwingAmount;
                                rightleg.yRot = 0.0F;
                                leftleg.xRot = Mth.cos(limbSwing * 0.3331F + (float) Math.PI) * 0.07F * limbSwingAmount;
                                leftleg.yRot = 0.0F;

                                float attack = entity.getAttackAnim(ageInTicks - entity.tickCount);
                                float first = Mth.sin(attack * (float) Math.PI);
                                float second = Mth.sin((1.0F - (1.0F - attack) * (1.0F - attack)) * (float) Math.PI);
                                rarm1.zRot = 0.0F;
                                larm1.zRot = 0.0F;
                                rarm1.yRot = -(0.1F - first * 0.6F);
                                larm1.yRot = 0.1F - first * 0.6F;
                                rarm1.xRot = -((float) Math.PI / 7.2F) - first * 1.2F + second * 0.4F;
                                larm1.xRot = -((float) Math.PI / 7.2F) - first * 1.2F + second * 0.4F;
                                rarm1.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
                                larm1.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
                                rarm1.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
                                larm1.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

        @Override
        public ModelPart getHead() {
                return this.head;
        }
}
