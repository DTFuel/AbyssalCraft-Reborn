package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Full faithful port of 1.12.2 ModelDreadTentacles (16x16). Geometry, part
 * hierarchy and the setRotationAngles animation are transcribed verbatim by
 * scripts/convert_modelbase_to_entitymodel.js --full (do not hand-edit; regenerate instead).
 */
public class DreadTentacleModel<T extends Entity> extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart base;
    private final ModelPart tentacle_1;
    private final ModelPart tentacle_1_1;
    private final ModelPart tentacle_2;
    private final ModelPart tentacle_2_1;
    private final ModelPart tentacle_3;
    private final ModelPart tentacle_3_1;

    public DreadTentacleModel(ModelPart root) {
        this.root = root;
        this.base = root.getChild("base");
        this.tentacle_1 = this.base.getChild("tentacle_1");
        this.tentacle_1_1 = this.tentacle_1.getChild("tentacle_1_1");
        this.tentacle_2 = this.tentacle_1_1.getChild("tentacle_2");
        this.tentacle_2_1 = this.tentacle_2.getChild("tentacle_2_1");
        this.tentacle_3 = this.tentacle_2_1.getChild("tentacle_3");
        this.tentacle_3_1 = this.tentacle_3.getChild("tentacle_3_1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition base = root.addOrReplaceChild("base",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.7853981633974483F, 0.7853981633974483F, 0.0F));
        PartDefinition tentacle_1 = base.addOrReplaceChild("tentacle_1",
                CubeListBuilder.create().texOffs(0, 8).addBox(0.0F, 0.0F, 0.0F, 3, 3, 5),
                PartPose.offset(-1.0F, -1.0F, 1.0F));
        PartDefinition tentacle_1_1 = tentacle_1.addOrReplaceChild("tentacle_1_1",
                CubeListBuilder.create().texOffs(0, 9).addBox(0.0F, 0.0F, 0.0F, 3, 3, 4),
                PartPose.offset(0.0F, 0.0F, 5.0F));
        PartDefinition tentacle_2 = tentacle_1_1.addOrReplaceChild("tentacle_2",
                CubeListBuilder.create().texOffs(4, 4).addBox(0.5F, 0.5F, 0.0F, 2, 2, 4),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        PartDefinition tentacle_2_1 = tentacle_2.addOrReplaceChild("tentacle_2_1",
                CubeListBuilder.create().texOffs(4, 5).addBox(0.5F, 0.5F, 0.0F, 2, 2, 3),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition tentacle_3 = tentacle_2_1.addOrReplaceChild("tentacle_3",
                CubeListBuilder.create().texOffs(6, 2).addBox(1.0F, 1.0F, 0.0F, 1, 1, 4),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        PartDefinition tentacle_3_1 = tentacle_3.addOrReplaceChild("tentacle_3_1",
                CubeListBuilder.create().texOffs(6, 2).addBox(1.0F, 1.0F, 0.0F, 1, 1, 3),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
        public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
                animate(ageInTicks, limbSwing, limbSwingAmount, true, 0.0F, 10.0F, 0.0F);
        }

        public void setupCarrierAnim(T entity, float limbSwing, float limbSwingAmount,
                                                                 float ageInTicks, float anchorX, float anchorY, float anchorZ) {
                animate(ageInTicks, limbSwing, limbSwingAmount, false, anchorX, anchorY, anchorZ);
        }

        private void animate(float ageInTicks, float limbSwing, float limbSwingAmount, boolean pointing,
                                                 float anchorX, float anchorY, float anchorZ) {
                base.setPos(anchorX, anchorY, anchorZ);
                base.xRot = pointing ? (float) Math.PI / 2.0F : 0.7853982F;
                base.yRot = pointing ? 0.0F : 0.7853982F;
                base.zRot = 0.0F;
                float movement = Mth.sin((limbSwing * 0.4F + 2.0F) * 1.5F) * 0.09F * limbSwingAmount;
                float flap = Mth.sin(ageInTicks * 0.2F) * 0.3F;
                float flap2 = Mth.cos(ageInTicks * 0.2F) * 0.4F;
                tentacle_1.yRot = flap * 0.1F + movement * 0.4F;
                tentacle_2.yRot = tentacle_1_1.yRot * 1.5F;
                tentacle_3.yRot = tentacle_2_1.yRot * 1.75F;
                tentacle_1.xRot = pointing ? -0.6396263F
                        : -0.1396263F - flap2 * 0.1F - movement * 0.4F;
                tentacle_2.xRot = -0.418879F - flap2 * 0.75F;
                tentacle_3.xRot = -0.1396263F - flap2;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
