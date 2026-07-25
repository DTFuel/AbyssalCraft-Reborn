package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
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
 * Full faithful port of 1.12.2 ModelDreadling (64x32). Geometry, part
 * hierarchy and the setRotationAngles animation are transcribed verbatim by
 * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).
 */
public class LegacyDreadlingModel extends HierarchicalModel<LegacyHostileMob> implements HeadedModel {

        private static final float DEG = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightarm;
    private final ModelPart leftarm;
    private final ModelPart rightleg;
    private final ModelPart leftleg;

    public LegacyDreadlingModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightarm = root.getChild("rightarm");
        this.leftarm = root.getChild("leftarm");
        this.rightleg = root.getChild("rightleg");
        this.leftleg = root.getChild("leftleg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().mirror().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition rightarm = root.addOrReplaceChild("rightarm",
                CubeListBuilder.create().mirror().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 7, 4),
                PartPose.offset(-5.0F, 12.0F, 0.0F));
        PartDefinition leftarm = root.addOrReplaceChild("leftarm",
                CubeListBuilder.create().mirror().texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4, 11, 4),
                PartPose.offsetAndRotation(5.0F, 12.0F, 0.0F, -0.3717861F, 0.0F, 0.0F));
        PartDefinition rightleg = root.addOrReplaceChild("rightleg",
                CubeListBuilder.create().mirror().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offsetAndRotation(-2.0F, 21.0F, 0.0F, 2.119181F, 0.0F, 0.0F));
        PartDefinition leftleg = root.addOrReplaceChild("leftleg",
                CubeListBuilder.create().mirror().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4),
                PartPose.offsetAndRotation(2.0F, 21.0F, 0.0F, 1.524323F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LegacyHostileMob entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
                                head.yRot = netHeadYaw * DEG;
                                head.xRot = headPitch * DEG;
                                rightarm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
                                leftarm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
                                rightarm.zRot = 0.0F;
                                leftarm.zRot = 0.0F;
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
