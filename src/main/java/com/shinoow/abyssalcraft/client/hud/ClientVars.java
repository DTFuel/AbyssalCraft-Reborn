package com.shinoow.abyssalcraft.client.hud;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Complete, validated representation of the legacy 94-field {@code clientvars.json} contract. */
public final class ClientVars {

    public static final int VERSION = 3;
    public static final int FIELD_COUNT = 94;
    public static final int CRYSTAL_COLOR_COUNT = 26;

    private static final Gson GSON = new Gson();
    private static final Map<String, Field> CONTRACT_FIELDS = contractFields();

    private String[] crystalColors;
    private int abyssalWastelandR;
    private int abyssalWastelandG;
    private int abyssalWastelandB;
    private int dreadlandsR;
    private int dreadlandsG;
    private int dreadlandsB;
    private int omotholR;
    private int omotholG;
    private int omotholB;
    private int darkRealmR;
    private int darkRealmG;
    private int darkRealmB;
    private String darklandsGrassColor;
    private String darklandsFoliageColor;
    private String darklandsWaterColor;
    private String darklandsSkyColor;
    private String darklandsPlainsGrassColor;
    private String darklandsPlainsFoliageColor;
    private String darklandsPlainsWaterColor;
    private String darklandsPlainsSkyColor;
    private String darklandsForestGrassColor;
    private String darklandsForestFoliageColor;
    private String darklandsForestWaterColor;
    private String darklandsForestSkyColor;
    private String darklandsHighlandsGrassColor;
    private String darklandsHighlandsFoliageColor;
    private String darklandsHighlandsWaterColor;
    private String darklandsHighlandsSkyColor;
    private String darklandsMountainsGrassColor;
    private String darklandsMountainsFoliageColor;
    private String darklandsMountainsWaterColor;
    private String darklandsMountainsSkyColor;
    private String coraliumInfestedSwampGrassColor;
    private String coraliumInfestedSwampFoliageColor;
    private String coraliumInfestedSwampWaterColor;
    private String abyssalWastelandGrassColor;
    private String abyssalWastelandFoliageColor;
    private String abyssalWastelandWaterColor;
    private String abyssalWastelandSkyColor;
    private String dreadlandsGrassColor;
    private String dreadlandsFoliageColor;
    private String dreadlandsSkyColor;
    private String dreadlandsForestGrassColor;
    private String dreadlandsForestFoliageColor;
    private String dreadlandsForestSkyColor;
    private String dreadlandsMountainsGrassColor;
    private String dreadlandsMountainsFoliageColor;
    private String dreadlandsMountainsSkyColor;
    private String omotholGrassColor;
    private String omotholFoliageColor;
    private String omotholWaterColor;
    private String omotholSkyColor;
    private String darkRealmGrassColor;
    private String darkRealmFoliageColor;
    private String darkRealmWaterColor;
    private String darkRealmSkyColor;
    private String purgedGrassColor;
    private String purgedFoliageColor;
    private String purgedWaterColor;
    private String purgedSkyColor;
    private String coraliumPlaguePotionColor;
    private String dreadPlaguePotionColor;
    private String antimatterPotionColor;
    private int asorahDeathR;
    private int asorahDeathG;
    private int asorahDeathB;
    private int jzaharDeathR;
    private int jzaharDeathG;
    private int jzaharDeathB;
    private int implosionR;
    private int implosionG;
    private int implosionB;
    private String coraliumAntidotePotionColor;
    private String dreadAntidotePotionColor;
    private String abyssalDesertGrassColor;
    private String abyssalDesertFoliageColor;
    private String abyssalDesertWaterColor;
    private String abyssalDesertSkyColor;
    private String abyssalPlateauGrassColor;
    private String abyssalPlateauFoliageColor;
    private String abyssalPlateauWaterColor;
    private String abyssalPlateauSkyColor;
    private String abyssalSwampGrassColor;
    private String abyssalSwampFoliageColor;
    private String abyssalSwampWaterColor;
    private String abyssalSwampSkyColor;
    private String coraliumLakeGrassColor;
    private String coraliumLakeFoliageColor;
    private String coraliumLakeWaterColor;
    private String coraliumLakeSkyColor;
    private String dreadlandsOceanGrassColor;
    private String dreadlandsOceanFoliageColor;
    private String dreadlandsOceanSkyColor;

