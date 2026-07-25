package com.shinoow.abyssalcraft.system.spell;

/**
 * Quality of a Necronomicon scroll (owned by PS-7), faithful to the 1.12.2
 * {@code api.spell.SpellEnum.ScrollType}. A scroll can inscribe / cast any spell whose required
 * {@link ScrollType} quality is less than or equal to the scroll's own.
 */
public enum ScrollType {

    NONE(-1),
    BASIC(0),
    LESSER(1),
    MODERATE(2),
    GREATER(3),
    UNIQUE(4);

    private final int quality;

    ScrollType(int quality) {
        this.quality = quality;
    }

    public int quality() {
        return quality;
    }

    public static ScrollType byQuality(int quality) {
        for (ScrollType type : values()) {
            if (type.quality == quality) {
                return type;
            }
        }
        return NONE;
    }
}
