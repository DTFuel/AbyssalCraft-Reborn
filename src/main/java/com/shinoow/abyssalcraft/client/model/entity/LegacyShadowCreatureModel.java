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
 * Full faithful port of 1.12.2 ModelShadowCreature (32x32). Geometry, part
 * hierarchy and the setRotationAngles animation are transcribed verbatim by
 * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).
 */
public class LegacyShadowCreatureModel extends BrightnessAlphaModel<LegacyHostileMob> implements HeadedModel {

        private static final float DEG = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart Body;
    private final ModelPart Head1;
    private final ModelPart Tail1;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Tail4;
    private final ModelPart Tail5;
    private final ModelPart Tail6;
    private final ModelPart Tail7;
    private final ModelPart LeftArm1;
    private final ModelPart RightArm1;
    private final ModelPart LeftArm2;
    private final ModelPart RightArm2;

    public LegacyShadowCreatureModel(ModelPart root) {
        this.root = root;
        this.Body = root.getChild("Body");
        this.Head1 = root.getChild("Head1");
        this.Tail1 = root.getChild("Tail1");
        this.Tail2 = root.getChild("Tail2");
        this.Tail3 = root.getChild("Tail3");
        this.Tail4 = root.getChild("Tail4");
        this.Tail5 = root.getChild("Tail5");
        this.Tail6 = root.getChild("Tail6");
        this.Tail7 = root.getChild("Tail7");
        this.LeftArm1 = root.getChild("LeftArm1");
        this.RightArm1 = root.getChild("RightArm1");
        this.LeftArm2 = root.getChild("LeftArm2");
        this.RightArm2 = root.getChild("RightArm2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition Body = root.addOrReplaceChild("Body",
                CubeListBuilder.create().mirror().texOffs(12, 22).addBox(0.0F, 0.0F, 0.0F, 3, 3, 7),
                PartPose.offset(-3.0F, 12.0F, -1.0F));
        PartDefinition Head1 = root.addOrReplaceChild("Head1",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-3.0F, -5.0F, -3.0F, 5, 5, 5),
                PartPose.offset(-1.0F, 12.0F, 0.0F));
        PartDefinition Tail1 = root.addOrReplaceChild("Tail1",
                CubeListBuilder.create().mirror().texOffs(18, 15).addBox(0.0F, 0.0F, 0.0F, 2, 2, 5),
                PartPose.offsetAndRotation(-2.466667F, 12.5F, 6.0F, -0.3717861F, 0.0F, 0.0F));
        PartDefinition Tail2 = root.addOrReplaceChild("Tail2",
                CubeListBuilder.create().mirror().texOffs(26, 12).addBox(0.0F, 0.0F, 0.0F, 1, 1, 2),
                PartPose.offsetAndRotation(-2.0F, 14.0F, 9.0F, 1.115358F, 0.0F, 0.0F));
        PartDefinition Tail3 = root.addOrReplaceChild("Tail3",
                CubeListBuilder.create().mirror().texOffs(23, 7).addBox(0.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(-1.0F, 14.5F, 9.0F, -0.4089647F, 0.0F, 0.0F));
        PartDefinition Tail4 = root.addOrReplaceChild("Tail4",
                CubeListBuilder.create().mirror().texOffs(23, 7).addBox(0.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(-1.0F, 13.5F, 7.0F, -0.4089647F, 0.0F, 0.0F));
        PartDefinition Tail5 = root.addOrReplaceChild("Tail5",
                CubeListBuilder.create().mirror().texOffs(23, 7).addBox(0.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(-5.0F, 14.5F, 9.0F, -0.4089647F, 0.0F, 0.0F));
        PartDefinition Tail6 = root.addOrReplaceChild("Tail6",
                CubeListBuilder.create().mirror().texOffs(23, 7).addBox(0.0F, 0.0F, 0.0F, 3, 1, 1),
                PartPose.offsetAndRotation(-5.0F, 13.5F, 7.0F, -0.4089647F, 0.0F, 0.0F));
        PartDefinition Tail7 = root.addOrReplaceChild("Tail7",
                CubeListBuilder.create().mirror().texOffs(26, 12).addBox(0.0F, 0.0F, 0.0F, 1, 1, 2),
                PartPose.offsetAndRotation(-2.0F, 13.0F, 7.0F, 1.115358F, 0.0F, 0.0F));
        PartDefinition LeftArm1 = root.addOrReplaceChild("LeftArm1",
                CubeListBuilder.create().mirror().texOffs(11, 19).addBox(0.0F, 0.0F, 0.0F, 2, 1, 1),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        PartDefinition RightArm1 = root.addOrReplaceChild("RightArm1",
                CubeListBuilder.create().mirror().texOffs(11, 19).addBox(0.0F, 0.0F, 0.0F, 2, 1, 1),
                PartPose.offset(-5.0F, 13.0F, 0.0F));
        PartDefinition LeftArm2 = root.addOrReplaceChild("LeftArm2",
                CubeListBuilder.create().mirror().texOffs(0, 22).addBox(0.0F, 0.0F, -5.0F, 1, 1, 5),
                PartPose.offset(1.0F, 13.0F, 0.0F));
        PartDefinition RightArm2 = root.addOrReplaceChild("RightArm2",
                CubeListBuilder.create().mirror().texOffs(0, 22).addBox(0.0F, 0.0F, -5.0F, 1, 1, 5),
                PartPose.offset(-5.0F, 13.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LegacyHostileMob entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
                                Head1.yRot = netHeadYaw * DEG;
                                Head1.xRot = headPitch * DEG;
                                RightArm2.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
                                LeftArm2.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
                                RightArm2.zRot = 0.0F;
                                LeftArm2.zRot = 0.0F;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

        @Override
        public ModelPart getHead() {
                return this.Head1;
        }
}
