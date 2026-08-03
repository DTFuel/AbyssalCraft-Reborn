package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Mob;

/** License-safe placeholder retaining the dragon renderer's bone interface. */
public class DragonModel<T extends Mob> extends HierarchicalModel<T> {

    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart head;

    public DragonModel(ModelPart root) {
        this.root = root;
        ModelPart body = root.getChild("body");
        ModelPart neck = body.getChild("neck");
        ModelPart neck2 = neck.getChild("neck2");
        this.head = neck2.getChild("head");
        this.head.getChild("jaw");
        body.getChild("tail").getChild("tail2").getChild("tail3");
        body.getChild("wing_right").getChild("wing_right_tip");
        body.getChild("wing_left").getChild("wing_left_tip");
        body.getChild("front_right");
        body.getChild("front_left");
        body.getChild("rear_right");
        body.getChild("rear_left");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * DEG_TO_RAD;
        this.head.xRot = headPitch * DEG_TO_RAD;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose origin = PartPose.offset(0.0F, 0.0F, 0.0F);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), origin);
        PartDefinition neck2 = neck.addOrReplaceChild("neck2", CubeListBuilder.create(), origin);
        PartDefinition head = neck2.addOrReplaceChild("head", CubeListBuilder.create(), origin);
        head.addOrReplaceChild("jaw", CubeListBuilder.create(), origin);
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), origin);
        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create(), origin);
        tail2.addOrReplaceChild("tail3", CubeListBuilder.create(), origin);
        PartDefinition rightWing = body.addOrReplaceChild("wing_right", CubeListBuilder.create(), origin);
        rightWing.addOrReplaceChild("wing_right_tip", CubeListBuilder.create(), origin);
        PartDefinition leftWing = body.addOrReplaceChild("wing_left", CubeListBuilder.create(), origin);
        leftWing.addOrReplaceChild("wing_left_tip", CubeListBuilder.create(), origin);
        body.addOrReplaceChild("front_right", CubeListBuilder.create(), origin);
        body.addOrReplaceChild("front_left", CubeListBuilder.create(), origin);
        body.addOrReplaceChild("rear_right", CubeListBuilder.create(), origin);
        body.addOrReplaceChild("rear_left", CubeListBuilder.create(), origin);

        return LayerDefinition.create(mesh, 16, 16);
    }
}