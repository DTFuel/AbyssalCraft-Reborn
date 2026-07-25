package com.shinoow.abyssalcraft.client.render.armor;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ArmorRenderCompat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class MonsterArmorVisuals {

    private MonsterArmorVisuals() {}

    public static LayerData resolve(String shape, ItemStack stack, EquipmentSlot slot) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String material = material(itemId.getPath());
        String layer = slot == EquipmentSlot.LEGS ? "2" : "1";
        String root = "textures/armor/" + shape + "/";

        if ("leather".equals(material)) {
            return new LayerData(ACRef.id(root + "leather_" + layer + ".png"),
                ACRef.id(root + "leather_" + layer + "_overlay.png"), ArmorRenderCompat.leatherColor(stack));
        }
        if ("chainmail".equals(material)) {
            return new LayerData(ACRef.id(root + "chainmail_" + layer + ".png"), null, 0xFFFFFF);
        }
        if ("iron".equals(material)) {
            return new LayerData(ACRef.id(root + "base_" + layer + ".png"), null, 0xFFFFFF);
        }
        return new LayerData(ACRef.id(root + "base_" + layer + ".png"),
            ACRef.id(root + "base_" + layer + "_overlay.png"), materialColor(material));
    }

    private static String material(String path) {
        for (String material : new String[] {
            "dreadium_samurai", "refined_coralium", "plated_coralium", "abyssalnite",
            "chainmail", "leather", "diamond", "ethaxium", "dreadium", "depths", "golden", "iron"
        }) {
            if (path.startsWith(material + "_")) return material;
        }
        return path;
    }

    private static int materialColor(String material) {
        return switch (material) {
            case "golden" -> 0xF3CC3E;
            case "diamond" -> 0x4BFBEA;
            case "abyssalnite" -> 0x4A1C89;
            case "dreadium", "dreadium_samurai" -> 0x880101;
            case "refined_coralium", "plated_coralium", "depths" -> 0x067047;
            case "ethaxium" -> 0xADC3AC;
            default -> 0xFFFFFF;
        };
    }

    public record LayerData(ResourceLocation texture, ResourceLocation overlay, int color) {}
}