package com.shinoow.abyssalcraft.config;

import java.util.Map;
import java.util.Set;

/** Permanent RR-CONTENT production-gate matrix. */
public final class ContentConfigMatrix {

    private static final Set<String> CONSUMED = Set.of(
        "smeltingRecipes", "purgeMobSpawns", "armorPotionEffects", "portalSpawnsNearPlayer",
        "showBossDialogs", "lootTableContent", "nightVisionEverywhere", "tombstoneMaxSpawn",
        "tombstoneCooldown", "tombstoneGhoulDistance");
    private static final Map<String, String> OWNERS = Map.of(
        "smeltingRecipes", "mixin.RecipeManagerMixin",
        "purgeMobSpawns", "common.handlers.PurgeHooks",
        "armorPotionEffects", "content.item.armor.ArmorEffects",
        "portalSpawnsNearPlayer", "content.entity.misc.DimensionPortal",
        "showBossDialogs", "content.entity.boss.BossMob",
        "lootTableContent", "platform.ContentLootCompat",
        "nightVisionEverywhere", "content.item.armor.ArmorEffects",
        "tombstoneMaxSpawn", "content.block.deco.TombstoneBlockEntity",
        "tombstoneCooldown", "content.block.deco.TombstoneBlockEntity",
        "tombstoneGhoulDistance", "content.block.deco.TombstoneBlockEntity");

    private ContentConfigMatrix() {}

    public static boolean smeltingRecipes() {
        return configuredBool(ACConfig.smeltingRecipes);
    }

    public static boolean purgeMobSpawns() {
        return ACConfig.purgeMobSpawns != null && Boolean.TRUE.equals(ACConfig.purgeMobSpawns.get());
    }

    public static boolean armorPotionEffects() {
        return configuredBool(ACConfig.armorPotionEffects);
    }

    public static boolean portalSpawnsNearPlayer() {
        return configuredBool(ACConfig.portalSpawnsNearPlayer);
    }

    public static boolean showBossDialogs() {
        return configuredBool(ACConfig.showBossDialogs);
    }

    public static boolean lootTableContent() {
        return configuredBool(ACConfig.lootTableContent);
    }

    public static boolean nightVisionEverywhere() {
        return configuredBool(ACConfig.nightVisionEverywhere);
    }

    public static int tombstoneMaxSpawn() {
        return configuredInt(ACConfig.tombstoneMaxSpawn);
    }

    public static int tombstoneCooldown() {
        return configuredInt(ACConfig.tombstoneCooldown);
    }

    public static int tombstoneGhoulDistance() {
        return configuredInt(ACConfig.tombstoneGhoulDistance);
    }

    static Set<String> consumedKeys() {
        return CONSUMED;
    }

    static Set<String> blockedKeys() {
        return Set.of();
    }

    static Map<String, String> productionOwners() {
        return OWNERS;
    }

    static boolean testGate(boolean value) {
        return value;
    }

    private static boolean configuredBool(java.util.function.Supplier<Boolean> value) {
        return value != null && Boolean.TRUE.equals(value.get());
    }

    private static int configuredInt(java.util.function.Supplier<Integer> value) {
        if (value == null) return 0;
        Integer configured = value.get();
        return configured == null ? 0 : configured;
    }
}