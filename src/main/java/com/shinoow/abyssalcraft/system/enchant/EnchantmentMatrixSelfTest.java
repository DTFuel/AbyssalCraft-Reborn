package com.shinoow.abyssalcraft.system.enchant;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shinoow.abyssalcraft.content.item.ritual.RitualItems;
import com.shinoow.abyssalcraft.platform.EnchantmentCompat;

/** Permanent T7.9b/c acquisition, applicability, exclusivity and production-effect matrix. */
public final class EnchantmentMatrixSelfTest {

    private static final List<String> IDS = List.of(
        "blinding_light", "iron_wall", "light_pierce", "multi_rend", "sapping");

    private EnchantmentMatrixSelfTest() {}

    public static void run() {
        int[] levels = {1, 1, 5, 1, 3};
        int[] weights = {10, 5, 10, 2, 5};
        int[] minBases = {14, 14, 5, 20, 12};
        int[] minPerLevels = {0, 0, 8, 0, 8};
        int[] maxBases = {44, 44, 25, 40, 32};
        String[] targets = {"minecraft:shield", "#minecraft:enchantable/chest_armor",
            "#minecraft:enchantable/sharp_weapon", "#abyssalcraft:enchantable/staff_of_rending",
            "#abyssalcraft:enchantable/staff_of_rending"};
        for (int index = 0; index < IDS.size(); index++) {
            JsonObject enchantment = json("data/abyssalcraft/enchantment/" + IDS.get(index) + ".json");
            require(enchantment.get("max_level").getAsInt() == levels[index],
                IDS.get(index) + " max level changed");
            require(enchantment.get("weight").getAsInt() == weights[index],
                IDS.get(index) + " acquisition weight changed");
            require(cost(enchantment, "min_cost", "base") == minBases[index]
                && cost(enchantment, "min_cost", "per_level_above_first") == minPerLevels[index]
                && cost(enchantment, "max_cost", "base") == maxBases[index]
                && cost(enchantment, "max_cost", "per_level_above_first") == minPerLevels[index],
                IDS.get(index) + " enchanting cost changed");
            require(targets[index].equals(enchantment.get("supported_items").getAsString()),
                IDS.get(index) + " supported-items target changed");
        }
        JsonObject lightPierce = json("data/abyssalcraft/enchantment/light_pierce.json");
        require("#minecraft:exclusive_set/damage".equals(lightPierce.get("exclusive_set").getAsString()),
            "light_pierce must exclude vanilla damage enchantments");

        JsonObject staffTag = json("data/abyssalcraft/tags/item/enchantable/staff_of_rending.json");
        List<String> staffIds = staffTag.getAsJsonArray("values").asList().stream()
            .map(value -> value.getAsString()).toList();
        require(staffIds.equals(List.of("abyssalcraft:staff_of_rending",
            "abyssalcraft:abyssal_wasteland_staff_of_rending",
            "abyssalcraft:dreadlands_staff_of_rending", "abyssalcraft:omothol_staff_of_rending")),
            "all four Staff of Rending tiers must be tagged exactly once");
        //? if <1.21 {
        require(EnchantmentCompat.isStaffOfRending(RitualItems.STAFF_OF_RENDING.get())
            && EnchantmentCompat.isStaffOfRending(RitualItems.OMOTHOL_STAFF_OF_RENDING.get()),
            "Forge Staff enchantment category must match production staff items");
        //?}

        require(EnchantmentEffects.preventsKnockback(1) && !EnchantmentEffects.preventsKnockback(0),
            "iron_wall knockback contract changed");
        require(EnchantmentEffects.absorbsShadowDamage(1, true, true)
            && !EnchantmentEffects.absorbsShadowDamage(1, false, true)
            && EnchantmentEffects.shieldDamage(4.9F) == 8,
            "blinding_light block/durability contract changed");
        require(EnchantmentEffects.lightPierceBonus(1) == 2.5F
            && EnchantmentEffects.lightPierceBonus(5) == 12.5F,
            "light_pierce shadow bonus changed");
        require(EnchantmentEffects.multiRendRadius(1) == 3.0D
            && EnchantmentEffects.multiRendRadius(0) == 0.0D,
            "multi_rend radius contract changed");
        require(EnchantmentEffects.sappingDrainAmount(0, 0) == 1
            && EnchantmentEffects.sappingDrainAmount(3, 3) == 7,
            "sapping drain contract changed");

        System.out.println("RR_ENCHANTMENT_MATRIX_OK enchantments=5 targets=5 effects=5 staffTiers=4");
    }

    private static JsonObject json(String path) {
        try (InputStream stream = EnchantmentMatrixSelfTest.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "missing resource " + path);
            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + path, exception);
        }
    }

    private static int cost(JsonObject enchantment, String range, String field) {
        return enchantment.getAsJsonObject(range).get(field).getAsInt();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("RR enchantment matrix failed: " + message);
    }
}