    public ClientVars() {
        crystalColors = new String[] {"0xD9D9D9", "0xF3CC3E", "0xF6FF00", "0x3D3D36", "0x00ffcc",
            "16777215", "16777215", "0x996A18", "0xD9D9D9", "0x1500FF", "0x19FC00", "0xFF0000",
            "0x4a1c89", "0x00FFEE", "0x880101", "0xFFCC00", "0x666666", "0xD9D9D9", "0xD9D9D9",
            "0x666666", "0xD9D8D9", "16777215", "0xD7D8D9", "16777215", "0xD7D8D9", "0xD9D9D9"};
        abyssalWastelandR = 0;
        abyssalWastelandG = 105;
        abyssalWastelandB = 45;
        dreadlandsR = 100;
        dreadlandsG = 14;
        dreadlandsB = 14;
        omotholR = 40;
        omotholG = 30;
        omotholB = 40;
        darkRealmR = 30;
        darkRealmG = 20;
        darkRealmB = 30;
        darklandsGrassColor = "0x17375c";
        darklandsFoliageColor = "0x17375c";
        darklandsWaterColor = "14745518";
        darklandsSkyColor = "0";
        darklandsPlainsGrassColor = "0x17375c";
        darklandsPlainsFoliageColor = "0x17375c";
        darklandsPlainsWaterColor = "14745518";
        darklandsPlainsSkyColor = "0";
        darklandsForestGrassColor = "0x17375c";
        darklandsForestFoliageColor = "0x17375c";
        darklandsForestWaterColor = "14745518";
        darklandsForestSkyColor = "0";
        darklandsHighlandsGrassColor = "0x17375c";
        darklandsHighlandsFoliageColor = "0x17375c";
        darklandsHighlandsWaterColor = "14745518";
        darklandsHighlandsSkyColor = "0";
        darklandsMountainsGrassColor = "0x17375c";
        darklandsMountainsFoliageColor = "0x17375c";
        darklandsMountainsWaterColor = "14745518";
        darklandsMountainsSkyColor = "0";
        coraliumInfestedSwampGrassColor = "0x59c6b4";
        coraliumInfestedSwampFoliageColor = "0x59c6b4";
        coraliumInfestedSwampWaterColor = "0x24FF83";
        abyssalWastelandGrassColor = "0x447329";
        abyssalWastelandFoliageColor = "0x447329";
        abyssalWastelandWaterColor = "0x24FF83";
        abyssalWastelandSkyColor = "0";
        dreadlandsGrassColor = "0x910000";
        dreadlandsFoliageColor = "0x910000";
        dreadlandsSkyColor = "0";
        dreadlandsForestGrassColor = "0x910000";
        dreadlandsForestFoliageColor = "0x910000";
        dreadlandsForestSkyColor = "0";
        dreadlandsMountainsGrassColor = "0x910000";
        dreadlandsMountainsFoliageColor = "0x910000";
        dreadlandsMountainsSkyColor = "0";
        omotholGrassColor = "0x17375c";
        omotholFoliageColor = "0x17375c";
        omotholWaterColor = "14745518";
        omotholSkyColor = "0";
        darkRealmGrassColor = "0x17375c";
        darkRealmFoliageColor = "0x17375c";
        darkRealmWaterColor = "14745518";
        darkRealmSkyColor = "0";
        purgedGrassColor = "0xD7D8D9";
        purgedFoliageColor = "0xD7D8D9";
        purgedWaterColor = "0xD7D8D9";
        purgedSkyColor = "0xD7D8D9";
        coraliumPlaguePotionColor = "0x00FFFF";
        dreadPlaguePotionColor = "0xAD1313";
        antimatterPotionColor = "0xFFFFFF";
        asorahDeathR = 0;
        asorahDeathG = 255;
        asorahDeathB = 255;
        jzaharDeathR = 81;
        jzaharDeathG = 189;
        jzaharDeathB = 178;
        implosionR = 255;
        implosionG = 255;
        implosionB = 255;
        coraliumAntidotePotionColor = "0x00ff06";
        dreadAntidotePotionColor = "0x00ff06";
        abyssalDesertGrassColor = "0x789455";
        abyssalDesertFoliageColor = "0x789455";
        abyssalDesertWaterColor = "0x24FF83";
        abyssalDesertSkyColor = "0";
        abyssalPlateauGrassColor = "0x2e7e67";
        abyssalPlateauFoliageColor = "0x2e7e67";
        abyssalPlateauWaterColor = "0x24FF83";
        abyssalPlateauSkyColor = "0";
        abyssalSwampGrassColor = "0x447329";
        abyssalSwampFoliageColor = "0x447329";
        abyssalSwampWaterColor = "0x24FF83";
        abyssalSwampSkyColor = "0";
        coraliumLakeGrassColor = "0x59c6b4";
        coraliumLakeFoliageColor = "0x59c6b4";
        coraliumLakeWaterColor = "0x24FF83";
        coraliumLakeSkyColor = "0";
        dreadlandsOceanGrassColor = "0x910000";
        dreadlandsOceanFoliageColor = "0x910000";
        dreadlandsOceanSkyColor = "0";
    }

