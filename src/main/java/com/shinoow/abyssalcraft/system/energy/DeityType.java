package com.shinoow.abyssalcraft.system.energy;

/**
 * The seven deities of the Potential Energy system (owned by PS-5), faithful to the 1.12.2
 * {@code EnergyEnum.DeityType}. A collector/statue is tied to a deity; energy manipulation can be
 * limited to matching deities.
 */
public enum DeityType {
    CTHULHU("Cthulhu"),
    HASTUR("Hastur"),
    JZAHAR("J'zahar"),
    AZATHOTH("Azathoth"),
    NYARLATHOTEP("Nyarlathotep"),
    SHUBNIGGURATH("Shub-Niggurath"),
    YOGSOTHOTH("Yog-Sothoth");

    private final String displayName;

    DeityType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
