package com.shinoow.abyssalcraft.system.ritual;

import com.shinoow.abyssalcraft.system.data.NecromancyData;

import net.minecraft.nbt.CompoundTag;

/** Permanent T7.11c snapshot, crystal, degradation, completion and reload matrix. */
public final class ResurrectionMatrixSelfTest {

    private ResurrectionMatrixSelfTest() {}

    public static void run() {
        NecromancyData snapshots = new NecromancyData();
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "minecraft:zombie");
        entity.putInt("HealthFixture", 17);
        snapshots.storeData("Fixture", entity, 2);
        CompoundTag read = snapshots.getDataForName("Fixture");
        require(read != null && read.getInt("HealthFixture") == 17
            && read.getInt("ResurrectionRitualCrystalSize") == 2, "snapshot read or crystal size changed");

        NecromancyData reloaded = NecromancyData.load(snapshots.serialize());
        require(reloaded.getDataForName("Fixture") != null
            && reloaded.getDataForName("Fixture").getInt("HealthFixture") == 17,
            "snapshot NBT reload changed");

        require(ResurrectionBehavior.crystalOfferingSize("crystal_shard_carbon") == 0
            && ResurrectionBehavior.crystalOfferingSize("crystal_carbon") == 1
            && ResurrectionBehavior.crystalOfferingSize("carbon_crystal_cluster") == 2
            && ResurrectionBehavior.crystalOfferingSize("diamond") == -1,
            "crystal offering size matrix changed");

        float[] chances = {0.90F, 0.75F, 0.60F, 0.45F, 0.30F, 0.15F, 1.0F};
        for (int reanimations = 4; reanimations <= 10; reanimations++) {
            require(ResurrectionBehavior.degradationChance(reanimations) == chances[reanimations - 4],
                "degradation chance changed at reanimation " + reanimations);
        }
        require(ResurrectionBehavior.degradationChance(3) == 0.0F
            && ResurrectionBehavior.degradationChance(11) == 0.0F,
            "degradation must only apply to legacy reanimations 4-10");

        ResurrectionBehavior.clearSnapshotAfterSpawn(reloaded, "Fixture", false);
        require(reloaded.getDataForName("Fixture") != null, "failed completion must retain the snapshot");
        ResurrectionBehavior.clearSnapshotAfterSpawn(reloaded, "Fixture", true);
        require(reloaded.getDataForName("Fixture") == null, "successful completion must clear the snapshot");
        require(NecromancyData.load(reloaded.serialize()).getDataForName("Fixture") == null,
            "cleared snapshot must stay cleared after NBT reload");

        System.out.println("RR_RESURRECTION_MATRIX_OK snapshots=1 crystalSizes=3 degradation=4-10 failure=retain success=clear reload=ok");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("RR resurrection matrix failed: " + message);
    }
}