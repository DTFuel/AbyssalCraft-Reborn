package com.shinoow.abyssalcraft.system.ritual;

import java.util.List;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.platform.ItemNameCompat;
import com.shinoow.abyssalcraft.system.data.NecromancyData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

/** T7.11c fixture that exercises resurrection against a real dedicated-server level. */
public final class ResurrectionLiveFixture {

    private static final String ENTITY_NAME = "RR Resurrection Live Fixture";
    private static final String RESTART_PROBE = "__rr_resurrection_live_restart__";

    private ResurrectionLiveFixture() {}

    public enum Result {
        PENDING_RESTART,
        COMPLETE
    }

    public static Result run(MinecraftServer server) {
        ServerLevel level = server.overworld();
        NecromancyData snapshots = NecromancyData.get(level);
        CompoundTag restart = snapshots.getDataForName(RESTART_PROBE);
        if (restart != null) {
            require(restart.getBoolean("FixtureSaved"), "SavedData restart probe changed");
            snapshots.clearEntry(RESTART_PROBE);
            cleanup(level, level.getSharedSpawnPos());
            System.out.println("RR_RESURRECTION_LIVE_FIXTURE_OK spawn=1 failureRetained=1 successCleared=1 restart=ok");
            return Result.COMPLETE;
        }

        BlockPos altar = level.getSharedSpawnPos().above(2);
        FixtureHost failedHost = new FixtureHost();
        String snapshotName = failedHost.ritualCenter().getHoverName().getString();
        cleanup(level, altar);
        snapshots.clearEntry(snapshotName);
        snapshots.clearEntry(RESTART_PROBE);

        Mob source = EntityType.ZOMBIE.create(level);
        require(source != null, "unable to create source zombie");
        source.setCustomName(Component.literal(ENTITY_NAME));
        source.getPersistentData().putInt("Reanimations", 4);
        CompoundTag entityTag = source.saveWithoutId(new CompoundTag());
        entityTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(source.getType()).toString());
        snapshots.storeData(snapshotName, entityTag, 1);
        Entity[] failedSpawn = new Entity[1];
        ResurrectionBehavior failure = new ResurrectionBehavior((ignored, count) -> true,
            (ignored, entity) -> {
                failedSpawn[0] = entity;
                return false;
            });
        expectSpawnFailure(() -> failure.complete(null, level, altar, null, failedHost));
        require(failedSpawn[0] != null && failedSpawn[0].getType() == LegacyEntities.GREATER_DREAD_SPAWN.get(),
            "forced degradation did not replace the deserialized entity");
        require(snapshots.getDataForName(snapshotName) != null,
            "failed addFreshEntity removed the snapshot");

        FixtureHost successfulHost = new FixtureHost();
        ResurrectionBehavior success = new ResurrectionBehavior((ignored, count) -> true,
            ServerLevel::addFreshEntity);
        success.complete(null, level, altar, null, successfulHost);
        require(snapshots.getDataForName(snapshotName) == null, "successful spawn retained the snapshot");
        require(successfulHost.ritualCenter().isEmpty(), "successful completion retained the ritual center");
        require(level.getEntitiesOfClass(Mob.class, new AABB(altar).inflate(8.0D),
            entity -> ENTITY_NAME.equals(entity.getName().getString())
                && entity.getType() == LegacyEntities.GREATER_DREAD_SPAWN.get()).size() == 1,
            "successful addFreshEntity did not publish the degraded replacement");

        cleanup(level, altar);
        CompoundTag probe = new CompoundTag();
        probe.putString("id", "minecraft:zombie");
        probe.putBoolean("FixtureSaved", true);
        snapshots.storeData(RESTART_PROBE, probe, 0);
        System.out.println("RR_RESURRECTION_LIVE_FIXTURE_PENDING restart=required");
        return Result.PENDING_RESTART;
    }

    private static void cleanup(ServerLevel level, BlockPos center) {
        for (Mob entity : level.getEntitiesOfClass(Mob.class, new AABB(center).inflate(16.0D),
            entity -> ENTITY_NAME.equals(entity.getName().getString()))) {
            entity.discard();
        }
    }

    private static void expectSpawnFailure(Runnable action) {
        try {
            action.run();
            throw new IllegalStateException("RR resurrection live fixture failed: failed spawn did not throw");
        } catch (IllegalStateException exception) {
            require("Unable to spawn resurrected mob".equals(exception.getMessage()),
                "failed spawn raised an unexpected exception: " + exception.getMessage());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("RR resurrection live fixture failed: " + message);
    }

    private static final class FixtureHost implements RitualHost {

        private ItemStack center = fixtureCenter();

        private static ItemStack fixtureCenter() {
            ItemStack stack = new ItemStack(Items.PAPER);
            ItemNameCompat.setCustomName(stack, Component.literal(ENTITY_NAME));
            return stack;
        }

        @Override
        public ItemStack ritualCenter() {
            return center;
        }

        @Override
        public void setRitualCenter(ItemStack stack) {
            center = stack;
        }

        @Override
        public List<ItemStack> ritualOfferingSnapshot() {
            return List.of();
        }

        @Override
        public List<BlockPos> ritualPedestalPositions() {
            return List.of();
        }

        @Override
        public void fillRitualPedestals(ItemStack stack) {}
    }
}