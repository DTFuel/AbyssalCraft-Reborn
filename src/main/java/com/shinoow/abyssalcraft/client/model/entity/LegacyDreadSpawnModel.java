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
 * Full faithful port of 1.12.2 ModelDreadSpawn (32x32). Geometry, part
 * hierarchy and the setRotationAngles animation are transcribed verbatim by
 * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).
 */
public class LegacyDreadSpawnModel extends HierarchicalModel<LegacyHostileMob> {

        private static final float DEG = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart thing;
    private final ModelPart arm;
    private final ModelPart jaw;
    private final ModelPart t1;
    private final ModelPart t2;
    private final ModelPart t3;
    private final ModelPart arm1;
    private final ModelPart t11;
    private final ModelPart t21;
    private final ModelPart t31;
    private final ModelPart arm2;
    private final ModelPart t12;
    private final ModelPart t22;
    private final ModelPart t32;
    private final ModelPart arm3;
    private final ModelPart t13;
    private final ModelPart t23;
    private final ModelPart t33;

    public LegacyDreadSpawnModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.thing = root.getChild("thing");
        this.arm = root.getChild("arm");
        this.jaw = this.head.getChild("jaw");
        this.t1 = this.thing.getChild("t1");
        this.t2 = this.thing.getChild("t2");
        this.t3 = this.thing.getChild("t3");
        this.arm1 = this.arm.getChild("arm1");
        this.t11 = this.t1.getChild("t11");
        this.t21 = this.t2.getChild("t21");
        this.t31 = this.t3.getChild("t31");
        this.arm2 = this.arm1.getChild("arm2");
        this.t12 = this.t11.getChild("t12");
        this.t22 = this.t21.getChild("t22");
        this.t32 = this.t31.getChild("t32");
        this.arm3 = this.arm2.getChild("arm3");
        this.t13 = this.t12.getChild("t13");
        this.t23 = this.t22.getChild("t23");
        this.t33 = this.t32.getChild("t33");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().mirror().texOffs(0, 17).addBox(-3.0F, -3.0F, -3.0F, 6, 5, 6),
                PartPose.offset(0.0F, 22.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().mirror().texOffs(0, 4).addBox(-1.5F, -4.0F, -1.5F, 3, 3, 3),
                PartPose.offsetAndRotation(0.0F, 19.0F, 0.0F, -0.4833219F, 0.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw",
                CubeListBuilder.create().mirror().texOffs(12, 4).addBox(-1.5F, -1.0F, -1.5F, 3, 1, 3),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4833219F, 0.0F, 0.0F));
        PartDefinition thing = root.addOrReplaceChild("thing",
                CubeListBuilder.create().mirror().texOffs(0, 10).addBox(0.0F, -1.0F, -3.0F, 6, 1, 6),
                PartPose.offset(3.0F, 21.0F, 2.0F));
        PartDefinition t1 = thing.addOrReplaceChild("t1",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offsetAndRotation(4.0F, -1.0F, 2.0F, 0.0F, 0.3F, 0.0F));
        PartDefinition t11 = t1.addOrReplaceChild("t11",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition t12 = t11.addOrReplaceChild("t12",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition t13 = t12.addOrReplaceChild("t13",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition t2 = thing.addOrReplaceChild("t2",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offsetAndRotation(4.0F, -1.0F, 4.0F, 0.0F, 0.7F, 0.0F));
        PartDefinition t21 = t2.addOrReplaceChild("t21",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition t22 = t21.addOrReplaceChild("t22",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition t23 = t22.addOrReplaceChild("t23",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition t3 = thing.addOrReplaceChild("t3",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, 0.0F, -0.3F, 0.0F));
        PartDefinition t31 = t3.addOrReplaceChild("t31",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition t32 = t31.addOrReplaceChild("t32",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition t33 = t32.addOrReplaceChild("t33",
                CubeListBuilder.create().mirror().texOffs(10, 5).addBox(0.0F, -1.0F, -3.0F, 1, 1, 1),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition arm = root.addOrReplaceChild("arm",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-1.0F, -1.0F, -3.0F, 2, 2, 2),
                PartPose.offset(-2.0F, 22.0F, 0.0F));
        PartDefinition arm1 = arm.addOrReplaceChild("arm1",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-1.0F, -1.0F, -3.0F, 2, 2, 2),
                PartPose.offset(0.0F, 0.0F, -2.0F));
        PartDefinition arm2 = arm1.addOrReplaceChild("arm2",
                CubeListBuilder.create().mirror().texOffs(8, 0).addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2),
                PartPose.offset(0.0F, 0.0F, -3.0F));
        PartDefinition arm3 = arm2.addOrReplaceChild("arm3",
                CubeListBuilder.create().mirror().texOffs(16, 0).addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2),
                PartPose.offset(0.0F, 0.0F, -2.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LegacyHostileMob entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
                                arm.yRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
                                float armPhase = 0.02F * (entity.getId() % 10);
                                float armWave = Mth.sin(ageInTicks * armPhase) * 4.5F * DEG;
                                arm1.yRot = armWave;
                                arm2.yRot = armWave;
                                arm3.yRot = armWave;
                                thing.zRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
                                head.yRot = netHeadYaw * DEG;

                                animateTentacle(t1, t11, t12, t13, Mth.cos(ageInTicks * 0.03F) * 4.25F * DEG);
                                animateTentacle(t2, t21, t22, t23, Mth.cos(ageInTicks * 0.04F) * 4.25F * DEG);
                                animateTentacle(t3, t31, t32, t33, Mth.cos(ageInTicks * -0.04F) * 4.25F * DEG);
    }

                private static void animateTentacle(ModelPart first, ModelPart second, ModelPart third, ModelPart fourth,
                                                                                                                                                                float rotation) {
                                first.zRot = rotation;
                                second.zRot = rotation;
                                third.zRot = rotation;
                                fourth.zRot = rotation;
                }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
