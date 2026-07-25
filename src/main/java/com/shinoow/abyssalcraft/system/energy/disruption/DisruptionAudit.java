package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shinoow.abyssalcraft.system.energy.DeityType;

/** Frozen four-state audit for all twenty-seven disruptions registered by 1.12.2. */
public final class DisruptionAudit {

    public static final Set<String> MIGRATED = Set.of(
        "lightning", "fire", "spawnShoggoth", "poisonPotion", "slownessPotion", "weaknessPotion",
        "witherPotion", "coraliumPotion", "potentialEnergy", "freeze", "swarmShadow", "fireRain",
        "displace", "randomTeleport", "potentialEnergyDrain", "swarmSheep", "animalCorruption",
        "spawnShubOffspring", "sacrificeCorruptionJzahar", "sacrificeCorruptionYogSothoth",
        "famineAzathoth", "famineShuNiggurath");

    public static final Set<String> BLOCKED = Set.of(
        "ooze", "randomSwarm", "randomSpawn", "invisibleSwarmHastur", "invisibleSwarmNyarlathotep");

    public static final Map<String, DeityType> DEITY_LIMITS = Map.of(
        "swarmSheep", DeityType.SHUBNIGGURATH,
        "animalCorruption", DeityType.SHUBNIGGURATH,
        "spawnShubOffspring", DeityType.SHUBNIGGURATH,
        "sacrificeCorruptionJzahar", DeityType.JZAHAR,
        "sacrificeCorruptionYogSothoth", DeityType.YOGSOTHOTH,
        "famineAzathoth", DeityType.AZATHOTH,
        "famineShuNiggurath", DeityType.SHUBNIGGURATH);

    private DisruptionAudit() {}

    public static void validate(DisruptionHandler handler) {
        List<Disruption> registered = handler.getDisruptions();
        Set<String> names = registered.stream().map(Disruption::name).collect(java.util.stream.Collectors.toSet());
        require(registered.size() == MIGRATED.size(), "registered disruption count changed");
        require(names.equals(MIGRATED), "registered disruption catalog differs from MIGRATED audit");
        require(MIGRATED.stream().noneMatch(BLOCKED::contains), "disruption audit states overlap");
        require(MIGRATED.size() + BLOCKED.size() == 27, "legacy disruption audit is not closed");
        for (Disruption disruption : registered) {
            require(disruption.deity() == DEITY_LIMITS.get(disruption.name()),
                "disruption deity mapping changed: " + disruption.name());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}