package com.shinoow.abyssalcraft.platform;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
//? if >=1.21 {
/*import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
*///?} else {
import net.minecraft.core.registries.BuiltInRegistries;
//?}

/** Read and write item enchantments across the 1.20 object-map and 1.21 Holder-component APIs. */
public final class EnchantmentDataCompat {

    private EnchantmentDataCompat() {}

    public static Map<ResourceLocation, Integer> read(ItemStack stack) {
        Map<ResourceLocation, Integer> result = new LinkedHashMap<>();
        //? if >=1.21 {
        /*ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            ResourceLocation id = entry.getKey().unwrapKey()
                .map(ResourceKey::location).orElse(null);
            if (id != null) result.put(id, entry.getIntValue());
        }
        *///?} else {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(entry.getKey());
            if (id != null) result.put(id, entry.getValue());
        }
        //?}
        return result;
    }

    public static boolean canStore(ItemStack stack) {
        if (stack.is(Items.BOOK)) return true;
        //? if >=1.21 {
        /*return EnchantmentHelper.canStoreEnchantments(stack);
        *///?} else {
        return stack.isEnchantable();
        //?}
    }

    public static boolean hasRepairCost(ItemStack stack) {
        //? if >=1.21 {
        /*return stack.getOrDefault(DataComponents.REPAIR_COST, 0) > 0;
        *///?} else {
        return stack.hasTag() && stack.getTag().contains("RepairCost");
        //?}
    }

    public static ItemStack apply(ItemStack target, Map<ResourceLocation, Integer> enchantments,
                                  RegistryAccess registries) {
        ItemStack result = target.is(Items.BOOK) ? new ItemStack(Items.ENCHANTED_BOOK) : target.copy();
        //? if >=1.21 {
        /*var registry = registries.registryOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Map.Entry<ResourceLocation, Integer> entry : enchantments.entrySet()) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, entry.getKey());
            mutable.set(registry.getHolderOrThrow(key), entry.getValue());
        }
        EnchantmentHelper.setEnchantments(result, mutable.toImmutable());
        *///?} else {
        Map<Enchantment, Integer> resolved = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.get(entry.getKey());
            if (enchantment != null) resolved.put(enchantment, entry.getValue());
        }
        EnchantmentHelper.setEnchantments(resolved, result);
        //?}
        return result;
    }
}