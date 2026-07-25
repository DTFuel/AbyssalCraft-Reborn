package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import net.minecraft.client.player.AbstractClientPlayer;

/**
 * Full faithful port of 1.12.2 ModelStarSpawnPlayer (64x32). Geometry, part
 * hierarchy and the setRotationAngles animation are transcribed verbatim by
 * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).
 */
public class StarSpawnTentacleModel extends HierarchicalModel<AbstractClientPlayer> {

    private final ModelPart root;
    private final ModelPart tentacle1;
    private final ModelPart tentacle2;
    private final ModelPart tentacle3;
    private final ModelPart tentacle4;
    private final ModelPart limb1;
    private final ModelPart limb2;
    private final ModelPart limb3;
    private final ModelPart limb4;
    private final ModelPart limb1_2;
    private final ModelPart limb2_2;
    private final ModelPart limb3_2;
    private final ModelPart limb4_2;
    private final ModelPart limb1_3;
    private final ModelPart limb2_3;
    private final ModelPart limb3_3;
    private final ModelPart limb4_3;
    private final ModelPart limb1_4;
    private final ModelPart limb2_4;
    private final ModelPart limb3_4;
    private final ModelPart limb4_4;

    public StarSpawnTentacleModel(ModelPart root) {
        this.root = root;
        this.tentacle1 = root.getChild("tentacle1");
        this.tentacle2 = root.getChild("tentacle2");
        this.tentacle3 = root.getChild("tentacle3");
        this.tentacle4 = root.getChild("tentacle4");
        this.limb1 = this.tentacle1.getChild("limb1");
        this.limb2 = this.tentacle2.getChild("limb2");
        this.limb3 = this.tentacle3.getChild("limb3");
        this.limb4 = this.tentacle4.getChild("limb4");
        this.limb1_2 = this.limb1.getChild("limb1_2");
        this.limb2_2 = this.limb2.getChild("limb2_2");
        this.limb3_2 = this.limb3.getChild("limb3_2");
        this.limb4_2 = this.limb4.getChild("limb4_2");
        this.limb1_3 = this.limb1_2.getChild("limb1_3");
        this.limb2_3 = this.limb2_2.getChild("limb2_3");
        this.limb3_3 = this.limb3_2.getChild("limb3_3");
        this.limb4_3 = this.limb4_2.getChild("limb4_3");
        this.limb1_4 = this.limb1_3.getChild("limb1_4");
        this.limb2_4 = this.limb2_3.getChild("limb2_4");
        this.limb3_4 = this.limb3_3.getChild("limb3_4");
        this.limb4_4 = this.limb4_3.getChild("limb4_4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition tentacle1 = root.addOrReplaceChild("tentacle1",
                CubeListBuilder.create().texOffs(36, 8).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 2),
                PartPose.offset(-3.0F, 0.0F, -6.0F));
        PartDefinition limb1 = tentacle1.addOrReplaceChild("limb1",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb1_2 = limb1.addOrReplaceChild("limb1_2",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb1_3 = limb1_2.addOrReplaceChild("limb1_3",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb1_4 = limb1_3.addOrReplaceChild("limb1_4",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition tentacle2 = root.addOrReplaceChild("tentacle2",
                CubeListBuilder.create().texOffs(36, 8).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 2),
                PartPose.offset(-1.0F, 0.0F, -6.0F));
        PartDefinition limb2 = tentacle2.addOrReplaceChild("limb2",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb2_2 = limb2.addOrReplaceChild("limb2_2",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb2_3 = limb2_2.addOrReplaceChild("limb2_3",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb2_4 = limb2_3.addOrReplaceChild("limb2_4",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition tentacle3 = root.addOrReplaceChild("tentacle3",
                CubeListBuilder.create().texOffs(36, 8).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 2),
                PartPose.offset(1.0F, 0.0F, -6.0F));
        PartDefinition limb3 = tentacle3.addOrReplaceChild("limb3",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb3_2 = limb3.addOrReplaceChild("limb3_2",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb3_3 = limb3_2.addOrReplaceChild("limb3_3",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb3_4 = limb3_3.addOrReplaceChild("limb3_4",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition tentacle4 = root.addOrReplaceChild("tentacle4",
                CubeListBuilder.create().texOffs(36, 8).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 2),
                PartPose.offset(3.0F, 0.0F, -6.0F));
        PartDefinition limb4 = tentacle4.addOrReplaceChild("limb4",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb4_2 = limb4.addOrReplaceChild("limb4_2",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb4_3 = limb4_2.addOrReplaceChild("limb4_3",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition limb4_4 = limb4_3.addOrReplaceChild("limb4_4",
                CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(AbstractClientPlayer entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

		float f16 = 0.0299F;
		limb1.xRot = Mth.sin(entity.tickCount * f16) * 4.5F * (float)Math.PI / 180.0F;
		limb1_2.xRot = Mth.sin(entity.tickCount * f16) * 4.5F * (float)Math.PI / 180.0F;
		limb1_3.xRot = Mth.sin(entity.tickCount * f16) * 4.5F * (float)Math.PI / 180.0F;
		limb1_4.xRot = Mth.sin(entity.tickCount * f16) * 4.5F * (float)Math.PI / 180.0F;

		float f17 = 0.0301F;
		limb2.xRot = Mth.sin(entity.tickCount * f17) * 4.5F * (float)Math.PI / 180.0F;
		limb2_2.xRot = Mth.sin(entity.tickCount * f17) * 4.5F * (float)Math.PI / 180.0F;
		limb2_3.xRot = Mth.sin(entity.tickCount * f17) * 4.5F * (float)Math.PI / 180.0F;
		limb2_4.xRot = Mth.sin(entity.tickCount * f17) * 4.5F * (float)Math.PI / 180.0F;

		float f18 = 0.0301F;
		limb3.xRot = Mth.sin(entity.tickCount * f18) * 4.5F * (float)Math.PI / 180.0F;
		limb3_2.xRot = Mth.sin(entity.tickCount * f18) * 4.5F * (float)Math.PI / 180.0F;
		limb3_3.xRot = Mth.sin(entity.tickCount * f18) * 4.5F * (float)Math.PI / 180.0F;
		limb3_4.xRot = Mth.sin(entity.tickCount * f18) * 4.5F * (float)Math.PI / 180.0F;

		float f19 = 0.0299F;
		limb4.xRot = Mth.sin(entity.tickCount * f19) * 4.5F * (float)Math.PI / 180.0F;
		limb4_2.xRot = Mth.sin(entity.tickCount * f19) * 4.5F * (float)Math.PI / 180.0F;
		limb4_3.xRot = Mth.sin(entity.tickCount * f19) * 4.5F * (float)Math.PI / 180.0F;
		limb4_4.xRot = Mth.sin(entity.tickCount * f19) * 4.5F * (float)Math.PI / 180.0F;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
