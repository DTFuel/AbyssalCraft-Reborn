package com.shinoow.abyssalcraft.system.effect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.platform.BiomeMutationCompat;
import com.shinoow.abyssalcraft.world.ACDimensions;
import com.shinoow.abyssalcraft.world.darklands.DarklandsBiomes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;

/** Server-authoritative Dread Plague biome mutation, faithful to the 1.12.2 3x3 spread. */
public final class DreadPlagueSpread {

    private static final Set<ResourceKey<Biome>> IMMUNE_BIOMES = Set.of(
        DarklandsBiomes.DREADLANDS, DarklandsBiomes.DREADLANDS_FOREST,
        DarklandsBiomes.DREADLANDS_MOUNTAINS, DarklandsBiomes.DREADLANDS_OCEAN,
        DarklandsBiomes.PURGED);

    private DreadPlagueSpread() {}

    public static boolean shouldRun(boolean disabled, boolean hardcore, int amplifier,
                                    ResourceKey<Level> dimension, boolean serverThread, int tickCount) {
        return !disabled && (amplifier > 0 || hardcore) && serverThread && tickCount % 100 == 0
            && dimension != ACDimensions.DARK_REALM && dimension != ACDimensions.OMOTHOL;
    }

    public static boolean shouldRun(BooleanSupplier disabled, boolean hardcore, int amplifier,
                                    ResourceKey<Level> dimension, boolean serverThread, int tickCount) {
        return shouldRun(disabled.getAsBoolean(), hardcore, amplifier, dimension, serverThread, tickCount);
    }

    public static boolean canReplace(ResourceKey<Biome> biome) {
        return !IMMUNE_BIOMES.contains(biome);
    }

    public static void tick(ServerLevel level, BlockPos center, int amplifier, int tickCount) {
        if (!level.getServer().isSameThread()) return;
        if (!shouldRun(ACConfig.no_dreadlands_spread::get, ACConfig.hardcoreMode.get(), amplifier,
                level.dimension(), true, tickCount)) return;
        Map<LevelChunk, Set<Long>> columnsByChunk = new HashMap<>();
        for (int x = center.getX() - 1; x <= center.getX() + 1; x++) {
            for (int z = center.getZ() - 1; z <= center.getZ() + 1; z++) {
                LevelChunk chunk = level.getChunkAt(new BlockPos(x, center.getY(), z));
                columnsByChunk.computeIfAbsent(chunk, ignored -> new HashSet<>())
                    .add(BiomeMutationCompat.quartColumn(x >> 2, z >> 2));
            }
        }
        for (Map.Entry<LevelChunk, Set<Long>> entry : columnsByChunk.entrySet()) {
            BiomeMutationCompat.rewriteQuartColumns(level, entry.getKey(), entry.getValue(),
                DreadPlagueSpread::canReplace, DarklandsBiomes.DREADLANDS);
        }
    }
}