package com.shinoow.abyssalcraft.system.energy;

/** Canonical PE capacities and relay rates for the five legacy energy-network tiers. */
public enum EnergyTier {
    BASIC("", 1000, 10000, 5000, 500, 4, 10, 20),
    OVERWORLD("overworld_", 1500, 20000, 7500, 600, 6, 20, 30),
    ABYSSAL_WASTELAND("abyssal_wasteland_", 2000, 60000, 10000, 700, 8, 30, 40),
    DREADLANDS("dreadlands_", 2500, 240000, 12500, 800, 10, 40, 50),
    OMOTHOL("omothol_", 3000, 1200000, 15000, 900, 12, 50, 60);

    private final String idPrefix;
    private final int collectorCapacity;
    private final int containerCapacity;
    private final int pedestalCapacity;
    private final int relayCapacity;
    private final int relayRange;
    private final float relayDrainQuanta;
    private final float transferQuanta;

    EnergyTier(String idPrefix, int collectorCapacity, int containerCapacity, int pedestalCapacity,
               int relayCapacity, int relayRange, float relayDrainQuanta, float transferQuanta) {
        this.idPrefix = idPrefix;
        this.collectorCapacity = collectorCapacity;
        this.containerCapacity = containerCapacity;
        this.pedestalCapacity = pedestalCapacity;
        this.relayCapacity = relayCapacity;
        this.relayRange = relayRange;
        this.relayDrainQuanta = relayDrainQuanta;
        this.transferQuanta = transferQuanta;
    }

    public String id(String suffix) {
        return idPrefix + suffix;
    }

    public int collectorCapacity() {
        return collectorCapacity;
    }

    public int containerCapacity() {
        return containerCapacity;
    }

    public int pedestalCapacity() {
        return pedestalCapacity;
    }

    public int relayCapacity() {
        return relayCapacity;
    }

    public int relayRange() {
        return relayRange;
    }

    public float relayDrainQuanta() {
        return relayDrainQuanta;
    }

    public float transferQuanta() {
        return transferQuanta;
    }

}