package com.shinoow.abyssalcraft.client.network;

import java.util.UUID;

import com.shinoow.abyssalcraft.content.entity.demon.AnimalKind;
import com.shinoow.abyssalcraft.content.entity.demon.EvilAnimal;
import com.shinoow.abyssalcraft.registry.ModParticles;
import com.shinoow.abyssalcraft.system.energy.disruption.Disruption;
import com.shinoow.abyssalcraft.system.energy.disruption.DisruptionHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Client-only side effects for legacy network messages. */
public final class ClientNetworkEffects {

    private static final int MAX_ROUTES = 32;
    private static final int MAX_POINTS_PER_ROUTE = 64;
    private static final int MAX_SEGMENTS = 256;

    private ClientNetworkEffects() {}

    public static void updateWindow(int windowId, int property, int value) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.containerMenu == null
            || player.containerMenu.containerId != windowId || property < 0 || property >= 64) return;
        try {
            player.containerMenu.setData(property, value);
        } catch (IndexOutOfBoundsException ignored) {
            // Stale legacy packet for a menu with fewer data slots.
        }
    }

    public static void refreshBiome(int x, int z, boolean batched) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.levelRenderer == null) return;
        if (!batched || x % 14 == 0 || z % 14 == 0) {
            minecraft.levelRenderer.setBlocksDirty(x - 7, minecraft.level.getMinBuildHeight(), z - 7,
                x + 7, minecraft.level.getMaxBuildHeight(), z + 7);
        }
    }

    public static void disruption(String deity, String name, BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Disruption disruption = DisruptionHandler.instance().find(name);
        if (level == null || minecraft.player == null || disruption == null
            || minecraft.player.distanceToSqr(Vec3.atCenterOf(pos)) > 4096.0D) return;
        minecraft.player.displayClientMessage(Component.translatable(
            "message.abyssalcraft.disruption", Component.translatable(disruption.translationKey())), false);
        for (int index = 0; index < 24; index++) {
            double angle = index * Math.PI * 2.0D / 24.0D;
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 1.0D,
                pos.getZ() + 0.5D, Math.cos(angle) * 0.08D, 0.04D, Math.sin(angle) * 0.08D);
        }
        level.playLocalSound(pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS,
            0.35F, deity.isEmpty() ? 0.8F : 0.65F, false);
    }

    public static void evilSheep(UUID playerId, String playerName, int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || playerName.length() > 64) return;
        Entity entity = level.getEntity(entityId);
        if (entity instanceof EvilAnimal animal && animal.kind() == AnimalKind.SHEEP) {
            animal.setKilledPlayer(playerId, playerName);
        }
    }

    public static void peStream(BlockPos from, BlockPos to) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || from.distManhattan(to) > 256) return;
        Vec3 delta = new Vec3(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());
        double distance = delta.length();
        if (distance == 0.0D) return;
        Vec3 direction = delta.scale(1.0D / distance);
        int increment = switch (minecraft.options.particles().get()) {
            case ALL -> 1;
            case DECREASED -> 2;
            case MINIMAL -> 3;
        };
        for (int index = 0; index < distance * 15.0D; index += increment) {
            double offset = index / 15.0D;
            level.addParticle(ModParticles.PE_STREAM.get(),
                from.getX() + direction.x * offset + 0.5D,
                from.getY() + direction.y * offset + 0.5D,
                from.getZ() + direction.z * offset + 0.5D,
                direction.x * 0.1D, 0.15D, direction.z * 0.1D);
        }
    }

    public static int peStreamSampleCount(BlockPos from, BlockPos to, int increment) {
        if (increment < 1) return 0;
        double distance = Math.sqrt(to.distSqr(from));
        int samples = 0;
        for (int index = 0; index < distance * 15.0D; index += increment) samples++;
        return samples;
    }

    public static void displayRoutes(CompoundTag root) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || root == null || !root.contains("Routes", Tag.TAG_LIST)) return;
        ListTag routes = root.getList("Routes", Tag.TAG_LIST);
        int segments = 0;
        for (int routeIndex = 0; routeIndex < Math.min(routes.size(), MAX_ROUTES); routeIndex++) {
            if (!(routes.get(routeIndex) instanceof ListTag route)) continue;
            BlockPos previous = null;
            for (int point = 0; point < Math.min(route.size(), MAX_POINTS_PER_ROUTE); point++) {
                if (!(route.get(point) instanceof net.minecraft.nbt.NumericTag number)) continue;
                BlockPos current = BlockPos.of(number.getAsLong());
                if (previous != null && ++segments <= MAX_SEGMENTS && previous.distManhattan(current) <= 256) {
                    spawnLine(level, Vec3.atCenterOf(previous), Vec3.atCenterOf(current), true);
                }
                previous = current;
            }
            if (segments >= MAX_SEGMENTS) break;
        }
    }

    private static void spawnLine(ClientLevel level, Vec3 from, Vec3 to, boolean route) {
        Vec3 delta = to.subtract(from);
        int points = Math.max(2, Math.min(24, (int) Math.ceil(delta.length() * 2.0D)));
        for (int index = 0; index <= points; index++) {
            Vec3 point = from.add(delta.scale(index / (double) points));
            level.addParticle(route ? ParticleTypes.WITCH : ModParticles.ABYSSAL_FX.get(),
                point.x, point.y, point.z, delta.x * 0.005D, 0.03D, delta.z * 0.005D);
        }
    }
}