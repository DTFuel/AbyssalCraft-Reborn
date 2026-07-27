package com.shinoow.abyssalcraft.content.entity.behavior;

import net.minecraft.core.registries.BuiltInRegistries;

import com.shinoow.abyssalcraft.AbyssalCraft;
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
        System.out.printf(
            "RR_ENTITY_BEHAVIOR_SELF_TEST_OK content=63 audit=69 direct=%d conditional=%d replaced=%d retired=%d baselineLoot=34 logicalLoot=97 emptyLoot=8 spawnPairs=9 snapshots=18%n",
            summary.direct(), summary.conditional(), summary.replaced(), summary.retired());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}