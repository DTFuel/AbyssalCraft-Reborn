package com.shinoow.abyssalcraft.system.energy;

/** Registry-name contract for the four five-tier PE network block families. */
public enum EnergyBlockKind {
    COLLECTOR("energycollector", "energy_collector"),
    CONTAINER("energycontainer", "energy_container"),
    PEDESTAL("energypedestal", "energy_pedestal"),
    RELAY("energyrelay", "energy_relay");

    private final String basicId;
    private final String tieredSuffix;

    EnergyBlockKind(String basicId, String tieredSuffix) {
        this.basicId = basicId;
        this.tieredSuffix = tieredSuffix;
    }

    public String id(EnergyTier tier) {
        return tier == EnergyTier.BASIC ? basicId : tier.id(tieredSuffix);
    }
}