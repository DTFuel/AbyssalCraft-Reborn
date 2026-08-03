package com.shinoow.abyssalcraft.client.render.armor;

import com.shinoow.abyssalcraft.platform.ArmorCompat;

import net.minecraft.world.entity.EquipmentSlot;

import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class DreadiumSamuraiArmorRenderer extends GeoArmorRenderer<ArmorCompat.SamuraiArmorItem> {

    public DreadiumSamuraiArmorRenderer() {
        super(new DreadiumSamuraiArmorModel());
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot slot) {
        if (slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) {
            super.applyBoneVisibilityBySlot(slot);
            return;
        }

        setAllBonesVisible(false);
        if (slot == EquipmentSlot.LEGS) {
            setBoneVisible(body, true);
        }
        setBoneVisible(rightLeg, true);
        setBoneVisible(leftLeg, true);
    }
}