package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;

/**
 * Full faithful port of 1.12.2 ModelChagarothSpawn (32x32). Geometry, part
 * hierarchy and the setRotationAngles animation are transcribed verbatim by
 * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).
 */
public class LegacyDreadbeastModel extends HierarchicalModel<LegacyHostileMob> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart smallspike1;
    private final ModelPart smallspike2;
    private final ModelPart smallspike3;
    private final ModelPart smallspike4;
    private final ModelPart bigspike1;
    private final ModelPart bigspike2;
    private final ModelPart bigspike3;
    private final ModelPart bigspike4;
    private final ModelPart leftleg1;
    private final ModelPart leftleg2;
    private final ModelPart leftleg3;
    private final ModelPart backleg1;
    private final ModelPart backleg2;
    private final ModelPart backleg3;
    private final ModelPart frontleg1;
    private final ModelPart frontleg2;
    private final ModelPart frontleg3;
    private final ModelPart rightleg1;
    private final ModelPart rightleg2;
    private final ModelPart rightleg3;

    public LegacyDreadbeastModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.smallspike1 = root.getChild("smallspike1");
        this.smallspike2 = root.getChild("smallspike2");
        this.smallspike3 = root.getChild("smallspike3");
        this.smallspike4 = root.getChild("smallspike4");
        this.bigspike1 = root.getChild("bigspike1");
        this.bigspike2 = root.getChild("bigspike2");
        this.bigspike3 = root.getChild("bigspike3");
        this.bigspike4 = root.getChild("bigspike4");
        this.leftleg1 = root.getChild("leftleg1");
        this.leftleg2 = root.getChild("leftleg2");
        this.leftleg3 = root.getChild("leftleg3");
        this.backleg1 = root.getChild("backleg1");
        this.backleg2 = root.getChild("backleg2");
        this.backleg3 = root.getChild("backleg3");
        this.frontleg1 = root.getChild("frontleg1");
        this.frontleg2 = root.getChild("frontleg2");
        this.frontleg3 = root.getChild("frontleg3");
        this.rightleg1 = root.getChild("rightleg1");
        this.rightleg2 = root.getChild("rightleg2");
        this.rightleg3 = root.getChild("rightleg3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().mirror().texOffs(0, 7).addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        PartDefinition smallspike1 = root.addOrReplaceChild("smallspike1",
                CubeListBuilder.create().mirror().texOffs(18, 1).addBox(0.0F, -4.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(2.0F, 18.0F, -3.0F, 0.3346075F, -0.7435722F, 0.0F));
        PartDefinition smallspike2 = root.addOrReplaceChild("smallspike2",
                CubeListBuilder.create().mirror().texOffs(18, 1).addBox(0.0F, -4.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(-3.0F, 18.0F, -2.0F, 0.3346145F, 0.8922867F, 0.0F));
        PartDefinition smallspike3 = root.addOrReplaceChild("smallspike3",
                CubeListBuilder.create().mirror().texOffs(18, 1).addBox(0.0F, -4.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(-2.0F, 18.0F, 3.0F, 0.3346075F, 2.41661F, 0.0F));
        PartDefinition smallspike4 = root.addOrReplaceChild("smallspike4",
                CubeListBuilder.create().mirror().texOffs(18, 1).addBox(0.0F, -4.0F, 0.0F, 1, 5, 1),
                PartPose.offsetAndRotation(3.0F, 18.0F, 2.0F, 0.3346075F, -2.379431F, 0.0F));
        PartDefinition bigspike1 = root.addOrReplaceChild("bigspike1",
                CubeListBuilder.create().mirror().texOffs(12, 0).addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2),
                PartPose.offsetAndRotation(0.0F, 21.0F, 2.0F, -0.8922867F, 0.0F, 0.0F));
        PartDefinition bigspike2 = root.addOrReplaceChild("bigspike2",
                CubeListBuilder.create().mirror().texOffs(12, 0).addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2),
                PartPose.offsetAndRotation(0.0F, 21.0F, -2.0F, 0.8922821F, 0.0F, 0.0F));
        PartDefinition bigspike3 = root.addOrReplaceChild("bigspike3",
                CubeListBuilder.create().mirror().texOffs(12, 0).addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2),
                PartPose.offsetAndRotation(-2.0F, 21.0F, 0.0F, 0.0F, 0.0F, -0.8922821F));
        PartDefinition bigspike4 = root.addOrReplaceChild("bigspike4",
                CubeListBuilder.create().mirror().texOffs(12, 0).addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2),
                PartPose.offsetAndRotation(2.0F, 21.0F, 0.0F, 0.0F, 0.0F, 0.8922821F));
        PartDefinition leftleg1 = root.addOrReplaceChild("leftleg1",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 5, 1, 1),
                PartPose.offset(3.0F, 23.0F, -2.5F));
        PartDefinition leftleg2 = root.addOrReplaceChild("leftleg2",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 5, 1, 1),
                PartPose.offset(3.0F, 23.0F, -0.5F));
        PartDefinition leftleg3 = root.addOrReplaceChild("leftleg3",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 5, 1, 1),
                PartPose.offset(3.0F, 23.0F, 1.5F));
        PartDefinition backleg1 = root.addOrReplaceChild("backleg1",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1, 1, 5),
                PartPose.offset(1.5F, 23.0F, 3.0F));
        PartDefinition backleg2 = root.addOrReplaceChild("backleg2",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1, 1, 5),
                PartPose.offset(-0.5F, 23.0F, 3.0F));
        PartDefinition backleg3 = root.addOrReplaceChild("backleg3",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1, 1, 5),
                PartPose.offset(-2.5F, 23.0F, 3.0F));
        PartDefinition frontleg1 = root.addOrReplaceChild("frontleg1",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, 0.0F, -5.0F, 1, 1, 5),
                PartPose.offset(1.5F, 23.0F, -3.0F));
        PartDefinition frontleg2 = root.addOrReplaceChild("frontleg2",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, 0.0F, -5.0F, 1, 1, 5),
                PartPose.offset(-0.5F, 23.0F, -3.0F));
        PartDefinition frontleg3 = root.addOrReplaceChild("frontleg3",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0.0F, 0.0F, -5.0F, 1, 1, 5),
                PartPose.offset(-2.5F, 23.0F, -3.0F));
        PartDefinition rightleg1 = root.addOrReplaceChild("rightleg1",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-5.0F, 0.0F, 0.0F, 5, 1, 1),
                PartPose.offset(-3.0F, 23.0F, 1.5F));
        PartDefinition rightleg2 = root.addOrReplaceChild("rightleg2",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-5.0F, 0.0F, 0.0F, 5, 1, 1),
                PartPose.offset(-3.0F, 23.0F, -0.5F));
        PartDefinition rightleg3 = root.addOrReplaceChild("rightleg3",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-5.0F, 0.0F, 0.0F, 5, 1, 1),
                PartPose.offset(-3.0F, 23.0F, -2.5F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LegacyHostileMob entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
                                float front = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
                                frontleg1.yRot = front;
                                frontleg2.yRot = front;
                                frontleg3.yRot = front;

                                float left = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
                                leftleg1.yRot = left;
                                leftleg2.yRot = left;
                                leftleg3.yRot = left;

                                float back = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
                                backleg1.yRot = back;
                                backleg2.yRot = back;
                                backleg3.yRot = back;

                                float right = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
                                rightleg1.yRot = right;
                                rightleg2.yRot = right;
                                rightleg3.yRot = right;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
