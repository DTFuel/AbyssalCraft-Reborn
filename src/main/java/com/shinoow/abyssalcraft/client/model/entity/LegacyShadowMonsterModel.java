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
 * Full faithful port of 1.12.2 ModelShadowMonster (64x32). Geometry, part
 * hierarchy and the setRotationAngles animation are transcribed verbatim by
 * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).
 */
public class LegacyShadowMonsterModel extends BrightnessAlphaModel<LegacyHostileMob> implements HeadedModel {

        private static final float DEG = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart Head;
    private final ModelPart Body1;
    private final ModelPart Body2;
    private final ModelPart Body3;
    private final ModelPart Lshoulder1;
    private final ModelPart Rshoulder1;
    private final ModelPart Lshoulder2;
    private final ModelPart Rshoulder2;
    private final ModelPart Larm1;
    private final ModelPart Larm2;
    private final ModelPart Rarm1;
    private final ModelPart Rarm2;
    private final ModelPart Back1;
    private final ModelPart Back2;
    private final ModelPart Back3;
    private final ModelPart Back4;
    private final ModelPart Back5;
    private final ModelPart Back6;
    private final ModelPart Back7;
    private final ModelPart Back8;
    private final ModelPart Back9;

    public LegacyShadowMonsterModel(ModelPart root) {
        this.root = root;
        this.Head = root.getChild("Head");
        this.Body1 = root.getChild("Body1");
        this.Body2 = root.getChild("Body2");
        this.Body3 = root.getChild("Body3");
        this.Lshoulder1 = root.getChild("Lshoulder1");
        this.Rshoulder1 = root.getChild("Rshoulder1");
        this.Lshoulder2 = root.getChild("Lshoulder2");
        this.Rshoulder2 = root.getChild("Rshoulder2");
        this.Larm1 = root.getChild("Larm1");
        this.Larm2 = root.getChild("Larm2");
        this.Rarm1 = root.getChild("Rarm1");
        this.Rarm2 = root.getChild("Rarm2");
        this.Back1 = root.getChild("Back1");
        this.Back2 = root.getChild("Back2");
        this.Back3 = root.getChild("Back3");
        this.Back4 = root.getChild("Back4");
        this.Back5 = root.getChild("Back5");
        this.Back6 = root.getChild("Back6");
        this.Back7 = root.getChild("Back7");
        this.Back8 = root.getChild("Back8");
        this.Back9 = root.getChild("Back9");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition Head = root.addOrReplaceChild("Head",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-3.5F, -7.0F, -3.5F, 7, 7, 7),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition Body1 = root.addOrReplaceChild("Body1",
                CubeListBuilder.create().mirror().texOffs(20, 14).addBox(-1.5F, -1.0F, -1.5F, 3, 10, 3),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4461433F, 0.0F, 0.0F));
        PartDefinition Body2 = root.addOrReplaceChild("Body2",
                CubeListBuilder.create().mirror().texOffs(28, 0).addBox(0.0F, 0.0F, 0.0F, 3, 6, 3),
                PartPose.offset(-1.5F, 7.0F, 2.0F));
        PartDefinition Body3 = root.addOrReplaceChild("Body3",
                CubeListBuilder.create().mirror().texOffs(28, 0).addBox(0.0F, 0.0F, 0.0F, 3, 6, 3),
                PartPose.offsetAndRotation(-1.5F, 11.0F, 2.5F, -0.5948578F, 0.0F, 0.0F));
        PartDefinition Lshoulder1 = root.addOrReplaceChild("Lshoulder1",
                CubeListBuilder.create().mirror().texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 3, 2, 2),
                PartPose.offset(1.5F, 0.0F, 0.0F));
        PartDefinition Rshoulder1 = root.addOrReplaceChild("Rshoulder1",
                CubeListBuilder.create().mirror().texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 3, 2, 2),
                PartPose.offset(-4.5F, 0.0F, 0.0F));
        PartDefinition Lshoulder2 = root.addOrReplaceChild("Lshoulder2",
                CubeListBuilder.create().mirror().texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 3, 2, 2),
                PartPose.offset(1.5F, 5.0F, 2.0F));
        PartDefinition Rshoulder2 = root.addOrReplaceChild("Rshoulder2",
                CubeListBuilder.create().mirror().texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 3, 2, 2),
                PartPose.offset(-4.5F, 5.0F, 2.0F));
        PartDefinition Larm1 = root.addOrReplaceChild("Larm1",
                CubeListBuilder.create().mirror().texOffs(0, 14).addBox(0.0F, -1.0F, -7.0F, 2, 2, 8),
                PartPose.offset(4.5F, 1.0F, 1.0F));
        PartDefinition Larm2 = root.addOrReplaceChild("Larm2",
                CubeListBuilder.create().mirror().texOffs(0, 14).addBox(0.0F, -1.0F, -7.0F, 2, 2, 8),
                PartPose.offset(4.5F, 6.0F, 3.0F));
        PartDefinition Rarm1 = root.addOrReplaceChild("Rarm1",
                CubeListBuilder.create().mirror().texOffs(0, 14).addBox(-2.0F, -1.0F, -7.0F, 2, 2, 8),
                PartPose.offset(-4.5F, 1.0F, 1.0F));
        PartDefinition Rarm2 = root.addOrReplaceChild("Rarm2",
                CubeListBuilder.create().mirror().texOffs(0, 14).addBox(-2.0F, -1.0F, -7.0F, 2, 2, 8),
                PartPose.offset(-4.5F, 6.0F, 3.0F));
        PartDefinition Back1 = root.addOrReplaceChild("Back1",
                CubeListBuilder.create().mirror().texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(-0.5F, 2.0F, 4.0F, -1.041002F, 0.0F, 0.0F));
        PartDefinition Back2 = root.addOrReplaceChild("Back2",
                CubeListBuilder.create().mirror().texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(-0.5F, 4.0F, 5.0F, -1.041002F, 0.0F, 0.0F));
        PartDefinition Back3 = root.addOrReplaceChild("Back3",
                CubeListBuilder.create().mirror().texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(-0.5F, 0.0F, 3.0F, -1.041002F, 0.0F, 0.0F));
        PartDefinition Back4 = root.addOrReplaceChild("Back4",
                CubeListBuilder.create().mirror().texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 1, 1, 2),
                PartPose.offset(-0.5F, 8.0F, 4.5F));
        PartDefinition Back5 = root.addOrReplaceChild("Back5",
                CubeListBuilder.create().mirror().texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 1, 1, 2),
                PartPose.offset(-0.5F, 10.0F, 4.5F));
        PartDefinition Back6 = root.addOrReplaceChild("Back6",
                CubeListBuilder.create().mirror().texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 1, 1, 2),
                PartPose.offset(-0.5F, 12.0F, 4.5F));
        PartDefinition Back7 = root.addOrReplaceChild("Back7",
                CubeListBuilder.create().mirror().texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(-0.5F, 14.0F, 3.5F, 1.00382F, 0.0F, 0.0F));
        PartDefinition Back8 = root.addOrReplaceChild("Back8",
                CubeListBuilder.create().mirror().texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(-0.5F, 15.5F, 2.5F, 1.00382F, 0.0F, 0.0F));
        PartDefinition Back9 = root.addOrReplaceChild("Back9",
                CubeListBuilder.create().mirror().texOffs(50, 0).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(-0.5F, 17.0F, 1.5F, 1.00382F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LegacyHostileMob entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
                                Head.yRot = netHeadYaw * DEG;
                                Head.xRot = headPitch * DEG;
                                Rarm1.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
                                Larm1.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
                                Rarm2.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount * 0.1F;
                                Larm2.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.1F;
                                Rarm1.zRot = 0.0F;
                                Larm1.zRot = 0.0F;
                                Rarm2.zRot = 0.0F;
                                Larm2.zRot = 0.0F;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

        @Override
        public ModelPart getHead() {
                return this.Head;
        }
}
