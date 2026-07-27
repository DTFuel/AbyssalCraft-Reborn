package com.shinoow.abyssalcraft.system.ritual;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.platform.ItemNameCompat;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.system.data.NecromancyData;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Faithful named-snapshot resurrection with legacy reanimation degradation. */
public final class ResurrectionBehavior implements RitualBehavior {

    @Override
    public boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                            Player player, RitualHost host) {
        if (!(level instanceof ServerLevel server) || !ItemNameCompat.hasCustomName(host.ritualCenter())) return false;
        CompoundTag data = NecromancyData.get(server).getDataForName(host.ritualCenter().getHoverName().getString());
        if (data == null) return false;
        int size = data.getInt("ResurrectionRitualCrystalSize");
        if (size < 0 || size > 2) return false;
        return host.ritualOfferingSnapshot().stream().filter(stack -> !stack.isEmpty())
            .allMatch(stack -> crystalSize(stack) == size);
    }

    @Override
    public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                         Player player, RitualHost host) {
        if (!(level instanceof ServerLevel server)) return;
        String name = host.ritualCenter().getHoverName().getString();
        NecromancyData snapshots = NecromancyData.get(server);
        CompoundTag data = snapshots.getDataForName(name);
        if (data == null) throw new IllegalStateException("Missing necromancy snapshot for " + name);

        Entity loaded = EntityType.create(data.copy(), server).orElse(null);
        if (!(loaded instanceof Mob mob)) throw new IllegalStateException("Snapshot is not a living mob: " + name);
        Mob revived = degraded(server, mob, data.getInt("ResurrectionRitualCrystalSize"));
        revived.moveTo(altar.getX() + 0.5D, altar.getY() + 1.0D, altar.getZ() + 0.5D,
            revived.getYRot(), revived.getXRot());
        revived.setHealth(revived.getMaxHealth());
        if (!server.addFreshEntity(revived)) throw new IllegalStateException("Unable to spawn resurrected mob");

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(server);
        if (lightning != null) {
            lightning.moveTo(altar.getX() + 0.5D, altar.getY() + 2.0D, altar.getZ() + 0.5D);
            lightning.setVisualOnly(true);
            server.addFreshEntity(lightning);
        }
        snapshots.clearEntry(name);
        host.setRitualCenter(net.minecraft.world.item.ItemStack.EMPTY);
    }

    private static Mob degraded(ServerLevel level, Mob original, int crystalSize) {
        CompoundTag persistent = original.getPersistentData();
        int reanimations = persistent.getInt("Reanimations");
        boolean degrade = switch (reanimations) {
            case 4 -> level.random.nextFloat() < 0.90F;
            case 5 -> level.random.nextFloat() < 0.75F;
            case 6 -> level.random.nextFloat() < 0.60F;
            case 7 -> level.random.nextFloat() < 0.45F;
            case 8 -> level.random.nextFloat() < 0.30F;
            case 9 -> level.random.nextFloat() < 0.15F;
            case 10 -> true;
            default -> false;
        };
        if (!degrade) {
            persistent.putInt("Reanimations", reanimations + 1);
            return original;
        }
        EntityType<? extends Mob> type = switch (crystalSize) {
            case 1 -> LegacyEntities.GREATER_DREAD_SPAWN.get();
            case 2 -> LegacyEntities.LESSER_DREADBEAST.get();
            default -> LegacyEntities.DREAD_SPAWN.get();
        };
        Mob replacement = type.create(level);
        if (replacement == null) return original;
        replacement.setCustomName(original.getCustomName());
        MobSpawnCompat.finalizeTriggeredSpawn(level, replacement);
        return replacement;
    }

    private static int crystalSize(net.minecraft.world.item.ItemStack stack) {
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (path.startsWith("crystal_shard_")) return 0;
        if (path.startsWith("crystal_") && !path.endsWith("_cluster")) return 1;
        if (path.endsWith("_crystal_cluster")) return 2;
        return -1;
    }
}