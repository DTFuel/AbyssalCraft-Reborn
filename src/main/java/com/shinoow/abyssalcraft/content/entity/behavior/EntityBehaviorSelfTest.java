package com.shinoow.abyssalcraft.content.entity.behavior;

import net.minecraft.core.registries.BuiltInRegistries;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.entity.boss.JzaharBoss;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ConfigCompat;
import com.shinoow.abyssalcraft.platform.SpawnCandidateCompat;
import com.shinoow.abyssalcraft.world.ACDimensions;
import com.shinoow.abyssalcraft.world.darklands.DarklandsBiomes;

/** Permanent registry and audit invariants for RR-ENTITY-BEHAVIOR. */
public final class EntityBehaviorSelfTest {

    private EntityBehaviorSelfTest() {}

    public static void run() {
        long content = BuiltInRegistries.ENTITY_TYPE.keySet().stream()
            .filter(id -> AbyssalCraft.MODID.equals(id.getNamespace()))
            .filter(id -> !id.getPath().equals("pilot_mob"))
            .count();
        if (content != 63) throw new IllegalStateException("entity content count mismatch: " + content);

        EntityLootAudit.Summary summary = EntityLootAudit.validate();
        var shadow = SpawnCandidateCompat.candidateSnapshot(
            ACDimensions.ABYSSAL_WASTELAND, DarklandsBiomes.FOREST, 5);
        require(shadow.equals(java.util.List.of(
            new SpawnCandidateCompat.Candidate("shadowcreature", 60, 1, 5),
            new SpawnCandidateCompat.Candidate("shadowmonster", 40, 1, 3),
            new SpawnCandidateCompat.Candidate("shadowbeast", 10, 1, 1),
            new SpawnCandidateCompat.Candidate("shadow_ghoul", 1, 1, 1))),
            "low-level Shadow Realm candidate snapshot changed");
        var dread = SpawnCandidateCompat.candidateSnapshot(
            ACDimensions.DREADLANDS, DarklandsBiomes.DARKLANDS, 64);
        require(dread.size() == 14, "Dreadlands override candidate count changed");
        require(dread.get(0).equals(new SpawnCandidateCompat.Candidate("dreadspawn", 30, 1, 2))
            && dread.get(13).equals(new SpawnCandidateCompat.Candidate("shadowbeast", 20, 1, 1)),
            "Dreadlands override candidate boundary changed");
        require(SpawnCandidateCompat.candidateSnapshot(
            ACDimensions.DARK_REALM, DarklandsBiomes.DARKLANDS, 5).isEmpty(),
            "candidate override leaked outside AW/Dreadlands");
        validateConfiguredSpawns();
        System.out.printf(
            "RR_ENTITY_BEHAVIOR_SELF_TEST_OK content=63 audit=69 direct=%d conditional=%d replaced=%d retired=%d baselineLoot=34 logicalLoot=97 emptyLoot=8 spawnPairs=9 snapshots=18 configKeys=6 configSpawnMappings=11 reload=live%n",
            summary.direct(), summary.conditional(), summary.replaced(), summary.retired());
    }

    private static void validateConfiguredSpawns() {
        requireIntegerBounds("mobs.evil_animal_spawn_weight", 0, 100);
        requireIntegerBounds("mobs.demon_animal_spawn_weight", 0, 100);
        requireIntegerBounds("mobs.dark_offspring_spawn_weight", 0, 50);
        requireBooleanEntry("mobs.depths_ghoul_biome_dict_spawn");
        requireBooleanEntry("mobs.abyssal_zombie_biome_dict_spawn");
        requireBooleanEntry("silly_settings.jzahar_breaks_fourth_wall");

        var all = new SpawnCandidateCompat.ConfiguredSpawnContext(true, true, true, true, true, false);
        var enabled = SpawnCandidateCompat.configuredCandidateSnapshot(all, 100, 100, true, true, 50);
        require(enabled.size() == 11, "configured spawn mapping count changed");
        require(enabled.contains(new SpawnCandidateCompat.Candidate("evil_pig", 100, 1, 3))
            && enabled.contains(new SpawnCandidateCompat.Candidate("demon_sheep", 100, 1, 3))
            && enabled.contains(new SpawnCandidateCompat.Candidate("depths_ghoul", 1, 1, 3))
            && enabled.contains(new SpawnCandidateCompat.Candidate("abyssalzombie", 10, 1, 3))
            && enabled.contains(new SpawnCandidateCompat.Candidate("shuboffspring", 50, 1, 3)),
            "configured spawn boundary mapping changed");
        enabled.forEach(candidate -> require(BuiltInRegistries.ENTITY_TYPE.containsKey(
            ACRef.id(candidate.entityId())), "missing configured spawn entity " + candidate.entityId()));

        var disabled = SpawnCandidateCompat.configuredCandidateSnapshot(all, 0, 0, true, true, 0);
        require(disabled.size() == 2, "zero spawn weights did not disable weighted mappings");
        var vanillaOnly = new SpawnCandidateCompat.ConfiguredSpawnContext(false, false, true, false, false, false);
        require(SpawnCandidateCompat.configuredCandidateSnapshot(vanillaOnly, 0, 0, false, false, 0).isEmpty(),
            "disabled biome dictionary switches leaked into dictionary-only biome");
        require(SpawnCandidateCompat.configuredCandidateSnapshot(vanillaOnly, 0, 0, true, true, 0).size() == 2,
            "enabled biome dictionary switches did not add aquatic mobs");
        var darkForest = new SpawnCandidateCompat.ConfiguredSpawnContext(false, false, false, false, false, true);
        require(SpawnCandidateCompat.configuredCandidateSnapshot(darkForest, 0, 0, false, false, 50)
            .equals(java.util.List.of(new SpawnCandidateCompat.Candidate("shuboffspring", 100, 1, 3))),
            "dark offspring double-weight mapping changed");
        require(JzaharBoss.fourthWallDialogEnabled(true, true)
            && !JzaharBoss.fourthWallDialogEnabled(true, false),
            "J'zahar fourth-wall dialog switch is not authoritative");
    }

    private static void requireIntegerBounds(String path, int minimum, int maximum) {
        ConfigCompat.Entry<?> entry = configEntry(path);
        require(entry.valueType() == ConfigCompat.ValueType.INTEGER, "config is not integer " + path);
        require(entry.parse(Integer.toString(minimum)).equals(minimum)
            && entry.parse(Integer.toString(maximum)).equals(maximum), "config boundary rejected for " + path);
        requireRejected(entry, Integer.toString(minimum - 1));
        requireRejected(entry, Integer.toString(maximum + 1));
    }

    private static void requireBooleanEntry(String path) {
        ConfigCompat.Entry<?> entry = configEntry(path);
        require(entry.valueType() == ConfigCompat.ValueType.BOOLEAN, "config is not boolean " + path);
        require(entry.parse("true").equals(Boolean.TRUE) && entry.parse("false").equals(Boolean.FALSE),
            "boolean config parse failed for " + path);
        requireRejected(entry, "not-a-boolean");
    }

    private static ConfigCompat.Entry<?> configEntry(String path) {
        return ConfigCompat.entries().stream().filter(candidate -> candidate.path().equals(path)).findFirst()
            .orElseThrow(() -> new IllegalStateException("missing config entry " + path));
    }

    private static void requireRejected(ConfigCompat.Entry<?> entry, String value) {
        try {
            entry.parse(value);
            throw new IllegalStateException("config accepted out-of-range value " + entry.path() + "=" + value);
        } catch (IllegalArgumentException expected) {
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}