    public static ClientVars parse(JsonObject root) {
        Set<String> expected = new LinkedHashSet<>(CONTRACT_FIELDS.keySet());
        expected.add("version");
        if (!root.keySet().equals(expected)) {
            Set<String> missing = new LinkedHashSet<>(expected);
            missing.removeAll(root.keySet());
            Set<String> extra = new LinkedHashSet<>(root.keySet());
            extra.removeAll(expected);
            throw new IllegalArgumentException("clientvars keys differ; missing=" + missing + ", extra=" + extra);
        }
        JsonElement version = root.get("version");
        if (!version.isJsonPrimitive() || version.getAsInt() != VERSION) {
            throw new IllegalArgumentException("clientvars version must be " + VERSION);
        }
        ClientVars parsed = GSON.fromJson(root, ClientVars.class);
        parsed.validate();
        return parsed;
    }

    public static Set<String> contractKeys() {
        return CONTRACT_FIELDS.keySet();
    }

    public static ContractStats contractStats() {
        int arrays = 0;
        int integers = 0;
        int strings = 0;
        for (Field field : CONTRACT_FIELDS.values()) {
            if (field.getType() == String[].class) arrays++;
            else if (field.getType() == int.class) integers++;
            else if (field.getType() == String.class) strings++;
        }
        return new ContractStats(CONTRACT_FIELDS.size(), arrays, integers, strings);
    }

    private void validate() {
        if (crystalColors == null || crystalColors.length != CRYSTAL_COLOR_COUNT) {
            throw new IllegalArgumentException("crystalColors must contain exactly 26 entries");
        }
        for (int index = 0; index < crystalColors.length; index++) {
            decodeColor(crystalColors[index], "crystalColors[" + index + "]");
        }
        for (Map.Entry<String, Field> entry : CONTRACT_FIELDS.entrySet()) {
            Field field = entry.getValue();
            try {
                if (field.getType() == int.class) {
                    int value = field.getInt(this);
                    if (value < 0 || value > 255) {
                        throw new IllegalArgumentException(entry.getKey() + " must be in 0..255");
                    }
                } else if (field.getType() == String.class) {
                    decodeColor((String) field.get(this), entry.getKey());
                }
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Unable to validate " + entry.getKey(), exception);
            }
        }
    }

    public int[] crystalColors() {
        return Arrays.stream(crystalColors).mapToInt(value -> decodeColor(value, "crystalColors")).toArray();
    }

    public int crystalColor(int index) {
        if (index < 0 || index >= crystalColors.length) return 0xFFFFFF;
        return decodeColor(crystalColors[index], "crystalColors[" + index + "]");
    }

    public int color(String fieldName) {
        Field field = CONTRACT_FIELDS.get(fieldName);
        if (field == null || field.getType() != String.class) {
            throw new IllegalArgumentException("Unknown client color field " + fieldName);
        }
        try {
            return decodeColor((String) field.get(this), fieldName);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read " + fieldName, exception);
        }
    }

    public int abyssalWastelandR() { return abyssalWastelandR; }
    public int abyssalWastelandG() { return abyssalWastelandG; }
    public int abyssalWastelandB() { return abyssalWastelandB; }
    public int dreadlandsR() { return dreadlandsR; }
    public int dreadlandsG() { return dreadlandsG; }
    public int dreadlandsB() { return dreadlandsB; }
    public int omotholR() { return omotholR; }
    public int omotholG() { return omotholG; }
    public int omotholB() { return omotholB; }
    public int darkRealmR() { return darkRealmR; }
    public int darkRealmG() { return darkRealmG; }
    public int darkRealmB() { return darkRealmB; }
    public int asorahDeathColor() { return rgb(asorahDeathR, asorahDeathG, asorahDeathB); }
    public int jzaharDeathColor() { return rgb(jzaharDeathR, jzaharDeathG, jzaharDeathB); }
    public int implosionColor() { return rgb(implosionR, implosionG, implosionB); }

    private static int rgb(int red, int green, int blue) {
        return red << 16 | green << 8 | blue;
    }

    private static int decodeColor(String value, String fieldName) {
        if (value == null) throw new IllegalArgumentException(fieldName + " is null");
        try {
            int decoded = Integer.decode(value);
            if (decoded < 0 || decoded > 0xFFFFFF) {
                throw new IllegalArgumentException(fieldName + " must decode to RGB 0x000000..0xFFFFFF");
            }
            return decoded;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " is not a decodable color: " + value, exception);
        }
    }

    private static Map<String, Field> contractFields() {
        Map<String, Field> result = new LinkedHashMap<>();
        for (Field field : ClientVars.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            result.put(field.getName(), field);
        }
        return Map.copyOf(result);
    }

    public record ContractStats(int fields, int arrays, int integers, int strings) {}
}
