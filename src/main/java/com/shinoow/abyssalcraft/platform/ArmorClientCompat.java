package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.client.render.armor.ACArmorVisuals;
import com.shinoow.abyssalcraft.content.item.armor.ArmorItems;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
*///?}

public final class ArmorClientCompat {

    private ArmorClientCompat() {}

    //? if forge {
    public static net.minecraftforge.client.extensions.common.IClientItemExtensions samuraiExtension() {
        return new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(
                    net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.item.ItemStack stack,
                    net.minecraft.world.entity.EquipmentSlot slot,
                    net.minecraft.client.model.HumanoidModel<?> defaultModel) {
                return ACArmorVisuals.samuraiModel(slot, defaultModel);
            }
        };
    }
    //?} else {
    /*public static net.neoforged.neoforge.client.extensions.common.IClientItemExtensions samuraiExtension() {
        return new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(
                    net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.item.ItemStack stack,
                    net.minecraft.world.entity.EquipmentSlot slot,
                    net.minecraft.client.model.HumanoidModel<?> defaultModel) {
                return ACArmorVisuals.samuraiModel(slot, defaultModel);
            }
        };
    }
    *///?}

    public static void attach(IEventBus modBus) {
        //? if >=1.21 {
        /*modBus.addListener((RegisterClientExtensionsEvent event) -> {
            var extension = samuraiExtension();
            ArmorItems.ALL.stream().map(java.util.function.Supplier::get)
                .filter(item -> item instanceof ArmorCompat.VisualArmorItem visual
                    && visual.visual() == ArmorCompat.Visual.DREADIUM_SAMURAI)
                .forEach(item -> event.registerItem(extension, item));
        });
        *///?}
    }
}