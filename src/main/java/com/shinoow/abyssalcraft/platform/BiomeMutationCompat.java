package com.shinoow.abyssalcraft.platform;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;

/** Runtime biome-container rewrite and client synchronization shared by both loader nodes. */
public final class BiomeMutationCompat {

    private BiomeMutationCompat() {}

    public static boolean rewriteChunk(ServerLevel level, LevelChunk chunk,
                                       Function<ResourceKey<Biome>, ResourceKey<Biome>> mapping) {
        Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        boolean[] changed = {false};
        chunk.fillBiomesFromNoise((quartX, quartY, quartZ, sampler) -> {
            Holder<Biome> current = chunk.getNoiseBiome(quartX, quartY, quartZ);
            ResourceKey<Biome> currentKey = current.unwrapKey().orElse(null);
            ResourceKey<Biome> targetKey = currentKey == null ? null : mapping.apply(currentKey);
            if (targetKey == null || targetKey.equals(currentKey)) return current;
            changed[0] = true;
            return registry.getHolderOrThrow(targetKey);
        }, level.getChunkSource().randomState().sampler());
        if (changed[0]) {
            chunk.setUnsaved(true);
            level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
        }
        return changed[0];
    }

    /** Rewrite selected world quart columns throughout a chunk, retaining every unselected biome. */
    public static boolean rewriteQuartColumns(ServerLevel level, LevelChunk chunk,
                                              Set<Long> columns, ResourceKey<Biome> target) {
        return rewriteQuartColumns(level, chunk, columns, current -> true, target);
    }

    public static boolean rewriteQuartColumns(ServerLevel level, LevelChunk chunk,
                                              Set<Long> columns, Predicate<ResourceKey<Biome>> canReplace,
                                              ResourceKey<Biome> target) {
        if (columns.isEmpty()) return false;
        Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> replacement = registry.getHolderOrThrow(target);
        boolean[] changed = {false};
        chunk.fillBiomesFromNoise((quartX, quartY, quartZ, sampler) -> {
            Holder<Biome> current = chunk.getNoiseBiome(quartX, quartY, quartZ);
                ResourceKey<Biome> currentKey = current.unwrapKey().orElse(null);
                if (!columns.contains(quartColumn(quartX, quartZ)) || current.is(target)
                    || currentKey == null || !canReplace.test(currentKey)) return current;
            changed[0] = true;
            return replacement;
        }, level.getChunkSource().randomState().sampler());
        if (changed[0]) {
            chunk.setUnsaved(true);
            level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
        }
        return changed[0];
    }

    public static long quartColumn(int quartX, int quartZ) {
        return (long) quartX << 32 | quartZ & 0xFFFFFFFFL;
    }
}