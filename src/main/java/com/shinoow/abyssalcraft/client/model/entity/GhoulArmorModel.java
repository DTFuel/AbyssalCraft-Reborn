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

/** License-safe armor placeholder retaining the Ghoul armor slot bones. */
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
        PartPose origin = PartPose.offset(0.0F, 0.0F, 0.0F);

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, inflate), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition shoulders = root.addOrReplaceChild("shoulders", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, inflate), PartPose.offset(0.0F, 14.0F, 0.0F));
        shoulders.addOrReplaceChild("rarm1", CubeListBuilder.create(), origin);
        shoulders.addOrReplaceChild("larm1", CubeListBuilder.create(), origin);
        PartDefinition leftLeg = root.addOrReplaceChild("lleg", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4, inflate), PartPose.offset(2.0F, 16.0F, 0.0F));
        leftLeg.addOrReplaceChild("lleg2", CubeListBuilder.create(), origin);
        PartDefinition rightLeg = root.addOrReplaceChild("rleg", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4, inflate), PartPose.offset(-2.0F, 16.0F, 0.0F));
        rightLeg.addOrReplaceChild("rleg2", CubeListBuilder.create(), origin);
        root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, inflate), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, inflate), PartPose.offset(0.0F, 14.0F, 0.0F));

        return LayerDefinition.create(mesh, 16, 16);
    }

    public void setSlotVisible(EquipmentSlot slot) {
        this.headArmor.visible = false;
        this.shouldersArmor.visible = false;
        this.pelvisArmor.visible = false;
        this.leftLegArmor.visible = false;
        this.rightLegArmor.visible = false;
        this.chestplate.visible = false;
        switch (slot) {
            case HEAD -> this.headArmor.visible = true;
            case CHEST -> {
                this.shouldersArmor.visible = true;
                this.chestplate.visible = true;
            }
            case LEGS -> {
                this.pelvisArmor.visible = true;
                this.leftLegArmor.visible = true;
                this.rightLegArmor.visible = true;
            }
            case FEET -> {
                this.leftLegArmor.visible = true;
                this.rightLegArmor.visible = true;
            }
            default -> { }
        }
    }
}