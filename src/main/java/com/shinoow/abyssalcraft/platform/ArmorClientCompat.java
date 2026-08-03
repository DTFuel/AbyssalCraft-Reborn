package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.client.render.armor.ACArmorVisuals;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
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
                return ACArmorVisuals.samuraiModel(entity, stack, slot, defaultModel);
            }
        };
    }
    //?}

    //? if >=1.21 {
    /*public static GeoRenderProvider samuraiGeoProvider() {
        return new GeoRenderProvider() {
            @Override
            public <T extends net.minecraft.world.entity.LivingEntity> net.minecraft.client.model.HumanoidModel<?>
                    getGeoArmorRenderer(T entity, net.minecraft.world.item.ItemStack stack,
                                        net.minecraft.world.entity.EquipmentSlot slot,
                                        net.minecraft.client.model.HumanoidModel<T> defaultModel) {
                return ACArmorVisuals.samuraiRenderer();
            }
        };
    }
    *///?}

    public static void attach(IEventBus modBus) {
    }
}