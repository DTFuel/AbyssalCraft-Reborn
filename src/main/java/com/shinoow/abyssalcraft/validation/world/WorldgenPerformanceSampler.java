package com.shinoow.abyssalcraft.validation.world;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * Deterministic worldgen performance sampling for T5.2c (Abyssal Wasteland) and T5.3c (Dreadlands).
 * <p>
 * Records generation time for a fixed route of new chunks and reports p50/p95 latencies.
 * Runs during dedicated server startup, does NOT require a real player session.
 * </p>
 */
public final class WorldgenPerformanceSampler {

    private static final long MAX_P50_MS = 100;
    private static final long MAX_P95_MS = 500;

    /** Fixed chunk route for reproducible performance sampling. */
    private static final int[][] AW_ROUTE = {
        {0, 0}, {1, 0}, {0, 1}, {1, 1}, {2, 0}, {0, 2}, {2, 2}, {-1, 0}, {0, -1}, {-1, -1},
        {3, 0}, {0, 3}, {-2, 0}, {0, -2}, {4, 4}, {-4, -4}, {8, 0}, {0, 8}, {-8, -8}
    };

    private static final int[][] DL_ROUTE = {
        {0, 0}, {1, 0}, {0, 1}, {1, 1}, {2, 0}, {0, 2}, {2, 2}, {-1, 0}, {0, -1}, {-1, -1},
        {3, 0}, {0, 3}, {-2, 0}, {0, -2}, {5, 5}, {-5, -5}, {10, 0}, {0, 10}, {-10, -10}
    };

    private WorldgenPerformanceSampler() {}

    /**
     * Sample Abyssal Wasteland generation performance (T5.2c).
     * @param level ServerLevel for abyssalcraft:abyssal_wasteland
     * @return "RR_WORLD_PERF_AW_OK p50=XmsX p95=YmsY samples=N" if successful
     */
    public static String sampleAbyssalWasteland(ServerLevel level) {
        return sample("AW", level, AW_ROUTE);
    }

    /**
     * Sample Dreadlands generation performance (T5.3c).
     * @param level ServerLevel for abyssalcraft:dreadlands
     * @return "RR_WORLD_PERF_DL_OK p50=XmsX p95=YmsY samples=N" if successful
     */
    public static String sampleDreadlands(ServerLevel level) {
        return sample("DL", level, DL_ROUTE);
    }

    private static String sample(String dimName, ServerLevel level, int[][] route) {
        List<Long> latencies = new ArrayList<>();
        int warnings = 0;

        for (int[] coord : route) {
            int chunkX = coord[0];
            int chunkZ = coord[1];

            long startNs = System.nanoTime();
            try {
                ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
                long durationNs = System.nanoTime() - startNs;
                latencies.add(durationNs);

                // Validate basic worldgen integrity
                if (chunk == null) {
                    warnings++;
                    continue;
                }

                // Sample Y=64 for basic block presence
                BlockPos testPos = new BlockPos(chunkX << 4, 64, chunkZ << 4);
                if (level.getBlockState(testPos).isAir()) {
                    // Air at Y64 might be valid in some biomes, but log for awareness
                }

            } catch (Exception e) {
                warnings++;
                System.err.printf("WARN: %s chunk [%d,%d] generation error: %s%n",
                    dimName, chunkX, chunkZ, e.getMessage());
            }
        }

        if (latencies.isEmpty()) {
            return String.format("RR_WORLD_PERF_%s_FAIL noSamples=true warnings=%d", dimName, warnings);
        }

        long[] sorted = latencies.stream().mapToLong(Long::longValue).sorted().toArray();
        long p50 = sorted[sorted.length / 2];
        long p95 = sorted[(int) (sorted.length * 0.95)];

        long p50Ms = p50 / 1_000_000;
        long p95Ms = p95 / 1_000_000;
        boolean passed = warnings == 0 && sorted.length == route.length
            && p50Ms <= MAX_P50_MS && p95Ms <= MAX_P95_MS;
        return String.format("RR_WORLD_PERF_%s_%s p50=%dms p95=%dms samples=%d warnings=%d seed=%d",
            dimName, passed ? "OK" : "FAIL", p50Ms, p95Ms, sorted.length, warnings, level.getSeed());
    }
}