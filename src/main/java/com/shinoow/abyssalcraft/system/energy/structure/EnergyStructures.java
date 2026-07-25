package com.shinoow.abyssalcraft.system.energy.structure;

import java.util.List;

/** Canonical catalog and idempotent bootstrap for the three implemented Places of Power. */
public final class EnergyStructures {

    public static final IPlaceOfPower BASIC = new BasicPlaceOfPower();
    public static final IPlaceOfPower TOTEM_POLE = new TotemPolePlaceOfPower();
    public static final IPlaceOfPower ARCHWAY = new ArchwayPlaceOfPower();
    public static final List<IPlaceOfPower> ALL = List.of(BASIC, TOTEM_POLE, ARCHWAY);

    private static boolean bootstrapped;

    private EnergyStructures() {}

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        ALL.forEach(StructureHandler.instance()::registerStructure);
    }
}