package com.shinoow.abyssalcraft.client.hud;

/**
 * Object representation of {@code assets/abyssalcraft/clientvars.json} (owned by PH-6), faithful to the 1.12.2
 * {@code lib.util.ClientVars} &mdash; hot-reloadable client-side colours (potion tints, dimension/biome
 * colours) driven by JSON so pack makers can retint without code. A representative subset is modelled here;
 * the full ~100-field colour set is deferred content (asset, PK). Deserialised by Gson (field names match).
 */
public final class ClientVars {

    private String coraliumPlaguePotionColor = "00FFFF";
    private String dreadPlaguePotionColor = "AD1313";
    private String antimatterPotionColor = "FFFFFF";
    private int abyssalWastelandR = 36;
    private int abyssalWastelandG = 255;
    private int abyssalWastelandB = 131;
    private int dreadlandsR = 145;
    private int dreadlandsG = 0;
    private int dreadlandsB = 0;

    public String coraliumPlaguePotionColor() {
        return coraliumPlaguePotionColor;
    }

    public String dreadPlaguePotionColor() {
        return dreadPlaguePotionColor;
    }

    public String antimatterPotionColor() {
        return antimatterPotionColor;
    }

    public int abyssalWastelandR() {
        return abyssalWastelandR;
    }

    public int abyssalWastelandG() {
        return abyssalWastelandG;
    }

    public int abyssalWastelandB() {
        return abyssalWastelandB;
    }

    public int dreadlandsR() {
        return dreadlandsR;
    }

    public int dreadlandsG() {
        return dreadlandsG;
    }

    public int dreadlandsB() {
        return dreadlandsB;
    }
}
