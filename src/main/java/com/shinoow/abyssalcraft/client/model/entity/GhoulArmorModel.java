package com.shinoow.abyssalcraft.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;

public final class GhoulArmorModel<T extends Mob> extends GhoulModel<T> {

    private final ModelPart headArmor;
    private final ModelPart shouldersArmor;
    private final ModelPart pelvisArmor;
    private final ModelPart leftLegArmor;
    private final ModelPart rightLegArmor;
    private final ModelPart chestplate;

    public GhoulArmorModel(ModelPart root) {
        super(root);
        this.headArmor = root.getChild("head");
        this.shouldersArmor = root.getChild("shoulders");
        this.pelvisArmor = root.getChild("pelvis");
        this.leftLegArmor = root.getChild("lleg");
        this.rightLegArmor = root.getChild("rleg");
        this.chestplate = root.getChild("chestplate");
    }

    public static LayerDefinition createBodyLayer(float deformation) {
        CubeDeformation inflate = new CubeDeformation(deformation);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-4.5F, -9.5F, -4.5F, 9, 9, 9, inflate),
            PartPose.offset(0.0F, 6.5F, -20.2F));

        PartDefinition leftLeg = root.addOrReplaceChild("lleg", CubeListBuilder.create().mirror().texOffs(16, 40)
            .addBox(-2.1F, 0.0F, -2.1F, 4, 12, 4, inflate),
            PartPose.offsetAndRotation(3.5F, 6.4F, 4.0F, -0.8726646F, -0.7853982F, 0.0F));
        leftLeg.addOrReplaceChild("lleg2", CubeListBuilder.create().mirror().texOffs(32, 40)
            .addBox(-2.1F, 0.1F, -3.2F, 4, 12, 4, inflate),
            PartPose.offsetAndRotation(0.0F, 9.4F, -0.2F, 0.8726646F, 0.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("rleg", CubeListBuilder.create().texOffs(16, 40)
            .addBox(-2.1F, 0.0F, -2.2F, 4, 12, 4, inflate),
            PartPose.offsetAndRotation(-3.3F, 6.4F, 4.1F, -0.8726646F, 0.7853982F, 0.0F));
        rightLeg.addOrReplaceChild("rleg2", CubeListBuilder.create().texOffs(32, 40)
            .addBox(-2.1F, 0.1F, -3.3F, 4, 12, 4, inflate),
            PartPose.offsetAndRotation(0.0F, 9.4F, -0.2F, 0.8726646F, 0.0F, 0.0F));

        PartDefinition shoulders = root.addOrReplaceChild("shoulders", CubeListBuilder.create().texOffs(54, 12)
            .addBox(-8.0F, -2.0F, -2.0F, 16, 4, 4, inflate),
            PartPose.offsetAndRotation(0.0F, 5.5F, -14.53F, 1.6997762F, 0.0F, 0.0F));
        PartDefinition rightArm = shoulders.addOrReplaceChild("rarm1", CubeListBuilder.create().texOffs(56, 22)
            .addBox(-4.0F, -2.0F, -2.0F, 4, 12, 4, inflate),
            PartPose.offsetAndRotation(-8.0F, 0.8F, -0.8F, -1.4835299F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("rarm2", CubeListBuilder.create().texOffs(72, 26)
            .addBox(-2.0F, 1.0F, 3.0F, 4, 8, 4, inflate),
            PartPose.offsetAndRotation(-2.0F, 4.0F, -2.0F, -0.8290314F, 0.0F, 0.0F));
        PartDefinition leftArm = shoulders.addOrReplaceChild("larm1", CubeListBuilder.create().mirror().texOffs(56, 22)
            .addBox(0.0F, -2.0F, -2.0F, 4, 12, 4, inflate),
            PartPose.offsetAndRotation(8.0F, 0.8F, -0.8F, -1.4835299F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("larm2", CubeListBuilder.create().mirror().texOffs(72, 26)
            .addBox(-2.0F, 1.0F, 3.0F, 4, 8, 4, inflate),
            PartPose.offsetAndRotation(2.0F, 4.0F, -2.0F, -0.8290314F, 0.0F, 0.0F));

        root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(72, 0)
            .addBox(-5.0F, -1.0F, -3.0F, 10, 6, 5, inflate),
            PartPose.offsetAndRotation(0.0F, 4.69F, 1.0F, 1.5707963F, 0.0F, 0.0F));
        root.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(0, 18)
            .addBox(-5.0F, -13.9F, -3.0F, 10, 13, 5, inflate),
            PartPose.offsetAndRotation(0.0F, 4.59F, 1.0F, 0.7853982F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        chestplate.xRot = pelvisArmor.xRot;
        chestplate.z = pelvisArmor.z;
    }

    public void setSlotVisible(EquipmentSlot slot) {
        headArmor.visible = false;
        shouldersArmor.visible = false;
        pelvisArmor.visible = false;
        leftLegArmor.visible = false;
        rightLegArmor.visible = false;
        chestplate.visible = false;
        switch (slot) {
            case HEAD -> headArmor.visible = true;
            case CHEST -> {
                shouldersArmor.visible = true;
                chestplate.visible = true;
            }
            case LEGS -> {
                pelvisArmor.visible = true;
                leftLegArmor.visible = true;
                rightLegArmor.visible = true;
                chestplate.visible = true;
            }
            case FEET -> {
                leftLegArmor.visible = true;
                rightLegArmor.visible = true;
            }
            default -> { }
        }
    }
}