package com.shinoow.abyssalcraft.client.render.armor;

import com.shinoow.abyssalcraft.client.model.entity.DreadiumSamuraiArmorModel;
import com.shinoow.abyssalcraft.platform.ArmorCompat;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class ACArmorVisuals {

    private static HumanoidModel<LivingEntity> inner;
    private static HumanoidModel<LivingEntity> outer;

    private ACArmorVisuals() {}

    public static HumanoidModel<?> samuraiModel(EquipmentSlot slot, HumanoidModel<?> defaultModel) {
        HumanoidModel<LivingEntity> model = slot == EquipmentSlot.LEGS ? inner() : outer();
        @SuppressWarnings("unchecked")
        HumanoidModel<LivingEntity> source = (HumanoidModel<LivingEntity>) defaultModel;
        source.copyPropertiesTo(model);
        setVisible(model, slot);
        return model;
    }

    private static HumanoidModel<LivingEntity> inner() {
        if (inner == null) {
            inner = new DreadiumSamuraiArmorModel<>(Minecraft.getInstance().getEntityModels()
                .bakeLayer(ModModelLayers.SAMURAI_INNER));
        }
        return inner;
    }

    private static HumanoidModel<LivingEntity> outer() {
        if (outer == null) {
            outer = new DreadiumSamuraiArmorModel<>(Minecraft.getInstance().getEntityModels()
                .bakeLayer(ModModelLayers.SAMURAI_OUTER));
        }
        return outer;
    }

    public static void setVisible(HumanoidModel<?> model, EquipmentSlot slot) {
        model.setAllVisible(false);
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> { }
        }
    }

    public static boolean hasVisual(ItemStack stack, ArmorCompat.Visual visual) {
        return stack.getItem() instanceof ArmorCompat.VisualArmorItem item && item.visual() == visual;
    }
}