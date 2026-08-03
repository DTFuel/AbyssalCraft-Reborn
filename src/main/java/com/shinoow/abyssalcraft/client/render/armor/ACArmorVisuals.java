package com.shinoow.abyssalcraft.client.render.armor;

import com.shinoow.abyssalcraft.platform.ArmorCompat;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class ACArmorVisuals {

    private static DreadiumSamuraiArmorRenderer renderer;

    private ACArmorVisuals() {}

    public static HumanoidModel<?> samuraiRenderer() {
        return renderer();
    }

    public static HumanoidModel<?> samuraiModel(LivingEntity entity, ItemStack stack,
                                                EquipmentSlot slot, HumanoidModel<?> defaultModel) {
        DreadiumSamuraiArmorRenderer renderer = renderer();
        renderer.prepForRender(entity, stack, slot, defaultModel);
        return renderer;
    }

    private static DreadiumSamuraiArmorRenderer renderer() {
        if (renderer == null) {
            renderer = new DreadiumSamuraiArmorRenderer();
        }
        return renderer;
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