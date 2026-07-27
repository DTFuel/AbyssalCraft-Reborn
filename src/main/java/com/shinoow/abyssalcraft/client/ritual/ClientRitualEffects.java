package com.shinoow.abyssalcraft.client.ritual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.shinoow.abyssalcraft.registry.ModParticles;
import com.shinoow.abyssalcraft.system.ritual.RitualIngredient;
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/** Client-only lifetime and visual state for active altar ceremonies (RR-CLIENT-FX). */
public final class ClientRitualEffects {

    /** Faithful 1.12.2 ring pedestal offsets (mirrors {@code RitualAltarBlockEntity.PEDESTAL_OFFSETS}). */
    private static final BlockPos[] PEDESTAL_OFFSETS = {
        new BlockPos(-3, 0, 0), new BlockPos(0, 0, -3),
        new BlockPos(3, 0, 0), new BlockPos(0, 0, 3),
        new BlockPos(-2, 0, 2), new BlockPos(-2, 0, -2),
        new BlockPos(2, 0, 2), new BlockPos(2, 0, -2)
    };

    private static final Map<BlockPos, ActiveRitual> ACTIVE = new HashMap<>();

    private ClientRitualEffects() {}

    public static void start(BlockPos pos, String id, int sacrificeId, int durationTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || durationTicks < 1) return;
        ACTIVE.put(pos.immutable(), new ActiveRitual(level, id, sacrificeId,
            level.getGameTime() + Math.min(durationTicks, 20 * 60), displayOfferings(id)));
    }

    public static void finish(BlockPos pos, String id, String disruption, boolean failed) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        ACTIVE.remove(pos);
        if (level == null || minecraft.player == null
            || minecraft.player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 4096.0D) {
            return;
        }
        for (int index = 0; index < 36; index++) {
            double angle = index * Math.PI * 2.0D / 36.0D;
            double speed = failed ? 0.12D : 0.08D;
            level.addParticle(failed ? ParticleTypes.SMOKE : ParticleTypes.SOUL_FIRE_FLAME,
                pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                Math.cos(angle) * speed, level.random.nextDouble() * 0.12D, Math.sin(angle) * speed);
        }
        level.playLocalSound(pos, failed ? SoundEvents.WITHER_DEATH
            : SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F,
            failed ? 0.7F : 1.2F, false);
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.isPaused()) return;
        Iterator<Map.Entry<BlockPos, ActiveRitual>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, ActiveRitual> entry = iterator.next();
            ActiveRitual ritual = entry.getValue();
            if (ritual.level() != level || level.getGameTime() > ritual.endGameTime()) {
                iterator.remove();
                continue;
            }
            BlockPos pos = entry.getKey();
            if (minecraft.player == null
                || minecraft.player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 4096.0D) {
                continue;
            }
            double phase = level.getGameTime() * 0.16D + (ritual.id().hashCode() & 255) * 0.02D;
            for (int index = 0; index < 4; index++) {
                double angle = phase + index * Math.PI / 2.0D;
                double radius = 2.6D;
                level.addParticle(ModParticles.ABYSSAL_FX.get(),
                    pos.getX() + 0.5D + Math.cos(angle) * radius,
                    pos.getY() + 0.25D + 0.15D * Math.sin(phase * 0.5D),
                    pos.getZ() + 0.5D + Math.sin(angle) * radius,
                    -Math.cos(angle) * 0.35D, 0.08D, -Math.sin(angle) * 0.35D);
            }
            emitPedestals(level, pos, ritual);
            markSacrifice(level, pos, ritual.sacrificeId());
        }
    }

    private static void markSacrifice(ClientLevel level, BlockPos altar, int sacrificeId) {
        if (sacrificeId <= 0 || level.getGameTime() % 2 != 0) return;
        Entity sacrifice = level.getEntity(sacrificeId);
        if (sacrifice == null) return;
        double targetX = sacrifice.getX();
        double targetY = sacrifice.getY() + sacrifice.getBbHeight() * 0.5D;
        double targetZ = sacrifice.getZ();
        for (int index = 1; index <= 4; index++) {
            double progress = index / 5.0D;
            level.addParticle(ParticleTypes.ENCHANT,
                altar.getX() + 0.5D + (targetX - altar.getX() - 0.5D) * progress,
                altar.getY() + 1.0D + (targetY - altar.getY() - 1.0D) * progress,
                altar.getZ() + 0.5D + (targetZ - altar.getZ() - 0.5D) * progress,
                0.0D, 0.02D, 0.0D);
        }
    }

    private static void emitPedestals(ClientLevel level, BlockPos altar, ActiveRitual ritual) {
        List<ItemStack> offerings = ritual.offerings();
        for (int index = 0; index < PEDESTAL_OFFSETS.length; index++) {
            BlockPos pedestal = altar.offset(PEDESTAL_OFFSETS[index]);
            double px = pedestal.getX() + 0.5D;
            double py = pedestal.getY() + 1.0D;
            double pz = pedestal.getZ() + 0.5D;
            // Faithful pedestal fire: one blue flame + one smoke wisp each tick.
            level.addParticle(ModParticles.BLUE_FLAME.get(), px, py, pz, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0D, 0.0D, 0.0D);
            // Faithful ITEM ritual: an item-icon particle drifting from the pedestal toward the altar.
            ItemStack offering = index < offerings.size() ? offerings.get(index) : ItemStack.EMPTY;
            if (!offering.isEmpty() && level.getGameTime() % 3 == 0) {
                double vx = (altar.getX() + 0.5D - px) * 0.2D;
                double vz = (altar.getZ() + 0.5D - pz) * 0.2D;
                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, offering), px, py, pz, vx, 0.15D, vz);
            }
        }
    }

    /** Rebuild the eight-slot offering display from the ritual manifest (empty slots stay empty). */
    private static List<ItemStack> displayOfferings(String id) {
        RitualManifest manifest = RitualManifestCatalog.get(id);
        List<ItemStack> stacks = new ArrayList<>(RitualManifest.PEDESTAL_COUNT);
        if (manifest == null) {
            return stacks;
        }
        for (RitualIngredient ingredient : manifest.offeringLayout()) {
            stacks.add(ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.example());
        }
        return stacks;
    }

    private record ActiveRitual(ClientLevel level, String id, int sacrificeId, long endGameTime,
                                List<ItemStack> offerings) {}
}