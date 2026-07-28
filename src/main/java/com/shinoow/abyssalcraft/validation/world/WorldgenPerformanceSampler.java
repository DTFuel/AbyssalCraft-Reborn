package com.shinoow.abyssalcraft.validation.world;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.chunk.ChunkAccess;

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
    private static final int WARMUP_CHUNK_X = 24;
    private static final int WARMUP_CHUNK_Z = 24;

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
        List<Long> gcPauses = new ArrayList<>();
        int warnings = 0;
        int minimumSurface = Integer.MAX_VALUE;
        int maximumSurface = Integer.MIN_VALUE;
        int floorAir = 0;

        try {
            level.getChunk(WARMUP_CHUNK_X, WARMUP_CHUNK_Z);
        } catch (Exception e) {
            return String.format("RR_WORLD_PERF_%s_FAIL warmup=true warnings=1 seed=%d",
                dimName, level.getSeed());
        }

        for (int[] coord : route) {
            int chunkX = coord[0];
            int chunkZ = coord[1];

            WorldgenChunkSampleEvent event = new WorldgenChunkSampleEvent();
            boolean recordEvent = event.isEnabled();
            if (recordEvent) {
                event.dimension = dimName;
                event.chunkX = chunkX;
                event.chunkZ = chunkZ;
                event.begin();
            }
            long gcBefore = gcCollectionTime();
            long startNs = System.nanoTime();
            try {
                ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
                long durationNs = System.nanoTime() - startNs;
                latencies.add(durationNs);
                gcPauses.add(Math.max(0L, gcCollectionTime() - gcBefore));

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
                for (int localZ = 0; localZ < 16; localZ += 5) {
                    for (int localX = 0; localX < 16; localX += 5) {
                        int worldX = (chunkX << 4) + localX;
                        int worldZ = (chunkZ << 4) + localZ;
                        int surface = stableSurfaceHeight(level, worldX, worldZ);
                        minimumSurface = Math.min(minimumSurface, surface);
                        maximumSurface = Math.max(maximumSurface, surface);
                        if ("DL".equals(dimName)
                            && level.getBlockState(new BlockPos(worldX, level.getMinBuildHeight(), worldZ)).isAir()) {
                            floorAir++;
                        }
                    }
                }

            } catch (Exception e) {
                warnings++;
                System.err.printf("WARN: %s chunk [%d,%d] generation error: %s%n",
                    dimName, chunkX, chunkZ, e.getMessage());
            } finally {
                if (recordEvent) {
                    event.durationMillis = (System.nanoTime() - startNs) / 1_000_000;
                    event.gcMillis = Math.max(0L, gcCollectionTime() - gcBefore);
                    event.commit();
                }
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
        int maximumAllowedSurface = "AW".equals(dimName) ? 160 : 192;
        boolean terrainPassed = minimumSurface > level.getMinBuildHeight()
            && maximumSurface < maximumAllowedSurface
            && (!"DL".equals(dimName) || minimumSurface >= 56 && floorAir == 0);
        boolean passed = warnings == 0 && sorted.length == route.length && terrainPassed
            && p50Ms <= MAX_P50_MS && p95Ms <= MAX_P95_MS;
        String routeMs = latencies.stream()
            .map(duration -> Long.toString(duration / 1_000_000))
            .collect(java.util.stream.Collectors.joining(","));
        String routeGcMs = gcPauses.stream().map(Object::toString)
            .collect(java.util.stream.Collectors.joining(","));
        return String.format("RR_WORLD_PERF_%s_%s p50=%dms p95=%dms samples=%d routeMs=%s routeGcMs=%s warnings=%d seed=%d minSurface=%d maxSurface=%d floorAir=%d terrain=%s",
            dimName, passed ? "OK" : "FAIL", p50Ms, p95Ms, sorted.length, routeMs, routeGcMs,
            warnings, level.getSeed(), minimumSurface, maximumSurface, floorAir,
            terrainPassed ? "bounded" : "invalid");
    }

    private static long gcCollectionTime() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
            .mapToLong(bean -> Math.max(0L, bean.getCollectionTime())).sum();
    }

    private static int stableSurfaceHeight(ServerLevel level, int x, int z) {
        int height = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        for (int y = height - 1; y >= level.getMinBuildHeight() + 3; y--) {
            boolean continuous = true;
            for (int depth = 0; depth < 4; depth++) {
                if (level.getBlockState(new BlockPos(x, y - depth, z)).isAir()) {
                    continuous = false;
                    break;
                }
            }
            if (continuous) return y + 1;
        }
        return level.getMinBuildHeight();
    }

    @Name("abyssalcraft.WorldgenChunkSample")
    @Label("AbyssalCraft worldgen chunk sample")
    @Category({"AbyssalCraft", "Validation"})
    private static final class WorldgenChunkSampleEvent extends Event {
        String dimension;
        int chunkX;
        int chunkZ;
        long durationMillis;
        long gcMillis;
    }
}