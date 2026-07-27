package com.shinoow.abyssalcraft.config;

import java.util.Set;

/** Permanent RR-CONTENT ten-key config matrix. */
public final class ContentConfigMatrixSelfTest {

    private ContentConfigMatrixSelfTest() {}

    public static void run() {
        require(ContentConfigMatrix.consumedKeys().equals(Set.of(
            "smeltingRecipes", "purgeMobSpawns", "armorPotionEffects", "portalSpawnsNearPlayer",
            "showBossDialogs", "lootTableContent", "nightVisionEverywhere", "tombstoneMaxSpawn",
            "tombstoneCooldown", "tombstoneGhoulDistance")),
            "RR-CONTENT consumed-key set changed");
        require(ContentConfigMatrix.blockedKeys().isEmpty(), "RR-CONTENT blocker set must stay empty");
        require(ContentConfigMatrix.productionOwners().keySet().equals(ContentConfigMatrix.consumedKeys()),
            "RR-CONTENT production-owner keys do not close");
        require(ContentConfigMatrix.productionOwners().values().stream()
            .allMatch(owner -> owner.contains(".") && !owner.contains("not ported")),
            "RR-CONTENT production owner is not concrete");
        require(!ContentConfigMatrix.testGate(false), "disabled content gate did not fail closed");
        require(ContentConfigMatrix.testGate(true), "enabled content gate did not pass");
        System.out.println("RR_CONTENT_CONFIG_MATRIX_OK consumed=10 blocked=0 dynamic=ok");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}